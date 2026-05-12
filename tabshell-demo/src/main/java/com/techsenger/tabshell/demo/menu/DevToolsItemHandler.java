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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.core.menu.AbstractMenuItemHandler;
import com.techsenger.tabshell.devtools.DevToolsHostType;
import com.techsenger.tabshell.devtools.DevToolsTabDockFxView;
import com.techsenger.tabshell.devtools.DevToolsTabDockPresenter;
import com.techsenger.tabshell.devtools.DevToolsWindowFxView;
import com.techsenger.tabshell.devtools.DevToolsWindowPresenter;
import com.techsenger.tabshell.icons.IconStylesheetFactory;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.dockhost.UtilityDockContainerFxView;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public class DevToolsItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public DevToolsItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostFxView<?> tabHost) {
            var tab = tabHost.getComposer().getSelectedTab();
            if (tab != null && tab instanceof UtilityDockContainerFxView<?> c) {
                var devTools = createDevToolsDock();
                devTools.getPresenter().initialize();
                devTools.getPresenter().setDraggable(true);
                c.getComposer().addUtilityDock(devTools);
            } else {
                var devTools = createDevToolsWindow();
                devTools.getPresenter().initialize();
                devTools.getPresenter().setMaximizable(false);
                devTools.getComposer().addTabDock();
                devTools.getWindow().show();
            }
        } else if (shell.getComposer().getWorkspace() instanceof DockHostFxView<?> dockHost) {
            var devTools = createDevToolsDock();
            devTools.getPresenter().initialize();
            devTools.getPresenter().setDraggable(true);
            dockHost.getComposer().addTabDock(devTools, Side.BOTTOM, 250);
        }
    }

    protected DevToolsTabDockFxView<?> createDevToolsDock() {
        var shell = getComponent();
        var view = new DevToolsTabDockFxView<>(shell, resolveDialogContainer());
        var context = shell.getPresenter().getContext();
        var presenter = new DevToolsTabDockPresenter<>(view, DevToolsHostType.SPLIT_SPACE,
                context.getSettings(), context.getHistoryManager());
        return view;
    }

    protected DevToolsWindowFxView<?> createDevToolsWindow() {
        var view = new DevToolsWindowFxView<>(getComponent(), IconStylesheetFactory.forAll());
        var context = getComponent().getPresenter().getContext();
        var presenter = new DevToolsWindowPresenter<>(view, context.getSettings(), context.getHistoryManager());
        return view;
    }

    private DialogContainerFxView<?> resolveDialogContainer() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostFxView<?> tabHost) {
            var tab = tabHost.getComposer().getSelectedTab();
            return tab;
        } else {
            return shell;
        }
    }

}
