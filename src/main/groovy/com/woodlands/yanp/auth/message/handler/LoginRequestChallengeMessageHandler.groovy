package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthCommand
import com.woodlands.yanp.auth.AuthResult
import com.woodlands.yanp.auth.constant.BanStatus
import com.woodlands.yanp.auth.message.AuthMessage
import com.woodlands.yanp.auth.message.LoginRequestChallengeMessage
import com.woodlands.yanp.auth.service.AccountService
import com.woodlands.yanp.auth.service.BanService
import com.woodlands.yanp.common.network.ByteBufWowPacket
import com.woodlands.yanp.common.srp.WowSrpService
import groovy.util.logging.Slf4j
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import org.springframework.stereotype.Service

@Slf4j
@Service
class LoginRequestChallengeMessageHandler implements AuthMessageHandler {

    final AccountService accountService
    final BanService banService
    final WowSrpService srpService

    LoginRequestChallengeMessageHandler(AccountService accountService, BanService banService, WowSrpService srpService) {
        this.accountService = accountService
        this.banService = banService
        this.srpService = srpService
    }

    @Override
    boolean handles(AuthMessage message) {
        return message instanceof LoginRequestChallengeMessage
    }

    @Override
    void handle(AuthMessage message, Channel ch) {
        log.debug('Handling LoginRequestMessage')
        LoginRequestChallengeMessage requestMessage = (LoginRequestChallengeMessage)message

        log.debug(requestMessage.toString())

        // Payload includes everything after the opCode
        ByteArrayOutputStream payload = new ByteArrayOutputStream()

        // Offload work into another method so we can return out of it on fail conditions and
        // simplify our write logic here
        populateResponse(ch, requestMessage, payload)
        ch.writeAndFlush(new ByteBufWowPacket(
                opCode: AuthCommand.CMD_AUTH_REQUEST_LOGIN_CHALLENGE.code,
                payload: Unpooled.wrappedBuffer(payload.toByteArray()))
        )
    }

    void populateResponse(Channel ch, LoginRequestChallengeMessage message, ByteArrayOutputStream payload) {

        payload.write(0) // Unknown Use - just a spacer?

        String remoteIp = ch.remoteAddress().toString().replace('/', '').split(':')[0]
        if (banService.isIpBanned(remoteIp)) {
            log.debug("Remote IP attempted to connect but is BANNED") // TODO Log Remote Ip on release
            payload.write(AuthResult.WOW_FAIL_FAIL_NO_ACCESS.code)
            return
        }

        def account = accountService.getAccount(message.accountName)
        if (account == null) {
            // Just log as debug, this could happen all the time by people mistyping account names
            log.debug("Account not found during login: $message.accountName")
            payload.write(AuthResult.WOW_FAIL_UNKNOWN_ACCOUNT.code)
            return
        }

        if (account.s == null || account.v == null) {
            log.error("Account has broken s/v values in database and cannot login: accountId=$account.id, accountName=$message.accountName")
            payload.write(AuthResult.WOW_FAIL_FAIL_NO_ACCESS.code)
            return
        }

        def banStatus = banService.getAccountBanStatus(account.id)
        log.debug("Account login ban status: accountId=$account.id, banStatus=$banStatus")
        switch (banStatus) {
            case BanStatus.TEMPORARY:
                log.debug("Banned Account attempted to login: accountId=$account.id, accountName=$message.accountName, banStatus:$banStatus")
                payload.write(AuthResult.WOW_FAIL_BANNED.code)
                return
            case BanStatus.PERMANENT:
                log.debug("Banned Account attempted to login: accountId=$account.id, accountName=$message.accountName, banStatus:$banStatus")
                payload.write(AuthResult.WOW_FAIL_SUSPENDED.code)
                return
            default:
                break
        }

        byte[] challengePayload = srpService.generateChallenge(ch, account)

        payload.write(AuthResult.WOW_SUCCESS.code)
        payload.writeBytes(challengePayload)

//        // TODO Break this whole SRP method and business into its own service for easier testing
        // TODO It's broken out - test that we get as far as we did the other night before committing anything
//        doSrpStuff(ch, account, payload)

        log.debug('Challenge Request successful, sending challenge')
    }

//    void doSrpStuff(Channel ch, AccountEntity account, ByteArrayOutputStream payload) {
//        def verifier = new BigInteger(account.v.decodeHex())
//        def salt = account.s.decodeHex()
//        def I = account.username.getBytes()
//        def securityFlags = (byte)0x00
//
//        WowSrp6Server srp6Server = WowSrp6Server.init(params, verifier, I, salt, new SHA1Digest(), RandomUtil.secureRandom)
//        BigInteger B = srp6Server.generateServerCredentials()
//
//        ch.attr(AuthServer.SRP_ATTRIBUTE).set(srp6Server)
//
//        payload.write(AuthResult.WOW_SUCCESS.code)
//
//        def lew = new LittleEndianOutputWriter()
//        // TODO Why are we converting BigIntegers to Little-Endian byte arrays just to write them into a LE writer which just re-reverses them back to their original state?
//        // Is it just to make sure we're 32-byte padded? We can do that without LE logic
//        // And there's nothing here to stop us from being MORE Than 32 bytes which does, in fact, break this by overwriting into what should be the next field
//        // The client expects 32 byte BigInts and nothing more or less
//        lew.write(BitUtil.toLEByteArray(B, 32))
//        lew.write(1) // Hard-coded in every server Impl I've seen
//        lew.write(g.toByteArray()) // TODO Does this just write a single byte? Why isn't this one reversed first?
//        byte[] N_bytes = BitUtil.toLEByteArray(N, 32)
//        lew.write(N_bytes.length) // Should always be 32 if we are enforcing min size
//        lew.write(N_bytes)
//        lew.write(salt)
//        lew.write(VERSION_CHALLENGE)
//        lew.write(securityFlags) // security flags
//        if ((securityFlags & 0x1) == 0x1) {
//            lew.writeInt(0)
//            lew.writeLong(0)
//            lew.writeLong(0)
//        }
//        if ((securityFlags & 0x2) == 0x2) {
//            lew.write(0)
//            lew.write(0)
//            lew.write(0)
//            lew.write(0)
//            lew.writeLong(0)
//        }
//        if ((securityFlags & 0x4) == 0x4) {
//            lew.write(1)
//        }
//
//        payload.writeBytes(lew.baos.toByteArray())
//    }
}
