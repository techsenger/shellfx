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

package com.techsenger.tabshell.web.toolbar;

import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public class ToolBarFxView<P extends ToolBarPresenter<?, ?>> extends AbstractAreaFxView<P> implements ToolBarView {

    private final Button backButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_LEFT));

    private final Button forwardButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_RIGHT));

    private final Button reloadButton = new Button(null, new FontIconView(SharedIcons.RELOAD));

    private final TextField urlTextField = new TextField();

    private final ToolBar toolBar = new ToolBar(backButton, forwardButton, reloadButton, urlTextField);

    public ToolBarFxView() {
        super();
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(urlTextField);
    }

    @Override
    public ToolBar getNode() {
        return this.toolBar;
    }

    @Override
    public boolean isBackDisabled() {
        return backButton.isDisable();
    }

    @Override
    public void setBackDisabled(boolean value) {
        this.backButton.setDisable(value);
    }

    @Override
    public boolean isForwardDisabled() {
        return forwardButton.isDisable();
    }

    @Override
    public void setForwardDisabled(boolean value) {
        this.forwardButton.setDisable(value);
    }

    @Override
    public boolean isReloadDisabled() {
        return reloadButton.isDisable();
    }

    @Override
    public void setReloadDisabled(boolean value) {
        this.reloadButton.setDisable(value);
    }

    @Override
    public String getUrl() {
        return urlTextField.getText();
    }

    @Override
    public void setUrl(String value) {
        this.urlTextField.setText(value);
    }

    @Override
    protected void build() {
        super.build();
        backButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        backButton.setTooltip(new Tooltip("Back"));
        backButton.setDisable(true);
        forwardButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        forwardButton.setTooltip(new Tooltip("Forward"));
        forwardButton.setDisable(true);
        reloadButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        reloadButton.setTooltip(new Tooltip("Reload"));
        reloadButton.setDisable(true);

        HBox.setHgrow(urlTextField, Priority.ALWAYS);
        toolBar.getStyleClass().add(StyleClasses.BLEND);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        urlTextField.focusedProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                Platform.runLater(() -> urlTextField.selectAll());
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var presenter = getPresenter();
        reloadButton.setOnAction(e -> presenter.onReload());
        backButton.setOnAction(e -> presenter.onBack());
        forwardButton.setOnAction(e -> presenter.onForward());
        urlTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                presenter.onLoad(urlTextField.getText());
            }
        });
    }

    protected Button getBackButton() {
        return backButton;
    }

    protected Button getForwardButton() {
        return forwardButton;
    }

    protected Button getReloadButton() {
        return reloadButton;
    }

    protected TextField getUrlTextField() {
        return urlTextField;
    }
}
