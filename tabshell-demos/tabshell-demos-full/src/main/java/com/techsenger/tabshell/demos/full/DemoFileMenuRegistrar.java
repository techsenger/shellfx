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

package com.techsenger.tabshell.demos.full;

import com.techsenger.tabshell.core.ShellKey;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.menu.FileMenuKeys;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.demos.full.dock.DockTabView;
import com.techsenger.tabshell.demos.full.dock.DockTabViewModel;
import com.techsenger.tabshell.demos.full.dock.DockableTabView;
import com.techsenger.tabshell.demos.full.dock.DockableTabViewModel;
import com.techsenger.tabshell.demos.full.dock.TextViewerView;
import com.techsenger.tabshell.demos.full.dock.TextViewerViewModel;
import com.techsenger.tabshell.hex.style.HexIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.registrars.FileMenuRegistrar;
import com.techsenger.tabshell.storage.FileStorages;
import com.techsenger.tabshell.storage.FileType;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.tabs.docktab.SpaceReceiver;
import com.techsenger.tabshell.tabs.docktab.TabDockView;
import com.techsenger.tabshell.terminal.TerminalTabView;
import com.techsenger.tabshell.terminal.TerminalTabViewModel;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.text.style.TextIcons;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Pavel Castornii
 */
public class DemoFileMenuRegistrar extends FileMenuRegistrar {

    public DemoFileMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        super.register();
        registerTextEditorItem(); //file actions group
        registerHexEditorItem(); //file actions group
        registerTerminalItem(); //file actions group
        registerThemeItem(); //file actions group
        registerDockTabItem(); //file actions group
    }

    protected void registerTextEditorItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.TEXT_EDITOR, "_Text Editor",
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
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerHexEditorItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.HEX_EDITOR, "_Hex Editor",
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
                var editorViewModel = new HexEditorTabViewModel(shell.getViewModel(), file);
                var editorView = new HexEditorTabView(shell, editorViewModel);
                editorView.initialize();
                shell.openTab(editorView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerTerminalItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.TERMINAL, "_Terminal",
                    new FontIconView(TerminalIcons.TERMINAL), 300);
            item.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                //terminal
                var terminalViewModel = new TerminalTabViewModel(shell.getViewModel(), null);
                var terminalView = new TerminalTabView(shell, terminalViewModel);
                terminalView.initialize();
                shell.openTab(terminalView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerThemeItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.THEME, "_Theme", 400);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var appearance = shell.getViewModel().getSettings().getAppearance();
                var dialogViewModel = new ThemeDialogViewModel(appearance.getTheme());
                dialogViewModel.okActionProperty().set(() -> {
                    appearance.themeProperty().set(dialogViewModel.themeProperty().get());
                    dialogViewModel.requestClose();
                });
                var dialogView = new ThemeDialogView(dialogViewModel);
                dialogView.initialize();
                shell.getDialogManager().openDialog(dialogView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerDockTabItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.DOCK_TAB, "Dock Tab", 1000);
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                var dockTabViewModel = new DockTabViewModel(shell.getViewModel());
                var dockTabView = new DockTabView(shell, dockTabViewModel);
                dockTabView.initialize();
                shell.openTab(dockTabView);

                var workspaceView = dockTabView.createWorkspace();

                dockTabView.setRoot(workspaceView);

                var mainViewModel = new TextViewerViewModel();
                var mainView = new TextViewerView(mainViewModel);
                mainView.initialize();
                workspaceView.getChildren().add(mainView);
                var tabDockView = dockTabView.createTabDock();
                fillTabs(tabDockView);
                tabDockView.getViewModel().setSpaceReceiver(SpaceReceiver.NEXT);
                workspaceView.getChildren().add(tabDockView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    private void fillTabs(TabDockView<?> tabDock) {
        for (var i = 0; i < 10; i++) {
            var tabViewModel = new DockableTabViewModel(i);
            var tabView = new DockableTabView(tabViewModel);
            tabView.initialize();
            tabDock.openTab(tabView);
        }
    }

}
