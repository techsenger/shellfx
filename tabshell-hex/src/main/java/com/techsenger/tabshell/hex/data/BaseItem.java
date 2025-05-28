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

import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class BaseItem extends AbstractItem {

    private final StringProperty base = new SimpleStringProperty();

    private final Function<Bases, String> baseSelector;

    public BaseItem(String base, Function<Bases, String> baseSelector) {
        setBase(base);
        this.baseSelector = baseSelector;
    }

    public StringProperty baseProperty() {
        return base;
    }

    public String getBase() {
        return base.get();
    }

    public void setBase(String base) {
        this.base.set(base);
    }

    public Function<Bases, String> getBaseSelector() {
        return baseSelector;
    }
}
