package com.woodlands.yanp.common.srp

import com.woodlands.yanp.auth.AuthServer
import com.woodlands.yanp.auth.db.entity.AccountEntity
import com.woodlands.yanp.common.BitUtil
import com.woodlands.yanp.common.RandomUtil
import com.woodlands.yanp.common.data.LittleEndianOutputWriter
import io.netty.channel.Channel
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.params.SRP6GroupParameters
import org.springframework.stereotype.Service

/**
 * Service for handling WoW SRP authentication protocols.<br>
 * See https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol for more complete information
 */
@Service
class WowSrpService {

    /** The 'Safe Prime'' */
    private static final BigInteger N = new BigInteger('894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7', 16)
    /** The 'Generator' of the multiplicative group */
    private static final BigInteger g = BigInteger.valueOf(7)
    /** Just a holder for the Safe Prime and Generator */
    private static final SRP6GroupParameters params = new SRP6GroupParameters(N, g)
    /** Byte array that somehow represents a version challenge. Hard-coded this way in every *Mangos impl and AzerothCore */
    public static final byte[] VERSION_CHALLENGE =
            [ 0xBA, 0xA3, 0x1E, 0x99, 0xA0, 0x0B, 0x21, 0x57, 0xFC, 0x37, 0x3F, 0xB3, 0x69, 0xCD, 0xD2, 0xF1 ]


    static byte[] generateChallenge(Channel ch, AccountEntity account) {
        def verifier = new BigInteger(account.v, 16)
        def salt = account.s.decodeHex()
        def I = account.username.getBytes()
        def securityFlags = (byte)0x00

        WowSrp6Server srp6Server = WowSrp6Server.init(params, verifier, I, salt, new SHA1Digest(), RandomUtil.secureRandom)
        BigInteger B = srp6Server.generateServerCredentials()

        ch.attr(AuthServer.SRP_ATTRIBUTE).set(srp6Server) // Has to be done here because we need the SRP server :/ I don't like that, refactor
        // TODO Return the srpServer AND the byte[] instead in an intermediary class

//        payload.write(AuthResult.WOW_SUCCESS.code) // Done in the calling class, this just returns a byte[] now

        def lew = new LittleEndianOutputWriter()
        // TODO Why are we converting BigIntegers to Little-Endian byte arrays just to write them into a LE writer which just re-reverses them back to their original state?
        // Is it just to make sure we're 32-byte padded? We can do that without LE logic
        // And there's nothing here to stop us from being MORE Than 32 bytes which does, in fact, break this by overwriting into what should be the next field
        // The client expects 32 byte BigInts and nothing more or less
        lew.write(BitUtil.toLEByteArray(B, 32))
        lew.write(1) // Hard-coded in every server Impl I've seen
        lew.write(g.toByteArray()) // This 'BigInteger' is only a single byte - 0x07
        byte[] N_bytes = BitUtil.toLEByteArray(N, 32)
        lew.write(N_bytes.length) // Should always be 32 if we are enforcing min size
        lew.write(N_bytes)
        lew.write(salt)
        lew.write(VERSION_CHALLENGE)
        lew.write(securityFlags) // security flags
        if ((securityFlags & 0x1) == 0x1) {
            lew.writeInt(0)
            lew.writeLong(0)
            lew.writeLong(0)
        }
        if ((securityFlags & 0x2) == 0x2) {
            lew.write(0)
            lew.write(0)
            lew.write(0)
            lew.write(0)
            lew.writeLong(0)
        }
        if ((securityFlags & 0x4) == 0x4) {
            lew.write(1)
        }

        return lew.baos.toByteArray()
    }
}
