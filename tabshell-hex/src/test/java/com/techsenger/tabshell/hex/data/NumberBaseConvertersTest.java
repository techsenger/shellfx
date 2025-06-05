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
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class NumberBaseConvertersTest {

    @Test
    public void convert_whenLongForMinSignedInt8_returnsCorrectBases() {
        long value = -128L;
        String decimal = "-128";
        String hexadecimal = "80";
        String octal = "200";
        String binary = "10000000";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForSignedInt8_returnsCorrectBases() {
        long value = -90L;
        String decimal = "-90";
        String hexadecimal = "A6";
        String octal = "246";
        String binary = "10100110";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt8_returnsCorrectBases() {
        long value = 127L;
        String decimal = "127";
        String hexadecimal = "7F";
        String octal = "177";
        String binary = "01111111";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinUnsignedInt8_returnsCorrectBases() {
        long value = 0L;
        String decimal = "0";
        String hexadecimal = "00";
        String octal = "000";
        String binary = "00000000";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForUnsignedInt8_returnsCorrectBases() {
        long value = 140L;
        String decimal = "140";
        String hexadecimal = "8C";
        String octal = "214";
        String binary = "10001100";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxUnsignedInt8_returnsCorrectBases() {
        long value = 255L;
        String decimal = "255";
        String hexadecimal = "FF";
        String octal = "377";
        String binary = "11111111";
        var bases = NumberBaseConverters.convert(value, 8);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinSignedInt16_returnsCorrectBases() {
        long value = -32_768L;
        String decimal = "-32768";
        String hexadecimal = "8000";
        String octal = "100000";
        String binary = "1000000000000000";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForSignedInt16_returnsCorrectBases() {
        long value = -12_039L;
        String decimal = "-12039";
        String hexadecimal = "D0F9";
        String octal = "150371";
        String binary = "1101000011111001";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt16_returnsCorrectBases() {
        long value = 32_767L;
        String decimal = "32767";
        String hexadecimal = "7FFF";
        String octal = "077777";
        String binary = "0111111111111111";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */


    @Test
    public void convert_whenLongForMinUnsignedInt16_returnsCorrectBases() {
        long value = 0L;
        String decimal = "0";
        String hexadecimal = "0000";
        String octal = "000000";
        String binary = "0000000000000000";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForUnsignedInt16_returnsCorrectBases() {
        long value = 32_861L;
        String decimal = "32861";
        String hexadecimal = "805D";
        String octal = "100135";
        String binary = "1000000001011101";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxUnsignedInt16_returnsCorrectBases() {
        long value = 65_535L;
        String decimal = "65535";
        String hexadecimal = "FFFF";
        String octal = "177777";
        String binary = "1111111111111111";
        var bases = NumberBaseConverters.convert(value, 16);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinSignedInt24_returnsCorrectBases() {
        long value = -8_388_608L;
        String decimal = "-8388608";
        String hexadecimal = "800000";
        String octal = "40000000";
        String binary = "100000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForSignedInt24_returnsCorrectBases() {
        long value = -6_184_273L;
        String decimal = "-6184273";
        String hexadecimal = "A1A2AF";
        String octal = "50321257";
        String binary = "101000011010001010101111";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt24_returnsCorrectBases() {
        long value = 8_388_607L;
        String decimal = "8388607";
        String hexadecimal = "7FFFFF";
        String octal = "37777777";
        String binary = "011111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinUnsignedInt24_returnsCorrectBases() {
        long value = 0L;
        String decimal = "0";
        String hexadecimal = "000000";
        String octal = "00000000";
        String binary = "000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForUnsignedInt24_returnsCorrectBases() {
        long value = 287_239L;
        String decimal = "287239";
        String hexadecimal = "046207";
        String octal = "01061007";
        String binary = "000001000110001000000111";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxUnsignedInt24_returnsCorrectBases() {
        long value = 16_777_215L;
        String decimal = "16777215";
        String hexadecimal = "FFFFFF";
        String octal = "77777777";
        String binary = "111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 24);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinSignedInt32_returnsCorrectBases() {
        long value = -2_147_483_648L;
        String decimal = "-2147483648";
        String hexadecimal = "80000000";
        String octal = "20000000000";
        String binary = "10000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);

    }

    @Test
    public void convert_whenLongForSignedInt32_returnsCorrectBases() {
        long value = -2_432_111L;
        String decimal = "-2432111";
        String hexadecimal = "FFDAE391";
        String octal = "37766561621";
        String binary = "11111111110110101110001110010001";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt32_returnsCorrectBases() {
        long value = 2_147_483_647L;
        String decimal = "2147483647";
        String hexadecimal = "7FFFFFFF";
        String octal = "17777777777";
        String binary = "01111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinUnsignedInt32_returnsCorrectBases() {
        long value = 0L;
        String decimal = "0";
        String hexadecimal = "00000000";
        String octal = "00000000000";
        String binary = "00000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForUnsignedInt32_returnsCorrectBases() {
        long value = 145_002L;
        String decimal = "145002";
        String hexadecimal = "0002366A";
        String octal = "00000433152";
        String binary = "00000000000000100011011001101010";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxUnsignedInt32_returnsCorrectBases() {
        long value = 4_294_967_295L;
        String decimal = "4294967295";
        String hexadecimal = "FFFFFFFF";
        String octal = "37777777777";
        String binary = "11111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinSignedInt48_returnsCorrectBases() {
        long value = -140_737_488_355_328L;
        String decimal = "-140737488355328";
        String hexadecimal = "800000000000";
        String octal = "4000000000000000";
        String binary = "100000000000000000000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);

    }

    @Test
    public void convert_whenLongForSignedInt48_returnsCorrectBases() {
        //no reliable data for the test
        long value = -101_222_500_007L;
        String decimal = "-101222500007";
        String hexadecimal = "FFE86EAB3959";
        String octal = "7776415652634531";
        String binary = "111111111110100001101110101010110011100101011001";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt48_returnsCorrectBases() {
        long value = 140_737_488_355_327L;
        String decimal = "140737488355327";
        String hexadecimal = "7FFFFFFFFFFF";
        String octal = "3777777777777777";
        String binary = "011111111111111111111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinUnsignedInt48_returnsCorrectBases() {
        long value = 0L;
        String decimal = "0";
        String hexadecimal = "000000000000";
        String octal = "0000000000000000";
        String binary = "000000000000000000000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForUnsignedInt48_returnsCorrectBases() {
        //no reliable data for the test
        long value = 83_333_756_657L;
        String decimal = "83333756657";
        String hexadecimal = "0013671436F1";
        String octal = "0001154705033361";
        String binary = "000000000001001101100111000101000011011011110001";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxUnsignedInt48_returnsCorrectBases() {
        long value = 281_474_976_710_655L;
        String decimal = "281474976710655";
        String hexadecimal = "FFFFFFFFFFFF";
        String octal = "7777777777777777";
        String binary = "111111111111111111111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 48);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenLongForMinSignedInt64_returnsCorrectBases() {
        long value = -9_223_372_036_854_775_808L;
        String decimal = "-9223372036854775808";
        String hexadecimal = "8000000000000000";
        String octal = "1000000000000000000000";
        String binary = "1000000000000000000000000000000000000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }


    @Test
    public void convert_whenLongForSignedInt64_returnsCorrectBases() {
        long value = -445_395_234_834_192L;
        String decimal = "-445395234834192";
        String hexadecimal = "FFFE6AEA573E38F0";
        String octal = "1777763256512717434360";
        String binary = "1111111111111110011010101110101001010111001111100011100011110000";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenLongForMaxSignedInt64_returnsCorrectBases() {
        long value = 9_223_372_036_854_775_807L;
        String decimal = "9223372036854775807";
        String hexadecimal = "7FFFFFFFFFFFFFFF";
        String octal = "0777777777777777777777";
        String binary = "0111111111111111111111111111111111111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    /* ************************************************************************************************************* */

    @Test
    public void convert_whenBigIntegerForMinUnsignedInt64_returnsCorrectBases() {
        BigInteger value = new BigInteger("0");
        String decimal = "0";
        String hexadecimal = "0000000000000000";
        String octal = "0000000000000000000000";
        String binary = "0000000000000000000000000000000000000000000000000000000000000000";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenBigIntegerForUnsignedInt64_returnsCorrectBases() {
        var value = new BigInteger("203948364738475");
        String decimal = "203948364738475";
        String hexadecimal = "0000B97D6DD52FAB";
        String octal = "0000005627655565227653";
        String binary = "0000000000000000101110010111110101101101110101010010111110101011";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_whenBigIntegerForMaxUnsignedInt64_returnsCorrectBases() {
        var value = new BigInteger("18446744073709551615", 10);
        String decimal = "18446744073709551615";
        String hexadecimal = "FFFFFFFFFFFFFFFF";
        String octal = "1777777777777777777777";
        String binary = "1111111111111111111111111111111111111111111111111111111111111111";
        var bases = NumberBaseConverters.convert(value, 64);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }


    /* ************************************************************************************************************* */

    @Test
    public void convert_positiveBigInteger32_returnsCorrectBases() {
        var value = new BigInteger("145002", 10);
        String decimal = "145002";
        String hexadecimal = "0002366A";
        String octal = "00000433152";
        String binary = "00000000000000100011011001101010";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    @Test
    public void convert_negativeBigInteger32_returnsCorrectBases() {
        var value = new BigInteger("-2432111");
        String decimal = "-2432111";
        String hexadecimal = "FFDAE391";
        String octal = "37766561621";
        String binary = "11111111110110101110001110010001";
        var bases = NumberBaseConverters.convert(value, 32);
        assertBases(decimal, hexadecimal, octal, binary, bases);
    }

    private void assertBases(String dec, String hex, String oct, String bin, NumberBases bases) {
        assertThat(bases.getDecimal()).isEqualTo(dec);
        assertThat(bases.getHexadecimal()).isEqualTo(hex);
        assertThat(bases.getOctal()).isEqualTo(oct);
        assertThat(bases.getBinary()).isEqualTo(bin);
    }
}
