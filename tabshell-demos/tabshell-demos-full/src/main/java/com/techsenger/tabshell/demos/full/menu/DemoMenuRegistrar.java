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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.core.CoreComponents;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.full.dialogs.DialogsDialogFxView;
import com.techsenger.tabshell.demos.full.dialogs.DialogsDialogPresenter;
import com.techsenger.tabshell.demos.full.dock.DockLayoutTabFxView;
import com.techsenger.tabshell.demos.full.dock.DockLayoutTabPresenter;
import com.techsenger.tabshell.demos.full.page.PagedTabFxView;
import com.techsenger.tabshell.demos.full.page.PagedTabHistory;
import com.techsenger.tabshell.demos.full.page.PagedTabPresenter;
import com.techsenger.tabshell.devtools.DevToolsTabDockFxView;
import com.techsenger.tabshell.devtools.DevToolsTabDockHistory;
import com.techsenger.tabshell.devtools.DevToolsTabDockPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogFxView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogPresenter;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.layout.dock.UtilityDockContainerFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.tabshell.terminal.TerminalTabFxView;
import com.techsenger.tabshell.terminal.TerminalTabPresenter;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.web.WebBrowserTabFxView;
import com.techsenger.tabshell.web.WebBrowserTabPresenter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Pavel Castornii
 */
public class DemoMenuRegistrar extends AbstractControlRegistrar  {

    public DemoMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerMenu();
        registerDefaultGroup();
//        registerTextEditorItem();
//        registerHexEditorItem();
        registerTerminalItem();
        registerDialogsItem();
        registerDockLayoutItem();
        registerDevToolsTabDockItem();
        registerWebBrowserItem();
        registerPagedTabItem();
    }

    protected void registerMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(DemoMenu.DEMO, "_Demo", 1000);
        };
        addRegistration(getRegistry().registerMenu(CoreComponents.SHELL, null, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(DemoMenu.DEFAULT, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponents.SHELL, DemoMenu.DEMO, f));
    }

//    protected void registerTextEditorItem() {
//        ControlFactory<NamedMenuItem> f = (v) -> {
//            var item = new NamedMenuItem(DemoMenuNames.TEXT_EDITOR, "_Text Editor",
//                    new FontIconView(TextIcons.EDITOR), 100);
//            item.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
//            item.setOnAction((e) -> {
//                var shell = (ShellView<?, ?>) v;
//                //editor
//                var stogages = FileStorages.getDefault(true);
//                var homeDir = GenericFile.getHome(stogages);
//                var file = GenericFile.getChild(homeDir, "Lorem Ipsum.txt", FileType.FILE);
//                var editorViewModel = new TextEditorTabViewModel(file, shell.getViewModel().getHistoryManager());
//                var editorView = new TextEditorTabView(editorViewModel);
//                var editorComponent = new TextEditorTabComponent(editorView, shell.getComponent());
//                editorComponent.initialize();
//                editorViewModel.setContent(Text.INSTANCE);
//                shell.getComponent().addTab(editorComponent);
//            });
//            return item;
//        };
//        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
//    }
//
//    protected void registerHexEditorItem() {
//        ControlFactory<NamedMenuItem> f = (v) -> {
//            var item = new NamedMenuItem(DemoMenuNames.HEX_EDITOR, "_Hex Editor",
//                    new FontIconView(HexIcons.EDITOR), 200);
//            item.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
//            item.setOnAction((e) -> {
//                var shell = (ShellView<?, ?>) v;
//                //file
//                Path currentPath = Paths.get(System.getProperty("user.dir"));
//                URI fileUri = currentPath.resolve("src" + File.separator + "main" + File.separator
//                        + "resources" + File.separator + "hex.png").toUri();
//                var storage = FileStorages.findByUri(FileStorages.getDefault(true), fileUri);
//                var file = new GenericFile.Builder()
//                        .storage(storage)
//                        .uri(fileUri)
//                        .type(FileType.FILE)
//                        .build();
//
//                //editor
//                var editorViewModel = new DemoHexEditorTabViewModel(file, shell.getViewModel().getHistoryManager());
//                var editorView = new DemoHexEditorTabView(editorViewModel);
//                var editorComponent = new DemoHexEditorTabComponent(editorView, shell.getComponent());
//                editorComponent.initialize();
//                shell.getComponent().addTab(editorComponent);
//            });
//            return item;
//        };
//        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
//    }
//
    protected void registerTerminalItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.TERMINAL, "_Terminal",
                    new FontIconView(TerminalIcons.TERMINAL), 300);
            item.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                //terminal
                var terminalV = new TerminalTabFxView<>(shell);
                var terminalP = new TerminalTabPresenter<>(terminalV, null, shell.getPresenter().getHistoryManager());
                terminalP.initialize();
                shell.getComposer().addTab(terminalV);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }

    protected void registerDialogsItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.DIALOGS, "Dialogs", 500);
            item.setOnAction((e) -> {
                var shellV = (ShellFxView<?>) v;
                var shellP = shellV.getPresenter();
                var dialogView = new DialogsDialogFxView();
                var dialogPresenter = new DialogsDialogPresenter(dialogView, shellP.getSettings().getAppearance(),
                        shellP.getHistoryManager());
                dialogPresenter.initialize();
                shellV.getComposer().addDialog(dialogView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }

    protected void registerDockLayoutItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.DOCK_LAYOUT, "Dock Layout", 600);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var dockTabView = new DockLayoutTabFxView(shell);
                var dockTabPresenter =
                        new DockLayoutTabPresenter(dockTabView, shell.getPresenter().getHistoryManager());
                dockTabPresenter.initialize();
                shell.getComposer().addTab(dockTabView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }

    protected void registerDevToolsTabDockItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.DEV_TOOLS, "DevTools", 700);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var currentTab = shell.getSelectedTab();
                if (currentTab != null && currentTab instanceof UtilityDockContainerFxView tab) {
                    var view = new DevToolsTabDockFxView<>(currentTab);
                    var hm = shell.getPresenter().getHistoryManager();
                    var presenter = new DevToolsTabDockPresenter<>(view, shell.getPresenter().getSettings(),
                            () -> hm.getOrCreateHistory(DevToolsTabDockHistory.class, DevToolsTabDockHistory::new));
                    presenter.initialize();
                    tab.getComposer().addUtilityDock(view);
                } else {
                    var alertView = new AlertDialogFxView<>(false);
                    var alertPresenter = new AlertDialogPresenter<>(alertView, OverlayScope.SHELL,
                            AlertDialogType.ERROR, "Tab doesn't support utility components");
                    alertPresenter.initialize();
                    shell.getComposer().addDialog(alertView);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }
//
    protected void registerWebBrowserItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.WEB_BROWSER, "Web Browser", 800);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var browserView = new WebBrowserTabFxView<>(shell);
                var browserPresenter = new WebBrowserTabPresenter<>(browserView, null);
                browserPresenter.initialize();
                shell.getComposer().addTab(browserView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }

    protected void registerPagedTabItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenu.PAGED_TAB, "Paged Tab", 900);
            item.setOnAction((e) -> {
                var shell = (ShellFxView<?>) v;
                var tabView = new PagedTabFxView(shell);
                var historyManager = shell.getPresenter().getHistoryManager();
                var tabPresenter = new PagedTabPresenter(tabView,
                        () -> historyManager.getOrCreateHistory(PagedTabHistory.class, PagedTabHistory::new));
                tabPresenter.initialize();
                shell.getComposer().addTab(tabView);
                tabView.requestFocus();
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponents.SHELL, DemoMenu.DEFAULT, f));
    }
}
