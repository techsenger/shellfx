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

package com.techsenger.tabshell.registrars;

import com.techsenger.tabshell.core.CoreComponentNames;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.shared.menu.EditMenuNames;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.NamedMenu;
import com.techsenger.tabshell.material.menu.NamedMenuGroup;
import com.techsenger.tabshell.material.menu.NamedMenuItem;
import com.techsenger.tabshell.text.editor.AbstractEditorTabView;
import com.techsenger.tabshell.text.viewer.AbstractViewerTabView;
import com.techsenger.tabshell.text.viewer.AbstractViewerTabViewModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import com.techsenger.tabshell.shared.style.SharedIcons;

/**
 *
 * @author Pavel Castornii
 */
public class EditMenuRegistrar extends AbstractControlRegistrar {

    public EditMenuRegistrar(ControlRegistry registry) {
        super(registry);
    }

    @Override
    public void register() {
        registerMenu();
        registerHistoryGroup();
        registerClipboardGroup();
        registerFindReplaceGroup();
        registerDefaultGroup();
        registerUndoItem();
        registerRedoItem();
        registerCutItem();
        registerCopyItem();
        registerPasteItem();
        registerFindItem();
        registerReplaceItem();
        registerFindSelectionItem();
        registerFindNextItem();
        registerFindPreviousItem();
        registerGoToLineItem();
    }

    protected void registerMenu() {
        ControlFactory<NamedMenu> f = (v) -> {
            return new NamedMenu(EditMenuNames.EDIT, true, false, false, "_Edit", 200);
        };
        addRegistration(getRegistry().registerMenu(CoreComponentNames.SHELL, null, f));
    }

    protected void registerHistoryGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(EditMenuNames.HISTORY, 100);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, EditMenuNames.EDIT, f));
    }

    protected void registerClipboardGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(EditMenuNames.CLIPBOARD, 200);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, EditMenuNames.EDIT, f));
    }

    protected void registerFindReplaceGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(EditMenuNames.FIND_REPLACE, 300);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, EditMenuNames.EDIT, f));
    }

    protected void registerDefaultGroup() {
        ControlFactory<NamedMenuGroup> f = (v) -> {
            return new NamedMenuGroup(EditMenuNames.DEFAULT, 10000);
        };
        addRegistration(getRegistry().registerMenuGroup(CoreComponentNames.SHELL, EditMenuNames.EDIT, f));
    }

    protected void registerUndoItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.UNDO, false, true, false, "_Undo",
                    new FontIconView(SharedIcons.UNDO), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).undo();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.HISTORY, f));
    }

    protected void registerRedoItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.REDO, false, true, false, "_Redo",
                    new FontIconView(SharedIcons.REDO), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).redo();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.HISTORY, f));
    }

    protected void registerCutItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.CUT, false, true, false, "Cu_t",
                    new FontIconView(SharedIcons.CUT), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).cut();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.CLIPBOARD, f));
    }

    protected void registerCopyItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.COPY, false, true, false, "Cop_y",
                    new FontIconView(SharedIcons.COPY), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    ((AbstractViewerTabView) tab).copy();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.CLIPBOARD, f));
    }

    protected void registerPasteItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.PASTE, false, true, false, "_Paste",
                    new FontIconView(SharedIcons.PASTE), 300);
            item.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).paste();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.CLIPBOARD, f));
    }

    protected void registerFindItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.FIND, false, true, false, "_Find",
                    new FontIconView(SharedIcons.FIND), 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    ((AbstractViewerTabViewModel) textView.getViewModel()).addFindPane(false);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.FIND_REPLACE, f));
    }

    protected void registerReplaceItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.REPLACE, false, true, false, "R_eplace",
                    new FontIconView(SharedIcons.REPLACE), 200);
            item.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    ((AbstractViewerTabViewModel) textView.getViewModel()).addFindPane(true);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.FIND_REPLACE, f));
    }

    protected void registerFindSelectionItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item =
                    new NamedMenuItem(EditMenuNames.FIND_SELECTION, false, true, false, "Find _Selection", null, 300);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    var find = textView.getFind();
                    boolean shouldReset = false;
                    if (find != null) {
                        shouldReset = true;
                    }
                    ((AbstractViewerTabViewModel) textView.getViewModel()).addFindPane(false);
                    //on reset selection is removed;
                    //that's why it's after setting selection to find text
                    textView.getFind().setSelectionToFindText();
                    if (shouldReset) {
                        find.getViewModel().resetMatches();
                    }
                    find.getViewModel().findNext();
                    find.selectNextRange(true);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.FIND_REPLACE, f));
    }

    protected void registerFindNextItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.FIND_NEXT, false, true, false, "Find Ne_xt", null, 400);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    var find = textView.getFind();
                    if (find != null) {
                        find.getViewModel().findNext();
                        find.selectNextRange(true);
                    }
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.FIND_REPLACE, f));
    }

    protected void registerFindPreviousItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.FIND_PREVIOUS, false, true, false, "Find Pre_vious", null, 500);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3, KeyCombination.SHIFT_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var find = ((AbstractViewerTabView<AbstractViewerTabViewModel>) tab).getFind();
                    if (find != null) {
                        find.getViewModel().findPrevious();
                        find.selectPreviousRange(true);
                    }
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.FIND_REPLACE, f));
    }

    protected void registerGoToLineItem() {
        ControlFactory<NamedMenuItem> f = (v) -> {
            var item = new NamedMenuItem(EditMenuNames.GO_TO_LINE, false, true, false, "Go to _Line", null, 100);
            item.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((ShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    ((AbstractViewerTabView<AbstractViewerTabViewModel>) tab).getViewModel().openGoToLineDialog();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(CoreComponentNames.SHELL, EditMenuNames.DEFAULT, f));
    }
}
