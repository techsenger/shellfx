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
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
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
public abstract class AbstractFullFindPanelFxView<P extends AbstractFullFindPanelPresenter<?, ?>>
        extends AbstractNavigableFindPanelFxView<P> implements FullFindPanelView {

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
    public void setWholeWordSelected(boolean value) {
        this.wholeWordButton.setSelected(value);
    }

    @Override
    public boolean isWholeWordSelected() {
        return this.wholeWordButton.isSelected();
    }

    @Override
    public void setWholeWordDisable(boolean value) {
        this.wholeWordButton.setDisable(value);
    }

    @Override
    public boolean isWholeWordDisable() {
        return this.wholeWordButton.isDisable();
    }

    @Override
    public void setRegExpSelected(boolean value) {
        this.regExpButton.setSelected(value);
    }

    @Override
    public boolean isRegExpSelected() {
        return this.regExpButton.isSelected();
    }

    @Override
    public void setRegExpDisable(boolean value) {
        this.regExpButton.setDisable(value);
    }

    @Override
    public boolean isRegExpDisable() {
        return this.regExpButton.isDisable();
    }

    @Override
    public void setHighlightSelected(boolean value) {
        this.highlightButton.setSelected(value);
    }

    @Override
    public boolean isHighlightSelected() {
        return this.highlightButton.isSelected();
    }

    @Override
    public void setHighlightDisable(boolean value) {
        this.highlightButton.setDisable(value);
    }

    @Override
    public boolean isHighlightDisable() {
        return this.highlightButton.isDisable();
    }

    @Override
    protected void build() {
        super.build();
        var css = AbstractFullFindPanelFxView.class.getResource("find.css").toExternalForm();
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
        GridPane.setVgrow(getFindComboBoxWrapper(), Priority.ALWAYS);

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

        this.toolBox.getChildren().addAll(getFindPreviousButton(), getFindNextButton(), getMatchCaseButton(),
                this.wholeWordButton, this.regExpButton, this.highlightButton,
                new Spacer(SizeConstants.INSET - SizeConstants.THIRD_INSET * 2), this.closeButton);
        this.toolBox.getStyleClass().add(Styles.DENSE);
        this.toolBox.setSpacing(SizeConstants.THIRD_INSET);
        this.toolBox.setAlignment(Pos.CENTER_LEFT);
        this.toolBox.setPadding(new Insets(0, SizeConstants.INSET, 0, SizeConstants.THIRD_INSET));
        GridPane.setVgrow(this.toolBox, Priority.ALWAYS);

        gridPane.add(this.findLabelWrapper, 0, 0);
        gridPane.add(getFindComboBoxWrapper(), 1, 0);
        gridPane.add(this.toolBox, 2, 0);
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var p = getPresenter();
        this.wholeWordButton.setOnAction((event) -> p.handleResetMatches());
        this.regExpButton.setOnAction((event) -> p.handleResetMatches());
        this.closeButton.setOnAction(e -> p.handleClose());
    }

    protected GridPane getGridPane() {
        return gridPane;
    }

    protected FocusTrap getFocusTrap() {
        return focusTrap;
    }

    protected ToggleButton getHighlightButton() {
        return highlightButton;
    }
}
