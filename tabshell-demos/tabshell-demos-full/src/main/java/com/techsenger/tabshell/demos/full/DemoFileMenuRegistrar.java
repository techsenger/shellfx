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
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.registrars.FileMenuRegistrar;
import com.techsenger.tabshell.storage.FileStorages;
import com.techsenger.tabshell.storage.FileType;
import com.techsenger.tabshell.storage.GenericFile;
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
            var item = new KeyedMenuItem(DemoFileMenuKeys.EDITOR, "E_ditor", new FontIconView(TextIcons.EDITOR), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
            item.setOnAction((e) -> {
                var shell = (ShellView<?>) v;
                //editor
                var stogages = FileStorages.getDefault(true);
                var homeDir = GenericFile.getHome(stogages);
                var file = GenericFile.getChild(homeDir, "Lorem Ipsum.txt", FileType.FILE);
                var editorViewModel = new EditorTabViewModel(shell.getViewModel(), file);
                var editorView = new EditorTabView(shell, editorViewModel);
                editorView.initialize();
                editorViewModel.setContent(Text.INSTANCE);
                shell.openTab(editorView);
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(ShellKey.INSTANCE, FileMenuKeys.FILE_ACTIONS, f));
    }

    protected void registerTerminalItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(DemoFileMenuKeys.TERMINAL, "_Terminal",
                    new FontIconView(TerminalIcons.TERMINAL), 200);
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
            var item = new KeyedMenuItem(DemoFileMenuKeys.THEME, "_Theme", 300);
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

}
