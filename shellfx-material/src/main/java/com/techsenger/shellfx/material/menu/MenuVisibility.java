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

import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Visibility bookkeeping shared by {@link MenuBarManager} (the persistent menu bar) and {@link ContextMenuManager}
 * (one-off popups): resolving whether a registry-composed {@code ManagedMenu} should show at all, and
 * collapsing separators around sections that ended up empty.
 *
 * @author Pavel Castornii
 */
public final class MenuVisibility {

    /**
     * Resolves whether {@code menu} should be visible. If it has its own {@link MenuHandler}, that handler's
     * decision (via {@link MenuHandler#onUpdate()}) is authoritative and {@code menu}'s items are left alone;
     * otherwise {@code menu} cannot know its own contributors (they come from independent, mutually-unaware
     * plugin registrations), so its visibility - and that of its own items' separators - is derived from
     * whether at least one item ends up visible, recursively.
     *
     * @param menu the menu to resolve.
     * @return whether {@code menu} ended up visible.
     */
    public static boolean resolve(ManagedMenu menu) {
        var handler = MenuHandler.getHandler(menu);
        if (handler != null) {
            handler.onShowing();
            handler.onUpdate();
            return menu.isVisible();
        }
        var anyVisible = resolveItems(menu.getItems());
        collapseSeparators(menu.getItems());
        menu.setVisible(anyVisible);
        return anyVisible;
    }

    /**
     * Resolves every item in {@code items} (see {@link #resolve(ManagedMenu)} for nested menus), without
     * deciding anything about a containing menu - used both by {@link #resolve(ManagedMenu)} for a menu's own
     * items and directly for a {@code ContextMenu}'s top-level items, which have no containing {@code Managed
     * Menu} of their own.
     *
     * @param items the items to resolve, in display order.
     * @return whether at least one item ended up visible.
     */
    public static boolean resolveItems(Iterable<MenuItem> items) {
        var anyVisible = false;
        for (var item : items) {
            if (resolveItem(item)) {
                anyVisible = true;
            }
        }
        return anyVisible;
    }

    /**
     * Hides separators that would otherwise sit at the start/end of {@code items}, or next to another
     * separator, because the section between them has no visible items left.
     *
     * @param items the items to inspect, in display order.
     */
    public static void collapseSeparators(Iterable<MenuItem> items) {
        SeparatorMenuItem previousVisibleSeparator = null;
        var visibleItemsPresent = false;
        for (var item : items) {
            if (item instanceof SeparatorMenuItem separator) {
                if (previousVisibleSeparator == null) {
                    if (!visibleItemsPresent) {
                        separator.setVisible(false);
                    } else {
                        separator.setVisible(true);
                        previousVisibleSeparator = separator;
                        visibleItemsPresent = false;
                    }
                } else {
                    if (!visibleItemsPresent) {
                        previousVisibleSeparator.setVisible(false);
                    }
                    separator.setVisible(true);
                    previousVisibleSeparator = separator;
                    visibleItemsPresent = false;
                }
            } else if (item.isVisible()) {
                visibleItemsPresent = true;
            }
        }
        if (previousVisibleSeparator != null && !visibleItemsPresent) {
            previousVisibleSeparator.setVisible(false);
        }
    }

    /**
     * Recursively fires {@code onHiding()} on every item's handler in {@code items}, including nested menus
     * that have no handler of their own (whose own items are then visited in turn).
     *
     * @param items the items to notify, in any order.
     */
    public static void fireHiding(Iterable<MenuItem> items) {
        for (var item : items) {
            if (item instanceof ManagedMenu managedMenu) {
                var handler = MenuHandler.getHandler(managedMenu);
                if (handler != null) {
                    handler.onHiding();
                } else {
                    fireHiding(managedMenu.getItems());
                }
            } else if (item instanceof ManagedItem) {
                var managedItem = (MenuItem & ManagedItem) item;
                var handler = MenuItemHandler.getHandler(managedItem);
                if (handler != null) {
                    handler.onHiding();
                }
            }
        }
    }

    private static boolean resolveItem(MenuItem item) {
        if (item instanceof ManagedMenu managedMenu) {
            return resolve(managedMenu);
        }
        if (item instanceof SeparatorMenuItem) {
            return false;
        }
        if (item instanceof ManagedItem) {
            var managedItem = (MenuItem & ManagedItem) item;
            var handler = MenuItemHandler.getHandler(managedItem);
            if (handler != null) {
                handler.onShowing();
                handler.onUpdate();
            }
            return item.isVisible();
        }
        return item.isVisible();
    }

    private MenuVisibility() {
        // empty
    }
}
