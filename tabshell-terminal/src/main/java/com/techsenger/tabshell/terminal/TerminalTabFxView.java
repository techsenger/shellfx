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

package com.techsenger.tabshell.terminal;

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.area.AreaPort;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutFxView;
import com.techsenger.tabshell.layout.dock.DockLayoutHistory;
import com.techsenger.tabshell.layout.dock.DockLayoutPresenter;
import com.techsenger.tabshell.terminal.area.TerminalAreaFxView;
import com.techsenger.tabshell.terminal.area.TerminalAreaPresenter;
import com.techsenger.tabshell.terminal.toolbar.ToolBarFxView;
import com.techsenger.tabshell.terminal.toolbar.ToolBarPort;
import com.techsenger.tabshell.terminal.toolbar.ToolBarPresenter;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabFxView<P extends TerminalTabPresenter<?, ?>> extends AbstractShellTabFxView<P>
        implements TerminalTabView {

    public class Composer extends AbstractShellTabFxView.Composer implements TerminalTabComposer {

        private final TerminalTabFxView<?> view = TerminalTabFxView.this;

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);

            toolBar = createToolBar();
            toolBar.getPresenter().initialize();
            view.getModifiableChildren().add(toolBar);
            view.getContentBox().getChildren().add(toolBar.getNode());

            layout = createLayout(null);
            layout.getPresenter().initialize();
            view.getModifiableChildren().add(layout);
            view.getContentBox().getChildren().add(layout.getNode());

            area = createArea();
            area.getPresenter().initialize();

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
        public AreaPort getArea() {
            return view.area.getPresenter().getPort();
        }

        protected ToolBarFxView<?> createToolBar() {
            var view = new ToolBarFxView<>();
            var presenter = new ToolBarPresenter<>(view, getPresenter().getHistory().getToolBar());
            return view;
        }

        protected DockLayoutFxView<?> createLayout(HistoryProvider<DockLayoutHistory> provider) {
            var v = new DockLayoutFxView<>();
            var p = new DockLayoutPresenter<>(v, provider);
            return v;
        }

        protected TerminalAreaFxView<?> createArea() {
            var view = new TerminalAreaFxView<>(this.view.getShell());
            var presenter = new TerminalAreaPresenter<>(view, () -> getPresenter().getPort(),
                    () -> toolBar.getPresenter().getPort());
            return view;
        }
    }

    private ToolBarFxView<?> toolBar;

    private DockLayoutFxView<?> layout;

    private TerminalAreaFxView<?> area;

    public TerminalTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {
        area.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new TerminalTabFxView.Composer();
    }
}
