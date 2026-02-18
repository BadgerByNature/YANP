package com.woodlands.yanp.common.data

import java.nio.charset.Charset

class LittleEndianOutputWriter {

    private static final Charset ASCII = Charset.forName("UTF-8")

    final ByteArrayOutputStream baos = new ByteArrayOutputStream()

//    protected GenericLittleEndianWriter() {}
//
//    def GenericLittleEndianWriter(ByteArrayOutputStream bos) {
//        this.baos = bos
//    }
//
//    protected void setByteArrayOutputStream(ByteArrayOutputStream bos) {
//        this.baos = bos
//    }

    final void writeZeroBytes(int i) {
        for (int x = 0; x < i; x++) {
            baos.write((byte) 0)
        }
    }

    final void write(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            baos.write(b[i])
        }
    }

    final void write(byte b) {
        baos.write(b)
    }

    final void write(int b) {
        baos.write((byte) b)
    }

    final void writeShort(int i) {
        baos.write((byte) (i & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
    }

    final void writeInt(int i) {
        baos.write((byte) (i & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
        baos.write((byte) ((i >>> 16) & 0xFF))
        baos.write((byte) ((i >>> 24) & 0xFF))
    }

    final void writeInt(long i) {
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
            this.write(0)
        }
    }

    final void writeNullTerminatedAsciiString(String s) {
        this.writeAsciiString(s)
        this.write(0)
    }

    final void writeLong(long l) {
        baos.write((byte) (l & 0xFF))
        baos.write((byte) ((l >>> 8) & 0xFF))
        baos.write((byte) ((l >>> 16) & 0xFF))
        baos.write((byte) ((l >>> 24) & 0xFF))
        baos.write((byte) ((l >>> 32) & 0xFF))
        baos.write((byte) ((l >>> 40) & 0xFF))
        baos.write((byte) ((l >>> 48) & 0xFF))
        baos.write((byte) ((l >>> 56) & 0xFF))
    }

    final void writeFloat(float f) {
        int i = Float.floatToIntBits(f)
        this.writeInt(i)
    }

    final void writeBEFloat(float f) {
        int i = Float.floatToIntBits(f)
        baos.write((byte) ((i >>> 24) & 0xFF))
        baos.write((byte) ((i >>> 16) & 0xFF))
        baos.write((byte) ((i >>> 8) & 0xFF))
        baos.write((byte) (i & 0xFF))
    }

    final void writeDouble(double d) {
        long l = Double.doubleToLongBits(d)
        this.writeLong(l)
    }

    final void writeBEDouble(double d) {
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
