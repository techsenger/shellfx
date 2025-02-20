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

package com.techsenger.tabshell.material.menu;

import com.techsenger.toolkit.core.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class KeyedMenuGroup {

    private final MenuGroupKey key;

    private final List<Pair<Integer, MenuItem>> itemsAndPositions = new ArrayList<>();

    public KeyedMenuGroup(MenuGroupKey key) {
        this.key = key;
    }

    public MenuGroupKey getKey() {
        return key;
    }

    /**
     * Returns items.
     *
     * @return
     */
    public List<MenuItem> getItems() {
        return itemsAndPositions.stream().map(p -> p.getSecond()).collect(Collectors.toList());
    }

    /**
     * Sorts group.
     */
    public void sort() {
        Collections.sort(itemsAndPositions, (o1, o2) -> Integer.compare(o1.getFirst(), o2.getFirst()));
    }

    public void addItem(Integer position, MenuItem item) {
        itemsAndPositions.add(new Pair<>(position, item));
    }

    public boolean isEmpty() {
        return itemsAndPositions.isEmpty();
    }
}
