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

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.CloseRequestResult;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPopupPresenter<V extends PopupView, C extends PopupComposer>
        extends AbstractAreaPresenter<V, C> implements PopupPresenter<V, C> {

    protected class Port extends AbstractAreaPresenter.Port implements PopupPort {

        private final AbstractPopupPresenter<?, ?> presenter = AbstractPopupPresenter.this;

        public Port() {
            // empty
        }

        @Override
        public OverlayScope getOverlayScope() {
            return presenter.getOverlayScope();
        }

        @Override
        public boolean isModal() {
            return presenter.isModal();
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
    }

    private final OverlayScope scope;

    private final boolean modal;

    public AbstractPopupPresenter(V view, OverlayScope scope, boolean modal) {
        super(view);
        this.scope = scope;
        this.modal = modal;
    }

    @Override
    public OverlayScope getOverlayScope() {
        return scope;
    }

    @Override
    public boolean isModal() {
        return this.modal;
    }

    @Override
    public PopupPort getPort() {
        return (PopupPort) super.getPort();
    }

    @Override
    public void close() {
        getComposer().remove();
    }

    @Override
    protected PopupHistory getHistory() {
        return (PopupHistory) super.getHistory();
    }

    @Override
    protected Port createPort() {
        return new AbstractPopupPresenter.Port();
    }
}
