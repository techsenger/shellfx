/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.core;

import atlantafx.base.theme.Styles;
import com.techsenger.patternfx.core.AbstractParentView;
import com.techsenger.stagepro.core.StageResizeEvent;
import com.techsenger.stagepro.core.StandardStageController;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.menu.manager.MenuManager;
import com.techsenger.tabshell.core.registry.ControlBuilder;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.style.CssAnchor;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.core.tab.TabContainerViewUtils;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.JavaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends ParentView but its interface doesn't because of encapsulation.
 *
 * @author Pavel Castornii
 */
public class DefaultShellView<T extends DefaultShellViewModel<?>, S extends DefaultShellComponent<?>>
        extends AbstractParentView<T, S> implements ShellView<T, S> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellView.class);

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    private class ShellDialogManager extends DefaultDialogManager {

        private final ShellStageController controller;

        ShellDialogManager(ShellStageController controller, StackPane stackPane, VBox mainPane,
                ReadOnlyIntegerWrapper dialogCount) {
            super(stackPane, mainPane, dialogCount);
            this.controller = controller;
        }

        @Override
        public void hideDialog(DialogView<?, ?> dialogView) {
            super.hideDialog(dialogView);
            if (getDialogCount() == 0) {
                controller.setUnfocused(false);
            }
        }

        @Override
        public void showDialog(DialogView<?, ?> dialogView) {
            if (getDialogCount() == 0) {
                controller.setUnfocused(true);
            }
            super.showDialog(dialogView);
        }
    }

    private class ShellStageController extends StandardStageController {

        private final Region leftSpacer = new Region();

        private final Region rightSpacer = new Region();

        private final IconViewBox iconViewBox = new IconViewBox();

        /**
         * Alignment of the title bar to the center both vertically and horizontally ((-fx-alignment: center;)
         * will lead to layout issues, similar to those in the TabPane or individual Tab. For example, tabs might not
         * fully expand during animations, and their content could be clipped.
         */
        ShellStageController(Stage stage, double width, double height, ReadOnlyIntegerProperty dialogCount) {
            super(stage, width, height, false);
            HBox.setHgrow(leftSpacer, Priority.ALWAYS);
            HBox.setHgrow(rightSpacer, Priority.ALWAYS);
            getButtonBox().getChildren().addAll(getMinimizeButton(), getMaximizeButton(), getCloseButton());
            getCloseButton().setOnAction(e -> getViewModel().close());
            getTitleBar().getChildren().addAll(iconViewBox, menuBar, leftSpacer, getTitleLabel(), rightSpacer,
                    getButtonBox());
            getTitleBar().widthProperty().addListener((ov, oldV, newV) -> updateSpacers());
            iconViewBox.widthProperty().addListener((ov, oldV, newV) -> updateSpacers());
            menuBar.widthProperty().addListener((ov, oldV, newV) -> updateSpacers());
            getTitleLabel().widthProperty().addListener((ov, oldV, newV) -> updateSpacers());
            getButtonBox().widthProperty().addListener((ov, oldV, newV) -> updateSpacers());
            getResizer().disabledProperty().unbind();
            getResizer().disabledProperty().bind(getStage().maximizedProperty()
                    .or(Bindings.not(getStage().resizableProperty()))
                    .or(dialogCount.greaterThan(0)));
        }

        void setUnfocused(boolean unfocused) {
            getStageBox().pseudoClassStateChanged(UNFOCUSED_PSEUDO_CLASS, unfocused);
        }

        private void updateSpacers() {
            var iconBox = iconViewBox.getWidth();
            //prefWidth returns the preferred width of the node, calculated based on its
            //content and styles, without considering the constraints of the container
            var menuBarWidth = Math.ceil(menuBar.prefWidth(-1));
            var labelHalfWidth = Math.ceil(getTitleLabel().prefWidth(-1) / 2);
            var buttonBoxWidth = Math.ceil(getButtonBox().prefWidth(-1));
            var titleBarWidth = getTitleBar().getWidth();
            double barHalfWidth = (titleBarWidth - SizeConstants.INSET * 2) / 2;

            var leftNodesWidth = iconBox + menuBarWidth + labelHalfWidth;
            var rightNodesWidth = labelHalfWidth + buttonBoxWidth;

            double leftSpacerWidth = barHalfWidth - leftNodesWidth;
            double rightSpacerWidth = barHalfWidth - rightNodesWidth;
            //setting pref width can lead to all nodes widths recalculation
            leftSpacer.setMaxWidth(leftSpacerWidth);
            rightSpacer.setMaxWidth(rightSpacerWidth);

//            logger.trace("Title centered. TitleBar: {}, iconBox: {}, menuBar: {}, leftSpacer: {}, label: {}, "
//                    + "rightSpacer: {}, buttonBox: {}", titleBarWidth, iconBox,
//                    menuBarWidth, leftSpacerWidth, labelHalfWidth * 2, rightSpacerWidth, buttonBoxWidth);
        }
    }


    private final Application application;

    private final Stage stage;

    private final MenuBar menuBar = new MenuBar();

    private final MenuManager menuManager;

    private final VBox contentPane = new VBox();

    private final StackPane stackPane = new StackPane();

    private final TabPanePro tabPane = new TabPanePro();

    private final ShellStageController stageController;

    private final DialogManager dialogManager;

    private final ObservableList<Stylesheet> stylesheets;

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private ThemeApplier themeApplier;

    private FontApplier fontApplier;

    public DefaultShellView(T viewModel, Application application, List<Stylesheet> stylesheets) {
        this(viewModel, application, new Stage(), stylesheets);
    }

    public DefaultShellView(T viewModel, Application application, Stage stage, List<Stylesheet> stylesheets) {
        super(viewModel);
        this.application = application;
        this.stage = stage;
        this.stylesheets = FXCollections.observableArrayList(createDefaultStylesheets());
        if (stylesheets != null) {
            this.stylesheets.addAll(stylesheets);
        }
        stageController = new ShellStageController(stage, viewModel.getDefaultWidth(),
                viewModel.getDefaultHeight(), viewModel.dialogCountProperty());
        this.menuManager = new MenuManager(this, this.menuBar);
        this.dialogManager = new ShellDialogManager(stageController, stackPane, contentPane,
                viewModel.dialogCountWrapper());
    }

    @Override
    public ControlRegistry getControlRegistry() {
        return controlRegistry;
    }

    @Override
    public void upgradeMenuBar() {
        this.menuBar.getMenus().clear();
        var builder = new ControlBuilder(controlRegistry);
        var menus = builder.buildMenuBarElements(this);
        this.menuBar.getMenus().addAll(menus);
        logger.debug("{} Menu bar upgraded", getComponent().getLogPrefix());
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuManager.updateMenuBar(getCurrentMenuAware());
        logger.debug("{} Menu bar updated", getComponent().getLogPrefix());
    }

    @Override
    public void addStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.addAll(sheets);
    }

    @Override
    public void removeStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.removeAll(sheets);
    }

    @Override
    public MenuHelper getMenuHelper(MenuName menuName) {
        return getViewModel().getMenuHelpersByName().get(menuName);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
        return getViewModel().getMenuItemHelpersByName().get(menuItemName);
    }

    @Override
    public void doOnMenuShowing(MenuName menuName) { }

    @Override
    public void doOnMenuHiding(MenuName menuName) { }

    public MenuAware getCurrentMenuAware() {
        var selectedTab = getSelectedTab();
        if (selectedTab != null) {
            return selectedTab;
        } else {
            return this;
        }
    }

    @Override
    public HostServices getHostServices() {
        return this.application.getHostServices();
    }

    @Override
    public Stage getStage() {
        return this.stage;
    }

    @Override
    public ShellTabView<?, ?> getSelectedTab() {
        var tab = (ComponentTab) this.tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            return (ShellTabView<?, ?>) tab.getView();
        } else {
            return null;
        }
    }

    protected DialogManager getDialogManager() {
        return this.dialogManager;
    }

    @Override
    protected void build() {
        super.build();
        var viewModel = getViewModel();
        themeApplier = new ThemeApplier(stageController, this.stylesheets,
                viewModel.getSettings().getAppearance());
        this.fontApplier = new FontApplier(stackPane,
                viewModel.getSettings().getAppearance());

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getStyleClass().addAll("shell-tab-pane", Styles.DENSE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        this.contentPane.getChildren().add(tabPane);
        TabContainerViewUtils.initTabPane(tabPane, getViewModel());
        var tabHeaderArea = getTabHeaderArea();
        tabHeaderArea.setTabHeaderFactory(c -> new SlantedTabHeaderSkin(c));
        tabHeaderArea.setTabGap(-10.0);
        // right corner is on top
        tabHeaderArea.setTabViewOrderResolver((tabHeader, index, tabCount, selected) -> {
            if (selected) {
                return  tabCount * -1.0;
            } else {
                return (tabCount - 1 - index) * -1.0;
            }
        });
        stageController.contentProperty().set(this.contentPane);
        //we add stackpane behind stage root
        stackPane.getChildren().add(stage.getScene().getRoot());
        stackPane.getStyleClass().add("root-stack-pane");
        stage.getScene().setRoot(stackPane);
        stage.setMaximized(viewModel.isMaximized());
        stage.show();
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        viewModel.widthWrapper().bind(this.contentPane.widthProperty());
        viewModel.heightWrapper().bind(this.contentPane.heightProperty());
        stage.titleProperty().bind(viewModel.titleProperty());
        viewModel.maximizedWrapper().bind(stage.maximizedProperty());
        stageController.iconViewBox.iconProperty().bind(viewModel.iconProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                ShellTabView<?, ?> view = (ShellTabView<?, ?>) ((ComponentTab) newV).getView();
                viewModel.selectedTabWrapper().set(view.getViewModel());
            } else {
                viewModel.selectedTabWrapper().set(null);
            }
            this.doOnTabChanged((ComponentTab) oldV, (ComponentTab) newV);
        });
        viewModel.selectedTabIndexWrapper().addListener((ov, oldV, newV) ->
                this.tabPane.getSelectionModel().select(newV.intValue()));
        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) ->
                viewModel.selectedTabIndexWrapper().set(newV.intValue()));
        ValueUtils.callAndAddListener(viewModel.getSettings().getAppearance().regularFontProperty(),
                (ov, oldV, newV) -> tabPane.setTabMaxWidth(newV.getSize() * 15));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
        stage.addEventHandler(StageResizeEvent.STAGE_RESIZE_FINISHED, e -> {
            if (!stage.isMaximized()) {
                viewModel.setDefaultWidth(stage.getWidth());
                viewModel.setDefaultHeight(stage.getHeight());
            }
        });
    }

    protected ShellStageController getStageController() {
        return stageController;
    }

    protected TabPaneProSkin.TabHeaderArea getTabHeaderArea() {
        TabPaneProSkin sourceSkin = (TabPaneProSkin) this.tabPane.getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
        return tabHeaderArea;
    }

    protected TabPanePro getTabPane() {
        return tabPane;
    }

    /**
     * JavaFX doesn't support the same accelerator to be installed in multiple MenuItems. See this bug:
     * https://bugs.openjdk.org/browse/JDK-8088068 . This method is a workaround for this problem.
     *
     * @param e
     */
    private void fixAcceleratorKeyPressed(KeyEvent e) {
        Node focusedNode = stage.getScene().getFocusOwner();
        if (focusedNode != null && focusedNode instanceof Control) {
            Control c = (Control) focusedNode;
            if (c.getContextMenu() != null) {
                for (MenuItem item : c.getContextMenu().getItems()) {
                    if (item.getAccelerator() != null && item.getAccelerator().match(e)) {
                        item.fire();
                        e.consume();
                    }
                }
            }
        }
    }

    /**
     * Attention! This method called in two situations - when new tab is created and when another tab gets selected.
     * That's why this method is not called from view.
     */
    private void doOnTabChanged(ComponentTab oldTab, ComponentTab newTab) {
        if (oldTab != null) {
            oldTab.getView().doOnDeselected();
        }
        this.menuManager.updateMenuBar(getCurrentMenuAware());
        if (newTab != null) {
            newTab.getView().doOnSelected();
        }
    }

    private List<Stylesheet> createDefaultStylesheets() {
        Set<Theme> allThemes = Stream.concat(
                Arrays.stream(AtlantaFxTheme.values()),
                Arrays.stream(JavaFxTheme.values()))
                .collect(Collectors.toSet());
        return List.of(
                new Stylesheet(CssAnchor.class.getResource("core.css"), Set.of(AtlantaFxTheme.values())),
                new Stylesheet(StyleClasses.class.getResource("material.css"), allThemes));
    }
}
