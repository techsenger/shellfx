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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.core.history.HistoryManager;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultFindViewModelTest {

    private static final class TestFindPanelViewModel extends DefaultFindPanelViewModel {

        private TestFindPanelViewModel(FindMatchesResetPolicy resetPolicy, HistoryManager historyManager) {
            super(resetPolicy, historyManager);
            setMediator(new DummyMediator());
        }

        public void setText(String text) {
            getTextWrapper().set(text);
        }

    }

    @Test
    public void find_noMatchCaseNoWholeWordNoRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("ansi4j");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(8);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "ansi4j",
                "ansi4j",
                "ANSI4J",
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j"
        ));
    }

    @Test
    public void find_yesMatchCaseNoWholeWordNoRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.getMatchCase().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("ansi4j");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(7);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j",
                "ansi4j"
        ));
    }

    @Test
    public void find_noMatchCaseYesWholeWordNoRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.getWholeWord().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("version");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(3);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "version",
                "version",
                "Version"
        ));
    }

    @Test
    public void find_yesMatchCaseYesWholeWordNoRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.getWholeWord().setSelected(true);
        viewModel.getMatchCase().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("version");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "version",
                "version"
        ));
    }

    @Test
    public void find_noMatchCaseNoWholeWordYesRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.getRegExp().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("an.*(?=<)");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(9);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "ansi4j",
                "ansi4j",
                "ANSI4J",
                "anuary 2020 has many features",
                "ansi4j-core-api",
                "ansi4j-core-impl",
                "ansi4j-core-it",
                "ansi4j-css-api",
                "ansi4j-css-impl"
        ));
    }

    @Test
    public void find_yesMatchCaseNoWholeWordYesRegExp_success() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.replaceModeProperty().set(true);
        viewModel.getRegExp().setSelected(true);
        viewModel.getMatchCase().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("an.*(?=<)");
        viewModel.find();
        assertThat(viewModel.getMatchRanges()).hasSize(8);
        var subStrings = Texts.rangesToSubStrings(viewModel.getMatchRanges(), Texts.TEXT);
        assertThat(subStrings).containsExactlyElementsOf(List.of(
                "ansi4j",
                "ansi4j",
                "anuary 2020 has many features",
                "ansi4j-core-api",
                "ansi4j-core-impl",
                "ansi4j-core-it",
                "ansi4j-css-api",
                "ansi4j-css-impl"
        ));
    }

    @Test
    public void moveToPreviousRange_withReplacedAndCaretIsRightAfterReplacedRange_movedToNotReplaced() {
        var viewModel = this.createViewModelForMoveTests();
        var ranges = viewModel.getMatchRanges();
        ranges.get(1).setReplaced(true);
        viewModel.caretPositionProperty().set(ranges.get(1).getEnd());
        viewModel.moveToPreviousRange();
        assertThat(viewModel.getMatchRange().getIndex()).isEqualTo(0);
    }

    @Test
    public void moveToPreviousRange_withReplacedAndCaretIsRightAfterCurrentRange_movedToNotReplaced() {
        var viewModel = this.createViewModelForMoveTests();
        var ranges = viewModel.getMatchRanges();
        ranges.get(1).setReplaced(true);
        viewModel.caretPositionProperty().set(ranges.get(2).getEnd() + 1); //making 2 current
        viewModel.moveToPreviousRange();
        viewModel.caretPositionProperty().set(ranges.get(2).getEnd());
        viewModel.moveToPreviousRange();
        assertThat(viewModel.getMatchRange().getIndex()).isEqualTo(0);
    }

    @Test
    public void moveToNextRange_withReplacedAndCaretIsRightBeforeReplacedRange_movedToNotReplaced() {
        var viewModel = this.createViewModelForMoveTests();
        var ranges = viewModel.getMatchRanges();
        ranges.get(0).setReplaced(true);
        viewModel.caretPositionProperty().set(ranges.get(0).getStart());
        viewModel.moveToNextRange();
        assertThat(viewModel.getMatchRange().getIndex()).isEqualTo(1);
    }

    @Test
    public void moveToNextRange_withReplacedAndCaretIsRightBeforeCurrentRange_movedToNotReplaced() {
        var viewModel = this.createViewModelForMoveTests();
        var ranges = viewModel.getMatchRanges();
        ranges.get(1).setReplaced(true);
        viewModel.caretPositionProperty().set(0); //making 1 current
        viewModel.moveToNextRange();
        viewModel.caretPositionProperty().set(ranges.get(0).getStart());
        viewModel.moveToNextRange();
        assertThat(viewModel.getMatchRange().getIndex()).isEqualTo(0);
    }

    private DefaultFindPanelViewModel<?> createViewModelForMoveTests() {
        var viewModel = new TestFindPanelViewModel(FindMatchesResetPolicy.AUTOMATIC, new DummyHistoryManager());
        viewModel.textAreaEditableProperty().set(true);
        viewModel.replaceModeProperty().set(true);
        viewModel.getRegExp().setSelected(true);
        viewModel.getMatchCase().setSelected(true);
        viewModel.setText(Texts.TEXT);
        viewModel.findTextProperty().set("an.*(?=<)");
        viewModel.find();
        return viewModel;
    }
}
