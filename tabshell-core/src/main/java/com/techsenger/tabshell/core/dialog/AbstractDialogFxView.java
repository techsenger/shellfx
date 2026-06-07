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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.window.AbstractWindowFxView;
import com.techsenger.tabshell.core.window.WindowType;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.FocusTrap;
import com.techsenger.toolkit.fx.Spacer;
import com.techsenger.toolkit.fx.utils.ButtonUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogFxView<P extends AbstractDialogPresenter<?>>
        extends AbstractWindowFxView<P> implements DialogFxView<P> {

    private final HBox leftBottomBox = new HBox();

    private final HBox rightBottomBox = new HBox();

    private final HBox buttomBox = new HBox(leftBottomBox, new Spacer(Orientation.HORIZONTAL), rightBottomBox);

    /**
     * Trap for focus dialog. This trap should always be activated after adding all controls to dialog. Otherwise
     * it is necessary to update it.
     */
    private final FocusTrap focusTrap = new FocusTrap(getContentBox());

    private final Map<ResultButtonName, Button> buttonsByName = new HashMap<>();

    @Override
    public void setLeftButtons(ResultButtonName... names) {
        removeButtons(leftBottomBox);
        addButtons(leftBottomBox, names);
    }

    @Override
    public void setRightButtons(ResultButtonName... names) {
        removeButtons(rightBottomBox);
        addButtons(rightBottomBox, names);
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

    protected FocusTrap getFocusTrap() {
        return focusTrap;
    }

    @Override
    protected void build() {
        super.build();
        if (getPresenter().getWindowType() == WindowType.NESTED) {
            setShadowVisible(true);
        }
        getContentBox().setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                0, Spacing.getHorizontal()));
        getWindowBox().getChildren().add(buttomBox);
        this.buttomBox.getStyleClass().addAll(StyleClasses.CORNERS_BOTTOM, "bottom-box");
        this.buttomBox.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        buttomBox.setSpacing(Spacing.getHorizontal());

        leftBottomBox.setSpacing(Spacing.getHorizontal());
        leftBottomBox.getStyleClass().add("left-bottom-box");
        rightBottomBox.setSpacing(Spacing.getHorizontal());
        rightBottomBox.getStyleClass().add("right-bottom-box");
    }

    /**
     * Makes the specified buttons equal in width.
     */
    protected void makeEqualWidth(Button... buttons) {
        makeEqualWidth(Arrays.asList(buttons));
    }

    /**
     * Makes the specified buttons equal in width.
     */
    protected void makeEqualWidth(List<Button> buttons) {
        buttomBox.applyCss();
        buttomBox.layout();
        buttons.stream().forEach(b -> System.out.println("button: " + b.getText() + " / " + b.getWidth()));
        ButtonUtils.makeEqualWidthBySize(buttons, true);
        buttons.stream().forEach(b -> System.out.println("button: " + b.getText() + " / " + b.getWidth()));
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

    /**
     * Returns an unmodifiable list of buttons located in the left button container.
     *
     * @param resultButtonsOnly if {@code true}, only buttons of type {@link ResultButton}
     *                          are returned; otherwise all {@link Button} instances are included
     * @return unmodifiable list of buttons from the left {@code HBox}
     */
    protected @Unmodifiable List<Button> getLeftButtons(boolean resultButtonsOnly) {
        return getButtons(leftBottomBox, resultButtonsOnly);
    }

    /**
     * Returns an unmodifiable list of buttons located in the right button container.
     *
     * @param resultButtonsOnly if {@code true}, only buttons of type {@link ResultButton}
     *                          are returned; otherwise all {@link Button} instances are included
     * @return unmodifiable list of buttons from the right {@code HBox}
     */
    protected @Unmodifiable List<Button> getRightButtons(boolean resultButtonsOnly) {
        return getButtons(rightBottomBox, resultButtonsOnly);
    }

    /**
     * Returns an unmodifiable combined list of buttons from both left and right button containers.
     *
     * <p>If {@code resultButtonsOnly} is {@code true}, only {@link ResultButton} instances are included.</p>
     *
     * @param resultButtonsOnly filter flag for selecting only result buttons
     * @return unmodifiable list of all matching buttons from both sides
     */
    protected @Unmodifiable List<Button> getButtons(boolean resultButtonsOnly) {
        return Stream.concat(
                getLeftButtons(resultButtonsOnly).stream(),
                getRightButtons(resultButtonsOnly).stream())
                .toList();
    }

    /**
     * Returns a bottom box that can contain both result and additional buttons. In other words, it is safe to
     * add custom buttons to this box.
     */
    protected HBox getLeftBottomBox() {
        return leftBottomBox;
    }

    /**
     * Returns a bottom box that can contain both result and additional buttons. In other words, it is safe to
     * add custom buttons to this box.
     */
    protected HBox getRightBottomBox() {
        return rightBottomBox;
    }

    protected HBox getBottomBox() {
        return buttomBox;
    }

    private void addButtons(HBox box, ResultButtonName... names) {
        for (var name : names) {
            var button = this.buttonsByName.get(name);
            if (button != null) {
                box.getChildren().add(button);
            }
        }
        updateTrap();
    }

    private void removeButtons(HBox box) {
        box.getChildren().removeIf(node ->
            node instanceof ResultButton button && buttonsByName.keySet().contains(button.getName())
        );
    }

    /**
     * Extracts buttons from the given {@link HBox} container with optional filtering by type.
     *
     * <p>If {@code resultButtonsOnly} is {@code true}, only instances of {@link ResultButton}
     * are included; otherwise all {@link Button} instances are returned.</p>
     *
     * @param buttonBox the container holding button nodes
     * @param resultButtonsOnly whether to include only {@link ResultButton} instances
     * @return unmodifiable list of buttons contained in the specified box
     */
    private @Unmodifiable List<Button> getButtons(HBox buttonBox, boolean resultButtonsOnly) {
        Class<? extends Button> filterClass = Button.class;
        if (resultButtonsOnly) {
            filterClass = ResultButton.class;
        }

        return buttonBox.getChildren().stream()
                .filter(filterClass::isInstance)
                .map(Button.class::cast)
                .toList();
    }

    private void updateTrap() {
        if (this.focusTrap.isActivated()) {
            this.focusTrap.update();
        }
    }
}
