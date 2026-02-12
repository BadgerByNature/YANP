package com.woodlands.yanp.auth

import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

/**
 * The primary handler invoked from connections made to the AuthServer
 */
@Slf4j
class AuthChannelInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx)
        log.info('Auth Inbound Channel Registered')
    }

    @Override
    final void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("IoSession opened with ${ctx.channel().remoteAddress()}")
//        Random r = new Random(1337)
//        // Send Authentication Challenge Packet:
//        LittleEndianWriterStream lews = new LittleEndianWriterStream(0x01EC);
//        lews.writeInt(1); // ?
//        byte[] authSeed = new byte[4];
//        byte[] seed1 = new byte[16];
//        byte[] seed2 = new byte[16];
//        r.nextBytes(authSeed);
//        r.nextBytes(seed1);
//        r.nextBytes(seed2);
//        System.out.println("auth seed: " + Arrays.toString(authSeed));
//        lews.write(authSeed); // auth seed
//        lews.write(seed1); // ?
//        lews.write(seed2); // ?
//        ctx.channel().writeAndFlush(lews.getPacket());
//        LOGGER.debug("Sent Authentication Challenge");
    }

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Discard the received data silently.
        ((ByteBuf) msg).release()
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }
}
