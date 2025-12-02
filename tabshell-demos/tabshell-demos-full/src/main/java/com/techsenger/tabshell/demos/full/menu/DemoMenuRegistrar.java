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

package com.techsenger.tabshell.demos.full.menu;

import com.techsenger.tabshell.core.CoreComponentNames;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.full.dialogs.DialogsDialogView;
import com.techsenger.tabshell.demos.full.dialogs.DialogsDialogViewModel;
import com.techsenger.tabshell.demos.full.dock.DockLayoutTabView;
import com.techsenger.tabshell.demos.full.dock.DockLayoutTabViewModel;
import com.techsenger.tabshell.demos.full.hex.DemoHexEditorTabView;
import com.techsenger.tabshell.demos.full.hex.DemoHexEditorTabViewModel;
import com.techsenger.tabshell.demos.full.text.Text;
import com.techsenger.tabshell.demos.full.text.TextEditorTabView;
import com.techsenger.tabshell.demos.full.text.TextEditorTabViewModel;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.alert.AlertDialogView;
import com.techsenger.tabshell.dialogs.alert.AlertDialogViewModel;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.jfx.JfxTabDockView;
import com.techsenger.tabshell.jfx.JfxTabDockViewModel;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.tabshell.storage.FileStorages;
import com.techsenger.tabshell.storage.FileType;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.terminal.TerminalTabView;
import com.techsenger.tabshell.terminal.TerminalTabViewModel;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.text.style.TextIcons;
import com.techsenger.tabshell.web.WebBrowserTabView;
import com.techsenger.tabshell.web.WebBrowserTabViewModel;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.geometry.Side;
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
        registerTextEditorItem();
        registerHexEditorItem();
        registerTerminalItem();
        registerDialogsItem();
        registerDockLayoutItem();
        registerJfxTabDockItem();
        registerWebBrowserItem();
    }

    protected void registerMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(DemoMenuNames.DEMO, "_Demo", 1000);
        };
        addRegistration(getRegistry().registerMenu(CoreComponentNames.SHELL, null, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(DemoMenuNames.DEFAULT, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, DemoMenuNames.DEMO, f));
    }

    protected void registerTextEditorItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.TEXT_EDITOR, "_Text Editor",
                    new FontIconView(TextIcons.EDITOR), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                //editor
                var stogages = FileStorages.getDefault(true);
                var homeDir = GenericFile.getHome(stogages);
                var file = GenericFile.getChild(homeDir, "Lorem Ipsum.txt", FileType.FILE);
                var editorViewModel = new TextEditorTabViewModel(shell.getViewModel(), file);
                var editorView = new TextEditorTabView(shell, editorViewModel);
                editorView.initialize();
                editorViewModel.setContent(Text.INSTANCE);
                shell.openTab(editorView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerHexEditorItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.HEX_EDITOR, "_Hex Editor",
                    new FontIconView(HexIcons.EDITOR), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                //file
                Path currentPath = Paths.get(System.getProperty("user.dir"));
                URI fileUri = currentPath.resolve("src" + File.separator + "main" + File.separator
                        + "resources" + File.separator + "hex.png").toUri();
                var storage = FileStorages.findByUri(FileStorages.getDefault(true), fileUri);
                var file = new GenericFile.Builder()
                        .storage(storage)
                        .uri(fileUri)
                        .type(FileType.FILE)
                        .build();

                //editor
                var editorViewModel = new DemoHexEditorTabViewModel(shell.getViewModel(), file);
                var editorView = new DemoHexEditorTabView(shell, editorViewModel);
                editorView.initialize();
                shell.openTab(editorView);
                System.gc();
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerTerminalItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.TERMINAL, "_Terminal",
                    new FontIconView(TerminalIcons.TERMINAL), 300);
            item.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                //terminal
                var terminalViewModel = new TerminalTabViewModel(shell.getViewModel(), null);
                var terminalView = new TerminalTabView<>(shell, terminalViewModel);
                terminalView.initialize();
                shell.openTab(terminalView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerDialogsItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.DIALOGS, "Dialogs", 500);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var dialogViewModel = new DialogsDialogViewModel(shell.getViewModel());
                var dialogView = new DialogsDialogView(shell, dialogViewModel);
                dialogView.initialize();
                shell.getDialogManager().openDialog(dialogView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerDockLayoutItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.DOCK_LAYOUT, "Dock Layout", 600);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var dockTabViewModel = new DockLayoutTabViewModel(shell.getViewModel());
                var dockTabView = new DockLayoutTabView(shell, dockTabViewModel);
                dockTabView.initialize();
                shell.openTab(dockTabView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerJfxTabDockItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.JFX_DOCK, "JFX Tools", 700);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var currentTab = shell.getSelectedTab();
                if (currentTab != null && currentTab instanceof DockLayoutTabView tab) {
                    var inspectorVM = new JfxTabDockViewModel();
                    var inspectorV = new JfxTabDockView<>(shell, tab.getLayout(), inspectorVM);
                    inspectorV.initialize();
                    tab.getLayout().addTabDock(inspectorV, Side.BOTTOM, 300);
                } else {
                    var alertVM = new AlertDialogViewModel(DialogScope.SHELL, AlertDialogType.ERROR,
                            "Currently, use it only in the dock layout");
                    var alertV  = new AlertDialogView<>(alertVM);
                    alertV.initialize();
                    shell.getDialogManager().openDialog(alertV);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }

    protected void registerWebBrowserItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(DemoMenuNames.WEB_BROWSER, "Web Browser", 800);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var browserViewModel = new WebBrowserTabViewModel(shell.getViewModel());
                var browserView = new WebBrowserTabView<>(shell, browserViewModel);
                browserView.initialize();
                shell.openTab(browserView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, DemoMenuNames.DEFAULT, f));
    }
}
