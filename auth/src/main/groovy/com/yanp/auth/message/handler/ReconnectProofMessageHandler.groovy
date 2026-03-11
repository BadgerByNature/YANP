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
package com.yanp.auth.message.handler

import com.yanp.auth.AuthAttributeKey
import com.yanp.auth.AuthCommand
import com.yanp.auth.AuthResult
import com.yanp.auth.constant.AuthStatus
import com.yanp.auth.message.AuthMessage
import com.yanp.auth.message.ReconnectProofMessage
import com.yanp.auth.service.VersionVerificationService
import com.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class ReconnectProofMessageHandler implements AuthMessageHandler {

    final VersionVerificationService versionVerificationService

    ReconnectProofMessageHandler(VersionVerificationService versionVerificationService) {
        this.versionVerificationService = versionVerificationService
    }

    @Override
    boolean handles(AuthMessage message) {
        message instanceof ReconnectProofMessage
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        def proofMessage = (ReconnectProofMessage) message
        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.CLOSED)

        ByteArrayOutputStream payload = new ByteArrayOutputStream()

        populateResponse(ch, proofMessage, payload)
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_RECONNECT_PROOF.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    void populateResponse(Channel ch, ReconnectProofMessage message, ByteArrayOutputStream payload) {
        def srpServer = ch.attr(AuthAttributeKey.SRP_ATTRIBUTE).get()
        def reconProof = ch.attr(AuthAttributeKey.RECON_PROOF).get()
        def account = ch.attr(AuthAttributeKey.ACCOUNT).get()

        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.CLOSED)

        if (!srpServer || !reconProof || !account) {
            payload.write(AuthResult.WOW_FAIL_VERSION_INVALID.code)
            return
        }

        def reconnectProofHash = srpServer.calculateReconnectProof(message.R1, account.username.getBytes(), reconProof)
        if (reconnectProofHash != message.R2) {
            payload.write(AuthResult.WOW_FAIL_VERSION_INVALID.code)
            return
        }

        Integer clientBuild = ch.attr(AuthAttributeKey.BUILD).get()
        String os = ch.attr(AuthAttributeKey.OS).get()
        if (!versionVerificationService.verifyVersion(message.R1, message.R3, clientBuild, os, true)) {
            // Client is found to be invalid via CRC Hash check validation
            log.error('User tried to reconnect with invalid client')
            payload.write(AuthResult.WOW_FAIL_VERSION_INVALID.code)
            return
        }

        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.AUTHED)

        payload.write(AuthResult.WOW_SUCCESS.code)
        payload.write(0)
        payload.write(0)

        log.debug('Reconnect Proof successful')
    }
}
