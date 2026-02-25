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

import com.techsenger.tabshell.core.popup.AbstractPopupPresenter;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.material.button.ResultButtonName;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogPresenter<V extends DialogView, C extends DialogComposer>
        extends AbstractPopupPresenter<V, C> implements DialogPresenter<V, C> {

    protected class Port extends AbstractPopupPresenter<V, C>.Port implements DialogPort {

        private final AbstractDialogPresenter<V, C> presenter = AbstractDialogPresenter.this;

        public Port() {
            // empty
        }

        @Override
        public boolean isActive() {
            return presenter.getView().isActive();
        }

        @Override
        public Runnable getCloseAction() {
            return presenter.getCloseAction();
        }

        @Override
        public void setCloseAction(Runnable value) {
            presenter.setCloseAction(value);
        }

        @Override
        public Consumer<ResultButtonName> getResultAction() {
            return presenter.resultAction;
        }

        @Override
        public void setResultAction(Consumer<ResultButtonName> action) {
            presenter.resultAction = action;
        }

        @Override
        public double getMinWidth() {
            return getView().getMinWidth();
        }

        @Override
        public double getMinHeight() {
            return getView().getMinHeight();
        }

        @Override
        public double getMaxWidth() {
            return getView().getMaxWidth();
        }

        @Override
        public double getMaxHeight() {
            return getView().getMaxHeight();
        }

        @Override
        public boolean isOutOfBoundsAllowed() {
            return getView().isOutOfBoundsAllowed();
        }

        @Override
        public boolean isResizable() {
            return getView().isResizable();
        }

        @Override
        public boolean isButtonWidthEqual() {
            return getView().isButtonWidthEqual();
        }

        @Override
        public boolean isCloseDisabled() {
            return getView().isCloseDisabled();
        }

        @Override
        public List<ResultButtonName> getLeftButtons() {
            return getView().getLeftButtons();
        }

        @Override
        public List<ResultButtonName> getRightButtons() {
            return getView().getRightButtons();
        }
    }

    /**
     * If it is necessary to close a dialog then dialog helper should be used. Default implementation uses window
     * closer set from view.
     */
    private Runnable closeAction = () -> requestClose();

    private Consumer<ResultButtonName> resultAction = (name) -> requestClose();

    public AbstractDialogPresenter(V view, OverlayScope scope) {
        super(view, scope, true);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
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
    protected Port createPort() {
        return new AbstractDialogPresenter.Port();
    }

    protected Runnable getCloseAction() {
        return closeAction;
    }

    protected void setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
    }

    protected Consumer<ResultButtonName> getResultAction() {
        return resultAction;
    }

    protected void setResultAction(Consumer<ResultButtonName> resultAction) {
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
