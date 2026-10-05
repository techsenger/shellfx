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
import com.techsenger.shellfx.material.slot.ContextMenuSlot;
import com.techsenger.shellfx.material.slot.GroupSlot;
import com.techsenger.shellfx.material.slot.MenuBarSlot;
import com.techsenger.shellfx.material.slot.MenuSlot;
import java.util.List;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link ManagedControlBuilder}: a menu bar with two menus, their groups and one submenu is
 * built from the real registries - once as it should be, and then with a hole in it, to check what is left out and
 * how the report of the build tells about it. The tree is
 * <pre>
 * MainMenu
 *     File (position 0)
 *         FileGroup: New (0), Open (100), Recent (200) with RecentGroup: a.txt (0), b.txt (100)
 *         ExitGroup: Exit (0)
 *     Edit (position 100)
 *         EditGroup: Copy (0), Paste (100)
 * </pre>
 *
 * @author Pavel Castornii
 */
public class ManagedControlBuilderIT {

    private static final MenuBarSlot<ParentView<?>> MAIN_MENU = new MenuBarSlot<>(ParentView.class, "MainMenu");

    private static final MenuSlot<ParentView<?>> FILE_MENU = new MenuSlot<>(ParentView.class, "File");

    private static final MenuSlot<ParentView<?>> EDIT_MENU = new MenuSlot<>(ParentView.class, "Edit");

    private static final MenuSlot<ParentView<?>> RECENT_MENU = new MenuSlot<>(ParentView.class, "Recent");

    private static final GroupSlot<ParentView<?>, MenuItem> FILE_GROUP = new GroupSlot<>(ParentView.class, "File");

    private static final GroupSlot<ParentView<?>, MenuItem> EXIT_GROUP = new GroupSlot<>(ParentView.class, "Exit");

    private static final GroupSlot<ParentView<?>, MenuItem> RECENT_GROUP =
            new GroupSlot<>(ParentView.class, "Recent");

    private static final GroupSlot<ParentView<?>, MenuItem> EDIT_GROUP = new GroupSlot<>(ParentView.class, "Edit");

    private static final GroupSlot<ParentView<?>, MenuItem> ORPHAN_GROUP =
            new GroupSlot<>(ParentView.class, "Orphan");

    private static final ContextMenuSlot<ParentView<?>> POPUP = new ContextMenuSlot<>(ParentView.class, "Popup");

    private static final GroupSlot<ParentView<?>, MenuItem> POPUP_GROUP = new GroupSlot<>(ParentView.class, "Popup");

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

    private final ManagedControlBuilder builder = new ManagedControlBuilder(slotRegistry, controlRegistry) {
        @Override
        SlotTree createSlotTree(ParentView<?> view) {
            var tree = super.createSlotTree(view);
            tree.getLogger().setEnabled(true);
            report = tree.getLogger();
            return tree;
        }
    };

    private SlotTreeLogger report;

    private Registration menuBarFactory;

    private Registration editMenuFactory;

    private Registration recentMenuFactory;

    private Registration editGroupFactory;

    @Test
    void buildMenuBar_twoMenusWithGroupsAndSubmenu_buildsWholeTree() {
        registerSlots();
        registerControls();

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

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
        registerSlots();
        registerControls();

        builder.buildMenuBar(MAIN_MENU, view);

        assertThat(report.hasProblems()).isFalse();
        assertThat(report.describeProblems()).isEmpty();
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
        registerSlots();
        registerControls();
        recentMenuFactory.unregister();

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(texts(menuBar.getMenus().get(0).getItems())).containsExactly("New", "Open", null, "Exit");
        assertThat(texts(menuBar.getMenus().get(1).getItems())).containsExactly("Copy", "Paste");
    }

