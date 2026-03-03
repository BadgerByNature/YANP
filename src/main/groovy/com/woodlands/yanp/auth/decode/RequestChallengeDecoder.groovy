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

import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.RequestChallengeMessage
import com.woodlands.yanp.common.BitUtil
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf

@Slf4j
abstract class RequestChallengeDecoder implements AuthCommandDecoder<RequestChallengeMessage> {

    /** The minimum size the 'body' of this message can be. Body starts at gamename */
    private static final int MIN_MESSAGE_BODY_SIZE = 31

    /* Reference From vMangos/CMangos/AzerothCore
    typedef struct AUTH_LOGON_CHALLENGE_C
    {
        uint8   cmd;
        uint8   error;
        uint16  size;
        uint8   gamename[4];    4
        uint8   version1;       1
        uint8   version2;       1
        uint8   version3;       1
        uint16  build;          2
        uint8   platform[4];    4
        uint8   os[4];          4
        uint8   country[4];     4
        uint32  timezone_bias;  4
        uint32  ip;             4
        uint8   I_len;          1
        uint8   I[1]; // AccountName min 1 byte (probably longer minimum than that, honestly)
    } sAuthLogonChallenge_C;
     */

    @Override
    DecodeResult<RequestChallengeMessage> decode(ByteBuf byteBuf) {
        // We need 3 more bytes to get the values that describes the size of the whole message
        if (byteBuf.readableBytes() < 3) {
            return new DecodeResult<>(status: DecodeStatus.NOT_ENOUGH_BYTES)
        }
        byte error = byteBuf.readByte() // TODO Handle ERROR = True here ?
        short size = byteBuf.readShortLE()
        // If the specified size is too small to include the minimum body size, then error
        if (size < MIN_MESSAGE_BODY_SIZE) {
            return new DecodeResult<>(status: DecodeStatus.INVALID)
        }
        // If not enough readable bytes, send it back and wait for more
        if (byteBuf.readableBytes() < size) {
            return new DecodeResult<>(status: DecodeStatus.NOT_ENOUGH_BYTES)
        }

        byte[] gameName = BitUtil.readLECString(byteBuf, 4)
        byte majorVersion = byteBuf.readByte()
        byte minorVersion = byteBuf.readByte()
        byte patchVersion = byteBuf.readByte()
        short build = byteBuf.readShortLE()
        byte[] arch = BitUtil.readLECString(byteBuf, 4)
        byte[] os = BitUtil.readLECString(byteBuf, 4)
        byte[] locale = new byte[4]
        byteBuf.readBytes(locale)
        BitUtil.reverseBuffer(locale)
        int timezone = byteBuf.readIntLE()
        int ip = byteBuf.readIntLE()
        byte iLength = byteBuf.readByte()

        // If for some reason there's not enough bytes for the iLength then we were lied to somewhere and should abort
        // This helps keep us from ever trying to read bytes that don't exist
        if (byteBuf.readableBytes() < iLength) {
            return new DecodeResult<>(status: DecodeStatus.INVALID)
        }
        // Every other codebase calls this `I`, which is a value designated as part of SRP6 authentication
        byte[] accountName = new byte[iLength]
        byteBuf.readBytes(accountName)

        // ByteBuf is converted into this AuthMessage which gets picked up in the Handler
        def message = new RequestChallengeMessage(
                error: error,
                size: size,
                gameName: new String(gameName).trim(),
                majorVersion: majorVersion,
                minorVersion: minorVersion,
                patchVersion: patchVersion,
                build: build,
                arch: new String(arch).trim(),
                os: new String(os).trim(),
                locale: new String(locale),
                timezone: timezone,
                ip: ip,
                nameLength: iLength,
                accountName: new String(accountName)
        )
        new DecodeResult<RequestChallengeMessage>(
                message: message,
                status: DecodeStatus.COMPLETE
        )
    }
}
