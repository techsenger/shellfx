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

package com.techsenger.tabshell.core.settings.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Pavel Castornii
 */
public class EnumAdapter<T extends Enum<T>> extends XmlAdapter<String, T> {

    private final Class<T> type;

    public EnumAdapter(Class<T> type) {
        this.type = type;
    }

    @Override
    public T unmarshal(String value) {
        if (value == null) {
            return null;
        }
        return Enum.valueOf(type, value);
    }

    @Override
    public String marshal(T value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
