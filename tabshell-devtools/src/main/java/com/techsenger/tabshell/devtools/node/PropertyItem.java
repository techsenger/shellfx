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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.connectorfx.scenegraph.attributes.Attribute;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyItem {

    record PropertyValueData(String text, boolean isDefault) { }

    private final AttributeCategory category;

    private final Attribute<?> attribute;

    private final PropertyItemType type;

    private final boolean readOnly;

    private PropertyValueData valueData;

    public PropertyItem() {
        this.type = PropertyItemType.ROOT;
        this.attribute = null;
        this.category = null;
        this.readOnly = false;
    }

    public PropertyItem(AttributeCategory category) {
        this.category = category;
        this.type = PropertyItemType.CATEGORY;
        this.attribute = null;
        this.readOnly = false;
    }

    public PropertyItem(AttributeCategory category, Attribute<?> attribute, boolean readOnly) {
        this.category = category;
        this.type = PropertyItemType.PROPERTY;
        this.attribute = attribute;
        this.readOnly = readOnly;
    }

    public PropertyItemType getType() {
        return type;
    }

    public AttributeCategory getCategory() {
        return category;
    }

    public Attribute<?> getAttribute() {
        return attribute;
    }

    /**
     * Use this property but not the {@link Attribute#observableType} because the last one is wrong.
     *
     * @return
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    PropertyValueData getValueData() {
        return valueData;
    }

    void setValueData(PropertyValueData valueData) {
        this.valueData = valueData;
    }
}
