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

package com.techsenger.tabshell.core.shelltab;

import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellTabPresenter<V extends ShellTabView, C extends ShellTabComposer>
        extends AbstractTabPresenter<V, C> implements ShellTabPresenter<V, C> {

    protected class Port extends AbstractTabPresenter<V, C>.Port implements ShellTabPort {

        private final AbstractShellTabPresenter<V, C> presenter = AbstractShellTabPresenter.this;

        public Port() {
            // empty
        }

        @Override
        public List<? extends DialogPort> getDialogs() {
            return getComposer().getDialogs();
        }

        @Override
        public List<? extends PopupPort> getPopups() {
            return getComposer().getPopups();
        }

        @Override
        public ShellPort getShell() {
            return presenter.getComposer().getShell();
        }
    }

    public AbstractShellTabPresenter(V view) {
        super(view);
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new AbstractShellTabPresenter.Port();
    }

    @Override
    protected ShellTabHistory getHistory() {
        return (ShellTabHistory) super.getHistory();
    }
}
