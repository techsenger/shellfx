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

package com.techsenger.tabshell.jfx;

import com.techsenger.tabshell.core.tab.ShellTabComponent;
import com.techsenger.tabshell.jfx.eventlog.EventLogTabComponent;
import com.techsenger.tabshell.jfx.eventlog.EventLogTabView;
import com.techsenger.tabshell.jfx.eventlog.EventLogTabViewModel;
import com.techsenger.tabshell.jfx.inspector.JfxInspectorTabComponent;
import com.techsenger.tabshell.jfx.inspector.JfxInspectorTabView;
import com.techsenger.tabshell.jfx.inspector.JfxInspectorTabViewModel;
import com.techsenger.tabshell.layout.dock.DockLayoutComponent;
import com.techsenger.tabshell.layout.dock.TabDockComponent;
import devtoolsfx.connector.Connector;
import devtoolsfx.connector.LocalConnector;

/**
 *
 * @author Pavel Castornii
 */
public class JfxTabDockComponent<T extends JfxTabDockView<?, ?>> extends TabDockComponent<T> {

    protected class Mediator extends TabDockComponent.Mediator implements JfxTabDockMediator {

        @Override
        public Connector getConnector() {
            return connector;
        }

    }

    private final ShellTabComponent<?> shellTab;

    private final Connector connector;

    private final JfxInspectorTabComponent<?> inpectorTab;

    private final EventLogTabComponent<?> eventLogTab;

    public JfxTabDockComponent(T view, DockLayoutComponent<?> layout, ShellTabComponent<?> shellTab) {
        super(view, layout);
        this.shellTab = shellTab;
        this.connector = new LocalConnector(shellTab.getShell().getView().getStage(), null);
        this.inpectorTab = createInspectorTab();
        this.inpectorTab.initialize();
        getModifiableChildren().add(this.inpectorTab);
        this.eventLogTab = createEventLogTab();
        this.eventLogTab.initialize();
        getModifiableChildren().add(this.eventLogTab);
    }

    @Override
    public void preInitialize() {
        super.preInitialize();
        this.connector.start();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var tabPane = getView().getNode();
        tabPane.getTabs().add(this.inpectorTab.getView().getNode());
        tabPane.getTabs().add(this.eventLogTab.getView().getNode());
        tabPane.getSelectionModel().select(0);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.connector.stop();
    }

    public JfxInspectorTabComponent<?> getInpectorTab() {
        return inpectorTab;
    }

    public EventLogTabComponent<?> getEventLogTab() {
        return eventLogTab;
    }

    @Override
    public Mediator createMediator() {
        return new Mediator();
    }

    protected JfxInspectorTabComponent<?> createInspectorTab() {
        var vm = new JfxInspectorTabViewModel(connector);
        var v = new JfxInspectorTabView<>(vm);
        var c = new JfxInspectorTabComponent<>(v, shellTab);
        return c;
    }

    protected EventLogTabComponent<?> createEventLogTab() {
        var vm = new EventLogTabViewModel(connector);
        var v = new EventLogTabView<>(vm);
        var c = new EventLogTabComponent<>(v);
        return c;
    }
}
