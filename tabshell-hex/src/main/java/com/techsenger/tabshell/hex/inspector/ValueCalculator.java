/*
 * Copyright 2024-2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.tabshell.hex.inspector;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author Pavel Castornii
 */
public final class ValueCalculator {

    private static class RawValue {

        private final long raw;

        private final int size;

        RawValue(long raw, int size) {
            this.raw = raw;
            this.size = size;
        }
    }

    public static void calculateInt(Values values, byte[] data, int offset, ByteOrder byteOrder) {
        // 8-bit
        RawValue int8 = calculateRaw(data, offset, byteOrder, 1);
        if (int8 != null) {
            values.setSignedInt8((long) toSigned(int8.raw, 8));
            values.setUnsignedInt8((long) int8.raw);
        }

        // 16-bit
        RawValue int16 = calculateRaw(data, offset, byteOrder, 2);
        if (int16 != null) {
            values.setSignedInt16((long) toSigned(int16.raw, 16));
            values.setUnsignedInt16((long) int16.raw);
        }

        // 24-bit
        RawValue int24 = calculateRaw(data, offset, byteOrder, 3);
        if (int24 != null) {
            values.setSignedInt24((long) toSigned(int24.raw, 24));
            values.setUnsignedInt24((long) int24.raw);
        }

        // 32-bit
        RawValue int32 = calculateRaw(data, offset, byteOrder, 4);
        if (int32 != null) {
            values.setSignedInt32((long) toSigned(int32.raw, 32));
            values.setUnsignedInt32(int32.raw);
        }

        // 48-bit
        RawValue int48 = calculateRaw(data, offset, byteOrder, 6);
        if (int48 != null) {
            values.setSignedInt48(toSigned(int48.raw, 48));
            values.setUnsignedInt48(int48.raw);
        }

        // 64-bit
        RawValue int64 = calculateRaw(data, offset, byteOrder, 8);
        if (int64 != null) {
            values.setSignedInt64((long) toSigned(int64.raw, 64));
            values.setUnsignedInt64(
                int64.raw >= 0
                    ? BigInteger.valueOf(int64.raw)
                    : new BigInteger(1, Arrays.copyOfRange(data, offset, offset + 8))
            );
        }
    }

    public static void calculateFloat(Values values, byte[] data, int offset, ByteOrder byteOrder) {
        // 32-bit float
        if (data != null && offset >= 0 && offset + 4 <= data.length) {
            int intBits;
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                intBits = ((data[offset] & 0xFF) << 24)
                        | ((data[offset + 1] & 0xFF) << 16)
                        | ((data[offset + 2] & 0xFF) << 8)
                        | (data[offset + 3] & 0xFF);
            } else {
                intBits = ((data[offset + 3] & 0xFF) << 24)
                        | ((data[offset + 2] & 0xFF) << 16)
                        | ((data[offset + 1] & 0xFF) << 8)
                        | (data[offset] & 0xFF);
            }
            values.setFloat32(Float.intBitsToFloat(intBits));
        }

