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

package com.techsenger.shellfx.core.menu.manager;

import com.techsenger.shellfx.material.menu.ManagedMenu;
import com.techsenger.shellfx.material.menu.ManagedMenuGroup;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;
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

    public static void buildComponentMenuMessage(MenuItem menuOrItem, StringBuilder logMessageBuilder) {
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
        }
    }

    public static void logMenus(ObservableList<Menu> menus, boolean afterChange) {
        if (logger.isDebugEnabled()) {
            var builder = new StringBuilder();
            for (var menu : menus) {
                logMenu((ManagedMenu) menu, 0, builder);
            }
            if (afterChange) {
                logger.debug("Shell Menus after change: {}", builder.toString());
            } else {
                logger.debug("Shell Menus: {}", builder.toString());
            }
        }
    }

    private static void logMenu(ManagedMenu menu, int depth, StringBuilder builder) {
        builder.append(System.lineSeparator());
        var tab = "    ";
        builder.append(tab.repeat(depth));
        builder.append("Menu: ");
        builder.append(menu.getText().replace("_", ""));
        builder.append(", position: ");
        builder.append(menu.getPosition());
        var tab1 = tab.repeat(depth + 1);
        var tab2 = tab.repeat(depth + 2);
        ManagedMenuGroup group = null;
        for (var m : menu.getItems()) {
            if (m instanceof ManagedMenu) {
                var namedMenu = (ManagedMenu) m;
                if (namedMenu.getGroup() != group) {
                    group = namedMenu.getGroup();
                    logGroup(group, depth + 1, builder, tab);
                }
                logMenu(namedMenu, depth + 2, builder);
            } else if (m instanceof ManagedMenuItem item) {
                if (item.getGroup() != group) {
                    group = item.getGroup();
                    logGroup(group, depth + 1, builder, tab);
                }
                builder.append(System.lineSeparator());
                builder.append(tab2);
                builder.append("MenuItem: ");
                builder.append(item.getText().replace("_", ""));
                builder.append(", position: ");
                builder.append(item.getPosition());
                if (item.getAccelerator() != null) {
                    builder.append(", hotkey: ");
                    builder.append(item.getAccelerator());
                }
            }
        }
    }

    private static void logGroup(ManagedMenuGroup group, int depth, StringBuilder builder, String tab) {
        if (group == null) {
            return;
        }
        builder.append(System.lineSeparator());
        builder.append(tab.repeat(depth));
        builder.append("Group: ");
        builder.append(group.getName().getText());
    }

    private MenuLogger() {
        //empty
    }
}
