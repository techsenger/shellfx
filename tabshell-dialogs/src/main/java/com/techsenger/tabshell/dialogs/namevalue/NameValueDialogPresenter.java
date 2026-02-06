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

package com.techsenger.tabshell.dialogs.namevalue;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.dialogs.DialogComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class NameValueDialogPresenter<V extends NameValueDialogView, C extends DialogComposer>
        extends AbstractDialogPresenter<V, C> {

    protected class Port extends AbstractDialogPresenter.Port implements NameValueDialogPort {

        private final NameValueDialogPresenter<V, C> presenter = NameValueDialogPresenter.this;

        @Override
        public void setCancelVisible(boolean visible) {
            getView().setButtonVisible(NameValueButtons.CANCEL, visible);
        }

        @Override
        public boolean isCancelVisible() {
            return getView().getButtonVisible(NameValueButtons.CANCEL).get();
        }

        @Override
        public void setName(String name) {
            getView().setName(name);
        }

        @Override
        public String getName() {
            return getView().getName();
        }

        @Override
        public void setNameEditable(boolean value) {
            getView().setNameEditable(value);
        }

        @Override
        public boolean isNameEditable() {
            return getView().isNameEditable();
        }

        @Override
        public void setValue(String value) {
            getView().setValue(value);
        }

        @Override
        public String getValue() {
            return getView().getValue();
        }

        @Override
        public void setValueEditable(boolean value) {
            getView().setValueEditable(value);
        }

        @Override
        public boolean isValueEditable() {
            return getView().isValueEditable();
        }

        @Override
        public void setTitle(String title) {
            getView().setTitle(title);
        }
    }

    public NameValueDialogPresenter(V view, OverlayScope scope) {
        super(view, scope);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DialogComponents.NAME_VALUE_DIALOG);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NameValueDialogPort getPort() {
        return (NameValueDialogPort) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new NameValueDialogPresenter.Port();
    }
}
