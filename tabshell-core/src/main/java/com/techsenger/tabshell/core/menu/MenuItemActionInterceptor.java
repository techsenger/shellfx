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

package com.techsenger.tabshell.core.menu;

import com.techsenger.tabshell.core.DefaultTabShellView;
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
class MenuItemActionInterceptor implements EventHandler<ActionEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemActionInterceptor.class);

    private final DefaultTabShellView shellView;

    private final EventHandler<ActionEvent> action;

    private final KeyedMenu menu;

    private final KeyedMenuItem item;

    MenuItemActionInterceptor(DefaultTabShellView shellView, KeyedMenu menu, KeyedMenuItem item) {
        this.shellView = shellView;
        this.action = item.getOnAction();
        this.menu = menu;
        this.item = item;
    }

    @Override
    public void handle(ActionEvent t) {
        var dispatched = false;
        var currentTab = shellView.getSelectedTab();
        if (currentTab != null) {
            var viewModel = currentTab.getViewModel();
            var supported = true;
            if (item.isOptional()) {
                supported = viewModel.isMenuItemSupported(menu.getKey(), item.getKey());
            }
            if (supported) {
                if (item.isValidatable()) {
                    if (viewModel.isMenuItemValid(menu.getKey(), item.getKey())) {
                        dispatched = true;
                    }
                } else {
                    dispatched = true;
                }
            }
        } else {
            dispatched = true;
        }
        if (dispatched) {
            if (this.action != null) {
                action.handle(t);
            }
            logger.debug("Event for '{}' was dispatched", item.getText().replace("_", ""));
        } else {
            logger.debug("Event for '{}' was consumed", item.getText().replace("_", ""));
        }
    }

    public EventHandler<ActionEvent> getAction() {
        return action;
    }
}
