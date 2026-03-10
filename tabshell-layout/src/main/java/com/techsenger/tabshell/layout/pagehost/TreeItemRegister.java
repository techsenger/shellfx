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

    private final Map<ComponentName, TreeItem<PageDescriptor>> itemsByComponent = new HashMap<>();

    private final ListChangeListener<TreeItem<PageDescriptor>> globalListener = this::onChildrenChanged;

    TreeItemRegister(TreeItem<PageDescriptor> rootItem) {
        traverseAndAdd(rootItem);
    }

    public TreeItem<PageDescriptor> getItem(ComponentName component) {
        return itemsByComponent.get(component);
    }

    private void traverseAndAdd(TreeItem<PageDescriptor> item) {
        item.getChildren().addListener(globalListener);
        var value = item.getValue();
        if (value != null) {
            if (value.getName() != null) {
                itemsByComponent.put(value.getName(), item);
            }
        }
        for (TreeItem<PageDescriptor> child : item.getChildren()) {
            traverseAndAdd(child);
        }
    }

    private void traverseAndRemove(TreeItem<PageDescriptor> item) {
        item.getChildren().removeListener(globalListener);
        var value = item.getValue();
        if (value != null) {
            if (value.getName() != null) {
                itemsByComponent.remove(value.getName());
            }
        }
        for (TreeItem<PageDescriptor> child : item.getChildren()) {
            traverseAndRemove(child);
        }
    }

    private void onChildrenChanged(ListChangeListener.Change<? extends TreeItem<PageDescriptor>> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (TreeItem<PageDescriptor> added : change.getAddedSubList()) {
                    traverseAndAdd(added);
                }
            }

            if (change.wasRemoved()) {
                for (TreeItem<PageDescriptor> removed : change.getRemoved()) {
                    traverseAndRemove(removed);
                }
            }
        }
    }
}
