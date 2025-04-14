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

package com.techsenger.tabshell.core.find;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPaneView<T extends AbstractFindPaneViewModel> extends AbstractPaneView<T> {

    protected static final String FOUND_STYLE_CLASS = "found";

    private static final String RESULT_LABEL_STYLE_CLASS = "result";

    private final GridPane gridPane = new GridPane();

    private final Label findLabel = new Label("Find");

    private final HBox findLabelWrapper = new HBox(findLabel);

    private final ComboBox<String> findComboBox = new ComboBox<>();

    private final Label resultLabel = new Label();

    private final StackPane findComboBoxWrapper = new StackPane(findComboBox, resultLabel);

    private final Button findPreviousButton = new Button(null, new FontIconView(CoreIcons.CHEVRON_UP));

    private final Button findNextButton = new Button(null, new FontIconView(CoreIcons.CHEVRON_DOWN));

    private final ToggleButton caseButton = new ToggleButton(null, new FontIconView(CoreIcons.MATCH_CASE));

    private final ToggleButton wholeWordButton = new ToggleButton(null, new FontIconView(CoreIcons.WHOLE_WORD));

    private final ToggleButton regExpButton = new ToggleButton(null, new FontIconView(CoreIcons.REG_EXP));

    private final ToggleButton highlightButton = new ToggleButton(null, new FontIconView(CoreIcons.HIGHLIGHT));

    private final Button closeButton = new Button();

    private final HBox toolBox = new HBox();

    private final FocusTrap focusTrap = new FocusTrap(gridPane);

    public AbstractFindPaneView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(this.findComboBox.getEditor(), () -> {
            var text = this.findComboBox.getEditor().getText();
            if (text != null && !text.isEmpty()) {
                var pos = (int) text.codePointCount(0, text.length());
                this.findComboBox.getEditor().positionCaret(pos);
            }
            if (!this.focusTrap.isActivated()) {
                this.focusTrap.activate();
            }
        });
    }

    @Override
    public Pane getNode() {
        return this.gridPane;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        var css = AbstractFindPaneView.class.getResource("find.css").toExternalForm();
        this.gridPane.getStylesheets().add(css);
        this.gridPane.getStyleClass().add("find");

        ColumnConstraints column0 = new ColumnConstraints();
        column0.setHgrow(Priority.NEVER);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.ALWAYS);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.NEVER);
        this.gridPane.getColumnConstraints().addAll(column0, column1, column2);

        this.findLabel.setMinWidth(Label.USE_PREF_SIZE);
        // 3 = 2(padding) + 1(bg-insetts)
        this.findLabelWrapper.setPadding(new Insets(2, SizeConstants.INSET, 3, SizeConstants.INSET));
        this.findLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        GridPane.setVgrow(this.findLabelWrapper, Priority.ALWAYS);
        this.findComboBox.setEditable(true);
        this.findComboBox.getStyleClass().addAll(Styles.DENSE, StyleClasses.NO_SELECTED);
        this.findComboBox.setItems(viewModel.getFindTexts());
        this.resultLabel.getStyleClass().add(RESULT_LABEL_STYLE_CLASS);
        StackPane.setMargin(this.resultLabel, new Insets(0, SizeConstants.INSET * 2, 0, 0));
        this.findComboBoxWrapper.setAlignment(Pos.CENTER_RIGHT);
        this.findComboBoxWrapper.setPadding(new Insets(SizeConstants.SIXTH_INSET, 0,
                SizeConstants.SIXTH_INSET, 0));
        GridPane.setVgrow(this.findComboBoxWrapper, Priority.ALWAYS);
        this.findPreviousButton.setTooltip(new Tooltip("Previous"));
        this.findPreviousButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.findPreviousButton.setFocusTraversable(false);
        this.findNextButton.setTooltip(new Tooltip("Next"));
        this.findNextButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.findNextButton.setFocusTraversable(false);

        this.caseButton.setTooltip(new Tooltip("Match Case"));
        this.caseButton.setSelected(false);
        this.caseButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.caseButton.setFocusTraversable(false);
        this.wholeWordButton.setTooltip(new Tooltip("Whole Word"));
        this.wholeWordButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.wholeWordButton.setFocusTraversable(false);
        this.regExpButton.setTooltip(new Tooltip("Regular Expression"));
        this.regExpButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.regExpButton.setFocusTraversable(false);
        this.highlightButton.setTooltip(new Tooltip("Highlight All"));
        this.highlightButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.highlightButton.setFocusTraversable(false);
        this.closeButton.getStyleClass().add(StyleClasses.CROSS_BUTTON);
        this.closeButton.setFocusTraversable(false);

        this.toolBox.getChildren().addAll(this.findPreviousButton, this.findNextButton, this.caseButton,
                this.wholeWordButton, this.regExpButton, this.highlightButton,
                new Spacer(SizeConstants.INSET - SizeConstants.THIRD_INSET * 2), this.closeButton);
        this.toolBox.getStyleClass().add(Styles.DENSE);
        this.toolBox.setSpacing(SizeConstants.THIRD_INSET);
        this.toolBox.setAlignment(Pos.CENTER_LEFT);
        this.toolBox.setPadding(new Insets(0, SizeConstants.INSET, 0, SizeConstants.THIRD_INSET));
        GridPane.setVgrow(this.toolBox, Priority.ALWAYS);

        gridPane.add(this.findLabelWrapper, 0, 0);
        gridPane.add(this.findComboBoxWrapper, 1, 0);
        gridPane.add(this.toolBox, 2, 0);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        this.findComboBox.maxWidthProperty().bind(this.findComboBoxWrapper.widthProperty());
        this.findComboBox.getEditor().textProperty().bindBidirectional(viewModel.findTextProperty());
        this.wholeWordButton.selectedProperty().bindBidirectional(viewModel.wholeWordSelectedProperty());
        this.wholeWordButton.disableProperty().bindBidirectional(viewModel.wholeWordDisableProperty());
        this.caseButton.selectedProperty().bindBidirectional(viewModel.caseSelectedProperty());
        this.regExpButton.selectedProperty().bindBidirectional(viewModel.regExpSelectedProperty());
        this.regExpButton.disableProperty().bindBidirectional(viewModel.regExpDisableProperty());
        this.highlightButton.selectedProperty().bindBidirectional(viewModel.highlightSelectedProperty());
        this.resultLabel.textProperty().bindBidirectional(viewModel.resultTextProperty());
        this.resultLabel.visibleProperty().bindBidirectional(viewModel.resultTextVisibleProperty());
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        this.caseButton.setOnAction((event) -> viewModel.resetMatches());
        this.wholeWordButton.setOnAction((event) -> viewModel.resetMatches());
        this.regExpButton.setOnAction((event) -> viewModel.resetMatches());
        this.closeButton.setOnAction(e -> viewModel.closeActionProperty().get().run());
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.notFoundProperty().addListener((ov, oldV, newV) -> {
            if (Boolean.TRUE.equals(newV)) {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, true);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            } else {
                this.findComboBox.getEditor().pseudoClassStateChanged(Styles.STATE_DANGER, false);
                this.findComboBox.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }

        });
        this.findComboBox.getEditor().textProperty().addListener((ov, t, t1) -> {
            if (!viewModel.historyUpdatedProperty().get()) {
                viewModel.resetMatches();
            }
        });
    }

    protected GridPane getGridPane() {
        return gridPane;
    }

    protected ComboBox<String> getFindComboBox() {
        return findComboBox;
    }

    protected FocusTrap getFocusTrap() {
        return focusTrap;
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
}
