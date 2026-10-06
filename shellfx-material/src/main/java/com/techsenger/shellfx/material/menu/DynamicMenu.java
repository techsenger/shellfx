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
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * A menu that is visible only while at least one of its items is visible, so a menu assembled from contributions of
 * independent plugins disappears when none of them has anything to show. It also hides the separators around the
 * groups that have nothing to show (see {@link GroupCollapser}).
 *
 * <p>The {@code visible} property of the menu is bound, so it cannot be set; use a plain {@link Menu} for a menu that
 * decides its own visibility. To make the visibility depend on something else as well, add a condition with
 * {@link #addVisibleCondition(ObservableValue)}: the menu is visible while it has a visible item and all its
 * conditions are true.
 *
 * @author Pavel Castornii
 */
public class DynamicMenu extends Menu {

    private final List<ObservableValue<Boolean>> visibleConditions = new ArrayList<>();

    private final BooleanBinding visibleBinding = Bindings.createBooleanBinding(this::computeVisible);

    private final InvalidationListener invalidator = observable -> visibleBinding.invalidate();

    private final WeakInvalidationListener weakInvalidator = new WeakInvalidationListener(invalidator);

    public DynamicMenu() {
        this("");
    }

    public DynamicMenu(String text) {
        this(text, null);
    }

    public DynamicMenu(String text, Node graphic) {
        super(text, graphic);
        ListChangeListener<MenuItem> itemsListener = change -> {
            while (change.next()) {
                for (var item : change.getRemoved()) {
                    item.visibleProperty().removeListener(weakInvalidator);
                }
                for (var item : change.getAddedSubList()) {
                    if (!(item instanceof SeparatorMenuItem)) {
                        item.visibleProperty().addListener(weakInvalidator);
                    }
                }
            }
            visibleBinding.invalidate();
        };
        getItems().addListener(itemsListener);
        visibleProperty().bind(visibleBinding);
        GroupCollapser.install(this);
    }

    /**
     * Makes the visibility of the menu depend on {@code condition} as well: the menu is visible while it has a visible
     * item and all its conditions are true; a {@code null} value of a condition counts as {@code false}. The condition
     * is owned by the caller, who removes it when it is no longer needed. Adding a condition that is already added
     * has no effect.
     *
     * @param condition the condition to add.
     */
    public void addVisibleCondition(ObservableValue<Boolean> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        if (visibleConditions.contains(condition)) {
            return;
        }
        visibleConditions.add(condition);
        condition.addListener(weakInvalidator);
        visibleBinding.invalidate();
    }

    /**
     * Stops the menu from depending on {@code condition}; does nothing if the condition was not added.
     *
     * @param condition the condition to remove.
     */
    public void removeVisibleCondition(ObservableValue<Boolean> condition) {
        if (visibleConditions.remove(condition)) {
            condition.removeListener(weakInvalidator);
            visibleBinding.invalidate();
        }
    }

    private boolean computeVisible() {
        return MenuItemUtils.hasVisibleItem(getItems())
                && visibleConditions.stream().allMatch(c -> Boolean.TRUE.equals(c.getValue()));
    }
}
