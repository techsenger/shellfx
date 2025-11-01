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

import atlantafx.base.theme.Styles;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Keyed menu allows program to understand where in menu it can add/remove items. That's why all menus must be
 * created using this class.
 *
 * @author Pavel Castornii
 */
public class NamedMenu extends Menu implements NamedMenuElement<MenuName> {

    private final MenuName name;

    private final boolean optional;

    private final boolean validatable;

    private final boolean updatable;

    private final int position;

    private final ObjectProperty<NamedMenuGroup> group = new SimpleObjectProperty<>();

    public NamedMenu(MenuName name, String text, int position) {
        this(name, false, false, false, text, null, position, null);
    }

    public NamedMenu(MenuName name, boolean optional, boolean validatable, boolean updatable, String text,
            int position) {
        this(name, optional, validatable, updatable, text, null, position, null);
    }

    public NamedMenu(MenuName name, String text, Node node, int position) {
        this(name, false, false, false, text, node, position, null);
    }

    public NamedMenu(MenuName name, boolean optional, boolean validatable, boolean updatable, String text,
            Node node, int position) {
        this(name, optional, validatable, updatable, text, node, position, null);
    }

    public NamedMenu(MenuName name, String text, Node node, int position, MenuItem... mis) {
        this(name, false, false, false, text, node, position, mis);
    }

    public NamedMenu(MenuName name, boolean optional, boolean validatable, boolean updatable, String text,
            Node node, int position, MenuItem... mis) {
        super(text, node, mis);
        this.optional = optional;
        this.validatable = validatable;
        this.updatable = updatable;
        this.name = name;
        this.position = position;
        this.getStyleClass().add(Styles.DENSE);
    }

    @Override
    public MenuName getName() {
        return name;
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
     * Returns the group this menu belongs to or null.
     *
     * @return
     */
    public ObjectProperty<NamedMenuGroup> groupProperty() {
        return this.group;
    }

    /**
     * Returns the group this menu belongs to or null.
     *
     * @return
     */
    public NamedMenuGroup getGroup() {
        return group.get();
    }

    /**
     * Sets the group this menu belongs to or null.
     *
     * @return
     */
    public void setGroup(NamedMenuGroup group) {
        this.group.set(group);
    }
}
