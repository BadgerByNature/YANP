package com.woodlands.yanp.auth.decode

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthCommandDecoder
import com.woodlands.yanp.auth.message.LoginProofMessage
import com.woodlands.yanp.common.BitUtil
import groovy.util.logging.Slf4j
import io.netty.buffer.ByteBuf
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginProofDecoder implements AuthCommandDecoder<LoginProofMessage> {

    private final static A_BYTES_LENGTH = 32
    private final static M1_BYTES_LENGTH = 20
    private final static CRC_HASH_BYTES_LENGTH = 20

    private static final int COMMAND_SIZE = 74

    @Override
    boolean handles(AuthCommand command) {
        return command == AuthCommand.CMD_AUTH_LOGIN_PROOF
    }

//    typedef struct AUTH_LOGON_PROOF_C
//    {
//        uint8   cmd; // 1 byte
//        Acore::Crypto::SRP6::EphemeralKey A; // 32 bytes
//        Acore::Crypto::SHA1::Digest clientM; // 20 bytes
//        Acore::Crypto::SHA1::Digest crc_hash; // 20 bytes
//        uint8   number_of_keys; // 1 byte
//        uint8   securityFlags; // 1 byte
//    } sAuthLogonProof_C;

    @Override
    LoginProofMessage decode(ByteBuf byteBuf) {
        log.debug('Decoding login proof message')

        if (byteBuf.readableBytes() != COMMAND_SIZE) {
            log.error("Incorrect packet size when decoding: $AuthCommand.CMD_AUTH_LOGIN_PROOF")
            byteBuf.resetReaderIndex()
            return null
        }

        def A_bytes = new byte[A_BYTES_LENGTH]
        def M1_bytes = new byte[M1_BYTES_LENGTH]
        def crcHash = new byte[CRC_HASH_BYTES_LENGTH]
        byteBuf.readBytes(A_bytes, 0, A_BYTES_LENGTH)
        byteBuf.readBytes(M1_bytes, 0, M1_BYTES_LENGTH)
        byteBuf.readBytes(crcHash, 0, CRC_HASH_BYTES_LENGTH)
        byte numberOfKeys = byteBuf.readByte()
        byte securityFlags = byteBuf.readByte()

        new LoginProofMessage(
                A: BitUtil.toBigInteger(A_bytes, true),
                M1: BitUtil.toBigInteger(M1_bytes, false),
                crcHash: crcHash,
                numberOfKeys: numberOfKeys,
                securityFlags: securityFlags
        )
    }
}
