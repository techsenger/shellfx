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

package com.techsenger.tabshell.jfx.inspector;

import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class AttributeInfo {

    private final String text;

    private final AttributeCategory category;

    private final Attribute<?> attribute;

    private final ObservableList<AttributeInfo> children;

    private final BooleanProperty expanded = new SimpleBooleanProperty();

    public AttributeInfo(Attribute<?> attribute) {
        this.category = null;
        this.attribute = attribute;
        this.children = FXCollections.emptyObservableList();
        this.text = attribute.name();
    }

    public AttributeInfo(AttributeCategory category) {
        this.category = category;
        this.attribute = null;
        this.children = FXCollections.observableArrayList();
        this.text = category + " Properties";
    }

    public AttributeCategory getCategory() {
        return category;
    }

    public Attribute<?> getAttribute() {
        return attribute;
    }

    public String getText() {
        return text;
    }

    public ObservableList<AttributeInfo> getChildren() {
        return children;
    }

    public final boolean isExpanded() {
        return expanded.get();
    }

    public final void setExpanded(boolean value) {
        expanded.set(value);
    }

    public final BooleanProperty expandedProperty() {
        return expanded;
    }
}
