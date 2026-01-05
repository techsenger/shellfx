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

package com.techsenger.tabshell.hex.editor;

import java.util.Arrays;
import java.util.HexFormat;

/**
 *
 * @author Pavel Castornii
 */
public final class NumberBaseUtils {

    private static final HexFormat hexFormat = HexFormat.of().withUpperCase();

    /**
     * Calculates the maximum length (in characters) needed to display any offset in the specified numeric base
     * (binary is not supported).
     *
     * @param data the file data array where data.length represents file size in bytes
     * @param numberBase the number base.
     * @return maximum character length required for offset representation
     * @throws IllegalArgumentException if numberBase is {@link NumberBase#BIN}
     */
    public static int calculateOffsetLength(byte[] data, NumberBase numberBase) {
        if (numberBase == NumberBase.BIN) {
            throw new IllegalArgumentException("Not supported base");
        }
        if (data == null || data.length == 0) {
            return 1;
        }

        int maxOffset = data.length - 1;
        if (maxOffset == 0) {
            return 1; // for log(0)
        }

        return (int) (Math.log(maxOffset) / Math.log(numberBase.getValue())) + 1;
    }

    /**
     * Converts an integer value to a specified number base (hex, octal, or decimal) and formats the result with
     * leading zeros to match the desired length.
     *
     * @param value  the integer value to convert (can be negative)
     * @param base   the target number base (HEX, OCT, or DEC)
     * @param length the desired length of the output string (truncates or pads with zeros)
     * @return the formatted string in the specified base, with leading zeros if needed
     * @throws AssertionError if an unsupported number base is provided
     */
    public static String convert(int value, NumberBase base, int length) {
        String result;
        switch (base) {
            case HEX:
                result = Integer.toHexString(value);
                break;
            case OCT:
                result = Integer.toOctalString(value);
                break;
            case DEC:
                result = Integer.toString(value);
                break;
            default:
                throw new AssertionError("Unknown number base: " + base);
        }

        // Handle negative numbers (preserve the '-' sign)
        boolean negative = value < 0 && !result.startsWith("-");
        if (negative) {
            result = "-" + result;
        }

        int resultLength = result.length();
        if (resultLength == length) {
            return base == NumberBase.HEX ? result.toUpperCase() : result;
        } else if (resultLength > length) {
            // If too long, take rightmost 'length' characters
            return (base == NumberBase.HEX ? result.toUpperCase() : result)
                    .substring(resultLength - length);
        } else {
            // If too short, prepend zeros
            int zerosNeeded = length - resultLength;
            char[] zeros = new char[zerosNeeded];
            Arrays.fill(zeros, '0');
            String padded = new String(zeros) + result;
            return base == NumberBase.HEX ? padded.toUpperCase() : padded;
        }
    }

    public static String convertToHex(byte value) {
        return hexFormat.toHexDigits(value);
    }

    private NumberBaseUtils() {
        //empty
    }
}
