package com.woodlands.yanp.auth

import com.woodlands.yanp.auth.message.AuthMessage
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
        log.debug('Auth Inbound Channel Registered')
    }

    @Override
    final void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("IoSession opened with ${ctx.channel().remoteAddress()}")
    }

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof AuthMessage) {
//            new LoginRequestMessageHandler().handle(
//                    (AuthMessage)msg, ctx.channel()
//            )
        } else if (msg instanceof ByteBuf) {
            ((ByteBuf) msg).release()
        }
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }
}
