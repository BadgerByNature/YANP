/*
 * Java World of Warcraft Emulation Project
 * Copyright (C) 2015-2020 JavaWoW
 *
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
 */
/*
* Copyright (c) 2026 YANP: You Are Not Prepared
* See CONTRIBUTORS.md for further Copyright information
*/
package com.woodlands.yanp.common.srp

import com.woodlands.yanp.common.BitUtil
import org.bouncycastle.crypto.CryptoException
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.agreement.srp.SRP6Server
import org.bouncycastle.crypto.params.SRP6GroupParameters

import java.security.SecureRandom

/**
 * Implements the server-side of the SRP6 protocol. Responsible for the following tasks:
 * <ul>
 *     <li>Generates server credentials to be sent to the client</li>
 *     <li>Authenticates the client's evidence message (M1)</li>
 *     <li>Computes the server's evidence message (M2) and the final session key</li>
 * </ul>
 * */
class WowSrp6Server extends SRP6Server {

    // TODO Remove this once we have a fully working implementation
    /** For testing/troubleshooting purposes when we need a KNOWN "random" to reproduce expected results */
    static BigInteger FAKE_B = null //new BigInteger("fc009cec3a0c60324f1ed1f37916fb4ad36571", 16)

    byte[] I // Account name as byte array
    byte[] salt // Salt

    /* TODO Once this is proven working, figure out which overrides we do and do not need.
     * Swap to a public constructor that takes the values instead of using .init
     * Try to figure out why so much is duplicated from the parent class
     */
    private WowSrp6Server() {}

    static final WowSrp6Server init(SRP6GroupParameters params, BigInteger v, byte[] I, byte[] s, Digest digest,
                                    SecureRandom random) {
        WowSrp6Server srp = new WowSrp6Server()
        srp.initInternal(params, v, I, s, digest, random)
        return srp
    }

    private final void initInternal(SRP6GroupParameters params, BigInteger v, byte[] I, byte[] s, Digest digest,
                                    SecureRandom random) {
        this.I = Arrays.copyOf(I, I.length)
        this.salt = BitUtil.reverse(Arrays.copyOf(s, s.length))
        super.init(params.getN(), params.getG(), v, digest, random)
    }

    @Override
    final void init(SRP6GroupParameters params, BigInteger v, Digest digest, SecureRandom random) {
        throw new UnsupportedOperationException()
    }

    @Override
    final void init(BigInteger N, BigInteger g, BigInteger v, Digest digest, SecureRandom random) {
        throw new UnsupportedOperationException()
    }

    /**
     * Generates the server's credentials that are to be sent to the client.
     *
     * @return The server's public value to the client
     */
    @Override
    final BigInteger generateServerCredentials() {
        BigInteger k = BigInteger.valueOf(3) // k = 3 for legacy SRP-6
        if (FAKE_B) {
            this.b = FAKE_B
        } else {
            this.b = selectPrivateValue()
        }
        this.B = k.multiply(v).mod(N).add(g.modPow(b, N)).mod(N)
        return B
    }

    /**
     * Processes the client's credentials. If valid the shared secret is generated
     * and returned.
     *
     * @param clientA The client's credentials
     * @return A shared secret BigInteger
     * @throws CryptoException If client's credentials are invalid
     */
    @Override
    final BigInteger calculateSecret(BigInteger clientA) throws CryptoException {
        this.A = WowSrp6Util.validatePublicValue(N, clientA)
        this.u = WowSrp6Util.calculateU(digest, N, A, B)
        this.S = v.modPow(u, N).multiply(A).mod(N).modPow(b, N)
        return S
    }

    /**
     * Authenticates the received client evidence message M1 and saves it only if
     * correct. To be called after calculating the secret S.
     *
     * @param clientM1 the client side generated evidence message
     * @return A boolean indicating if the client message M1 was the expected one.
     * @throws CryptoException
     */
    @Override
    final boolean verifyClientEvidenceMessage(BigInteger clientM1) throws CryptoException {
        // Verify pre-requirements
        if (this.A == null || this.B == null || this.S == null) {
            throw new CryptoException("Impossible to compute and verify M1: some data are missing from the previous operations (A,B,S)")
        }
        // Compute the own client evidence message 'M1'
        BigInteger computedM1 = WowSrp6Util.calculateM1(digest, N, g, I, salt, A, B, S)
        if (computedM1 == clientM1) {
            this.M1 = clientM1
            return true
        }
        return false
    }

    /**
     * Computes the server evidence message M2 using the previously verified values.
     * To be called after successfully verifying the client evidence message M1.
     *
     * @return M2: the server side generated evidence message
     * @throws CryptoException
     */
    @Override
    final BigInteger calculateServerEvidenceMessage() throws CryptoException {
        // Verify pre-requirements
        if (this.A == null || this.M1 == null || this.S == null) {
            throw new CryptoException("Impossible to compute M2: some data are missing from the previous operations (A,M1,S)")
        }
        // Compute the server evidence message 'M2'
        this.M2 = WowSrp6Util.calculateM2(digest, N, A, M1, S)
        return M2
    }

    /**
     * Computes the final session key as a result of the SRP successful mutual
     * authentication To be called after calculating the server evidence message M2.
     *
     * @return Key: the mutual authenticated symmetric session key
     * @throws CryptoException
     */
    @Override
    final BigInteger calculateSessionKey() throws CryptoException {
        // Verify pre-requirements
        if (this.S == null || this.M1 == null || this.M2 == null) {
            throw new CryptoException("Impossible to compute Key: " + "some data are missing from the previous operations (S,M1,M2)")
        }
        this.Key = WowSrp6Util.calculateKey(digest, S)
        return Key
    }
}