    @Test
    void buildMenuBar_submenuWithoutFactory_builtTreeLacksItAndProblemsShowThePathToIt() {
        registerSlots();
        registerControls();
        recentMenuFactory.unregister();

        builder.buildMenuBar(MAIN_MENU, view);

        assertThat(report.toString()).doesNotContain("Recent").contains("Menu: File", "Menu: Edit");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: File, position: 0",
                "        Group: File, position: 0",
                "            Menu: Recent, position: 200, warning: no Menu factory registered, it is left out"));
    }

    @Test
    void buildMenuBar_groupWithoutFactory_menuWithoutItemsLeftOutOfTheMenuBar() {
        registerSlots();
        registerControls();
        editGroupFactory.unregister();

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File");
    }

    @Test
    void buildMenuBar_groupWithoutFactory_builtTreeLacksTheMenuAndProblemsShowThePathToTheGroup() {
        registerSlots();
        registerControls();
        editGroupFactory.unregister();

        builder.buildMenuBar(MAIN_MENU, view);

        assertThat(report.toString()).doesNotContain("Edit", "Copy");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: Edit, position: 100",
                "        Group: Edit, position: 0, warning: no ControlGroup factory registered, "
                        + "its content is left out"));
    }

    @Test
    void buildMenuBar_itemWithoutHandler_itemKeptInBothTreesAndMarkedOnlyInTheProblems() {
        registerSlots();
        registerControls();
        controlRegistry.register(EDIT_GROUP, 500, v -> new MenuItem("Bare"));

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(texts(menuBar.getMenus().get(1).getItems())).containsExactly("Copy", "Paste", "Bare");
        assertThat(report.toString()).contains("MenuItem: Bare, position: 500").doesNotContain("WARNING");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: Edit, position: 100",
                "        Group: Edit, position: 0",
                "            MenuItem: Bare, position: 500, warning: no handler"));
    }

    @Test
    void buildMenuBar_menuWithoutHandler_menuBuiltAndProblemsSayItsVisibilityFollowsItsItems() {
        registerSlots();
        registerControls();
        editMenuFactory.unregister();
        controlRegistry.register(EDIT_MENU, v -> new Menu("Edit"));

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
        assertThat(report.toString()).contains("Menu: Edit, position: 100").doesNotContain("WARNING");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Menu: Edit, position: 100, warning: no handler, its visibility is determined by walking its "
                        + "items"));
    }

    @Test
    void buildMenuBar_itemsWithSamePosition_bothBuiltAndTheSecondMarkedInTheProblems() {
        registerSlots();
        registerControls();
        controlRegistry.register(FILE_GROUP, 100, v -> RegistryTestSupport.createItem("Twin"));

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(texts(menuBar.getMenus().get(0).getItems())).contains("Open", "Twin");
        assertThat(report.toString()).contains("Open", "Twin").doesNotContain("WARNING");
        assertThat(report.describeProblems())
                .contains("Menu: File, position: 0", "Group: File, position: 0")
                .contains(", warning: shares the position 100 with the previous sibling")
                .doesNotContain("Menu: Edit");
    }

    @Test
    void buildMenuBar_controlsOfGroupPutNowhere_builtTreeLacksThemAndProblemsListTheGroup() {
        registerSlots();
        registerControls();
        controlRegistry.register(ORPHAN_GROUP, 0, v -> RegistryTestSupport.createItem("Lost"));

        var menuBar = builder.buildMenuBar(MAIN_MENU, view);

        assertThat(menuBar.getMenus()).extracting(Menu::getText).containsExactly("File", "Edit");
        assertThat(report.toString()).doesNotContain("Orphan", "Lost");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Menu bar: MainMenu",
                "    Group: Orphan, warning: not put into any menu or tool bar, its 1 controls are never shown"));
    }

    @Test
    void buildMenuBar_noMenuBarFactory_throws() {
        registerSlots();
        registerControls();
        menuBarFactory.unregister();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildMenuBar(MAIN_MENU, view))
                .withMessageContaining("MainMenu");
    }

    @Test
    void buildMenu_fileMenuWithGroupsAndSubmenu_buildsMenuWithItsGroups() {
        registerSlots();
        registerControls();

        var menu = builder.buildMenu(FILE_MENU, view);

        assertThat(menu.getText()).isEqualTo("File");
        assertThat(texts(menu.getItems())).containsExactly("New", "Open", "Recent", null, "Exit");
    }

    @Test
    void buildMenu_noMenuFactory_throws() {
        registerSlots();

        assertThatIllegalStateException().isThrownBy(() -> builder.buildMenu(FILE_MENU, view))
                .withMessageContaining("File");
    }

    @Test
    void buildContextMenu_groupWithItems_buildsItems() {
        slotRegistry.register(POPUP, 0, POPUP_GROUP);
        controlRegistry.register(POPUP, v -> new ContextMenu());
        controlRegistry.register(POPUP_GROUP, v -> new ControlGroup<>());
        controlRegistry.register(POPUP_GROUP, 0, v -> RegistryTestSupport.createItem("Cut"));
        controlRegistry.register(POPUP_GROUP, 100, v -> RegistryTestSupport.createItem("Paste"));

        var contextMenu = builder.buildContextMenu(POPUP, view);

        assertThat(texts(contextMenu.getItems())).containsExactly("Cut", "Paste");
    }

    @Test
    void buildContextMenu_groupWithoutFactory_menuEmptyAndProblemsShowTheGroup() {
        slotRegistry.register(POPUP, 0, POPUP_GROUP);
        controlRegistry.register(POPUP, v -> new ContextMenu());
        controlRegistry.register(POPUP_GROUP, 0, v -> RegistryTestSupport.createItem("Cut"));

        var contextMenu = builder.buildContextMenu(POPUP, view);

        assertThat(contextMenu.getItems()).isEmpty();
        assertThat(report.toString()).isEqualTo("Context menu: Popup");
        assertThat(report.describeProblems()).isEqualTo(String.join(System.lineSeparator(),
                "Context menu: Popup",
                "    Group: Popup, position: 0, warning: no ControlGroup factory registered, "
                        + "its content is left out"));
    }

    private void registerSlots() {
        slotRegistry.register(MAIN_MENU, 0, FILE_MENU);
        slotRegistry.register(MAIN_MENU, 100, EDIT_MENU);
        slotRegistry.register(FILE_MENU, 0, FILE_GROUP);
        slotRegistry.register(FILE_MENU, 100, EXIT_GROUP);
        slotRegistry.register(FILE_GROUP, 200, RECENT_MENU);
        slotRegistry.register(RECENT_MENU, 0, RECENT_GROUP);
        slotRegistry.register(EDIT_MENU, 0, EDIT_GROUP);
    }

    private void registerControls() {
        menuBarFactory = controlRegistry.register(MAIN_MENU, v -> new MenuBar());
        controlRegistry.register(FILE_MENU, v -> RegistryTestSupport.createMenu("File"));
        editMenuFactory = controlRegistry.register(EDIT_MENU, v -> RegistryTestSupport.createMenu("Edit"));
        recentMenuFactory = controlRegistry.register(RECENT_MENU, v -> RegistryTestSupport.createMenu("Recent"));

        controlRegistry.register(FILE_GROUP, v -> new ControlGroup<>());
        controlRegistry.register(EXIT_GROUP, v -> new ControlGroup<>());
        controlRegistry.register(RECENT_GROUP, v -> new ControlGroup<>());
        editGroupFactory = controlRegistry.register(EDIT_GROUP, v -> new ControlGroup<>());

        controlRegistry.register(FILE_GROUP, 0, v -> RegistryTestSupport.createItem("New"));
        controlRegistry.register(FILE_GROUP, 100, v -> RegistryTestSupport.createItem("Open"));
        controlRegistry.register(EXIT_GROUP, 0, v -> RegistryTestSupport.createItem("Exit"));
        controlRegistry.register(RECENT_GROUP, 0, v -> RegistryTestSupport.createItem("a.txt"));
        controlRegistry.register(RECENT_GROUP, 100, v -> RegistryTestSupport.createItem("b.txt"));
        controlRegistry.register(EDIT_GROUP, 0, v -> RegistryTestSupport.createItem("Copy"));
        controlRegistry.register(EDIT_GROUP, 100, v -> RegistryTestSupport.createItem("Paste"));
    }
}
