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
import com.techsenger.patternfx.mvp.Presenter;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuHelpers;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.settings.Settings;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.List;
import java.util.function.Consumer;
import javafx.application.HostServices;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultShellPresenter<V extends ShellView, C extends ShellComposer>
        extends AbstractParentPresenter<V, C> implements ShellPresenter<V, C> {

    private final Settings settings;

    private final HistoryManager historyManager;

    private final HostServices hostServices;

    private final MenuHelpers menuHelpers = new MenuHelpers();

    private Runnable onClose;

    private double width;

    private double height;

    private String title;

    private boolean maximized;

    private Icon<?> icon;

    public DefaultShellPresenter(V view, Settings settings, HistoryManager historyManager, HostServices hostServices) {
        super(view);
        this.settings = settings;
        this.historyManager = historyManager;
        this.hostServices = hostServices;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DefaultShellHistory.class, DefaultShellHistory::new));
    }

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public HostServices getHostServices() {
        return this.hostServices;
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
    public void close() {
        var iterator = getComposer().breadthFirstIterator();
        while (iterator.hasNext()) {
            var c = iterator.next();
            if (iterator.getDepth() > 0) {
                ((Presenter<?>) c).deinitialize();
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
    public Runnable getOnClose() {
        return this.onClose;
    }

    @Override
    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public void setWidth(double width) {
        this.width = width;
        getView().setWidth(width);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
        getView().setHeight(height);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        getView().setTitle(title);
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon = icon;
        getView().setIcon(icon);
    }

    @Override
    public boolean isMaximized() {
        return maximized;
    }

    @Override
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
        getView().setMaximized(maximized);
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
    public MenuHelper getMenuHelper(MenuName menuName) {
        return menuHelpers.getMenuHelpersByName().get(menuName);
    }

    @Override
    public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
        return menuHelpers.getMenuItemHelpersByName().get(menuItemName);
    }

    @Override
    public void onMenuShowing(MenuName menuName) {
        // empty
    }

    @Override
    public void onMenuHiding(MenuName menuName) {
        // empty
    }

    @Override
    protected DefaultShellHistory getHistory() {
        return (DefaultShellHistory) super.getHistory();
    }

    protected void onWidthChanged(double width) {
        this.width = width;
    }

    protected void onHeightChanged(double height) {
        this.height = height;
    }

    protected void onMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        if (!h.isNew()) {
            if (h.isMaximized()) {
                setMaximized(true);
            } else {
                setHeight(h.getHeight());
                setWidth(h.getWidth());
            }
        } else {
            setHeight(ShellView.DEFAULT_HEIGHT);
            setWidth(ShellView.DEFAULT_WIDTH);
        }
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.setWidth(getWidth());
        h.setHeight(getHeight());
        h.setMaximized(isMaximized());
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(CoreComponents.SHELL);
    }
}
