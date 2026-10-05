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
import com.techsenger.shellfx.material.menu.ContextMenuHandler;
import com.techsenger.shellfx.material.menu.MenuHandler;
import com.techsenger.shellfx.material.menu.MenuItemHandler;
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.Slot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assembles the menu tree from the slots of a menu bar, a menu or a context menu: the menus put into a
 * slot are {@link Menu}s, a context menu is a {@link ContextMenu}, the groups hold menu items and
 * nested menus, and the groups are set apart from each other with separators. A menu without a registered control
 * does not make it into the result, and neither does an empty menu or group. The tree that was built, with the
 * registered positions, is logged at debug level; what looks wrong in it - a menu or a group without a factory, a
 * menu item without a handler, controls of a group that is put nowhere, equal positions - is logged at warning
 * level as a second tree with only the places that lead to the problems.
 *
 * @author Pavel Castornii
 */
public class ManagedControlBuilder {

    /**
     * A control of a group or a nested menu of it, put at a position.
     */
    private record Entry(int position, @Nullable LeafRegistration leaf, @Nullable Slot<?> menuSlot) {

    }

    private static final Logger logger = LoggerFactory.getLogger(ManagedControlBuilder.class);

    private static final String MISSING_MENU = "no Menu factory registered, it is left out";

    private final SlotRegistry slotRegistry;

    private final ControlRegistry controlRegistry;

    public ManagedControlBuilder(SlotRegistry slotRegistry, ControlRegistry controlRegistry) {
        this.slotRegistry = slotRegistry;
        this.controlRegistry = controlRegistry;
    }

    /**
     * Builds the menu bar {@code menuBarSlot} stands for, with every menu put directly into the slot. The control
     * registered for the slot is the {@link MenuBar} to fill; empty menus are left out.
     *
     * @param menuBarSlot the slot of the menu bar to build
     * @param view        the component view passed to each control factory; its class (and ancestors/interfaces)
     *     determines which registrations apply
     * @return the assembled menu bar, empty if none of its menus has any items
     * @throws IllegalStateException if no menu bar control is registered for the slot
     */
    public MenuBar buildMenuBar(MenuBarSlot<?> menuBarSlot, ParentView<?> view) {
        var tree = createSlotTree(view);
        if (!(tree.createNode(menuBarSlot) instanceof MenuBar menuBar)) {
            throw new IllegalStateException("No menu bar control is registered for slot " + menuBarSlot.getText());
        }
        var loggerRoot = tree.getLogger().add("Menu bar: " + menuBarSlot.getText());
        for (var link : tree.getChildren(menuBarSlot)) {
            var child = link.getChild();
            if (tree.createNode(child) instanceof Menu menu) {
                if (buildMenu(child, menu, link.getPosition(), loggerRoot, tree)) {
                    menuBar.getMenus().add(menu);
                }
            } else {
                loggerRoot.add("Menu: " + child.getText(), link.getPosition()).skip(MISSING_MENU);
            }
        }
        tree.logOrphanGroups(loggerRoot);
        tree.getLogger().write(logger, "Menu bar");
        return menuBar;
    }

    /**
     * Builds the menu {@code menuSlot} stands for, with all its groups.
     *
     * @param menuSlot the slot of the menu to build
     * @param view      the component view passed to each control factory
     * @return the assembled {@link Menu}, without items if none of its groups has any
     * @throws IllegalStateException if no menu control is registered for the slot
     */
    public Menu buildMenu(MenuSlot<?> menuSlot, ParentView<?> view) {
        var tree = createSlotTree(view);
        if (!(tree.createNode(menuSlot) instanceof Menu menu)) {
            throw new IllegalStateException("No menu control is registered for slot " + menuSlot.getText());
        }
        var loggerRoot = tree.getLogger().add("Menu: " + describe(menu));
        warnIfNoHandler(menu, loggerRoot);
        assemble(menuSlot, menu.getItems(), loggerRoot, tree);
        tree.logOrphanGroups(loggerRoot);
        tree.getLogger().write(logger, "Menu");
        return menu;
    }

    /**
     * Builds the context menu {@code contextMenuSlot} stands for, with all its groups. The control registered for
     * the slot is the {@link ContextMenu} to fill; attach a {@link ContextMenuHandler} to it in its factory
     * if whether to show the popup at all depends on something other than which items it ended up with.
     *
     * @param contextMenuSlot the slot of the context menu to build
     * @param view             the component view passed to each control factory
     * @return the assembled menu, without items if none of its groups has any
     * @throws IllegalStateException if no context menu control is registered for the slot
     */
    public ContextMenu buildContextMenu(ContextMenuSlot<?> contextMenuSlot,
            ParentView<?> view) {
        var tree = createSlotTree(view);
        if (!(tree.createNode(contextMenuSlot) instanceof ContextMenu contextMenu)) {
            throw new IllegalStateException("No context menu control is registered for slot "
                    + contextMenuSlot.getText());
        }
        var loggerRoot = tree.getLogger().add("Context menu: " + contextMenuSlot.getText());
        assemble(contextMenuSlot, contextMenu.getItems(), loggerRoot, tree);
        tree.logOrphanGroups(loggerRoot);
        tree.getLogger().write(logger, "Context menu");
        return contextMenu;
    }

