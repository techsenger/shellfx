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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.patternfx.core.ComponentName;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TreeItem;

/**
 * Maintains a registry of TreeItem<Page> instances keyed by their Page's ComponentName.
 * <p>
 * Listens to structural changes across the entire tree using a single global listener. Automatically registers
 * new items when added and deregisters them when removed.
 *
 * @author Pavel Castornii
 */
class TreeItemRegister {

    private final Map<ComponentName, TreeItem<Page>> itemsByComponent = new HashMap<>();

    private final ListChangeListener<TreeItem<Page>> globalListener = this::onChildrenChanged;

    TreeItemRegister(TreeItem<Page> rootItem) {
        traverseAndAdd(rootItem);
    }

    public TreeItem<Page> getItem(ComponentName component) {
        return itemsByComponent.get(component);
    }

    private void traverseAndAdd(TreeItem<Page> item) {
        item.getChildren().addListener(globalListener);
        if (item.getValue() != null) {
            validatePage(item);
            itemsByComponent.put(item.getValue().getName(), item);
        }
        for (TreeItem<Page> child : item.getChildren()) {
            traverseAndAdd(child);
        }
    }

    private void traverseAndRemove(TreeItem<Page> item) {
        item.getChildren().removeListener(globalListener);
        if (item.getValue() != null) {
            validatePage(item);
            itemsByComponent.remove(item.getValue().getName());
        }
        for (TreeItem<Page> child : item.getChildren()) {
            traverseAndRemove(child);
        }
    }

    /**
     * Some nodes can be empty containers, for example, root.
     *
     * @param item
     */
    private void validatePage(TreeItem<Page> item) {
        var page = item.getValue();
        Objects.requireNonNull(page.getName(), "Page name not provided in " + item);
        Objects.requireNonNull(page.getFactory(), "Factory not provided in " + item);
    }

    private void onChildrenChanged(ListChangeListener.Change<? extends TreeItem<Page>> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (TreeItem<Page> added : change.getAddedSubList()) {
                    traverseAndAdd(added);
                }
            }

            if (change.wasRemoved()) {
                for (TreeItem<Page> removed : change.getRemoved()) {
                    traverseAndRemove(removed);
                }
            }
        }
    }
}
