/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.hex.data;

import java.math.BigInteger;
import java.util.Arrays;

/**
 *
 * @author Pavel Castornii
 */
public final class BaseConverters {

    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    public static NumberBases convert(Character value) {
        long num = Character.getNumericValue(value);
        if (num >= 0) {
            return convert(num, 16);
        } else {
            return null;
        }
    }

    public static NumberBases convert(String value, int bits) {
        long v = value.codePointAt(0);
        return convert(v, bits);
    }

    /**
    * Converts the given {@code value} into different numerical representations, taking into account the actual
    * byte size of the value.
    *
    * @param value the numeric value to convert (never {@code null}).
    * @param bits the number of bits to consider. Supported values are: 8, 16, 24, 32, 48, and 64.
    * @return a {@link NumberBases} instance containing multiple representations of the value.
    */
    public static NumberBases convert(Long value, int bits) {
        return new NumberBases(
                value.toString(),
                toHexString(value, bits),
                toOctalString(value, bits),
                toBinaryString(value, bits));
    }

    /**
    * Converts the given {@code value} into different numerical representations, taking into account the actual
    * byte size of the value.
    *
    * <p>This function is slow. Use {@link #convert(java.lang.Long, int)} when possible.
    *
    * @param value the numeric value to convert (never {@code null}).
    * @param bits the number of bits to consider. Supported values are: 8, 16, 24, 32, 48, and 64.
    * @return a {@link NumberBases} instance containing multiple representations of the value.
    */
    public static NumberBases convert(BigInteger value, int bits) {
        BigInteger maxValue = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);

        // Get the unsigned value for binary/hex/octal representations
        BigInteger unsignedValue = value.and(maxValue);
        if (value.signum() < 0) {
            unsignedValue = value.mod(BigInteger.ONE.shiftLeft(bits));
        }

        // Pre-calculate required lengths for each base
        int hexLength = (bits + 3) / 4;  // 4 bits per hex digit
        int octalLength = (bits + 2) / 3; // 3 bits per octal digit
        int binaryLength = bits;          // 1 bit per binary digit

        // Convert to strings with leading zeros
        String hex = toPaddedString(unsignedValue, 16, hexLength);
        String octal = toPaddedString(unsignedValue, 8, octalLength);
        String binary = toPaddedString(unsignedValue, 2, binaryLength);

        return new NumberBases(
                value.toString(),  // Keep original decimal representation with sign
                hex.toUpperCase(),
                octal,
                binary
        );
    }

    private static String toPaddedString(BigInteger value, int radix, int length) {
        String str = value.toString(radix);
        if (str.length() >= length) {
            return str;
        }

        // Pad with leading zeros
        char[] zeros = new char[length - str.length()];
        Arrays.fill(zeros, '0');
        return new String(zeros) + str;
    }

    private static String toHexString(Long value, int bits) {
        int chars = bits / 4;
        char[] result = new char[chars];

        long mask = 0xFL << (bits - 4);
        for (int i = 0; i < chars; i++) {
            int digit = (int) ((value & mask) >>> (bits - (i + 1) * 4));
            result[i] = HEX_DIGITS[digit];
            mask >>>= 4;
        }
        return new String(result);
    }

    private static String toOctalString(Long value, int bits) {
        int chars = (bits + 2) / 3;
        char[] result = new char[chars];
        long maskedValue = (bits == 64) ? value : value & ((1L << bits) - 1);
        for (int i = chars - 1; i >= 0; i--) {
            result[i] = (char) ('0' + (maskedValue & 0x7));
            maskedValue >>>= 3;
        }
        return new String(result);
    }

    private static String toBinaryString(Long value, int bits) {
        char[] result = new char[bits];

        long mask = 1L << (bits - 1);
        for (int i = 0; i < bits; i++) {
            result[i] = (value & mask) != 0 ? '1' : '0';
            mask >>>= 1;
        }
        return new String(result);
    }

    private BaseConverters() {
        //empty
    }

}
