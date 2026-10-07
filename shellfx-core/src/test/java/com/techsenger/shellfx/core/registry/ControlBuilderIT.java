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
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import com.techsenger.shellfx.material.slot.Slot;
import com.techsenger.shellfx.material.slot.ToolBarSlot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import static com.techsenger.shellfx.core.registry.RegistryTestSupport.provider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link ControlBuilder}: a menu bar and a tool bar are built from the real registries - once
 * as they should be, and then with a hole in them, to check what is left out and how the report of the build tells
 * about it. The menu tree is
 * <pre>
 * MainMenu
 *     File (position 0)
 *         FileGroup: New (0), Open (100), Recent (200) with RecentGroup: a.txt (0), b.txt (100)
 *         ExitGroup: Exit (0)
 *     Edit (position 100)
 *         EditGroup: Copy (0), Paste (100)
 * </pre>
 * and the tool bar tree is
 * <pre>
 * ToolBar
 *     FileGroup (position 0): New (0), Open (100)
 *     EditGroup (position 100): Copy (0), Paste (100)
 * </pre>
 *
 * @author Pavel Castornii
 */
public class ControlBuilderIT {

    /**
     * The slots of the menu bar tree.
     */
    private static final class MenuSlots {

        private static final MenuBarSlot<ParentView<?>> MAIN_MENU = new MenuBarSlot<>(ParentView.class, "MainMenu");

        private static final MenuSlot<ParentView<?>> FILE_MENU = new MenuSlot<>(ParentView.class, "File");

        private static final MenuSlot<ParentView<?>> EDIT_MENU = new MenuSlot<>(ParentView.class, "Edit");

        private static final MenuSlot<ParentView<?>> RECENT_MENU = new MenuSlot<>(ParentView.class, "Recent");

        private static final GroupSlot<ParentView<?>, MenuItem> FILE_GROUP =
                new GroupSlot<>(ParentView.class, "File");

        private static final GroupSlot<ParentView<?>, MenuItem> EXIT_GROUP =
                new GroupSlot<>(ParentView.class, "Exit");

        private static final GroupSlot<ParentView<?>, MenuItem> RECENT_GROUP =
                new GroupSlot<>(ParentView.class, "Recent");

        private static final GroupSlot<ParentView<?>, MenuItem> EDIT_GROUP =
                new GroupSlot<>(ParentView.class, "Edit");

        private static final GroupSlot<ParentView<?>, MenuItem> ORPHAN_GROUP =
                new GroupSlot<>(ParentView.class, "Orphan");

        private static final ContextMenuSlot<ParentView<?>> POPUP = new ContextMenuSlot<>(ParentView.class, "Popup");

        private static final GroupSlot<ParentView<?>, MenuItem> POPUP_GROUP =
                new GroupSlot<>(ParentView.class, "Popup");
    }

    /**
     * The slots of the tool bar tree.
     */
    private static final class ToolBarSlots {

        private static final ToolBarSlot<ParentView<?>> TOOL_BAR = new ToolBarSlot<>(ParentView.class, "ToolBar");

        private static final GroupSlot<ParentView<?>, Control> FILE_GROUP =
                new GroupSlot<>(ParentView.class, "File");

        private static final GroupSlot<ParentView<?>, Control> EDIT_GROUP =
                new GroupSlot<>(ParentView.class, "Edit");

        private static final GroupSlot<ParentView<?>, Control> ORPHAN_GROUP =
                new GroupSlot<>(ParentView.class, "Orphan");
    }

    private static List<String> texts(List<MenuItem> items) {
        return items.stream().map(MenuItem::getText).toList();
    }

    @BeforeAll
    static void startFxToolkit() {
        RegistryTestSupport.startFxToolkit();
    }

    private final SlotRegistry slotRegistry = new SlotRegistry();

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private final ParentView<?> view = RegistryTestSupport.createView();

    private final ControlBuilder builder = new ControlBuilder(slotRegistry, controlRegistry) {
        @Override
        <V extends ParentView<?>> SlotTree<V> createSlotTree(V view, Predicate<? super Slot<?>> slotFilter) {
            var tree = super.createSlotTree(view, slotFilter);
            tree.getLogger().setEnabled(true);
            report = tree.getLogger();
            return tree;
        }
    };

    private SlotTreeLogger report;

    private Registration menuBarFactory;

    private Registration editMenuFactory;