    /**
     * Creates the tree of slots for a build; its report is on if the logger of the builder logs at warning level.
     */
    SlotTree createSlotTree(ParentView<?> view) {
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        tree.getLogger().setEnabled(logger.isWarnEnabled());
        return tree;
    }

    /**
     * Fills {@code menu} with the groups put into {@code slot} and describes it under {@code loggerParent}; a menu that
     * has nothing to describe is not described at all.
     *
     * @return whether the menu got items.
     */
    private boolean buildMenu(Slot<?> slot, Menu menu, int position, SlotTreeLogger.Node loggerParent, SlotTree tree) {
        var loggerNode = loggerParent.add("Menu: " + describe(menu), position);
        warnIfNoHandler(menu, loggerNode);
        assemble(slot, menu.getItems(), loggerNode, tree);
        var built = !menu.getItems().isEmpty();
        loggerParent.settle(loggerNode, built);
        return built;
    }

    /**
     * Puts the groups of {@code container} into {@code target} in the order of their positions, with a separator
     * between the groups, and describes them under {@code loggerParent}; empty groups are left out.
     */
    private void assemble(Slot<?> container, List<MenuItem> target, SlotTreeLogger.Node loggerParent, SlotTree tree) {
        var groups = new ArrayList<List<MenuItem>>();
        for (var link : tree.getChildren(container)) {
            if (link.getChild() instanceof GroupSlot<?, ?> group) {
                var items = buildGroup(group, link.getPosition(), loggerParent, tree);
                if (!items.isEmpty()) {
                    groups.add(items);
                }
            }
        }
        for (var i = 0; i < groups.size(); i++) {
            if (i != 0) {
                target.add(new SeparatorMenuItem());
            }
            target.addAll(groups.get(i));
        }
    }

    /**
     * Fills the group {@code group} stands for with the controls put into it and the nested menus of the group, in
     * the order of their positions, and describes it under {@code loggerParent}; a group that has nothing to
     * describe is not described at all.
     *
     * @return the items of the group, none if the group has no factory or ended up empty.
     */
    @SuppressWarnings("unchecked")
    private List<MenuItem> buildGroup(GroupSlot<?, ?> group, int position, SlotTreeLogger.Node loggerParent,
            SlotTree tree) {
        var leaves = tree.getLeaves(group);
        var nestedLinks = tree.getChildren(group).stream()
                .filter(link -> !(link.getChild() instanceof GroupSlot<?, ?>)).toList();
        if (leaves.isEmpty() && nestedLinks.isEmpty()) {
            return List.of();
        }
        var loggerNode = loggerParent.add("Group: " + group.getText(), position);
        if (!(tree.createNode(group) instanceof ControlGroup<?> controlGroup)) {
            loggerNode.skip("no ControlGroup factory registered, its content is left out");
            return List.of();
        }
        var entries = new ArrayList<Entry>();
        leaves.forEach(leaf -> entries.add(new Entry(leaf.getPosition(), leaf, null)));
        nestedLinks.forEach(link -> entries.add(new Entry(link.getPosition(), null, link.getChild())));
        entries.sort(Comparator.comparingInt(e -> e.position));
        var items = new ArrayList<MenuItem>();
        for (var entry : entries) {
            if (entry.leaf != null) {
                var item = (MenuItem) entry.leaf.create(tree.getView());
                var loggerItem = loggerNode.add("MenuItem: " + describe(item), entry.position,
                        item.getAccelerator() == null ? null : "hotkey: " + item.getAccelerator());
                if (!(item instanceof Menu) && !(item instanceof SeparatorMenuItem)
                        && MenuItemHandler.getHandler(item) == null) {
                    loggerItem.warn("no handler");
                }
                items.add(item);
            } else if (entry.menuSlot != null) {
                if (tree.createNode(entry.menuSlot) instanceof Menu nested) {
                    if (buildMenu(entry.menuSlot, nested, entry.position, loggerNode, tree)) {
                        items.add(nested);
                    }
                } else {
                    loggerNode.add("Menu: " + entry.menuSlot.getText(), entry.position).skip(MISSING_MENU);
                }
            }
        }
        loggerParent.settle(loggerNode, !items.isEmpty());
        ((ControlGroup<MenuItem>) controlGroup).getItems().setAll(items);
        return items;
    }

    /**
     * Notes a menu without a handler: it has no logic of its own to show or hide it, so its visibility is
     * determined by walking its items.
     */
    private void warnIfNoHandler(Menu menu, SlotTreeLogger.Node loggerNode) {
        if (MenuHandler.getHandler(menu) == null) {
            loggerNode.warn("no handler, its visibility is determined by walking its items");
        }
    }

    private String describe(MenuItem item) {
        return String.valueOf(item.getText()).replace("_", "");
    }
}
