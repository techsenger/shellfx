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

import com.techsenger.tabshell.dialogs.DialogShellTabComposer;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabComposer<T extends WebBrowserTabView<?>> extends DialogShellTabComposer<T> {

    protected class Mediator extends DialogShellTabComposer.Mediator implements WebBrowserTabMediator {

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

    public WebBrowserTabComposer(T view) {
        super(view);
    }

    @Override
    public WebBrowserTabMediator getMediator() {
        return (WebBrowserTabMediator) super.getMediator();
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

    public WebToolBarView<?> getToolBar() {
        return this.toolBar;
    }

    public void addToolBar(VBox contentPane) {
        contentPane.getChildren().add(this.toolBar.getNode());
    }

    @Override
    protected WebBrowserTabMediator createMediator() {
        return new WebBrowserTabComposer.Mediator();
    }

    protected WebToolBarView<?> createToolBar() {
        return new WebToolBarView(getView(), getMediator().getToolBar());
    }
}
