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

package com.techsenger.tabshell.kit.registrar;

import com.techsenger.tabshell.kit.core.menu.EditMenuKeys;
import com.techsenger.tabshell.core.TabShellKey;
import com.techsenger.tabshell.core.TabShellView;
import com.techsenger.tabshell.core.registry.AbstractControlRegistrar;
import com.techsenger.tabshell.core.registry.ControlFactory;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.kit.core.style.CoreIcons;
import com.techsenger.tabshell.kit.text.editor.AbstractEditorTabView;
import com.techsenger.tabshell.kit.text.viewer.AbstractViewerTabView;
import com.techsenger.tabshell.kit.text.viewer.AbstractViewerTabViewModel;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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
        ControlFactory<KeyedMenu> f = (v) -> {
            return new KeyedMenu(EditMenuKeys.EDIT, true, false, false, "_Edit");
        };
        addRegistration(getRegistry().registerMenu(TabShellKey.INSTANCE, null, f, 200));
    }

    protected void registerHistoryGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(EditMenuKeys.HISTORY);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, EditMenuKeys.EDIT, f, 100));
    }

    protected void registerClipboardGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(EditMenuKeys.CLIPBOARD);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, EditMenuKeys.EDIT, f, 200));
    }

    protected void registerFindReplaceGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(EditMenuKeys.FIND_REPLACE);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, EditMenuKeys.EDIT, f, 300));
    }

    protected void registerDefaultGroup() {
        ControlFactory<KeyedMenuGroup> f = (v) -> {
            return new KeyedMenuGroup(EditMenuKeys.DEFAULT);
        };
        addRegistration(getRegistry().registerMenuGroup(TabShellKey.INSTANCE, EditMenuKeys.EDIT, f, 10000));
    }

    protected void registerUndoItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.UNDO, false, true, false, "_Undo",
                    new FontIconView(CoreIcons.UNDO));
            item.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).undo();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.HISTORY, f, 100));
    }

    protected void registerRedoItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.REDO, false, true, false, "_Redo",
                    new FontIconView(CoreIcons.REDO));
            item.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).redo();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.HISTORY, f, 200));
    }

    protected void registerCutItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.CUT, false, true, false, "Cu_t",
                    new FontIconView(CoreIcons.CUT));
            item.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).cut();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.CLIPBOARD, f, 100));
    }

    protected void registerCopyItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.COPY, false, true, false, "Cop_y",
                    new FontIconView(CoreIcons.COPY));
            item.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    ((AbstractViewerTabView) tab).copy();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.CLIPBOARD, f, 200));
    }

    protected void registerPasteItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.PASTE, false, true, false, "_Paste",
                    new FontIconView(CoreIcons.PASTE));
            item.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractEditorTabView) {
                    ((AbstractEditorTabView) tab).paste();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.CLIPBOARD, f, 300));
    }

    protected void registerFindItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.FIND, false, true, false, "_Find",
                    new FontIconView(CoreIcons.FIND));
            item.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    ((AbstractViewerTabViewModel) textView.getViewModel()).addFindPane(false);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.FIND_REPLACE, f, 100));
    }

    protected void registerReplaceItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.REPLACE, false, true, false, "R_eplace",
                    new FontIconView(CoreIcons.REPLACE));
            item.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    var textView = (AbstractViewerTabView) tab;
                    ((AbstractViewerTabViewModel) textView.getViewModel()).addFindPane(true);
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.FIND_REPLACE, f, 200));
    }

    protected void registerFindSelectionItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.FIND_SELECTION, false, true, false, "Find _Selection", null);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
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
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.FIND_REPLACE, f, 300));
    }

    protected void registerFindNextItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.FIND_NEXT, false, true, false, "Find Ne_xt", null);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
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
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.FIND_REPLACE, f, 400));
    }

    protected void registerFindPreviousItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.FIND_PREVIOUS, false, true, false, "Find Pre_vious", null);
            item.setAccelerator(new KeyCodeCombination(KeyCode.F3, KeyCombination.SHIFT_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
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
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.FIND_REPLACE, f, 500));
    }

    protected void registerGoToLineItem() {
        ControlFactory<KeyedMenuItem> f = (v) -> {
            var item = new KeyedMenuItem(EditMenuKeys.GO_TO_LINE, false, true, false, "Go to _Line", null);
            item.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
            item.setOnAction(e -> {
                var tab = ((TabShellView<?>) v).getSelectedTab();
                if (tab != null && tab instanceof AbstractViewerTabView) {
                    ((AbstractViewerTabView<AbstractViewerTabViewModel>) tab).getViewModel().openGoToLineDialog();
                }
            });
            return item;
        };
        addRegistration(getRegistry().registerMenuItem(TabShellKey.INSTANCE, EditMenuKeys.DEFAULT, f, 100));
    }
}
