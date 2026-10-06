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

package com.techsenger.shellfx.material.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.WindowEvent;

/**
 * A context menu that is not shown when none of its items is visible, so a menu assembled from contributions of
 * independent plugins does not pop up empty. It also hides the separators around the groups that have nothing to
 * show (see {@link GroupCollapser}).
 *
 * <p>To make showing depend on something else as well, add a condition with
 * {@link #addVisibleCondition(ObservableValue)}: the menu is shown only while it has a visible item and all its
 * conditions are true.
 *
 * @author Pavel Castornii
 */
public class DynamicContextMenu extends ContextMenu {

    private final List<ObservableValue<Boolean>> visibleConditions = new ArrayList<>();

    public DynamicContextMenu(MenuItem... items) {
        super(items);
        GroupCollapser.install(this);
        addEventHandler(WindowEvent.WINDOW_SHOWING, e -> {
            if (!canShow()) {
                e.consume();
            }
        });
    }

    /**
     * Makes showing of the menu depend on {@code condition} as well: the menu is shown only while it has a visible
     * item and all its conditions are true; a {@code null} value of a condition counts as {@code false}. The condition
     * is owned by the caller, who removes it when it is no longer needed. Adding a condition that is already added
     * has no effect.
     *
     * @param condition the condition to add.
     */
    public void addVisibleCondition(ObservableValue<Boolean> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        if (!visibleConditions.contains(condition)) {
            visibleConditions.add(condition);
        }
    }

    /**
     * Stops showing of the menu from depending on {@code condition}; does nothing if the condition was not added.
     *
     * @param condition the condition to remove.
     */
    public void removeVisibleCondition(ObservableValue<Boolean> condition) {
        visibleConditions.remove(condition);
    }

    private boolean canShow() {
        return MenuItemUtils.hasVisibleItem(getItems())
                && visibleConditions.stream().allMatch(c -> Boolean.TRUE.equals(c.getValue()));
    }
}
