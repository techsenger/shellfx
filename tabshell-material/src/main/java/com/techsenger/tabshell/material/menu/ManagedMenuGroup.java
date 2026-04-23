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

package com.techsenger.tabshell.material.menu;

import com.techsenger.tabshell.material.Named;
import com.techsenger.tabshell.material.Positioned;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ManagedMenuGroup implements Named, Positioned {

    private final MenuGroupName name;

    private final int position;

    private final List<MenuItem> items = new ArrayList<>();

    /**
     * Group doesn't have a text, but it has a name.
     *
     * @param name
     */
    public ManagedMenuGroup(MenuGroupName name, int position) {
        this.name = name;
        this.position = position;
    }

    @Override
    public MenuGroupName getName() {
        return name;
    }

    /**
     * Returns the items.
     *
     * @return
     */
    public List<MenuItem> getItems() {
        return items;
    }

    /**
     * Returns the position.
     *
     * @return
     */
    @Override
    public int getPosition() {
        return position;
    }

    /**
     * Sorts the group.
     */
    public void sort() {
        Collections.sort(items, (o1, o2) ->
                Integer.compare(((Positioned) o1).getPosition(), ((Positioned) o2).getPosition()));
    }
}
