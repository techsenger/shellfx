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
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class EditPropertyTask<T> {

    private static final Logger logger = LoggerFactory.getLogger(EditPropertyTask.class);

    private final Attribute<?> attribute;

    private final Class<T> type;

    private final PropertyValueConverter<T> converter;

    private final Consumer<T> setter;

    private final Supplier<T> getter;

    public EditPropertyTask(Attribute<?> attribute, Class<T> type, PropertyValueConverter<T> converter,
            Consumer<T> setter, Supplier<T> getter) {
        this.attribute = attribute;
        this.type = type;
        this.converter = converter;
        this.setter = setter;
        this.getter = getter;
        logger.debug("EditorTask created; {}", this);
    }

    public Attribute<?> getAttribute() {
        return attribute;
    }

    public Class<T> getType() {
        return type;
    }

    public PropertyValueConverter<T> getConverter() {
        return converter;
    }

    public Consumer<T> getSetter() {
        return setter;
    }

    public Supplier<T> getGetter() {
        return getter;
    }

    @Override
    public String toString() {
        return "[" + "attribute=" + attribute + ", type=" + type + ']';
    }
}
