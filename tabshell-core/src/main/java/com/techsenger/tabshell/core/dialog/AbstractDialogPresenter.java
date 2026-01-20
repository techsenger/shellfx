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

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.CloseRequestResult;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogPresenter<V extends DialogView, C extends DialogComposer>
        extends AbstractAreaPresenter<V, C> implements DialogPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements DialogPort {

        private final AbstractDialogPresenter<?, ?> presenter = AbstractDialogPresenter.this;

        public Port() {
            // empty
        }

        @Override
        public DialogScope getScope() {
            return presenter.getScope();
        }

        @Override
        public boolean isActive() {
            return presenter.getView().isActive();
        }

        @Override
        public void close() {
            presenter.close();
        }

        @Override
        public void requestClose(int maxAttempts, Consumer<CloseRequestResult> resultConsumer) {
            presenter.requestClose(maxAttempts, resultConsumer);
        }

        @Override
        public CloseCheckResult isReadyToClose() {
            return presenter.isReadyToClose();
        }

        @Override
        public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
            presenter.prepareToClose(resultCallback);
        }

        @Override
        public Runnable getCloseAction() {
            return presenter.getCloseAction();
        }

        @Override
        public void setCloseAction(Runnable value) {
            presenter.setCloseAction(closeAction);
        }
    }

    private final DialogScope scope;

    /**
     * If it is necessary to close a dialog then dialog helper should be used. Default implementation uses window
     * closer set from view.
     */
    private Runnable closeAction = () -> requestClose();

    public AbstractDialogPresenter(V view, DialogScope scope) {
        super(view);
        this.scope = scope;
    }

    @Override
    public DialogScope getScope() {
        return scope;
    }

    @Override
    public Runnable getCloseAction() {
        return closeAction;
    }

    @Override
    public DialogPort getPort() {
        return (DialogPort) super.getPort();
    }

    @Override
    public void close() {
        getComposer().remove();
    }

    protected void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    @Override
    protected DialogHistory getHistory() {
        return (DialogHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        var v = getView();
        v.setPrefWidth(h.getWidth());
        v.setPrefHeight(h.getHeight());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        var v = getView();
        h.setWidth(v.getWidth());
        h.setHeight(v.getHeight());
    }
}
