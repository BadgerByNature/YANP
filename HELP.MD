# So how's this all work then?

First, we set up a server called Netty to listen for incoming connections 
[here](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/AuthServer.groovy).
Inside this class you can see we set up a channel pipeline with a couple of handlers. Ignore the IdleStateHandler
for now and take a look at the "decoder" handler: 
[AuthByteToMessageDecoderService](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/decode/AuthByteToMessageDecoderService.groovy).

The AuthByteToMessageDecoderService receives packets from the clients and determines which 
[AuthCommand](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/AuthCommand.groovy) the client is sending.
It uses the first byte to identify the AuthCommand we're receiving and finds an [AuthCommandDecoder](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/AuthCommandDecoder.groovy)
that can handle that specific AuthCommand. This decoder determines if the command is correctly-formatted and, if so,
converts it into the matching [AuthMessage](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/message/AuthMessage.groovy).

For the example, let's look at the very first AuthCommand a client sends, which is a [RequestChallenge](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/message/RequestChallengeMessage.groovy) 
message - specifically a LoginRequestChallenge. 

1. This message comes in as a CMD_AUTH_REQUEST_LOGIN_CHALLENGE AuthCommand with a Command byte of 0x00.
The code determines this is decoded by LoginRequestChallengeDecoder which offloads most of the work to its parent class,
[RequestChallengeDecoder](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/decode/RequestChallengeDecoder.groovy). (This is because Reconnect Challenges are very similar, so
this code is shared)
2. [RequestChallengeDecoder](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/decode/RequestChallengeDecoder.groovy)
verifies all the expected fields, and converts the data into a [RequestChallengeMessage](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/message/RequestChallengeMessage.groovy) so that
we can start working in Objects instead of bytes. This completes the "AuthByteToMessage" workflow.

Now that we have returned an AuthMessage, the next Netty handler kicks in, which is [AuthChannelInboundHandler](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/AuthChannelInboundHandler.groovy).
All this handler really does at this point is determine which AuthMessageHandler to call. In our example, we proceed
to [LoginRequestChallengeMessageHandler](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/message/handler/LoginRequestChallengeMessageHandler.groovy).

[LoginRequestChallengeMessageHandler](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/message/handler/LoginRequestChallengeMessageHandler.groovy)
is responsible for generating the requested Challenge. The client is asking for us to provide a randomly-generated Challenge
that we can both do some math against to authenticate without ever sending a password over the wire and without even storing
that password in the database. That's a whole other topic. With the ChallengeRequest we:
1. Validate that the account exists, and has been set up in a way to be able to verify authentication
2. Check to see if the account is banned in any way (permanent or temporary) and respond appropriately
3. Generate a Random Challenge and write the associated values to a byte array.
4. Wrap that byte array in a [ByteBufWowPacket](https://github.com/BadgerByNature/YANP/blob/main/shared/src/main/groovy/com/yanp/shared/network/ByteBufWowPacket.groovy)

We're almost done with one round-trip message! The ByteBufWowPacket we wrote goes back into Netty and hits our
[AuthResponseEncoder](https://github.com/BadgerByNature/YANP/blob/main/auth/src/main/groovy/com/yanp/auth/encode/AuthResponseEncoder.groovy) which
writes the message back out to the client via Netty.

From there, several more round-trip messages that proceed along the same path. Each AuthCommand gets Decoded into a Message,
goes through its respective Handler, and gets the result gets Encoded back to the client.

The normal flow is: LoginRequestChallenge -> LoginProof -> RealmList