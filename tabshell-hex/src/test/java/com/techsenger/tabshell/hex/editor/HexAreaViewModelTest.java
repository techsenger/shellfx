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

import com.techsenger.tabshell.hex.model.ByteRange;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class HexAreaViewModelTest {

    @Test
    void createSelection_whenSingleRowSelection_returnsSimpleType() {
        ByteRange selection = new ByteRange(5, 10);
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.SIMPLE);
        assertThat(result.getRowCount()).isEqualTo(1);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(5);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(5);
        assertThat(result.getLastRow()).isNull();
    }

    @Test
    void createSelection_whenMultiRowNonOverlapping_returnsSimpleType() {
        ByteRange selection = new ByteRange(8, 24); // bytes 8-23: last 8 of row0, first 8 of row1
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.SIMPLE);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(8);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(8);
        assertThat(result.getLastRow().getIndex()).isEqualTo(1);
        assertThat(result.getLastRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getLastRow().getByteCount()).isEqualTo(8);
    }

    @Test
    void createSelection_whenMultiRowOverlapping_returnsJoinedType() {
        ByteRange selection = new ByteRange(8, 25); // bytes 8-24: last 8 of row0, first 9 of row1
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.JOINED);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(8);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(8);
        assertThat(result.getLastRow().getIndex()).isEqualTo(1);
        assertThat(result.getLastRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getLastRow().getByteCount()).isEqualTo(9);
    }

    @Test
    void createSelection_whenFullRowSelection_returnsJoinedType() {
        ByteRange selection = new ByteRange(0, 32); // two full rows
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.JOINED);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(16);
        assertThat(result.getLastRow().getIndex()).isEqualTo(1);
        assertThat(result.getLastRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getLastRow().getByteCount()).isEqualTo(16);
    }

    @Test
    void createSelection_whenThreeRowsWithMiddleFull_returnsJoinedType() {
        ByteRange selection = new ByteRange(4, 44); // last 12 of row0, full row1, first 12 of row2
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.JOINED);
        assertThat(result.getRowCount()).isEqualTo(3);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(0);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(4);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(12);
        assertThat(result.getLastRow().getIndex()).isEqualTo(2);
        assertThat(result.getLastRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getLastRow().getByteCount()).isEqualTo(12);
    }

    @Test
    void createSelection_whenSelectionAtFileEnd_returnsCorrectRowCounts() {
        ByteRange selection = new ByteRange(90, 100); // near end of file
        int contentLength = 100;
        int rowByteCount = 16;
        SelectionInfo result = HexAreaViewModel.createSelection(selection, contentLength, rowByteCount);

        assertThat(result.getType()).isEqualTo(SelectionInfo.SelectionType.SIMPLE);
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getFirstRow().getIndex()).isEqualTo(5);
        assertThat(result.getFirstRow().getByteIndex()).isEqualTo(10);
        assertThat(result.getFirstRow().getByteCount()).isEqualTo(6);
        assertThat(result.getLastRow().getIndex()).isEqualTo(6);
        assertThat(result.getLastRow().getByteIndex()).isEqualTo(0);
        assertThat(result.getLastRow().getByteCount()).isEqualTo(4);
    }

    @Test
    void createSelection_whenNullSelection_throwsException() {
        ByteRange selection = null;
        int contentLength = 100;
        int rowByteCount = 16;

        assertThatThrownBy(() -> HexAreaViewModel.createSelection(selection, contentLength, rowByteCount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Selection cannot be null");
    }
}
