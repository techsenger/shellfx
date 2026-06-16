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

package com.techsenger.shellfx.material.menu;

import com.techsenger.shellfx.material.Named;
import com.techsenger.shellfx.material.Positioned;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * All handlers for managed menu are set by external manager.
 *
 * @author Pavel Castornii
 */
public class ManagedMenu extends Menu implements Named, Positioned {

    private final MenuName name;

    private final int position;

    private final ObjectProperty<ManagedMenuGroup> group = new SimpleObjectProperty<>();

    public ManagedMenu(MenuName name, String text, int position) {
        this(name, text, null, position, null);
    }

    public ManagedMenu(MenuName name, String text, Node node, int position) {
        this(name, text, node, position, null);
    }

    public ManagedMenu(MenuName name, String text, Node node, int position, MenuItem... mis) {
        super(text, node, mis);
        this.name = name;
        this.position = position;
    }

    @Override
    public MenuName getName() {
        return name;
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
    public ObjectProperty<ManagedMenuGroup> groupProperty() {
        return this.group;
    }

    /**
     * Returns the group this menu belongs to or null.
     *
     * @return
     */
    public ManagedMenuGroup getGroup() {
        return group.get();
    }

    /**
     * Sets the group this menu belongs to or null.
     *
     * @return
     */
    public void setGroup(ManagedMenuGroup group) {
        this.group.set(group);
    }
}
