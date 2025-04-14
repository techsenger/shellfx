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
import com.techsenger.mvvm4fx.core.AbstractParentView;
import com.techsenger.stagepro.core.StageResizeEvent;
import com.techsenger.stagepro.core.StandardStageController;
import static com.techsenger.tabshell.core.CloseScope.SHELL;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.menu.manager.MenuManager;
import com.techsenger.tabshell.core.registry.ControlBuilder;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.Stylesheet;
import com.techsenger.tabshell.core.tab.AbstractShellTabView;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.ShellTabView;
import com.techsenger.tabshell.core.tab.TabHostViewUtils;
import com.techsenger.tabshell.core.tab.TabView;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.menu.MenuItemKey;
import com.techsenger.tabshell.material.menu.MenuKey;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
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
public class DefaultShellView extends AbstractParentView<DefaultShellViewModel> implements
        ShellView<DefaultShellViewModel> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellView.class);

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    private static class TabShellDialogManager extends DefaultDialogManager {

        private final ShellStageController controller;

        TabShellDialogManager(ShellStageController controller, StackPane stackPane, VBox mainPane,
                ReadOnlyIntegerWrapper dialogCount) {
            super(DialogScope.SHELL, stackPane, mainPane, dialogCount);
            this.controller = controller;
        }

        @Override
        public void closeDialog(DialogView<?> dialogView) {
            super.closeDialog(dialogView);
            if (getDialogCount() == 0) {
                controller.setUnfocused(false);
            }
        }

        @Override
        public void openDialog(DialogView<?> dialogView) {
            if (getDialogCount() == 0) {
                controller.setUnfocused(true);
            }
            super.openDialog(dialogView);
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
            getCloseButton().setOnAction(e -> close());
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

            logger.trace("Title centered. TitleBar: {}, iconBox: {}, menuBar: {}, leftSpacer: {}, label: {}, "
                    + "rightSpacer: {}, buttonBox: {}", titleBarWidth, iconBox, menuBarWidth, leftSpacerWidth,
                    labelHalfWidth * 2, rightSpacerWidth, buttonBoxWidth);
        }
    }

    /**
     * The instance of this class it passed to tab, they run it (if they agree to be closed). Finally this class
     * closes application.
     */
    private final class Closer implements Runnable {

        @Override
        public void run() {
            ShellTabView<?> selectedTab;
            var allCanBeClosed = true;
            for (var tab: tabPane.getTabs()) {
                var view = ((ComponentTab) tab).getView();
                var callback = createCloseCallback(SHELL, null);
                if (!view.doOnCloseAttempt(CloseScope.SHELL, callback)) {
                    allCanBeClosed = false;
                    break;
                }
            }
            if (allCanBeClosed) {
                while ((selectedTab = getSelectedTab()) != null) {
                    doCloseTab(selectedTab);
                }
                if (tabPane.getTabs().size() == 0) {
                    stage.hide();
                    deinitialize();
                    stage.close();
                    var onClosed = getViewModel().getOnClosed();
                    if (onClosed != null) {
                        onClosed.call();
                    }
                }
            }
        }
    }

    private final Stage stage;

    private final MenuBar menuBar = new MenuBar();

    private final MenuManager menuManager;

    private final VBox contentPane  = new VBox();

    private final StackPane stackPane = new StackPane();

    private final TabPane tabPane = new TabPane();

    private final ThemeManager themeManager;

    private final FontManager fontManager;

    private final ShellStageController stageController;

    private final DialogManager dialogManager;

    private final ObservableList<Stylesheet> stylesheets;

    private final ControlRegistry controlRegistry = new ControlRegistry();

    public DefaultShellView(List<Stylesheet> stylesheets, DefaultShellViewModel viewModel) {
        this(new Stage(), stylesheets, viewModel);
    }

    public DefaultShellView(Stage stage, List<Stylesheet> stylesheets, DefaultShellViewModel viewModel) {
        super(viewModel);
        this.stage = stage;
        stageController = new ShellStageController(stage, viewModel.getDefaultWidth(),
                viewModel.getDefaultHeight(), viewModel.dialogCountProperty());
        this.stylesheets = FXCollections.observableArrayList(createDefaultStylesheets());
        if (stylesheets != null) {
            this.stylesheets.addAll(stylesheets);
        }
        themeManager = new ThemeManager(stageController, this.stylesheets, viewModel.getSettings().getAppearance());
        this.dialogManager = new TabShellDialogManager(stageController, stackPane, contentPane,
                viewModel.dialogCountWrapper());
        this.menuManager = new MenuManager(this, this.menuBar);
        this.fontManager = new FontManager(stackPane, viewModel.getSettings().getAppearance());
    }

    @Override
    public void openTab(ShellTabView<?> tabView) {
        tabPane.getTabs().add((Tab) tabView.getNode());
    }

    @Override
    public void closeTab(ComponentTab tab) {
        this.closeTab((ShellTabView<?>) tab.getView());
    }

    @Override
    public void closeTab(ShellTabView<?> tabView) {
        if (tabView.doOnCloseAttempt(CloseScope.TAB, createCloseCallback(CloseScope.TAB, tabView))) {
            this.doCloseTab(tabView);
        }
    }

    @Override
    public ShellTabView<?> getSelectedTab() {
        var tab = (ComponentTab) this.tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            return (ShellTabView<?>) tab.getView();
        } else {
            return null;
        }
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
        logger.debug("Menu bar upgraded");
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuManager.updateMenuBar(getCurrentMenuAware());
        logger.debug("Menu bar updated");
    }

    @Override
    public DialogManager getDialogManager() {
        return this.dialogManager;
    }

    @Override
    public void close() {
        var closer = this.new Closer();
        closer.run();
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
    public MenuHelper getMenuHelper(MenuKey menuKey) {
        return getViewModel().getMenuHelpersByKey().get(menuKey);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemKey menuItemKey) {
        return getViewModel().getMenuItemHelpersByKey().get(menuItemKey);
    }

    @Override
    public void doOnMenuShowing(MenuKey menuKey) { }

    @Override
    public void doOnMenuHiding(MenuKey menuKey) { }

    public MenuAware getCurrentMenuAware() {
        var selectedTab = getSelectedTab();
        if (selectedTab != null) {
            return selectedTab;
        } else {
            return this;
        }
    }

    @Override
    protected void build(DefaultShellViewModel viewModel) {
        super.build(viewModel);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getStyleClass().addAll("shell-tab-pane", Styles.TABS_FLOATING, Styles.DENSE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        this.contentPane.getChildren().add(tabPane);
        TabHostViewUtils.initTabPane(tabPane, this);
        stageController.contentProperty().set(this.contentPane);
        //we add stackpane behind stage root
        stackPane.getChildren().add(stage.getScene().getRoot());
        stackPane.getStyleClass().add("root-stack-pane");
        stage.getScene().setRoot(stackPane);
        stage.setMaximized(viewModel.isMaximized());
        stage.show();
    }

    @Override
    protected void bind(DefaultShellViewModel viewModel) {
        super.bind(viewModel);
        viewModel.widthWrapper().bind(this.contentPane.widthProperty());
        viewModel.heightWrapper().bind(this.contentPane.heightProperty());
        stage.titleProperty().bind(viewModel.titleProperty());
        viewModel.maximizedWrapper().bind(stage.maximizedProperty());
        stageController.iconViewBox.iconProperty().bind(viewModel.iconProperty());
    }

    @Override
    protected void addListeners(DefaultShellViewModel viewModel) {
        super.addListeners(viewModel);
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            if (newV != null) {
                ShellTabView<?> view = (ShellTabView<?>) ((ComponentTab) newV).getView();
                viewModel.selectedTabWrapper().set(view.getViewModel());
            } else {
                viewModel.selectedTabWrapper().set(null);
            }
            this.doOnTabChanged((ComponentTab) oldV, (ComponentTab) newV);
        });
        //tabs can be added/removed using open/close methods, and close button in tab.
        this.tabPane.getTabs().addListener((ListChangeListener<? super Tab>) (change) -> {

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Tab tab : change.getAddedSubList()) {
                        ShellTabView<?> tabView = (ShellTabView<?>) ((ComponentTab) tab).getView();
                        ((AbstractShellTabView<?>) tabView).setTabHost(this);
                        var tabViewModel = tabView.getViewModel();
                        viewModel.getModifiableTabs().add(tabViewModel);
                        ((AbstractShellTabViewModel) tabViewModel).setTabHost(viewModel);
                    }
                }
                if (change.wasRemoved()) {
                    for (Tab tab : change.getRemoved()) {
                        ShellTabView<?> tabView = (ShellTabView<?>) ((ComponentTab) tab).getView();
                        ((AbstractShellTabView<?>) tabView).setTabHost(null);
                        var tabViewModel = tabView.getViewModel();
                        viewModel.getModifiableTabs().remove(tabViewModel);
                        ((AbstractShellTabViewModel) tabViewModel).setTabHost(null);
                    }
                }
            }
        });
        viewModel.selectedTabIndexWrapper().addListener((ov, oldV, newV) ->
                this.tabPane.getSelectionModel().select(newV.intValue()));
        this.tabPane.getSelectionModel().selectedIndexProperty().addListener((ov, oldV, newV) ->
                viewModel.selectedTabIndexWrapper().set(newV.intValue()));
        viewModel.closeRequestedSource().addListener(v -> {
            if (Boolean.TRUE.equals(v)) {
                close();
            }
        });
    }

    @Override
    protected void addHandlers(DefaultShellViewModel viewModel) {
        super.addHandlers(viewModel);
        this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
        stage.setOnCloseRequest((e) -> {
            close();
        });
        stage.addEventHandler(StageResizeEvent.STAGE_RESIZING_FINISHED, e -> {
            if (!stage.isMaximized()) {
                viewModel.setDefaultWidth(stage.getWidth());
                viewModel.setDefaultHeight(stage.getHeight());
            }
        });
    }

    @Override
    protected void postDeinitialize(DefaultShellViewModel viewModel) {
        super.postDeinitialize(viewModel);
    }

    protected ShellStageController getStageController() {
        return stageController;
    }

    private void doCloseTab(TabView<?> tabView) {
        this.tabPane.getTabs().remove(tabView.getNode());
        tabView.deinitialize();
        var closedCallback = tabView.getViewModel().getOnClosed();
        if (closedCallback != null) {
            closedCallback.call();
        }
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
     * See {@link TabPaneHolderViewUtils}.
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
        return List.of(
                new Stylesheet(Stylesheet.class.getResource("core.css")),
                new Stylesheet(TabShellTheme.CASPIAN, Stylesheet.class.getResource("core-caspian.css")),
                new Stylesheet(TabShellTheme.CUPERTINO_DARK, Stylesheet.class.getResource("core-cupertino-dark.css")),
                new Stylesheet(TabShellTheme.CUPERTINO_LIGHT,
                        Stylesheet.class.getResource("core-cupertino-light.css")),
                new Stylesheet(TabShellTheme.DRACULA, Stylesheet.class.getResource("core-dracula.css")),
                new Stylesheet(TabShellTheme.MODENA, Stylesheet.class.getResource("core-modena.css")),
                new Stylesheet(TabShellTheme.NORD_DARK, Stylesheet.class.getResource("core-nord-dark.css")),
                new Stylesheet(TabShellTheme.NORD_LIGHT, Stylesheet.class.getResource("core-nord-light.css")),
                new Stylesheet(TabShellTheme.PRIMER_DARK, Stylesheet.class.getResource("core-primer-dark.css")),
                new Stylesheet(TabShellTheme.PRIMER_LIGHT, Stylesheet.class.getResource("core-primer-light.css"))
        );
    }

    private Runnable createCloseCallback(CloseScope scope, ShellTabView<?> tab) {
        switch (scope) {
            case SHELL:
                return () -> close();
            case TAB:
                return () -> closeTab(tab);
            default:
                throw new AssertionError();
        }
    }
}
