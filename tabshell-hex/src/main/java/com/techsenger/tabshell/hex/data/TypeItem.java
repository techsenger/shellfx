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

package com.techsenger.tabshell.hex.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TypeItem<T> {

    private final StringProperty value = new SimpleStringProperty();

    private final StringProperty type = new SimpleStringProperty();

    private final ValueProvider<T> valueProvider;

    private final NumberBaseConverter<T> converter;

    private int size;

    private final SizeProvider sizeProvider;

    public TypeItem(int size, String type, ValueProvider<T> provider) {
        this(size, type, provider, null);
    }

    public TypeItem(int size, String type, ValueProvider<T> provider, NumberBaseConverter<T> converter) {
        this(size, type, provider, converter, null);
    }

    public TypeItem(String type, ValueProvider<T> provider, NumberBaseConverter<T> converter,
            SizeProvider sizeProvider) {
        this(0, type, provider, converter, sizeProvider);
    }

    private TypeItem(int size, String type, ValueProvider<T> provider, NumberBaseConverter<T> converter,
            SizeProvider sizeProvider) {
        this.size = size;
        this.type.set(type);
        this.valueProvider = provider;
        this.converter = converter;
        this.sizeProvider = sizeProvider;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public ValueProvider<T> getValueProvider() {
        return valueProvider;
    }

    public NumberBases createBases(Values values) {
        if (this.converter == null) {
            return null;
        }
        var value = this.valueProvider.provide(values);
        if (value == null) {
            return null;
        }
        var bits = getSize(values);
        return this.converter.convert(value, bits);
    }

    private int getSize(Values values) {
        if (this.sizeProvider == null) {
            return this.size;
        } else {
            return this.sizeProvider.provide(values);
        }
    }
}
