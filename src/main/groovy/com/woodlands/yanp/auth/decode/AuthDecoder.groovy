package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

// TODO Can this be Spring-managed or do we need one per thread? Can we multithread it with Java 25 features?
@Service
class AuthDecoder extends ByteToMessageDecoder {

    @Autowired
    List<AuthCommandDecoder<Object>> authCommandDecoders

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> listOut) throws Exception {
        // One byte should be all we need to determine the AuthCommand
        if (byteBuf.readableBytes() < 1) {
            return
        }

        // Marks the reader index in case it needs to be reset - when could that happen?
        byteBuf.markReaderIndex()


        byte commandCode = byteBuf.readByte()

        AuthCommand command = AuthCommand.fromCode(commandCode)
        var decoder = authCommandDecoders.find { it -> it.handles(command) }
        if (decoder != null) {
            listOut.add(decoder.decode(byteBuf))
        }
    }
}
