package com.woodlands.yanp.common.network

import io.netty.buffer.ByteBuf

class ByteBufWowPacket implements WowPacket {
    int opCode
    ByteBuf payload
}
