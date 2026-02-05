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

package com.techsenger.tabshell.core.dialog;

import com.techsenger.tabshell.core.popup.AbstractPopupFxView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import com.techsenger.toolkit.fx.pulse.LayoutPulseListener;
import com.techsenger.toolkit.fx.value.ValueUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
public abstract class AbstractDialogFxView<P extends AbstractDialogPresenter<?, ?>>
        extends AbstractPopupFxView<P> implements DialogFxView<P> {

    public class Composer extends AbstractPopupFxView.Composer implements DialogFxView.Composer {

        private final AbstractDialogFxView<?> view = AbstractDialogFxView.this;

        @Override
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((DialogContainerFxView.Composer) parent.getComposer()).removeDialog(view);
            }
        }
    }

    private static final PseudoClass INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("inactive");

    private final IconViewBox iconViewBox = new IconViewBox();

    private final Label titleLabel = new Label();

    private final Pane spacePane = new Pane();

    private final StackPane closeIcon = new StackPane();

    private final Button closeButton = new Button();

    private final HBox buttonBox = new HBox(closeButton);

    private final HBox titleBar = new HBox(iconViewBox, titleLabel, spacePane, buttonBox);

    /**
     * Trap for focus dialog. This trap should always be activated after adding all controls to dialog. Otherwise
     * it is necessary to update it.
     */
    private final FocusTrap focusTrap = new FocusTrap(super.getContentPane());

    private final VBox dialogBox = new VBox(titleBar, super.getNode());

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
     * If it is true user can move dialog only with minimum top constrain. If this value is false user
     * can only move the dialog within the bounds of the parent Pane.
     */
    private final BooleanProperty outOfBoundsAllowed = new SimpleBooleanProperty(true);

    private final BooleanProperty active = new SimpleBooleanProperty(true);

    private final BooleanProperty resizable = new SimpleBooleanProperty();

    private final BooleanProperty buttonWidthEqual = new SimpleBooleanProperty(false);

    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private final LayoutPulseListener buttonWidthListener = () -> {
        makeEqualButtons();
        return false;
    };

    public AbstractDialogFxView(boolean resizable) {
        super();
        setResizable(resizable);
    }

    @Override
    public VBox getNode() {
        return this.dialogBox;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public double getPrefWidth() {
        return dialogBox.getPrefWidth();
    }

    @Override
    public void setPrefWidth(double value) {
        dialogBox.setPrefWidth(value);
    }

    @Override
    public double getPrefHeight() {
        return dialogBox.getPrefHeight();
    }

    @Override
    public void setPrefHeight(double value) {
        dialogBox.setPrefHeight(value);
    }

    @Override
    public double getMinWidth() {
        return minWidth.get();
    }

    @Override
    public void setMinWidth(double value) {
        this.minWidth.set(value);
    }

    @Override
    public double getMinHeight() {
        return this.minHeight.get();
    }

    @Override
    public void setMinHeight(double value) {
        this.minHeight.set(value);
    }

    @Override
    public double getMaxWidth() {
        return maxWidth.get();
    }

    @Override
    public void setMaxWidth(double value) {
       this.maxWidth.set(value);
    }

    @Override
    public double getMaxHeight() {
        return maxHeight.get();
    }

    @Override
    public void setMaxHeight(double value) {
        this.maxHeight.set(value);
    }

    @Override
    public String getTitle() {
        return titleLabel.textProperty().get();
    }

    @Override
    public void setTitle(String title) {
        titleLabel.textProperty().set(title);
    }

    @Override
    public boolean isOutOfBoundsAllowed() {
        return outOfBoundsAllowed.get();
    }

    @Override
    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        this.outOfBoundsAllowed.set(outOfBoundsAllowed);
    }

    @Override
    public boolean isResizable() {
        return this.resizable.get();
    }

    @Override
    public void setResizable(boolean value) {
        this.resizable.set(value);
    }

    @Override
    public Icon<?> getIcon() {
        return iconViewBox.getIcon();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    @Override
    public boolean isButtonWidthEqual() {
        return buttonWidthEqual.get();
    }

    @Override
    public void setButtonWidthEqual(boolean value) {
        this.buttonWidthEqual.set(value);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractDialogFxView.Composer();
    }

    protected BooleanProperty resizableProperty() {
        return resizable;
    }

    protected BooleanProperty buttonWidthEqualProperty() {
        return buttonWidthEqual;
    }

    protected BooleanProperty outOfBoundsAllowedProperty() {
        return outOfBoundsAllowed;
    }

    protected FocusTrap getFocusTrap() {
        return focusTrap;
    }

    @Override
    protected void build() {
        titleLabel.getStyleClass().add("title-label");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().addAll("title-bar", StyleClasses.CORNERS_TOP);
        HBox.setHgrow(spacePane, Priority.ALWAYS);

        this.buttonBox.getStyleClass().add("button-box");
        this.buttonBox.setAlignment(Pos.CENTER);
        closeIcon.getStyleClass().add("icon");
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("close-button");

        VBox.setVgrow(getContentPane(), Priority.ALWAYS);
        getContentPane().getStyleClass().addAll("content-pane", StyleClasses.CORNERS_BOTTOM);
        super.getNode().getStyleClass().addAll("wrapper", StyleClasses.CORNERS_BOTTOM);
        VBox.setVgrow(super.getNode(), Priority.ALWAYS);
        this.dialogBox.getStyleClass().addAll("dialog-box", StyleClasses.CORNERS_ALL, StyleClasses.SHADOW);
        this.resizer = new RegionResizer(minWidth, minHeight, maxWidth, maxHeight,
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
    protected void bind() {
        super.bind();
        this.resizer.disabledProperty().bind(Bindings.not(this.resizable));
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        ValueUtils.callAndAddListener(this.buttonWidthEqual, (ov, oldV, newV) -> {
            if (Boolean.FALSE.equals(oldV)) {
                getPulseListenerManager().removeListener(LayoutPhase.POST, buttonWidthListener);
            }
            if (newV) {
                getPulseListenerManager().addListener(LayoutPhase.POST, buttonWidthListener);
            }
        });
        ValueUtils.callAndAddListener(this.active, (ov, oldV, newV) -> {
            dialogBox.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !newV);
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        titleBar.setOnMousePressed((event) -> this.doOnMousePressed(event));
        titleBar.setOnMouseDragged((event) -> this.doOnMouseDragged(event));
        closeButton.setOnAction(e -> {
            var r = getPresenter().getCloseAction();
            if (r != null) {
                r.run();
            }
        });
    }

    protected abstract void makeEqualButtons();

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
            if (!isOutOfBoundsAllowed()) {
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
