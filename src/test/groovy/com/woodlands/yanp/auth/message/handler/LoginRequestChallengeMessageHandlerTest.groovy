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
package com.woodlands.yanp.auth.message.handler

import com.woodlands.yanp.auth.AuthAttributeKey
import com.woodlands.yanp.auth.constant.BanStatus
import com.woodlands.yanp.common.db.entity.AccountEntity
import com.woodlands.yanp.auth.message.RequestChallengeMessage
import com.woodlands.yanp.common.service.AccountService
import com.woodlands.yanp.auth.service.BanService
import com.woodlands.yanp.common.srp.WowSrp6Server
import io.netty.channel.Channel
import io.netty.util.Attribute
import spock.lang.Specification

import java.security.SecureRandom

class LoginRequestChallengeMessageHandlerTest extends Specification {

    private static final BigInteger TEST_B = new BigInteger('98 00 60 81 b0 8e f2 5a 14 5a a4 05 2e 89 29 06 af e1 a7'.replace(' ', ''), 16)

    def mockAccountService = Mock(AccountService)
    def mockBanService = Mock(BanService)
    def mockSecureRandom = Mock(SecureRandom)

    LoginRequestChallengeMessageHandler systemUnderTest

    void setup() {
        systemUnderTest = new LoginRequestChallengeMessageHandler(mockAccountService, mockBanService, mockSecureRandom)
    }

     def "GenerateChallenge produces values matching CMangos implementation"() {
            setup:
            WowSrp6Server.TEST_B = TEST_B
            def channel = Mock(Channel)
            def mockSocketAddress = Mock(SocketAddress)
            channel.remoteAddress() >> mockSocketAddress
            mockSocketAddress.toString() >> 'TEST ADDRESS'

            mockBanService.isIpBanned(_ as String) >> false

            def account = new AccountEntity(
                    id: 123,
                    s: 'F34034494262040664D1FA8F870051CE6BC8A6CD83EFAC353200053A4DDED8D5',
                    v: '6F8CD45AA99E82E0DCBE120FBA52E38DBA5151D040E8CD297C9CC2DEDAAF629A',
                    username: 'TEST'
            )
            mockAccountService.getAccount(_ as String) >> account
            mockBanService.getAccountBanStatus(123) >> BanStatus.NONE

            def attribute = Mock(Attribute)
            channel.attr(AuthAttributeKey.SRP_ATTRIBUTE) >> attribute
            channel.attr(AuthAttributeKey.ACCOUNT) >> attribute
            channel.attr(AuthAttributeKey.STATUS) >> attribute

            def requestChallengeMessage = new RequestChallengeMessage(accountName: 'TEST')
            def payload = new ByteArrayOutputStream()

            when:
            systemUnderTest.populateResponse(channel, requestChallengeMessage, payload)

            then:
            // Validate payload
            payload.toByteArray().encodeHex().toString() ==
                    '00' + // spacer
                    '00' + // 'Success'
                    '2268ff8ed7a7fb0e2e4c0a8b7f7473b1535fd6d3a23b56a280be640e9a134007' + // 'B'
                    '01' + // Unknown
                    '07' + // 'g'
                    '20' + // N byte length (32 in hex)
                    'b79b3e2a87823cab8f5ebfbf8eb10108535006298b5badbd5b53e1895e644b89' + // 'N'
                    'd5d8de4d3a05003235acef83cda6c86bce5100878ffad16406046242493440f3' + // salt
                    'baa31e99a00b2157fc373fb369cdd2f1' + // VERSION_CHALLENGE
                    '00' // security flags
        }
}
