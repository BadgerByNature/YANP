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
import com.woodlands.yanp.auth.constant.BanStatus
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.RequestChallengeMessage
import com.woodlands.yanp.auth.service.AccountService
import com.woodlands.yanp.auth.service.BanService
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.data.PacketDataWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import com.woodlands.yanp.common.srp.WowSrp6Server

import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.params.SRP6GroupParameters
import org.springframework.stereotype.Service

import java.security.SecureRandom

@Slf4j
@Service
class LoginRequestChallengeMessageHandler implements AuthMessageHandler {

    /** The 'Safe Prime'' */
    private static final BigInteger N = new BigInteger('894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7', 16)
    /** The 'Generator' of the multiplicative group */
    private static final BigInteger g = BigInteger.valueOf(7)
    /** Just a holder for the Safe Prime and Generator */
    static final SRP6GroupParameters srpParams = new SRP6GroupParameters(N, g)
    /** Byte array that somehow represents a version challenge. Hard-coded this way in every *Mangos impl and AzerothCore */
    public static final byte[] VERSION_CHALLENGE =
            [ 0xBA, 0xA3, 0x1E, 0x99, 0xA0, 0x0B, 0x21, 0x57, 0xFC, 0x37, 0x3F, 0xB3, 0x69, 0xCD, 0xD2, 0xF1 ]

    final AccountService accountService
    final BanService banService
    final SecureRandom secureRandom

    LoginRequestChallengeMessageHandler(AccountService accountService, BanService banService, SecureRandom secureRandom) {
        this.accountService = accountService
        this.banService = banService
        this.secureRandom = secureRandom
    }

    @Override
    boolean handles(AuthMessage message) {
        return message instanceof RequestChallengeMessage && message.command == AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling LoginRequestMessage')
        RequestChallengeMessage requestMessage = (RequestChallengeMessage)message

        ch.attr(AuthAttributeKey.BUILD).set(requestMessage.build)

        // Payload includes everything after the opCode
        ByteArrayOutputStream payload = new ByteArrayOutputStream()

        // Offload work into another method so we can return out of it on fail conditions and
        // simplify our write logic here
        populateResponse(ch, requestMessage, payload)
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    void populateResponse(Channel ch, RequestChallengeMessage message, ByteArrayOutputStream payload) {

        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.CLOSED)
        payload.write(0) // Unknown Use - just a spacer?

        String remoteIp = ch.remoteAddress().toString().replace('/', '').split(':')[0]
        if (banService.isIpBanned(remoteIp)) {
            log.debug("Remote IP attempted to connect but is BANNED") // TODO Log Remote Ip on release
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

        if (account.s == null || account.v == null) {
            log.error("Account has broken s/v values in database and cannot login: accountId=$account.id, accountName=$message.accountName")
            payload.write(AuthResult.WOW_FAIL_FAIL_NO_ACCESS.code)
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

        def verifier = new BigInteger(1, account.v.decodeHex())
        def salt = account.s.decodeHex()
        def I = account.username.getBytes()
        def securityFlags = (byte)0x00

        WowSrp6Server srp6Server = WowSrp6Server.init(srpParams, verifier, I, salt, new SHA1Digest(), secureRandom)
        BigInteger B = srp6Server.generateServerCredentials()

        ch.attr(AuthAttributeKey.SRP_ATTRIBUTE).set(srp6Server)

        def writer = new PacketDataWriter()
        writer.write(BitUtil.toLEByteArray(B, 32))
        writer.writeByte(1) // Hard-coded in every server Impl I've seen
        writer.write(g.toByteArray()) // This 'BigInteger' is only a single byte - 0x07
        byte[] N_bytes = BitUtil.toLEByteArray(N, 32)
        writer.writeByte(N_bytes.length) // Should always be 32 if we are enforcing min size, some cores hard-code it
        writer.write(N_bytes)
        writer.write(BitUtil.reverse(salt))
        writer.write(VERSION_CHALLENGE)
        writer.write(securityFlags) // security flags
        if ((securityFlags & 0x1) == 0x1) {
            writer.writeIntLE(0)
            writer.writeLongLE(0)
            writer.writeLongLE(0)
        }
        if ((securityFlags & 0x2) == 0x2) {
            writer.writeByte(0)
            writer.writeByte(0)
            writer.writeByte(0)
            writer.writeByte(0)
            writer.writeLongLE(0)
        }
        if ((securityFlags & 0x4) == 0x4) {
            writer.writeByte(1)
        }

        ch.attr(AuthAttributeKey.ACCOUNT).set(account)
        ch.attr(AuthAttributeKey.STATUS).set(AuthStatus.LOGIN_PROOF)

        payload.write(AuthResult.WOW_SUCCESS.code)
        payload.writeBytes(writer.bytes)

        log.debug('Challenge Request successful, sending challenge')
    }
}
