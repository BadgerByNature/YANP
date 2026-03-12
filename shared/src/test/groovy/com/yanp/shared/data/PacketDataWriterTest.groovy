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
package com.yanp.shared.data

import spock.lang.Specification

class PacketDataWriterTest extends Specification {

    PacketDataWriter systemUnderTest

    void setup() {
        systemUnderTest = new PacketDataWriter()
    }

    def 'Writes multiple byte arrays as-is'() {
        given:
        byte[] b1 = [ 0x12, 0x34 ] as byte[]
        byte[] b2 = [ 0x56, 0x78 ] as byte[]

        when:
        systemUnderTest.write(b1)
        systemUnderTest.write(b2)

        then: 'The bytes are written in original order'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x12, 0x34, 0x56, 0x78 ] as byte[]
    }

    def 'Writes single bytes'() {
        when:
        systemUnderTest.write((byte)0x12)
        systemUnderTest.write((byte)0x34)

        then: 'The bytes are written in order'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x12, 0x34 ] as byte[]
    }

    def 'Writes ints as bytes'() {
        when:
        systemUnderTest.writeByte(Integer.valueOf(20))

        then: 'Int is written as byte'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 20 ] as byte[]
    }

    def 'Writes only the smallest byte of a large Integer'() {
        when:
        systemUnderTest.writeByte(Integer.MAX_VALUE)
        systemUnderTest.writeByte(256)
        systemUnderTest.writeByte(257)

        then: 'The least-significant byte per int is written'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0xFF, 0x00, 0x01 ] as byte[]
    }

    def 'Writes shorts as two bytes Little-Endian style'() {
        when:
        systemUnderTest.writeShortLE(0xABCD)

        then: 'Two bytes are written in Little-Endian order'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0xCD, 0xAB ] as byte[]
    }

    def 'Writes integers as four bytes Little-Endian order'() {
        when:
        systemUnderTest.writeIntLE(0x7FABCDEF)

        then: 'Four bytes are written in Little-Endian order'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0xEF, 0xCD, 0xAB, 0x7F ] as byte[]
    }

    def 'Write longs as eight bytes Little-Endian order'() {
        when:
        systemUnderTest.writeLongLE(0x7fA1B2C3D4E5F689L)

        then: 'Eight bytes are written in Little-Endian order'
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x89, 0xF6, 0xE5, 0xD4, 0xC3, 0xB2, 0xA1, 0x7F ] as byte[]
    }

    def 'Writes strings as ASCII bytes'() {
        when: 
        systemUnderTest.writeAsciiString('ASCII')
        
        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x41, 0x53, 0x43, 0x49, 0x49] as byte[]
    }

    def 'Writes padded strings as ASCII bytes'() {
        when: 'String is too short - requires padding'
        systemUnderTest.writeAsciiString('ASCII', 8)

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00] as byte[]
    }

    def 'Writes null-terminated strings as ASCII bytes'() {
        when:
        systemUnderTest.writeNullTerminatedAsciiString('ASCII')

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x41, 0x53, 0x43, 0x49, 0x49, 0x00] as byte[]
    }

    def 'Writes floats as four bytes Big-Endian order'() {
        when:
        systemUnderTest.writeFloatBE(12.56f)

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x41, 0x48, 0xF5, 0xC3 ] as byte[]
    }

    def 'Write floats as four bytes Little-Endian order'() {
        when:
        systemUnderTest.writeFloatLE(12.56f)

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0xC3, 0xF5, 0x48, 0x41 ] as byte[]
    }

    def 'Writes doubles as eight bytes Big-Endian order'() {
        when:
        systemUnderTest.writeDoubleBE(12.56d)

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x40, 0x29, 0x1E, 0xB8, 0x51, 0xEB, 0x85, 0x1F ] as byte[]
    }

    def 'Writes doubles as eight bytes Little-Endian order'() {
        when:
        systemUnderTest.writeDoubleLE(12.56d)

        then:
        def result = systemUnderTest.baos.toByteArray()
        result == [ 0x1F, 0x85, 0xEB, 0x51, 0xB8, 0x1E, 0x29, 0x40 ] as byte[]
    }
}
