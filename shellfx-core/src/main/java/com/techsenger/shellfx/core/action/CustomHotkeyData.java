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

package com.techsenger.shellfx.core.action;

import com.techsenger.shellfx.core.StoreData;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javafx.scene.input.KeyCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The choices of the user about the hotkeys of the actions of a component, in the form they are kept in a store: the
 * root object the store saves and restores. A component copies the choices to its actions with {@link #copyTo}
 * before it registers them, and copies them back with {@link #copyFrom} after it has unregistered them.
 *
 * @author Pavel Castornii
 */
public final class CustomHotkeyData implements StoreData {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CustomHotkeyData.class);

    private final List<CustomHotkey> customs = new ArrayList<>();

    public CustomHotkeyData() {
        // empty
    }

    /**
     * Gives each action the hotkey the user has chosen for it; the others keep theirs. Choices for names that are not
     * among the actions are skipped.
     *
     * @param actions the actions the component declares
     * @throws IllegalArgumentException if a saved hotkey cannot be read
     */
    public void copyTo(Collection<Action> actions) {
        var actionsByName = new HashMap<ActionName, Action>();
        for (var action : actions) {
            actionsByName.put(action.getName(), action);
        }
        for (var custom : customs) {
            var action = actionsByName.get(custom.getName());
            if (action == null) {
                logger.debug("No action {} in the component, the saved one is skipped", custom.getName());
                continue;
            }
            var hotkey = custom.getHotkey();
            action.setHotkey(hotkey == null ? null : KeyCombination.valueOf(hotkey));
        }
    }

    /**
     * Replaces the choices with the actions the user has changed, i.e. those whose hotkey differs from the default
     * one, so that the owner of the data can save them.
     *
     * @param actions the actions the component declares
     */
    public void copyFrom(Collection<Action> actions) {
        customs.clear();
        for (var action : actions) {
            if (!action.isCustomHotkey()) {
                continue;
            }
            try {
                var hotkey = action.getHotkey();
                customs.add(new CustomHotkey(action.getName(), hotkey == null ? null : hotkey.getName()));
            } catch (IllegalArgumentException ex) {
                logger.error("Couldn't save {}", action, ex);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return customs.isEmpty();
    }

    @Override
    public String toString() {
        return "CustomHotkeyData [customs: " + customs + ']';
    }
}
