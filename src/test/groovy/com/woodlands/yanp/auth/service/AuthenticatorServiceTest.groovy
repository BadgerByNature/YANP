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

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock

class AuthenticatorServiceTest extends Specification {

    private static final String TEST_TOKEN = 'a4b4c4d4a4b4c4d4'.toUpperCase()
    private static final long TEST_TIMESTAMP = 1772844815872

    private static final int MINUS_30S_TOKEN = 820603
    private static final int CURRENT_TOKEN = 308661
    private static final int PLUS_30S_TOKEN = 647399

    AuthenticatorService systemUnderTest
    Clock mockClock = Mock(Clock)

    void setup() {
        systemUnderTest = new AuthenticatorService(mockClock)
    }

    // Just to prove we're generating the expected values in case someone tweaks the algorithm for some reason
    def 'Generates an expected key value at a specific time'() {
        given:
        String key = TEST_TOKEN
        Long timestamp = (Long) (TEST_TIMESTAMP / 1000 / 30)

        when:
        def minus30Token = systemUnderTest.generateToken(key, timestamp - 1)
        def nowToken = systemUnderTest.generateToken(key, timestamp)
        def plus30Token = systemUnderTest.generateToken(key, timestamp + 1)

        then:
        minus30Token == MINUS_30S_TOKEN
        nowToken == CURRENT_TOKEN
        plus30Token == PLUS_30S_TOKEN
    }

    @SuppressWarnings('GroovyAssignabilityCheck')
    @Unroll
    def 'Validate token succeeds when timestamp is 1772844815872 and pin is #pin'() {
        given:
        mockClock.millis() >> TEST_TIMESTAMP

        expect:
        systemUnderTest.validateToken(TEST_TOKEN, String.valueOf(pin).bytes)

        where:
        // Validate that if we send the -30s token, current time token, or +30s token all succeed
        pin << [MINUS_30S_TOKEN, CURRENT_TOKEN, PLUS_30S_TOKEN]
    }

    def 'Validate token fails when pin is not correct'() {
        given:
        mockClock.millis() >> TEST_TIMESTAMP
        int pin = MINUS_30S_TOKEN + 1 // Same pin as previous test by incremented

        expect:
        !systemUnderTest.validateToken(TEST_TOKEN, String.valueOf(pin).bytes)
    }
}
