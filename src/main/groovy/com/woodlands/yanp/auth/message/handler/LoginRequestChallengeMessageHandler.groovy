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

        log.debug('Challenge Request successful, sending challenge')
    }
}
