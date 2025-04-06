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

import com.techsenger.tabshell.core.TabShellKey;
import com.techsenger.tabshell.core.TabShellView;
import com.techsenger.tabshell.core.file.FileStorages;
import com.techsenger.tabshell.core.file.FileType;
import com.techsenger.tabshell.core.file.GenericFile;
import com.techsenger.tabshell.core.menu.FileMenuKeys;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.registrars.FileMenuRegistrar;
import com.techsenger.tabshell.terminal.TerminalTabView;
import com.techsenger.tabshell.terminal.TerminalTabViewModel;
import com.techsenger.tabshell.terminal.style.TerminalIcons;
import com.techsenger.tabshell.text.style.TextIcons;
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
        registerEditorItem(); //file actions group
        registerTerminalItem(); //file actions group
        registerThemeItem(); //file actions group
    }

    protected void registerEditorItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.EDITOR, "E_ditor", new FontIconView(TextIcons.EDITOR));
            item.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var tabShell = (TabShellView<?>) v;
                //editor
                var stogages = FileStorages.getDefault(true);
                var homeDir = GenericFile.getHome(stogages);
                var file = GenericFile.getChild(homeDir, "Lorem Ipsum.txt", FileType.FILE);
                var editorViewModel = new EditorTabViewModel(tabShell.getViewModel(), file);
                var editorView = new EditorTabView(tabShell, editorViewModel);
                editorView.initialize();
                editorViewModel.setContent(Text.INSTANCE);
                tabShell.openTab(editorView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f, 100));
    }

    protected void registerTerminalItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.TERMINAL, "_Terminal",
                    new FontIconView(TerminalIcons.TERMINAL));
            item.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var tabShell = (TabShellView<?>) v;
                //terminal
                var terminalViewModel = new TerminalTabViewModel(tabShell.getViewModel(), null);
                var terminalView = new TerminalTabView(tabShell, terminalViewModel);
                terminalView.initialize();
                tabShell.openTab(terminalView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f, 200));
    }

    protected void registerThemeItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.THEME, "_Theme");
            item.setOnAction((e) -> {
                var tabShell = (TabShellView<?>) v;
                var appearance = tabShell.getViewModel().getSettings().getAppearance();
                var dialogViewModel = new ThemeDialogViewModel(appearance.getTheme());
                dialogViewModel.okActionProperty().set(() -> {
                    dialogViewModel.close();
                    appearance.themeProperty().set(dialogViewModel.themeProperty().get());
                });
                var dialogView = new ThemeDialogView(dialogViewModel);
                dialogView.initialize();
                tabShell.getDialogManager().openDialog(dialogView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f, 300));
    }

}
