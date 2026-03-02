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
package com.woodlands.yanp.auth.service

import com.woodlands.yanp.auth.model.BuildInfo
import groovy.util.logging.Slf4j
import org.bouncycastle.crypto.digests.SHA1Digest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Slf4j
@Service
class VersionVerificationService {

    private static final byte[] ZEROES = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0] as byte[]
    private final boolean strictVersionCheck

    VersionVerificationService(@Value('${auth.strictVersionCheck:false}') boolean strictVersionCheck) {
        this.strictVersionCheck = strictVersionCheck
    }

    boolean verifyVersion(byte[] clientRandom, byte[] crcHash, Integer clientBuild, String operatingSystem, boolean isReconnect) {
        if (!strictVersionCheck) return true

        byte[] versionHash
        if (isReconnect) {
            versionHash = ZEROES
        } else {
            BuildInfo buildInfo = BuildInfo.BUILDS.get(clientBuild) // We should have already validated this as non-null before getting here
            if (operatingSystem == "Win") {
                versionHash = buildInfo.windowsHash
            } else if (operatingSystem == "OSX") {
                versionHash = buildInfo.macHash
            } else {
                return false
            }
        }

        SHA1Digest digest = new SHA1Digest()
        digest.update(clientRandom, 0, clientRandom.length)
        digest.update(versionHash, 0, versionHash.length)
        byte[] output = new byte[20]
        digest.doFinal(output, 0)

        return output == crcHash
    }
}
