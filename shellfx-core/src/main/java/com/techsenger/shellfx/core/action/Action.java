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
import java.util.Objects;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.KeyCombination;

/**
 * Something a user can do in the application, described by its name, the group it belongs to and the hotkey that
 * invokes it: the one it has until the user changes it and the one it has now. Either hotkey can be {@code null},
 * which stands for an action without a key. It carries no behavior: what the action does is up to the control that
 * is bound to its hotkey. The name is also the key a text for the user is found by.
 *
 * @author Pavel Castornii
 */
public final class Action {

    private final ActionName name;

    private final ActionGroupName groupName;

    private final @Nullable KeyCombination defaultHotkey;

    private final ObjectProperty<@Nullable KeyCombination> hotkey = new SimpleObjectProperty<>();

    /**
     * Creates an action whose current hotkey is its default one.
     *
     * @param name the name, unique in the application; it is what the user's choice is saved under, so it is never
     *     changed once released
     * @param groupName the name of the group the action is listed in
     * @param defaultHotkey the hotkey the action has until the user changes it, or {@code null} for none
     */
    public Action(ActionName name, ActionGroupName groupName, @Nullable KeyCombination defaultHotkey) {
        this.name = name;
        this.groupName = groupName;
        this.defaultHotkey = defaultHotkey;
        this.hotkey.set(defaultHotkey);
    }

    public ActionName getName() {
        return name;
    }

    public ActionGroupName getGroupName() {
        return groupName;
    }

    public @Nullable KeyCombination getDefaultHotkey() {
        return defaultHotkey;
    }

    public @Nullable KeyCombination getHotkey() {
        return hotkey.get();
    }

    public void setHotkey(@Nullable KeyCombination hotkey) {
        this.hotkey.set(hotkey);
    }

    public ObjectProperty<@Nullable KeyCombination> hotkeyProperty() {
        return hotkey;
    }

    /**
     * Gives the action its default hotkey, undoing a change of the user.
     */
    public void resetHotkey() {
        hotkey.set(defaultHotkey);
    }

    /**
     * Tells whether the current hotkey differs from the default one, i.e. whether the user has changed it.
     */
    public boolean isCustomHotkey() {
        return !Objects.equals(hotkey.get(), defaultHotkey);
    }

    @Override
    public String toString() {
        return "Action[" + name + "]";
    }
}
