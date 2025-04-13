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

package com.techsenger.tabshell.core.menu.manager;

import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuGroup;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import com.techsenger.tabshell.material.menu.KeyedMenuItemUpdate;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 *
 * @author Pavel Castornii
 */
final class MenuLogger {

    public static final Level CONFIGURED_MENU_LOG_LEVEL = Level.DEBUG;

    private static final Logger logger = LoggerFactory.getLogger(MenuLogger.class);

    public static void buildComponentMenuMessage(MenuItem menuOrItem, KeyedMenuItemUpdate update,
            StringBuilder logMessageBuilder) {
        if (logMessageBuilder != null) {
            logMessageBuilder.append(System.lineSeparator());
            logMessageBuilder.append("    ");
            if (menuOrItem instanceof Menu) {
                logMessageBuilder.append("menu: ");
            } else {
                logMessageBuilder.append("item: ");
            }
            logMessageBuilder.append(menuOrItem.getText().replace("_", ""));
            logMessageBuilder.append(", included: ");
            logMessageBuilder.append(menuOrItem.isVisible());
            logMessageBuilder.append(", valid: ");
            logMessageBuilder.append(!menuOrItem.isDisable());
            logMessageBuilder.append(", update: ");
            logMessageBuilder.append(update);
        }
    }

    public static void logComponentMenuMessage(String menuName, MenuAware menuAware,
            StringBuilder logMessageBuilder) {
        if (logMessageBuilder != null) {
            logger.atLevel(CONFIGURED_MENU_LOG_LEVEL).log("Configuration of '{}' menu for '{}' component: {}",
                menuName.replace("_", ""), menuAware.getKey(), logMessageBuilder.toString());
        }
    }

    public static void logMenus(ObservableList<Menu> menus, boolean afterChange) {
        if (logger.isDebugEnabled()) {
            var builder = new StringBuilder();
            for (var menu : menus) {
                logMenu((KeyedMenu) menu, 0, builder);
            }
            if (afterChange) {
                logger.debug("TabShell Menus after change: {}", builder.toString());
            } else {
                logger.debug("TabShell Menus: {}", builder.toString());
            }
        }
    }

    private static void logMenu(KeyedMenu menu, int depth, StringBuilder builder) {
        builder.append(System.lineSeparator());
        var tab = "    ";
        builder.append(tab.repeat(depth));
        builder.append("Menu: ");
        builder.append(menu.getText().replace("_", ""));
        builder.append(", optional: ");
        builder.append(menu.isOptional());
        builder.append(", validatable: ");
        builder.append(menu.isValidatable());
        builder.append(", updatable: ");
        builder.append(menu.isUpdatable());
        builder.append(", position: ");
        builder.append(menu.getPosition());
        var tab1 = tab.repeat(depth + 1);
        var tab2 = tab.repeat(depth + 2);
        KeyedMenuGroup group = null;
        for (var m : menu.getItems()) {
            if (m instanceof KeyedMenu) {
                var keyedMenu = (KeyedMenu) m;
                if (keyedMenu.getGroup() != group) {
                    group = keyedMenu.getGroup();
                    logGroup(group, builder, tab);
                }
                logMenu(keyedMenu, depth + 2, builder);
            } else if (m instanceof KeyedMenuItem) {
                var keyedItem = (KeyedMenuItem) m;
                if (keyedItem.getGroup() != group) {
                    group = keyedItem.getGroup();
                    logGroup(group, builder, tab);
                }
                builder.append(System.lineSeparator());
                builder.append(tab2);
                builder.append("MenuItem: ");
                builder.append(keyedItem.getText().replace("_", ""));
                builder.append(", optional: ");
                builder.append(keyedItem.isOptional());
                builder.append(", validatable: ");
                builder.append(keyedItem.isValidatable());
                builder.append(", updatable: ");
                builder.append(keyedItem.isUpdatable());
                builder.append(", position: ");
                builder.append(keyedItem.getPosition());
                if (keyedItem.getAccelerator() != null) {
                    builder.append(", hotkey: ");
                    builder.append(keyedItem.getAccelerator());
                }
            }
        }
    }

    private static void logGroup(KeyedMenuGroup group, StringBuilder builder, String tab) {
        if (group == null) {
            return;
        }
        builder.append(System.lineSeparator());
        builder.append(tab);
        builder.append("Group: ");
        builder.append(group.getName());
    }

    private MenuLogger() {
        //empty
    }
}
