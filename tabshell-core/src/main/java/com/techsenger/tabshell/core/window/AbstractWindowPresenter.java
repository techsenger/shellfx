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

import com.techsenger.patternfx.mvp.AbstractParentPresenter;
import com.techsenger.patternfx.mvp.ComponentPresenter;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.settings.SettingsSubscription;
import com.techsenger.tabshell.material.icon.Icon;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowPresenter<T extends WindowView> extends AbstractParentPresenter<T>
        implements WindowPresenter<T> {

    private double width;

    private double height;

    private String title;

    private boolean maximized = false;

    private boolean maximizable = true;

    private boolean minimized = false;

    private boolean minimizable = true;

    private boolean closable = true;

    private Icon<?> icon;

    private final AppearanceSettings setting;

    private SettingsSubscription themeSubscription;

    private SettingsSubscription regularFontSubscription;

    private SettingsSubscription monospaceFontSubscription;

    public AbstractWindowPresenter(T view, AppearanceSettings setting) {
        super(view);
        this.setting = setting;
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
    public boolean isMaximizable() {
        return maximizable;
    }

    @Override
    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
        getView().setMaximizable(maximizable);
    }

    @Override
    public boolean isMinimized() {
        return minimized;
    }

    @Override
    public void setMinimized(boolean minimized) {
        this.minimized = minimized;
        getView().setMinimized(minimized);
    }

    @Override
    public boolean isMinimizable() {
        return minimizable;
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        this.minimizable = minimizable;
        getView().setMinimizable(minimizable);
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    @Override
    public void setClosable(boolean closable) {
        this.closable = closable;
        getView().setClosable(closable);
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
        var iterator = getView().getComposer().breadthFirstIterator();
        while (iterator.hasNext()) {
            var c = iterator.next();
            if (iterator.getDepth() > 0) {
                ((ComponentPresenter<?>) c).deinitialize();
            }
        }
        // the window is deinitilized at the end
        deinitialize();
        getView().closeWindow();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().setRegularFont(this.setting.getRegularFont());
        getView().setMonospaceFont(this.setting.getMonospaceFont());
        getView().setTheme(this.setting.getTheme());
        this.monospaceFontSubscription =
                this.setting.onMonospaceFontChanged((oldV, newV) -> getView().setMonospaceFont(newV));
        this.regularFontSubscription =
                this.setting.onRegularFontChanged((oldV, newV) -> getView().setRegularFont(newV));
        this.themeSubscription = this.setting.onThemeChanged((oldV, newV) -> getView().setTheme(newV));
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.monospaceFontSubscription.unsubscribe();
        this.regularFontSubscription.unsubscribe();
        this.themeSubscription.unsubscribe();
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
    protected WindowHistory getHistory() {
        return (WindowHistory) super.getHistory();
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        setWidth(1200);
        setHeight(800);
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
}
