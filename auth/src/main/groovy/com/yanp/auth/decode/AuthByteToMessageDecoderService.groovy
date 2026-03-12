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
package com.yanp.auth.decode

import com.yanp.auth.AuthAttributeKey
import com.yanp.auth.AuthCommand
import com.yanp.auth.AuthCommandDecoder
import com.yanp.auth.constant.AuthStatus
import com.yanp.auth.message.AuthMessage
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.springframework.stereotype.Service

/**
 * Selects the proper decoder based on the instruction/command contained in the first byte
 * and uses it to decode the ByteBuf into a standard Object for easier manipulation in the
 * MessageHandler.
 * <p>
 * If not enough bytes are found for the detected AuthCommand then we reset the readerIndex
 * so that we'll re-read the message from the beginning when more bytes come in.
 */
@Slf4j
@Service
class AuthByteToMessageDecoderService extends ByteToMessageDecoder {

    List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders

    AuthByteToMessageDecoderService(List<AuthCommandDecoder<? extends AuthMessage>> authCommandDecoders) {
        this.authCommandDecoders = authCommandDecoders
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> listOut) throws Exception {
        // One byte should be all we need to determine the AuthCommand, but no command is shorter than 3 bytes
        if (byteBuf.readableBytes() < 3) {
            return
        }

        // Marks the reader index so we can reset it until the full command bytes come in
        byteBuf.markReaderIndex()

        // First byte should represent the command we need to process
        byte commandCode = byteBuf.readByte()

        AuthCommand command = AuthCommand.fromCode(commandCode)
        AuthStatus currentAuthStatus = ctx.channel().attr(AuthAttributeKey.STATUS).get()
        if (currentAuthStatus != command.expectedAuthStatus) {
            log.warn('Client sent a command that did not match the current auth status')
            ctx.close()
            return
        }

        def decoder = authCommandDecoders.find { it -> it.handles(command) }
        if (decoder == null) {
            log.warn("No decoder found for command code ${Integer.toHexString(commandCode)}")
            ctx.close()
            return
        }

        def result = decoder.decode(byteBuf)
        switch (result.status) {
            case DecodeStatus.COMPLETE:
                listOut.add(result.message)
                break
            case DecodeStatus.NOT_ENOUGH_BYTES:
                byteBuf.resetReaderIndex()
                break
            case DecodeStatus.INVALID:
                log.warn("Decoding packet from ${ctx.channel().remoteAddress()} resulted in status $DecodeStatus.INVALID")
                ctx.close()
                break
            default:
                log.error('Reached default case on DecodeStatus')
                ctx.close()
                break
        }
    }
}
