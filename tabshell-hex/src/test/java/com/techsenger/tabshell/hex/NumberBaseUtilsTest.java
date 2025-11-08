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

package com.techsenger.tabshell.hex;

import com.techsenger.tabshell.hex.editor.NumberBase;
import com.techsenger.tabshell.hex.editor.NumberBaseUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class NumberBaseUtilsTest {

    private static final byte[] EMPTY_DATA = new byte[0];

    private static final byte[] SMALL_DATA = new byte[64];    // 64 bytes (max offset = 63)

    private static final byte[] LARGE_DATA = new byte[10000];  // max offset = 9999

    @Test
    void calculateLength_nullData_returns1() {
        assertThat(NumberBaseUtils.calculateOffsetLength(null, NumberBase.DEC)).isEqualTo(1);
    }

    @Test
    void calculateLength_emptyData_returns1() {
        assertThat(NumberBaseUtils.calculateOffsetLength(EMPTY_DATA, NumberBase.OCT)).isEqualTo(1);
    }

    @Test
    void calculateLength_smallDataDecimal_returns2() {
        assertThat(NumberBaseUtils.calculateOffsetLength(SMALL_DATA, NumberBase.DEC)).isEqualTo(2);  // 63 → 2 digits
    }

    @Test
    void calculateLength_smallDataOctal_returns2() {
        assertThat(NumberBaseUtils.calculateOffsetLength(SMALL_DATA, NumberBase.OCT)).isEqualTo(2);  // 77 → 2 digits
    }

    @Test
    void calculateLength_largeDataDecimal_returns4() {
        assertThat(NumberBaseUtils.calculateOffsetLength(LARGE_DATA, NumberBase.DEC)).isEqualTo(4);  // 9999 → 4 digits
    }

    @Test
    void calculateLength_largeDataOctal_returns5() {
        assertThat(NumberBaseUtils.calculateOffsetLength(LARGE_DATA, NumberBase.OCT)).isEqualTo(5);  // 23417 → 5 digits
    }

    @Test
    void calculateLength_singleByteData_returns1() {
        // 0 → 1 digit
        assertThat(NumberBaseUtils.calculateOffsetLength(new byte[1], NumberBase.DEC)).isEqualTo(1);
    }

    @Test
    void calculateLength_binaryBase_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> NumberBaseUtils.calculateOffsetLength(SMALL_DATA, NumberBase.BIN))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Not supported base");
    }

}
