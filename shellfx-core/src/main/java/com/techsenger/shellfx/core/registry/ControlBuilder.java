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

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.ControlGroup;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.Slot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles the controls of a menu bar, a menu, a context menu or a tool bar from the slots put into the slot of the
 * root control. The root is the control registered for its slot ({@link MenuBar}, {@link Menu}, {@link ContextMenu},
 * {@link ToolBar}), the groups and the controls follow the order of their positions and the groups are set apart
 * with separators. A menu, a group or a tool bar without a registered provider does not make it into the result,
 * and neither does an empty menu or group. A build has two passes: the first plans what is going to be built, so
 * nothing that would be left out is created; the second creates a provider per control, initializes it and takes its
 * control. The providers are returned with the built root control, and deinitializing them is up to its owner.
 * The builder does not change the behavior of the controls: a menu hides itself and the separators around empty
 * groups when its control does so, as {@code DynamicMenu} and {@code DynamicContextMenu} do. The result is
 * reported in two trees, logged at debug and warning level: the built tree and the tree of the defects of the
 * registrations - a menu or a group without a provider, controls of a group that is put nowhere, equal positions -
 * with only the places that lead to them.
 *
 * @author Pavel Castornii
 */
public class ControlBuilder {

    /**
     * A control of a group or a nested menu of it, put at a position.
     */
    private record Candidate(int position, @Nullable LeafRegistration leaf, @Nullable Slot<?> menuSlot) {

    }

    private static final Logger logger = LoggerFactory.getLogger(ControlBuilder.class);

    private static final String MISSING_MENU = "no Menu provider registered, it is left out";

    private static final String MISSING_GROUP = "no ControlGroup provider registered, its content is left out";

    private final SlotRegistry slotRegistry;

    private final ControlRegistry controlRegistry;

    public ControlBuilder(SlotRegistry slotRegistry, ControlRegistry controlRegistry) {
        this.slotRegistry = slotRegistry;
        this.controlRegistry = controlRegistry;
    }

    /**
     * Builds the menu bar {@code menuBarSlot} stands for, with every menu put directly into the slot. The control
     * registered for the slot is the {@link MenuBar} to fill; empty menus are left out.
     *
     * @param view        the component view passed to each provider; its class (and ancestors/interfaces)
     *     determines which registrations apply
     * @param menuBarSlot the slot of the menu bar to build
     * @return the assembled menu bar, empty if none of its menus has any items, with the initialized providers of
     *     all its controls
     * @throws IllegalStateException if no menu bar control is registered for the slot
     */
    public <V extends ParentView<?>> Controls<V, MenuBar> buildMenuBar(V view,
            MenuBarSlot<? super V> menuBarSlot) {
        var tree = createSlotTree(view);
        if (!tree.hasNode(menuBarSlot)) {
            throw new IllegalStateException("No menu bar control is registered for slot " + menuBarSlot.getText());
        }
        var defects = tree.getLogger().addDefects("Menu bar: " + menuBarSlot.getText());
        var menus = new ArrayList<PlanNode>();
        for (var link : tree.getChildren(menuBarSlot)) {
            var menu = planMenu(link.getChild(), link.getPosition(), defects, tree);
            if (menu != null) {
                menus.add(menu);
            }
        }
        tree.logOrphanGroups(defects);
        var menuBar = (MenuBar) tree.createNode(menuBarSlot);
        var built = tree.getLogger().addBuilt("Menu bar: " + menuBarSlot.getText());
        menus.forEach(menu -> menuBar.getMenus().add(createMenu(menu, built, tree)));
        tree.getLogger().write(logger, "Menu bar");
        return new Controls<>(menuBar, tree.getProviders());
    }

