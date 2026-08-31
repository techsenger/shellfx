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

package com.techsenger.shellfx.material.menu;

import javafx.collections.ListChangeListener;
import javafx.scene.control.MenuItem;

/**
 * Wires runtime behavior onto a {@link ManagedContextMenu} built via {@code MenuBuilder#buildContextMenu} -
 * action dispatch for its items (including nested menus, and items added/removed after {@link #manage()}
 * runs - the same registry-driven activation/deactivation that can change an already-built {@link ManagedMenu}
 * tree can just as well change one already handed to a {@code ContextMenuManager}), and, on showing, the same
 * visibility resolution {@link MenuBarManager} uses for the persistent menu bar.
 *
 * <p>Unlike {@link MenuBarManager}, which manages one {@code MenuBar} kept alive for the shell's lifetime and
 * must disambiguate a mouse click from a keyboard accelerator, a {@code ContextMenuManager} has no competing
 * global accelerators to disambiguate from, so every action fires unconditionally.
 *
 * @author Pavel Castornii
 */
public class ContextMenuManager {

    private final ManagedContextMenu contextMenu;

    private final ListChangeListener<MenuItem> menuItemsListener =
            (ListChangeListener.Change<? extends MenuItem> c) -> processMenuListChange(c);

    public ContextMenuManager(ManagedContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public void manage() {
        contextMenu.getItems().addListener(menuItemsListener);
        initializeItems(contextMenu.getItems());
        contextMenu.setOnShowing(e -> {
            var handler = ContextMenuHandler.getHandler(contextMenu);
            if (handler != null) {
                handler.onShowing();
                handler.onUpdate();
                if (!contextMenu.isVisible()) {
                    e.consume();
                    return;
                }
            }
            if (!MenuVisibility.resolveItems(contextMenu.getItems())) {
                e.consume();
                return;
            }
            MenuVisibility.collapseSeparators(contextMenu.getItems());
        });
        contextMenu.setOnHiding(e -> {
            var handler = ContextMenuHandler.getHandler(contextMenu);
            if (handler != null) {
                handler.onHiding();
            }
            MenuVisibility.fireHiding(contextMenu.getItems());
        });
    }

    private <T extends MenuItem> void processMenuListChange(ListChangeListener.Change<T> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                initializeItems(c.getAddedSubList());
            } else if (c.wasRemoved()) {
                deinitializeItems(c.getRemoved());
            }
        }
    }

    /**
     * Recursively wires action dispatch for {@code items}, including nested menus.
     *
     * @param items the items to wire, in any order.
     */
    private void initializeItems(Iterable<? extends MenuItem> items) {
        for (var item : items) {
            if (item instanceof ManagedMenu managedMenu) {
                initializeMenu(managedMenu);
            } else if (item instanceof ManagedItem) {
                var managedItem = (MenuItem & ManagedItem) item;
                var handler = MenuItemHandler.getHandler(managedItem);
                if (handler != null) {
                    item.setOnAction(e -> handler.onAction());
                }
            }
        }
    }

    private void initializeMenu(ManagedMenu managedMenu) {
        managedMenu.getItems().addListener(menuItemsListener);
        initializeItems(managedMenu.getItems());
    }

    /**
     * Recursively unwires action dispatch for {@code items}, including nested menus - called for items removed
     * from an already-{@link #manage()}d menu, so a stale handler never keeps firing after removal.
     *
     * @param items the items to unwire, in any order.
     */
    private void deinitializeItems(Iterable<? extends MenuItem> items) {
        for (var item : items) {
            if (item instanceof ManagedMenu managedMenu) {
                deinitializeMenu(managedMenu);
            } else if (item instanceof MenuItem mi) {
                mi.setOnAction(null);
            }
        }
    }

    private void deinitializeMenu(ManagedMenu managedMenu) {
        managedMenu.getItems().removeListener(menuItemsListener);
        managedMenu.setOnAction(null);
        deinitializeItems(managedMenu.getItems());
    }
}
