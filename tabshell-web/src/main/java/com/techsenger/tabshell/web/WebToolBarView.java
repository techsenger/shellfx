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

package com.techsenger.tabshell.web;

import com.techsenger.tabshell.core.area.AbstractAreaView;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.icon.FontIconView;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.techsenger.tabshell.shared.style.SharedIcons;

/**
 *
 * @author Pavel Castornii
 */
public class WebToolBarView<T extends WebToolBarViewModel> extends AbstractAreaView<T> {

    private final WebBrowserTabView<?> webBrowser;

    private final Button backButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_LEFT));

    private final Button forwardButton = new Button(null, new FontIconView(SharedIcons.CHEVRON_RIGHT));

    private final Button reloadButton = new Button(null, new FontIconView(SharedIcons.RELOAD));

    private final TextField urlTextField = new TextField();

    private final ToolBar toolBar = new ToolBar(backButton, forwardButton, reloadButton, urlTextField);

    public WebToolBarView(WebBrowserTabView<?> webBrowser, T viewModel) {
        super(viewModel);
        this.webBrowser = webBrowser;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public ToolBar getNode() {
        return this.toolBar;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        backButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        backButton.setTooltip(new Tooltip("Back"));
        forwardButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        forwardButton.setTooltip(new Tooltip("Forward"));
        reloadButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        reloadButton.setTooltip(new Tooltip("Reload"));

        HBox.setHgrow(urlTextField, Priority.ALWAYS);
        toolBar.getStyleClass().add(StyleClasses.BLEND);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        backButton.disableProperty().bind(viewModel.backDisableProperty());
        forwardButton.disableProperty().bind(viewModel.forwardDisableProperty());
        reloadButton.disableProperty().bind(viewModel.reloadDisableProperty());
        urlTextField.textProperty().bindBidirectional(viewModel.urlProperty());
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        urlTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                this.webBrowser.getViewModel().load();
            }
        });
        reloadButton.setOnAction(e -> this.webBrowser.reload());
        backButton.setOnAction(e -> this.webBrowser.getViewModel().goBack());
        forwardButton.setOnAction(e -> this.webBrowser.getViewModel().goForward());
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
