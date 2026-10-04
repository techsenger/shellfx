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

import com.techsenger.annotations.Nullable;
import java.util.function.Supplier;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;

/**
 * Wires runtime behavior onto a {@link ContextMenu} built via {@code ManagedControlBuilder#buildContextMenu} -
 * action dispatch for its items (including nested menus) and, on showing, the same visibility resolution
 * {@link MenuBarManager} uses for the persistent menu bar. The menu structure is never changed after it is
 * built.
 *
 * <p>Unlike {@link MenuBarManager}, which manages one {@code MenuBar} kept alive for the shell's lifetime and
 * must disambiguate a mouse click from a keyboard accelerator, a {@code ContextMenuManager} has no competing
 * global accelerators to disambiguate from, so every action fires unconditionally.
 *
 * @author Pavel Castornii
 */
public class ContextMenuManager {

    private final Supplier<@Nullable InputEvent> inputEvent;

    /**
     * Creates a manager of {@code contextMenu} and wires its behavior right away. The context menu must be fully
     * built; the manager is discarded together with it.
     *
     * @param contextMenu the context menu to manage.
     * @param inputEvent supplies the input event the window of the menu is dispatching right now, which tells an
     *     accelerator from a mouse click.
     */
    public ContextMenuManager(ContextMenu contextMenu, Supplier<@Nullable InputEvent> inputEvent) {
        this.inputEvent = inputEvent;
        initializeItems(contextMenu.getItems());
        contextMenu.setOnShowing(e -> {
            var handler = ContextMenuHandler.getHandler(contextMenu);
            if (handler != null) {
                handler.onShowing();
                handler.onUpdate();
                if (!ContextMenuHandler.isVisible(contextMenu)) {
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

    /**
     * Recursively wires action dispatch for {@code items}, including nested menus.
     *
     * @param items the items to wire, in any order.
     */
    private void initializeItems(Iterable<? extends MenuItem> items) {
        for (var item : items) {
            if (item instanceof Menu menu) {
                initializeItems(menu.getItems());
            } else {
                var handler = MenuItemHandler.getHandler(item);
                if (handler != null) {
                    item.setOnAction(e -> MenuItemDispatcher.dispatch(item, handler, inputEvent));
                }
            }
        }
    }
}
