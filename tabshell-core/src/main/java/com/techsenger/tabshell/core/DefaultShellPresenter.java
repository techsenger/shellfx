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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.AbstractParentPresenter;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuHelpers;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.core.shelltab.ShellTabPort;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellPresenter<V extends ShellView, C extends ShellComposer>
        extends AbstractParentPresenter<V, C> implements ShellPresenter<V, C> {

    protected class Port extends AbstractParentPresenter.Port implements ShellPort {

        private final DefaultShellPresenter<?, ?> presenter = DefaultShellPresenter.this;

        @Override
        public ShellTabPort getSelectedTab() {
            return getComposer().getSelectedTab();
        }

        @Override
        public void selectTab(int tabIndex) {
            getView().selectTab(tabIndex);
        }

        @Override
        public int getSelectedTabIndex() {
            return getView().getSelectedTabIndex();
        }

        @Override
        public HistoryManager getHistoryManager() {
            return presenter.getHistoryManager();
        }

        @Override
        public Settings getSettings() {
            return presenter.getSettings();
        }

        @Override
        public <T extends Settings> T getSettings(Class<T> settingsClass) {
            return presenter.getSettings(settingsClass);
        }

        @Override
        public double getWidth() {
            return getView().getWidth();
        }

        @Override
        public double getHeight() {
            return getView().getHeight();
        }

        @Override
        public boolean isMaximized() {
            return getView().isMaximized();
        }

        @Override
        public String getTitle() {
            return getView().getTitle();
        }

        @Override
        public List<? extends ShellTabPort> getTabs() {
            return getComposer().getTabs();
        }

        @Override
        public Icon<?> getIcon() {
            return getView().getIcon();
        }

        @Override
        public List<? extends PopupPort> getPopups() {
            return getComposer().getPopups();
        }

        @Override
        public List<? extends DialogPort> getDialogs() {
            return getComposer().getDialogs();
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
        public void closeOtherTabs(ShellTabPort tab) {
            presenter.handleCloseOtherTabs(tab);
        }

        @Override
        public void closeTabs(List<? extends ShellTabPort> tabs) {
            presenter.handleCloseTabs(tabs);
        }

        @Override
        public void closeAllTabs() {
            presenter.handleCloseAllTabs();
        }

        @Override
        public void closeRightTabs(ShellTabPort tab) {
            presenter.handleCloseRightTabs(tab);
        }

        @Override
        public void closeLeftTabs(ShellTabPort tab) {
            presenter.handleCloseLeftTabs(tab);
        }

        @Override
        public void closeTab(ShellTabPort tab) {
            presenter.handleCloseTab(tab);
        }
    }

    private final Settings settings;

    private final HistoryManager historyManager;

    private final MenuHelpers menuHelpers = new MenuHelpers();

    private Runnable onClose;

    public DefaultShellPresenter(V view, Settings settings, HistoryManager historyManager) {
        super(view);
        this.settings = settings;
        this.historyManager = historyManager;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DefaultShellHistory.class, DefaultShellHistory::new));
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public <T extends Settings> T getSettings(Class<T> settingsClass) {
        return (T) settings;
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
    public void handleSelectedTabChange(int index) {
        // empty
    }

    @Override
    public void close() {
        var iterator = getComposer().breadthFirstIterator();
        while (iterator.hasNext()) {
            var c = iterator.next();
            if (iterator.getDepth() > 0) {
                c.deinitialize();
            }
        }
        // the shell is deinitilized at the end
        deinitialize();
        if (this.onClose != null) {
            this.onClose.run();
        }
    }

    @Override
    public MenuHelpers getMenuHelpers() {
        return menuHelpers;
    }

    @Override
    public ShellPort getPort() {
        return (ShellPort) super.getPort();
    }

    @Override
    public MenuHelper getMenuHelper(MenuName menuName) {
        return this.menuHelpers.getMenuHelpersByName().get(menuName);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
        return this.menuHelpers.getMenuItemHelpersByName().get(menuItemName);
    }

    @Override
    public void handleMenuShowing(MenuName menuName) {
        // empty
    }

    @Override
    public void handleMenuHiding(MenuName menuName) {
        // empty
    }

    @Override
    public Runnable getOnClose() {
        return this.onClose;
    }

    @Override
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @Override
    protected DefaultShellHistory getHistory() {
        return (DefaultShellHistory) super.getHistory();
    }

    @Override
    protected Port createPort() {
        return new DefaultShellPresenter.Port();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var appearance = getSettings().getAppearance();
        getView().setRegularFont(appearance.getRegularFont());
        appearance.observeRegularFont((oldV, newV) -> getView().setRegularFont(newV));
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        var v = getView();
        if (!h.isFresh()) {
            if (h.isMaximized()) {
                v.setMaximized(true);
            } else {
                v.setHeight(h.getHeight());
                v.setWidth(h.getWidth());
            }
        } else {
            v.setHeight(ShellView.DEFAULT_HEIGHT);
            v.setWidth(ShellView.DEFAULT_WIDTH); // todo:
        }
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        var v = getView();
        h.setWidth(v.getWidth());
        h.setHeight(v.getHeight());
        h.setMaximized(v.isMaximized());
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(CoreComponents.SHELL);
    }
}
