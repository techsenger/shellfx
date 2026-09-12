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

package com.techsenger.shellfx.demo.controls;

import com.techsenger.connectorfx.LocalConnector;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.devtools.DevToolsHostType;
import com.techsenger.shellfx.devtools.DevToolsTabDockView;
import com.techsenger.shellfx.devtools.DevToolsTabDockParams;
import com.techsenger.shellfx.devtools.DevToolsTabDockViewModel;
import com.techsenger.shellfx.devtools.DevToolsWindowView;
import com.techsenger.shellfx.devtools.DevToolsWindowParams;
import com.techsenger.shellfx.devtools.DevToolsWindowViewModel;
import com.techsenger.shellfx.layout.dockhost.DockHostView;
import com.techsenger.shellfx.layout.tabhost.TabHostView;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;
import javafx.geometry.Side;
import com.techsenger.shellfx.layout.dockhost.UtilityDockContainerView;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsItemHandler extends AbstractMenuItemHandler<ShellView<?>, ManagedMenuItem> {

    public DevToolsItemHandler(ShellView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() != null) {
            if (shell.getComposer().getWorkspace() instanceof TabHostView<?> tabHost) {
                var tab = tabHost.getComposer().getSelectedTab();
                if (tab != null && tab instanceof UtilityDockContainerView<?> c) {
                    var iterator = tab.getComposer().depthFirstIterator();
                    boolean devToolsPresent = false;
                    while (iterator.hasNext()) {
                        if (iterator.next() instanceof DevToolsTabDockView<?>) {
                            devToolsPresent = true;
                            break;
                        }
                    }
                    if (!devToolsPresent) {
                        var devTools = createDevToolsDock();
                        devTools.getViewModel().setDraggable(true);
                        c.getComposer().addUtilityDock(devTools);
                    }
                } else {
                    openInWindow();
                }
            } else if (shell.getComposer().getWorkspace() instanceof DockHostView<?> dockHost) {
                var devTools = createDevToolsDock();
                devTools.getViewModel().setDraggable(true);
                dockHost.getComposer().addTabDock(devTools, Side.BOTTOM, 250);
            }
        } else {
            openInWindow();
        }
    }

    protected void openInWindow() {
        var devTools = createDevToolsWindow();
        devTools.getComposer().addTabDock();
        devTools.getStage().show();
    }

    protected DevToolsTabDockView<?> createDevToolsDock() {
        var shell = getComponent();
        var context = shell.getViewModel().getContext();
        var connector = new LocalConnector(shell.getStage(), null);
        var params = new DevToolsTabDockParams(DevToolsHostType.SPLIT_SPACE,
                context.getSettings(), context.getHistoryManager(), connector, shell.getStage().hashCode());
        var viewModel = new DevToolsTabDockViewModel<>(params);
        var view = new DevToolsTabDockView<>(viewModel, shell, resolveDialogContainer());
        view.initialize();
        return view;
    }

    protected DevToolsWindowView<?> createDevToolsWindow() {
        var context = getComponent().getViewModel().getContext();
        var params = new DevToolsWindowParams(context.getSettings().getAppearance(), context.getHistoryManager());
        var viewModel = new DevToolsWindowViewModel<>(params);
        var view = new DevToolsWindowView<>(viewModel, getComponent());
        view.initialize();
        return view;
    }

    private WindowContainerView<?> resolveDialogContainer() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostView<?> tabHost) {
            var tab = tabHost.getComposer().getSelectedTab();
            return (WindowContainerView<?>) tab;
        } else {
            return shell;
        }
    }

}
