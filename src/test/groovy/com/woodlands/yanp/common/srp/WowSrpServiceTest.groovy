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

    private static final BigInteger FAKE_B = new BigInteger("98 00 60 81 b0 8e f2 5a 14 5a a4 05 2e 89 29 06 af e1 a7".replace(' ', ''), 16)

    @Mock
    SecureRandom secureRandom

    WowSrpService wowSrpService

    void setup() {
        wowSrpService = new WowSrpService(secureRandom)
    }

    def "GenerateChallenge produces values matching CMangos implementation"() {
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
        result == ("2268ff8ed7a7fb0e2e4c0a8b7f7473b1535fd6d3a23b56a280be640e9a134007" +
                "01" +
                "07" +
                "20" +
                "b7 9b 3e 2a 87 82 3c ab 8f 5e bf bf 8e b1 01 08 53 50 06 29 8b 5b ad bd 5b 53 e1 89 5e 64 4b 89" +
                "d5 d8 de 4d 3a 05 00 32 35 ac ef 83 cd a6 c8 6b ce 51 00 87 8f fa d1 64 06 04 62 42 49 34 40 f3" +
                "ba a3 1e 99 a0 0b 21 57 fc 37 3f b3 69 cd d2 f1" +
                "00").replace(' ', '').decodeHex()
    }

    def "Proof calculation matches CMangos implementation"() {
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

        BigInteger A = new BigInteger("4a b0 03 80 77 ae c7 02 fd 20 55 02 fd 3a 8b 71 cf f7 3f 8a bb c6 09 a7 8f 6c 2f 96 d0 7b 5b 0f".replace(' ', ''), 16)
        BigInteger M1 = fromByteStringReverse("85 67 ef 7b 10 e6 e1 e3 8a 8c 84 d9 48 9f bc e4 37 96 d0 b4")
        BigInteger M2 = fromByteString("8e be 06 8e 93 3b f7 aa b7 36 b6 db 13 e4 47 93 59 6a f1 61")

        // Manual confirmation when debugging
        // BigInteger U = new BigInteger("fe e8 f4 20 87 49 7b 97 ec 7b be 81 d1 9d 8b 37 8a ef aa 27".replace(' ', ''), 16)
        // BigInteger S = new BigInteger("1d bb 74 2e 88 a7 b6 7c 1e f2 91 58 d2 e8 b4 68 de 26 0d a5 f3 42 0e f8 a7 74 7a 68 1d 8c 04 62".replace(' ', ''), 16)
        // BigInteger K = fromByteString("b0 91 bf 7f 84 7e 51 9c 5b e8 b1 4b a0 43 0c ec ec 54 f0 11 84 51 38 9b 34 d8 32 14 17 74 79 d9 aa 77 20 e3 26 7c 00 a9")

        def clientProofMessage = new LoginProofMessage(A: A, M1: M1)

        when:
        wowSrpService.generateChallenge(channel, account)
        def loginProofResponse = WowSrpService.calculateSessionKey(wowSrp6Server, clientProofMessage)

        then:
        loginProofResponse.M2 == M2
        loginProofResponse.sessionKey
    }

    private static BigInteger fromByteString(String byteString) {
        new BigInteger(1, byteString.replace(' ', '').decodeHex())
    }

    private static BigInteger fromByteStringReverse(String byteString) {
        String result = ""
        def split = byteString.split(' ')
        for (int i = split.length - 1; i >=0; i--) {
            result += split[i]
        }

        new BigInteger(1, result.decodeHex())
    }
}
