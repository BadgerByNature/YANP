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
import com.woodlands.yanp.auth.message.RequestChallengeMessage
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginRequestChallengeDecoder extends RequestChallengeDecoder {

    /* We assume for the decoder that we've already read the first byte containing the command,
       so this size is one less than the entire packet sent to us */
    private static final int COMMAND_SIZE = 33
    // TODO Actually the command passes in the size, we should check that we get the error and size bites, then make sure the size is correct
    // This is because the length of the account name can vary, so we nee dynamic size-checking. Max name size is 16 bytes

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
    DecodeResult<RequestChallengeMessage> decode(ByteBuf byteBuf) {
        def result = super.decode(byteBuf)
        if (result.status != DecodeStatus.COMPLETE) {
            return result
        }

        result.message.command = AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE
        result
    }
}
