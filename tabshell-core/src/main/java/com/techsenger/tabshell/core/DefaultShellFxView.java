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

package com.techsenger.tabshell.core;

import atlantafx.base.theme.Styles;
import com.techsenger.patternfx.mvp.AbstractParentFxView;
import com.techsenger.stagepro.core.StandardStageController;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.menu.MenuAware;
import com.techsenger.tabshell.core.menu.manager.MenuManager;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.registry.ControlBuilder;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.shelltab.ShellTabFxView;
import com.techsenger.tabshell.core.shelltab.ShellTabPort;
import com.techsenger.tabshell.core.style.CssAnchor;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.core.tab.TabContainerFxViewUtils;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.JavaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends ParentView but its interface doesn't because of encapsulation.
 *
 * @author Pavel Castornii
 */
public class DefaultShellFxView<P extends ShellPresenter<?, ?>>
        extends AbstractParentFxView<P> implements ShellFxView<P> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellFxView.class);

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    public class Composer extends AbstractParentFxView<P>.Composer implements ShellFxView.Composer {

        private final DefaultShellFxView<P> view = DefaultShellFxView.this;

        @Override
        public List<? extends ShellTabPort> getTabs() {
            return view.getTabPane().getTabs().stream()
                    .map(t -> (ShellTabFxView<?>) ((ComponentTab) t).getView())
                    .map(v -> v.getPresenter().getPort())
                    .toList();
        }

        @Override
        public void addTab(ShellTabFxView<?> tab) {
            view.tabPane.getTabs().add(tab.getNode());
            view.getModifiableChildren().add(tab);
        }

        @Override
        public void removeTab(ShellTabFxView<?> tab) {
            view.tabPane.getTabs().remove(tab.getNode());
            view.getModifiableChildren().remove(tab);
            tab.getPresenter().deinitializeTree();
        }

        @Override
        public ShellTabPort getSelectedTab() {
            var selectedTab = view.getSelectedTab();
            if (selectedTab != null) {
                return selectedTab.getPresenter().getPort();
            } else {
                return null;
            }
        }

        @Override
        public OverlayScope getOverlayScope() {
            return OverlayScope.SHELL;
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showDialog(dialog);
                view.getModifiableChildren().add(dialog);
            } else {
                var selectedTab = view.getSelectedTab();
                if (selectedTab != null) {
                    selectedTab.getComposer().addDialog(dialog);
                }
            }
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hideDialog(dialog);
                view.getModifiableChildren().remove(dialog);
                dialog.getPresenter().deinitializeTree();
            } else {
                var selectedTab = view.getSelectedTab();
                if (selectedTab != null) {
                    selectedTab.getComposer().removeDialog(dialog);
                }
            }
        }

        @Override
        public List<? extends DialogPort> getDialogs() {
            return view.getDialogManager().getDialogs().stream().map(d -> d.getPresenter().getPort()).toList();
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showPopup(popup, anchors);
                view.getModifiableChildren().add(popup);
            } else {
                var selectedTab = view.getSelectedTab();
                if (selectedTab != null) {
                    selectedTab.getComposer().addPopup(popup, anchors);
                }
            }
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hidePopup(popup);
                view.getModifiableChildren().remove(popup);
                popup.getPresenter().deinitializeTree();
            } else {
                var selectedTab = view.getSelectedTab();
                if (selectedTab != null) {
                    selectedTab.getComposer().removePopup(popup);
                }
            }
        }

        @Override
        public List<? extends PopupPort> getPopups() {
            return view.getDialogManager().getPopups().stream().map(d -> d.getPresenter().getPort()).toList();
        }
    }

    private class ShellDialogManager extends DefaultDialogManager {

        private final ShellStageController controller;

        ShellDialogManager(ShellStageController controller, StackPane stackPane, VBox mainPane) {
            super(stackPane, mainPane);
            this.controller = controller;
        }

        @Override
        public void hideDialog(DialogFxView<?> dialog) {
            super.hideDialog(dialog);
            if (getDialogCount() == 0) {
                controller.setUnfocused(false);
            }
        }

        @Override
        public void showDialog(DialogFxView<?> dialog) {
            if (getDialogCount() == 0) {
                controller.setUnfocused(true);
            }
            super.showDialog(dialog);
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
            getCloseButton().setOnAction(e -> getPresenter().close());
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

    private final VBox contentBox = new VBox();

    private final StackPane stackPane = new StackPane();

    private final TabPanePro tabPane = new TabPanePro();

    private final ShellStageController stageController;

    private final DialogManager dialogManager;

    private final ObservableList<Stylesheet> stylesheets;

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private ThemeApplier themeApplier;

    private FontApplier fontApplier;

    public DefaultShellFxView(Application application, List<Stylesheet> stylesheets) {
        this(application, new Stage(), stylesheets);
    }

    public DefaultShellFxView(Application application, Stage stage, List<Stylesheet> stylesheets) {
        this.application = application;
        this.stage = stage;
        this.stylesheets = FXCollections.observableArrayList(createDefaultStylesheets());
        if (stylesheets != null) {
            this.stylesheets.addAll(stylesheets);
        }
        var dialogCount = new SimpleIntegerProperty();
        stageController = new ShellStageController(stage, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialogCount);
        this.menuManager = new MenuManager(this, this.menuBar);
        this.dialogManager = new ShellDialogManager(stageController, stackPane, contentBox);
        dialogCount.bind(Bindings.size(this.dialogManager.getDialogs()));
    }

    @Override
    public void requestFocus() {
        this.tabPane.requestFocus();
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
        logger.debug("{} Menu bar upgraded", getDescriptor().getLogPrefix());
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuManager.updateMenuBar(getCurrentMenuAware());
        logger.debug("{} Menu bar updated", getDescriptor().getLogPrefix());
    }

    @Override
    public void addStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.addAll(sheets);
    }

    @Override
    public void removeStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.removeAll(sheets);
    }

    public MenuAware getCurrentMenuAware() {
        var selectedTab = getSelectedTab();
        if (selectedTab != null) {
            return selectedTab.getPresenter().getPort();
        } else {
            return getPresenter();
        }
    }

    @Override
    public void selectTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < this.tabPane.getTabs().size())  {
            this.tabPane.getSelectionModel().select(tabIndex);
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
    public ShellTabFxView<?> getSelectedTab() {
        var tab = (ComponentTab) this.tabPane.getSelectionModel().getSelectedItem();
        if (tab != null) {
            return (ShellTabFxView<?>) tab.getView();
        } else {
            return null;
        }
    }

    @Override
    public boolean isMaximized() {
        return this.stage.isMaximized();
    }

    @Override
    public void setMaximized(boolean value) {
        this.stage.setMaximized(value);
    }

    @Override
    public double getWidth() {
        return this.stage.getWidth();
    }

    @Override
    public void setWidth(double value) {
        this.stage.setWidth(value);
    }

    @Override
    public double getHeight() {
        return this.stage.getHeight();
    }

    @Override
    public void setHeight(double value) {
        this.stage.setHeight(value);
    }

    @Override
    public int getSelectedTabIndex() {
        return this.tabPane.getSelectionModel().getSelectedIndex();
    }

    @Override
    public Icon<?> getIcon() {
        return this.stageController.iconViewBox.getIcon();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.stageController.iconViewBox.setIcon(icon);
    }

    @Override
    public String getTitle() {
        return this.stage.getTitle();
    }

    @Override
    public void setTitle(String title) {
        this.stage.setTitle(title);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void setRegularFont(Font font) {
        tabPane.setTabMaxWidth(font.getSize() * 15);
    }

    @Override
    protected Composer createComposer() {
        return new DefaultShellFxView.Composer();
    }

    protected DialogManager getDialogManager() {
        return this.dialogManager;
    }

    @Override
    protected void build() {
        super.build();
        var presenter = getPresenter();
        themeApplier = new ThemeApplier(stageController, this.stylesheets,
                presenter.getSettings().getAppearance());
        this.fontApplier = new FontApplier(stackPane,
                presenter.getSettings().getAppearance());

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.getStyleClass().addAll("shell-tab-pane", Styles.DENSE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        this.contentBox.getChildren().add(tabPane);
        TabContainerFxViewUtils.initTabPane(tabPane, getPresenter());
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
        stageController.contentProperty().set(this.contentBox);
        //we add stackpane behind stage root
        stackPane.getChildren().add(stage.getScene().getRoot());
        stackPane.getStyleClass().add("root-stack-pane");
        stage.getScene().setRoot(stackPane);
        stage.show();
    }


    @Override
    protected void addListeners() {
        super.addListeners();
        var presenter = getPresenter();
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            this.menuManager.updateMenuBar(getCurrentMenuAware());
            presenter.onSelectedTabChanged(tabPane.getSelectionModel().getSelectedIndex());
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
//        var viewModel = getViewModel();
//        stage.addEventHandler(StageResizeEvent.STAGE_RESIZE_FINISHED, e -> {
//            if (!stage.isMaximized()) {
//                viewModel.setDefaultWidth(stage.getWidth());
//                viewModel.setDefaultHeight(stage.getHeight());
//            }
//        });
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
