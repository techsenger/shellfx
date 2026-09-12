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
import com.techsenger.patternfx.mvvm.AbstractChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.dialog.DialogResizeEvent;
import com.techsenger.shellfx.core.style.CoreIcons;
import com.techsenger.shellfx.core.style.CssAnchor;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.shellfx.material.style.Density;
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
import com.techsenger.toolkit.fx.value.ValueUtils;
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
public abstract class AbstractWindowView<VM extends AbstractWindowViewModel<?>> extends AbstractChildView<VM>
        implements WindowView<VM> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWindowView.class);

    public class Composer extends AbstractChildView<VM>.Composer implements WindowView.Composer {

        private final ReadOnlyObjectWrapper<ParentView<?>> focused = new ReadOnlyObjectWrapper<>();

        private final AbstractWindowView<VM> view = AbstractWindowView.this;

        /**
         * PauseTransition used to implement debounce on the JavaFX thread with duration.
         */
        private PauseTransition focusDebouncePause;

        @Override
        public void compose() {
            super.compose();
            if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
                this.focusDebouncePause = new PauseTransition(Duration.millis(150));
                if (logger.isDebugEnabled()) {
                    this.focused.addListener((ov, oldV, newV) -> {
                        logger.debug("{} Focused component: {}", getDescriptor().getLogPrefix(),
                                (newV == null) ? null : newV.getViewModel().getDescriptor().getFullName());
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
        public ReadOnlyObjectProperty<@Nullable ParentView<?>> focusedProperty() {
            checkIfTopLevel();
            return this.focused.getReadOnlyProperty();
        }

        @Override
        public @Nullable ParentView<?> getFocused() {
            checkIfTopLevel();
            return this.focused.get();
        }

        @Override
        public void close() {
            checkIfNested();
            var parent = getParent();
            if (parent != null) {
                ((WindowContainerView.Composer) parent.getComposer()).closeWindow(view);
            }
        }

        @Override
        public @Nullable WindowContainerView<?> getParent() {
            checkIfNested();
            return (WindowContainerView<?>) super.getParent();
        }

        @Override
        public @Nullable WindowContainerPort getParentPort() {
            checkIfNested();
            var container = getParent();
            return container == null ? null : container.getViewModel();
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
            var component = ViewUtils.findView(newNode, ParentView.class);
            setFocused(component);
        }

        private void setFocused(ParentView<?> focused) {
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
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetX;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetY;

    private boolean shadowVisible;

    /**
     * The minimum width calculated from the window content.
     */
    private final DoubleProperty calculatedMinWidth = new SimpleDoubleProperty();

    /**
     * The minimum height calculated from the window content.
     */
    private final DoubleProperty calculatedMinHeight = new SimpleDoubleProperty();

    private @Nullable RegionResizer resizer;

    private @Nullable WindowManager windowManager;

    private boolean resizingInProgress;

    private final EventHandler<? super MouseEvent> maximizeHandler = (e) -> {
        if (e.getClickCount() == 2) {
            getViewModel().onMaximize();
        }
    };

    public AbstractWindowView(VM viewModel) {
        this(viewModel, null, null);
    }

    /**
     * Creates a new stage backed by the given {@link Stage}.
     *
     * <p>This constructor is intended for {@link WindowType#TOP_LEVEL} windows only.
     *
     * @param viewModel   the view model of this window
     * @param stage       the {@link Stage} that backs this stage
     * @param stylesheets the initial list of stylesheets to apply to this stage
     */
    public AbstractWindowView(VM viewModel, Stage stage, List<Stylesheet> stylesheets) {
        super(viewModel);
        this.stage = stage;
        this.stylesheetManager = new StylesheetManager(
                () -> getViewModel().getWindowType() == WindowType.TOP_LEVEL
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
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractWindowView<VM>.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.closeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SQUARE,
                StyleClasses.SIZE_S, "close-button");
        this.minimizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SQUARE, StyleClasses.SIZE_S,
                "minimize-button");
        this.maximizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.SQUARE, StyleClasses.SIZE_S,
                "maximize-button");
        this.windowBox.getStyleClass().add("window-box");
        this.leftBox.getStyleClass().add("left-box");
        this.rightBox.getStyleClass().add("right-box");
        this.rightBox.setSpacing(Spacing.getHorizontal() + Spacing.getHorizontalThird());
        this.contentBox.getStyleClass().add("content-box");
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        this.titlePane.getStyleClass().add("title-pane");
        this.contentPane.getStyleClass().add("content-pane");
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
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
            ViewUtils.setView(scene, this);
            if (viewModel.isModal()) {
                // it is not possible to change modality for the primary stage after it has been shown
                this.stage.initModality(Modality.WINDOW_MODAL);
            }
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
            resultMinWidth.bind(Bindings.when(viewModel.minWidthProperty().isEqualTo(0))
                    .then(calculatedMinWidth)
                    .otherwise(Bindings.min(viewModel.minWidthProperty(), calculatedMinWidth)));
            DoubleProperty resultMinHeight = new SimpleDoubleProperty();
            resultMinHeight.bind(Bindings.when(viewModel.minHeightProperty().isEqualTo(0))
                    .then(calculatedMinHeight)
                    .otherwise(Bindings.min(viewModel.minHeightProperty(), calculatedMinHeight)));
            this.resizer = new RegionResizer(resultMinWidth, resultMinHeight, viewModel.maxWidthProperty(),
                    viewModel.maxHeightProperty(),
                    (e) -> {
                        resizingInProgress = true;
                        getViewModel().resizingProperty().set(true);
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_STARTED, e);
                        this.windowNode.fireEvent(event);
                        calculateMinSize();
                        // it is necessary to set sizes for both sides, as we don't know which side the user will resize
                        updateWidth(windowNode.getWidth());
                        updateHeight(windowNode.getHeight());
                    },
                    (e) -> {
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e);
                        this.windowNode.fireEvent(event);
                        resizingInProgress = false;
                        getViewModel().resizingProperty().set(false);
                    });
            this.resizer.initialize(windowNode);
            ViewUtils.setView(windowNode, this);
        }
        this.titleBar.getStyleClass().add("title-bar");
        this.titlePane.getChildren().add(this.titleBar);
        this.windowBox.getChildren().add(0, titlePane);
        windowNode.getStyleClass().add("window");
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        titleLabel.textProperty().bind(viewModel.titleProperty());
        iconViewBox.iconProperty().bind(viewModel.iconProperty());
        closeButton.disableProperty().bind(viewModel.closableProperty().not());
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            // it is necessary to update the title of the stage because it is shown in the title bar of the OS
            this.stage.titleProperty().bind(this.titleLabel.textProperty());
            viewModel.widthWrapper().bind(this.stage.widthProperty());
            viewModel.heightWrapper().bind(this.stage.heightProperty());
            viewModel.maximizedWrapper().bind(this.stage.maximizedProperty());
            viewModel.minimizedWrapper().bind(this.stage.iconifiedProperty());
            viewModel.xWrapper().bind(this.stage.xProperty());
            viewModel.yWrapper().bind(this.stage.yProperty());
            this.stage.minWidthProperty().bind(viewModel.minWidthProperty());
            this.stage.minHeightProperty().bind(viewModel.minHeightProperty());
            this.stage.maxWidthProperty().bind(viewModel.maxWidthProperty());
            this.stage.maxHeightProperty().bind(viewModel.maxHeightProperty());
        } else {
            this.resizer.disabledProperty().bind(viewModel.resizableProperty().not()
                    .or(this.maximized).or(this.minimized));
            viewModel.widthWrapper().bind(windowNode.widthProperty());
            viewModel.heightWrapper().bind(windowNode.heightProperty());
            viewModel.xWrapper().bind(windowNode.layoutXProperty());
            viewModel.yWrapper().bind(windowNode.layoutYProperty());
        }
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        viewModel.closeWindowSource().addListener((newV) -> this.stage.close());
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.maximizedProperty().addListener((ov, oldV, newV) -> this.maximized.set(newV));
            this.stage.focusedProperty().addListener((ov, oldV, newV) -> getViewModel().setActive(newV));
            ValueUtils.callAndAddListener(viewModel.resizableProperty(),
                    (ov, oldV, newV) -> this.stage.setResizable(newV));
        } else {
            windowNode.widthProperty().addListener((ov, oldV, newV) -> checkContentFits());
            windowNode.heightProperty().addListener((ov, oldV, newV) -> checkContentFits());
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
        ValueUtils.callAndAddListener(viewModel.alwaysOnTopProperty(), (ov, oldV, newV) -> updateAlwaysOnTop(newV));
        updateMaximized(viewModel.isMaximized());
        viewModel.maximizedSource().addListener((value) -> updateMaximized(value));
        ValueUtils.callAndAddListener(viewModel.maximizableProperty(), (ov, oldV, newV) -> updateMaximizable(newV));
        updateMinimized(viewModel.isMinimized());
        viewModel.minimizedSource().addListener((value) -> updateMinimized(value));
        ValueUtils.callAndAddListener(viewModel.minimizableProperty(), (ov, oldV, newV) -> updateMinimizable(newV));
        viewModel.widthSource().addListener((value) -> updateWidth(value));
        viewModel.heightSource().addListener((value) -> updateHeight(value));
        updateX(viewModel.getX());
        viewModel.xSource().addListener((value) -> updateX(value));
        updateY(viewModel.getY());
        viewModel.ySource().addListener((value) -> updateY(value));

        ValueUtils.callAndAddListener(viewModel.themeProperty(), (ov, oldV, newV) -> updateTheme(newV));
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            // density/font only ever apply to a TOP_LEVEL window's own Scene/FontApplier; a NESTED window's
            // density/font come from its enclosing TOP_LEVEL window instead, so no listener is needed here.
            ValueUtils.callAndAddListener(viewModel.densityProperty(), (ov, oldV, newV) -> updateDensity(newV));
            ValueUtils.callAndAddListener(viewModel.regularFontProperty(),
                    (ov, oldV, newV) -> updateRegularFont(newV));
            ValueUtils.callAndAddListener(viewModel.monospaceFontProperty(),
                    (ov, oldV, newV) -> updateMonospaceFont(newV));
        }
        ValueUtils.callAndAddListener(viewModel.blockedProperty(), (ov, oldV, newV) -> updateBlocked(newV));
        ValueUtils.callAndAddListener(viewModel.activeProperty(), (ov, oldV, newV) -> updateActive(newV));
    }

    private void updateAlwaysOnTop(boolean alwaysOnTop) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setAlwaysOnTop(alwaysOnTop);
        } else if (this.windowManager != null) {
            this.windowManager.updateWindow(this);
        }
    }

    private void updateMaximized(boolean maximized) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setMaximized(maximized);
        } else {
            var parent = getComposer().getParent();
            if (parent != null) {
                if (maximized) {
                    parent.getComposer().maximizeWindow(this);
                } else {
                    parent.getComposer().restoreWindow(this);
                }
            }
        }
        this.maximized.set(maximized);
    }

    private void updateMaximizable(boolean maximizable) {
        if (maximizable) {
            if (this.maximizeButton.getParent() == null) {
                rightBox.getChildren().add(getMaximizeButtonIndex(), this.maximizeButton);
                if (getViewModel().getWindowType() == WindowType.NESTED) {
                    titleBar.addEventHandler(MouseEvent.MOUSE_CLICKED, maximizeHandler);
                }
            }
        } else {
            if (this.maximizeButton.getParent() != null) {
                rightBox.getChildren().remove(this.maximizeButton);
                if (getViewModel().getWindowType() == WindowType.NESTED) {
                    titleBar.removeEventHandler(MouseEvent.MOUSE_CLICKED, maximizeHandler);
                }
            }
        }
    }

    private void updateMinimized(boolean minimized) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.setIconified(minimized);
        } else {
            var parent = getComposer().getParent();
            if (parent != null) {
                if (minimized) {
                    parent.getComposer().minimizeWindow(this);
                } else {
                    parent.getComposer().restoreWindow(this);
                }
            }
        }
        this.minimized.set(minimized);
    }

    private void updateMinimizable(boolean minimizable) {
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

    private void updateDensity(@Nullable Density density) {
        checkIfTopLevel();
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().remove(this.density.getStyleClass());
        }
        this.density = density;
        if (this.density != null) {
            this.stage.getScene().getRoot().getStyleClass().add(this.density.getStyleClass());
        }
    }

    private void updateTheme(Theme theme) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        }
        this.stylesheetManager.setTheme(theme);
    }

    private void updateRegularFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setRegularFont(font);
    }

    private void updateMonospaceFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setMonospaceFont(font);
    }

    private void updateBlocked(boolean blocked) {
        if (blocked) {
            this.blockPane = new Pane();
            this.blockPane.setMouseTransparent(false);
            this.windowNode.getChildren().add(this.blockPane);
        } else {
            this.windowNode.getChildren().remove(this.blockPane);
            this.blockPane = null;
        }
    }

    void updateActive(boolean active) {
        windowBox.pseudoClassStateChanged(PseudoClasses.INACTIVE, !active);
    }

    private void updateX(double x) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            // NaN is the Stage's own sentinel for "not positioned yet"; forwarding it into setX would mark the
            // Stage as explicitly positioned and suppress the platform's default centering on first show()
            if (!Double.isNaN(x)) {
                this.stage.setX(x);
            }
        } else {
            this.windowNode.setLayoutX(x);
        }
    }

    private void updateY(double y) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            if (!Double.isNaN(y)) {
                this.stage.setY(y);
            }
        } else {
            this.windowNode.setLayoutY(y);
        }
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.closeButton.setOnAction(e -> viewModel.onCloseRequest());
        this.maximizeButton.setOnAction(e -> viewModel.onMaximize());
        this.minimizeButton.setOnAction(e -> viewModel.onMinimize());
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            this.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
            stage.setOnCloseRequest(event -> {
                event.consume();
                viewModel.onCloseRequest();
            });
        } else {
            titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> this.onMousePressed(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> this.onMouseDragged(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> this.onMouseReleased(event));
            windowNode.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> windowNode.requestFocus());
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

    void setWindowManager(@Nullable WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    void onMaximized(boolean maximized) {
        getViewModel().maximizedWrapper().set(maximized);
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
        getViewModel().minimizedWrapper().set(minimized);
        this.minimized.set(minimized);
    }

    private void updateWidth(double value) {
        if (this.stage == null) {
            this.windowNode.setMinWidth(value);
            this.windowNode.setMaxWidth(value);
        } else {
            this.stage.setWidth(value);
        }
    }

    private void updateHeight(double value) {
        if (this.stage == null) {
            this.windowNode.setMinHeight(value);
            this.windowNode.setMaxHeight(value);
        } else {
            this.stage.setHeight(value);
        }
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
        if (getViewModel().getWindowType() != WindowType.TOP_LEVEL) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.NESTED
                    + " Window");
        }
    }

    private void checkIfNested() {
        if (getViewModel().getWindowType() != WindowType.NESTED) {
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
            if (!getViewModel().isOutOfBoundsAllowed()) {
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
        var width = windowBox.minWidth(-1) + windowNode.getPadding().getLeft() + windowNode.getPadding().getRight();
        this.calculatedMinWidth.set(width);
        var height = windowBox.minHeight(-1) + windowNode.getPadding().getTop() + windowNode.getPadding().getBottom();
        this.calculatedMinHeight.set(height);
    }
}
