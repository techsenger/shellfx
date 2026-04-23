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
import com.techsenger.tabshell.devtools.DevToolsTabDockFxView;
import com.techsenger.tabshell.devtools.DevToolsTabDockPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
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
                var devTools = createDevTools();
                devTools.getPresenter().initialize();
                devTools.getPresenter().setDraggable(true);
                c.getComposer().addUtilityDock(devTools);
            } else {
                var alertView = new AlertDialogFxView<>();
                var alertPresenter = new AlertDialogPresenter<>(alertView, AlertDialogType.ERROR,
                        "DevTools can only be opened in the main tab");
                alertPresenter.initialize();
                shell.getComposer().addDialog(alertView);
            }
        } else if (shell.getComposer().getWorkspace() instanceof DockHostFxView<?> dockHost) {
            var devTools = createDevTools();
            devTools.getPresenter().initialize();
            devTools.getPresenter().setDraggable(true);
            dockHost.getComposer().addTabDock(devTools, Side.BOTTOM, 250);
        }
    }

    protected DevToolsTabDockFxView<?> createDevTools() {
        var shell = getComponent();
        var view = new DevToolsTabDockFxView<>(shell, resolveDialogContainer());
        var hm = shell.getPresenter().getContext().getHistoryManager();
        var presenter = new DevToolsTabDockPresenter<>(view, shell.getPresenter().getContext().getSettings(),
                shell.getPresenter().getContext().getHistoryManager());
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
