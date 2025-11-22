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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.mvvm4fx.core.ComponentComposer;
import com.techsenger.mvvm4fx.core.ComponentViewModel;
import com.techsenger.tabshell.core.area.AbstractAreaView;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import com.techsenger.toolkit.fx.pulse.LayoutPulseListener;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogView<T extends AbstractDialogViewModel> extends AbstractAreaView<T>
        implements DialogView<T> {

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    private final IconViewBox iconViewBox = new IconViewBox();

    private final Label titleLabel = new Label();

    private final Pane spacePane = new Pane();

    private final StackPane closeIcon = new StackPane();

    private final Button closeButton = new Button();

    private final HBox buttonBox = new HBox(closeButton);

    private final HBox titleBar = new HBox(iconViewBox, titleLabel, spacePane, buttonBox);

    private final VBox contentPane = new VBox();

    /**
     * Trap for focus dialog. This trap should always be activated after adding all controls to dialog. Otherwise
     * it is necessary to update it.
     */
    private final FocusTrap focusTrap = new FocusTrap(contentPane);

    /**
     * This is internal pane that is required for waiting mode.
     */
    private final StackPane stackPane = new StackPane(contentPane);

    private final VBox dialogBox = new VBox(titleBar, stackPane);

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetX;

    /**
     * While dragging we need the difference. So, we keep in this variable previous value.
     */
    private double offsetY;

    private Pane parent;

    private RegionResizer resizer;

    /**
     * Dialog manager that opens and closes this dialog.
     */
    private DialogManager dialogManager;

    private Pane backgroundPane;

    private final LayoutPulseListener buttonWidthListener = () -> {
        makeEqualButtons();
        return false;
    };

    public AbstractDialogView(T viewModel) {
        super(viewModel);
    }

    @Override
    public VBox getNode() {
        return this.dialogBox;
    }

    @Override
    public void close() {
        if (this.dialogManager != null) {
            this.dialogManager.closeDialog(this);
        }
    }

    @Override
    public DialogComposer getComposer() {
        return (DialogComposer) super.getComposer();
    }

    protected VBox getContentPane() {
        return contentPane;
    }

    protected FocusTrap getFocusTrap() {
        return focusTrap;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        viewModel.closeRequestedSource().addListener((v) -> {
            if (Boolean.TRUE.equals(v)) {
                close();
            }
        });
        titleLabel.getStyleClass().add("title-label");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().addAll("title-bar", StyleClasses.CORNERS_TOP);
        HBox.setHgrow(spacePane, Priority.ALWAYS);

        this.buttonBox.getStyleClass().add("button-box");
        this.buttonBox.setAlignment(Pos.CENTER);
        closeIcon.getStyleClass().add("icon");
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("close-button");
        closeButton.setOnAction(e -> {
            var r = viewModel.closeActionProperty().get();
            if (r != null) {
                r.run();
            }
        });

        this.contentPane.getStyleClass().addAll("content-pane", StyleClasses.CORNERS_BOTTOM);
        VBox.setVgrow(contentPane, Priority.ALWAYS);
        VBox.setVgrow(stackPane, Priority.ALWAYS);
        this.stackPane.getStyleClass().addAll("wrapper", StyleClasses.CORNERS_BOTTOM);
        this.dialogBox.getStyleClass().addAll("dialog-box", StyleClasses.CORNERS_ALL, StyleClasses.SHADOW);
        this.resizer = new RegionResizer(
                viewModel.minWidthProperty(), viewModel.minHeightProperty(),
                viewModel.maxWidthProperty(), viewModel.maxHeightProperty(),
                (e) -> {
                    var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_STARTED, e);
                    this.dialogBox.fireEvent(event);
                },
                (e) -> {
                    var event = new DialogResizeEvent(DialogResizeEvent.DIALOG_RESIZE_FINISHED, e);
                    this.dialogBox.fireEvent(event);
                });
        this.resizer.initialize(dialogBox);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        iconViewBox.iconProperty().bind(viewModel.iconProperty());
        titleLabel.textProperty().bind(viewModel.titleProperty());
        viewModel.widthWrapper().bind(dialogBox.widthProperty());
        viewModel.heightWrapper().bind(dialogBox.heightProperty());
        dialogBox.prefWidthProperty().bindBidirectional(viewModel.prefWidthProperty());
        dialogBox.prefHeightProperty().bindBidirectional(viewModel.prefHeightProperty());
        this.resizer.disabledProperty().bind(Bindings.not(viewModel.resizableProperty()));
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        viewModel.waitingProperty().addListener((ov, oldV, newV) -> {
            if (newV) {
                var bgPane = new Pane();
                bgPane.setMouseTransparent(false);
                stackPane.getChildren().add(bgPane);
                bgPane.setCursor(Cursor.WAIT);
            } else {
                stackPane.getChildren().remove(stackPane.getChildren().size() - 1);
            }
        });
        ValueUtils.callAndAddListener(viewModel.buttonWidthEqualProperty(), (ov, oldV, newV) -> {
            if (Boolean.FALSE.equals(oldV)) {
                getPulseListenerManager().removeListener(LayoutPhase.POST, buttonWidthListener);
            }
            if (newV) {
                getPulseListenerManager().addListener(LayoutPhase.POST, buttonWidthListener);
            }
        });
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        titleBar.setOnMousePressed((event) -> this.doOnMousePressed(event));
        titleBar.setOnMouseDragged((event) -> this.doOnMouseDragged(event));
    }

    @Override
    protected ComponentComposer<?> createComposer() {
        // we suppose that all child dialogs will be with the same scope
        return new AbstractDialogComposer<AbstractDialogView<?>>(this) {

            @Override
            protected ComponentViewModel.Composer createViewModelComposer() {
                return new AbstractDialogComposer.ViewModelComposer() {

                };
            }

            @Override
            public void openDialog(DialogView<?> dialog) {
                getView().getDialogManager().openDialog(dialog);
            }
        };
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    protected abstract void makeEqualButtons();

    protected void setDialogManager(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
    }

    void setUnfocused(boolean unfocused) {
        dialogBox.pseudoClassStateChanged(UNFOCUSED_PSEUDO_CLASS, unfocused);
    }

    Pane getBackgroundPane() {
        return backgroundPane;
    }

    void setBackgroundPane(Pane backgroundPane) {
        this.backgroundPane = backgroundPane;
    }

    private void doOnMousePressed(MouseEvent event) {
        if (isMoving()) {
            offsetX = event.getSceneX() - this.dialogBox.getLayoutX();
            offsetY = event.getSceneY() - this.dialogBox.getLayoutY();
            event.consume();
        }
    }

    private void doOnMouseDragged(MouseEvent event) {
        if (isMoving()) {
            var newX = event.getSceneX() - offsetX;
            var newY = event.getSceneY() - offsetY;
            if (this.parent == null) {
                this.parent = (Pane) this.dialogBox.getParent();
            }
            //checking position constraints
            if (newY < 0) {
                newY = 0;
            }
            if (!this.getViewModel().isOutOfBoundsAllowed()) {
                if (newX < 0) {
                    newX = 0;
                }
                if ((newX + this.dialogBox.getWidth()) > this.parent.getWidth()) {
                    newX = this.parent.getWidth() - this.dialogBox.getWidth();
                }
                if ((newY + this.dialogBox.getHeight()) > this.parent.getHeight()) {
                    newY = this.parent.getHeight() - this.dialogBox.getHeight();
                }
            }
            this.dialogBox.setLayoutX(newX);
            this.dialogBox.setLayoutY(newY);
            event.consume();
        }
    }

    /**
     * There is also a resizing handlers. so, we check cursor type to know if resizing is enabled.
     *
     * @return
     */
    private boolean isMoving() {
        var currentCursor = this.dialogBox.getCursor();
        return (currentCursor == null || currentCursor == Cursor.DEFAULT);
    }
}
