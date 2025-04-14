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

import com.techsenger.tabshell.core.DefaultShellView;
import com.techsenger.tabshell.material.menu.KeyedMenu;
import com.techsenger.tabshell.material.menu.KeyedMenuItem;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
class MenuItemActionInterceptor implements ActionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemActionInterceptor.class);

    private final DefaultShellView shellView;

    private final EventHandler<ActionEvent> action;

    private final KeyedMenu menu;

    private final KeyedMenuItem item;

    MenuItemActionInterceptor(DefaultShellView shellView, KeyedMenu menu, KeyedMenuItem item) {
        this.shellView = shellView;
        this.action = item.getOnAction();
        this.menu = menu;
        this.item = item;
    }

    @Override
    public void handle(ActionEvent t) {
        var menuAware = shellView.getViewModel().getCurrentMenuAware();
        var helper = menuAware.getMenuItemHelper(item.getKey());
        var included = true;
        var valid = false;
        if (item.isOptional()) {
            if (helper == null || !Boolean.TRUE.equals(helper.getItemIncluded())) {
                included = false;
            }
        }
        if (included) {
            if (item.isValidatable()) {
                if (helper != null && Boolean.TRUE.equals(helper.getItemValid())) {
                    valid = true;
                }
            } else {
                valid = true;
            }
        }
        if (included && valid) {
            if (this.action != null) {
                action.handle(t);
            }
            logger.debug("Event from '{}' menu item was dispatched", menu.getText().replace("_", ""));
        } else {
            logger.debug("Event from '{}' menu item was ignored; included: {}, valid: {}",
                    menu.getText().replace("_", ""), included, valid);
        }
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return action;
    }
}
