/*
 * Copyright (c) 2006-2009 Philipp Meinen <philipp@bind.ch>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.io;

public final class EndianConverter {

    public EndianConverter() {
    }

    // #################
    // # ENCODE INT 64 #
    // #################

    public static void encodeInt64BE(long value, byte[] output) {
        encodeInt64BE(value, output, 0);
    }

    public static void encodeInt64LE(long value, byte[] output) {
        encodeInt64LE(value, output, 0);
    }

    public static void encodeInt64BE(long value, byte[] output, int offset) {
        output[offset + 0] = (byte) (value >> 56 & 0xFF);
        output[offset + 1] = (byte) (value >> 48 & 0xFF);
        output[offset + 2] = (byte) (value >> 40 & 0xFF);
        output[offset + 3] = (byte) (value >> 32 & 0xFF);
        output[offset + 4] = (byte) (value >> 24 & 0xFF);
        output[offset + 5] = (byte) (value >> 16 & 0xFF);
        output[offset + 6] = (byte) (value >> 8 & 0xFF);
        output[offset + 7] = (byte) (value & 0xFF);
    }

    public static void encodeInt64LE(long value, byte[] output, int offset) {
        output[offset + 0] = (byte) (value & 0xFF);
        output[offset + 1] = (byte) (value >> 8 & 0xFF);
        output[offset + 2] = (byte) (value >> 16 & 0xFF);
        output[offset + 3] = (byte) (value >> 24 & 0xFF);
        output[offset + 4] = (byte) (value >> 32 & 0xFF);
        output[offset + 5] = (byte) (value >> 40 & 0xFF);
        output[offset + 6] = (byte) (value >> 48 & 0xFF);
        output[offset + 7] = (byte) (value >> 56 & 0xFF);
    }

    // #################
    // # DECODE INT 64 #
    // #################
    public static long decodeInt64BE(byte[] input) {
        return decodeInt64BE(input, 0);
    }

    public static long decodeInt64LE(byte[] input) {
        return decodeInt64LE(input, 0);
    }

    public static long decodeInt64BE(byte[] input, int offset) {
        return ((input[offset + 0] & 0xFF) << 56) | //
                ((input[offset + 1] & 0xFF) << 48) | //
                ((input[offset + 2] & 0xFF) << 40) | //
                ((input[offset + 3] & 0xFF) << 32) | //
                ((input[offset + 4] & 0xFF) << 24) | //
                ((input[offset + 5] & 0xFF) << 16) | //
                ((input[offset + 6] & 0xFF) << 8) | //
                (input[offset + 7] & 0xFF);
    }

    public static long decodeInt64LE(byte[] input, int offset) {
        return (input[offset + 0] & 0xFF) | //
                ((input[offset + 1] & 0xFF) << 8) | //
                ((input[offset + 2] & 0xFF) << 16) | //
                ((input[offset + 3] & 0xFF) << 24) | //
                ((input[offset + 4] & 0xFF) << 32) | //
                ((input[offset + 5] & 0xFF) << 40) | //
                ((input[offset + 6] & 0xFF) << 48) | //
                ((input[offset + 7] & 0xFF)) << 56;
    }
}
