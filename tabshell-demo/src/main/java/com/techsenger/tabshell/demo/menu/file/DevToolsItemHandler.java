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

package com.techsenger.tabshell.demo.menu.file;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.menu.AbstractMenuItemHandler;
import com.techsenger.tabshell.core.window.WindowContainerFxView;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.DevToolsHostType;
import com.techsenger.tabshell.devtools.DevToolsTabDockFxView;
import com.techsenger.tabshell.devtools.DevToolsTabDockParams;
import com.techsenger.tabshell.devtools.DevToolsTabDockPresenter;
import com.techsenger.tabshell.devtools.DevToolsWindowFxView;
import com.techsenger.tabshell.devtools.DevToolsWindowParams;
import com.techsenger.tabshell.devtools.DevToolsWindowPresenter;
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
        if (shell.getComposer().getWorkspace() != null) {
            if (shell.getComposer().getWorkspace() instanceof TabHostFxView<?> tabHost) {
                var tab = tabHost.getComposer().getSelectedTab();
                if (tab != null && tab instanceof UtilityDockContainerFxView<?> c) {
                    var iterator = tab.getComposer().depthFirstIterator();
                    boolean devToolsPresent = false;
                    while (iterator.hasNext()) {
                        if (iterator.next().getDescriptor().getName() == DevToolsComponents.TAB_DOCK) {
                            devToolsPresent = true;
                            break;
                        }
                    }
                    if (!devToolsPresent) {
                        var devTools = createDevToolsDock();
                        devTools.getPresenter().setDraggable(true);
                        c.getComposer().addUtilityDock(devTools);
                    }
                } else {
                    openInWindow();
                }
            } else if (shell.getComposer().getWorkspace() instanceof DockHostFxView<?> dockHost) {
                var devTools = createDevToolsDock();
                devTools.getPresenter().setDraggable(true);
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

    protected DevToolsTabDockFxView<?> createDevToolsDock() {
        var shell = getComponent();
        var view = new DevToolsTabDockFxView<>(shell, resolveDialogContainer());
        var context = shell.getPresenter().getContext();
        var params = new DevToolsTabDockParams(DevToolsHostType.SPLIT_SPACE,
                context.getSettings(), context.getHistoryManager());
        var presenter = new DevToolsTabDockPresenter<>(view, params);
        presenter.initialize();
        return view;
    }

    protected DevToolsWindowFxView<?> createDevToolsWindow() {
        var view = new DevToolsWindowFxView<>(getComponent());
        var context = getComponent().getPresenter().getContext();
        var params = new DevToolsWindowParams(context.getSettings().getAppearance(), context.getHistoryManager());
        var presenter = new DevToolsWindowPresenter<>(view, params);
        presenter.initialize();
        return view;
    }

    private WindowContainerFxView<?> resolveDialogContainer() {
        var shell = getComponent();
        if (shell.getComposer().getWorkspace() instanceof TabHostFxView<?> tabHost) {
            var tab = tabHost.getComposer().getSelectedTab();
            return (WindowContainerFxView<?>) tab;
        } else {
            return shell;
        }
    }

}
