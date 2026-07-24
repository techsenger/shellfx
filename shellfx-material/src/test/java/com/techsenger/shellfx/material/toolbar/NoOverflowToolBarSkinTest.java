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


import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Guards the undocumented assumptions {@link NoOverflowToolBarSkin} makes about {@code ToolBarSkin}'s internal
 * structure. This is not a behavioral spec of {@code NoOverflowToolBarSkin} itself — it's a tripwire: if a JDK
 * upgrade changes {@code ToolBarSkin} in a way that breaks one of these assumptions, this test should fail
 * loudly here rather than surfacing as a silent layout glitch or {@code ClassCastException} deep in
 * production code. Re-run against every new JDK/JavaFX version before upgrading.
 * <p>
 * Deliberately never creates a {@code Stage}/{@code Scene} or calls {@code show()}: doing so triggers a real
 * paint pass, which on this environment crashes native GLX code (see project history) — and is unnecessary
 * here regardless, since every assertion below is structural (child list contents/order), not visual.
 * {@link Region#resize} / {@code layout()} work correctly without a live {@code Scene} as long as the JavaFX
 * toolkit has been started, which is all {@link #initJavaFxToolkit()} does.
 *
 * @author Pavel Castornii
 */
class NoOverflowToolBarSkinTest {

    @BeforeAll
    static void initJavaFxToolkit() {
        try {
            System.setProperty("glass.platform", "Headless");
            Platform.startup(() -> { });
        } catch (IllegalStateException alreadyStarted) {
            // toolkit already running in this JVM (e.g. started by another test class); nothing to do
        }
    }

    private ToolBar newSkinnedToolBar() {
        var toolBar = new ToolBar();
        toolBar.setSkin(new NoOverflowToolBarSkin(toolBar));
        return toolBar;
    }

    @Test
    void getChildren_afterSkinInit_firstChildIsHBoxWithContainerStyleClass() {
        var toolBar = newSkinnedToolBar();

        // ToolBarSkin.initialize() adds three children in a fixed order: box, then overflowBox (an
        // internal-only HBox that also carries the "container" style class via
        // Bindings.bindContent(overflowBox.getStyleClass(), box.getStyleClass()) — so two HBox.container
        // nodes existing in the tree is expected, not a sign the wrong one was picked up), then
        // overflowMenu. Index 0 is always the real "box" that ToolBarSkin actually lays children out into.
        assertThat(toolBar.getChildrenUnmodifiable())
                .as("ToolBarSkin.initialize() should have added at least the container as a child")
                .isNotEmpty();
        var first = toolBar.getChildrenUnmodifiable().get(0);
        assertThat(first)
                .as("Assumption broken: ToolBarSkin's first child is no longer an HBox on this JDK — "
                        + "NoOverflowToolBarSkin.getContainer() needs updating")
                .isInstanceOf(HBox.class);
        assertThat(first.getStyleClass())
                .as("Assumption broken: the container no longer carries the 'container' style class — "
                        + "AtlantaFX's '.tool-bar > .container > .button' hover CSS will silently stop matching")
                .contains("container");
    }

    @Test
    void layoutChildren_itemsOverflowWidth_overflowButtonStaysInvisibleAndUnmanaged() {
        var toolBar = newSkinnedToolBar();
        toolBar.resize(50, 30); // deliberately too narrow for the buttons below

        for (int i = 0; i < 20; i++) {
            toolBar.getItems().add(new Button("Item " + i));
        }
        toolBar.applyCss();
        toolBar.layout();

        // ToolBarSkin.initialize() always adds the overflow button as a child node — its mere presence in
        // the tree is normal internal structure, not a sign of overflow having triggered. The overflow
        // decision is expressed only through its visible/managed flags, which organizeOverflow() would flip
        // to true — but that method is never called here, since layoutChildren() below skips super().
        var overflowButton = toolBar.getChildrenUnmodifiable().stream()
                .filter(child ->
                        child instanceof StackPane && child.getStyleClass().contains("tool-bar-overflow-button"))
                .findFirst();
        assertThat(overflowButton)
                .as("Overflow button node should exist (ToolBarSkin always creates one) but stay hidden — "
                        + "NoOverflowToolBarSkin.layoutChildren() must never invoke the overflow computation "
                        + "that would make it visible/managed")
                .hasValueSatisfying(node -> {
                    assertThat(node.isVisible()).as("overflow button visible").isFalse();
                    assertThat(node.isManaged()).as("overflow button managed").isFalse();
                });
    }

    @Test
    void getItems_outOfOrderInsert_containerChildOrderMatches() {
        var toolBar = newSkinnedToolBar();
        toolBar.resize(400, 30);

        var a = new Button("a");
        var b = new Button("b");
        var c = new Button("c");
        toolBar.getItems().addAll(a, b);
        // Insert at the front, not the end — this is exactly the case where ToolBarSkin's own
        // itemsListener (append-only: box.getChildren().addAll(c.getAddedSubList())) would leave the
        // container out of order if NoOverflowToolBarSkin's own resync listener didn't correct it.
        toolBar.getItems().add(0, c);
        toolBar.applyCss();
        toolBar.layout();

        var container = (HBox) toolBar.getChildrenUnmodifiable().get(0);
        assertThat(container.getChildren())
                .as("Container child order diverged from items order — the resync listener in "
                        + "NoOverflowToolBarSkin's constructor did not correct the superclass's "
                        + "append-only itemsListener as expected")
                .containsExactly(c, a, b);
    }
}
