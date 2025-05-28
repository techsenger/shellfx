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

/**
 *
 * @author Pavel Castornii
 */
public class Bases {

    private final String decimal;

    private final String hexadecimal;

    private final String octal;

    private final String binary;

    public Bases(String decimal, String hexadecimal, String octal, String binary) {
        this.decimal = decimal;
        this.hexadecimal = hexadecimal;
        this.octal = octal;
        this.binary = binary;
    }

    public String getDecimal() {
        return decimal;
    }

    public String getHexadecimal() {
        return hexadecimal;
    }

    public String getOctal() {
        return octal;
    }

    public String getBinary() {
        return binary;
    }
}
