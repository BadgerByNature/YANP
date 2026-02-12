package com.woodlands.yanp.auth

import io.netty.buffer.ByteBuf

interface AuthCommandDecoder<T> {
    boolean handles(AuthCommand command)

    T decode(ByteBuf byteBuf)
}