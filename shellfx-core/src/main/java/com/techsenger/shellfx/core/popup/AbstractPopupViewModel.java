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

package com.techsenger.shellfx.core.popup;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.window.AbstractWindowViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * The base view model of a popup: a bare window that can also wait.
 *
 * @param <C> the composer of the popup
 * @author Pavel Castornii
 */
public abstract class AbstractPopupViewModel<C extends PopupComposer> extends AbstractWindowViewModel<C>
        implements PopupViewModel<C> {

    private final BooleanProperty waiting = new SimpleBooleanProperty();

    private final BooleanProperty autoHide = new SimpleBooleanProperty();

    public AbstractPopupViewModel(PopupParams params) {
        super(params);
    }

    @Override
    public boolean isWaiting() {
        return waiting.get();
    }

    @Override
    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    @Override
    public BooleanProperty waitingProperty() {
        return waiting;
    }

    @Override
    public boolean isAutoHide() {
        return autoHide.get();
    }

    @Override
    public void setAutoHide(boolean autoHide) {
        this.autoHide.set(autoHide);
    }

    @Override
    public BooleanProperty autoHideProperty() {
        return autoHide;
    }

    @Override
    protected @Nullable PopupConfig getConfig() {
        return (PopupConfig) super.getConfig();
    }
}
