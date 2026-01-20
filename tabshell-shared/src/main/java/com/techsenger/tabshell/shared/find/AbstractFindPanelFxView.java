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
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractFindPanelFxView<P extends AbstractFindPanelPresenter<?, ?>>
        extends AbstractAreaFxView<P> implements FindPanelView {

    private static final String RESULT_LABEL_STYLE_CLASS = "result";

    private final ComboBox<String> findComboBox = new ComboBox<>();

    private final Label matchesLabel = new Label();

    private final StackPane findComboBoxWrapper = new StackPane(findComboBox, matchesLabel);

    private final ToggleButton matchCaseButton = new ToggleButton(null, new FontIconView(SharedIcons.MATCH_CASE));

    private final BooleanProperty notFound = new SimpleBooleanProperty();

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
    public String getFindText() {
        return this.findComboBox.getEditor().textProperty().get();
    }

    @Override
    public void setMatchCaseSelected(boolean value) {
        this.matchCaseButton.setSelected(value);
    }

    @Override
    public boolean isMatchCaseSelected() {
        return this.matchCaseButton.isSelected();
    }

    @Override
    public void setMatchCaseDisable(boolean value) {
        this.matchCaseButton.setDisable(value);
    }

    @Override
    public boolean isMatchCaseDisable() {
        return this.matchCaseButton.isDisable();
    }

    @Override
    public void setMatchesText(String text) {
        this.matchesLabel.textProperty().set(text);
    }

    @Override
    public String getMatchesText() {
        return this.matchesLabel.textProperty().get();
    }

    @Override
    public void setMatchesVisible(boolean visible) {
        this.matchesLabel.setVisible(visible);
    }

    @Override
    public boolean isMatchesVisible() {
        return this.matchesLabel.isVisible();
    }

    @Override
    public void setFindTexts(List<String> texts) {
        this.findComboBox.getItems().clear();
        this.findComboBox.getItems().addAll(texts);
    }

    @Override
    public List<String> getFindTexts() {
        return this.findComboBox.getItems();
    }

    @Override
    public void setNotFound(boolean value) {
        this.notFound.set(value);
    }

    @Override
    public boolean isNotFound() {
        return this.notFound.get();
    }

    @Override
    protected void build() {
        super.build();
        this.findComboBox.setEditable(true);
        this.findComboBox.getStyleClass().addAll(Styles.DENSE, StyleClasses.NO_SELECTED);
        this.matchesLabel.getStyleClass().add(RESULT_LABEL_STYLE_CLASS);
        StackPane.setMargin(this.matchesLabel, new Insets(0, SizeConstants.INSET * 2, 0, 0));
        this.findComboBoxWrapper.setAlignment(Pos.CENTER_RIGHT);
        this.findComboBoxWrapper.setPadding(new Insets(SizeConstants.THIRD_INSET, 0,
                SizeConstants.THIRD_INSET, 0));

        this.matchCaseButton.setTooltip(new Tooltip("Match Case"));
        this.matchCaseButton.setSelected(false);
        this.matchCaseButton.getStyleClass().addAll(StyleClasses.ICONED_BUTTON, Styles.FLAT);
        this.matchCaseButton.setFocusTraversable(false);
    }

    @Override
    protected void bind() {
        super.bind();
        this.findComboBox.maxWidthProperty().bind(this.findComboBoxWrapper.widthProperty().subtract(1));
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
        this.findComboBox.getEditor().textProperty().addListener((ov, t, t1) -> getPresenter().handleResetMatches());
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.matchCaseButton.setOnAction((event) -> getPresenter().handleResetMatches());
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

    protected StackPane getFindComboBoxWrapper() {
        return findComboBoxWrapper;
    }

    protected ToggleButton getMatchCaseButton() {
        return matchCaseButton;
    }
}
