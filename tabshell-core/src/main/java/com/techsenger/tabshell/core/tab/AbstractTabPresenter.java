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

package com.techsenger.tabshell.core.tab;

import com.techsenger.patternfx.mvp.AbstractChildPresenter;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.CloseRequestResult;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.menu.MenuHelpers;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabPresenter<V extends TabView, C extends TabComposer>
        extends AbstractChildPresenter<V, C> implements TabPresenter<V, C> {

    protected class Port extends AbstractChildPresenter<V, C>.Port implements TabPort {

        private final AbstractTabPresenter<V, C> presenter = AbstractTabPresenter.this;

        public Port() {
            // empty
        }

        @Override
        public boolean isWaiting() {
            return presenter.getView().isWaiting();
        }

        @Override
        public void setWaiting(boolean waiting) {
            presenter.getView().setWaiting(waiting);
        }

        @Override
        public String getTitle() {
            return presenter.getView().getTitle();
        }

        @Override
        public void setTitle(String title) {
            presenter.getView().setTitle(title);
        }

        @Override
        public Icon<?> getIcon() {
            return presenter.getView().getIcon();
        }

        @Override
        public void setIcon(Icon<?> icon) {
            presenter.getView().setIcon(icon);
        }

        @Override
        public String getTooltip() {
            return presenter.getView().getTooltip();
        }

        @Override
        public void setTooltip(String tooltip) {
            presenter.getView().setTooltip(tooltip);
        }

        @Override
        public boolean isSelected() {
            return presenter.getView().isSelected();
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
        public boolean isClosable() {
            return getView().isClosable();
        }

        @Override
        public void setClosable(boolean value) {
            getView().setClosable(value);
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

    private final MenuHelpers menuHelpers = new MenuHelpers();

    public AbstractTabPresenter(V view) {
        super(view);
    }

    @Override
    public void onSelected(boolean selected) {
        // empty
    }

    public MenuHelpers getMenuHelpers() {
        return menuHelpers;
    }

    @Override
    public void close() {
        getComposer().remove();
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new AbstractTabPresenter.Port();
    }

    @Override
    protected TabHistory getHistory() {
        return (TabHistory) super.getHistory();
    }

    protected void onMenuShowing(MenuName menuName) {
        // empty
    }

    protected void onMenuHiding(MenuName menuName) {
        // empty
    }
}
