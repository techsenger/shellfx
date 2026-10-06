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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.WindowEvent;

/**
 * Hides the separators around the sections of a menu that ended up empty. It only reads the {@code visible} state of
 * the items when the menu is about to be shown and never changes the visibility of the menu or of its items. The
 * dynamic menus install it themselves; a plain menu can install it on its own.
 *
 * @author Pavel Castornii
 */
public final class GroupCollapser {

    /**
     * Collapses the separators of {@code menu} every time it is about to be shown.
     *
     * @param menu the menu to manage.
     */
    public static void install(Menu menu) {
        menu.addEventHandler(Menu.ON_SHOWING, e -> collapseSeparators(menu.getItems()));
    }

    /**
     * Collapses the separators of {@code contextMenu} every time it is about to be shown.
     *
     * @param contextMenu the context menu to manage.
     */
    public static void install(ContextMenu contextMenu) {
        contextMenu.addEventHandler(WindowEvent.WINDOW_SHOWING, e -> collapseSeparators(contextMenu.getItems()));
    }

    private static void collapseSeparators(Iterable<MenuItem> items) {
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

    private GroupCollapser() {
        // empty
    }
}