    /**
     * Builds the menu {@code menuSlot} stands for, with all its groups.
     *
     * @param view     the component view passed to each provider
     * @param menuSlot the slot of the menu to build
     * @return the assembled {@link Menu}, without items if none of its groups has any, with the initialized
     *     providers of all its controls
     * @throws IllegalStateException if no menu control is registered for the slot
     */
    public <V extends ParentView<?>> Controls<V, Menu> buildMenu(V view, MenuSlot<? super V> menuSlot) {
        var tree = createSlotTree(view);
        if (!tree.hasNode(menuSlot)) {
            throw new IllegalStateException("No menu control is registered for slot " + menuSlot.getText());
        }
        var defects = tree.getLogger().addDefects("Menu: " + menuSlot.getText());
        var plan = planContainer(menuSlot, 0, defects, tree);
        tree.logOrphanGroups(defects);
        var menu = (Menu) tree.createNode(menuSlot);
        var built = tree.getLogger().addBuilt("Menu: " + describeMenuItem(menu));
        createMenuGroups(plan, menu.getItems(), built, tree);
        tree.getLogger().write(logger, "Menu");
        return new Controls<>(menu, tree.getProviders());
    }

    /**
     * Builds the context menu {@code contextMenuSlot} stands for, with all its groups. The control registered for
     * the slot is the {@link ContextMenu} to fill.
     *
     * @param view            the component view passed to each provider
     * @param contextMenuSlot the slot of the context menu to build
     * @return the assembled menu, without items if none of its groups has any, with the initialized providers of
     *     all its controls
     * @throws IllegalStateException if no context menu control is registered for the slot
     */
    public <V extends ParentView<?>> Controls<V, ContextMenu> buildContextMenu(V view,
            ContextMenuSlot<? super V> contextMenuSlot) {
        var tree = createSlotTree(view);
        if (!tree.hasNode(contextMenuSlot)) {
            throw new IllegalStateException("No context menu control is registered for slot "
                    + contextMenuSlot.getText());
        }
        var defects = tree.getLogger().addDefects("Context menu: " + contextMenuSlot.getText());
        var plan = planContainer(contextMenuSlot, 0, defects, tree);
        tree.logOrphanGroups(defects);
        var contextMenu = (ContextMenu) tree.createNode(contextMenuSlot);
        var built = tree.getLogger().addBuilt("Context menu: " + contextMenuSlot.getText());
        createMenuGroups(plan, contextMenu.getItems(), built, tree);
        tree.getLogger().write(logger, "Context menu");
        return new Controls<>(contextMenu, tree.getProviders());
    }

    /**
     * Builds the tool bar {@code toolBarSlot} stands for, with the controls of every group put into the slot. The
     * control registered for the slot is the {@link ToolBar} to fill; empty groups are left out, so a tool bar
     * without groups is returned empty.
     *
     * @param view        the component view passed to each provider; its class (and ancestors/interfaces)
     *     determines which registrations apply
     * @param toolBarSlot the slot of the tool bar to build
     * @return the assembled tool bar with the initialized providers of all its controls
     * @throws IllegalStateException if no tool bar control is registered for the slot
     */
    public <V extends ParentView<?>> Controls<V, ToolBar> buildToolBar(V view,
            ToolBarSlot<? super V> toolBarSlot) {
        var tree = createSlotTree(view);
        if (!tree.hasNode(toolBarSlot)) {
            throw new IllegalStateException("No tool bar control is registered for slot " + toolBarSlot.getText());
        }
        var defects = tree.getLogger().addDefects("Tool bar: " + toolBarSlot.getText());
        var groups = planToolBarGroups(toolBarSlot, defects, tree);
        tree.logOrphanGroups(defects);
        var toolBar = (ToolBar) tree.createNode(toolBarSlot);
        var built = tree.getLogger().addBuilt("Tool bar: " + toolBarSlot.getText());
        var separatorOrientation = toolBar.getOrientation() == Orientation.HORIZONTAL
                ? Orientation.VERTICAL : Orientation.HORIZONTAL;
        for (var i = 0; i < groups.size(); i++) {
            if (i != 0) {
                toolBar.getItems().add(new Separator(separatorOrientation));
            }
            toolBar.getItems().addAll(createToolBarGroup(groups.get(i), built, tree));
        }
        tree.getLogger().write(logger, "Controls");
        return new Controls<>(toolBar, tree.getProviders());
    }

