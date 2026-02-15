package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.message.AuthMessage
import io.netty.channel.Channel

interface AuthMessageHandler {

    void handle(AuthMessage message, Channel ch)

    boolean handles(AuthMessage message)

}