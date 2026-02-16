package com.woodlands.yanp.auth

import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.handler.AuthMessageHandler
import groovy.util.logging.Slf4j
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.springframework.stereotype.Service

/**
 * The primary handler invoked from connections made to the AuthServer
 */
@Slf4j
@Service
@Sharable
class AuthChannelInboundHandler extends ChannelInboundHandlerAdapter {

    List<AuthMessageHandler> authMessageHandlers

    AuthChannelInboundHandler(List<AuthMessageHandler> authMessageHandlers) {
        this.authMessageHandlers = authMessageHandlers
    }

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
            // This should be guaranteed assuming you don't add an AuthMessage without also adding a handler for it
            authMessageHandlers.find {
                it.handles(msg)
            }.handle(msg, ctx.channel())
        }
        // TODO Do we need to release the underlying ByteBuf at this point? At any point?
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace()
        ctx.close()
    }
}
