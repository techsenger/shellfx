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

import com.techsenger.mvvm4fx.core.ComponentComposer;
import com.techsenger.mvvm4fx.core.ComponentView;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabView<T extends WebBrowserTabViewModel> extends AbstractShellTabView<T> {

    public interface Composer extends ComponentView.Composer {

        WebToolBarView<?> getToolBar();

        void addToolBar(VBox contentPane);

    }

    private final WebView webView = new WebView();

    public WebBrowserTabView(ShellView<?> shell, T viewModel) {
        super(shell, viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public WebBrowserTabView.Composer getComposer() {
        return (WebBrowserTabView.Composer) super.getComposer();
    }

    @Override
    protected ComponentComposer<?> createComposer() {
        return new WebBrowserComposer<>(this);
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        getComposer().initialize();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        VBox.setVgrow(webView, Priority.ALWAYS);
        getComposer().addToolBar(getContentPane());
        getContentPane().getChildren().add(webView);
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.getUrlSource().addListener((ulr) -> this.webView.getEngine().load(ulr));
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        viewModel.pageTitleProperty().bind(this.webView.getEngine().titleProperty());
    }

    @Override
    protected void preDeinitialize(T viewModel) {
        super.preDeinitialize(viewModel);
        webView.getEngine().load(null);
    }

    @Override
    protected void postDeinitialize(T viewModel) {
        super.postDeinitialize(viewModel);
        getComposer().deinitialize();
    }

    protected void reload() {
        this.webView.getEngine().reload();
    }
}
