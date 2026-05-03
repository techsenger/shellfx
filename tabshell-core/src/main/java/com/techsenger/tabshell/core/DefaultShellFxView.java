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
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.AbstractParentFxView;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.area.AreaPort;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.menu.manager.MenuManager;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.registry.ControlBuilder;
import com.techsenger.tabshell.core.registry.ControlRegistry;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.CssAnchor;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.JavaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends ParentView but its interface doesn't because of encapsulation.
 *
 * @author Pavel Castornii
 */
public class DefaultShellFxView<P extends DefaultShellPresenter<?>>
        extends AbstractParentFxView<P> implements ShellFxView<P> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultShellFxView.class);

    private static final PseudoClass MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    public class Composer extends AbstractParentFxView<P>.Composer implements ShellFxView.Composer {

        private final ReadOnlyObjectWrapper<ParentFxView<?>> focused = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<ParentFxView<?>> menuAware = new ReadOnlyObjectWrapper<>();

        private final DefaultShellFxView<P> view = DefaultShellFxView.this;

        private AreaFxView<?> workspace;

        /**
         * PauseTransition used to implement debounce on the JavaFX thread with duration.
         */
        private PauseTransition focusDebouncePause = new PauseTransition(Duration.millis(250));

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            view.dialogManager.showDialog(dialog);
            view.getModifiableChildren().add(dialog);
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            view.dialogManager.hideDialog(dialog);
            view.getModifiableChildren().remove(dialog);
            dialog.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends DialogPort> getDialogPorts() {
            return view.getDialogManager().getDialogs().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends DialogFxView<?>> getDialogs() {
            return view.getDialogManager().getDialogs();
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            view.dialogManager.showPopup(popup, anchors);
            view.getModifiableChildren().add(popup);
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            view.dialogManager.hidePopup(popup);
            view.getModifiableChildren().remove(popup);
            popup.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupPort> getPopupPorts() {
            return view.getDialogManager().getPopups().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return view.getDialogManager().getPopups();
        }

        @Override
        public void addWorkspace(AreaFxView<?> workspace) {
            this.workspace = workspace;
            view.getModifiableChildren().add(workspace);
            VBox.setVgrow(workspace.getNode(), Priority.ALWAYS);
            view.contentBox.getChildren().add(workspace.getNode());
        }

        @Override
        public void removeWorkspace() {
            if (this.workspace == null) {
                return;
            }
            view.getModifiableChildren().remove(this.workspace);
            view.contentBox.getChildren().remove(this.workspace.getNode());
        }

        @Override
        public AreaFxView<?> getWorkspace() {
            return this.workspace;
        }

        @Override
        public AreaPort getWorkspacePort() {
            return this.workspace == null ? null : this.workspace.getPresenter();
        }

        @Override
        public ReadOnlyObjectProperty<ParentFxView<?>> focusedProperty() {
            return this.focused.getReadOnlyProperty();
        }

        @Override
        public ParentFxView<?> getFocused() {
            return this.focused.get();
        }

        @Override
        public ReadOnlyObjectProperty<ParentFxView<?>> menuAwareProperty() {
            return this.menuAware.getReadOnlyProperty();
        }

        @Override
        public ParentFxView<?> getMenuAware() {
            return this.menuAware.get();
        }

        private void init() {
            this.focused.addListener((ov, oldV, newV) -> {
                logger.debug("{} Focused component: {}", getDescriptor().getLogPrefix(),
                        (newV == null) ? null : newV.getDescriptor().getFullName());
            });
            this.menuAware.addListener((ov, oldV, newV) -> {
                logger.debug("{} Menu aware component: {}", getDescriptor().getLogPrefix(),
                        (newV == null) ? null : newV.getDescriptor().getFullName());
                updateMenuBar();
            });
            view.stage.getScene().focusOwnerProperty().addListener((ov, oldV, newV) -> {
                this.focusDebouncePause.stop();
                this.focusDebouncePause.playFromStart();
            });
            this.focusDebouncePause.setOnFinished((e) -> onFocusPauseFinished());
        }

        private void setFocused(ParentFxView<?> focused) {
            this.focused.set(focused);
        }

        private void setMenuAware(ParentFxView<?> menuAware) {
            this.menuAware.set(menuAware);
        }

        private void onFocusPauseFinished() {
            var newNode = view.stage.getScene().getFocusOwner();
            if (newNode == null) {
                setFocused(null);
                setMenuAware(view);
                return;
            }
            Node currentNode = newNode;
            while (currentNode != null) {
                var view = FxViewUtils.getView(currentNode);
                if (view instanceof ParentFxView<?> component) {
                    setFocused(component);
                    break;
                }
                currentNode = currentNode.getParent();
            }
            resolvedMenuAware(currentNode);
        }

        private void resolvedMenuAware(Node node) {
            ParentFxView<?> focused = getFocused();
            if (focused == null) {
                setMenuAware(view);
                return;
            }
            if (focused == this) { // when user clicks on shell main menu, the previous  menu aware is used
                return;
            }
            ParentFxView<?> currentComponent = focused;
            while (true) {
                var port = currentComponent.getPresenter();
                if (port instanceof MenuAwarePort menuAware) {
                    setMenuAware(currentComponent);
                    return;
                }
                if (currentComponent instanceof ChildFxView<?> child) {
                    currentComponent = child.getParent();
                    if (currentComponent == null) {
                        logger.warn("{} Child {} has no parent", getDescriptor().getLogPrefix(),
                                child.getDescriptor().getFullName());
                        setMenuAware(view);
                        return;
                    }
                } else {
                    setMenuAware(view);
                    return;
                }
            }
        }
    }

    private class ShellDialogManager extends DefaultDialogManager {

//        private final ShellStageController controller;

        ShellDialogManager(StackPane stackPane, VBox mainPane) {
            super(stackPane, mainPane);
//            this.controller = controller;
        }

        @Override
        public void hideDialog(DialogFxView<?> dialog) {
            super.hideDialog(dialog);
//            if (getDialogCount() == 0) {
//                controller.setUnfocused(false);
//            }
        }

        @Override
        public void showDialog(DialogFxView<?> dialog) {
//            if (getDialogCount() == 0) {
//                controller.setUnfocused(true);
//            }
            super.showDialog(dialog);
        }
    }

    private final Application application;

    private final Stage stage;

    private final ObservableList<Stylesheet> stylesheets;

    private final IconViewBox iconViewBox = new IconViewBox();

    private final MenuBar menuBar = new MenuBar();

    private final HBox leftBox = new HBox(iconViewBox, menuBar);

    private final Label titleLabel = new Label();

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_MINIMIZE));

    private final FontIconView maximizeIconView = new FontIconView(CoreIcons.WINDOW_MAXIMIZE);

    private final Button maximizeButton = new Button(null, maximizeIconView);

    private final Button closeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_CLOSE));

    private final HBox rightBox = new HBox(minimizeButton, maximizeButton, closeButton);

    private final HeaderBar titleBar = new HeaderBar(leftBox, titleLabel, rightBox);

    private final VBox contentBox = new VBox();

    private final VBox stageBox = new VBox(titleBar, contentBox);

    private final StackPane stackPane = new StackPane(stageBox);

    private final MenuManager menuManager;

    private final DialogManager dialogManager;

    private final ControlRegistry controlRegistry = new ControlRegistry();

    private ThemeApplier themeApplier;

    private FontApplier fontApplier;

    public DefaultShellFxView(Application application, List<Stylesheet> stylesheets) {
        this(application, new Stage(), stylesheets);
    }

    public DefaultShellFxView(Application application, Stage stage, List<Stylesheet> stylesheets) {
        Objects.requireNonNull(application, "Application can't be null");
        this.application = application;
        Objects.requireNonNull(stage, "Stage can't be null");
        this.stage = stage;
        this.stylesheets = FXCollections.observableArrayList(createDefaultStylesheets());
        if (stylesheets != null) {
            this.stylesheets.addAll(stylesheets);
        }
        var dialogCount = new SimpleIntegerProperty();
//        stageController = new ShellStageController(stage, DEFAULT_WIDTH, DEFAULT_HEIGHT, dialogCount);
        this.menuManager = new MenuManager(this, this.menuBar);
        this.dialogManager = new ShellDialogManager(stackPane, contentBox);
        dialogCount.bind(Bindings.size(this.dialogManager.getDialogs()));
    }

    @Override
    public void requestFocus() {
        var workspace = getComposer().getWorkspace();
        if (workspace != null) {
            workspace.requestFocus();
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
        var menus = builder.buildMainMenus(this);
        this.menuBar.getMenus().addAll(menus);
        logger.debug("{} Menu bar upgraded", getDescriptor().getLogPrefix());
        updateMenuBar();
    }

    @Override
    public void updateMenuBar() {
        this.menuManager.updateMenuBar((MenuAwarePort) getComposer().getMenuAware().getPresenter());
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

    @Override
    public Stage getStage() {
        return this.stage;
    }

    @Override
    public void setMaximized(boolean value) {
        this.stage.setMaximized(value);
    }

    @Override
    public void setWidth(double value) {
        this.stage.setWidth(value);
    }

    @Override
    public void setHeight(double value) {
        this.stage.setHeight(value);
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    @Override
    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void closeWindow() {
        this.stage.close();
    }

    @Override
    protected Composer createComposer() {
        return new DefaultShellFxView.Composer();
    }

    protected DialogManager getDialogManager() {
        return this.dialogManager;
    }

    @Override
    protected void initialize() {
        super.initialize();
        getComposer().init();
        getComposer().setMenuAware(this);
    }

    @Override
    protected void build() {
        super.build();
        this.closeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.COMPACT);
//        this.closeButton.getGraphic().getStyleClass().add("icon");
        this.minimizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.COMPACT);
//        this.minimizeButton.getGraphic().getStyleClass().add("icon");
        this.maximizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.COMPACT);
//        this.maximizeButton.getGraphic().getStyleClass().add("icon");
        this.stageBox.getStyleClass().add("stage-box");
        this.leftBox.getStyleClass().add("left-box");
        this.titleBar.getStyleClass().add("title-bar");
        this.rightBox.getStyleClass().add("right-box");
        this.rightBox.setSpacing(Spacing.HORIZONTAL + Spacing.HORIZONTAL_THIRD);
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        this.contentBox.getStyleClass().add("content-box");
        HeaderBar.setPrefButtonHeight(stage, 0); // to hide default buttons
        stage.initStyle(StageStyle.EXTENDED);
        var scene = new Scene(stackPane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        FxViewUtils.setView(scene, this);
        stage.setScene(scene);
        //we add stackpane behind stage root
        stackPane.getStyleClass().add("root-stack-pane");
        var presenter = getPresenter();
        themeApplier = new ThemeApplier(scene, this.stylesheets, presenter.getContext().getSettings().getAppearance());
        this.fontApplier = new FontApplier(stackPane, presenter.getContext().getSettings().getAppearance());
        stage.show();
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.stage.maximizedProperty().addListener((ov, oldV, newV) -> {
            this.stageBox.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, newV);
            if (newV) {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
            } else {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_MAXIMIZE);
            }
        });
        this.stage.widthProperty().addListener((ov, oldV, newV) -> getPresenter().onWidthChanged(newV.doubleValue()));
        this.stage.heightProperty().addListener((ov, oldV, newV) -> getPresenter().onHeightChanged(newV.doubleValue()));
        this.stage.maximizedProperty().addListener((ov, oldV, newV) -> getPresenter().onMaximized(newV));
        this.closeButton.getStyleClass().add("close-button");
        this.minimizeButton.getStyleClass().add("minimize-button");
        this.maximizeButton.getStyleClass().add("maximize-button");
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.closeButton.setOnAction(e -> getPresenter().requestClose());
        this.maximizeButton.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        this.minimizeButton.setOnAction(e -> stage.setIconified(true));
        this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
        this.stage.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED,
                e -> menuManager.setLastMouseClickTime(System.nanoTime()));
//        var viewModel = getViewModel();
//        stage.addEventHandler(StageResizeEvent.STAGE_RESIZE_FINISHED, e -> {
//            if (!stage.isMaximized()) {
//                viewModel.setDefaultWidth(stage.getWidth());
//                viewModel.setDefaultHeight(stage.getHeight());
//            }
//        });
        stage.setOnCloseRequest(event -> {
            event.consume();
            getPresenter().requestClose();
        });
    }

    /**
     * JavaFX doesn't support the same accelerator to be installed in multiple MenuItems. See this bug:
     * https://bugs.openjdk.org/browse/JDK-8088068 . This method is a workaround for this problem.
     *
     * @param e
     */
    private void fixAcceleratorKeyPressed(KeyEvent e) {
        menuManager.setLastKeyPressedTime(System.nanoTime());
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
