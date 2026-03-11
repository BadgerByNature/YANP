/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2026 YANP: You Are Not Prepared
 * See CONTRIBUTORS.md for further Copyright information
 */
//file:noinspection GroovyAssignabilityCheck
package com.yanp.auth.message.handler

import com.yanp.auth.AuthAttributeKey
import com.yanp.auth.constant.AuthStatus
import com.yanp.common.db.entity.AccountEntity
import com.yanp.auth.message.LoginProofMessage
import com.yanp.auth.model.BuildInfo
import com.yanp.common.service.AccountService
import com.yanp.auth.service.AuthenticatorService
import com.yanp.auth.service.VersionVerificationService
import com.yanp.common.srp.WowSrp6Server
import io.netty.channel.Channel
import io.netty.util.Attribute
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.params.SRP6GroupParameters
import spock.lang.Specification

import java.security.SecureRandom

class LoginProofMessageHandlerTest extends Specification {

    private static final BigInteger N = new BigInteger('894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7', 16)
    /** The 'Generator' of the multiplicative group */
    private static final BigInteger g = BigInteger.valueOf(7)
    /** Just a holder for the Safe Prime and Generator */
    private static final SRP6GroupParameters params = new SRP6GroupParameters(N, g)

    private static final BigInteger TEST_B = new BigInteger('98 00 60 81 b0 8e f2 5a 14 5a a4 05 2e 89 29 06 af e1 a7'.replace(' ', ''), 16)

    AccountService mockAccountService = Mock(AccountService)
    AuthenticatorService mockAuthenticatorService = Mock(AuthenticatorService)
    SecureRandom mockSecureRandom = Mock(SecureRandom)
    VersionVerificationService mockVersionVerificationService = Mock(VersionVerificationService)

    LoginProofMessageHandler systemUnderTest

    void setup() {
        systemUnderTest = new LoginProofMessageHandler(mockAccountService, mockAuthenticatorService, mockVersionVerificationService, false)
    }

    def "Proof calculation matches CMangos implementation"() {
        setup:
        WowSrp6Server.TEST_B = TEST_B

        def mockChannel = Mock(Channel)
        def srpServerAttribute = Mock(Attribute<WowSrp6Server>)
        mockChannel.attr(AuthAttributeKey.SRP_ATTRIBUTE) >> srpServerAttribute
        def buildAttribute = Mock(Attribute<Integer>)
        mockChannel.attr(AuthAttributeKey.BUILD) >> buildAttribute
        buildAttribute.get() >> BuildInfo.TBC.buildNum
        def osAttribute = Mock(Attribute<String>)
        mockChannel.attr(AuthAttributeKey.OS) >> osAttribute
        osAttribute.get() >> 'mock'
        mockVersionVerificationService.verifyVersion(*_) >> true

        def account = new AccountEntity(
                s: 'F34034494262040664D1FA8F870051CE6BC8A6CD83EFAC353200053A4DDED8D5',
                v: '6F8CD45AA99E82E0DCBE120FBA52E38DBA5151D040E8CD297C9CC2DEDAAF629A',
                username: 'TEST'
        )
        mockAccountService.getAccount(_ as String) >> account
        def mockAccountAttribute = Mock(Attribute<AccountEntity>)
        mockChannel.attr(AuthAttributeKey.ACCOUNT) >> mockAccountAttribute
        mockAccountAttribute.get() >> account

        def mockStatusAttribute = Mock(Attribute<AuthStatus>)
        mockChannel.attr(AuthAttributeKey.STATUS) >> mockStatusAttribute

        def verifier = new BigInteger(1, account.v.decodeHex())
        def salt = account.s.decodeHex()
        def I = account.username.getBytes()

        WowSrp6Server srp6Server = WowSrp6Server.init(params, verifier, I, salt, new SHA1Digest(), mockSecureRandom)
        srp6Server.generateServerCredentials()

        srpServerAttribute.get() >> srp6Server

        BigInteger A = new BigInteger('4a b0 03 80 77 ae c7 02 fd 20 55 02 fd 3a 8b 71 cf f7 3f 8a bb c6 09 a7 8f 6c 2f 96 d0 7b 5b 0f'.replace(' ', ''), 16)
        BigInteger M1 = fromByteStringReverse('85 67 ef 7b 10 e6 e1 e3 8a 8c 84 d9 48 9f bc e4 37 96 d0 b4')
        BigInteger M2 = fromByteString('8e be 06 8e 93 3b f7 aa b7 36 b6 db 13 e4 47 93 59 6a f1 61')

        // Manual confirmation when debugging
        // BigInteger U = new BigInteger("fe e8 f4 20 87 49 7b 97 ec 7b be 81 d1 9d 8b 37 8a ef aa 27".replace(' ', ''), 16)
        // BigInteger S = new BigInteger("1d bb 74 2e 88 a7 b6 7c 1e f2 91 58 d2 e8 b4 68 de 26 0d a5 f3 42 0e f8 a7 74 7a 68 1d 8c 04 62".replace(' ', ''), 16)
        // BigInteger K = fromByteString("b0 91 bf 7f 84 7e 51 9c 5b e8 b1 4b a0 43 0c ec ec 54 f0 11 84 51 38 9b 34 d8 32 14 17 74 79 d9 aa 77 20 e3 26 7c 00 a9")

        def clientProofMessage = new LoginProofMessage(A: A, M1: M1)
        def payload = new ByteArrayOutputStream()

        when:
        systemUnderTest.populateResponse(mockChannel, clientProofMessage, payload)
        def result = payload.toByteArray().encodeHex().toString()

        then:
        result.contains(M2.toByteArray().encodeHex().toString())
        result  == '008ebe068e933bf7aab736b6db13e44793596af16100008000000000000000'
    }

    private static BigInteger fromByteString(String byteString) {
        new BigInteger(1, byteString.replace(' ', '').decodeHex())
    }

    private static BigInteger fromByteStringReverse(String byteString) {
        String result = ''
        def split = byteString.split(' ')
        for (int i = split.length - 1; i >=0; i--) {
            result += split[i]
        }

        new BigInteger(1, result.decodeHex())
    }
}
