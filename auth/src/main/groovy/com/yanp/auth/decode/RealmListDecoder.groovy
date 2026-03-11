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
package com.yanp.auth.decode

import com.yanp.auth.AuthCommand
import com.yanp.auth.AuthCommandDecoder
import com.yanp.auth.message.RealmListMessage
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class RealmListDecoder implements AuthCommandDecoder<RealmListMessage> {

    private static final COMMAND_SIZE = 4

    @Override
    boolean handles(AuthCommand command) {
        command == AuthCommand.CMD_REALM_LIST
    }

    @Override
    DecodeResult<RealmListMessage> decode(ByteBuf byteBuf) {

        log.debug('Decoding login request message')
        if (byteBuf.readableBytes() < COMMAND_SIZE) {
            return new DecodeResult<>(status: DecodeStatus.NOT_ENOUGH_BYTES)
        }
        if (byteBuf.readableBytes() > COMMAND_SIZE) {
            return new DecodeResult<>(status: DecodeStatus.INVALID)
        }

        // Realm List sends four empty bytes after the command code
        byteBuf.skipBytes(4)

        def message = new RealmListMessage()
        new DecodeResult<RealmListMessage>(status: DecodeStatus.COMPLETE, message: message)
    }
}
