/*
 * Copyright 2026 Pavel Castornii.
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
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the action of a managed menu item. A mouse click happens on a menu whose state was just resolved, so the
 * action runs as is. An accelerator can fire while the menu is closed and its state is stale, so the state is
 * updated first and the action is skipped if the item turns out to be disabled or invisible.
 *
 * @author Pavel Castornii
 */
final class MenuItemDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemDispatcher.class);

    static void dispatch(MenuItem item, MenuItemHandler<?> handler, Supplier<@Nullable InputEvent> inputEvent) {
        var event = inputEvent.get();
        var updateCalled = event instanceof KeyEvent;
        if (updateCalled) {
            handler.onUpdate();
        }
        var disabled = item.isDisable();
        var visible = item.isVisible();
        var actionCalled = !updateCalled || (!disabled && visible);
        if (actionCalled) {
            handler.onAction();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Action of '{}': input event: {}, onUpdate called: {}, onAction called: {}, "
                    + "disabled: {}, visible: {}", getText(item), describe(event), updateCalled, actionCalled,
                    disabled, visible);
        }
    }

    private static String getText(MenuItem item) {
        return String.valueOf(item.getText()).replace("_", "");
    }

    private static String describe(@Nullable InputEvent inputEvent) {
        return inputEvent == null ? "none" : inputEvent.getEventType().getName();
    }

    private MenuItemDispatcher() {
        // empty
    }
}
