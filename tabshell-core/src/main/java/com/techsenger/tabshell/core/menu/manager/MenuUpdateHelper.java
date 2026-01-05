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

package com.techsenger.tabshell.core.menu.manager;

import com.techsenger.tabshell.material.menu.NamedMenuItemUpdate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.MenuItem;

/**
 * This class manages updates for menu elements (items and menus). One instance of manager is created for every menu.
 *
 * @author Pavel Castornii
 */
class MenuUpdateHelper {

    /**
     * One for for menu and menu item.
     */
    private static class UpdateEntry {

        private final MenuItem menuItem;

        private final MenuElementState state;

        private final NamedMenuItemUpdate update;

        UpdateEntry(MenuItem menuItem, MenuElementState savedState, NamedMenuItemUpdate update) {
            this.menuItem = menuItem;
            this.state = savedState;
            this.update = update;
        }

        public MenuItem getMenuItem() {
            return menuItem;
        }

        public MenuElementState getState() {
            return state;
        }

        public NamedMenuItemUpdate getUpdate() {
            return update;
        }
    }

    private List<UpdateEntry> updateEntries = new ArrayList<>();

    MenuUpdateHelper() {
        //empty
    }

    public void applyUpdate(MenuItem item, MenuElementState state, NamedMenuItemUpdate update) {
        if (update == null) {
            return;
        }
        var entry = new UpdateEntry(item, state, update);
        this.updateEntries.add(entry);
        if (update.isTextChanged()) {
            item.setText(update.getText());
        }
        if (update.isGraphicChanged()) {
            item.setGraphic(update.getGraphic());
        }
    }

    public void removeUpdates() {
        for (var entry : this.updateEntries) {
            var update = entry.getUpdate();
            var savedState = entry.getState();
            var item = entry.getMenuItem();
            if (update.isTextChanged()) {
                item.setText(savedState.getText());
            }
            if (update.isGraphicChanged()) {
                item.setGraphic(savedState.getGraphic());
            }
        }
        this.updateEntries.clear();
    }
}
