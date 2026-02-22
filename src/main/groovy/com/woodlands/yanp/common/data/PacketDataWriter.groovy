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
package com.woodlands.yanp.common.data

import java.nio.charset.Charset

/**
 * A wrapper for writing values to a ByteArrayOutputStream that handles
 * BigEndian and LittleEndian writing
 */
class PacketDataWriter {

    private static final Charset ASCII = Charset.forName("UTF-8")

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream()

    byte[] getBytes() {
        baos.toByteArray()
    }

    final void write(byte[] b) {
        baos.write(b)
    }

    final void write(byte b) {
        baos.write(b)
    }

    final void writeByte(int b) {
        baos.write((byte) b)
    }

    final void writeShortLE(int i) {
        baos.write((byte) (i & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
    }

    final void writeIntLE(int i) {
        baos.write((byte) (i & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
        baos.write((byte) ((i >>> 16) & 0xFF))
        baos.write((byte) ((i >>> 24) & 0xFF))
    }

    final void writeAsciiString(String s) {
        this.write(s.getBytes(ASCII))
    }

    final void writeAsciiString(String s, int max) {
        this.write(s.getBytes(ASCII))
        for (int i = s.length(); i < max; i++) {
            this.writeByte(0)
        }
    }

    final void writeNullTerminatedAsciiString(String s) {
        this.writeAsciiString(s)
        this.writeByte(0)
    }

    final void writeLongLE(long l) {
        baos.write((byte) (l & 0xFF))
        baos.write((byte) ((l >>> 8) & 0xFF))
        baos.write((byte) ((l >>> 16) & 0xFF))
        baos.write((byte) ((l >>> 24) & 0xFF))
        baos.write((byte) ((l >>> 32) & 0xFF))
        baos.write((byte) ((l >>> 40) & 0xFF))
        baos.write((byte) ((l >>> 48) & 0xFF))
        baos.write((byte) ((l >>> 56) & 0xFF))
    }

    final void writeFloatLE(float f) {
        int i = Float.floatToIntBits(f)
        this.writeIntLE(i)
    }

    final void writeFloatBE(float f) {
        int i = Float.floatToIntBits(f)
        baos.write((byte) ((i >>> 24) & 0xFF))
        baos.write((byte) ((i >>> 16) & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
        baos.write((byte) (i & 0xFF))
    }

    final void writeDoubleLE(double d) {
        long l = Double.doubleToLongBits(d)
        this.writeLongLE(l)
    }

    final void writeDoubleBE(double d) {
        long l = Double.doubleToLongBits(d)
        baos.write((byte) ((l >>> 56) & 0xFF))
        baos.write((byte) ((l >>> 48) & 0xFF))
        baos.write((byte) ((l >>> 40) & 0xFF))
        baos.write((byte) ((l >>> 32) & 0xFF))
        baos.write((byte) ((l >>> 24) & 0xFF))
        baos.write((byte) ((l >>> 16) & 0xFF))
        baos.write((byte) ((l >>> 8) & 0xFF))
        baos.write((byte) (l & 0xFF))
    }

}
