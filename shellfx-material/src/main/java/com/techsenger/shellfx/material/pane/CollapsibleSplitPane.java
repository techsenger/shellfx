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

package com.techsenger.shellfx.material.pane;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * A vertical {@link SplitPane}-backed container of {@link SplitPaneItem}s, each rendered under a header built
 * by {@link #headerFactoryProperty()}. Setting {@link SplitPaneItem#setCollapsed(boolean)} pins that item down
 * to just its header's height and redistributes the freed space evenly among the remaining, uncollapsed items
 * - collapsing every item but one is how that one ends up filling all the available space. Vertical-only for
 * now: a horizontal layout would need a differently-shaped header (title running the other way, etc.), and
 * there's no use case for it yet.
 *
 * @param <T> the type of value each hosted {@link SplitPaneItem} carries for its header factory to render.
 * @author Pavel Castornii
 */
public class CollapsibleSplitPane<T> extends StackPane {

    private static <T> Region createDefaultHeader(SplitPaneItem<T> item) {
        var box = new HBox(item.getCollapseButton());
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private static void detachFromParent(Node node) {
        if (node.getParent() instanceof Pane pane) {
            pane.getChildren().remove(node);
        }
    }

    private final SplitPane splitPane = new SplitPane();

    private final ObservableList<SplitPaneItem<T>> items = FXCollections.observableArrayList();

    private final ObjectProperty<Callback<SplitPaneItem<T>, Region>> headerFactory =
            new SimpleObjectProperty<>(CollapsibleSplitPane::createDefaultHeader);

    public CollapsibleSplitPane() {
        splitPane.setOrientation(Orientation.VERTICAL);
        getChildren().add(splitPane);
        items.addListener((ListChangeListener<SplitPaneItem<T>>) change -> rebuild());
        headerFactory.addListener((ov, oldValue, newValue) -> rebuild());
    }

    public ObservableList<SplitPaneItem<T>> getItems() {
        return items;
    }

    public ObjectProperty<Callback<SplitPaneItem<T>, Region>> headerFactoryProperty() {
        return headerFactory;
    }

    public Callback<SplitPaneItem<T>, Region> getHeaderFactory() {
        return headerFactory.get();
    }

    public void setHeaderFactory(Callback<SplitPaneItem<T>, Region> factory) {
        headerFactory.set(factory);
    }

    /**
     * Called by {@code item}'s own {@link SplitPaneItem#getCollapseButton()} action: maximizes {@code item}
     * (collapsing every sibling), or - if {@code item} is already the sole one not collapsed - restores an
     * even split. This is the only place cross-item state changes happen; {@link SplitPaneItem#setCollapsed}
     * itself never looks at siblings.
     */
    void toggleMaximized(SplitPaneItem<T> item) {
        var restore = isSoleExpanded(item);
        for (var other : items) {
            other.setCollapsed(!restore && other != item);
        }
    }

    /**
     * Rebuilds {@link #splitPane}'s items from {@link #items}' current content/header, wrapping each in a
     * {@code VBox} whose minimum height is bound to its header's actual height - so a collapsed item can never
     * shrink below its own header, which must stay clickable to restore it.
     */
    private void rebuild() {
        splitPane.getItems().clear();
        for (var item : items) {
            item.setOwner(this);
            var header = headerFactory.get().call(item);
            var content = item.getContent();
            detachFromParent(content);
            VBox.setVgrow(content, Priority.ALWAYS);
            var box = new VBox(header, content);
            box.minHeightProperty().bind(header.heightProperty());
            splitPane.getItems().add(box);
            item.collapsedProperty().addListener((ov, oldValue, newValue) -> onCollapsedChanged());
        }
        onCollapsedChanged();
    }

    /**
     * Recomputes divider positions and every item's button icon/tooltip after any item's collapsed state
     * changes.
     */
    private void onCollapsedChanged() {
        updateDividers();
        for (var item : items) {
            item.updateButton(isSoleExpanded(item));
        }
    }

    /**
     * Sets every divider so each collapsed item gets none of {@link #splitPane}'s space and the remaining,
     * uncollapsed items split the rest evenly; falls back to an even split across all items if every one of
     * them is collapsed, since none can meaningfully claim the freed space otherwise.
     */
    private void updateDividers() {
        var count = items.size();
        if (count < 2) {
            return;
        }
        var weights = new double[count];
        var totalWeight = 0.0;
        for (var i = 0; i < count; i++) {
            weights[i] = items.get(i).isCollapsed() ? 0.0 : 1.0;
            totalWeight += weights[i];
        }
        if (totalWeight == 0.0) {
            for (var i = 0; i < count; i++) {
                weights[i] = 1.0;
            }
            totalWeight = count;
        }
        var cumulative = 0.0;
        for (var i = 0; i < count - 1; i++) {
            cumulative += weights[i];
            splitPane.setDividerPosition(i, cumulative / totalWeight);
        }
    }

    /**
     * Returns whether {@code item} is currently the only one among {@link #items} that isn't collapsed - the
     * "maximized" state its own button's icon/tooltip reflects.
     */
    private boolean isSoleExpanded(SplitPaneItem<T> item) {
        if (items.size() < 2 || item.isCollapsed()) {
            return false;
        }
        for (var other : items) {
            if (other != item && !other.isCollapsed()) {
                return false;
            }
        }
        return true;
    }
}
