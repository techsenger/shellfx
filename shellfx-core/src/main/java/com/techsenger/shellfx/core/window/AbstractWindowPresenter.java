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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.AbstractChildPresenter;
import com.techsenger.patternfx.mvp.Presenter;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.SettingsSubscription;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.Objects;
import javafx.scene.text.Font;

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

    private boolean resizable = true;

    private String title;

    private boolean maximized;

    private boolean maximizable;

    private boolean minimized;

    private boolean minimizable;

    private boolean closable = true;

    private boolean blocked;

    private Icon<?> icon;

    private Density density;

    private Theme theme;

    private Font regularFont;

    private Font monospaceFont;

    private final AppearanceSettings appearanceSettings;

    private SettingsSubscription densitySubscription;

    private SettingsSubscription themeSubscription;

    private SettingsSubscription regularFontSubscription;

    private SettingsSubscription monospaceFontSubscription;

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    private boolean outOfBoundsAllowed;

    private boolean active;

    private double x;

    private double y;

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
        getView().updateAlwaysOnTop(alwaysOnTop);
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
        if (this.width == width) {
            return;
        }
        this.width = width;
        getView().updateWidth(width);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        if (this.height == height) {
            return;
        }
        this.height = height;
        getView().updateHeight(height);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        if (Objects.equals(this.title, title)) {
            return;
        }
        this.title = title;
        getView().updateTitle(title);
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public void setIcon(Icon<?> icon) {
        if (Objects.equals(this.icon, icon)) {
            return;
        }
        this.icon = icon;
        getView().updateIcon(icon);
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
        getView().updateMaximized(maximized);
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
        getView().updateMaximizable(maximizable);
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
        getView().updateMinimized(minimized);
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
        getView().updateMinimizable(minimizable);
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
        getView().updateClosable(closable);
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
        if (getWindowType() == WindowType.NESTED) {
            getView().getComposer().close();
        } else {
            var iterator = getView().getComposer().breadthFirstPortIterator();
            while (iterator.hasNext()) {
                var c = iterator.next();
                if (iterator.getDepth() > 0) {
                    ((Presenter<?>) c).deinitialize();
                }
            }
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
        getView().updateBlocked(blocked);
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
        getView().updateOutOfBoundsAllowed(outOfBoundsAllowed);
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
        if (this.minWidth == minWidth) {
            return;
        }
        this.minWidth = minWidth;
        getView().updateMinWidth(minWidth);
    }

    @Override
    public double getMinHeight() {
        return minHeight;
    }

    @Override
    public void setMinHeight(double minHeight) {
        if (this.minHeight == minHeight) {
            return;
        }
        this.minHeight = minHeight;
        getView().updateMinHeight(minHeight);
    }

    @Override
    public double getMaxWidth() {
        return maxWidth;
    }

    @Override
    public void setMaxWidth(double maxWidth) {
        if (this.maxWidth == maxWidth) {
            return;
        }
        this.maxWidth = maxWidth;
        getView().updateMaxWidth(maxWidth);
    }

    @Override
    public double getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setMaxHeight(double maxHeight) {
        if (this.maxHeight == maxHeight) {
            return;
        }
        this.maxHeight = maxHeight;
        getView().updateMaxHeight(maxHeight);
    }

    @Override
    public boolean isResizable() {
        return resizable;
    }

    @Override
    public void setResizable(boolean resizable) {
        if (this.resizable == resizable) {
            return;
        }
        this.resizable = resizable;
        getView().updateResizable(resizable);
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public @Nullable Density getDensity() {
        return this.density;
    }

    @Override
    public Theme getTheme() {
        return this.theme;
    }

    @Override
    public Font getRegularFont() {
        return this.regularFont;
    }

    @Override
    public Font getMonospaceFont() {
        return this.monospaceFont;
    }

    @Override
    public void setX(double x) {
        if (this.x == x) {
            return;
        }
        this.x = x;
        getView().updateX(x);
    }

    @Override
    public void setY(double y) {
        if (this.y == y) {
            return;
        }
        this.y = y;
        getView().updateY(y);
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
            setDensity(this.appearanceSettings.getDensity());
            setRegularFont(this.appearanceSettings.getRegularFont());
            setMonospaceFont(this.appearanceSettings.getMonospaceFont());
            this.densitySubscription =
                    this.appearanceSettings.onDensityChanged((oldV, newV) -> setDensity(newV));
            this.monospaceFontSubscription =
                    this.appearanceSettings.onMonospaceFontChanged((oldV, newV) -> setMonospaceFont(newV));
            this.regularFontSubscription =
                    this.appearanceSettings.onRegularFontChanged((oldV, newV) -> setRegularFont(newV));
        }
        setTheme(this.appearanceSettings.getTheme());
        this.themeSubscription = this.appearanceSettings.onThemeChanged((oldV, newV) -> setTheme(newV));
        if (modal) {
            getView().updateModal(); // it is not possible to set modality for the primary stage
        }
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        if (this.windowType == WindowType.TOP_LEVEL) {
            this.densitySubscription.unsubscribe();
            this.monospaceFontSubscription.unsubscribe();
            this.regularFontSubscription.unsubscribe();
        }
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

    protected void onMinimized(boolean minimized) {
        this.minimized = minimized;
    }

    protected void onActiveChanged(boolean active) {
        this.active = active;
    }

    protected void onXChanged(double x) {
        this.x = x;
    }

    protected void onYChanged(double y) {
        this.y = y;
    }

    @Override
    protected WindowHistory getHistory() {
        return (WindowHistory) super.getHistory();
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setMaximized(h.isMaximized());
        setHeight(h.getHeight());
        setWidth(h.getWidth());
    }

    @Override
    protected void savePersistentState() {
        super.savePersistentState();
        var h = getHistory();
        h.setWidth(getWidth());
        h.setHeight(getHeight());
        h.setMaximized(isMaximized());
    }

    protected AppearanceSettings getAppearanceSettings() {
        return appearanceSettings;
    }

    protected void setDensity(@Nullable Density density) {
        if (Objects.equals(this.density, density)) {
            return;
        }
        this.density = density;
        getView().updateDensity(density);
    }

    protected void setTheme(Theme theme) {
        if (Objects.equals(this.theme, theme)) {
            return;
        }
        this.theme = theme;
        getView().updateTheme(theme);
    }

    protected void setRegularFont(Font font) {
        if (Objects.equals(this.regularFont, font)) {
            return;
        }
        this.regularFont = font;
        getView().updateRegularFont(font);
    }

    protected void setMonospaceFont(Font font) {
        if (Objects.equals(this.monospaceFont, font)) {
            return;
        }
        this.monospaceFont = font;
        getView().updateMonospaceFont(font);
    }

    private void checkIfNested() {
        if (windowType != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }
}
