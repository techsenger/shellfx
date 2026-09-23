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

package com.techsenger.shellfx.shared.find;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindView<VM extends AbstractFindViewModel<?, ?>> extends AbstractAreaView<VM> {

    private final ComboBox<String> findComboBox = new ComboBox<>();

    private final Button clearButton = new Button();

    private final Label matchesLabel = new Label();

    private final HBox findRightBox = new HBox();

    private final StackPane findComboBoxWrapper = new StackPane(findComboBox, findRightBox);

    private final ToggleButton matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));

    private final BooleanProperty notFound = new SimpleBooleanProperty();

    public AbstractFindView(VM viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(this.findComboBox.getEditor(), () -> {
            applyFindComboBoxFocused();
        });
    }

    /**
     * Provides minimal visual configuration to avoid imposing styling on subclasses.
     */
    @Override
    protected void build() {
        super.build();
        getNode().getStylesheets().add(AbstractFindView.class.getResource("find-base.css").toExternalForm());
        this.findComboBox.setItems(getViewModel().getFindTexts());
        this.findComboBox.setEditable(true);
        this.findComboBox.getStyleClass().addAll(StyleClasses.NO_SELECTED);
        this.matchesLabel.getStyleClass().add("matches");
        this.findRightBox.getStyleClass().add("find-right-box");
        StackPane.setMargin(this.findRightBox, new Insets(0, Spacing.getHorizontal() * 2, 0, 0));
        this.findComboBoxWrapper.setAlignment(Pos.CENTER_RIGHT);
        this.findRightBox.setMaxWidth(HBox.USE_PREF_SIZE);
        this.findRightBox.setAlignment(Pos.CENTER);
        this.findComboBoxWrapper.setPadding(new Insets(Spacing.getVerticalHalf(), 0,
                Spacing.getVerticalHalf(), 0));
        clearButton.getStyleClass().addAll(StyleClasses.CROSS_BUTTON, StyleClasses.SIZE_XXS, StyleClasses.SQUARE);
        clearButton.setFocusTraversable(false);

        this.matchCaseButton.setTooltip(new Tooltip("Match Case"));
        this.matchCaseButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.matchCaseButton.setFocusTraversable(false);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        notFound.bind(viewModel.notFoundProperty());
        matchesLabel.textProperty().bind(viewModel.matchesTextProperty());
        matchCaseButton.selectedProperty().bindBidirectional(viewModel.matchCaseSelectedProperty());
        matchCaseButton.disableProperty().bind(viewModel.matchCaseDisabledProperty());
        findComboBox.getEditor().textProperty().bindBidirectional(viewModel.editedFindTextProperty());
        viewModel.findTextWrapper().bind(findComboBox.getSelectionModel().selectedItemProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        ValueUtils.callAndAddListener(notFound, (ov, oldV, newV) -> {
            if (Boolean.TRUE.equals(newV)) {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, true);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            } else {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, false);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }

        });
        ValueUtils.callAndAddListener(viewModel.matchesVisibleProperty(),
                (ov, oldV, newV) -> updateMatchesVisible(newV));
        ValueUtils.callAndAddListener(viewModel.clearVisibleProperty(), (ov, oldV, newV) -> updateClearVisible(newV));
        viewModel.findTextSource().addListener((value) -> updateFindText(value));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        // Blocks the ComboBox's own default behavior of committing the editor's raw typed text into
        // value/selectedItem on Enter (ComboBoxPopupControl#handleKeyEvent, triggered on KEY_RELEASED, not
        // KEY_PRESSED). That default commit would fire for arbitrary typed text too, not just real history
        // entries, but findText is bound to selectedItem and expected to change only on a genuine pick from the
        // list (see bind()). Must be added here, before the ComboBox is shown and its skin installs its own
        // internal filter on the same node, so this filter runs first and can consume the event before the
        // control's own commit logic sees it.
        findComboBox.addEventFilter(KeyEvent.KEY_RELEASED, (e) -> {
            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
            }
        });

        // Enter key: immediate invocation (useful for manual search and also useful to allow
        // immediate search in incremental mode)
        findComboBox.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                viewModel.onFindSubmitted();
            }
        });

        clearButton.setOnAction(e -> viewModel.onClearFindText());
        matchCaseButton.setOnAction(e -> viewModel.onMatchCase());
    }

    protected ComboBox<String> getFindComboBox() {
        return findComboBox;
    }

    protected Label getMatchesLabel() {
        return matchesLabel;
    }

    protected Button getClearButton() {
        return clearButton;
    }

    protected HBox getFindRightBox() {
        return findRightBox;
    }

    protected StackPane getFindComboBoxWrapper() {
        return findComboBoxWrapper;
    }

    protected ToggleButton getMatchCaseButton() {
        return matchCaseButton;
    }

    void applyFindComboBoxFocused() {
        var text = this.findComboBox.getEditor().getText();
        if (text != null && !text.isEmpty()) {
            var pos = (int) text.codePointCount(0, text.length());
            this.findComboBox.getEditor().positionCaret(pos);
        }
    }

    private void updateFindText(String value) {
        if (value == null) {
            // SingleSelectionModel#select(T) is a no-op for null, it does not clear the current selection
            findComboBox.getSelectionModel().clearSelection();
        } else {
            findComboBox.getSelectionModel().select(value);
        }
    }

    private void updateMatchesVisible(boolean visible) {
        var visibleNow = this.matchesLabel.getParent() != null;
        if (visible && !visibleNow) {
            this.findRightBox.getChildren().add(this.matchesLabel);
        }
        if (!visible && visibleNow)  {
            this.findRightBox.getChildren().remove(this.matchesLabel);
        }
    }

    private void updateClearVisible(boolean visible) {
        var visibleNow = this.clearButton.getParent() != null;
        if (visible && !visibleNow) {
            this.findRightBox.getChildren().add(0, this.clearButton);
        }
        if (!visible && visibleNow)  {
            this.findRightBox.getChildren().remove(this.clearButton);
        }
    }
}
