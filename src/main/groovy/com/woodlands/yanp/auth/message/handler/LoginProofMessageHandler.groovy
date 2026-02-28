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
package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthAttributeKey
import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthResult
import com.woodlands.yanp.auth.constant.AuthStatus
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.LoginProofMessage
import com.woodlands.yanp.auth.service.AccountService
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.data.PacketDataWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.bouncycastle.util.BigIntegers
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginProofMessageHandler implements AuthMessageHandler {

    final AccountService accountService

    LoginProofMessageHandler(AccountService accountService) {
        this.accountService = accountService
    }

    @Override
    boolean handles(AuthMessage message) {
        message instanceof LoginProofMessage
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling LoginProofMessage')
        LoginProofMessage proofMessage = (LoginProofMessage)message

        ByteArrayOutputStream payload = new ByteArrayOutputStream()
        populateResponse(ch, proofMessage, payload)

        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_LOGIN_PROOF.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    void populateResponse(Channel channel, LoginProofMessage message, ByteArrayOutputStream payload) {
        def srpServer = channel.attr(AuthAttributeKey.SRP_ATTRIBUTE).get()

        // This should only happen if using a hacked client or direct socket connection to attempt to bypass security
        // If you went through the LoginChallenge already this will be here
        if (srpServer == null) {
            log.error("LoginProof received but SRP is not set on channel")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            payload.writeBytes([0x00, 0x00] as byte) // Comment in ACore: "LoginFlags, 1 has account message" - I don't know what this means, but I do see CMangos sending '3' in this field for invalid PIN
            return
        }

        // TODO Handle security tokens etc here
        // TODO Verify client version - ACore and CMangos both do this here, why don't they do it earlier on Challenge message?

        srpServer.calculateSecret(message.A)
        BigInteger K = srpServer.calculateSessionKey()
        if (!srpServer.verifyClientEvidenceMessage(message.M1)) {
            log.error("LoginProof failed")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            payload.write(0)
            payload.write(0)
            return
        }
        BigInteger M2 = srpServer.calculateServerEvidenceMessage()

        def account = channel.attr(AuthAttributeKey.ACCOUNT).get()
        account.sessionKey = BigIntegers.asUnsignedByteArray(K).encodeHex().toString()
        accountService.save(account)

        // TODO Set os, locale, failed_logins, platform as well

        // TODO Verify version via CRC Hash

        channel.attr(AuthAttributeKey.STATUS).set(AuthStatus.AUTHED)

        payload.write(AuthResult.WOW_SUCCESS.code)
        def writer = new PacketDataWriter()
        writer.write(BitUtil.toByteArray(M2, 20))
        writer.writeIntLE(0x00800000) // All the cores use this. Acore labels it "Pro pass (arena tournament)"
        writer.writeIntLE(0) // Survey Id
        writer.writeShortLE(0) // Login Flags - Per ACore and CMangos: "0x01 has account message"
        payload.writeBytes(writer.getBytes())
    }

    static class LoginProofResponse {
        BigInteger sessionKey
        BigInteger M2
    }
}
