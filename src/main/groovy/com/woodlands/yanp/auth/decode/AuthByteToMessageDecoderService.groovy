package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.AuthMessage
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.springframework.stereotype.Service

/**
 * Selects the proper decoder based on the instruction/command contained in the first byte
 * and uses it to decode the ByteBuf into a standard Object for easier manipulation in the
 * MessageHandler
 */
@Slf4j
@Service
class AuthByteToMessageDecoderService extends ByteToMessageDecoder {

    List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders

    AuthByteToMessageDecoderService(List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders) {
        this.authCommandDecoders = authCommandDecoders
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> listOut) throws Exception {
        // One byte should be all we need to determine the AuthCommand
        if (byteBuf.readableBytes() < 1) {
            log.warn('Decode called with 0 bytes')
            return
        }

        // Marks the reader index in case it needs to be reset - when could that happen?
        byteBuf.markReaderIndex()

        // First byte should represent the command we need to process
        byte commandCode = byteBuf.readByte()

        AuthCommand command = AuthCommand.fromCode(commandCode)
        def decoder = authCommandDecoders.find { it -> it.handles(command) }
        if (decoder != null) {
            // TODO Handle failure to decode
            // Wrap in DecodeResult<T> with success/fail and also object?
            // Return an error result object and pass it down to the handler?
            listOut.add(decoder.decode(byteBuf))
            // TODO Do we need to release here?
        } else {
            log.error("No decoder found for packet with command code ${Integer.toHexString(commandCode)}")
            byteBuf.resetReaderIndex()
        }
    }
}
