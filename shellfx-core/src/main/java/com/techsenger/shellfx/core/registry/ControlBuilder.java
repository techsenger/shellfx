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
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles a tool bar from the groups of controls put into its slot: the tool bar is the control registered for the
 * slot, its groups and controls follow the order of their positions and the groups are set apart with separators.
 * A group without a registered control is left out. For menus see {@link ManagedControlBuilder}. The tree that was
 * built, with the registered positions, is logged at debug level; what looks wrong in it - a group without a
 * factory, controls of a group that is put nowhere, equal positions - is logged at warning level as a second tree
 * with only the places that lead to the problems.
 *
 * @author Pavel Castornii
 */
public class ControlBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ControlBuilder.class);

    private final SlotRegistry slotRegistry;

    private final ControlRegistry controlRegistry;

    public ControlBuilder(SlotRegistry slotRegistry, ControlRegistry controlRegistry) {
        this.slotRegistry = slotRegistry;
        this.controlRegistry = controlRegistry;
    }

    /**
     * Builds the tool bar {@code toolBarSlot} stands for, with the controls of every group put into the slot. The
     * control registered for the slot is the {@link ToolBar} to fill; empty groups are left out, so a tool bar
     * without groups is returned empty.
     *
     * @param toolBarSlot the slot of the tool bar to build
     * @param view        the component view passed to each control factory; its class (and ancestors/interfaces)
     *     determines which registrations apply
     * @return the assembled tool bar
     * @throws IllegalStateException if no tool bar control is registered for the slot
     */
    public ToolBar buildToolBar(ToolBarSlot<?> toolBarSlot, ParentView<?> view) {
        var tree = createSlotTree(view);
        if (!(tree.createNode(toolBarSlot) instanceof ToolBar toolBar)) {
            throw new IllegalStateException("No tool bar control is registered for slot " + toolBarSlot.getText());
        }
        var groups = build(toolBarSlot, Node.class, tree);
        var separatorOrientation = toolBar.getOrientation() == Orientation.HORIZONTAL
                ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        for (var i = 0; i < groups.size(); i++) {
            if (i != 0) {
                toolBar.getItems().add(new Separator(separatorOrientation));
            }
            toolBar.getItems().addAll(groups.get(i).getItems());
        }
        return toolBar;
    }

    /**
     * Creates the tree of slots for a build; its report is on if the logger of the builder logs at warning level.
     */
    SlotTree createSlotTree(ParentView<?> view) {
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        tree.getLogger().setEnabled(logger.isWarnEnabled());
        return tree;
    }

    @SuppressWarnings("unchecked")
    private <C> List<ControlGroup<C>> build(ToolBarSlot<?> toolBar, Class<C> controlType, SlotTree tree) {
        var view = tree.getView();
        var result = new ArrayList<ControlGroup<C>>();
        var loggerRoot = tree.getLogger().add("Tool bar: " + toolBar.getText());
        for (var link : tree.getChildren(toolBar)) {
            var group = link.getChild();
            var leaves = tree.getLeaves(group);
            if (!(group instanceof GroupSlot<?, ?>) || leaves.isEmpty()) {
                continue;
            }
            var loggerGroup = loggerRoot.add("Group: " + group.getText(), link.getPosition());
            if (!(tree.createNode(group) instanceof ControlGroup<?> controlGroup)) {
                loggerGroup.skip("no ControlGroup factory registered, its " + leaves.size() + " controls are left out");
                continue;
            }
            var controls = new ArrayList<C>();
            for (var leaf : leaves) {
                var control = controlType.cast(leaf.create(view));
                controls.add(control);
                loggerGroup.add("Control: " + describe(control), leaf.getPosition());
            }
            var typedGroup = (ControlGroup<C>) controlGroup;
            typedGroup.getItems().setAll(controls);
            result.add(typedGroup);
        }
        tree.logOrphanGroups(loggerRoot);
        tree.getLogger().write(logger, "Controls");
        return result;
    }

    /**
     * Names a control by its type and the most telling text it has: its own text, or else its tooltip.
     */
    private String describe(Object control) {
        String text = null;
        if (control instanceof Labeled labeled) {
            text = labeled.getText();
        }
        if ((text == null || text.isEmpty()) && control instanceof Control c && c.getTooltip() != null) {
            text = c.getTooltip().getText();
        }
        var type = control.getClass().getSimpleName();
        return text == null || text.isEmpty() ? type : type + " '" + text + "'";
    }
}
