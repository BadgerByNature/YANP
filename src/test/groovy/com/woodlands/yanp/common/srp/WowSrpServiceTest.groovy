package com.woodlands.yanp.common.srp

import com.woodlands.yanp.auth.AuthServer
import com.woodlands.yanp.auth.db.entity.AccountEntity
import com.woodlands.yanp.auth.message.LoginProofMessage
import io.netty.channel.Channel
import io.netty.util.Attribute
import org.mockito.Mock
import spock.lang.Specification

import java.security.SecureRandom

class WowSrpServiceTest extends Specification {

    private static final BigInteger FAKE_B = new BigInteger("a0 f3 ea 3d 34 d7 d2 7b d0 0b 8c 3f 29 37 8d ed fb b4 e3".replace(' ', ''), 16)

    @Mock
    SecureRandom secureRandom

    WowSrpService wowSrpService

    void setup() {
        wowSrpService = new WowSrpService(secureRandom)
    }

    def "GenerateChallenge"() {
        setup:
        WowSrp6Server.FAKE_B = FAKE_B
        def channel = Mock(Channel)
        def attribute = Mock(Attribute)
        channel.attr(AuthServer.SRP_ATTRIBUTE) >> attribute
        def account = new AccountEntity(
                s: 'F34034494262040664D1FA8F870051CE6BC8A6CD83EFAC353200053A4DDED8D5',
                v: '6F8CD45AA99E82E0DCBE120FBA52E38DBA5151D040E8CD297C9CC2DEDAAF629A',
                username: 'TEST'
        )

        when:
        def result = wowSrpService.generateChallenge(channel, account)
        println result.encodeHex()

        then:
        result == ("76 b3 f4 7f 25 de 3d bd a5 67 e3 1e 96 e3 8f ce c3 2b c7 9c ac 63 a4 fd e7 87 96 67 61 9b 0c 60" +
                "01" +
                "07" +
                "20" +
                "b7 9b 3e 2a 87 82 3c ab 8f 5e bf bf 8e b1 01 08 53 50 06 29 8b 5b ad bd 5b 53 e1 89 5e 64 4b 89" +
                "d5 d8 de 4d 3a 05 00 32 35 ac ef 83 cd a6 c8 6b ce 51 00 87 8f fa d1 64 06 04 62 42 49 34 40 f3" +
                "ba a3 1e 99 a0 0b 21 57 fc 37 3f b3 69 cd d2 f1" +
                "00").replace(' ', '').decodeHex()
    }

    def "Troubleshoot Proof Received to match CMangos"() {
        setup:
        WowSrp6Server.FAKE_B = FAKE_B
        def channel = Mock(Channel)
        def attribute = Mock(Attribute<WowSrp6Server>)
        channel.attr(AuthServer.SRP_ATTRIBUTE) >> attribute
        def account = new AccountEntity(
                s: 'F34034494262040664D1FA8F870051CE6BC8A6CD83EFAC353200053A4DDED8D5',
                v: '6F8CD45AA99E82E0DCBE120FBA52E38DBA5151D040E8CD297C9CC2DEDAAF629A',
                username: 'TEST'
        )

        WowSrp6Server wowSrp6Server = null
        attribute.set(_ as WowSrp6Server) >> { args ->
            wowSrp6Server = args[0]
        }

        BigInteger A = new BigInteger("51 7d 6c 8d 68 cd d6 1f 4f 8c 3c 51 29 8a 18 51 2c af b7 22 0c 45 3d 6b 74 7f 61 0c 5e 76 dd 0f".replace(' ', ''), 16)
        BigInteger M = fromByteString("d9 6b 51 68 3c 20 8f fd e7 c3 9b 53 2c 30 a5 2a 57 49 3e 15")

        // Manual confirmation when debugging
        // BigInteger U = new BigInteger("a5 6f e3 3e fe d9 82 16 7e 66 ef 91 34 1d a6 76 eb 3b 82 7b".replace(' ', ''), 16)
        // BigInteger S = new BigInteger("3c 99 f3 4b a0 0c 27 7d 1b 43 30 33 75 4a b5 ac ef 23 8c ef 69 88 00 04 28 05 80 fd f2 b0 fb a3".replace(' ', ''), 16)
        // BigInteger K = fromByteString("cd b2 82 9c 07 12 a3 29 fc 80 93 b0 72 bd da c8 5d 93 4d 48 fd fe 89 91 87 31 b8 45 90 44 e6 74 7e da 35 61 48 46 3d 9c ")

        def clientProofMessage = new LoginProofMessage(A: A, M1: M)

        when:
        wowSrpService.generateChallenge(channel, account)
        def sessionKey = WowSrpService.calculateSessionKey(wowSrp6Server, clientProofMessage)

        then:
        sessionKey
    }

    private BigInteger fromByteString(String byteString) {
        new BigInteger(byteString.replace(' ', ''), 16)
    }

    private BigInteger fromByteStringReverse(String byteString) {
        String result = ""
        def split = byteString.split(' ')
        for (int i = split.length - 1; i >=0; i--) {
            result += split[i]
        }

        new BigInteger(result, 16)
    }
}
