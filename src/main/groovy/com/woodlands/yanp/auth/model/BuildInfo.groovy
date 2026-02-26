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
package com.woodlands.yanp.auth.model

import groovy.transform.TupleConstructor

@TupleConstructor
class BuildInfo {

    /** This is the only TBC build listed in CMangos. ACore stores its buildInfo in the database */
    final static BuildInfo TBC = new BuildInfo(8606, 2, 4, 3, ' ',
            [ 0x31, 0x9A, 0xFA, 0xA3, 0xF2, 0x55, 0x96, 0x82, 0xF9, 0xFF, 0x65, 0x8B, 0xE0, 0x14, 0x56, 0x25, 0x5F, 0x45, 0x6F, 0xB1 ] as byte[],
            [ 0xD8, 0xB0, 0xEC, 0xFE, 0x53, 0x4B, 0xC1, 0x13, 0x1E, 0x19, 0xBA, 0xD1, 0xD4, 0xC0, 0xE8, 0x13, 0xEE, 0xE4, 0x99, 0x4F ] as byte[]
    )
    final static Map<Integer, BuildInfo> BUILDS = Map.of(TBC.buildNum, BuildInfo.TBC)

    int buildNum
    int majorVersion
    int minorVersion
    int bugfixVersion
    String hotfixVersion
    byte[] windowsHash
    byte[] macHash
}
