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

import com.techsenger.mvvm4fx.core.ComponentMediator;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.jfx.inspector.JfxInspectorTabView;
import com.techsenger.tabshell.jfx.inspector.JfxInspectorTabViewModel;
import com.techsenger.tabshell.layout.dock.AbstractTabDockComposer;
import devtoolsfx.connector.Connector;
import devtoolsfx.connector.LocalConnector;

/**
 *
 * @author Pavel Castornii
 */
public class JfxTabDockComposer<T extends JfxTabDockView<?>> extends AbstractTabDockComposer<T> {

    private final ShellTabView<?> shellTab;

    private final Connector connector;

    private final JfxInspectorTabView<?> inpector;

    public JfxTabDockComposer(ShellTabView<?> shellTab, T view) {
        super(view);
        this.shellTab = shellTab;
        this.connector = new LocalConnector(shellTab.getShell().getStage(), null);
        this.inpector = createInspector();
    }

    @Override
    public void initialize() {
        super.initialize();
        this.connector.start();
        this.inpector.initialize();
    }

    @Override
    public void deinitialize() {
        super.deinitialize();
        this.inpector.deinitialize();
        this.connector.stop();
    }

    public JfxInspectorTabView<?> getInpector() {
        return inpector;
    }

    protected JfxInspectorTabView<?> createInspector() {
        var vm = new JfxInspectorTabViewModel(shellTab.getViewModel(), connector);
        var v = new JfxInspectorTabView<>(shellTab, vm);
        return v;
    }

    @Override
    protected ComponentMediator createMediator() {
        return new AbstractTabDockComposer.Mediator() { };
    }
}