        // 64-bit float
        if (data != null && offset >= 0 && offset + 8 <= data.length) {
            long longBits;
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                longBits = ((long) (data[offset] & 0xFF) << 56)
                        | ((long) (data[offset + 1] & 0xFF) << 48)
                        | ((long) (data[offset + 2] & 0xFF) << 40)
                        | ((long) (data[offset + 3] & 0xFF) << 32)
                        | ((long) (data[offset + 4] & 0xFF) << 24)
                        | ((long) (data[offset + 5] & 0xFF) << 16)
                        | ((long) (data[offset + 6] & 0xFF) << 8)
                        | ((long) (data[offset + 7] & 0xFF));
            } else {
                longBits = ((long) (data[offset + 7] & 0xFF) << 56)
                        | ((long) (data[offset + 6] & 0xFF) << 48)
                        | ((long) (data[offset + 5] & 0xFF) << 40)
                        | ((long) (data[offset + 4] & 0xFF) << 32)
                        | ((long) (data[offset + 3] & 0xFF) << 24)
                        | ((long) (data[offset + 2] & 0xFF) << 16)
                        | ((long) (data[offset + 1] & 0xFF) << 8)
                        | ((long) (data[offset] & 0xFF));
            }
            values.setFloat64(Double.longBitsToDouble(longBits));
        }
    }

    public static void calculateChars(Values values, byte[] data, int offset, ByteOrder byteOrder) {
        // char8
        if (offset >= 0 && offset + 1 <= data.length) {
            char char8 = (char) (data[offset] & 0xFF);
            values.setChar8(char8);
        }

        // char16
        if (offset >= 0 && offset + 2 <= data.length) {
            char char16;
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                char16 = (char) ((data[offset] << 8) | (data[offset + 1] & 0xFF));
            } else {
                char16 = (char) ((data[offset + 1] << 8) | (data[offset] & 0xFF));
            }
            values.setChar16(char16);
        }
    }

    public static void calculateUnicode(Values values, byte[] data, int offset, ByteOrder byteOrder) {
        if (data == null || offset < 0) {
            return;
        }

        // UTF-8 detection (1-4 bytes)
        if (offset < data.length) {
            int b1 = data[offset] & 0xFF;
            int utf8Size = 0;
            String utf8Char = null;

            if ((b1 & 0x80) == 0) {  // 1-byte (ASCII)
                utf8Size = 8;
                utf8Char = new String(data, offset, 1, StandardCharsets.UTF_8);
            } else if ((b1 & 0xE0) == 0xC0 && offset + 1 < data.length) {  // 2-byte
                utf8Size = 16;
                utf8Char = new String(data, offset, 2, StandardCharsets.UTF_8);
            } else if ((b1 & 0xF0) == 0xE0 && offset + 2 < data.length) {  // 3-byte
                utf8Size = 24;
                utf8Char = new String(data, offset, 3, StandardCharsets.UTF_8);
            } else if ((b1 & 0xF8) == 0xF0 && offset + 3 < data.length) {  // 4-byte
                utf8Size = 32;
                utf8Char = new String(data, offset, 4, StandardCharsets.UTF_8);
            }

            if (utf8Char != null && !utf8Char.isEmpty()) {
                values.setUtf8Char(utf8Char);
                values.setUtf8Size(utf8Size);
            }
        }

        // UTF-16 detection (2 or 4 bytes)
        if (offset + 1 < data.length) {
            char c1 = (byteOrder == ByteOrder.BIG_ENDIAN)
                    ? (char) ((data[offset] << 8) | (data[offset + 1] & 0xFF))
                    : (char) ((data[offset + 1] << 8) | (data[offset] & 0xFF));

            if (Character.isHighSurrogate(c1) && offset + 3 < data.length) {
                // Handle surrogate pair (4 bytes)
                char c2 = (byteOrder == ByteOrder.BIG_ENDIAN)
                        ? (char) ((data[offset + 2] << 8) | (data[offset + 3] & 0xFF))
                        : (char) ((data[offset + 3] << 8) | (data[offset + 2] & 0xFF));

                if (Character.isLowSurrogate(c2)) {
                    values.setUtf16Char(new String(new char[]{c1, c2}));
                    values.setUtf16Size(32);

                }
            } else {
                // Regular 2-byte character
                values.setUtf16Char(String.valueOf(c1));
                values.setUtf16Size(16);
            }
        }

        // UTF-32 detection (4 bytes)
        if (offset + 3 < data.length) {
            int codePoint = (byteOrder == ByteOrder.BIG_ENDIAN)
                    ? ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                    | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF)
                    : ((data[offset + 3] & 0xFF) << 24) | ((data[offset + 2] & 0xFF) << 16)
                    | ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF);

            if (Character.isValidCodePoint(codePoint)) {
                values.setUtf32Char(new String(Character.toChars(codePoint)));
            }

        }
    }

    private static RawValue calculateRaw(byte[] data, int offset, ByteOrder byteOrder, int numBytes) {
        if (data == null || offset < 0 || offset + numBytes > data.length) {
            return null;
        }

        long value = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < numBytes; i++) {
                value = (value << 8) | (data[offset + i] & 0xFF);
            }
        } else {
            for (int i = 0; i < numBytes; i++) {
                value = (value << 8) | (data[offset + numBytes - 1 - i] & 0xFF);
            }
        }
        return new RawValue(value, numBytes);
    }

    private static long toSigned(long rawValue, int numBits) {
        boolean isNegative = (rawValue & (1L << (numBits - 1))) != 0;
        return isNegative ? rawValue - (1L << numBits) : rawValue;
    }

    private ValueCalculator() {
        //empty
    }
}

