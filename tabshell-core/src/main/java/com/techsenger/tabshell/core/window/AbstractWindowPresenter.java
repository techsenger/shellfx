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

package com.techsenger.tabshell.core.window;

import com.techsenger.patternfx.mvp.AbstractChildPresenter;
import com.techsenger.patternfx.mvp.Presenter;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.settings.SettingsSubscription;
import com.techsenger.tabshell.material.icon.Icon;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowPresenter<T extends WindowView> extends AbstractChildPresenter<T>
        implements WindowPresenter<T> {

    private final WindowType windowType;

    private final boolean modal;

    private boolean alwaysOnTop;

    private double width;

    private double height;

    private double minWidth;

    private double minHeight;

    private double maxWidth;

    private double maxHeight;

    private boolean resizable;

    private String title;

    private boolean maximized;

    private boolean maximizable;

    private boolean minimized;

    private boolean minimizable;

    private boolean closable = true;

    private boolean blocked;

    private Icon<?> icon;

    private final AppearanceSettings appearanceSettings;

    private SettingsSubscription densitySubscription;

    private SettingsSubscription themeSubscription;

    private SettingsSubscription regularFontSubscription;

    private SettingsSubscription monospaceFontSubscription;

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    private boolean outOfBoundsAllowed;

    private boolean active;

    public AbstractWindowPresenter(T view, WindowParams params) {
        super(view, params);
        this.windowType = params.getWindowType();
        this.modal = params.isModal();
        this.appearanceSettings = params.getSettings();
    }

    @Override
    public WindowType getWindowType() {
        return this.windowType;
    }

    @Override
    public boolean isModal() {
        return modal;
    }

    @Override
    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        if (this.alwaysOnTop == alwaysOnTop) {
            return;
        }
        this.alwaysOnTop = alwaysOnTop;
        getView().setAlwaysOnTop(alwaysOnTop);
    }

    @Override
    public boolean isActive() {
        return active;
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
        if (this.maximized == maximized) {
            return;
        }
        this.maximized = maximized;
        getView().setMaximized(maximized);
    }

    @Override
    public boolean isMaximizable() {
        return maximizable;
    }

    @Override
    public void setMaximizable(boolean maximizable) {
        if (this.maximizable == maximizable) {
            return;
        }
        this.maximizable = maximizable;
        getView().setMaximizable(maximizable);
    }

    @Override
    public boolean isMinimized() {
        return minimized;
    }

    @Override
    public void setMinimized(boolean minimized) {
        if (this.minimized == minimized) {
            return;
        }
        this.minimized = minimized;
        getView().setMinimized(minimized);
    }

    @Override
    public boolean isMinimizable() {
        return minimizable;
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        if (this.minimizable == minimizable) {
            return;
        }
        this.minimizable = minimizable;
        getView().setMinimizable(minimizable);
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    @Override
    public void setClosable(boolean closable) {
        if (this.closable == closable) {
            return;
        }
        this.closable = closable;
        getView().setClosable(closable);
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
    public void close() {
        var iterator = getView().getComposer().breadthFirstPortIterator();
        while (iterator.hasNext()) {
            var c = iterator.next();
            if (iterator.getDepth() > 0) {
                ((Presenter<?>) c).deinitialize();
            }
        }
        if (getWindowType() == WindowType.NESTED) {
            getView().getComposer().close();
        } else {
            deinitialize();
            getView().closeWindow();
        }
        if (this.onClosed != null) {
            this.onClosed.run();
        }
    }

    @Override
    public void setBlocked(boolean blocked) {
        if (this.blocked == blocked) {
            return;
        }
        this.blocked = blocked;
        getView().setBlocked(blocked);
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    @Override
    public boolean isOutOfBoundsAllowed() {
        checkIfNested();
        return outOfBoundsAllowed;
    }

    @Override
    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        checkIfNested();
        if (this.outOfBoundsAllowed == outOfBoundsAllowed) {
            return;
        }
        this.outOfBoundsAllowed = outOfBoundsAllowed;
        getView().setOutOfBoundsAllowed(outOfBoundsAllowed);
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
    public double getMinWidth() {
        return minWidth;
    }

    @Override
    public void setMinWidth(double minWidth) {
        this.minWidth = minWidth;
        getView().setMinWidth(minWidth);
    }

    @Override
    public double getMinHeight() {
        return minHeight;
    }

    @Override
    public void setMinHeight(double minHeight) {
        this.minHeight = minHeight;
        getView().setMinHeight(minHeight);
    }

    @Override
    public double getMaxWidth() {
        return maxWidth;
    }

    @Override
    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
        getView().setMaxWidth(maxWidth);
    }

    @Override
    public double getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setMaxHeight(double maxHeight) {
        this.maxHeight = maxHeight;
        getView().setMaxHeight(maxHeight);
    }

    @Override
    public boolean isResizable() {
        return resizable;
    }

    @Override
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        getView().setResizable(resizable);
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }

    protected void onMaximize() {
        setMaximized(!maximized);
    }

    protected void onMinimize() {
        setMinimized(!minimized);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        if (this.windowType == WindowType.TOP_LEVEL) {
            getView().setDensity(this.appearanceSettings.getDensity());
            getView().setRegularFont(this.appearanceSettings.getRegularFont());
            getView().setMonospaceFont(this.appearanceSettings.getMonospaceFont());
            getView().setTheme(this.appearanceSettings.getTheme());
            this.densitySubscription =
                    this.appearanceSettings.onDensityChanged((oldV, newV) -> getView().setDensity(newV));
            this.monospaceFontSubscription =
                    this.appearanceSettings.onMonospaceFontChanged((oldV, newV) -> getView().setMonospaceFont(newV));
            this.regularFontSubscription =
                    this.appearanceSettings.onRegularFontChanged((oldV, newV) -> getView().setRegularFont(newV));
            this.themeSubscription = this.appearanceSettings.onThemeChanged((oldV, newV) -> getView().setTheme(newV));
        }
        getView().setModal(modal);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        if (this.windowType == WindowType.TOP_LEVEL) {
            this.densitySubscription.unsubscribe();
            this.monospaceFontSubscription.unsubscribe();
            this.regularFontSubscription.unsubscribe();
            this.themeSubscription.unsubscribe();
        }
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

    protected void onActiveChanged(boolean active) {
        this.active = active;
    }

    @Override
    protected WindowHistory getHistory() {
        return (WindowHistory) super.getHistory();
    }

    @Override
    protected void restoreAppearance() {
        super.restoreAppearance();
        var h = getHistory();
        setMaximized(h.isMaximized());
        setHeight(h.getHeight());
        setWidth(h.getWidth());
    }

    @Override
    protected void saveAppearance() {
        super.saveAppearance();
        var h = getHistory();
        h.setWidth(getWidth());
        h.setHeight(getHeight());
        h.setMaximized(isMaximized());
    }

    protected AppearanceSettings getAppearanceSettings() {
        return appearanceSettings;
    }

    private void checkIfNested() {
        if (windowType != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }
}
