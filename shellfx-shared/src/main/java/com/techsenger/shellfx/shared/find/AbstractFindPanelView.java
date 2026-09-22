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
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelView<VM extends AbstractFindPanelViewModel<?>>
        extends AbstractFindBaseView<VM> {

    protected static final String FOUND_STYLE_CLASS = "found";

    private final GridPane gridPane = new GridPane();

    private final Label findLabel = new Label("Find");

    private final HBox findLabelWrapper = new HBox(findLabel);

    private final ToggleButton wholeWordButton = new ToggleButton(null, new FontIconView(SharedIcons.WHOLE_WORD));

    private final ToggleButton regExpButton = new ToggleButton(null, new FontIconView(SharedIcons.REG_EXP));

    private final ToggleButton highlightButton = new ToggleButton(null, new FontIconView(SharedIcons.HIGHLIGHT));

    private final Button closeButton = new Button();

    private final HBox toolBox = new HBox();

    private final FocusTrap focusTrap = new FocusTrap(gridPane);

    public AbstractFindPanelView(VM viewModel, FindTrigger searchTrigger) {
        super(viewModel, searchTrigger);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(getFindComboBox().getEditor(), () -> {
            onFindComboBoxFocused();
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
    protected void build() {
        super.build();
        var css = AbstractFindPanelView.class.getResource("find-panel.css").toExternalForm();
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
        this.findLabelWrapper.setPadding(new Insets(2, Spacing.getHorizontal(), 3, Spacing.getHorizontal()));
        this.findLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        GridPane.setVgrow(this.findLabelWrapper, Priority.ALWAYS);
        GridPane.setVgrow(getFindComboBoxWrapper(), Priority.ALWAYS);

        this.wholeWordButton.setTooltip(new Tooltip("Whole Word"));
        this.wholeWordButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.wholeWordButton.setFocusTraversable(false);

        this.regExpButton.setTooltip(new Tooltip("Regular Expression"));
        this.regExpButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.regExpButton.setFocusTraversable(false);

        this.highlightButton.setTooltip(new Tooltip("Highlight All"));
        this.highlightButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SIZE_M);
        this.highlightButton.setFocusTraversable(false);

        this.closeButton.getStyleClass().addAll(StyleClasses.CROSS_BUTTON, StyleClasses.SIZE_XXS,
                StyleClasses.SQUARE);
        this.closeButton.setFocusTraversable(false);

        this.toolBox.getChildren().addAll(getFindPreviousButton(), getFindNextButton(), getMatchCaseButton(),
                this.wholeWordButton, this.regExpButton, this.highlightButton,
                new Spacer(Spacing.getHorizontal() - Spacing.getHorizontalThird() * 2), this.closeButton);
        this.toolBox.setSpacing(Spacing.getHorizontalThird());
        this.toolBox.setAlignment(Pos.CENTER_LEFT);
        this.toolBox.setPadding(new Insets(0, Spacing.getHorizontal(), 0, Spacing.getHorizontalThird()));
        GridPane.setVgrow(this.toolBox, Priority.ALWAYS);

        gridPane.add(this.findLabelWrapper, 0, 0);
        gridPane.add(getFindComboBoxWrapper(), 1, 0);
        gridPane.add(this.toolBox, 2, 0);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        wholeWordButton.selectedProperty().bindBidirectional(viewModel.wholeWordSelectedProperty());
        wholeWordButton.disableProperty().bind(viewModel.wholeWordDisabledProperty());
        regExpButton.selectedProperty().bindBidirectional(viewModel.regExpSelectedProperty());
        regExpButton.disableProperty().bind(viewModel.regExpDisabledProperty());
        highlightButton.selectedProperty().bindBidirectional(viewModel.highlightSelectedProperty());
        highlightButton.disableProperty().bind(viewModel.highlightDisabledProperty());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        closeButton.setOnAction(e -> viewModel.onCloseRequest());
        wholeWordButton.setOnAction(e -> viewModel.onWholeWord());
        regExpButton.setOnAction(e -> viewModel.onRegExp());
        highlightButton.setOnAction(e -> viewModel.onHighlight());
    }

    protected GridPane getGridPane() {
        return gridPane;
    }

    protected ToggleButton getWholeWordButton() {
        return wholeWordButton;
    }

    protected ToggleButton getRegExpButton() {
        return regExpButton;
    }

    protected ToggleButton getHighlightButton() {
        return highlightButton;
    }

    protected FocusTrap getFocusTrap() {
        return focusTrap;
    }
}
