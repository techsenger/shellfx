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

package com.techsenger.tabshell.web;

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutHistory;
import com.techsenger.tabshell.layout.dock.DockLayoutPresenter;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.web.area.WebAreaFxView;
import com.techsenger.tabshell.web.area.WebAreaPort;
import com.techsenger.tabshell.web.area.WebAreaPresenter;
import com.techsenger.tabshell.web.toolbar.ToolBarFxView;
import com.techsenger.tabshell.web.toolbar.ToolBarPort;
import com.techsenger.tabshell.web.toolbar.ToolBarPresenter;
import javafx.application.Platform;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabFxView<P extends WebBrowserTabPresenter<?, ?>>
        extends AbstractShellTabFxView<P> implements WebBrowserTabView {

    protected class Composer extends AbstractShellTabFxView.Composer implements WebBrowserTabComposer {

        private final WebBrowserTabFxView<?> view = WebBrowserTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);

            var toolBar = createToolBar();
            toolBar.getPresenter().initialize();
            view.getModifiableChildren().add(toolBar);
            view.getContentBox().getChildren().add(toolBar.getNode());
            view.toolBar = toolBar;

            var webParams = (WebComposeParameters) params;
            var area = createArea(webParams.getUrl());
            area.getPresenter().initialize();
            view.area = area;

            var layout = createLayout(null);
            layout.getPresenter().initialize();
            view.getModifiableChildren().add(layout);
            view.getContentBox().getChildren().add(layout.getNode());
            view.layout = layout;

            var splitSpace = layout.getComposer().createSplitSpace(Orientation.HORIZONTAL);
            splitSpace.getPresenter().initialize();
            splitSpace.getComposer().addChild(area);
            layout.getComposer().setRoot(splitSpace);
            layout.getComposer().setMain(area);
        }

        @Override
        public ToolBarPort getToolBar() {
            return view.toolBar.getPresenter().getPort();
        }

        @Override
        public WebAreaPort getArea() {
            return view.area.getPresenter().getPort();
        }

        protected DockLayoutFxView<?> createLayout(HistoryProvider<DockLayoutHistory> provider) {
            var v = new DockLayoutFxView<>();
            var p = new DockLayoutPresenter<>(v, provider);
            return v;
        }

        protected ToolBarFxView<?> createToolBar() {
            var view = new ToolBarFxView<>();
            var presenter = new ToolBarPresenter<>(view);
            return view;
        }

        protected WebAreaFxView<?> createArea(String url) {
            var view = new WebAreaFxView<>();
            var browserPresenter = this.view.getPresenter();
            var presenter = new WebAreaPresenter<>(view, () -> browserPresenter.getPort(),
                    () -> this.view.toolBar.getPresenter().getPort(), url);
            return view;
        }
    }

    private DockLayoutFxView<?> layout;

    private ToolBarFxView<?> toolBar;

    private WebAreaFxView<?> area;

    public WebBrowserTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {
        toolBar.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    protected ToolBarFxView<?> getToolBar() {
        return toolBar;
    }

    protected DockLayoutFxView<?> getLayout() {
        return layout;
    }

    @Override
    public void setIcon(Icon<?> icon) {
        Platform.runLater(() -> super.setIcon(icon));
    }
}