    /**
     * Creates the tree of slots for a build; its report is on if the logger of the builder logs at warning level.
     */
    <V extends ParentView<?>> SlotTree<V> createSlotTree(V view) {
        var tree = new SlotTree<>(slotRegistry, controlRegistry, view);
        tree.getLogger().setEnabled(logger.isWarnEnabled());
        return tree;
    }

    /**
     * Plans the menu {@code slot} stands for and describes what is wrong with it under {@code defectsParent}.
     *
     * @return the plan, or {@code null} if the menu has no provider or nothing to build.
     */
    private @Nullable PlanNode planMenu(Slot<?> slot, int position, SlotTreeLogger.Node defectsParent,
            SlotTree<?> tree) {
        var defects = defectsParent.add("Menu: " + slot.getText(), position);
        if (!tree.hasNode(slot)) {
            defects.warn(MISSING_MENU);
            return null;
        }
        var plan = planContainer(slot, position, defects, tree);
        return plan.isEmpty() ? null : plan;
    }

    /**
     * Plans the groups put into {@code container}, in the order of their positions; empty groups are not planned.
     */
    private PlanNode planContainer(Slot<?> container, int position, SlotTreeLogger.Node defects, SlotTree<?> tree) {
        var plan = new PlanNode(container, position);
        for (var link : tree.getChildren(container)) {
            if (link.getChild() instanceof GroupSlot<?, ?> group) {
                var groupPlan = planGroup(group, link.getPosition(), defects, tree);
                if (groupPlan != null) {
                    plan.addNode(groupPlan);
                }
            }
        }
        return plan;
    }

    /**
     * Plans the group {@code group} stands for with the controls put into it and the nested menus of the group, in
     * the order of their positions, and describes what is wrong with it under {@code defectsParent}.
     *
     * @return the plan, or {@code null} if the group has no provider or nothing to build.
     */
    private @Nullable PlanNode planGroup(GroupSlot<?, ?> group, int position, SlotTreeLogger.Node defectsParent,
            SlotTree<?> tree) {
        var leaves = tree.getLeaves(group);
        var nestedLinks = tree.getChildren(group).stream()
                .filter(link -> !(link.getChild() instanceof GroupSlot<?, ?>)).toList();
        if (leaves.isEmpty() && nestedLinks.isEmpty()) {
            return null;
        }
        var defects = defectsParent.add("Group: " + group.getText(), position);
        if (!tree.hasNode(group)) {
            defects.warn(MISSING_GROUP);
            return null;
        }
        var candidates = new ArrayList<Candidate>();
        leaves.forEach(leaf -> candidates.add(new Candidate(leaf.getPosition(), leaf, null)));
        nestedLinks.forEach(link -> candidates.add(new Candidate(link.getPosition(), null, link.getChild())));
        candidates.sort(Comparator.comparingInt(Candidate::position));
        var plan = new PlanNode(group, position);
        for (var candidate : candidates) {
            var leaf = candidate.leaf();
            var menuSlot = candidate.menuSlot();
            if (leaf != null) {
                defects.add("Control", candidate.position());
                plan.addLeaf(leaf);
            } else if (menuSlot != null) {
                var menuPlan = planMenu(menuSlot, candidate.position(), defects, tree);
                if (menuPlan != null) {
                    plan.addNode(menuPlan);
                }
            }
        }
        return plan.isEmpty() ? null : plan;
    }

    /**
     * Creates the menu of {@code plan} with its groups and describes it under {@code builtParent}.
     */
    private Menu createMenu(PlanNode plan, SlotTreeLogger.Node builtParent, SlotTree<?> tree) {
        var menu = (Menu) tree.createNode(plan.getSlot());
        var built = builtParent.add("Menu: " + describeMenuItem(menu), plan.getPosition());
        createMenuGroups(plan, menu.getItems(), built, tree);
        return menu;
    }

