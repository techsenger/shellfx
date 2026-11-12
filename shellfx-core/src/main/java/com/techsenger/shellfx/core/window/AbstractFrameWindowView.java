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
import com.techsenger.shellfx.core.dialog.DialogResizeEvent;
import com.techsenger.shellfx.core.registries.StylesheetRegistry;
import com.techsenger.shellfx.core.style.CoreIcons;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.IconViewBox;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The base view of a window with a title bar: it adds the title, the icon, the buttons for closing, minimizing and
 * maximizing, the border and the shadow, and lets the user move and resize a nested window.
 *
 * @param <VM> the view model of the window
 * @author Pavel Castornii
 */
public abstract class AbstractFrameWindowView<VM extends AbstractFrameWindowViewModel<?>>
        extends AbstractWindowView<VM> implements FrameWindowView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer {

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

    private final StackPane titlePane = new StackPane();

    private Region titleBar;

    private @Nullable Pane blockPane;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetX;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetY;

    private @Nullable RegionResizer resizer;

    private boolean resizingInProgress;

    private final EventHandler<? super MouseEvent> maximizeHandler = (e) -> {
        if (e.getClickCount() == 2) {
            getViewModel().onMaximize();
        }
    };

    public AbstractFrameWindowView(VM viewModel, StylesheetRegistry stylesheetRegistry) {
        this(viewModel, stylesheetRegistry,
                viewModel.getWindowType() == WindowType.TOP_LEVEL ? new Stage() : null);
    }

    /**
     * Creates a new window backed by the given stage, which gets the scene with the content of this window.
     *
     * @param viewModel the view model of this window
     * @param stylesheetRegistry the registry of the stylesheets that every window applies
     * @param stage the stage that backs a {@link WindowType#TOP_LEVEL} window; {@code null} for a
     *     {@link WindowType#NESTED} one
     */
    public AbstractFrameWindowView(VM viewModel, StylesheetRegistry stylesheetRegistry, @Nullable Stage stage) {
        super(viewModel, stylesheetRegistry, stage);
        if (stage != null) {
            stage.setScene(new Scene(getNode()));
        }
    }

    @Override
    public Stage getWindow() {
        return (Stage) super.getWindow();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractFrameWindowView<VM>.Composer();
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
        this.leftBox.getStyleClass().add("left-box");
        this.rightBox.getStyleClass().add("right-box");
        this.rightBox.setSpacing(Spacing.getHorizontal() + Spacing.getHorizontalThird());
        this.titlePane.getStyleClass().add("title-pane");
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            this.titleBar = new HeaderBar(leftBox, null, rightBox);
            var stage = getWindow();
            HeaderBar.setSystemButtonHeight(stage, 0); // to hide default buttons
            stage.initStyle(StageStyle.EXTENDED);
            if (viewModel.isModal()) {
                // it is not possible to change modality for the primary stage after it has been shown
                stage.initModality(Modality.WINDOW_MODAL);
            }
        } else {
            this.titleBar = new HBox(leftBox, titleLabel, new Spacer(Orientation.HORIZONTAL), rightBox);
            titleBar.getStyleClass().add(StyleClasses.CORNERS_TOP);
            titleBar.setMinHeight(Region.USE_PREF_SIZE);
            VBox.setVgrow(this.titleBar, Priority.NEVER);
            getContentBox().getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
            var windowNode = getWindowNode();
            DoubleProperty resultMinWidth = new SimpleDoubleProperty();
            resultMinWidth.bind(Bindings.when(viewModel.minWidthProperty().isEqualTo(0))
                    .then(calculatedMinWidthProperty())
                    .otherwise(viewModel.minWidthProperty()));
            DoubleProperty resultMinHeight = new SimpleDoubleProperty();
            resultMinHeight.bind(Bindings.when(viewModel.minHeightProperty().isEqualTo(0))
                    .then(calculatedMinHeightProperty())
                    .otherwise(viewModel.minHeightProperty()));
            this.resizer = new RegionResizer(resultMinWidth, resultMinHeight, viewModel.maxWidthProperty(),
                    viewModel.maxHeightProperty(),
                    (e) -> {
                        resizingInProgress = true;
                        releaseAnchors();
                        getViewModel().resizingProperty().set(true);
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_STARTED, e);
                        windowNode.fireEvent(event);
                        calculateMinSize();
                        // it is necessary to set sizes for both sides, as we don't know which side the user will resize
                        updateWidth(windowNode.getWidth());
                        updateHeight(windowNode.getHeight());
                    },
                    (e) -> {
                        var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e);
                        windowNode.fireEvent(event);
                        resizingInProgress = false;
                        getViewModel().resizingProperty().set(false);
                    });
            this.resizer.initialize(windowNode);
        }
        this.titleBar.getStyleClass().add("title-bar");
        this.titlePane.getChildren().add(this.titleBar);
        getWindowBox().getChildren().add(0, titlePane);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        titleLabel.textProperty().bind(viewModel.titleProperty());
        iconViewBox.iconProperty().bind(viewModel.iconProperty());
        closeButton.disableProperty().bind(viewModel.closableProperty().not());
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            var stage = getWindow();
            // it is necessary to update the title of the stage because it is shown in the title bar of the OS
            stage.titleProperty().bind(this.titleLabel.textProperty());
            viewModel.maximizedWrapper().bind(stage.maximizedProperty());
            viewModel.minimizedWrapper().bind(stage.iconifiedProperty());
            stage.minWidthProperty().bind(viewModel.minWidthProperty());
            stage.minHeightProperty().bind(viewModel.minHeightProperty());
            stage.maxWidthProperty().bind(viewModel.maxWidthProperty());
            stage.maxHeightProperty().bind(viewModel.maxHeightProperty());
        } else {
            this.resizer.disabledProperty().bind(viewModel.resizableProperty().not()
                    .or(this.maximized).or(this.minimized));
        }
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        var viewModel = getViewModel();
        if (viewModel.getWindowType() == WindowType.TOP_LEVEL) {
            var stage = getWindow();
            stage.maximizedProperty().addListener((ov, oldV, newV) -> this.maximized.set(newV));
            ValueUtils.callAndAddListener(viewModel.visibleProperty(), (ov, oldV, newV) -> {
                if (newV) {
                    stage.show();
                } else {
                    stage.hide();
                }
            });
            ValueUtils.callAndAddListener(viewModel.alwaysOnTopProperty(),
                    (ov, oldV, newV) -> stage.setAlwaysOnTop(newV));
            ValueUtils.callAndAddListener(viewModel.resizableProperty(),
                    (ov, oldV, newV) -> stage.setResizable(newV));
        } else {
            this.minimized.addListener((ov, oldV, newV) -> {
                if (newV) {
                    this.minimizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
                } else {
                    this.minimizeIconView.setIcon(CoreIcons.WINDOW_MINIMIZE);
                }
            });
        }
        this.maximized.addListener((ov, oldV, newV) -> {
            getWindowBox().pseudoClassStateChanged(PseudoClasses.MAXIMIZED, newV);
            if (newV) {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
            } else {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_MAXIMIZE);
            }
        });
        updateMaximized(viewModel.isMaximized());
        viewModel.maximizedSource().addListener((value) -> updateMaximized(value));
        ValueUtils.callAndAddListener(viewModel.maximizableProperty(), (ov, oldV, newV) -> updateMaximizable(newV));
        updateMinimized(viewModel.isMinimized());
        viewModel.minimizedSource().addListener((value) -> updateMinimized(value));
        ValueUtils.callAndAddListener(viewModel.minimizableProperty(), (ov, oldV, newV) -> updateMinimizable(newV));
        ValueUtils.callAndAddListener(viewModel.blockedProperty(), (ov, oldV, newV) -> updateBlocked(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        var viewModel = getViewModel();
        this.closeButton.setOnAction(e -> viewModel.onCloseRequest());
        this.maximizeButton.setOnAction(e -> viewModel.onMaximize());
        this.minimizeButton.setOnAction(e -> viewModel.onMinimize());
        if (viewModel.getWindowType() == WindowType.NESTED) {
            titleBar.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> this.onMousePressed(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_DRAGGED, (event) -> this.onMouseDragged(event));
            titleBar.addEventHandler(MouseEvent.MOUSE_RELEASED, (event) -> this.onMouseReleased(event));
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

    protected Pane getBlockPane() {
        return blockPane;
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

    private void updateMaximized(boolean maximized) {
        if (getViewModel().getWindowType() == WindowType.TOP_LEVEL) {
            getWindow().setMaximized(maximized);
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
            getWindow().setIconified(minimized);
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

    private void updateBlocked(boolean blocked) {
        if (blocked) {
            this.blockPane = new Pane();
            this.blockPane.setMouseTransparent(false);
            getWindowNode().getChildren().add(this.blockPane);
        } else {
            getWindowNode().getChildren().remove(this.blockPane);
            this.blockPane = null;
        }
    }

    private void onMousePressed(MouseEvent event) {
        if (!resizingInProgress) {
            offsetX = event.getSceneX() - getWindowNode().getLayoutX();
            offsetY = event.getSceneY() - getWindowNode().getLayoutY();
            event.consume();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (!resizingInProgress) {
            this.titleBar.setCursor(Cursor.CLOSED_HAND);
            releaseAnchors();
            var windowNode = getWindowNode();
            var newX = event.getSceneX() - offsetX;
            var newY = event.getSceneY() - offsetY;
            var parent = (Pane) windowNode.getParent();
            //checking position constraints
            if (newY < 0) {
                newY = 0;
            }
            if (!getViewModel().isOutOfBoundsAllowed()) {
                if (newX < 0) {
                    newX = 0;
                }
                if ((newX + windowNode.getWidth()) > parent.getWidth()) {
                    newX = parent.getWidth() - windowNode.getWidth();
                }
                if ((newY + windowNode.getHeight()) > parent.getHeight()) {
                    newY = parent.getHeight() - windowNode.getHeight();
                }
            }
            windowNode.setLayoutX(newX);
            windowNode.setLayoutY(newY);
            event.consume();
        }
    }

    private void onMouseReleased(MouseEvent event) {
        if (!resizingInProgress) {
            this.titleBar.setCursor(Cursor.DEFAULT);
        }
    }
}
