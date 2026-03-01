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
package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.ReconnectProofMessage
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class ReconnectProofDecoder implements AuthCommandDecoder<ReconnectProofMessage> {

    private static int R1_BYTES_SIZE = 16
    private static int R2_BYTES_SIZE = 20
    private static int R3_BYTES_SIZE = 20
    private static int COMMAND_SIZE = 57

    @Override
    boolean handles(AuthCommand command) {
        return command == AuthCommand.CMD_AUTH_RECONNECT_PROOF
    }

    @Override
    DecodeResult<ReconnectProofMessage> decode(ByteBuf byteBuf) {
        log.debug('Decoding reconnect proof message')

        if (byteBuf.readableBytes() < COMMAND_SIZE) {
            return new DecodeResult<>(status: DecodeStatus.NOT_ENOUGH_BYTES)
        }

        def R1_bytes = new byte[R1_BYTES_SIZE ]
        def R2_bytes = new byte[R2_BYTES_SIZE]
        def R3_bytes = new byte[R3_BYTES_SIZE]
        byteBuf.readBytes(R1_bytes, 0, R1_BYTES_SIZE)
        byteBuf.readBytes(R2_bytes, 0, R2_BYTES_SIZE)
        byteBuf.readBytes(R3_bytes, 0, R3_BYTES_SIZE)
        byteBuf.readByte() // numberOfKeys - not used

        def message = new ReconnectProofMessage(
                R1: R1_bytes,
                R2: R2_bytes,
                R3: R3_bytes
        )

        new DecodeResult<ReconnectProofMessage>(
                message: message,
                status: DecodeStatus.COMPLETE
        )
    }
}
