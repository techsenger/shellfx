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

import com.techsenger.tabshell.core.CoreComponents;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.tab.TabContainerFxView;
import com.techsenger.tabshell.demo.browser.BrowserMainTabFxView;
import com.techsenger.tabshell.demo.browser.BrowserMainTabPresenter;
import com.techsenger.tabshell.demo.dialogs.DialogsDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DialogsDialogPresenter;
import com.techsenger.tabshell.demo.ide.IdeMainTabFxView;
import com.techsenger.tabshell.demo.ide.IdeMainTabPresenter;
import com.techsenger.tabshell.demo.page.PageTabFxView;
import com.techsenger.tabshell.demo.page.PageTabHistory;
import com.techsenger.tabshell.demo.page.PageTabPresenter;
import com.techsenger.tabshell.demo.theme.ThemeDialogFxView;
import com.techsenger.tabshell.demo.theme.ThemeDialogPresenter;
import com.techsenger.tabshell.devtools.DevToolsTabDockFxView;
import com.techsenger.tabshell.devtools.DevToolsTabDockHistory;
import com.techsenger.tabshell.devtools.DevToolsTabDockPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.layout.dockhost.DockHostFxView;
import com.techsenger.tabshell.layout.dockhost.UtilityDockContainerFxView;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import javafx.geometry.Side;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Pavel Castornii
 */
public class FileMenuRegistrar extends AbstractControlRegistrar {

    private final ShellFxView<?> shell;

    public FileMenuRegistrar(ControlRegistry registry, ShellFxView<?> shell) {
        super(registry);
        this.shell = shell;
    }

    @Override
    public void register() {
        registerMenu();
        registerGroups();
        registerMainTabItem();
        registerPagedTabItem();
        registerDialogsItem();
        registerDevToolsTabDockItem();
        registerThemeItem();
        registerExitItem();
    }

    protected void registerMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(FileMenu.NAME, "_File", 0);
        };
        addRegistration(getRegistry().registerMenu(CoreComponents.SHELL, null, f));
    }

    protected void registerGroups() {
        ControlFactory<NamedMenuGroup> f = (v) -> new NamedMenuGroup(FileMenu.DEMO, 100);
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, FileMenu.NAME, f));
        f = (v) -> new NamedMenuGroup(FileMenu.SETTINGS, 200);
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, FileMenu.NAME, f));
        f = (v) -> new NamedMenuGroup(FileMenu.LAST, 300);
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, FileMenu.NAME, f));
    }

    protected void registerMainTabItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.MAIN_TAB, "Main Tab", 100);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                AbstractTabFxView<?> tabV;
                if (shell.getWorkspace() instanceof TabHostFxView<?>) {
                    tabV = new BrowserMainTabFxView(shell);
                    var tabP = new BrowserMainTabPresenter(tabV, shell.getPresenter().getContext().getHistoryManager());
                } else {
                    tabV = new IdeMainTabFxView<>(shell);
                    var tabP = new IdeMainTabPresenter<>((IdeMainTabFxView<?>) tabV);
                }
                tabV.getPresenter().initialize();
                resolveMainTabContainer().getComposer().addTab(tabV);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.DEMO, f));
    }

    protected void registerPagedTabItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.PAGED_TAB, "Paged Tab", 200);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var tabView = new PageTabFxView(shell);
                var historyManager = shell.getPresenter().getContext().getHistoryManager();
                var tabPresenter = new PageTabPresenter(tabView,
                        () -> historyManager.getOrCreateHistory(PageTabHistory.class, PageTabHistory::new));
                tabPresenter.initialize();
                resolveMainTabContainer().getComposer().addTab(tabView);
                tabView.requestFocus();
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.DEMO, f));
    }

    protected void registerDialogsItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.DIALOGS, "Dialogs", 300);
            item.setOnAction((e) -> {
                var shellV = (ShellFxView<?>) v;
                var shellP = shellV.getPresenter();
                var dialogView = new DialogsDialogFxView();
                var dialogPresenter = new DialogsDialogPresenter(dialogView,
                        shellP.getContext().getSettings().getAppearance(),
                        shellP.getContext().getHistoryManager());
                dialogPresenter.initialize();
                shellV.getComposer().addDialog(dialogView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.DEMO, f));
    }

    protected void registerDevToolsTabDockItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.DEV_TOOLS, "DevTools", 400);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                if (shell.getWorkspace() instanceof TabHostFxView<?> tabHost) {
                    var tab = tabHost.getSelectedTab();
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
                } else if (shell.getWorkspace() instanceof DockHostFxView<?> dockHost) {
                    var devTools = createDevTools();
                    devTools.getPresenter().initialize();
                    devTools.getPresenter().setDraggable(true);
                    dockHost.getComposer().addTabDock(devTools, Side.BOTTOM, 250);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.DEMO, f));
    }

    protected DevToolsTabDockFxView<?> createDevTools() {
        var view = new DevToolsTabDockFxView<>(shell, resolveDialogContainer());
        var hm = shell.getPresenter().getContext().getHistoryManager();
        var presenter = new DevToolsTabDockPresenter<>(view, shell.getPresenter().getContext().getSettings(),
                () -> hm.getOrCreateHistory(DevToolsTabDockHistory.class, DevToolsTabDockHistory::new));
        return view;
    }

    protected void registerThemeItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.THEME, "_Theme", 100);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var appearance = shell.getPresenter().getContext().getSettings().getAppearance();
                var view = new ThemeDialogFxView();
                var presenter = new ThemeDialogPresenter(view, appearance);
                presenter.initialize();
                shell.getComposer().addDialog(view);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.SETTINGS, f));
    }

    protected void registerExitItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(FileMenu.EXIT, false, false, false, "E_xit", 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> ((ShellFxView<?>) v).getPresenter().close());
            return item;

        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, FileMenu.LAST, f));
    }

    private TabContainerFxView<?> resolveMainTabContainer() {
        if (shell.getWorkspace() instanceof TabHostFxView<?> tabHost) {
            return tabHost;
        } else if (shell.getWorkspace() instanceof DockHostFxView<?> dockHost) {
            return (TabContainerFxView<?>) dockHost.getMain();
        }
        return null;
    }

    private DialogContainerFxView<?> resolveDialogContainer() {
        if (shell.getWorkspace() instanceof TabHostFxView<?> tabHost) {
            var tab = tabHost.getSelectedTab();
            return tab;
        } else {
            return shell;
        }
    }
}
