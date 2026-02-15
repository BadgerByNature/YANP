package com.woodlands.yanp.common.network

import io.netty.buffer.ByteBuf

interface WowPacket {

    int getOpCode()

    ByteBuf getPayload()
}