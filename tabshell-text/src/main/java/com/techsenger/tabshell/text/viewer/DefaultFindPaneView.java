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

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.find.AbstractFindPaneView;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.material.textarea.TextAreaStyle;
import com.techsenger.tabshell.material.textarea.TextAreaStyleKeys;
import com.techsenger.toolkit.core.collection.ListUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.model.RichTextChange;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class DefaultFindPaneView extends AbstractFindPaneView<DefaultFindPaneViewModel> implements FindPaneView {

    private static final Logger logger = LoggerFactory.getLogger(DefaultFindPaneView.class);

    private final Label replaceLabel = new Label("Replace With");

    private final HBox replaceLabelWrapper = new HBox(replaceLabel);

    private final ComboBox<String> replaceComboBox = new ComboBox<>();

    private final HBox replaceComboBoxWrapper = new HBox(replaceComboBox);

    private final Button replaceButton = new Button("Replace");

    private final Button replaceAllButton = new Button("Replace All");

    private final ToolBar replaceToolBar = new ToolBar();

    private final ExtendedTextArea textArea;

    private Subscription changeObserver;

    DefaultFindPaneView(ExtendedTextArea textArea, DefaultFindPaneViewModel viewModel) {
        super(viewModel);
        this.textArea = textArea;
    }

    @Override
    public DefaultFindPaneViewModel getViewModel() {
        return (DefaultFindPaneViewModel) super.getViewModel();
    }

    @Override
    public void setSelectionToFindText() {
        var selectedText = this.textArea.getSelectedText();
        if (!selectedText.isEmpty()) {
            getFindComboBox().getEditor().setText(selectedText);
        }
    }

    /**
     * User can use caret to change its location. Besides of this there can be situation when foundRangeProperty
     * doesn't changes (because range is the same) but it is necessary to display current range.
     */
    @Override
    public void selectNextRange(boolean focusToTextArea) {
        var range = this.getViewModel().getMatchRange();
        if (range != null) {
            this.textArea.moveTo(range.getEnd());
            this.textArea.requestFollowCaret();
            this.textArea.selectRange(range.getStart(), range.getEnd());
        }
        if (focusToTextArea) {
            this.textArea.requestFocus();
        }
    }

    /**
     * User can use caret to change its location. Besides of this there can be situation when foundRangeProperty
     * doesn't changes (because range is the same) but it is necessary to display current range.
     */
    @Override
    public void selectPreviousRange(boolean focusToTextArea) {
        var range = this.getViewModel().getMatchRange();
        if (range != null) {
            this.textArea.moveTo(range.getStart());
            this.textArea.requestFollowCaret();
            this.textArea.selectRange(range.getEnd(), range.getStart());
        }
        if (focusToTextArea) {
            this.textArea.requestFocus();
        }
    }

    @Override
    protected void build(DefaultFindPaneViewModel viewModel) {
        super.build(viewModel);
        this.replaceLabel.setMinWidth(Label.USE_PREF_SIZE);
        this.replaceLabelWrapper.getStyleClass().add(TOOLBAR_LIKE_STYLE_CLASS);
        // 3 = 2(padding) + 1(bg-insetts)
        this.replaceLabelWrapper.setPadding(new Insets(2, SizeConstants.INSET, 3, SizeConstants.INSET));
        this.replaceLabelWrapper.setAlignment(Pos.CENTER_RIGHT);
        this.replaceComboBox.setEditable(true);
        this.replaceComboBox.getStyleClass().addAll(Styles.DENSE, StyleClasses.NO_SELECTED);
        HBox.setHgrow(this.replaceComboBox, Priority.ALWAYS);
        this.replaceComboBox.setItems(viewModel.getReplaceTexts());
        this.replaceComboBoxWrapper.getStyleClass().add(TOOLBAR_LIKE_STYLE_CLASS);
        this.replaceComboBoxWrapper.setAlignment(Pos.CENTER_LEFT);
        this.replaceButton.setFocusTraversable(false);
        this.replaceAllButton.setFocusTraversable(false);
        this.replaceToolBar.setPadding(new Insets(0, 0, 0, SizeConstants.INSET));
        this.replaceToolBar.getItems().addAll(this.replaceButton, this.replaceAllButton);
        this.replaceToolBar.setMinWidth(ToolBar.USE_PREF_SIZE);
        this.replaceToolBar.getStyleClass().add(Styles.DENSE);
        if (viewModel.replaceModeProperty().get()) {
            this.manageReplaceControls(true);
        }
    }

    @Override
    protected void bind(DefaultFindPaneViewModel viewModel) {
        super.bind(viewModel);
        viewModel.textProperty().bind(this.textArea.textProperty());
        viewModel.textLengthProperty().bind(this.textArea.lengthProperty());
        viewModel.textAreaEditableProperty().bind(this.textArea.editableProperty());
        viewModel.caretPositionProperty().bind(this.textArea.caretPositionProperty());
        viewModel.selectionProperty().bind(this.textArea.selectionProperty());
        this.replaceComboBox.maxWidthProperty().bind(this.replaceComboBoxWrapper.widthProperty());
        viewModel.replaceTextProperty().bindBidirectional(this.replaceComboBox.getEditor().textProperty());
    }

    @Override
    protected void addListeners(DefaultFindPaneViewModel viewModel) {
        super.addListeners(viewModel);
        viewModel.replaceModeProperty().addListener((ov, oldV, newV) -> this.manageReplaceControls(newV));
        viewModel.matchesResetProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                this.textArea.deselect();
            }
        });
        viewModel.highlightActiveProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                addHighlighting();
            } else {
                removeAllHighlighting();
            }
        });
        //as we will do style changes, we don't neet changes on their styles update
        this.changeObserver = this.textArea.richChanges().observe((c) -> {
            doOnTextChange(c);
        });
    }

    @Override
    protected void addHandlers(DefaultFindPaneViewModel viewModel) {
        super.addHandlers(viewModel);
        //in text field Ctrl+H is "remove backward". However, we this combination as a shortcut. This filter fixes it.
        getFindComboBox().getEditor().addEventFilter(KeyEvent.KEY_PRESSED, (t) -> {
            if (t.getCode() == KeyCode.H && t.isControlDown()) {
                viewModel.replaceModeProperty().set(true);
                t.consume();
            }
        });
        getFindComboBox().setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (viewModel.matchesResetProperty().get()) {
                    viewModel.find();
                }
                if (viewModel.moveToNextRange()) {
                    this.selectNextRange(false);
                } else if (viewModel.moveToPreviousRange()) {
                    this.selectPreviousRange(false);
                }
            }
        });
        getFindPreviousButton().setOnAction((event) -> {
            viewModel.findPrevious();
            this.selectPreviousRange(true);
        });
        getFindNextButton().setOnAction((event) -> {
            viewModel.findNext();
            this.selectNextRange(true);
        });

        this.replaceComboBox.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                replace();
            }
        });
        this.replaceButton.setOnAction(e -> {
            replace();
            this.textArea.requestFocus();
        });
        this.replaceAllButton.setOnAction(e -> replaceAll());
    }

    protected ComboBox<String> getFindComboBox() {
        return super.getFindComboBox();
    }

    protected ToggleButton getHighlightButton() {
        return super.getHighlightButton();
    }

    @Override
    protected void preDeinitialize(DefaultFindPaneViewModel viewModel) {
        super.preDeinitialize(viewModel);
        if (viewModel.isHighlightActive()) {
            this.removeAllHighlighting();
        }
    }

    @Override
    protected void unbuild(DefaultFindPaneViewModel viewModel) {
        super.unbuild(viewModel);
    }

    @Override
    protected void unbind(DefaultFindPaneViewModel viewModel) {
        super.unbind(viewModel);
        viewModel.textProperty().unbind();
        viewModel.textLengthProperty().unbind();
        viewModel.textAreaEditableProperty().unbind();
        viewModel.caretPositionProperty().unbind();
        viewModel.selectionProperty().unbind();
    }

    @Override
    protected void removeListeners(DefaultFindPaneViewModel viewModel) {
        super.removeListeners(viewModel);
        this.changeObserver.unsubscribe();
    }

    ComboBox<String> getReplaceComboBox() {
        return this.replaceComboBox;
    }

    /**
     * Replace always works in two steps: 1) text for replace is selected 2) on replace button text is replaced.
     */
    void replace() {
        var viewModel = getViewModel();
        if (viewModel.matchesResetProperty().get()) {
            viewModel.find();
            viewModel.moveToNextRange();
            this.selectNextRange(true);
            return;
        }
        //checking if selection is current range
        var selectedRange = this.textArea.getSelection();
        var range = viewModel.getMatchRange();
        if (range == null
                || selectedRange.getStart() != range.getStart() || selectedRange.getEnd() != range.getEnd()
                || range.isReplaced()) {
            viewModel.moveToNextRange();
            this.selectNextRange(true);
            return;
        }
        var replaceText = this.replaceComboBox.getEditor().getText();
        viewModel.setReplacingIsDone(true);
        this.textArea.replace(range.getStart(), range.getEnd(), replaceText, TextAreaStyle.EMPTY);
        viewModel.setReplacingIsDone(false);
        range.setReplaced(true);
        viewModel.updateNextRangesOnReplace();
        viewModel.moveToNextRange();
        this.selectNextRange(true);
    }

    void replaceAll() {
        var viewModel = getViewModel();
        if (viewModel.matchesResetProperty().get()) {
            viewModel.find();
            viewModel.moveToNextRange();
            this.selectNextRange(false);
        }
        var ranges = viewModel.getMatchRanges();
        if (ranges.isEmpty()) {
            return;
        }
        var replaceText = this.replaceComboBox.getEditor().getText();
        var changes = this.textArea.createMultiChange();
        MatchRange lastRange = null;
        for (var range : ranges) {
            if (!range.isReplaced()) {
                //important!!! when we do multi changes we use positions in the document with CURRENT STATE
                //so, we don't need to calculate new positions according to replace text length for every next
                //range
                changes.replaceText(range.getStart(), range.getEnd(), replaceText);
                range.setReplaced(true);
                lastRange = range;
            }
        }
        if (changes.hasChanges()) {
            //we need to get the end position of the last replaced range. This position we can take only
            //after all replaces from event stream
            List<Integer> positions = new ArrayList<>();
            viewModel.setReplacingIsDone(true);
            var subscribtion = this.textArea.richChanges().observe((t) -> {
                positions.add(t.getInsertionEnd());
            });
            changes.commit();
            viewModel.setReplacingIsDone(false);
            subscribtion.unsubscribe();
            viewModel.moveToRange(lastRange);
            this.textArea.moveTo(positions.get(positions.size() - 1));
            this.textArea.requestFollowCaret();
            this.textArea.requestFocus();
        }
    }

    private void manageReplaceControls(boolean visible) {
        var gridPane = getGridPane();
        if (visible) {
            gridPane.add(this.replaceLabelWrapper, 0, 1);
            gridPane.add(this.replaceComboBoxWrapper, 1, 1);
            gridPane.add(this.replaceToolBar, 2, 1);
        } else {
            gridPane.getChildren().removeAll(this.replaceLabelWrapper, this.replaceComboBoxWrapper,
                    this.replaceToolBar);
        }
        if (getFocusTrap().isActivated()) {
            getFocusTrap().update();
        } else {
            getFocusTrap().activate();
        }
    }

    private void doOnTextChange(RichTextChange<Collection<String>, String, Collection<TextAreaStyle>> c) {
        if (!this.textArea.isStyleOnlyChange(c)) {
            var viewModel = this.getViewModel();
            if (viewModel.isHighlightActive()) {
                this.removeHighlighting(c);
            }
            //model is updated only after removing highlight
            viewModel.updateMatchesOnTextChange(c);
        }
    }

    /**
     * In richtextfx we can make text highlighted using textArea.setStyleSpans(..) or textArea.setStyleClass(...).
     * We will use spans because setStyleClass will erase & replace the current styling of the specified range,
     * while setStyleSpans will add the provided styling to any existing styling. This can be important when
     * we will use highlighting for spell checking and found matches.
     *
     * <p>It is important to understand how StyleSpan works. There is always only ONE layer of StyleSpans, but every
     * StyleSpan can have multiple styles. For every text there is always at least one StyleSpan. For example,
     * if text doesn't have any styles then there will be one empty StyleSpan from 0 and length = text.length().
     * When we add ONE StyleSpan somewhere in the middle of the text, then there will be three (or more) StyleSpans -
     * empty, added, empty. When it is necessary to modify span then it is required to create new StyleSpan(s) and
     * set it to text area. By other words, StyleSpans are immutable.
     */
    private void addHighlighting() {
        var viewModel = getViewModel();
        var ranges = viewModel.getMatchRanges();
        if (ranges.isEmpty()) {
            return;
        }
        for (var range : ranges) {
            if (range.isReplaced()) {
                continue;
            }
            StyleSpansBuilder<Collection<TextAreaStyle>> ssb = new StyleSpansBuilder<>();
            for (var existingSpan : this.textArea.getStyleSpans(range.getStart(), range.getEnd())) {
                var length = range.getEnd() - range.getStart();
                var highlightStyle = new TextAreaStyle(TextAreaStyleKeys.FIND_HIGHLIGHT, length, FOUND_STYLE_CLASS);
                if (existingSpan.getStyle().isEmpty()) {
                    ssb.add(ListUtils.newArrayList(highlightStyle), existingSpan.getLength());
                } else {
                    var styles = new ArrayList<>(existingSpan.getStyle());
                    styles.add(highlightStyle);
                    ssb.add(styles, existingSpan.getLength());
                }
            }
            //update only pieces
            this.textArea.getSuspendUndo()
                    .suspendWhile(() -> this.textArea.setStyleSpans(range.getStart(), ssb.create()));
        }
    }

    /**
     * As user can modify text we can't keep references to StyleSpans as RichTextFX will divide spans into pieces
     * and create new StyleSpans, so all references will be broken.
     */
    private void removeAllHighlighting() {
        //we search whole text, but update only peaces as it is much faster
        var position = 0;
        for (var existingSpan : this.textArea.getStyleSpans(0, this.textArea.getLength())) {
            var oldStyles = existingSpan.getStyle();
            if (!oldStyles.isEmpty()) {
                var newStyles = new ArrayList<TextAreaStyle>();
                for (var style : oldStyles) {
                    if (style.getKey() != TextAreaStyleKeys.FIND_HIGHLIGHT) {
                        newStyles.add(style);
                    }
                }
                if (oldStyles.size() != newStyles.size()) {
                    final StyleSpansBuilder<Collection<TextAreaStyle>> ssb = new StyleSpansBuilder<>();
                    ssb.add(newStyles, existingSpan.getLength());
                    var finalPos = position;
                    this.textArea.getSuspendUndo()
                        .suspendWhile(() -> this.textArea.setStyleSpans(finalPos, ssb.create()));
                }
            }
            position += existingSpan.getLength();
        }
    }

    /**
     * This method removes highlighting for pieces that were modified. The main idea is to understand that there
     * are only two types of changes - text insertion and text removal. For example, there is no text replacement.
     * When some text is selected and after that new text is pasted, then firstly old text is removed and after that
     * new text is inserted. Attention! This rule is applied even when inserted text partially is equal to the old one.
     *
     * <p>Note. When new text is inserted inside styled spand then removed() contains information about style even if
     * the removalEnd is equal to position.
     *
     * @param change
     */
    private void removeHighlighting(RichTextChange<Collection<String>, String, Collection<TextAreaStyle>> change) {
        var removed = change.getRemoved();
        var nonEmptySpansPresent = false;
        for (var span : removed.getStyleSpans(0, removed.length())) {
            if (!span.getStyle().isEmpty()) {
                nonEmptySpansPresent = true;
                break;
            }
        }
        if (nonEmptySpansPresent) {
            var checkStart = change.getPosition();
            var checkEnd = change.getInsertionEnd();
            //checking styles to the left
            if (checkStart > 0) {
                removeHighlightingStyleAt(checkStart - 1);
            }
            //checking style to the right
            if (checkEnd != this.textArea.getLength() - 1) {
                //by idea there must be no + 1, however, this is the way getAbsoluteStyleRangeAt works.
                removeHighlightingStyleAt(checkEnd + 1);
            }
        }
    }

    /**
     * Fixes styles when text is modified.
     *
     * @param position
     */
    private void removeHighlightingStyleAt(int position) {
        final IndexRange range = this.textArea.getAbsoluteStyleRangeAt(position);
        var spans = this.textArea.getStyleSpans(range);
        //there must be only one span
        var span = spans.iterator().next();
        var replaceRequired = false;
        List<TextAreaStyle> newStyles = new ArrayList<>();
        for (var style : span.getStyle()) {
            if (style.getKey() == TextAreaStyleKeys.FIND_HIGHLIGHT) {
                if (style.getLength() != span.getLength()) {
                    replaceRequired = true;
                } else {
                    break;
                }
            } else {
                newStyles.add(style);
            }
        }
        if (replaceRequired) {
            if (newStyles.isEmpty()) {
                newStyles = TextAreaStyle.EMPTY;
            }
            StyleSpansBuilder<Collection<TextAreaStyle>> ssb = new StyleSpansBuilder<>();
            ssb.add(newStyles, span.getLength());
            this.textArea.getSuspendUndo()
                    .suspendWhile(() -> this.textArea.setStyleSpans(range.getStart(), ssb.create()));
        }
    }
}
