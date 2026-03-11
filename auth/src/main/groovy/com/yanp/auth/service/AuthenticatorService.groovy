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
package com.yanp.auth.service

import org.bouncycastle.util.encoders.Base32
import org.springframework.stereotype.Service

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.time.Clock

@Service
class AuthenticatorService {

    final static String ALGORITHM = "HmacSHA1"

    final Clock clock

    AuthenticatorService(Clock clock) {
        this.clock = clock
    }

    /**
     * Validate the account token vs the pin-entry from the user in an attempt to validate
     * their authenticator. As a backup, validate the previous 30s token and next 30s token
     * in case we have a slight clock desync or they send their pin right at the 30s boundary.
     *
     * @param accountToken token value from the account table
     * @param pins byte array representing the numeric pin the user entered
     * @return true if matches, false if not
     */
    boolean validateToken(String accountToken, byte[] pins) {
        // Get current timestamp divided by 30 seconds - the time period each token is good for
        long timestampNow = (long) (clock.millis() / 1000 / 30)
        int token = generateToken(accountToken, timestampNow)
        if (token.toString() == new String(pins)) {
            return true
        }

        // If the first token didn't match,
        // try previous and next 30 second values just in case it was latency or a small clock desync
        token = generateToken(accountToken, timestampNow + 1)
        if (token.toString() == new String(pins)) {
            return true
        }
        token = generateToken(accountToken, timestampNow - 1)
        return token.toString() == new String(pins)
    }

    static int generateToken(String base32key, Long timestamp) throws NoSuchAlgorithmException, InvalidKeyException {
        // Decode base32 key to binary
        byte[] decodedKey = Base32.decode(base32key)
        // Convert timestamp to 8-byte big-endian array
        byte[] challenge = ByteBuffer.allocate(8).putLong(timestamp).array()

        // Compute HMAC-SHA1
        Mac hmac = Mac.getInstance(ALGORITHM)
        SecretKeySpec keySpec = new SecretKeySpec(decodedKey, ALGORITHM)
        hmac.init(keySpec)
        byte[] hmacResult = hmac.doFinal(challenge)

        // Dynamic truncation
        // First take the last 4 bytes of the last index of the final result for a pseudo-random value 0-15
        int offset = hmacResult[19] & 0x0F // 0x0F (or 0xF) = 00001111 = 15
        // Then take four byte values starting with the random index to make a 32-bit value
        // Mask each value against 0xFF to force a positive unsigned byte 0-255 before shifting anything
        int truncHash = ((hmacResult[offset] & 0xFF) << 24)
                        | ((hmacResult[offset + 1] & 0xFF) << 16)
                        | ((hmacResult[offset + 2] & 0xFF) << 8)
                        | (hmacResult[offset + 3] & 0xFF)

        // Mask to a positive 31 bit value
        truncHash &= 0x7FFFFFFF

        // Reduce to 6 digits via modulo to get our 6-digit code
        return truncHash % 1000000
    }
}
