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

package com.techsenger.shellfx.core.window;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.AbstractChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.shellfx.core.dialog.DialogResizeEvent;
import com.techsenger.shellfx.core.settings.Density;
import com.techsenger.shellfx.core.style.CoreIcons;
import com.techsenger.shellfx.core.style.CssAnchor;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.shellfx.material.style.IconStylesheets;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.material.style.Stylesheet;
import com.techsenger.shellfx.material.style.StylesheetManager;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.JavaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.Spacer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
        public ReadOnlyObjectProperty<@Nullable ParentFxView<?>> focusedProperty() {
            checkIfTopLevel();
            return this.focused.getReadOnlyProperty();
        }

        @Override
        public @Nullable ParentFxView<?> getFocused() {
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
            checkIfNested();
            return getParent(WindowContainerFxView.class);
        }

        @Override
        public @Nullable WindowContainerPort getContainerPort() {
            checkIfNested();
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

    /**
     * This icon box is always present to prevent flickering when an icon is added or removed. For example, its minimum
     * and maximum sizes can be set.
     */
    private final IconViewBox iconViewBox = new IconViewBox();

    private final Label titleLabel = new Label();

    private final HBox leftBox = new HBox(iconViewBox, titleLabel);

    private final Button closeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_CLOSE));

    private final FontIconView minimizeIconView = new FontIconView(CoreIcons.WINDOW_MINIMIZE);

    private final Button minimizeButton = new Button(null, minimizeIconView);

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

    private final StackPane windowNode = new StackPane(windowBox);

    private final StylesheetManager stylesheetManager;

    private @Nullable Pane blockPane;

    private @Nullable Stage stage;

    private @Nullable FontApplier fontApplier;

    private @Nullable Density density;

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

    /**
     * The minimum width explicitly specified by the user.
     */
    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    /**
     * The minimum height explicitly specified by the user.
     */
    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    /**
     * The minimum width calculated from the window content.
     */
    private final DoubleProperty calculatedMinWidth = new SimpleDoubleProperty();

    /**
     * The minimum height calculated from the window content.
     */
    private final DoubleProperty calculatedMinHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private @Nullable RegionResizer resizer;

    private final BooleanProperty resizable = new SimpleBooleanProperty(true);

    private @Nullable WindowManager windowManager;

    private boolean resizingInProgress;

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
        this.stylesheetManager = new StylesheetManager(
                () -> getPresenter().getWindowType() == WindowType.TOP_LEVEL
                        ? this.stage.getScene().getStylesheets()
                        : this.windowNode.getStylesheets(),
                () -> {
                    var descriptor = getDescriptor();
                    if (descriptor != null) {
                        return descriptor.getLogPrefix();
                    } else {
                        return null;
                    }
                });
        if (stylesheets != null) {
            stylesheetManager.addStylesheets(stylesheets);
        }
    }

    @Override
    public Region getNode() {
        return this.windowNode;
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
    public void setMaximized(boolean maximized) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setMaximized(maximized);
        } else {
            if (maximized) {
                getComposer().getContainer().getComposer().maximizeWindow(this);
            } else {
                getComposer().getContainer().getComposer().restoreWindow(this);
            }
        }
        this.maximized.set(maximized);
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
        } else {
            if (minimized) {
                getComposer().getContainer().getComposer().minimizeWindow(this);
            } else {
                getComposer().getContainer().getComposer().restoreWindow(this);
            }
        }
        this.minimized.set(minimized);
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
            this.windowNode.setMinWidth(value);
            this.windowNode.setMaxWidth(value);
        } else {
            this.stage.setWidth(value);
        }
    }

    @Override
    public void setHeight(double value) {
        if (this.stage == null) {
            this.windowNode.setMinHeight(value);
            this.windowNode.setMaxHeight(value);
        } else {
            this.stage.setHeight(value);
        }
    }

    @Override
    public void setMinWidth(double value) {
        if (this.stage == null) {
            this.minWidth.set(value);
        } else {
            this.stage.setMinWidth(value);
        }
    }

    @Override
    public void setMinHeight(double value) {
        if (this.stage == null) {
            this.minHeight.set(value);
        } else {
            this.stage.setMinHeight(value);
        }
    }

    @Override
    public void setMaxWidth(double value) {
        if (this.stage == null) {
            this.maxWidth.set(value);
        } else {
            this.stage.setMaxWidth(value);
        }
    }

    @Override
    public void setMaxHeight(double value) {
        if (this.stage == null) {
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
        this.stylesheetManager.addStylesheets(sheets);
    }

    @Override
    public void removeStylesheets(List<Stylesheet> sheets) {
        this.stylesheetManager.removeStylesheets(sheets);
    }

    @Override
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        return this.stylesheetManager.getStylesheets();
    }

    @Override
    public void setDensity(@Nullable Density density) {
        checkIfTopLevel();
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().remove(this.density.getStyleClass());
        }
        this.density = density;
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().add(this.density.getStyleClass());
        }
    }

    @Override
    public void setTheme(Theme theme) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        }
        this.stylesheetManager.setTheme(theme);
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
            this.windowNode.getChildren().add(this.blockPane);
        } else {
            this.windowNode.getChildren().remove(this.blockPane);
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
    public void setX(double x) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setX(x);
        } else {
            this.windowNode.setLayoutX(x);
        }
    }

    @Override
    public void setY(double y) {
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setY(y);
        } else {
            this.windowNode.setLayoutY(y);
        }
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
        this.titlePane.getStyleClass().add("title-pane");
        this.contentPane.getStyleClass().add("content-pane");
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            this.titleBar = new HeaderBar(leftBox, null, rightBox);
            if (this.stage == null) {
                this.stage = new Stage();
            }
            HeaderBar.setPrefButtonHeight(stage, 0); // to hide default buttons
            stage.initStyle(StageStyle.EXTENDED);
            var scene = new Scene(windowNode);
            stage.setScene(scene);
            this.stylesheetManager.addStylesheets(createDefaultStylesheets());
            this.fontApplier = new FontApplier(windowNode);
            FxViewUtils.setView(scene, this);
        } else {
            this.titleBar = new HBox(leftBox, titleLabel, new Spacer(Orientation.HORIZONTAL), rightBox);
            titleBar.getStyleClass().add(StyleClasses.CORNERS_TOP);
            titleBar.setMinHeight(Region.USE_PREF_SIZE);
            VBox.setVgrow(this.titleBar, Priority.NEVER);
            this.stage = null;
            contentBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
            this.windowBox.getStyleClass().add(StyleClasses.CORNERS_ALL);
            this.windowNode.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            this.windowNode.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            this.windowNode.setLayoutX(0.0);
            this.windowNode.setLayoutY(0.0);
            DoubleProperty resultMinWidth = new SimpleDoubleProperty();
            resultMinWidth.bind(Bindings.when(minWidth.isEqualTo(0))
                    .then(calculatedMinWidth)
                    .otherwise(Bindings.min(minWidth, calculatedMinWidth)));
            DoubleProperty resultMinHeight = new SimpleDoubleProperty();
            resultMinHeight.bind(Bindings.when(minHeight.isEqualTo(0))
                    .then(calculatedMinHeight)
                    .otherwise(Bindings.min(minHeight, calculatedMinHeight)));
            this.resizer = new RegionResizer(resultMinWidth, resultMinHeight, maxWidth, maxHeight,
                    (e) -> {
                        resizingInProgress = true;
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_STARTED, e);
                        this.windowNode.fireEvent(event);
                        calculateMinSize();
                        // it is necessary to set sizes for both sides, as we don't know which side the user will resize
                        setWidth(windowNode.getWidth());
                        setHeight(windowNode.getHeight());
                    },
                    (e) -> {
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e);
                        this.windowNode.fireEvent(event);
                        resizingInProgress = false;
                    });
            this.resizer.initialize(windowNode);
            FxViewUtils.setView(windowNode, this);
        }
        this.titleBar.getStyleClass().add("title-bar");
        this.titlePane.getChildren().add(this.titleBar);
        this.windowBox.getChildren().add(0, titlePane);
        windowNode.getStyleClass().add("window");
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
            this.stage.maximizedProperty().addListener((ov, oldV, newV) -> {
                getPresenter().onMaximized(newV);
                this.maximized.set(newV);
            });
            this.stage.focusedProperty().addListener((ov, oldV, newV) -> setActive(newV));
            this.stage.xProperty().addListener((ov, oldV, newV) -> getPresenter().onXChanged(newV.doubleValue()));
            this.stage.yProperty().addListener((ov, oldV, newV) -> getPresenter().onYChanged(newV.doubleValue()));
        } else {
            windowNode.widthProperty().addListener((ov, oldV, newV) -> {
                getPresenter().onWidthChanged(newV.doubleValue());
                checkContentFits();
            });
            windowNode.heightProperty().addListener((ov, oldV, newV) -> {
                getPresenter().onHeightChanged(newV.doubleValue());
                checkContentFits();
            });
            windowNode.layoutXProperty().addListener((ov, oldV, newV) -> getPresenter().onXChanged(newV.doubleValue()));
            windowNode.layoutYProperty().addListener((ov, oldV, newV) -> getPresenter().onYChanged(newV.doubleValue()));
            this.minimized.addListener((ov, oldV, newV) -> {
                if (newV) {
                    this.minimizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
                } else {
                    this.minimizeIconView.setIcon(CoreIcons.WINDOW_MINIMIZE);
                }
            });
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
            stage.setOnCloseRequest(event -> {
                event.consume();
                getPresenter().onCloseRequest();
            });
        } else {
            titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> this.onMousePressed(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> this.onMouseDragged(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> this.onMouseReleased(event));
            windowNode.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> windowNode.requestFocus());
        }
    }

    @Override
    protected void bind() {
        super.bind();
        if (getPresenter().getWindowType() == WindowType.TOP_LEVEL) {
            // it is necessary to update the title of the stage because it is shown in the title bar of the OS
            this.stage.titleProperty().bind(this.titleLabel.textProperty());
        } else {
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

    protected FontIconView getMinimizeIconView() {
        return minimizeIconView;
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

    protected @Unmodifiable List<Stylesheet> createDefaultStylesheets() {
        Set<Theme> allThemes = Stream.concat(
                Arrays.stream(AtlantaFxTheme.values()),
                Arrays.stream(JavaFxTheme.values()))
                .collect(Collectors.toSet());
        List<Stylesheet> result = new ArrayList<>();
        result.add(new Stylesheet(CssAnchor.class.getResource("core.css"), Set.of(AtlantaFxTheme.values())));
        result.add(new Stylesheet(StyleClasses.class.getResource("material.css"), allThemes));
        result.addAll(IconStylesheets.getAll());
        return Collections.unmodifiableList(result);
    }

    protected void setShadowVisible(boolean visible) {
        checkIfNested();
        if (this.shadowVisible == visible) {
            return;
        }
        if (visible) {
            this.windowNode.getStyleClass().add(StyleClasses.SHADOW);
        } else {
            this.windowNode.getStyleClass().remove(StyleClasses.SHADOW);
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

    void onMaximized(boolean maximized) {
        var pane = getContentPane();
        getPresenter().onMaximized(maximized);
        this.maximized.set(maximized);
    }

    void onMinimized(boolean minimized) {
        if (minimized) {
            titleBar.getStyleClass().remove(StyleClasses.CORNERS_TOP);
            titleBar.getStyleClass().add(StyleClasses.CORNERS_ALL);
            calculateMinSize();
        } else {
            titleBar.getStyleClass().remove(StyleClasses.CORNERS_ALL);
            titleBar.getStyleClass().add(StyleClasses.CORNERS_TOP);
        }
        getPresenter().onMinimized(minimized);
        this.minimized.set(minimized);
    }

    /**
     * Hides the content if the window is too small to fit it during resizing (manual or animated).
     */
    private void checkContentFits() {
        var containerHeight = windowNode.getHeight() - windowNode.getPadding().getTop()
                        - windowNode.getPadding().getBottom();
        var containerWidth = windowNode.getWidth() - windowNode.getPadding().getLeft()
                        - windowNode.getPadding().getRight();
        boolean fits = containerHeight >= calculatedMinHeight.get() && containerWidth >= calculatedMinWidth.get();
        contentPane.setVisible(fits);
        contentPane.setManaged(fits);
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
        if (!resizingInProgress) {
            offsetX = event.getSceneX() - this.windowNode.getLayoutX();
            offsetY = event.getSceneY() - this.windowNode.getLayoutY();
            event.consume();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (!resizingInProgress) {
            this.titleBar.setCursor(Cursor.CLOSED_HAND);
            var newX = event.getSceneX() - offsetX;
            var newY = event.getSceneY() - offsetY;
            var parent = (Pane) this.windowNode.getParent();
            //checking position constraints
            if (newY < 0) {
                newY = 0;
            }
            if (!outOfBoundsAllowed) {
                if (newX < 0) {
                    newX = 0;
                }
                if ((newX + this.windowNode.getWidth()) > parent.getWidth()) {
                    newX = parent.getWidth() - this.windowNode.getWidth();
                }
                if ((newY + this.windowNode.getHeight()) > parent.getHeight()) {
                    newY = parent.getHeight() - this.windowNode.getHeight();
                }
            }
            this.windowNode.setLayoutX(newX);
            this.windowNode.setLayoutY(newY);
            event.consume();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (!resizingInProgress) {
            this.titleBar.setCursor(Cursor.DEFAULT);
        }
    }

    private void calculateMinSize() {
        var width = contentPane.minWidth(-1) + windowNode.getPadding().getLeft() + windowNode.getPadding().getRight();
        this.calculatedMinWidth.set(width);
        var height = contentPane.minHeight(-1) + windowNode.getPadding().getTop() + windowNode.getPadding().getBottom()
                + titlePane.getHeight();
        this.calculatedMinHeight.set(height);
    }
}
