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
 * A group without a registered control is left out. For menus see {@link ManagedControlBuilder}. What was built,
 * with the registered positions, is logged at debug level.
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
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        if (!(tree.createNode(toolBarSlot) instanceof ToolBar toolBar)) {
            throw new IllegalStateException("No tool bar control is registered for slot " + toolBarSlot.getText());
        }
        var groups = build(toolBarSlot, Node.class, tree);
        for (var i = 0; i < groups.size(); i++) {
            if (i != 0) {
                toolBar.getItems().add(new Separator());
            }
            toolBar.getItems().addAll(groups.get(i).getItems());
        }
        return toolBar;
    }

    @SuppressWarnings("unchecked")
    private <C> List<ControlGroup<C>> build(ToolBarSlot<?> toolBar, Class<C> controlType, SlotTree tree) {
        var view = tree.getView();
        var result = new ArrayList<ControlGroup<C>>();
        var description = new StringBuilder();
        for (var link : tree.getChildren(toolBar)) {
            var group = link.getChild();
            if (!(group instanceof GroupSlot<?, ?>) || tree.getLeaves(group).isEmpty()
                    || !(tree.createNode(group) instanceof ControlGroup<?> controlGroup)) {
                continue;
            }
            description.append(System.lineSeparator()).append("    Group: ").append(group.getText());
            description.append(", position: ").append(link.getPosition());
            var controls = new ArrayList<C>();
            for (var leaf : tree.getLeaves(group)) {
                var control = controlType.cast(leaf.create(view));
                controls.add(control);
                description.append(System.lineSeparator()).append("        Control: ").append(describe(control));
                description.append(", position: ").append(leaf.getPosition());
            }
            var typedGroup = (ControlGroup<C>) controlGroup;
            typedGroup.getItems().setAll(controls);
            result.add(typedGroup);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Controls built for {}:{}Tool bar: {}{}", tree.getViewName(), System.lineSeparator(),
                    toolBar.getText(), description);
        }
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
