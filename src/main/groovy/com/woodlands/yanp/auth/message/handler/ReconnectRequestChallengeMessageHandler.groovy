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
package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthAttributeKey
import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthResult
import com.woodlands.yanp.auth.constant.AuthStatus
import com.woodlands.yanp.auth.constant.BanStatus
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.RequestChallengeMessage
import com.woodlands.yanp.common.service.AccountService
import com.woodlands.yanp.auth.service.BanService
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.data.PacketDataWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import com.woodlands.yanp.common.srp.WowSrp6Server
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

import java.security.SecureRandom

@Slf4j
@Service
class ReconnectRequestChallengeMessageHandler implements AuthMessageHandler {

    public static final byte[] VERSION_CHALLENGE =
            [ 0xBA, 0xA3, 0x1E, 0x99, 0xA0, 0x0B, 0x21, 0x57, 0xFC, 0x37, 0x3F, 0xB3, 0x69, 0xCD, 0xD2, 0xF1 ]

    final AccountService accountService
    final BanService banService
    final SecureRandom secureRandom

    ReconnectRequestChallengeMessageHandler(AccountService accountService, BanService banService, SecureRandom secureRandom) {
        this.accountService = accountService
        this.banService = banService
        this.secureRandom = secureRandom
    }

    @Override
    boolean handles(AuthMessage message) {
        return message instanceof RequestChallengeMessage && message.command == AuthCommand.CMD_AUTH_RECONNECT_CHALLENGE
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling ReconnectRequestMessage')
        RequestChallengeMessage requestMessage = (RequestChallengeMessage)message

        ch.attr(AuthAttributeKey.BUILD).set(requestMessage.build)
        ch.attr(AuthAttributeKey.OS).set(requestMessage.os)

        // Payload includes everything after the opCode
        ByteArrayOutputStream payload = new ByteArrayOutputStream()

        // Offload work into another method so we can return out of it on fail conditions and
        // simplify our write logic here
        populateResponse(ch, requestMessage, payload)
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_RECONNECT_CHALLENGE.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    void populateResponse(Channel ch, RequestChallengeMessage message, ByteArrayOutputStream payload) {

        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.CLOSED)

        String remoteIp = ch.remoteAddress().toString().replace('/', '').split(':')[0]
        if (banService.isIpBanned(remoteIp)) {
            log.info("Remote IP attempted to connect but is BANNED: remoteAddress=${ch.remoteAddress().toString()}")
            payload.write(AuthResult.WOW_FAIL_FAIL_NO_ACCESS.code)
            return
        }

        def account = accountService.getAccount(message.accountName)
        if (account == null) {
            // Just log as debug, this could happen all the time by people mistyping account names
            log.debug("Account not found during login: $message.accountName")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            return
        }

        def banStatus = banService.getAccountBanStatus(account.id)
        log.debug("Account login ban status: accountId=$account.id, banStatus=$banStatus")
        switch (banStatus) {
            case BanStatus.TEMPORARY:
                log.debug("Banned Account attempted to login: accountId=$account.id, accountName=$message.accountName, banStatus:$banStatus")
                payload.write(AuthResult.WOW_FAIL_BANNED.code)
                return
            case BanStatus.PERMANENT:
                log.debug("Banned Account attempted to login: accountId=$account.id, accountName=$message.accountName, banStatus:$banStatus")
                payload.write(AuthResult.WOW_FAIL_SUSPENDED.code)
                return
            default:
                break
        }

        def sessionKey = new BigInteger(1, account.sessionKey.decodeHex())
        def srpServer = new WowSrp6Server()
        srpServer.setSessionKey(sessionKey)

        ch.attr(AuthAttributeKey.SRP_ATTRIBUTE).set(srpServer)
        ch.attr(AuthAttributeKey.ACCOUNT).set(account)
        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.RECON_PROOF)

        def randBytes = new byte[16]
        secureRandom.nextBytes(randBytes)

        // Our random reconnect proof salt
        BigInteger reconnectProof = new BigInteger(randBytes)
        ch.attr(AuthAttributeKey.RECON_PROOF).set(reconnectProof)

        def writer = new PacketDataWriter()
        writer.write(BitUtil.toLEByteArray(reconnectProof, 16))
        writer.write(VERSION_CHALLENGE)

        payload.write(AuthResult.WOW_SUCCESS.code)
        payload.writeBytes(writer.bytes)
    }
}
