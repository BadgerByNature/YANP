/*
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
 *
 * Copyright (c) 2026 YANP: You Are Not Prepared
 * See CONTRIBUTORS.md for further Copyright information
 */
package com.woodlands.yanp.auth

import com.woodlands.yanp.auth.constant.AuthStatus
import groovy.util.logging.Slf4j
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

@Slf4j
class AuthChannelIdleHandler extends ChannelInboundHandlerAdapter {

    private static final int DURATION_MULTIPLIER = 2

    int idleDuringAuthCount = 0

    @Override
    void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // Close the connection when it is detected as being idle
        if (evt instanceof IdleStateEvent && ((IdleStateEvent)evt).state() == IdleState.ALL_IDLE) {

            // Don't close on first idle if we're in the middle of giving proof
            // user could be looking up their authenticator code
            def statusKey = ctx.channel().attr(AuthAttributeKey.STATUS)
            if (statusKey && statusKey.get() == AuthStatus.LOGIN_PROOF) {
                // Extra idle is a multiplier of the defined idle duration.
                // Each time we don't close the channel allows one extra full duration to pass by
                if (++idleDuringAuthCount >= DURATION_MULTIPLIER) {
                    log.info("Channel connection to ${ctx.channel().remoteAddress()} has been detected as idle - closing")
                    ctx.close()
                    return
                }

                ctx.fireUserEventTriggered(evt)
                return
            }

            log.info("Channel connection to ${ctx.channel().remoteAddress()} has been detected as idle - closing")
            ctx.close()
        } else {
            ctx.fireUserEventTriggered(evt)
        }
    }

}
