/*
 * Java World of Warcraft Emulation Project
 * Copyright (C) 2015-2020 JavaWoW
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
* Copyright (c) 2026 YANP: You Are Not Prepared
* See CONTRIBUTORS.md for further Copyright information
*/
package com.woodlands.yanp.auth

import com.woodlands.yanp.auth.constant.AuthStatus
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.handler.AuthMessageHandler
import groovy.util.logging.Slf4j
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
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
        log.debug("Channel registered: ${ctx.channel().remoteAddress()}")
    }

    @Override
    final void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel active: ${ctx.channel().remoteAddress()}")
        // Automatically set as 'Challenge' when first connecting - could be LogonChallenge or ReconnectChallenge
        ctx.channel().attr(AuthAttributeKey.STATUS).set(AuthStatus.CHALLENGE)
    }

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof AuthMessage) {
            // This should be guaranteed assuming you don't add an AuthMessage without also adding a handler for it
            authMessageHandlers.find {
                it.handles(msg)
            }.handle(msg, ctx.channel())
        }
    }

    @Override
    void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel inactive: ${ctx.channel().remoteAddress()}")
    }

    @Override
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel unregistered: ${ctx.channel().remoteAddress()}")
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        log.warn("Channel connection to ${ctx.channel().remoteAddress()} threw an exception - closing")
        cause.printStackTrace()
        ctx.close()
    }

    @Override
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Close the connection when it is detected as being idle
        if (evt instanceof IdleStateEvent && ((IdleStateEvent)evt).state() == IdleState.ALL_IDLE) {
            log.info("Channel connection to ${ctx.channel().remoteAddress()} has been detected as idle - closing")
            ctx.close()
        } else {
            ctx.fireUserEventTriggered(evt)
        }
    }
}
