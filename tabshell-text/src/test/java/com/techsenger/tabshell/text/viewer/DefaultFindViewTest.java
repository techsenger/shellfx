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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.material.textarea.TextAreaStyle;
import com.techsenger.toolkit.fx.FxPlatform;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.techsenger.tabshell.material.textarea.TextAreaStyleNames;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultFindViewTest {

    @BeforeAll
    public static void doBeforeAll() {
        FxPlatform.start();
    }

    @Test @Disabled
    public void replace_autoMovingToNext_success() {
        var textArea = new ExtendedTextArea(Texts.TEXT);
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        viewModel.regExpSelectedProperty().set(true);
        view.getFindComboBox().getEditor().setText("an.*(?=<)");
        view.getReplaceComboBox().getEditor().setText("test");
        viewModel.findNext();
        view.selectNextRange(false);
        for (var i = 0; i < 9; i++) {
            view.replace();
        }
        assertThat(textArea.getText()).isEqualTo(Texts.REPLACED_ALL_TEXT);
    }

    @Test
    public void replaceAll_textPresent_success() {
        var textArea = new ExtendedTextArea(Texts.TEXT);
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        viewModel.regExpSelectedProperty().set(true);
        view.getFindComboBox().getEditor().setText("an.*(?=<)");
        view.getReplaceComboBox().getEditor().setText("test");
        view.replaceAll();
        assertThat(textArea.getText()).isEqualTo(Texts.REPLACED_ALL_TEXT);
    }

    @Test
    public void removeHighlighting_insertionStyleSpanModifed_highlightingRemoved() {
        var textArea = new ExtendedTextArea("ansiansi");
        var view = this.createView(textArea);
        view.getHighlightButton().setSelected(true);
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        textArea.insert(6, " ", TextAreaStyle.EMPTY);
        assertThat(textArea.getText()).isEqualTo("ansian si");
        var spans = textArea.getStyleSpans(0, textArea.getText().length());
        //note - spans with same style insance are merged
        assertThat(spans.getSpanCount()).isEqualTo(2);
        var iterator = spans.iterator();
        var span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
        span = iterator.next();
        assertThat(span.getLength()).isEqualTo(5);
        assertThat(span.getStyle()).hasSize(0);
    }

    @Test
    public void removeHighlighting_insertionStyleSpanNotModified_highlightingNotRemoved() {
        var textArea = new ExtendedTextArea("ansiansi");
        var view = this.createView(textArea);
        view.getHighlightButton().setSelected(true);
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        textArea.insert(4, " ", TextAreaStyle.EMPTY);
        assertThat(textArea.getText()).isEqualTo("ansi ansi");
        var spans = textArea.getStyleSpans(0, textArea.getText().length());
        //note - spans with same style insance are merged
        assertThat(spans.getSpanCount()).isEqualTo(3);
        var iterator = spans.iterator();
        var span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
        span = iterator.next();
        assertThat(span.getLength()).isEqualTo(1);
        assertThat(span.getStyle()).hasSize(0);
        span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
    }

    @Test
    public void removeHighlighting_removalStyleSpanModifed_highlightingRemoved() {
        var textArea = new ExtendedTextArea("ansiansi");
        var view = this.createView(textArea);
        view.getHighlightButton().setSelected(true);
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        textArea.deleteText(6, 7);
        assertThat(textArea.getText()).isEqualTo("ansiani");
        var spans = textArea.getStyleSpans(0, textArea.getText().length());
        //note - spans with same style insance are merged
        assertThat(spans.getSpanCount()).isEqualTo(2);
        var iterator = spans.iterator();
        var span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
        span = iterator.next();
        assertThat(span.getLength()).isEqualTo(3);
        assertThat(span.getStyle()).hasSize(0);
    }

    @Test
    public void removeHighlighting_removalStyleSpanNotModified_highlightingNotRemoved() {
        var textArea = new ExtendedTextArea("ansi ansi");
        var view = this.createView(textArea);
        view.getHighlightButton().setSelected(true);
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        textArea.deleteText(4, 5);
        assertThat(textArea.getText()).isEqualTo("ansiansi");
        var spans = textArea.getStyleSpans(0, textArea.getText().length());
        //note - spans with same style insance are merged
        assertThat(spans.getSpanCount()).isEqualTo(2);
        var iterator = spans.iterator();
        var span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
        span = iterator.next();
        assertThat(span.getLength()).isEqualTo(4);
        assertThat(span.getStyle().iterator().next().getName()).isEqualTo(TextAreaStyleNames.FIND_HIGHLIGHT);
    }

    @Test
    public void doOnTextChange_deletionLastCharater_intersectingRangeRemoved() {
        var textArea = new ExtendedTextArea("ansi b ansi b");
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        textArea.deleteText(3, 4);
        var modifiedText = "ans b ansi b";
        assertThat(textArea.getText()).isEqualTo(modifiedText);
        assertThat(viewModel.getMatchRanges()).hasSize(1);
        var range = viewModel.getMatchRanges().get(0);
        assertThat(range.getIndex()).isEqualTo(0);
        assertThat(Texts.rangesToSubStrings(viewModel.getMatchRanges(), modifiedText)).containsExactlyElementsOf(
                List.of("ansi")
        );
    }

    @Test
    public void doOnTextChange_deletionFirstCharater_intersectingRangeRemoved() {
        var textArea = new ExtendedTextArea("ansi b ansi b");
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        textArea.deleteText(7, 8);
        var modifiedText = "ansi b nsi b";
        assertThat(textArea.getText()).isEqualTo(modifiedText);
        assertThat(viewModel.getMatchRanges()).hasSize(1);
        var range = viewModel.getMatchRanges().get(0);
        assertThat(range.getIndex()).isEqualTo(0);
        assertThat(Texts.rangesToSubStrings(viewModel.getMatchRanges(), modifiedText)).containsExactlyElementsOf(
                List.of("ansi")
        );
    }

    @Test
    public void doOnTextChange_insertInMiddle_intersectingRangeRemoved() {
        var textArea = new ExtendedTextArea("ansi ansi");
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        textArea.insertText(2, " ");
        var modifiedText = "an si ansi";
        assertThat(textArea.getText()).isEqualTo(modifiedText);
        assertThat(viewModel.getMatchRanges()).hasSize(1);
        var range = viewModel.getMatchRanges().get(0);
        assertThat(range.getIndex()).isEqualTo(0);
        assertThat(Texts.rangesToSubStrings(viewModel.getMatchRanges(), modifiedText)).containsExactlyElementsOf(
                List.of("ansi")
        );
    }

    @Test
    public void doOnTextChange_insertBefore_rangeUpdated() {
        var textArea = new ExtendedTextArea("ansi ansi");
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        textArea.insertText(0, " ");
        var modifiedText = " ansi ansi";
        assertThat(textArea.getText()).isEqualTo(modifiedText);
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        var range0 = viewModel.getMatchRanges().get(0);
        assertThat(range0.getIndex()).isEqualTo(0);
        var range1 = viewModel.getMatchRanges().get(1);
        assertThat(range1.getIndex()).isEqualTo(1);
        assertThat(Texts.rangesToSubStrings(viewModel.getMatchRanges(), modifiedText)).containsExactlyElementsOf(
                List.of("ansi", "ansi")
        );
    }

    @Test
    public void doOnTextChange_insertAfter_rangeUpdated() {
        var textArea = new ExtendedTextArea("ansi ansi");
        var view = this.createView(textArea);
        var viewModel = view.getViewModel();
        view.getFindComboBox().getEditor().setText("ansi");
        view.getViewModel().findNext();
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        textArea.insertText(4, " ");
        var modifiedText = "ansi  ansi";
        assertThat(textArea.getText()).isEqualTo(modifiedText);
        assertThat(viewModel.getMatchRanges()).hasSize(2);
        var range0 = viewModel.getMatchRanges().get(0);
        assertThat(range0.getIndex()).isEqualTo(0);
        var range1 = viewModel.getMatchRanges().get(1);
        assertThat(range1.getIndex()).isEqualTo(1);
        assertThat(Texts.rangesToSubStrings(viewModel.getMatchRanges(), modifiedText)).containsExactlyElementsOf(
                List.of("ansi", "ansi")
        );
    }

    private DefaultFindPaneView createView(ExtendedTextArea codeArea) {
        var viewModel = new TestFindPaneViewModel(FindMatchesResetPolicy.AUTOMATIC);
        var view = new DefaultFindPaneView(codeArea, viewModel);
        view.initialize();
        viewModel.replaceModeProperty().set(true);
        return view;
    }
}
