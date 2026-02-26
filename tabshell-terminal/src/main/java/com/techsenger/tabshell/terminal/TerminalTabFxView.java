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
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.dockhost.DockHostHistory;
import com.techsenger.tabshell.layout.dockhost.DockHostPresenter;
import com.techsenger.tabshell.terminal.area.TerminalAreaFxView;
import com.techsenger.tabshell.terminal.area.TerminalAreaPresenter;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.terminal.toolbar.ToolBarFxView;
import com.techsenger.tabshell.terminal.toolbar.ToolBarPort;
import com.techsenger.tabshell.terminal.toolbar.ToolBarPresenter;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabFxView<P extends TerminalTabPresenter<?, ?>> extends AbstractTabFxView<P>
        implements TerminalTabView {

    public class Composer extends AbstractTabFxView.Composer implements TerminalTabComposer {

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

        protected DockHostFxView<?> createLayout(HistoryProvider<DockHostHistory> provider) {
            var v = new DockHostFxView<>();
            var p = new DockHostPresenter<>(v, provider);
            return v;
        }

        protected TerminalAreaFxView<?> createArea() {
            var view = new TerminalAreaFxView<>(this.view.getShell(), tabContainer);
            var presenter = new TerminalAreaPresenter<>(view, () -> getPresenter().getPort(),
                    () -> toolBar.getPresenter().getPort());
            return view;
        }
    }

    private final TabContainerFxView<?> tabContainer;

    private ToolBarFxView<?> toolBar;

    private DockHostFxView<?> layout;

    private TerminalAreaFxView<?> area;

    public TerminalTabFxView(ShellFxView<?> shell, TabContainerFxView<?> tabContainer) {
        super(shell);
        this.tabContainer = tabContainer;
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
    protected void build() {
        super.build();
        setIcon(TerminalIcons.TERMINAL);
        setTitle("Terminal");
    }

    @Override
    protected Composer createComposer() {
        return new TerminalTabFxView.Composer();
    }
}
