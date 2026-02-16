package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.LoginRequestChallengeMessage
import com.woodlands.yanp.common.BitUtils
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginRequestChallengeDecoder implements AuthCommandDecoder<LoginRequestChallengeMessage> {

    /* We assume for the decoder that we've already read the first byte containing the command,
       so this size is one less than the entire packet sent to us */
    private static final int COMMAND_SIZE = 33

    /* Reference From vMangos/CMangos/AzerothCore
    typedef struct AUTH_LOGON_CHALLENGE_C
    {
        uint8   cmd;
        uint8   error;
        uint16  size;
        uint8   gamename[4];
        uint8   version1;
        uint8   version2;
        uint8   version3;
        uint16  build;
        uint8   platform[4];
        uint8   os[4];
        uint8   country[4];
        uint32  timezone_bias;
        uint32  ip;
        uint8   I_len;
        uint8   I[1]; // AccountName
    } sAuthLogonChallenge_C;
     */

    @Override
    boolean handles(AuthCommand command) {
        return command == AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE
    }

    @Override
    LoginRequestChallengeMessage decode(ByteBuf byteBuf) {
        log.debug('Decoding login request message')

        if (byteBuf.readableBytes() < COMMAND_SIZE) {
            log.error("Incorrect packet size when decoding: $AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE")
            byteBuf.resetReaderIndex()
            return null
        }

        byte error = byteBuf.readByte()
        short size = byteBuf.readShortLE()
        byte[] gameName = BitUtils.readLECString(byteBuf, 4)
        byte majorVersion = byteBuf.readByte()
        byte minorVersion = byteBuf.readByte()
        byte patchVersion = byteBuf.readByte()
        short build = byteBuf.readShortLE()
        byte[] arch = BitUtils.readLECString(byteBuf, 4);
        byte[] os = BitUtils.readLECString(byteBuf, 4);
        byte[] locale = new byte[4]
        byteBuf.readBytes(locale)
        BitUtils.reverseBuffer(locale)
        int timezone = byteBuf.readIntLE()
        int ip = byteBuf.readIntLE()
        byte iLength = byteBuf.readByte()
        if (byteBuf.readableBytes() < iLength) {
            log.error("Incorrect packet size when decoding I (account name): $AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE")
            byteBuf.resetReaderIndex()
            return null
        }
        // Every other codebase calls this `I` with no explanation
        byte[] accountName = new byte[iLength]
        byteBuf.readBytes(accountName)

        // ByteBuf is converted into this AuthMessage which gets picked up in the Handler
        new LoginRequestChallengeMessage(
                error: error,
                size: size,
                gameName: new String(gameName),
                majorVersion: majorVersion,
                minorVersion: minorVersion,
                patchVersion: patchVersion,
                build: build,
                arch: new String(arch),
                os: new String(os),
                locale: new String(locale),
                timezone: timezone,
                ip: ip,
                nameLength: iLength,
                accountName: new String(accountName)
        )
    }
}
