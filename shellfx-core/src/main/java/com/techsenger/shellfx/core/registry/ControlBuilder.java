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
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles the controls of the groups put into a tool bar slot without knowing anything about them beyond
 * their type: groups and controls come back in the order of their positions, so the caller only decides how to lay them
 * out (for example, with a separator between groups). For menus see {@link ManagedControlBuilder}. What was built,
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
     * Builds the controls of every group put directly into {@code toolBar}. Empty groups are left out.
     *
     * @param toolBar     the slot of the tool bar whose groups are built
     * @param controlType the type of controls the caller expects
     * @param view        the component view passed to each control factory; its class (and ancestors/interfaces)
     *     determines which registrations apply
     * @param <C>         the type of controls the caller expects
     * @return the non-empty groups sorted by position, each with its controls sorted by position
     */
    public <C> List<List<C>> build(ToolBarSlot<?> toolBar, Class<C> controlType, ParentView<?> view) {
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        var result = new ArrayList<List<C>>();
        var description = new StringBuilder();
        for (var link : tree.getChildren(toolBar)) {
            var group = link.getChild();
            if (!(group instanceof GroupSlot<?, ?>) || tree.getLeaves(group).isEmpty()) {
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
            result.add(controls);
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
