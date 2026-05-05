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

import com.techsenger.tabshell.core.ShellContext;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.window.AbstractWindowFxView;
import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsWindowFxView<P extends DevToolsWindowPresenter<?>> extends AbstractWindowFxView<P>
        implements DevToolsWindowView {

    public class Composer extends AbstractWindowFxView<P>.Composer implements DevToolsWindowView.Composer {

        private final DevToolsWindowFxView<P> view = DevToolsWindowFxView.this;

        @Override
        public void addTabDock(ShellContext context) {
            var tabDock = createDevToolsDock(context);
            tabDock.getPresenter().initialize();
            VBox.setVgrow(tabDock.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(tabDock.getNode());
        }

        protected DevToolsTabDockFxView<?> createDevToolsDock(ShellContext context) {
            var view = new DevToolsTabDockFxView<>(this.view.shell, this.view);
            var presenter = new DevToolsTabDockPresenter<>(view, context.getSettings(), context.getHistoryManager());
            return view;
        }
    }

    private final ShellFxView<?> shell;

    public DevToolsWindowFxView(ShellFxView<?> shell, List<Stylesheet> stylesheets) {
        super(stylesheets);
        this.shell = shell;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    protected void build() {
        super.build();
        getWindow().setAlwaysOnTop(true);
    }


}