    private Registration recentMenuFactory;

    private Registration menuEditGroupFactory;

    private Registration toolBarFactory;

    private Registration toolBarEditGroupFactory;

    @Test
    void buildMenuBar_twoMenusWithGroupsAndSubmenu_buildsWholeTree() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
        var file = menuBar.getMenus().get(0);
        assertThat(texts(file.getItems())).containsExactly("New", "Open", "Recent", null, "Exit");
        assertThat(file.getItems().get(2)).isInstanceOf(Menu.class);
        assertThat(file.getItems().get(3)).isInstanceOf(SeparatorMenuItem.class);
        var recent = (Menu) file.getItems().get(2);
        assertThat(texts(recent.getItems())).containsExactly("a.txt", "b.txt");
        assertThat(texts(menuBar.getMenus().get(1).getItems())).containsExactly("Copy", "Paste");
    }

    @Test
    void buildMenuBar_correctTree_reportsTheBuiltTreeWithoutProblems() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU);

        assertThat(report.hasDefects()).isFalse();
        assertThat(report.hasWarnings()).isFalse();
        assertThat(report.describeDefects()).isEmpty();
        assertThat(report.toString()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: File, position: 0",
                "        Group: File, position: 0",
                "            MenuItem: New, position: 0",
                "            MenuItem: Open, position: 100",
                "            Menu: Recent, position: 200",
                "                Group: Recent, position: 0",
                "                    MenuItem: a.txt, position: 0",
                "                    MenuItem: b.txt, position: 100",
                "        Group: Exit, position: 100",
                "            MenuItem: Exit, position: 0",
                "    Menu: Edit, position: 100",
                "        Group: Edit, position: 0",
                "            MenuItem: Copy, position: 0",
                "            MenuItem: Paste, position: 100"));
    }

    @Test
    void buildMenuBar_submenuWithoutFactory_submenuLeftOutOfTheMenu() {
        registerMenuSlots();
        registerMenuControls();
        recentMenuFactory.unregister();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU).root();

        assertThat(texts(menuBar.getMenus().get(0).getItems())).containsExactly("New", "Open", null, "Exit");
        assertThat(texts(menuBar.getMenus().get(1).getItems())).containsExactly("Copy", "Paste");
    }

    @Test
    void buildMenuBar_submenuWithoutFactory_builtTreeLacksItAndProblemsShowThePathToIt() {
        registerMenuSlots();
        registerMenuControls();
        recentMenuFactory.unregister();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU);

        assertThat(report.toString()).doesNotContain("Recent").contains("Menu: File", "Menu: Edit");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: File, position: 0",
                "        Group: File, position: 0",
                "            Menu: Recent, position: 200, warning: no Menu provider registered, it is left out"));
    }

    @Test
    void buildMenuBar_groupWithoutFactory_menuWithoutItemsLeftOutOfTheMenuBar() {
        registerMenuSlots();
        registerMenuControls();
        menuEditGroupFactory.unregister();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File");
    }

    @Test
    void buildMenuBar_groupWithoutFactory_builtTreeLacksTheMenuAndProblemsShowThePathToTheGroup() {
        registerMenuSlots();
        registerMenuControls();
        menuEditGroupFactory.unregister();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU);

        assertThat(report.toString()).doesNotContain("Edit", "Copy");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: Edit, position: 100",
                "        Group: Edit, position: 0, warning: no ControlGroup provider registered, "
                        + "its content is left out"));
    }

    @Test
    void buildMenuBar_itemsWithSamePosition_bothBuiltAndTheSecondMarkedInTheProblems() {
        registerMenuSlots();
        registerMenuControls();
        controlRegistry.register(MenuSlots.FILE_GROUP, 100, provider(v -> RegistryTestSupport.createItem("Twin")));

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU).root();

        assertThat(texts(menuBar.getMenus().get(0).getItems())).contains("Open", "Twin");
        assertThat(report.toString()).contains("Open", "Twin");
        assertThat(report.describeDefects())
                .contains("Menu: File, position: 0", "Group: File, position: 0")
                .contains(", warning: shares the position 100 with the previous sibling")
                .doesNotContain("Menu: Edit");
    }

    @Test
    void buildMenuBar_controlsOfGroupPutNowhere_builtTreeLacksThemAndProblemsListTheGroup() {
        registerMenuSlots();
        registerMenuControls();
        controlRegistry.register(MenuSlots.ORPHAN_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Lost")));

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
        assertThat(report.toString()).doesNotContain("Orphan", "Lost");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Group: Orphan, warning: not put into any menu or tool bar, its 1 controls are never shown"));
    }

    @Test
    void buildMenuBar_noMenuBarFactory_throws() {
        registerMenuSlots();
        registerMenuControls();
        menuBarFactory.unregister();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildMenuBar(view, MenuSlots.MAIN_MENU))
                .withMessageContaining("MainMenu");
    }

    @Test
    void buildMenuBar_filterAcceptsAll_sameResultAsWithoutFilter() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> true).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
        assertThat(texts(menuBar.getMenus().get(0).getItems()))
                .containsExactly("New", "Open", "Recent", null, "Exit");
        assertThat(texts(menuBar.getMenus().get(1).getItems())).containsExactly("Copy", "Paste");
    }

    @Test
    void buildMenuBar_filterRejectsMenu_menuLeftOutWithItsContent() {
        registerMenuSlots();
        registerMenuControls();

        var controls = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EDIT_MENU);

        assertThat(controls.root().getMenus()).extracting(Menu::getText).containsExactly("File");
        assertThat(controls.providers()).noneMatch(p -> p.getSlot() == MenuSlots.EDIT_MENU
                || p.getSlot() == MenuSlots.EDIT_GROUP);
        assertThat(report.toString()).doesNotContain("Edit", "Copy", "Paste");
    }

    @Test
    void buildMenuBar_filterRejectsMenu_noProblemsReportedForLeftOutContent() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EDIT_MENU);

        assertThat(report.hasDefects()).isFalse();
        assertThat(report.describeDefects()).isEmpty();
    }

    @Test
    void buildMenuBar_filterRejectsGroup_groupAndItsSeparatorLeftOut() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EXIT_GROUP).root();

        assertThat(texts(menuBar.getMenus().get(0).getItems())).containsExactly("New", "Open", "Recent");
    }

    @Test
    void buildMenuBar_filterRejectsOnlyGroupOfMenu_emptyMenuLeftOut() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EDIT_GROUP).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File");
    }

    @Test
    void buildMenuBar_filterRejectsNestedMenu_submenuLeftOutOfTheGroup() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.RECENT_MENU).root();

        assertThat(texts(menuBar.getMenus().get(0).getItems())).containsExactly("New", "Open", null, "Exit");
        assertThat(report.toString()).doesNotContain("Recent", "a.txt");
    }

    @Test
    void buildMenuBar_filterRejectsEveryMenu_returnsEmptyMenuBar() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> false).root();

        assertThat(menuBar.getMenus()).isEmpty();
    }

    @Test
    void buildMenuBar_filterRejectsRoot_rootStillBuilt() {
        registerMenuSlots();
        registerMenuControls();

        var menuBar = builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.MAIN_MENU).root();

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
    }

    @Test
    void buildMenuBar_filterGetsEveryPlacedSlotOnce_rootNotAmongThem() {
        registerMenuSlots();
        registerMenuControls();
        var tested = new ArrayList<Slot<?>>();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> tested.add(slot));

        assertThat(tested).doesNotContain(MenuSlots.MAIN_MENU)
                .containsExactlyInAnyOrder(MenuSlots.FILE_MENU, MenuSlots.EDIT_MENU, MenuSlots.FILE_GROUP,
                        MenuSlots.EXIT_GROUP, MenuSlots.RECENT_MENU, MenuSlots.RECENT_GROUP, MenuSlots.EDIT_GROUP);
    }

    @Test
    void buildMenuBar_filterRejectsGroupWithControls_groupNotReportedAsOrphan() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EXIT_GROUP);

        assertThat(report.hasDefects()).isFalse();
        assertThat(report.describeDefects()).isEmpty();
    }

    @Test
    void buildMenuBar_filterRejectsMenu_filteredOutSlotsListed() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> slot != MenuSlots.EDIT_MENU);

        assertThat(report.describeFilteredOut()).isEqualTo("Filtered out slots: MenuSlot: Edit");
    }

    @Test
    void buildMenuBar_filterRejectsSlotsOnDifferentLevels_allListed() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU,
                slot -> slot != MenuSlots.EDIT_MENU && slot != MenuSlots.EXIT_GROUP);

        assertThat(report.describeFilteredOut()).startsWith("Filtered out slots: ")
                .contains("MenuSlot: Edit", "GroupSlot: Exit", ", ");
    }

    @Test
    void buildMenuBar_filterRejectsMenuAndItsGroup_bothListed() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU,
                slot -> slot != MenuSlots.FILE_MENU && slot != MenuSlots.FILE_GROUP);

        assertThat(report.describeFilteredOut()).startsWith("Filtered out slots:")
                .contains("MenuSlot: File", "GroupSlot: File");
    }

    @Test
    void buildMenuBar_filterAcceptsAll_noFilteredOutBlock() {
        registerMenuSlots();
        registerMenuControls();

        builder.buildMenuBar(view, MenuSlots.MAIN_MENU, slot -> true);

        assertThat(report.describeFilteredOut()).isEmpty();
    }

    @Test
    void buildToolBar_filterRejectsGroup_filteredOutSlotsListed() {
        registerToolBarSlots();
        registerToolBarControls();

        builder.buildToolBar(view, ToolBarSlots.TOOL_BAR, slot -> slot != ToolBarSlots.EDIT_GROUP);

        assertThat(report.describeFilteredOut()).isEqualTo("Filtered out slots: GroupSlot: Edit");
    }

    @Test
    void buildMenu_filterRejectsGroup_groupLeftOutOfTheMenu() {
        registerMenuSlots();
        registerMenuControls();

        var menu = builder.buildMenu(view, MenuSlots.FILE_MENU, slot -> slot != MenuSlots.FILE_GROUP).root();

        assertThat(texts(menu.getItems())).containsExactly("Exit");
    }

    @Test
    void buildMenu_fileMenuWithGroupsAndSubmenu_buildsMenuWithItsGroups() {
        registerMenuSlots();
        registerMenuControls();

        var menu = builder.buildMenu(view, MenuSlots.FILE_MENU).root();

        assertThat(menu.getText()).isEqualTo("File");
        assertThat(texts(menu.getItems())).containsExactly("New", "Open", "Recent", null, "Exit");
    }

    @Test
    void buildMenu_noMenuFactory_throws() {
        registerMenuSlots();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildMenu(view, MenuSlots.FILE_MENU))
                .withMessageContaining("File");
    }

    @Test
    void buildContextMenu_groupWithItems_buildsItems() {
        slotRegistry.register(MenuSlots.POPUP, 0, MenuSlots.POPUP_GROUP);
        controlRegistry.register(MenuSlots.POPUP, provider(v -> new ContextMenu()));
        controlRegistry.register(MenuSlots.POPUP_GROUP, SimpleGroupProvider::new);
        controlRegistry.register(MenuSlots.POPUP_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Cut")));
        controlRegistry.register(MenuSlots.POPUP_GROUP, 100, provider(v -> RegistryTestSupport.createItem("Paste")));

        var contextMenu = builder.buildContextMenu(view, MenuSlots.POPUP).root();

        assertThat(texts(contextMenu.getItems())).containsExactly("Cut", "Paste");
    }

    @Test
    void buildContextMenu_groupWithoutFactory_menuEmptyAndProblemsShowTheGroup() {
        slotRegistry.register(MenuSlots.POPUP, 0, MenuSlots.POPUP_GROUP);
        controlRegistry.register(MenuSlots.POPUP, provider(v -> new ContextMenu()));
        controlRegistry.register(MenuSlots.POPUP_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Cut")));

        var contextMenu = builder.buildContextMenu(view, MenuSlots.POPUP).root();

        assertThat(contextMenu.getItems()).isEmpty();
        assertThat(report.toString()).isEqualTo("Context menu: Popup");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Context menu: Popup",
                "    Group: Popup, position: 0, warning: no ControlGroup provider registered, "
                        + "its content is left out"));
    }

    @Test
    void buildContextMenu_filterRejectsGroup_menuEmpty() {
        slotRegistry.register(MenuSlots.POPUP, 0, MenuSlots.POPUP_GROUP);
        controlRegistry.register(MenuSlots.POPUP, provider(v -> new ContextMenu()));
        controlRegistry.register(MenuSlots.POPUP_GROUP, SimpleGroupProvider::new);
        controlRegistry.register(MenuSlots.POPUP_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Cut")));

        var contextMenu = builder.buildContextMenu(view, MenuSlots.POPUP, slot -> slot != MenuSlots.POPUP_GROUP)
                .root();

        assertThat(contextMenu.getItems()).isEmpty();
        assertThat(report.hasDefects()).isFalse();
    }

    @Test
    void buildToolBar_filterRejectsGroup_groupLeftOutWithItsControls() {
        registerToolBarSlots();
        registerToolBarControls();

        var controls = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR, slot -> slot != ToolBarSlots.FILE_GROUP);

        assertThat(controls.root().getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("Copy", "Paste");
        assertThat(controls.providers()).noneMatch(p -> p.getSlot() == ToolBarSlots.FILE_GROUP);
        assertThat(report.toString()).doesNotContain("File", "New", "Open");
        assertThat(report.hasDefects()).isFalse();
    }

    @Test
    void buildToolBar_filterAcceptsAll_sameResultAsWithoutFilter() {
        registerToolBarSlots();
        registerToolBarControls();

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR, slot -> true).root();

        assertThat(toolBar.getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("New", "Open", null, "Copy", "Paste");
    }

    @Test
    void buildToolBar_twoGroups_controlsFollowPositionsWithVerticalSeparatorBetweenGroups() {
        registerToolBarSlots();
        registerToolBarControls();

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR).root();

        assertThat(toolBar.getItems()).hasSize(5);
        assertThat(toolBar.getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("New", "Open", null, "Copy", "Paste");
        assertThat(toolBar.getItems().get(2)).isInstanceOf(Separator.class);
        assertThat(((Separator) toolBar.getItems().get(2)).getOrientation()).isEqualTo(Orientation.VERTICAL);
    }

    @Test
    void buildToolBar_correctTree_reportsTheBuiltTreeWithoutProblems() {
        registerToolBarSlots();
        registerToolBarControls();

        builder.buildToolBar(view, ToolBarSlots.TOOL_BAR);

        assertThat(report.hasDefects()).isFalse();
        assertThat(report.hasWarnings()).isFalse();
        assertThat(report.describeDefects()).isEmpty();
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
        registerToolBarSlots();
        registerToolBarControls();
        toolBarEditGroupFactory.unregister();

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR).root();

        assertThat(toolBar.getItems()).extracting(n -> n instanceof Button b ? b.getText() : null)
                .containsExactly("New", "Open");
    }

    @Test
    void buildToolBar_groupWithoutFactory_builtTreeLacksItAndProblemsShowThePathToIt() {
        registerToolBarSlots();
        registerToolBarControls();
        toolBarEditGroupFactory.unregister();

        builder.buildToolBar(view, ToolBarSlots.TOOL_BAR);

        assertThat(report.toString()).doesNotContain("Edit", "Copy").contains("Group: File");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Tool bar: ToolBar",
                "    Group: Edit, position: 100, warning: no ControlGroup provider registered, "
                        + "its 2 controls are left out"));
    }

    @Test
    void buildToolBar_controlsWithSamePosition_bothBuiltAndTheSecondMarkedInTheProblems() {
        registerToolBarSlots();
        registerToolBarControls();
        controlRegistry.register(ToolBarSlots.FILE_GROUP, 100, provider(v -> new Button("Twin")));

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR).root();

        assertThat(toolBar.getItems()).hasSize(6);
        assertThat(report.toString()).contains("Open", "Twin");
        assertThat(report.describeDefects())
                .contains("Tool bar: ToolBar", "Group: File, position: 0")
                .contains(", warning: shares the position 100 with the previous sibling")
                .doesNotContain("Group: Edit");
    }

    @Test
    void buildToolBar_controlsOfGroupPutNowhere_builtTreeLacksThemAndProblemsListTheGroup() {
        registerToolBarSlots();
        registerToolBarControls();
        controlRegistry.register(ToolBarSlots.ORPHAN_GROUP, 0, provider(v -> new Button("Lost")));

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR).root();

        assertThat(toolBar.getItems()).hasSize(5);
        assertThat(report.toString()).doesNotContain("Orphan", "Lost");
        assertThat(report.describeDefects()).isEqualTo(String.join(System.lineSeparator(),
                "Tool bar: ToolBar",
                "    Group: Orphan, warning: not put into any menu or tool bar, its 1 controls are never shown"));
    }

    @Test
    void buildToolBar_noGroups_returnsEmptyToolBar() {
        controlRegistry.register(ToolBarSlots.TOOL_BAR, provider(v -> new ToolBar()));

        var toolBar = builder.buildToolBar(view, ToolBarSlots.TOOL_BAR).root();

        assertThat(toolBar.getItems()).isEmpty();
    }

    @Test
    void buildToolBar_noToolBarFactory_throws() {
        registerToolBarSlots();
        registerToolBarControls();
        toolBarFactory.unregister();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildToolBar(view, ToolBarSlots.TOOL_BAR))
                .withMessageContaining("ToolBar");
    }

    private void registerMenuSlots() {
        slotRegistry.register(MenuSlots.MAIN_MENU, 0, MenuSlots.FILE_MENU);
        slotRegistry.register(MenuSlots.MAIN_MENU, 100, MenuSlots.EDIT_MENU);
        slotRegistry.register(MenuSlots.FILE_MENU, 0, MenuSlots.FILE_GROUP);
        slotRegistry.register(MenuSlots.FILE_MENU, 100, MenuSlots.EXIT_GROUP);
        slotRegistry.register(MenuSlots.FILE_GROUP, 200, MenuSlots.RECENT_MENU);
        slotRegistry.register(MenuSlots.RECENT_MENU, 0, MenuSlots.RECENT_GROUP);
        slotRegistry.register(MenuSlots.EDIT_MENU, 0, MenuSlots.EDIT_GROUP);
    }

    private void registerMenuControls() {
        menuBarFactory = controlRegistry.register(MenuSlots.MAIN_MENU, provider(v -> new MenuBar()));
        controlRegistry.register(MenuSlots.FILE_MENU, provider(v -> RegistryTestSupport.createMenu("File")));
        editMenuFactory = controlRegistry.register(MenuSlots.EDIT_MENU,
                provider(v -> RegistryTestSupport.createMenu("Edit")));
        recentMenuFactory = controlRegistry.register(MenuSlots.RECENT_MENU,
                provider(v -> RegistryTestSupport.createMenu("Recent")));

        controlRegistry.register(MenuSlots.FILE_GROUP, SimpleGroupProvider::new);
        controlRegistry.register(MenuSlots.EXIT_GROUP, SimpleGroupProvider::new);
        controlRegistry.register(MenuSlots.RECENT_GROUP, SimpleGroupProvider::new);
        menuEditGroupFactory = controlRegistry.register(MenuSlots.EDIT_GROUP, SimpleGroupProvider::new);

        controlRegistry.register(MenuSlots.FILE_GROUP, 0, provider(v -> RegistryTestSupport.createItem("New")));
        controlRegistry.register(MenuSlots.FILE_GROUP, 100, provider(v -> RegistryTestSupport.createItem("Open")));
        controlRegistry.register(MenuSlots.EXIT_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Exit")));
        controlRegistry.register(MenuSlots.RECENT_GROUP, 0, provider(v -> RegistryTestSupport.createItem("a.txt")));
        controlRegistry.register(MenuSlots.RECENT_GROUP, 100, provider(v -> RegistryTestSupport.createItem("b.txt")));
        controlRegistry.register(MenuSlots.EDIT_GROUP, 0, provider(v -> RegistryTestSupport.createItem("Copy")));
        controlRegistry.register(MenuSlots.EDIT_GROUP, 100, provider(v -> RegistryTestSupport.createItem("Paste")));
    }

    private void registerToolBarSlots() {
        slotRegistry.register(ToolBarSlots.TOOL_BAR, 0, ToolBarSlots.FILE_GROUP);
        slotRegistry.register(ToolBarSlots.TOOL_BAR, 100, ToolBarSlots.EDIT_GROUP);
    }

    private void registerToolBarControls() {
        toolBarFactory = controlRegistry.register(ToolBarSlots.TOOL_BAR, provider(v -> new ToolBar()));
        controlRegistry.register(ToolBarSlots.FILE_GROUP, SimpleGroupProvider::new);
        toolBarEditGroupFactory = controlRegistry.register(ToolBarSlots.EDIT_GROUP, SimpleGroupProvider::new);

        controlRegistry.register(ToolBarSlots.FILE_GROUP, 0, provider(v -> new Button("New")));
        controlRegistry.register(ToolBarSlots.FILE_GROUP, 100, provider(v -> new Button("Open")));
        controlRegistry.register(ToolBarSlots.EDIT_GROUP, 0, provider(v -> new Button("Copy")));
        controlRegistry.register(ToolBarSlots.EDIT_GROUP, 100, provider(v -> new Button("Paste")));
    }
}
