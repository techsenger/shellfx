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
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.RegionResizer;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import com.techsenger.toolkit.fx.pulse.LayoutPulseListener;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
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

    private static final PseudoClass INACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("inactive");

    public class Composer extends AbstractPopupFxView<P>.Composer implements DialogFxView.Composer {

        private final AbstractDialogFxView<P> view = AbstractDialogFxView.this;

        @Override
        public void remove() {
            var parent = view.getParent();
            if (parent != null) {
                ((DialogContainerFxView.Composer) parent.getComposer()).removeDialog(view);
            }
        }
    }

    private final IconViewBox iconViewBox = new IconViewBox();

    private final Label titleLabel = new Label();

    private final Pane spacePane = new Pane();

    private final StackPane closeIcon = new StackPane();

    private final Button closeButton = new Button();

    private final HBox titleButtonBox = new HBox(closeButton);

    private final HBox titleBar = new HBox(iconViewBox, titleLabel, spacePane, titleButtonBox);

    private final VBox dialogBox = new VBox(titleBar, super.getNode());

    private final HBox leftButtonBox = new HBox();

    private final HBox rightButtonBox = new HBox();

    private final HBox buttonBox = new HBox(leftButtonBox, new Spacer(Orientation.HORIZONTAL), rightButtonBox);

    private final VBox mainBox = new VBox(super.getContentBox(), buttonBox);

    /**
     * Trap for focus dialog. This trap should always be activated after adding all controls to dialog. Otherwise
     * it is necessary to update it.
     */
    private final FocusTrap focusTrap = new FocusTrap(mainBox);

    /**
     * If it is true user can move dialog only with minimum top constrain. If this value is false user
     * can only move the dialog within the bounds of the parent Pane.
     */
    private final BooleanProperty outOfBoundsAllowed = new SimpleBooleanProperty(false);

    private final BooleanProperty active = new SimpleBooleanProperty(true);

    private final BooleanProperty resizable = new SimpleBooleanProperty();

    private final BooleanProperty buttonWidthEqual = new SimpleBooleanProperty(false);

    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private final Map<ResultButtonName, Button> buttonsByName = new HashMap<>();

    private boolean buttonWidthListenerAdded = false;

    private final LayoutPulseListener buttonWidthListener = () -> {
        makeResultButtonsEqual();
        buttonWidthListenerAdded = false;
        return false;
    };

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

    @Override
    public VBox getNode() {
        return this.dialogBox;
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public void setPrefWidth(double value) {
        dialogBox.setPrefWidth(value);
    }

    @Override
    public void setPrefHeight(double value) {
        dialogBox.setPrefHeight(value);
    }

    @Override
    public void setMinWidth(double value) {
        this.minWidth.set(value);
    }

    @Override
    public void setMinHeight(double value) {
        this.minHeight.set(value);
    }

    @Override
    public void setMaxWidth(double value) {
       this.maxWidth.set(value);
    }

    @Override
    public void setMaxHeight(double value) {
        this.maxHeight.set(value);
    }

    @Override
    public void setTitle(String title) {
        titleLabel.textProperty().set(title);
    }

    @Override
    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        this.outOfBoundsAllowed.set(outOfBoundsAllowed);
    }

    @Override
    public void setResizable(boolean value) {
        this.resizable.set(value);
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
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
    public void setCloseDisabled(boolean value) {
        this.closeButton.setDisable(value);
    }

    @Override
    public void setLeftButtons(ResultButtonName... names) {
        removeButtons(leftButtonBox);
        addButtons(leftButtonBox, names);
    }

    @Override
    public void setRightButtons(ResultButtonName... names) {
        removeButtons(rightButtonBox);
        addButtons(rightButtonBox, names);
    }

    @Override
    public void setButtonDisabled(ResultButtonName name, boolean value) {
        var button = this.buttonsByName.get(name);
        if (button != null) {
            button.setDisable(value);
        }
    }

    @Override
    public void setButtonDefault(ResultButtonName name, boolean value) {
        var button = this.buttonsByName.get(name);
        if (button != null) {
            button.setDefaultButton(value);
        }
    }

    public boolean isOutOfBoundsAllowed() {
        return outOfBoundsAllowed.get();
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isResizable() {
        return resizable.get();
    }

    public boolean isButtonWidthEqual() {
        return buttonWidthEqual.get();
    }

    protected BooleanProperty activeProperty() {
        return resizable;
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

    protected DialogContainerFxView getContainer() {
        return (DialogContainerFxView) getParent();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractDialogFxView.Composer();
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

        this.titleButtonBox.getStyleClass().add("button-box");
        this.titleButtonBox.setAlignment(Pos.CENTER);
        closeIcon.getStyleClass().add("icon");
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("close-button");

        super.getNode().getChildren().clear();
        super.getNode().getChildren().add(mainBox);
        mainBox.getStyleClass().add("main-box");
        VBox.setVgrow(getContentBox(), Priority.ALWAYS);
        getContentBox().setPadding(new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET));
        getContentBox().getStyleClass().addAll("content-box", StyleClasses.CORNERS_BOTTOM);
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

        this.buttonBox.getStyleClass().add(StyleClasses.CORNERS_BOTTOM);
        this.buttonBox.setPadding(new Insets(SizeConstants.INSET));
        buttonBox.setSpacing(SizeConstants.INSET);

        leftButtonBox.setSpacing(SizeConstants.INSET);
        rightButtonBox.setSpacing(SizeConstants.INSET);
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
            if (newV) {
                updateButtonsEqual();
            }
        });
        ValueUtils.callAndAddListener(this.active, (ov, oldV, newV) -> {
            dialogBox.pseudoClassStateChanged(INACTIVE_PSEUDO_CLASS, !newV);
        });
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        titleBar.setOnMousePressed((event) -> this.onMousePressed(event));
        titleBar.setOnMouseDragged((event) -> this.onMouseDragged(event));
        closeButton.setOnAction(e -> {
            var r = getPresenter().getCloseAction();
            if (r != null) {
                r.run();
            }
        });
    }

    /**
     * Makes all buttons in left and right boxes equal.
     */
    protected void makeResultButtonsEqual() {
        var buttons = getResultButtons();
        ButtonUtils.makeEqualWidthBySize(buttons, true);
    }

    /**
     * Returns added buttons from the left and right boxes.
     *
     * @return list of all added buttons or empty collection
     */
    protected List<ResultButton> getResultButtons() {
        Stream<Node> allChildren = Stream.concat(
            leftButtonBox.getChildren().stream(),
            rightButtonBox.getChildren().stream()
        );
        return allChildren
                .filter(ResultButton.class::isInstance)
                .map(ResultButton.class::cast)
                .toList();
    }

    /**
     * Registers a result button. After registration button can be modified only from presenter.
     *
     * @param buttons
     */
    protected void registerButtons(ResultButton... buttons) {
        for (var button: buttons) {
            this.buttonsByName.put(button.getName(), button);
            button.setOnAction((e) -> {
                getPresenter().onResult(button.getName());
            });
            getPresenter().onButtonRegistered(button.getName(), button.isDefaultButton(), button.isDisable());
        }
    }

    /**
     * Unregisters a result button.
     *
     * @param buttons
     */
    protected void unregisterButtons(ResultButton... buttons) {
        for (var button : buttons) {
            this.buttonsByName.remove(button.getName());
            button.setOnAction(null);
            getPresenter().onButtonUnregistered(button.getName());
        }
    }

    protected HBox getLeftButtonBox() {
        return leftButtonBox;
    }

    protected HBox getRightButtonBox() {
        return rightButtonBox;
    }

    protected HBox getButtonBox() {
        return buttonBox;
    }

    protected VBox getMainBox() {
        return mainBox;
    }

    private void onMousePressed(MouseEvent event) {
        if (isMoving()) {
            offsetX = event.getSceneX() - this.dialogBox.getLayoutX();
            offsetY = event.getSceneY() - this.dialogBox.getLayoutY();
            event.consume();
        }
    }

    private void onMouseDragged(MouseEvent event) {
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

    private void addButtons(HBox box, ResultButtonName... names) {
        for (var name : names) {
            var button = this.buttonsByName.get(name);
            if (button != null) {
                box.getChildren().add(button);
            }
        }
        updateButtonsEqual();
        updateTrap();
    }

    private void removeButtons(HBox box) {
        box.getChildren().removeIf(node ->
            node instanceof ResultButton button && buttonsByName.keySet().contains(button.getName())
        );
    }

    private List<ResultButtonName> getButtons(HBox box) {
        return box.getChildren().stream()
                .filter(ResultButton.class::isInstance)
                .map(ResultButton.class::cast)
                .map(b -> b.getName())
                .toList();
    }

    private void updateTrap() {
        if (this.focusTrap.isActivated()) {
            this.focusTrap.update();
        }
    }

    private void updateButtonsEqual() {
        if (!this.buttonWidthListenerAdded) {
            getPulseListenerManager().addListener(LayoutPhase.POST, buttonWidthListener);
            buttonWidthListenerAdded = true;
        }
    }
}
