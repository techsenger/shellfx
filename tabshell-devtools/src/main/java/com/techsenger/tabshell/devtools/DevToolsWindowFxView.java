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

package com.techsenger.tabshell.devtools;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.window.AbstractWindowFxView;
import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsWindowFxView<P extends DevToolsWindowPresenter<?>> extends AbstractWindowFxView<P>
        implements DevToolsWindowView {

    public class Composer extends AbstractWindowFxView<P>.Composer implements DevToolsWindowView.Composer {

        private final DevToolsWindowFxView<P> view = DevToolsWindowFxView.this;

        private DevToolsTabDockFxView<?> tabDock;

        private HistoryManager historyManager;

        public void addTabDock() {
            var tabDock = createTabDock();
            doAddTabDock(tabDock);
        }

        public void addTabDock(DevToolsTabDockFxView<?> tabDock) {
            doAddTabDock(tabDock);
            tabDock.getPresenter().setHostType(DevToolsHostType.WINDOW);
        }

        @Override
        public void setHistoryManager(HistoryManager historyManager) {
            this.historyManager = historyManager;
        }

        protected DevToolsTabDockFxView<?> createTabDock() {
            var view = new DevToolsTabDockFxView<>(this.view.shell, null);
            var context = this.view.shell.getPresenter().getContext();
            var params = new DevToolsTabDockParams(DevToolsHostType.WINDOW, context.getSettings(), historyManager);
            var presenter = new DevToolsTabDockPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        private void doAddTabDock(DevToolsTabDockFxView<?> tabDock) {
            this.tabDock = tabDock;
            getModifiableChildren().add(tabDock);
            VBox.setVgrow(tabDock.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(tabDock.getNode());
        }
    }

    private final ShellFxView<?> shell;

    public DevToolsWindowFxView(ShellFxView<?> shell, List<Stylesheet> stylesheets) {
        super(new Stage(), stylesheets);
        this.shell = shell;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        getStage().setAlwaysOnTop(true);
    }
}
