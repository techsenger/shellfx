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
import javafx.animation.PauseTransition;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindBaseView<VM extends AbstractFindBaseViewModel<?>>
        extends AbstractAreaView<VM> {

    private final FindTrigger findTrigger;

    private final ComboBox<String> findComboBox = new ComboBox<>();

    private final Button clearButton = new Button();

    private final Label matchesLabel = new Label();

    private final HBox findRightBox = new HBox();

    private final StackPane findComboBoxWrapper = new StackPane(findComboBox, findRightBox);

    private final ToggleButton matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));

    private final Button findPreviousButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));

    private final Button findNextButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));

    private final BooleanProperty notFound = new SimpleBooleanProperty();

    /*
     * Debounce duration in milliseconds.
     */
    private int debounceMillis = 300;

    /**
     * Minimum characters to trigger incremental search (default 3).
     */
    private int minSearchLength = 3;

    /**
     * PauseTransition used to implement debounce on the JavaFX thread.
     */
    private PauseTransition debouncePause;

    public AbstractFindBaseView(VM viewModel, FindTrigger findTrigger) {
        super(viewModel);
        this.findTrigger = findTrigger;
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(this.findComboBox.getEditor(), () -> {
            onFindComboBoxFocused();
        });
    }

    public FindTrigger getFindTrigger() {
        return findTrigger;
    }

    /**
     * Provides minimal visual configuration to avoid imposing styling on subclasses.
     */
    @Override
    protected void build() {
        super.build();
        getNode().getStylesheets().add(AbstractFindBaseView.class.getResource("find-base.css").toExternalForm());
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

        this.findNextButton.setTooltip(new Tooltip("Next"));
        this.findNextButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.findNextButton.setFocusTraversable(false);

        this.findPreviousButton.setTooltip(new Tooltip("Previous"));
        this.findPreviousButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.findPreviousButton.setFocusTraversable(false);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        notFound.bind(viewModel.notFoundProperty());
        matchesLabel.textProperty().bind(viewModel.matchesTextProperty());
        matchCaseButton.selectedProperty().bindBidirectional(viewModel.matchCaseSelectedProperty());
        matchCaseButton.disableProperty().bind(viewModel.matchCaseDisabledProperty());
        findNextButton.disableProperty().bind(viewModel.findNextDisabledProperty());
        findPreviousButton.disableProperty().bind(viewModel.findPreviousDisabledProperty());
        findComboBox.getEditor().textProperty().bindBidirectional(viewModel.findTextProperty());
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

        findComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> {
            if (this.findTrigger == FindTrigger.ON_TYPE) {
                debouncePause.stop();
                if (newV != null && newV.length() >= minSearchLength) {
                    // restart debounce timer
                    debouncePause.playFromStart();
                }
            }
            if (newV == null || newV.isEmpty()) {
                viewModel.onFindCleared();
            }
        });
        // Combobox value change: keep existing behavior (invokes handler on selection)
        findComboBox.valueProperty().addListener((ov, oldV, newV) -> {
            invokeFindHandler();
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        if (findTrigger == FindTrigger.ON_TYPE) {
            debouncePause = new PauseTransition(Duration.millis(debounceMillis));
            debouncePause.setOnFinished(e -> {
                invokeFindHandler();
            });
        }

        // Enter key: immediate invocation (useful for manual search and also useful to allow
        // immediate search in incremental mode)
        findComboBox.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                invokeFindHandler();
            }
        });

        clearButton.setOnAction(e -> viewModel.onClearFindText());
        findPreviousButton.setOnAction(e -> viewModel.onFindPrevious());
        findNextButton.setOnAction(e -> viewModel.onFindNext());
        matchCaseButton.setOnAction(e -> viewModel.onMatchCase());
    }

    /**
     * Returns debounce time in milliseconds.
     */
    protected int getDebounceMillis() {
        return debounceMillis;
    }

    /**
     * Sets debounce time in milliseconds. Updates the internal debounce timer immediately.
     */
    protected void setDebounceMillis(int debounceMillis) {
        if (debounceMillis < 0) {
            throw new IllegalArgumentException("debounceMillis must be >= 0");
        }
        this.debounceMillis = debounceMillis;
        if (debouncePause != null) {
            debouncePause.setDuration(Duration.millis(debounceMillis));
        }
    }

    /**
     * Returns minimum number of characters required for {@link SearchTrigger#ON_TYPE} trigger.
     */
    protected int getMinSearchLength() {
        return minSearchLength;
    }

    /**
     * Sets minimum number of characters required for {@link SearchTrigger#ON_TYPE} trigger.
     */
    protected void setMinSearchLength(int minSearchLength) {
        if (minSearchLength < 1) {
            throw new IllegalArgumentException("minSearchLength must be >= 1");
        }
        this.minSearchLength = minSearchLength;
    }

    protected void onFindComboBoxFocused() {
        var text = this.findComboBox.getEditor().getText();
        if (text != null && !text.isEmpty()) {
            var pos = (int) text.codePointCount(0, text.length());
            this.findComboBox.getEditor().positionCaret(pos);
        }
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

    protected Button getFindPreviousButton() {
        return findPreviousButton;
    }

    protected Button getFindNextButton() {
        return findNextButton;
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

    private void invokeFindHandler() {
        var text = this.findComboBox.getEditor().getText();
        if (text != null && !text.isEmpty()) {
            if (this.findTrigger == FindTrigger.ON_TYPE && text.length() < minSearchLength) {
                return;
            }
            getViewModel().onFind();
        }
    }
}
