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

package com.techsenger.tabshell.core.window;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.dialog.DialogResizeEvent;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.CssAnchor;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.JavaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.Spacer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowFxView<P extends AbstractWindowPresenter<?>> extends AbstractChildFxView<P>
        implements WindowFxView<P> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWindowFxView.class);

    public class Composer extends AbstractChildFxView<P>.Composer implements WindowFxView.Composer {

        private final ReadOnlyObjectWrapper<ParentFxView<?>> focused = new ReadOnlyObjectWrapper<>();

        private final AbstractWindowFxView<P> view = AbstractWindowFxView.this;

        /**
         * PauseTransition used to implement debounce on the JavaFX thread with duration.
         */
        private PauseTransition focusDebouncePause;

        public Composer() {

        }

        @Override
        public void compose() {
            super.compose();
            if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
                this.focusDebouncePause = new PauseTransition(Duration.millis(150));
                if (logger.isDebugEnabled()) {
                    this.focused.addListener((ov, oldV, newV) -> {
                        logger.debug("{} Focused component: {}", getDescriptor().getLogPrefix(),
                                (newV == null) ? null : newV.getDescriptor().getFullName());
                    });
                }
                view.stage.getScene().focusOwnerProperty().addListener((ov, oldV, newV) -> {
                    this.focusDebouncePause.stop();
                    this.focusDebouncePause.playFromStart();
                });
                this.focusDebouncePause.setOnFinished((e) -> onFocusPauseFinished());
            }
        }

        @Override
        public ReadOnlyObjectProperty<ParentFxView<?>> focusedProperty() {
            checkIfTopLevel();
            return this.focused.getReadOnlyProperty();
        }

        @Override
        public ParentFxView<?> getFocused() {
            checkIfTopLevel();
            return this.focused.get();
        }

        @Override
        public void close() {
            checkIfNested();
            var parent = getParent();
            if (parent != null) {
                ((WindowContainerFxView.Composer) parent.getComposer()).closeWindow(view);
            }
        }

        @Override
        public @Nullable WindowContainerFxView<?> getContainer() {
            return getParent(WindowContainerFxView.class);
        }

        @Override
        public @Nullable WindowContainerPort getContainerPort() {
            var container = getContainer();
            return container == null ? null : container.getPresenter();
        }

        protected void onFocusPauseFinished() {
            var newNode = view.stage.getScene().getFocusOwner();
            if (logger.isDebugEnabled()) {
                logger.debug("{} Focused node: {}", getDescriptor().getLogPrefix(), newNode);
            }
            if (newNode == null) {
                setFocused(null);
                return;
            }
            var component = FxViewUtils.findView(newNode, ParentFxView.class);
            setFocused(component);
        }

        private void setFocused(ParentFxView<?> focused) {
            this.focused.set(focused);
        }
    }

    private final IconViewBox iconViewBox = new IconViewBox();

    private final HBox leftBox = new HBox(iconViewBox);

    private final Label titleLabel = new Label();

    private final Button closeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_CLOSE));

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_MINIMIZE));

    private final FontIconView maximizeIconView = new FontIconView(CoreIcons.WINDOW_MAXIMIZE);

    private final Button maximizeButton = new Button(null, maximizeIconView);

    private final BooleanProperty maximized = new SimpleBooleanProperty();

    private final BooleanProperty minimized = new SimpleBooleanProperty();

    private final HBox rightBox = new HBox(closeButton);

    private Region titleBar;

    private final StackPane titlePane = new StackPane();

    private final VBox contentBox = new VBox();

    private final StackPane contentPane = new StackPane(contentBox);

    private final VBox windowBox = new VBox(contentPane);

    private final StackPane windowPane = new StackPane(windowBox);

    private final VBox windowRoot = new VBox(windowPane);

    private @Nullable Pane blockPane;

    private @Nullable Stage stage;

    private @Nullable ObservableList<Stylesheet> stylesheets;

    private @Nullable ThemeApplier themeApplier;

    private @Nullable FontApplier fontApplier;

    private @Nullable String density;

    /**
     * If it is true user can move dialog only with minimum top constrain. If this value is false user
     * can only move the dialog within the bounds of the parent Pane.
     */
    private boolean outOfBoundsAllowed = false;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetX;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetY;

    private boolean shadowVisible;

    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private @Nullable RegionResizer resizer;

    private final BooleanProperty resizable = new SimpleBooleanProperty();

    private @Nullable WindowManager windowManager;

    private final EventHandler<? super MouseEvent> maximizeHandler = (e) -> {
        if (e.getClickCount() == 2) {
            getPresenter().onMaximize();
        }
    };

    public AbstractWindowFxView() {
        this(null, null);
    }

    /**
     * Creates a new stage backed by the given {@link Stage}.
     *
     * <p>This constructor is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param stage       the {@link Stage} that backs this stage
     * @param stylesheets the initial list of stylesheets to apply to this stage
     */
    public AbstractWindowFxView(Stage stage, List<Stylesheet> stylesheets) {
        this.stage = stage;
        if (stylesheets != null) {
            this.stylesheets = FXCollections.observableArrayList(stylesheets);
        }
    }

    @Override
    public Region getNode() {
        return this.windowRoot;
    }

    @Override
    public void setModal(boolean modal) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.initModality(Modality.WINDOW_MODAL);
        }
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setAlwaysOnTop(alwaysOnTop);
        } else if (this.windowManager != null) {
            this.windowManager.updateWindow(this);
        }
    }

    @Override
    public void setMaximized(boolean value) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setMaximized(value);
        } else {
            if (value) {
                getComposer().getContainer().getComposer().maximizeWindow(this);
            } else {
                getComposer().getContainer().getComposer().restoreWindow(this);
            }
        }
        this.maximized.set(value);
    }

    @Override
    public void setMaximizable(boolean maximizable) {
        if (maximizable) {
            if (this.maximizeButton.getParent() == null) {
                rightBox.getChildren().add(getMaximizeButtonIndex(), this.maximizeButton);
                if (getPresenter().getWindowType() == WindowType.NESTED) {
                    titleBar.addEventHandler(MouseEvent.MOUSE_CLICKED, maximizeHandler);
                }
            }
        } else {
            if (this.maximizeButton.getParent() != null) {
                rightBox.getChildren().remove(this.maximizeButton);
                if (getPresenter().getWindowType() == WindowType.NESTED) {
                    titleBar.removeEventHandler(MouseEvent.MOUSE_CLICKED, maximizeHandler);
                }
            }
        }
    }

    @Override
    public void setMinimized(boolean minimized) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setIconified(minimized);
        }
        // todo: implement for nested windows
        this.maximized.set(minimized);
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        if (minimizable) {
            if (this.minimizeButton.getParent() == null) {
                rightBox.getChildren().add(getMinimizeButtonIndex(), this.minimizeButton);
            }
        } else {
            if (this.minimizeButton.getParent() != null) {
                rightBox.getChildren().remove(this.minimizeButton);
            }
        }
    }

    @Override
    public void setClosable(boolean closable) {
        this.closeButton.setDisable(!closable);
    }

    @Override
    public void setWidth(double value) {
        if (this.stage == null) {
            this.windowRoot.setMinWidth(value);
            this.windowRoot.setMaxWidth(value);
        } else {
            this.stage.setWidth(value);
        }
    }

    @Override
    public void setHeight(double value) {
        if (this.stage == null) {
            this.windowRoot.setMinHeight(value);
            this.windowRoot.setMaxHeight(value);
        } else {
            this.stage.setHeight(value);
        }
    }

    @Override
    public void setMinWidth(double value) {
        if (this.stage == null) {
//            this.stackPane.setMinWidth(value);
            this.minWidth.set(value);
        } else {
            this.stage.setMinWidth(value);
        }
    }

    @Override
    public void setMinHeight(double value) {
        if (this.stage == null) {
//            this.stackPane.setMinHeight(value);
            this.minHeight.set(value);
        } else {
            this.stage.setMinHeight(value);
        }
    }

    @Override
    public void setMaxWidth(double value) {
        if (this.stage == null) {
//            this.stackPane.setMaxWidth(value);
            this.maxWidth.set(value);
        } else {
            this.stage.setMaxWidth(value);
        }
    }

    @Override
    public void setMaxHeight(double value) {
        if (this.stage == null) {
//            this.stackPane.setMaxHeight(value);
            this.maxHeight.set(value);
        } else {
            this.stage.setMaxHeight(value);
        }
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
    public Stage getStage() {
        checkIfTopLevel();
        return this.stage;
    }

    @Override
    public void addStylesheets(List<Stylesheet> sheets) {
        checkIfTopLevel();
        this.stylesheets.addAll(sheets);
    }

    @Override
    public void removeStylesheets(List<Stylesheet> sheets) {
        checkIfTopLevel();
        this.stylesheets.removeAll(sheets);
    }

    @Override
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        checkIfTopLevel();
        return Collections.unmodifiableList(stylesheets);
    }

    @Override
    public void setDensity(@Nullable String density) {
        checkIfTopLevel();
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().remove(this.density);
        }
        this.density = density;
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().add(this.density);
        }
    }

    @Override
    public void setTheme(Theme theme) {
        checkIfTopLevel();
        this.themeApplier.setTheme(theme);
    }

    @Override
    public void setRegularFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setRegularFont(font);
    }

    @Override
    public void setMonospaceFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setMonospaceFont(font);
    }

    @Override
    public void setBlocked(boolean blocked) {
        if (blocked) {
            this.blockPane = new Pane();
            this.blockPane.setMouseTransparent(false);
            this.windowPane.getChildren().add(this.blockPane);
        } else {
            this.windowPane.getChildren().remove(this.blockPane);
            this.blockPane = null;
        }
    }

    @Override
    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        this.outOfBoundsAllowed = outOfBoundsAllowed;
    }

    @Override
    public void setResizable(boolean value) {
        if (this.stage == null) {
            this.resizable.set(value);
        } else {
            this.stage.setResizable(value);
        }
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
        return new AbstractWindowFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.closeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON,
                StyleClasses.SIZE_S, "close-button");
        this.minimizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_S,
                "minimize-button");
        this.maximizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_S,
                "maximize-button");
        this.windowBox.getStyleClass().add("window-box");
        this.leftBox.getStyleClass().add("left-box");
        this.rightBox.getStyleClass().add("right-box");
        this.rightBox.setSpacing(Spacing.getHorizontal() + Spacing.getHorizontalThird());
        this.contentBox.getStyleClass().add("content-box");
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        VBox.setVgrow(windowPane, Priority.ALWAYS);
        this.titlePane.getStyleClass().add("title-pane");
        this.windowPane.getStyleClass().add("window-pane");
        this.contentPane.getStyleClass().add("content-pane");

        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.titleBar = new HeaderBar(leftBox, titleLabel, rightBox);
            if (this.stage == null) {
                this.stage = new Stage();
            }
            HeaderBar.setPrefButtonHeight(stage, 0); // to hide default buttons
            stage.initStyle(StageStyle.EXTENDED);
            if (stylesheets == null) {
                this.stylesheets = FXCollections.observableArrayList();
            }
            this.stylesheets.addAll(createDefaultStylesheets());
            var scene = new Scene(windowRoot);
            stage.setScene(scene);
            themeApplier = new ThemeApplier(scene, this.stylesheets, getDescriptor());
            this.fontApplier = new FontApplier(windowRoot);
            FxViewUtils.setView(scene, this);
        } else {
            this.titleBar = new HBox(leftBox, titleLabel, new Spacer(Orientation.HORIZONTAL), rightBox);
            titleBar.getStyleClass().add(StyleClasses.CORNERS_TOP);
            this.stage = null;
            this.stylesheets = null;
            contentBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
            this.windowBox.getStyleClass().add(StyleClasses.CORNERS_ALL);
            this.windowRoot.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            this.resizer = new RegionResizer(minWidth, minHeight, maxWidth, maxHeight,
                    (e) -> {
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_STARTED, e);
                        this.windowRoot.fireEvent(event);
                    },
                    (e) -> {
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e);
                        this.windowRoot.fireEvent(event);
                    });
            this.resizer.initialize(windowRoot);
            windowRoot.getStyleClass().add("root-stack-pane");
            FxViewUtils.setView(windowRoot, this);
        }
        this.titleBar.getStyleClass().add("title-bar");
        this.titlePane.getChildren().add(this.titleBar);
        this.windowBox.getChildren().add(0, titlePane);
        setActive(false);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.widthProperty()
                    .addListener((ov, oldV, newV) -> getPresenter().onWidthChanged(newV.doubleValue()));
            this.stage.heightProperty()
                    .addListener((ov, oldV, newV) -> getPresenter().onHeightChanged(newV.doubleValue()));
            this.stage.maximizedProperty().addListener((ov, oldV, newV) -> getPresenter().onMaximized(newV));
            this.stage.focusedProperty().addListener((ov, oldV, newV) -> setActive(newV));
        } else {
            getNode().widthProperty()
                    .addListener((ov, oldV, newV) -> getPresenter().onWidthChanged(newV.doubleValue()));
            getNode().heightProperty()
                    .addListener((ov, oldV, newV) -> getPresenter().onHeightChanged(newV.doubleValue()));
        }
        this.maximized.addListener((ov, oldV, newV) -> {
            this.windowBox.pseudoClassStateChanged(PseudoClasses.MAXIMIZED, newV);
            if (newV) {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
            } else {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_MAXIMIZE);
            }
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.closeButton.setOnAction(e -> getPresenter().onCloseRequest());
        this.maximizeButton.setOnAction(e -> getPresenter().onMaximize());
        this.minimizeButton.setOnAction(e -> getPresenter().onMinimize());
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
//            var viewModel = getViewModel();
//            stage.addEventHandler(StageResizeEvent.STAGE_RESIZE_FINISHED, e -> {
//                if (!stage.isMaximized()) {
//                    viewModel.setDefaultWidth(stage.getWidth());
//                    viewModel.setDefaultHeight(stage.getHeight());
//                }
//            });
            stage.setOnCloseRequest(event -> {
                event.consume();
                getPresenter().onCloseRequest();
            });
        } else {
            titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> this.onMousePressed(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> this.onMouseDragged(event));
            windowRoot.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> windowRoot.requestFocus());
        }
    }


    @Override
    protected void bind() {
        super.bind();
        if (getPresenter().getWindowType() == WindowType.NESTED) {
            this.resizer.disabledProperty().bind(this.resizable.not().or(this.maximized).or(this.minimized));
        }
    }

    protected IconViewBox getIconViewBox() {
        return iconViewBox;
    }

    protected HBox getLeftBox() {
        return leftBox;
    }

    protected Label getTitleLabel() {
        return titleLabel;
    }

    protected Button getCloseButton() {
        return closeButton;
    }

    protected int getCloseButtonIndex() {
        return rightBox.getChildren().size();
    }

    protected Button getMinimizeButton() {
        return minimizeButton;
    }

    protected int getMinimizeButtonIndex() {
        var index = rightBox.getChildren().size();
        if (this.maximizeButton.getParent() != null) {
            index--;
        }
        if (this.closeButton.getParent() != null) {
            index--;
        }
        return index;
    }

    protected FontIconView getMaximizeIconView() {
        return maximizeIconView;
    }

    protected Button getMaximizeButton() {
        return maximizeButton;
    }

    protected int getMaximizeButtonIndex() {
        var index = rightBox.getChildren().size();
        if (this.closeButton.getParent() != null) {
            index--;
        }
        return index;
    }

    protected HBox getRightBox() {
        return rightBox;
    }

    protected Region getTitleBar() {
        return titleBar;
    }

    protected StackPane getTitlePane() {
        return titlePane;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    public StackPane getContentPane() {
        return contentPane;
    }

    protected VBox getWindowBox() {
        return windowBox;
    }

    protected Pane getBlockPane() {
        return blockPane;
    }

    protected StackPane getWindowPane() {
        return windowPane;
    }

    /**
     * JavaFX doesn't support the same accelerator to be installed in multiple MenuItems. See this bug:
     * https://bugs.openjdk.org/browse/JDK-8088068 . This method is a workaround for this problem.
     *
     * @param e
     */
    protected void fixAcceleratorKeyPressed(KeyEvent e) {
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

    protected List<Stylesheet> createDefaultStylesheets() {
        Set<Theme> allThemes = Stream.concat(
                Arrays.stream(AtlantaFxTheme.values()),
                Arrays.stream(JavaFxTheme.values()))
                .collect(Collectors.toSet());
        return List.of(
                new Stylesheet(CssAnchor.class.getResource("core.css"), Set.of(AtlantaFxTheme.values())),
                new Stylesheet(StyleClasses.class.getResource("material.css"), allThemes));
    }

    protected void setShadowVisible(boolean visible) {
        checkIfNested();
        if (this.shadowVisible == visible) {
            return;
        }
        if (visible) {
            this.windowRoot.getStyleClass().add(StyleClasses.SHADOW);
        } else {
            this.windowRoot.getStyleClass().remove(StyleClasses.SHADOW);
        }
        this.shadowVisible = visible;
    }

    protected boolean isShadowVisible() {
        checkIfNested();
        return shadowVisible;
    }

    void setActive(boolean active) {
        windowBox.pseudoClassStateChanged(PseudoClasses.INACTIVE, !active);
        getPresenter().onActiveChanged(active);
    }

    void setWindowManager(@Nullable WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    private void checkIfTopLevel() {
        if (getPresenter().getWindowType() != WindowType.TOP_LEVEL) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.NESTED
                    + " Window");
        }
    }

    private void checkIfNested() {
        if (getPresenter().getWindowType() != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (isMoving()) {
            offsetX = event.getSceneX() - this.windowRoot.getLayoutX();
            offsetY = event.getSceneY() - this.windowRoot.getLayoutY();
            event.consume();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (isMoving()) {
            var newX = event.getSceneX() - offsetX;
            var newY = event.getSceneY() - offsetY;
            var parent = (Pane) this.windowRoot.getParent();
            //checking position constraints
            if (newY < 0) {
                newY = 0;
            }
            if (!outOfBoundsAllowed) {
                if (newX < 0) {
                    newX = 0;
                }
                if ((newX + this.windowRoot.getWidth()) > parent.getWidth()) {
                    newX = parent.getWidth() - this.windowRoot.getWidth();
                }
                if ((newY + this.windowRoot.getHeight()) > parent.getHeight()) {
                    newY = parent.getHeight() - this.windowRoot.getHeight();
                }
            }
            this.windowRoot.setLayoutX(newX);
            this.windowRoot.setLayoutY(newY);
            event.consume();
        }
    }

    /**
     * There is also a resizing handlers. so, we check cursor type to know if resizing is enabled.
     *
     * @return
     */
    private boolean isMoving() {
        var currentCursor = this.windowRoot.getCursor();
        return (currentCursor == null || currentCursor == Cursor.DEFAULT);
    }
}
