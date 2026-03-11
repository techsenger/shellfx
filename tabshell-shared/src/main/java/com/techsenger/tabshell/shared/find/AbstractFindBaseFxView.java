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

package com.techsenger.tabshell.shared.find;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.List;
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
public abstract class AbstractFindBaseFxView<P extends AbstractFindBasePresenter<?, ?>>
        extends AbstractAreaFxView<P> implements FindBaseView {

    private final FindTrigger findTrigger;

    private final ComboBox<String> findComboBox = new ComboBox<>();

    private final Button clearButton = new Button();

    private final Label matchesLabel = new Label();

    private final HBox findRightBox = new HBox();

    private final StackPane findComboBoxWrapper = new StackPane(findComboBox, findRightBox);

    private ToggleButton matchCaseButton;

    private Button findPreviousButton;

    private Button findNextButton;

    private ToggleButton wholeWordButton;

    private ToggleButton regExpButton;

    private ToggleButton highlightButton;

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

    public AbstractFindBaseFxView(FindTrigger findTrigger) {
        this.findTrigger = findTrigger;
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(this.findComboBox.getEditor(), () -> {
            onFindComboBoxFocused();
        });
    }

    @Override
    public void setFindText(String text) {
        this.findComboBox.getEditor().textProperty().set(text);
    }

    @Override
    public void setupMatchCase() {
        this.matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));
        this.matchCaseButton.setTooltip(new Tooltip("Match Case"));
        this.matchCaseButton.setSelected(false);
        this.matchCaseButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.matchCaseButton.setFocusTraversable(false);
        this.matchCaseButton.setOnAction((event) -> getPresenter().onMatchCase(this.matchCaseButton.isSelected()));
    }

    @Override
    public void setMatchCaseSelected(boolean value) {
        if (this.matchCaseButton != null) {
            this.matchCaseButton.setSelected(value);
        }
    }

    @Override
    public void setMatchCaseDisabled(boolean value) {
        if (this.matchCaseButton != null) {
            this.matchCaseButton.setDisable(value);
        }
    }

    @Override
    public void setMatchesText(String text) {
        this.matchesLabel.textProperty().set(text);
    }

    @Override
    public void setMatchesVisible(boolean visible) {
        var visibleNow = this.matchesLabel.getParent() != null;
        if (visible && !visibleNow) {
            this.findRightBox.getChildren().add(this.matchesLabel);
        }
        if (!visible && visibleNow)  {
            this.findRightBox.getChildren().remove(this.matchesLabel);
        }
    }

    @Override
    public void setClearVisible(boolean visible) {
        var visibleNow = this.clearButton.getParent() != null;
        if (visible && !visibleNow) {
            this.findRightBox.getChildren().add(0, this.clearButton);
        }
        if (!visible && visibleNow)  {
            this.findRightBox.getChildren().remove(this.clearButton);
        }
    }

    @Override
    public void setFindTexts(List<String> texts) {
        this.findComboBox.getItems().clear();
        this.findComboBox.getItems().addAll(texts);
    }

    @Override
    public void setNotFound(boolean value) {
        this.notFound.set(value);
    }

    @Override
    public void setupFindNext() {
        this.findNextButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_DOWN));
        this.findNextButton.setTooltip(new Tooltip("Next"));
        this.findNextButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.findNextButton.setFocusTraversable(false);
        findNextButton.setOnAction(e -> getPresenter().onFindNext());
    }

    @Override
    public void setFindNextDisabled(boolean value) {
        if (this.findNextButton != null) {
            this.findNextButton.setDisable(value);
        }
    }

    @Override
    public void setupFindPrevious() {
        this.findPreviousButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_UP));
        this.findPreviousButton.setTooltip(new Tooltip("Previous"));
        this.findPreviousButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.findPreviousButton.setFocusTraversable(false);
        findPreviousButton.setOnAction(e -> getPresenter().onFindPrevious());
    }

    @Override
    public void setFindPreviousDisabled(boolean value) {
        if (this.findPreviousButton != null) {
            this.findPreviousButton.setDisable(value);
        }
    }

    @Override
    public void setupWholeWord() {
        this.wholeWordButton = new ToggleButton(null, new FontIconView(SharedIcons.WHOLE_WORD));
        this.wholeWordButton.setTooltip(new Tooltip("Whole Word"));
        this.wholeWordButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.wholeWordButton.setFocusTraversable(false);
        this.wholeWordButton.setOnAction((event) -> getPresenter().onWholeWord(this.wholeWordButton.isSelected()));
    }

    @Override
    public void setWholeWordSelected(boolean value) {
        if (this.wholeWordButton != null) {
            this.wholeWordButton.setSelected(value);
        }
    }

    @Override
    public void setWholeWordDisabled(boolean value) {
        if (this.wholeWordButton != null) {
            this.wholeWordButton.setDisable(value);
        }
    }

    @Override
    public void setupRegExp() {
        this.regExpButton = new ToggleButton(null, new FontIconView(SharedIcons.REG_EXP));
        this.regExpButton.setTooltip(new Tooltip("Regular Expression"));
        this.regExpButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.regExpButton.setFocusTraversable(false);
        this.regExpButton.setOnAction((event) -> getPresenter().onRegExp(this.regExpButton.isSelected()));
    }

    @Override
    public void setRegExpSelected(boolean value) {
        if (this.regExpButton != null) {
            this.regExpButton.setSelected(value);
        }
    }

    @Override
    public void setRegExpDisabled(boolean value) {
        if (this.regExpButton != null) {
            this.regExpButton.setDisable(value);
        }
    }

    @Override
    public void setupHighlight() {
        this.highlightButton = new ToggleButton(null, new FontIconView(SharedIcons.HIGHLIGHT));
        this.highlightButton.setTooltip(new Tooltip("Highlight All"));
        this.highlightButton.getStyleClass().addAll(StyleClasses.ICON_BUTTON, Styles.FLAT);
        this.highlightButton.setFocusTraversable(false);
        this.highlightButton.setOnAction(e -> getPresenter().onHighlight(this.highlightButton.isSelected()));
    }

    @Override
    public void setHighlightSelected(boolean value) {
        if (this.highlightButton != null) {
            this.highlightButton.setSelected(value);
        }
    }

    @Override
    public void setHighlightDisabled(boolean value) {
        if (this.highlightButton != null) {
            this.highlightButton.setDisable(value);
        }
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
        getNode().getStylesheets().add(AbstractFindBaseFxView.class.getResource("find-base.css").toExternalForm());
        this.findComboBox.setEditable(true);
        this.findComboBox.getStyleClass().addAll(StyleClasses.NO_SELECTED);
        this.matchesLabel.getStyleClass().add("matches");
        this.findRightBox.getStyleClass().add("find-right-box");
        StackPane.setMargin(this.findRightBox, new Insets(0, Spacing.HORIZONTAL * 2, 0, 0));
        this.findComboBoxWrapper.setAlignment(Pos.CENTER_RIGHT);
        this.findRightBox.setMaxWidth(HBox.USE_PREF_SIZE);
        this.findRightBox.setAlignment(Pos.CENTER);
        this.findComboBoxWrapper.setPadding(new Insets(Spacing.VERTICAL_HALF, 0,
                Spacing.VERTICAL_HALF, 0));
        clearButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);
        clearButton.setFocusTraversable(false);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        notFound.addListener((ov, oldV, newV) -> {
            if (Boolean.TRUE.equals(newV)) {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, true);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            } else {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, false);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }

        });
        this.findComboBox.getEditor().textProperty()
                .addListener((ov, t, t1) -> getPresenter().onFindTextChanged(findComboBox.getEditor().getText()));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        if (findTrigger == FindTrigger.ON_TYPE) {
            debouncePause = new PauseTransition(Duration.millis(debounceMillis));
            debouncePause.setOnFinished(e -> {
                invokeFindHandler();
            });
        }

        findComboBox.getEditor().textProperty().addListener((ov, oldV, newV) -> {
            if (this.findTrigger == FindTrigger.ON_TYPE) {
                debouncePause.stop();
                if (newV != null && newV.length() >= minSearchLength) {
                    // restart debounce timer
                    debouncePause.playFromStart();
                }
            }
            if (newV == null || newV.isEmpty()) {
                getPresenter().onFindCleared();
            }
        });

        // Enter key: immediate invocation (useful for manual search and also useful to allow
        // immediate search in incremental mode)
        findComboBox.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                invokeFindHandler();
            }
        });

        // Combobox value change: keep existing behavior (invokes handler on selection)
        findComboBox.valueProperty().addListener((ov, oldV, newV) -> {
            invokeFindHandler();
        });

        clearButton.setOnAction(e -> getPresenter().onClearFindText());
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

    protected ToggleButton getHighlightButton() {
        return highlightButton;
    }

    protected ToggleButton getWholeWordButton() {
        return wholeWordButton;
    }

    protected ToggleButton getRegExpButton() {
        return regExpButton;
    }

    private void invokeFindHandler() {
        var text = this.findComboBox.getEditor().getText();
        if (text != null && !text.isEmpty()) {
            if (this.findTrigger == FindTrigger.ON_TYPE && text.length() < minSearchLength) {
                return;
            }
            getPresenter().onFind();
        }
    }
}
