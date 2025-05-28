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

/**
 *
 * @author Pavel Castornii
 */
public final class BaseConverters {

    public static Bases convert(Integer value) {
        return new Bases(
                value.toString(),
                Integer.toHexString(value).toUpperCase(),
                Integer.toOctalString(value),
                Integer.toBinaryString(value));
    }

    public static Bases convert(Long value) {
        return new Bases(
                value.toString(),
                Long.toHexString(value).toUpperCase(),
                Long.toOctalString(value),
                Long.toBinaryString(value));
    }

    public static Bases convert(BigInteger value) {
        return new Bases(
                value.toString(),
                value.toString(16).toUpperCase(),
                value.toString(8),
                value.toString(2));
    }

    public static Bases convert(Character value) {
        var num = Character.getNumericValue(value);
        if (num >= 0) {
            return convert(num);
        } else {
            return null;
        }
    }

    public static Bases convert(String value) {
        return convert(value.codePointAt(0));
    }

    private BaseConverters() {
        //empty
    }

}
