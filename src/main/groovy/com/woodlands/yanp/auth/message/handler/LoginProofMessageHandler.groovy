package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthResult
import com.woodlands.yanp.auth.AuthServer
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.LoginProofMessage
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.data.LittleEndianOutputWriter
import com.woodlands.yanp.common.network.ByteBufWowPacket
import com.woodlands.yanp.common.srp.WowSrpService
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginProofMessageHandler implements AuthMessageHandler {

    final WowSrpService srpService

    LoginProofMessageHandler(WowSrpService srpService) {
        this.srpService = srpService
    }

    @Override
    boolean handles(AuthMessage message) {
        message instanceof LoginProofMessage
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling LoginProofMessage')
        LoginProofMessage proofMessage = (LoginProofMessage)message
        log.debug(proofMessage.toString())

        ByteArrayOutputStream payload = new ByteArrayOutputStream()
        populateResponse(ch, proofMessage, payload)

        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_LOGIN_PROOF.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    void populateResponse(Channel channel, LoginProofMessage message, ByteArrayOutputStream payload) {
        def srpServer = channel.attr(AuthServer.SRP_ATTRIBUTE).get()

        // This should only happen if using a hacked client or direct socket connection to attempt to bypass security
        // If you went through the LoginChallenge already this will be here
        if (srpServer == null) {
            log.error("LoginProof received but SRP is not set on channel")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            payload.writeBytes([0x00, 0x00] as byte) // Comment in ACore: "LoginFlags, 1 has account message" - I don't know what this means, but I do see CMangos sending '3' in this field for invalid PIN
            return
        }

        def response = srpService.calculateSessionKey(srpServer, message)
        // TODO Handle security tokens etc here
        // TODO Verify client version - ACore and CMangos both do this here, why don't they do it earlier on Challenge message?
        if (response == null) {
            log.error("LoginProof failed")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            payload.write(0)
            payload.write(0)
            return
        }

        // TODO Set SessionKey into Account Table
        // TODO Set os, locale, failed_logins, platform as well


        // TODO Validate M2 size not greater than 20?
        payload.write(AuthResult.WOW_SUCCESS.code)
        def lew = new LittleEndianOutputWriter()
        lew.write(BitUtil.toByteArray(response.M2, 20))
        lew.writeInt(0x00800000) // All the cores use this. Acore labels it "Pro pass (arena tournament)"
        lew.writeInt(0) // Survey Id
        lew.writeShort(0) // Login Flags - Per ACore and CMangos: "0x01 has account message"
        payload.writeBytes(lew.baos.toByteArray())
    }

    static class LoginProofResponse {
        BigInteger sessionKey
        BigInteger M2
    }
}
