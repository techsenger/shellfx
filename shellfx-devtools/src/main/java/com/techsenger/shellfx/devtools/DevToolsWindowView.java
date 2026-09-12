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

package com.techsenger.shellfx.devtools;

import com.techsenger.connectorfx.LocalConnector;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsWindowView<VM extends DevToolsWindowViewModel<?>> extends AbstractWindowView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer {

        private final DevToolsWindowView<VM> view = DevToolsWindowView.this;

        private DevToolsTabDockView<?> tabDock;

        public void addTabDock() {
            var tabDock = createTabDock();
            doAddTabDock(tabDock);
        }

        public void addTabDock(DevToolsTabDockView<?> tabDock) {
            doAddTabDock(tabDock);
            tabDock.getViewModel().setHostType(DevToolsHostType.WINDOW);
        }

        protected DevToolsTabDockView<?> createTabDock() {
            var shell = this.view.shell;
            var context = shell.getViewModel().getContext();
            var connector = new LocalConnector(shell.getStage(), null);
            var params = new DevToolsTabDockParams(DevToolsHostType.WINDOW, context.getSettings(),
                    getViewModel().getHistoryManager(), connector, shell.getStage().hashCode());
            var tabDockViewModel = new DevToolsTabDockViewModel<>(params);
            var tabDockView = new DevToolsTabDockView<>(tabDockViewModel, shell, null);
            tabDockView.initialize();
            return tabDockView;
        }

        private void doAddTabDock(DevToolsTabDockView<?> tabDock) {
            this.tabDock = tabDock;
            getModifiableChildren().add(tabDock);
            VBox.setVgrow(tabDock.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(tabDock.getNode());
        }
    }

    private final ShellView<?> shell;

    public DevToolsWindowView(VM viewModel, ShellView<?> shell) {
        super(viewModel, new Stage(), null);
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
