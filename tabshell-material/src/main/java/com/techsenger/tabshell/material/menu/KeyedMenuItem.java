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

package com.techsenger.tabshell.material.menu;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * Keyed menu allows program to understand where in menu it can add/remove items. That's why all menu items
 * must be created using this class.
 *
 * @author Pavel Castornii
 */
public class KeyedMenuItem extends MenuItem implements KeyedMenuElement<MenuItemKey> {

    private final MenuItemKey key;

    private final boolean optional;

    private final boolean validatable;

    private final boolean updatable;

    private final int position;

    private final ObjectProperty<KeyedMenuGroup> group = new SimpleObjectProperty<>();

    public KeyedMenuItem(MenuItemKey key, String text, int position) {
        this(key, false, false, false, text, null, position);
    }

    public KeyedMenuItem(MenuItemKey key, boolean optional, boolean validatable, boolean updatable, String text,
            int position) {
        this(key, optional, validatable, updatable, text, null, position);
    }

    public KeyedMenuItem(MenuItemKey key, String text, Node node, int position) {
        this(key, false, false, false, text, node, position);
    }

    public KeyedMenuItem(MenuItemKey key, boolean optional, boolean validatable, boolean updatable, String text,
            Node node, int position) {
        super(text, node);
        this.key = key;
        this.optional = optional;
        this.validatable = validatable;
        this.updatable = updatable;
        this.position = position;
    }

    @Override
    public MenuItemKey getKey() {
        return key;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isValidatable() {
        return validatable;
    }

    @Override
    public boolean isUpdatable() {
        return updatable;
    }

    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Returns the group this item belongs to or null.
     *
     * @return
     */
    public ObjectProperty<KeyedMenuGroup> groupProperty() {
        return this.group;
    }

    /**
     * Returns the group this item belongs to or null.
     *
     * @return
     */
    public KeyedMenuGroup getGroup() {
        return group.get();
    }

    /**
     * Sets the group this item belongs to or null.
     *
     * @return
     */
    public void setGroup(KeyedMenuGroup group) {
        this.group.set(group);
    }
}
