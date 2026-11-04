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

import com.techsenger.annotations.Nullable;
import java.io.Serial;
import java.io.Serializable;
import javafx.scene.input.KeyCombination;

/**
 * What the user has changed in an action, in the form it is saved in: the name of the action and the hotkey the user
 * has chosen as a string, or {@code null} if the user has left the action without a key. Only the actions that differ
 * from their defaults are saved this way, so a default that changes in a newer version of a plugin still reaches the
 * users who have not changed the action.
 *
 * <p>The name is a constant of an enum (see {@link ActionName}), as it is found by the name after a restart.
 *
 * @author Pavel Castornii
 */
final class CustomHotkey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ActionName name;

    private final @Nullable String hotkey;

    /**
     * Creates the record of a change.
     *
     * @param name the name of the action, a constant of an enum
     * @param hotkey the hotkey in the form {@link KeyCombination#getName()} gives, or {@code null} for none
     * @throws IllegalArgumentException if {@code name} is not a constant of an enum
     */
    CustomHotkey(ActionName name, @Nullable String hotkey) {
        if (!(name instanceof Enum<?>)) {
            throw new IllegalArgumentException("The name of a saved action must be a constant of an enum: " + name);
        }
        this.name = name;
        this.hotkey = hotkey;
    }

    @Override
    public String toString() {
        return "CustomHotkey[" + name + ", " + hotkey + "]";
    }

    ActionName getName() {
        return name;
    }

    @Nullable String getHotkey() {
        return hotkey;
    }
}