    /**
     * Puts the groups of {@code container} into {@code target} in the order of their positions, with a separator
     * between the groups, and describes them under {@code built}.
     */
    private void createMenuGroups(PlanNode container, List<MenuItem> target, SlotTreeLogger.Node built,
            SlotTree<?> tree) {
        var groups = container.getNodes().stream().map(group -> createMenuGroup(group, built, tree)).toList();
        for (var i = 0; i < groups.size(); i++) {
            if (i != 0) {
                target.add(new SeparatorMenuItem());
            }
            target.addAll(groups.get(i));
        }
    }

    /**
     * Creates the group of {@code plan} with the controls put into it and the nested menus of the group, and
     * describes it under {@code builtParent}.
     *
     * @return the items of the group.
     */
    @SuppressWarnings("unchecked")
    private List<MenuItem> createMenuGroup(PlanNode plan, SlotTreeLogger.Node builtParent, SlotTree<?> tree) {
        var controlGroup = (ControlGroup<MenuItem>) tree.createNode(plan.getSlot());
        var built = builtParent.add("Group: " + plan.getSlot().getText(), plan.getPosition());
        var items = new ArrayList<MenuItem>();
        for (var entry : plan.getEntries()) {
            var leaf = entry.leaf();
            var node = entry.node();
            if (leaf != null) {
                var item = (MenuItem) tree.createLeaf(leaf);
                built.add("MenuItem: " + describeMenuItem(item), entry.position(),
                        item.getAccelerator() == null ? null : "hotkey: " + item.getAccelerator());
                items.add(item);
            } else if (node != null) {
                items.add(createMenu(node, built, tree));
            }
        }
        controlGroup.getItems().setAll(items);
        return items;
    }

    private String describeMenuItem(MenuItem item) {
        return String.valueOf(item.getText()).replace("_", "");
    }

    /**
     * Plans the groups put into {@code toolBar}, in the order of their positions, and describes what is wrong with
     * them under {@code defects}; a group without controls or without a provider is not planned.
     */
    private List<PlanNode> planToolBarGroups(ToolBarSlot<?> toolBar, SlotTreeLogger.Node defects, SlotTree<?> tree) {
        var result = new ArrayList<PlanNode>();
        for (var link : tree.getChildren(toolBar)) {
            var group = link.getChild();
            var leaves = tree.getLeaves(group);
            if (!(group instanceof GroupSlot<?, ?>) || leaves.isEmpty()) {
                continue;
            }
            var defectsGroup = defects.add("Group: " + group.getText(), link.getPosition());
            if (!tree.hasNode(group)) {
                defectsGroup.warn("no ControlGroup provider registered, its " + leaves.size()
                        + " controls are left out");
                continue;
            }
            var plan = new PlanNode(group, link.getPosition());
            for (var leaf : leaves) {
                defectsGroup.add("Control", leaf.getPosition());
                plan.addLeaf(leaf);
            }
            result.add(plan);
        }
        return result;
    }

    /**
     * Creates the group of {@code plan} with the controls put into it and describes it under {@code builtParent}.
     *
     * @return the controls of the group.
     */
    @SuppressWarnings("unchecked")
    private List<Node> createToolBarGroup(PlanNode plan, SlotTreeLogger.Node builtParent, SlotTree<?> tree) {
        var controlGroup = (ControlGroup<Node>) tree.createNode(plan.getSlot());
        var built = builtParent.add("Group: " + plan.getSlot().getText(), plan.getPosition());
        var controls = new ArrayList<Node>();
        for (var entry : plan.getEntries()) {
            var leaf = entry.leaf();
            if (leaf != null) {
                var control = (Node) tree.createLeaf(leaf);
                controls.add(control);
                built.add("Control: " + describe(control), entry.position());
            }
        }
        controlGroup.getItems().setAll(controls);
        return controls;
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
