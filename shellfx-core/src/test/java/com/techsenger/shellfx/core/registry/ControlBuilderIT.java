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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.ControlGroup;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link ControlBuilder}: a tool bar with two groups of buttons is built from the real
 * registries - once as it should be, and then with a hole in it, to check what is left out and how the report of
 * the build tells about it. The tree is
 * <pre>
 * ToolBar
 *     FileGroup (position 0): New (0), Open (100)
 *     EditGroup (position 100): Copy (0), Paste (100)
 * </pre>
 *
 * @author Pavel Castornii
 */
public class ControlBuilderIT {

    private static final ToolBarSlot<ParentView<?>> TOOL_BAR = new ToolBarSlot<>(ParentView.class, "ToolBar");

    private static final GroupSlot<ParentView<?>, Control> FILE_GROUP = new GroupSlot<>(ParentView.class, "File");

    private static final GroupSlot<ParentView<?>, Control> EDIT_GROUP = new GroupSlot<>(ParentView.class, "Edit");

    private static final GroupSlot<ParentView<?>, Control> ORPHAN_GROUP =
            new GroupSlot<>(ParentView.class, "Orphan");

    @BeforeAll
    static void startFxToolkit() {
        RegistryTestSupport.startFxToolkit();
    }

    private final SlotRegistry slotRegistry = new SlotRegistry();

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private final ParentView<?> view = RegistryTestSupport.createView();

    private final ControlBuilder builder = new ControlBuilder(slotRegistry, controlRegistry) {
        @Override
        SlotTree createSlotTree(ParentView<?> view) {
            var tree = super.createSlotTree(view);
            tree.getLogger().setEnabled(true);
            report = tree.getLogger();
            return tree;
        }
    };

    private SlotTreeLogger report;

    private Registration toolBarFactory;

    private Registration editGroupFactory;

    @Test
    void buildToolBar_twoGroups_controlsFollowPositionsWithVerticalSeparatorBetweenGroups() {
        registerSlots();
        registerControls();

        var toolBar = builder.buildToolBar(TOOL_BAR, view);

        assertThat(toolBar.getItems()).hasSize(5);
        assertThat(toolBar.getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("New", "Open", null, "Copy", "Paste");
        assertThat(toolBar.getItems().get(2)).isInstanceOf(Separator.class);
        assertThat(((Separator) toolBar.getItems().get(2)).getOrientation()).isEqualTo(Orientation.VERTICAL);
    }

    @Test
    void buildToolBar_correctTree_reportsTheBuiltTreeWithoutProblems() {
        registerSlots();
        registerControls();

        builder.buildToolBar(TOOL_BAR, view);

        assertThat(report.hasProblems()).isFalse();
        assertThat(report.describeProblems()).isEmpty();
        assertThat(report.toString()).isEqualTo(String.join(System.lineSeparator(),
                "Tool bar: ToolBar",
                "    Group: File, position: 0",
                "        Control: Button 'New', position: 0",
                "        Control: Button 'Open', position: 100",
                "    Group: Edit, position: 100",
                "        Control: Button 'Copy', position: 0",
                "        Control: Button 'Paste', position: 100"));
    }

    @Test
    void buildToolBar_groupWithoutFactory_groupLeftOutOfTheToolBar() {
        registerSlots();
        registerControls();
        editGroupFactory.unregister();

        var toolBar = builder.buildToolBar(TOOL_BAR, view);

        assertThat(toolBar.getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("New", "Open");
    }

    @Test
    void buildToolBar_groupWithoutFactory_builtTreeLacksItAndProblemsShowThePathToIt() {
        registerSlots();
        registerControls();
        editGroupFactory.unregister();

        builder.buildToolBar(TOOL_BAR, view);

        assertThat(report.toString()).doesNotContain("Edit", "Copy").contains("Group: File");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Tool bar: ToolBar",
                "    Group: Edit, position: 100, warning: no ControlGroup factory registered, "
                        + "its 2 controls are left out"));
    }

    @Test
    void buildToolBar_controlsWithSamePosition_bothBuiltAndTheSecondMarkedInTheProblems() {
        registerSlots();
        registerControls();
        controlRegistry.register(FILE_GROUP, 100, v -> new Button("Twin"));

        var toolBar = builder.buildToolBar(TOOL_BAR, view);

        assertThat(toolBar.getItems()).hasSize(6);
        assertThat(report.toString()).contains("Open", "Twin").doesNotContain("WARNING");
        assertThat(report.describeProblems())
                .contains("Tool bar: ToolBar", "Group: File, position: 0")
                .contains(", warning: shares the position 100 with the previous sibling")
                .doesNotContain("Group: Edit");
    }

    @Test
    void buildToolBar_controlsOfGroupPutNowhere_builtTreeLacksThemAndProblemsListTheGroup() {
        registerSlots();
        registerControls();
        controlRegistry.register(ORPHAN_GROUP, 0, v -> new Button("Lost"));

        var toolBar = builder.buildToolBar(TOOL_BAR, view);

        assertThat(toolBar.getItems()).hasSize(5);
        assertThat(report.toString()).doesNotContain("Orphan", "Lost");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Tool bar: ToolBar",
                "    Group: Orphan, warning: not put into any menu or tool bar, its 1 controls are never shown"));
    }

    @Test
    void buildToolBar_noGroups_returnsEmptyToolBar() {
        controlRegistry.register(TOOL_BAR, v -> new ToolBar());

        var toolBar = builder.buildToolBar(TOOL_BAR, view);

        assertThat(toolBar.getItems()).isEmpty();
    }

    @Test
    void buildToolBar_noToolBarFactory_throws() {
        registerSlots();
        registerControls();
        toolBarFactory.unregister();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildToolBar(TOOL_BAR, view))
                .withMessageContaining("ToolBar");
    }

    private void registerSlots() {
        slotRegistry.register(TOOL_BAR, 0, FILE_GROUP);
        slotRegistry.register(TOOL_BAR, 100, EDIT_GROUP);
    }

    private void registerControls() {
        toolBarFactory = controlRegistry.register(TOOL_BAR, v -> new ToolBar());
        controlRegistry.register(FILE_GROUP, v -> new ControlGroup<>());
        editGroupFactory = controlRegistry.register(EDIT_GROUP, v -> new ControlGroup<>());

        controlRegistry.register(FILE_GROUP, 0, v -> new Button("New"));
        controlRegistry.register(FILE_GROUP, 100, v -> new Button("Open"));
        controlRegistry.register(EDIT_GROUP, 0, v -> new Button("Copy"));
        controlRegistry.register(EDIT_GROUP, 100, v -> new Button("Paste"));
    }
}
