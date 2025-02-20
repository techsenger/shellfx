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

package com.techsenger.tabshell.kit.core.settings;

import jakarta.xml.bind.annotation.XmlAttribute;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Pavel Castornii
 */
public class FontSettings implements com.techsenger.tabshell.core.settings.FontSettings {

    private IntegerProperty size = new SimpleIntegerProperty();

    @Override
    public IntegerProperty sizeProperty() {
        return size;
    }

    @Override
    public int getSize() {
        return size.get();
    }

    @XmlAttribute(name = "size")
    public void setSize(int size) {
        this.size.set(size);
    }
}
