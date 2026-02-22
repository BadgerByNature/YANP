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
package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.LoginProofMessage
import com.woodlands.yanp.common.BitUtil
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginProofDecoder implements AuthCommandDecoder<LoginProofMessage> {

    private final static A_BYTES_LENGTH = 32
    private final static M1_BYTES_LENGTH = 20
    private final static CRC_HASH_BYTES_LENGTH = 20

    private static final int COMMAND_SIZE = 74

    @Override
    boolean handles(AuthCommand command) {
        return command == AuthCommand.CMD_AUTH_LOGIN_PROOF
    }

//    typedef struct AUTH_LOGON_PROOF_C
//    {
//        uint8   cmd; // 1 byte
//        Acore::Crypto::SRP6::EphemeralKey A; // 32 bytes
//        Acore::Crypto::SHA1::Digest clientM; // 20 bytes
//        Acore::Crypto::SHA1::Digest crc_hash; // 20 bytes
//        uint8   number_of_keys; // 1 byte
//        uint8   securityFlags; // 1 byte
//    } sAuthLogonProof_C;

    @Override
    DecodeResult<LoginProofMessage> decode(ByteBuf byteBuf) {
        log.debug('Decoding login proof message')

        if (byteBuf.readableBytes() < COMMAND_SIZE) {
            return new DecodeResult<LoginProofMessage>(status: DecodeStatus.NOT_ENOUGH_BITES)
        }

        def A_bytes = new byte[A_BYTES_LENGTH]
        def M1_bytes = new byte[M1_BYTES_LENGTH]
        def crcHash = new byte[CRC_HASH_BYTES_LENGTH]
        byteBuf.readBytes(A_bytes, 0, A_BYTES_LENGTH)
        byteBuf.readBytes(M1_bytes, 0, M1_BYTES_LENGTH)
        byteBuf.readBytes(crcHash, 0, CRC_HASH_BYTES_LENGTH)
        byte numberOfKeys = byteBuf.readByte()
        byte securityFlags = byteBuf.readByte()

        def message = new LoginProofMessage(
                A: BitUtil.toBigInteger(A_bytes, true),
                M1: BitUtil.toBigInteger(M1_bytes, true),
                crcHash: crcHash,
                numberOfKeys: numberOfKeys,
                securityFlags: securityFlags
        )
        new DecodeResult<LoginProofMessage>(
                message: message,
                status: DecodeStatus.COMPLETE
        )
    }
}
