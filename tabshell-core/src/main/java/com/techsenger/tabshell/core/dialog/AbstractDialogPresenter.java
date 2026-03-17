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
import com.techsenger.tabshell.core.popup.AbstractPopupPresenter;
import com.techsenger.tabshell.material.button.ResultButtonName;
import com.techsenger.tabshell.material.icon.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogPresenter<V extends DialogView, C extends DialogComposer>
        extends AbstractPopupPresenter<V, C> implements DialogPresenter<V, C> {

    private static final class ButtonModel {

        private boolean isDefault;

        private boolean disabled;

        public boolean isDefault() {
            return isDefault;
        }

        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }

    private Runnable closeAction = () -> requestClose();

    private Consumer<ResultButtonName> resultAction = (name) -> requestClose();

    private boolean active;

    private double minWidth;

    private double minHeight;

    private double maxWidth;

    private double maxHeight;

    private boolean outOfBoundsAllowed;

    private boolean resizable;

    private boolean buttonWidthEqual;

    private boolean closeDisabled;

    private String title;

    private Icon<?> icon;

    private final List<ResultButtonName> leftButtons = new ArrayList<>();

    private final List<ResultButtonName> rightButtons = new ArrayList<>();

    private final Map<ResultButtonName, ButtonModel> buttonsByName = new HashMap<>();

    public AbstractDialogPresenter(V view) {
        super(view, true);
    }

    @Override
    public void onClose() {
        if (this.closeAction != null) {
            this.closeAction.run();
        }
    }

    @Override
    public void onResult(ResultButtonName name) {
        if (this.resultAction != null) {
            this.resultAction.accept(name);
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        getView().setActive(active);
    }

    @Override
    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
        getView().setMinWidth(maxWidth);
    }

    @Override
    public double getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
        getView().setMinHeight(maxWidth);
    }

    @Override
    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
        getView().setMaxWidth(maxWidth);
    }

    @Override
    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
        getView().setMaxHeight(maxHeight);
    }

    @Override
    public boolean isOutOfBoundsAllowed() {
        return outOfBoundsAllowed;
    }

    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        this.outOfBoundsAllowed = outOfBoundsAllowed;
        getView().setOutOfBoundsAllowed(outOfBoundsAllowed);
    }

    @Override
    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        getView().setResizable(active);
    }

    @Override
    public boolean isButtonWidthEqual() {
        return buttonWidthEqual;
    }

    public void setButtonWidthEqual(boolean buttonWidthEqual) {
        this.buttonWidthEqual = buttonWidthEqual;
        getView().setButtonWidthEqual(active);
    }

    @Override
    public boolean isCloseDisabled() {
        return closeDisabled;
    }

    public void setCloseDisabled(boolean closeDisabled) {
        this.closeDisabled = closeDisabled;
        getView().setCloseDisabled(active);
    }

    @Override
    public @Unmodifiable List<ResultButtonName> getLeftButtons() {
        return Collections.unmodifiableList(this.leftButtons);
    }

    @Override
    public @Unmodifiable List<ResultButtonName> getRightButtons() {
        return Collections.unmodifiableList(this.rightButtons);
    }

    @Override
    public void setLeftButtons(ResultButtonName... names) {
        this.leftButtons.clear();
        List<ResultButtonName> foundNames = new ArrayList<>();
        for (var name : names) {
            var button = this.buttonsByName.get(name);
            if (button != null) {
                this.leftButtons.add(name);
                foundNames.add(name);
            }
        }
        getView().setLeftButtons(foundNames.toArray(ResultButtonName[]::new));
    }

    @Override
    public void setRightButtons(ResultButtonName... names) {
        this.rightButtons.clear();
        List<ResultButtonName> foundNames = new ArrayList<>();
        for (var name : names) {
            var button = this.buttonsByName.get(name);
            if (button != null) {
                this.rightButtons.add(name);
                foundNames.add(name);
            }
        }
        getView().setRightButtons(foundNames.toArray(ResultButtonName[]::new));
    }

    @Override
    public void setButtonDisabled(ResultButtonName name, boolean value) {
        var button = this.buttonsByName.get(name);
        if (button != null) {
            button.setDisabled(value);
            getView().setButtonDisabled(name, value);
        }
    }

    @Override
    public void setButtonDefault(ResultButtonName name, boolean value) {
        var button = this.buttonsByName.get(name);
        if (button != null) {
            button.setDefault(value);
            getView().setButtonDefault(name, value);
        }
    }

    @Override
    public Optional<Boolean> getButtonDisabled(ResultButtonName name) {
        return Optional.ofNullable(this.buttonsByName.get(name)).map(ButtonModel::isDisabled);
    }

    @Override
    public Optional<Boolean> getButtonDefault(ResultButtonName name) {
        return Optional.ofNullable(this.buttonsByName.get(name)).map(ButtonModel::isDefault);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        getView().setTitle(title);
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon = icon;
        getView().setIcon(icon);
    }

    @Override
    public Icon<?> getIcon() {
        return this.icon;
    }

    @Override
    public Runnable getCloseAction() {
        return closeAction;
    }

    @Override
    public void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    public Consumer<ResultButtonName> getResultAction() {
        return resultAction;
    }

    @Override
    public void setResultAction(Consumer<ResultButtonName> resultAction) {
        this.resultAction = resultAction;
    }

    @Override
    protected DialogHistory getHistory() {
        return (DialogHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        setPrefWidth(h.getWidth());
        setPrefHeight(h.getHeight());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.setWidth(getWidth());
        h.setHeight(getHeight());
    }

    void onButtonRegistered(ResultButtonName name, boolean isDefault, boolean disabled) {
        var model = new ButtonModel();
        model.setDefault(isDefault);
        model.setDisabled(disabled);
        this.buttonsByName.put(name, model);
    }

    void onButtonUnregistered(ResultButtonName name) {
        this.buttonsByName.remove(name);
    }
}
