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

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;

/**
 * All handlers for managed menu items are set by external manager.
 *
 * @author Pavel Castornii
 */
public class ManagedCheckMenuItem extends CheckMenuItem implements ManagedItem {
    private final ManagedItemSupport support;

    public ManagedCheckMenuItem(int position) {
        this(null, null, position);
    }

    public ManagedCheckMenuItem(String string, int position) {
        this(string, null, position);
    }

    public ManagedCheckMenuItem(String text, Node node, int position) {
        super(text, node);
        this.support = new ManagedItemSupport(position);
    }

    @Override
    public int getPosition() {
        return support.getPosition();
    }

    @Override
    public ObjectProperty<ManagedMenuGroup> groupProperty() {
        return support.groupProperty();
    }
}

