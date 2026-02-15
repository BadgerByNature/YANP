package com.woodlands.yanp.auth.encode


import com.woodlands.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.springframework.stereotype.Service

@Slf4j
@Service
class AuthResponseEncoder extends MessageToByteEncoder<ByteBufWowPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBufWowPacket msg, ByteBuf out) throws Exception {
        log.debug('Encoding ByteBufWoWPacket')
        out.writeByte(msg.getOpCode())
        out.writeBytes(msg.getPayload())
        log.debug('Encoded packet written to out')
    }
}
