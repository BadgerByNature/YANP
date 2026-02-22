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
import com.woodlands.yanp.auth.message.LoginRequestChallengeMessage
import com.woodlands.yanp.common.BitUtil
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginRequestChallengeDecoder implements AuthCommandDecoder<LoginRequestChallengeMessage> {

    /* We assume for the decoder that we've already read the first byte containing the command,
       so this size is one less than the entire packet sent to us */
    private static final int COMMAND_SIZE = 33
    // TODO Actually the command passes in the size, we should check that we get the error and size bites, then make sure the size is correct

    /* Reference From vMangos/CMangos/AzerothCore
    typedef struct AUTH_LOGON_CHALLENGE_C
    {
        uint8   cmd;
        uint8   error;
        uint16  size;
        uint8   gamename[4];
        uint8   version1;
        uint8   version2;
        uint8   version3;
        uint16  build;
        uint8   platform[4];
        uint8   os[4];
        uint8   country[4];
        uint32  timezone_bias;
        uint32  ip;
        uint8   I_len;
        uint8   I[1]; // AccountName
    } sAuthLogonChallenge_C;
     */

    @Override
    boolean handles(AuthCommand command) {
        return command == AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE
    }

    @Override
    DecodeResult<LoginRequestChallengeMessage> decode(ByteBuf byteBuf) {
        log.debug('Decoding login request message')
        if (byteBuf.readableBytes() < COMMAND_SIZE) {
            return new DecodeResult<>(status: DecodeStatus.NOT_ENOUGH_BITES)
        }

        byte error = byteBuf.readByte() // TODO Handle ERROR = True here ?
        short size = byteBuf.readShortLE() // TODO Handle size not matching the rest of the body
        byte[] gameName = BitUtil.readLECString(byteBuf, 4)
        byte majorVersion = byteBuf.readByte()
        byte minorVersion = byteBuf.readByte()
        byte patchVersion = byteBuf.readByte()
        short build = byteBuf.readShortLE()
        byte[] arch = BitUtil.readLECString(byteBuf, 4);
        byte[] os = BitUtil.readLECString(byteBuf, 4);
        byte[] locale = new byte[4]
        byteBuf.readBytes(locale)
        BitUtil.reverseBuffer(locale)
        int timezone = byteBuf.readIntLE()
        int ip = byteBuf.readIntLE()
        byte iLength = byteBuf.readByte()
        if (byteBuf.readableBytes() < iLength) {
            log.error("Incorrect packet size when decoding I (account name): $AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE")
            return new DecodeResult<>(status: DecodeStatus.INVALID)
        }
        // Every other codebase calls this `I`, which is a value designated as part of SRP6 authentication
        byte[] accountName = new byte[iLength]
        byteBuf.readBytes(accountName)

        // ByteBuf is converted into this AuthMessage which gets picked up in the Handler
        def message = new LoginRequestChallengeMessage(
                error: error,
                size: size,
                gameName: new String(gameName),
                majorVersion: majorVersion,
                minorVersion: minorVersion,
                patchVersion: patchVersion,
                build: build,
                arch: new String(arch),
                os: new String(os),
                locale: new String(locale),
                timezone: timezone,
                ip: ip,
                nameLength: iLength,
                accountName: new String(accountName)
        )
        new DecodeResult<LoginRequestChallengeMessage>(
                message: message,
                status: DecodeStatus.COMPLETE
        )
    }
}
