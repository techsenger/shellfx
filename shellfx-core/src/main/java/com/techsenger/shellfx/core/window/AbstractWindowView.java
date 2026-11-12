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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvvm.AbstractChildView;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.patternfx.mvvm.ViewUtils;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.style.CssAnchor;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.shellfx.material.style.Stylesheet;
import com.techsenger.shellfx.material.theme.AtlantaFxTheme;
import com.techsenger.shellfx.material.theme.JavaFxTheme;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.pulse.PulseListenerManager;
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base view of a window: a surface with a position, a size, a visibility and a focus. A top-level window is
 * backed by a JavaFX window, a nested one by a node of the window manager. The title bar, the buttons and the
 * border belong to {@code AbstractFrameWindowView}.
 *
 * @param <VM> the view model of the window
 * @author Pavel Castornii
 */
public abstract class AbstractWindowView<VM extends AbstractWindowViewModel<?>> extends AbstractChildView<VM>
        implements WindowView<VM> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWindowView.class);

    /**
     * Creates the stylesheets that windows need to look right: the platform styles. The application registers them
     * in the {@link StylesheetRegistry} before the first window is created and before the other stylesheets.
     *
     * @return the default stylesheets
     */
    public static @Unmodifiable List<Stylesheet> createDefaultStylesheets() {
        Set<Theme> allThemes = Stream.concat(
                Arrays.stream(AtlantaFxTheme.values()),
                Arrays.stream(JavaFxTheme.values()))
                .collect(Collectors.toSet());
        List<Stylesheet> result = new ArrayList<>();
        result.add(new Stylesheet(CssAnchor.class.getResource("core.css").toExternalForm(),
                Set.of(AtlantaFxTheme.values())));
        result.add(new Stylesheet(StyleClasses.class.getResource("material.css").toExternalForm(), allThemes));
        return Collections.unmodifiableList(result);
    }

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
                view.window.getScene().focusOwnerProperty().addListener((ov, oldV, newV) -> {
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
            var newNode = view.window.getScene().getFocusOwner();
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

    private final VBox contentBox = new VBox();

    private final StackPane contentPane = new StackPane(contentBox);

    /**
     * Keeps the preferred size of the window within the minimum and maximum size of the view model, so a window
     * is opened with a size that follows them, not only the content.
     */
    private final VBox windowBox = new VBox(contentPane) {

        @Override
        protected double computePrefWidth(double height) {
            var viewModel = getViewModel();
            return Math.max(viewModel.getMinWidth(), Math.min(viewModel.getMaxWidth(), super.computePrefWidth(height)));
        }

        @Override
        protected double computePrefHeight(double width) {
            var viewModel = getViewModel();
            return Math.max(viewModel.getMinHeight(),
                    Math.min(viewModel.getMaxHeight(), super.computePrefHeight(width)));
        }
    };

    private final StackPane windowNode = new StackPane(windowBox);

    private final StylesheetRegistry stylesheetRegistry;

    /**
     * The minimum width calculated from the window content.
     */
    private final DoubleProperty calculatedMinWidth = new SimpleDoubleProperty();

    /**
     * The minimum height calculated from the window content.
     */
    private final DoubleProperty calculatedMinHeight = new SimpleDoubleProperty();

    private @Nullable StylesheetManager stylesheetManager;

    private @Nullable Window window;

    private @Nullable InputEvent inputEvent;

    private @Nullable FontApplier fontApplier;

    private Density density = Density.DEFAULT;

    private @Nullable WindowManager windowManager;

    private final PulseListenerManager pulseListenerManager;

    private boolean shadowVisible;

    /**
     * Creates a new window backed by the given JavaFX window.
     *
     * @param viewModel the view model of this window
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param window the JavaFX window that backs a {@link WindowType#TOP_LEVEL} window and already shows a scene
     *     whose root is {@link #getNode()}; {@code null} for a {@link WindowType#NESTED} one
     */
    public AbstractWindowView(VM viewModel, StylesheetRegistry stylesheetRegistry, @Nullable Window window) {
        super(viewModel);
        this.window = window;
        this.stylesheetRegistry = stylesheetRegistry;
        this.pulseListenerManager = new PulseListenerManager(getDescriptor().getFullName(), () -> {
            if (viewModel.getWindowType() == WindowType.NESTED) {
                return getNode().sceneProperty();
            } else {
                return getWindow().sceneProperty();
            }
        });
    }

    @Override
    public Region getNode() {
        return this.windowNode;
    }

    @Override
    public Window getWindow() {
        checkIfTopLevel();
        return this.window;
    }

    @Override
    public @Nullable InputEvent getInputEvent() {
        checkIfTopLevel();
        return this.inputEvent;
    }

    @Override
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        checkIfTopLevel();
        return this.stylesheetManager.getStylesheets();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    public StackPane getContentPane() {
        return contentPane;
    }

    @Override
    protected Composer createComposer() {
        return new AbstractWindowView<VM>.Composer();
    }

    @Override
    protected void build() {
        super.build();
        this.windowBox.getStyleClass().add("window-box");
        this.contentBox.getStyleClass().add("content-box");
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        this.contentPane.getStyleClass().add("content-pane");
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            var scene = this.window.getScene();
            this.stylesheetManager = new StylesheetManager(scene.getStylesheets(), getDescriptor().getLogPrefix(),
                    stylesheetRegistry);
            this.fontApplier = new FontApplier(windowNode);
            ViewUtils.setView(scene, this);
        } else {
            this.windowBox.getStyleClass().add(StyleClasses.CORNERS_ALL);
            this.windowNode.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            this.windowNode.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            this.windowNode.setLayoutX(0.0);
            this.windowNode.setLayoutY(0.0);
            ViewUtils.setView(windowNode, this);
        }
        windowNode.getStyleClass().add("window");
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            viewModel.widthWrapper().bind(this.window.widthProperty());
            viewModel.heightWrapper().bind(this.window.heightProperty());
            viewModel.xWrapper().bind(this.window.xProperty());
            viewModel.yWrapper().bind(this.window.yProperty());
        } else {
            // a nested window follows the visibility of its node; an invisible one takes no place in the layout
            windowNode.visibleProperty().bindBidirectional(viewModel.visibleProperty());
            windowNode.managedProperty().bind(windowNode.visibleProperty());
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
        viewModel.closeWindowSource().addListener((newV) -> this.window.hide());
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            this.window.focusedProperty().addListener((ov, oldV, newV) -> getViewModel().setActive(newV));
            this.window.showingProperty().addListener((ov, oldV, newV) -> getViewModel().setVisible(newV));
        } else {
            windowNode.widthProperty().addListener((ov, oldV, newV) -> checkContentFits());
            windowNode.heightProperty().addListener((ov, oldV, newV) -> checkContentFits());
            ValueUtils.callAndAddListener(viewModel.alwaysOnTopProperty(),
                    (ov, oldV, newV) -> updateAlwaysOnTop());
        }
        viewModel.minWidthProperty().addListener((ov, oldV, newV) -> this.windowBox.requestLayout());
        viewModel.maxWidthProperty().addListener((ov, oldV, newV) -> this.windowBox.requestLayout());
        viewModel.minHeightProperty().addListener((ov, oldV, newV) -> this.windowBox.requestLayout());
        viewModel.maxHeightProperty().addListener((ov, oldV, newV) -> this.windowBox.requestLayout());
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
        ValueUtils.callAndAddListener(viewModel.activeProperty(), (ov, oldV, newV) -> updateActive(newV));
    }

    @Override
    protected void removeListeners() {
        super.removeListeners();
        if (this.stylesheetManager != null) {
            this.stylesheetManager.dispose();
        }
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            var originalDispatcher = this.window.getEventDispatcher();
            this.window.setEventDispatcher((event, tail) -> {
                if (!(event instanceof InputEvent e)) {
                    return originalDispatcher.dispatchEvent(event, tail);
                }
                this.inputEvent = e;
                try {
                    return originalDispatcher.dispatchEvent(event, tail);
                } finally {
                    this.inputEvent = null;
                }
            });
            this.window.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
            window.setOnCloseRequest(event -> {
                event.consume();
                getViewModel().onCloseRequest();
            });
        } else {
            windowNode.addEventFilter(MouseEvent.MOUSE_PRESSED, (event) -> {
                // A click gives the window focus only if it has none yet: taking focus from a control inside
                // would, for example, collapse the selection of a text field before its context menu is built.
                if (!windowNode.isFocusWithin()) {
                    windowNode.requestFocus();
                }
            });
        }
    }

    protected PulseListenerManager getPulseListenerManager() {
        return pulseListenerManager;
    }

    protected StackPane getWindowNode() {
        return windowNode;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected VBox getWindowBox() {
        return windowBox;
    }

    /**
     * Shows or hides the shadow around a nested window.
     *
     * @param visible {@code true} to show the shadow
     */
    protected void setShadowVisible(boolean visible) {
        checkIfNested();
        if (this.shadowVisible == visible) {
            return;
        }
        if (visible) {
            getWindowNode().getStyleClass().add(StyleClasses.SHADOW);
        } else {
            getWindowNode().getStyleClass().remove(StyleClasses.SHADOW);
        }
        this.shadowVisible = visible;
    }

    protected boolean isShadowVisible() {
        checkIfNested();
        return shadowVisible;
    }

    /**
     * JavaFX doesn't support the same accelerator to be installed in multiple MenuItems. See this bug:
     * https://bugs.openjdk.org/browse/JDK-8088068 . This method is a workaround for this problem.
     *
     * @param e
     */
    protected void fixAcceleratorKeyPressed(KeyEvent e) {
        Node focusedNode = window.getScene().getFocusOwner();
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

    protected StylesheetRegistry getStylesheetRegistry() {
        return stylesheetRegistry;
    }

    /**
     * Adds stylesheets to this window only. Stylesheets that every window needs belong in the registry.
     *
     * @param sheets the stylesheets to add
     */
    protected void addStylesheets(List<Stylesheet> sheets) {
        checkIfTopLevel();
        this.stylesheetManager.addStylesheets(sheets);
    }

    /**
     * Removes stylesheets from this window.
     *
     * @param sheets the stylesheets to remove
     */
    protected void removeStylesheets(List<Stylesheet> sheets) {
        checkIfTopLevel();
        this.stylesheetManager.removeStylesheets(sheets);
    }

    void updateActive(boolean active) {
        windowBox.pseudoClassStateChanged(PseudoClasses.INACTIVE, !active);
    }

    void setWindowManager(@Nullable WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Makes this window floating, as any manual positioning of a nested window does.
     */
    void releaseAnchors() {
        if (this.windowManager != null) {
            this.windowManager.setAnchors(this, null);
        }
    }

    void updateWidth(double value) {
        if (this.window == null) {
            this.windowNode.setMinWidth(value);
            this.windowNode.setMaxWidth(value);
        } else {
            this.window.setWidth(value);
        }
    }

    void updateHeight(double value) {
        if (this.window == null) {
            this.windowNode.setMinHeight(value);
            this.windowNode.setMaxHeight(value);
        } else {
            this.window.setHeight(value);
        }
    }

    void calculateMinSize() {
        var width = windowBox.minWidth(-1) + windowNode.getPadding().getLeft() + windowNode.getPadding().getRight();
        this.calculatedMinWidth.set(width);
        var height = windowBox.minHeight(-1) + windowNode.getPadding().getTop() + windowNode.getPadding().getBottom();
        this.calculatedMinHeight.set(height);
    }

    DoubleProperty calculatedMinWidthProperty() {
        return calculatedMinWidth;
    }

    DoubleProperty calculatedMinHeightProperty() {
        return calculatedMinHeight;
    }

    void checkIfTopLevel() {
        if (getViewModel().getWindowType() != WindowType.TOP_LEVEL) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.NESTED
                    + " Window");
        }
    }

    void checkIfNested() {
        if (getViewModel().getWindowType() != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }

    private void updateAlwaysOnTop() {
        if (this.windowManager != null) {
            this.windowManager.updateWindow(this);
        }
    }

    private void updateDensity(Density density) {
        checkIfTopLevel();
        var styleClasses = this.window.getScene().getRoot().getStyleClass();
        if (this.density.getStyleClass() != null) {
            styleClasses.remove(this.density.getStyleClass());
        }
        this.density = density;
        if (this.density.getStyleClass() != null) {
            styleClasses.add(this.density.getStyleClass());
        }
    }

    private void updateTheme(Theme theme) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
            this.stylesheetManager.setTheme(theme);
        }
    }

    private void updateRegularFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setRegularFont(font);
    }

    private void updateMonospaceFont(Font font) {
        checkIfTopLevel();
        this.fontApplier.setMonospaceFont(font);
    }

    private void updateX(double x) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            // NaN is the window's own sentinel for "not positioned yet"; forwarding it into setX would mark the
            // window as explicitly positioned and suppress the platform's default centering on first show()
            if (!Double.isNaN(x)) {
                this.window.setX(x);
            }
        } else {
            releaseAnchors();
            this.windowNode.setLayoutX(x);
        }
    }

    private void updateY(double y) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            if (!Double.isNaN(y)) {
                this.window.setY(y);
            }
        } else {
            releaseAnchors();
            this.windowNode.setLayoutY(y);
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
}
