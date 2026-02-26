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
package com.woodlands.yanp.common.srp

import com.woodlands.yanp.auth.AuthAttributeKey
import com.woodlands.yanp.auth.db.entity.AccountEntity
import com.woodlands.yanp.auth.message.LoginProofMessage
import com.woodlands.yanp.auth.message.handler.LoginProofMessageHandler
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.data.PacketDataWriter
import groovy.util.logging.Slf4j
import io.netty.channel.Channel
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.params.SRP6GroupParameters
import org.springframework.stereotype.Service

import java.security.SecureRandom

/**
 * Service for handling WoW SRP authentication protocols.<br>
 * See https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol for more complete information
 */
@Slf4j
@Service
class WowSrpService {

    /** The 'Safe Prime'' */
    private static final BigInteger N = new BigInteger('894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7', 16)
    /** The 'Generator' of the multiplicative group */
    private static final BigInteger g = BigInteger.valueOf(7)
    /** Just a holder for the Safe Prime and Generator */
    private static final SRP6GroupParameters params = new SRP6GroupParameters(N, g)
    /** Byte array that somehow represents a version challenge. Hard-coded this way in every *Mangos impl and AzerothCore */
    public static final byte[] VERSION_CHALLENGE =
            [ 0xBA, 0xA3, 0x1E, 0x99, 0xA0, 0x0B, 0x21, 0x57, 0xFC, 0x37, 0x3F, 0xB3, 0x69, 0xCD, 0xD2, 0xF1 ]

    final SecureRandom secureRandom

    WowSrpService(SecureRandom secureRandom) {
        this.secureRandom = secureRandom
    }

    byte[] generateChallenge(Channel ch, AccountEntity account) {
        def verifier = new BigInteger(1, account.v.decodeHex())
        def salt = account.s.decodeHex()
        def I = account.username.getBytes()
        def securityFlags = (byte)0x00

        WowSrp6Server srp6Server = WowSrp6Server.init(params, verifier, I, salt, new SHA1Digest(), secureRandom)
        BigInteger B = srp6Server.generateServerCredentials()

        // TODO Has to be done here because we need the SRP server. Bad design, refactor once things are working
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

        return writer.getBytes()
    }

    static LoginProofMessageHandler.LoginProofResponse calculateSessionKey(WowSrp6Server srp6Server, LoginProofMessage loginProofMessage) {

        def response = new LoginProofMessageHandler.LoginProofResponse()
        def S = srp6Server.calculateSecret(loginProofMessage.A)
        if (!srp6Server.verifyClientEvidenceMessage(loginProofMessage.M1)) {
            return null // TODO Don't like this
        }

        log.info('Successfully validated client M1 against server M1')
        response.M2 = srp6Server.calculateServerEvidenceMessage()
        response.sessionKey = srp6Server.calculateSessionKey()
        response
    }
}
