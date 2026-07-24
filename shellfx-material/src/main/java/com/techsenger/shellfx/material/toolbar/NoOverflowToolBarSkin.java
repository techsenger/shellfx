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

package com.techsenger.shellfx.material.toolbar;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.control.skin.ToolBarSkin;
import javafx.scene.layout.HBox;

/**
 * A {@link ToolBarSkin} variant that never overflows items into the built-in "&gt;&gt;" overflow menu.
 *
 * <p>The default {@code ToolBarSkin} decides what fits by computing an {@code organizeOverflow} pass inside
 * {@link #layoutChildren}, moving any item that doesn't fit into a popup menu behind an overflow button. This
 * conflicts with a component that manages its own visible/hidden item set (e.g. via {@code managed}/
 * {@code visible} toggling driven by its own width-fit logic) — the two mechanisms fight over the same items,
 * and the overflow button ends up firing even when the component's own logic already decided what should be
 * visible.
 *
 * <p>This skin suppresses the overflow computation entirely by overriding {@link #layoutChildren} without
 * calling {@code super.layoutChildren(...)} — that's the only place the overflow decision is made
 * ({@code organizeOverflow}/{@code addNodesToToolBar} in the JDK source), so skipping it means the decision
 * never runs, and the "&gt;&gt;" button is never shown.
 *
 * <p><b>Caveats</b> — this relies on undocumented internals of {@code ToolBarSkin} (verified against the
 * JavaFX 26 source; re-verify after any JDK upgrade):
 * <ul>
 *   <li>The skin's internal item container ({@code box}, exposed here only via the inherited, protected
 *       {@code getChildren()}) is assumed to be {@code getChildren().get(0)} — true as of this JDK version
 *       ({@code initialize()} adds {@code box} first, before {@code overflowBox} and {@code overflowMenu}),
 *       but not part of any public contract.
 *   <li>{@code ToolBarSkin}'s own {@code itemsListener} (registered in its constructor, before this
 *       subclass's constructor body runs) still fires on every {@code items} change and appends new items to
 *       the container in the wrong order for anything but simple trailing inserts. {@link #syncContainer}
 *       corrects the container's order immediately after, so the net result is correct, at the cost of doing
 *       the child-list sync twice per {@code items} change — negligible for toolbars with a handful of items.
 *   <li>Keyboard focus traversal between items (Tab/Shift+Tab) relies on {@code ParentTraversalEngine}
 *       wiring set up by the superclass against {@code box}'s children and the (permanently invisible)
 *       overflow menu; since the container stays correctly populated, traversal should keep working, but this
 *       hasn't been exhaustively tested against every {@code Direction} case.
 *   <li>Orientation changes ({@code toolBar.setOrientation(...)}) trigger the superclass's
 *       {@code initialize()}, which recreates {@code box} from scratch — {@link #getContainer} always looks
 *       it up fresh rather than caching, so this is handled correctly, but vertical orientation itself is
 *       untested with this skin (not needed for its current use).
 * </ul>
 *
* @author Pavel Castornii
 */
public class NoOverflowToolBarSkin extends ToolBarSkin {

    private final ListChangeListener<Node> containerSyncListener = change -> syncContainer();

    public NoOverflowToolBarSkin(ToolBar toolBar) {
        super(toolBar);
        // Keep the internal container's real children in the exact order of toolBar.getItems() ourselves.
        // ToolBarSkin's own itemsListener (already registered by super(toolBar)) only appends changed items
        // to the end of the container — the correct, order-preserving resync normally happens inside
        // organizeOverflow(), called from layoutChildren(), which we override below without calling super
        // and therefore never runs. This listener fires right after the superclass's own listener on the
        // same change, and corrects whatever order it left behind.
        toolBar.getItems().addListener(containerSyncListener);
        syncContainer(); // cover items already present at construction time
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // Deliberately does NOT call super.layoutChildren(x, y, w, h) — that's exactly where ToolBarSkin
        // computes what overflows into the '>>' menu (organizeOverflow/addNodesToToolBar). Skipping it means
        // the overflow decision never runs; syncContainer() (see constructor) takes over the item-ordering
        // role that super.layoutChildren() would otherwise have performed.
        getContainer().resizeRelocate(x, y, w, h);
    }

    @Override
    public void dispose() {
        var toolBar = getSkinnable();
        if (toolBar != null) {
            toolBar.getItems().removeListener(containerSyncListener);
        }
        super.dispose();
    }

    /**
     * Forces the container's children to exactly match {@code getSkinnable().getItems()}, in order. See the
     * class javadoc for why this is needed in addition to {@code ToolBarSkin}'s own item-change listener.
     */
    private void syncContainer() {
        getContainer().getChildren().setAll(getSkinnable().getItems());
    }

    /**
     * Returns {@code ToolBarSkin}'s internal item container — an {@code HBox} (or {@code VBox} for vertical
     * orientation) carrying the {@code "container"} style class. Looked up fresh on every call rather than
     * cached, since orientation changes cause the superclass to replace this node entirely.
     */
    private HBox getContainer() {
        return (HBox) getChildren().get(0);
    }
}
