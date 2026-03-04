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

import com.techsenger.tabshell.core.DefaultShellFxView;
import com.techsenger.tabshell.core.MenuAwarePort;
import com.techsenger.tabshell.material.menu.NamedMenu;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We need this class because when menu items are not shown to user they are NOT disabled and their actions can be
 * called using accelerator keys. So, this class intercepts the event and decides to call origin action or not.
 *
 * @author Pavel Castornii
 */
class MenuActionInterceptor implements ActionInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MenuActionInterceptor.class);

    private final DefaultShellFxView<?> shellView;

    private final EventHandler<ActionEvent> action;

    private final NamedMenu menu;

    MenuActionInterceptor(DefaultShellFxView<?> shellView, NamedMenu menu) {
        this.shellView = shellView;
        this.action = menu.getOnAction();
        this.menu = menu;
    }

    @Override
    public void handle(ActionEvent t) {
        MenuAwarePort menuAware = (MenuAwarePort) shellView.getMenuAware().getPresenter();
        var helper = menuAware.getMenuHelper(menu.getName());
        var included = true;
        var valid = false;
        if (menu.isOptional()) {
            if (helper == null || !Boolean.TRUE.equals(helper.getMenuIncluded())) {
                included = false;
            }
        }
        if (included) {
            if (menu.isValidatable()) {
                if (helper != null && Boolean.TRUE.equals(helper.getMenuValid())) {
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
            logger.debug("Event from '{}' menu was dispatched", menu.getText().replace("_", ""));
        } else {
            logger.debug("Event from '{}' menu was ignored; included: {}, valid: {}",
                    menu.getText().replace("_", ""), included, valid);
        }
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return action;
    }
}
