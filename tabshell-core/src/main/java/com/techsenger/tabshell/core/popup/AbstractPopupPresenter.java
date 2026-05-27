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

package com.techsenger.tabshell.core.popup;

import com.techsenger.tabshell.core.area.AbstractAreaPresenter;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupPresenter<V extends PopupView> extends AbstractAreaPresenter<V>
        implements PopupPresenter<V> {

    private final boolean modal;

    private double prefWidth;

    private double prefHeight;

    public AbstractPopupPresenter(V view, PopupParams params) {
        super(view, params);
        this.modal = params.isModal();
    }

    @Override
    public boolean isModal() {
        return this.modal;
    }

    @Override
    public void close() {
        getView().getComposer().close();
    }

    @Override
    public double getPrefWidth() {
        return this.prefWidth;
    }

    @Override
    public double getPrefHeight() {
        return this.prefHeight;
    }

    public void setPrefWidth(double prefWidth) {
        this.prefWidth = prefWidth;
        getView().setPrefWidth(prefWidth);
    }

    public void setPrefHeight(double prefHeight) {
        this.prefHeight = prefHeight;
        getView().setPrefHeight(prefHeight);
    }

    @Override
    protected PopupHistory getHistory() {
        return (PopupHistory) super.getHistory();
    }
}
