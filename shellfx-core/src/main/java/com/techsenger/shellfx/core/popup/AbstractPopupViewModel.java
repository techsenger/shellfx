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

import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupViewModel<C extends PopupComposer> extends AbstractAreaViewModel<C>
        implements PopupViewModel<C> {

    private final boolean modal;

    private final ObservableSource<Double> widthSource = new SimpleObservableSource<>();

    private final ObservableSource<Double> heightSource = new SimpleObservableSource<>();

    private final BooleanProperty waiting = new SimpleBooleanProperty();

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    public AbstractPopupViewModel(PopupParams params) {
        super(params);
        this.modal = params.isModal();
    }

    @Override
    public PopupPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    public boolean isModal() {
        return this.modal;
    }

    @Override
    public void close() {
        getComposer().close();
        if (this.onClosed != null) {
            this.onClosed.run();
        }
    }

    @Override
    @RequestSetter
    public void setWidth(double width) {
        this.widthSource.next(width);
    }

    @Override
    @RequestSetter
    public void setHeight(double height) {
        this.heightSource.next(height);
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
    public Runnable getOnCloseRequest() {
        return this.onCloseRequest;
    }

    @Override
    public void setOnCloseRequest(Runnable runnable) {
        this.onCloseRequest = runnable;
    }

    @Override
    public Runnable getOnClosed() {
        return onClosed;
    }

    @Override
    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    @Override
    protected PopupHistory getHistory() {
        return (PopupHistory) super.getHistory();
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }

    ObservableSource<Double> widthSource() {
        return this.widthSource;
    }

    ObservableSource<Double> heightSource() {
        return this.heightSource;
    }
}
