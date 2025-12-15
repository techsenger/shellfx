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

import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.toolkit.fx.binding.ListBinder;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabView<T extends WebBrowserTabViewModel<?>, S extends WebBrowserTabComponent<?>>
        extends AbstractShellTabView<T, S> {

    private final WebView webView = new WebView();

    private ListBinder<String, WebHistory.Entry> entryBinder;

    public WebBrowserTabView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(webView, Priority.ALWAYS);
        getContentPane().getChildren().add(webView);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.getUrlSource().addListener((ulr) -> this.webView.getEngine().load(ulr));
        var webViewHistory = this.webView.getEngine().getHistory();
        this.entryBinder = ListBinder.bindContent(viewModel.getModifiableHistoryEntries(), webViewHistory.getEntries(),
                (e) -> e.getUrl());
        viewModel.historyIndexProperty().addListener((ov, oldV, newV) -> {
            if (newV.intValue() != webViewHistory.getCurrentIndex()) {
                int currentIndex = webViewHistory.getCurrentIndex();
                int offset = newV.intValue() - currentIndex;
                webViewHistory.go(offset);
            }
        });
        webViewHistory.currentIndexProperty()
                .addListener((ov, oldV, newV) -> viewModel.setHistoryIndex(newV.intValue()));
        viewModel.getLocationWrapper().bind(this.webView.getEngine().locationProperty());
    }

    @Override
    protected void bind() {
        super.bind();
        getViewModel().pageTitleProperty().bind(this.webView.getEngine().titleProperty());
    }

    @Override
    protected void deinitialize() {
        webView.getEngine().load(null);
        super.deinitialize();
    }

    protected void reload() {
        this.webView.getEngine().reload();
    }

    void addToolBar(ToolBar bar) {
        getContentPane().getChildren().add(0, bar);
    }
}
