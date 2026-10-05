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

package com.techsenger.shellfx.material;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A group of controls that are laid out together. A builder puts the registered controls into it, and a subclass
 * created by a custom factory can observe them, for example, to count the items or to react to their changes.
 *
 * @param <C> the type of the controls the group holds
 * @author Pavel Castornii
 */
public class ControlGroup<C> {

    private final ObservableList<C> items = FXCollections.observableArrayList();

    /**
     * Returns the controls of the group in the order of their positions. A builder replaces the content of the list
     * while it assembles the group, so only observe it; a group left empty is not shown.
     */
    public ObservableList<C> getItems() {
        return this.items;
    }
}
