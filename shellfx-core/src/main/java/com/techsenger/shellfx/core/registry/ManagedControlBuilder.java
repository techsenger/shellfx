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
 * does not make it into the result, and neither does an empty menu or group. What was built, with the registered
 * positions, is logged at debug level.
 *
 * @author Pavel Castornii
 */
public class ManagedControlBuilder {

    private static final class Element {

        private final MenuItem item;

        private final int position;

        private final String description;

        Element(MenuItem item, int position, String description) {
            this.item = item;
            this.position = position;
            this.description = description;
        }
    }

    private static final class Segment {

        private final List<MenuItem> items;

        private final String description;

        Segment(List<MenuItem> items, String description) {
            this.items = items;
            this.description = description;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ManagedControlBuilder.class);

    private static final String TAB = "    ";

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
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        if (!(tree.createNode(menuBarSlot) instanceof MenuBar menuBar)) {
            throw new IllegalStateException("No menu bar control is registered for slot " + menuBarSlot.getText());
        }
        var description = new StringBuilder();
        for (var link : tree.getChildren(menuBarSlot)) {
            if (tree.createNode(link.getChild()) instanceof Menu menu) {
                var segment = buildMenu(link.getChild(), menu, link.getPosition(), 1, tree);
                if (segment != null) {
                    menuBar.getMenus().add(menu);
                    description.append(segment.description);
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Menu bar built for {}:{}Menu bar: {}{}", tree.getViewName(), System.lineSeparator(),
                    menuBarSlot.getText(), description);
        }
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
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        if (!(tree.createNode(menuSlot) instanceof Menu menu)) {
            throw new IllegalStateException("No menu control is registered for slot " + menuSlot.getText());
        }
        var segment = buildMenu(menuSlot, menu, 0, 0, tree);
        if (segment != null && logger.isDebugEnabled()) {
            logger.debug("Menu built for {}:{}", tree.getViewName(), segment.description);
        }
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
        var tree = new SlotTree(slotRegistry, controlRegistry, view);
        if (!(tree.createNode(contextMenuSlot) instanceof ContextMenu contextMenu)) {
            throw new IllegalStateException("No context menu control is registered for slot "
                    + contextMenuSlot.getText());
        }
        var description = assemble(contextMenuSlot, contextMenu.getItems(), 1, tree);
        if (description != null && logger.isDebugEnabled()) {
            logger.debug("Context menu built for {}:{}Context menu: {}{}", tree.getViewName(),
                    System.lineSeparator(), contextMenuSlot.getText(), description);
        }
        return contextMenu;
    }

    /**
     * Fills {@code menu} with the groups put into {@code slot}, in the order of their positions.
     *
     * @return the menu with its description, or {@code null} if the menu ended up empty.
     */
    private @Nullable Segment buildMenu(Slot<?> slot, Menu menu, int position, int depth,
            SlotTree tree) {
        var childrenDescription = assemble(slot, menu.getItems(), depth + 1, tree);
        if (childrenDescription == null) {
            return null;
        }
        var description = System.lineSeparator() + TAB.repeat(depth) + "Menu: "
                + String.valueOf(menu.getText()).replace("_", "") + ", position: " + position + childrenDescription;
        return new Segment(List.of(menu), description);
    }

    /**
     * Puts the groups of {@code container} into {@code target} in the order of their positions, with a separator
     * between the groups; empty groups are left out.
     *
     * @return the description of the groups, or {@code null} if there was nothing to put into {@code target}.
     */
    private @Nullable String assemble(Slot<?> container, List<MenuItem> target, int depth,
            SlotTree tree) {
        var segments = new ArrayList<Segment>();
        var description = new StringBuilder();
        for (var link : tree.getChildren(container)) {
            if (link.getChild() instanceof GroupSlot<?, ?> group) {
                var segment = buildGroup(group, link.getPosition(), depth, tree);
                if (segment != null) {
                    segments.add(segment);
                    description.append(segment.description);
                }
            }
        }
        if (segments.isEmpty()) {
            return null;
        }
        for (var i = 0; i < segments.size(); i++) {
            if (i != 0) {
                target.add(new SeparatorMenuItem());
            }
            target.addAll(segments.get(i).items);
        }
        return description.toString();
    }

    /**
     * Fills the group {@code group} stands for with the controls put into it and the nested menus of the group, in
     * the order of their positions.
     *
     * @return the group's items with its description, or {@code null} if no control is registered for the group
     *     or it ended up empty.
     */
    @SuppressWarnings("unchecked")
    private @Nullable Segment buildGroup(GroupSlot<?, ?> group, int position, int depth, SlotTree tree) {
        if (!(tree.createNode(group) instanceof ControlGroup<?> controlGroup)) {
            return null;
        }
        var elements = new ArrayList<Element>();
        for (var leaf : tree.getLeaves(group)) {
            var item = (MenuItem) leaf.create(tree.getView());
            var description = System.lineSeparator() + TAB.repeat(depth + 1) + "MenuItem: "
                    + String.valueOf(item.getText()).replace("_", "") + ", position: " + leaf.getPosition()
                    + (item.getAccelerator() == null ? "" : ", hotkey: " + item.getAccelerator());
            elements.add(new Element(item, leaf.getPosition(), description));
        }
        for (var link : tree.getChildren(group)) {
            if (tree.createNode(link.getChild()) instanceof Menu nested) {
                var segment = buildMenu(link.getChild(), nested, link.getPosition(), depth + 1, tree);
                if (segment != null) {
                    elements.add(new Element(nested, link.getPosition(), segment.description));
                }
            }
        }
        if (elements.isEmpty()) {
            return null;
        }
        elements.sort(Comparator.comparingInt(e -> e.position));
        var items = new ArrayList<MenuItem>();
        var description = new StringBuilder(System.lineSeparator() + TAB.repeat(depth) + "Group: " + group.getText()
                + ", position: " + position);
        for (var element : elements) {
            items.add(element.item);
            description.append(element.description);
        }
        ((ControlGroup<MenuItem>) controlGroup).getItems().setAll(items);
        return new Segment(items, description.toString());
    }
}
