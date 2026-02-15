package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthResult
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.LoginRequestMessage
import com.woodlands.yanp.common.network.ByteBufWowPacket
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginRequestMessageHandler implements AuthMessageHandler {

    // TODO Inject Database, check account credentials - basically actual message processing
    LoginRequestMessageHandler() {

    }

    @Override
    boolean handles(AuthMessage message) {
        return message instanceof LoginRequestMessage
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling LoginRequestMessage')
        // Payload does not include opCode, that's added separately
        ByteArrayOutputStream payload = new ByteArrayOutputStream()
        payload.write(0) // Unknown Use
        payload.write(AuthResult.WOW_FAIL_BANNED.code)
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_LOGON_CHALLENGE.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }
}
