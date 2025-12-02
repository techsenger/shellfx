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

import com.techsenger.tabshell.dialogs.AbstractDialogShellTabComposer;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserComposer<T extends WebBrowserTabView<?>> extends AbstractDialogShellTabComposer<T>
        implements WebBrowserTabView.Composer {

    protected class ViewModelComposer extends AbstractDialogShellTabComposer.ViewModelComposer
            implements WebBrowserTabViewModel.Composer {

        private final WebToolBarViewModel toolBar = createToolBar();

        @Override
        public WebToolBarViewModel getToolBar() {
            return toolBar;
        }

        protected WebToolBarViewModel createToolBar() {
            return new WebToolBarViewModel();
        }
    }

    private WebToolBarView<?> toolBar = createToolBar();

    public WebBrowserComposer(T view) {
        super(view);
    }

    @Override
    public WebBrowserComposer.ViewModelComposer getViewModelComposer() {
        return (WebBrowserComposer.ViewModelComposer) super.getViewModelComposer();
    }

    @Override
    public void initialize() {
        super.initialize();
        this.toolBar.initialize();
    }

    @Override
    public void deinitialize() {
        super.deinitialize();
        this.toolBar.deinitialize();
    }

    @Override
    public WebToolBarView<?> getToolBar() {
        return this.toolBar;
    }

    @Override
    public void addToolBar(VBox contentPane) {
        contentPane.getChildren().add(this.toolBar.getNode());
    }

    @Override
    protected WebBrowserComposer.ViewModelComposer createViewModelComposer() {
        return new WebBrowserComposer.ViewModelComposer();
    }

    protected WebToolBarView<?> createToolBar() {
        return new WebToolBarView(getView(), getViewModelComposer().getToolBar());
    }
}
