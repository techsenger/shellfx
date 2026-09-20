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
import com.techsenger.patternfx.mvvm.AbstractChildViewModel;
import com.techsenger.patternfx.mvvm.ViewModel;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.SettingsSubscription;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowViewModel<C extends WindowComposer> extends AbstractChildViewModel<C>
        implements WindowViewModel<C> {

    private final WindowType windowType;

    private final boolean modal;

    private final BooleanProperty alwaysOnTop = new SimpleBooleanProperty();

    private final ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> widthSource = new SimpleObservableSource<>();

    private final ReadOnlyDoubleWrapper height = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> heightSource = new SimpleObservableSource<>();

    private final DoubleProperty minWidth = new SimpleDoubleProperty();

    private final DoubleProperty minHeight = new SimpleDoubleProperty();

    private final DoubleProperty maxWidth = new SimpleDoubleProperty();

    private final DoubleProperty maxHeight = new SimpleDoubleProperty();

    private final BooleanProperty resizable = new SimpleBooleanProperty(true);

    private final StringProperty title = new SimpleStringProperty();

    private final ReadOnlyBooleanWrapper maximized = new ReadOnlyBooleanWrapper();

    private final ObservableSource<Boolean> maximizedSource = new SimpleObservableSource<>();

    private final BooleanProperty maximizable = new SimpleBooleanProperty();

    private final ReadOnlyBooleanWrapper minimized = new ReadOnlyBooleanWrapper();

    private final ObservableSource<Boolean> minimizedSource = new SimpleObservableSource<>();

    private final BooleanProperty minimizable = new SimpleBooleanProperty();

    private final BooleanProperty closable = new SimpleBooleanProperty(true);

    private final BooleanProperty blocked = new SimpleBooleanProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    private final ReadOnlyObjectWrapper<Density> density = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Font> regularFont = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Font> monospaceFont = new ReadOnlyObjectWrapper<>();

    private final AppearanceSettings appearanceSettings;

    private SettingsSubscription densitySubscription;

    private SettingsSubscription themeSubscription;

    private SettingsSubscription regularFontSubscription;

    private SettingsSubscription monospaceFontSubscription;

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    private final BooleanProperty outOfBoundsAllowed = new SimpleBooleanProperty();

    private final ReadOnlyBooleanWrapper active = new ReadOnlyBooleanWrapper();

    private final ReadOnlyDoubleWrapper x = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> xSource = new SimpleObservableSource<>();

    private final ReadOnlyDoubleWrapper y = new ReadOnlyDoubleWrapper();

    private final ObservableSource<Double> ySource = new SimpleObservableSource<>();

    private final BooleanProperty resizing = new SimpleBooleanProperty();

    private final ObservableSource<Void> closeWindowSource = new SimpleObservableSource<>();

    public AbstractWindowViewModel(WindowParams params) {
        super(params);
        this.windowType = params.getWindowType();
        this.modal = params.isModal();
        this.appearanceSettings = params.getSettings();
    }

    @Override
    public WindowPort.ComposerAccess getComposerAccess() {
        return getComposer();
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
        return alwaysOnTop.get();
    }

    @Override
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop.set(alwaysOnTop);
    }

    @Override
    public BooleanProperty alwaysOnTopProperty() {
        return alwaysOnTop;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public void setActive(boolean active) {
        this.active.set(active);
    }

    @Override
    public ReadOnlyBooleanProperty activeProperty() {
        return active.getReadOnlyProperty();
    }

    @Override
    public double getWidth() {
        return width.get();
    }

    @Override
    @RequestSetter
    public void setWidth(double width) {
        widthSource.next(width);
    }

    @Override
    public ReadOnlyDoubleProperty widthProperty() {
        return width.getReadOnlyProperty();
    }

    @Override
    public double getHeight() {
        return height.get();
    }

    @Override
    @RequestSetter
    public void setHeight(double height) {
        heightSource.next(height);
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return height.getReadOnlyProperty();
    }

    @Override
    public String getTitle() {
        return title.get();
    }

    @Override
    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public Icon<?> getIcon() {
        return icon.get();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon.set(icon);
    }

    @Override
    public ObjectProperty<Icon<?>> iconProperty() {
        return icon;
    }

    @Override
    public boolean isMaximized() {
        return maximized.get();
    }

    @Override
    @RequestSetter
    public void setMaximized(boolean maximized) {
        maximizedSource.next(maximized);
    }

    @Override
    public ReadOnlyBooleanProperty maximizedProperty() {
        return maximized.getReadOnlyProperty();
    }

    @Override
    public boolean isMaximizable() {
        return maximizable.get();
    }

    @Override
    public void setMaximizable(boolean maximizable) {
        this.maximizable.set(maximizable);
    }

    @Override
    public BooleanProperty maximizableProperty() {
        return maximizable;
    }

    @Override
    public boolean isMinimized() {
        return minimized.get();
    }

    @Override
    @RequestSetter
    public void setMinimized(boolean minimized) {
        minimizedSource.next(minimized);
    }

    @Override
    public ReadOnlyBooleanProperty minimizedProperty() {
        return minimized.getReadOnlyProperty();
    }

    @Override
    public boolean isMinimizable() {
        return minimizable.get();
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        this.minimizable.set(minimizable);
    }

    @Override
    public BooleanProperty minimizableProperty() {
        return minimizable;
    }

    @Override
    public boolean isClosable() {
        return closable.get();
    }

    @Override
    public void setClosable(boolean closable) {
        this.closable.set(closable);
    }

    @Override
    public BooleanProperty closableProperty() {
        return closable;
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
            getComposer().close();
        } else {
            var iterator = getComposer().breadthFirstPortIterator();
            while (iterator.hasNext()) {
                var c = iterator.next();
                if (iterator.getDepth() > 0) {
                    ((ViewModel) c).deinitialize();
                }
            }
            deinitialize();
            closeWindowSource.next(null);
        }
        if (this.onClosed != null) {
            this.onClosed.run();
        }
    }

    @Override
    public void setBlocked(boolean blocked) {
        this.blocked.set(blocked);
    }

    @Override
    public boolean isBlocked() {
        return blocked.get();
    }

    @Override
    public BooleanProperty blockedProperty() {
        return blocked;
    }

    @Override
    public boolean isOutOfBoundsAllowed() {
        checkIfNested();
        return outOfBoundsAllowed.get();
    }

    @Override
    public void setOutOfBoundsAllowed(boolean outOfBoundsAllowed) {
        checkIfNested();
        this.outOfBoundsAllowed.set(outOfBoundsAllowed);
    }

    @Override
    public BooleanProperty outOfBoundsAllowedProperty() {
        return outOfBoundsAllowed;
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
        return minWidth.get();
    }

    @Override
    public void setMinWidth(double minWidth) {
        this.minWidth.set(minWidth);
    }

    @Override
    public DoubleProperty minWidthProperty() {
        return minWidth;
    }

    @Override
    public double getMinHeight() {
        return minHeight.get();
    }

    @Override
    public void setMinHeight(double minHeight) {
        this.minHeight.set(minHeight);
    }

    @Override
    public DoubleProperty minHeightProperty() {
        return minHeight;
    }

    @Override
    public double getMaxWidth() {
        return maxWidth.get();
    }

    @Override
    public void setMaxWidth(double maxWidth) {
        this.maxWidth.set(maxWidth);
    }

    @Override
    public DoubleProperty maxWidthProperty() {
        return maxWidth;
    }

    @Override
    public double getMaxHeight() {
        return maxHeight.get();
    }

    @Override
    public void setMaxHeight(double maxHeight) {
        this.maxHeight.set(maxHeight);
    }

    @Override
    public DoubleProperty maxHeightProperty() {
        return maxHeight;
    }

    @Override
    public boolean isResizable() {
        return resizable.get();
    }

    @Override
    public void setResizable(boolean resizable) {
        this.resizable.set(resizable);
    }

    @Override
    public BooleanProperty resizableProperty() {
        return resizable;
    }

    @Override
    public double getX() {
        return this.x.get();
    }

    @Override
    @RequestSetter
    public void setX(double x) {
        xSource.next(x);
    }

    @Override
    public ReadOnlyDoubleProperty xProperty() {
        return x.getReadOnlyProperty();
    }

    @Override
    public double getY() {
        return this.y.get();
    }

    @Override
    @RequestSetter
    public void setY(double y) {
        ySource.next(y);
    }

    @Override
    public ReadOnlyDoubleProperty yProperty() {
        return y.getReadOnlyProperty();
    }

    @Override
    public @Nullable Density getDensity() {
        return this.density.get();
    }

    @Override
    public ReadOnlyObjectProperty<Density> densityProperty() {
        return density.getReadOnlyProperty();
    }

    @Override
    public Theme getTheme() {
        return this.theme.get();
    }

    @Override
    public ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    @Override
    public Font getRegularFont() {
        return this.regularFont.get();
    }

    @Override
    public ReadOnlyObjectProperty<Font> regularFontProperty() {
        return regularFont.getReadOnlyProperty();
    }

    @Override
    public Font getMonospaceFont() {
        return this.monospaceFont.get();
    }

    @Override
    public ReadOnlyObjectProperty<Font> monospaceFontProperty() {
        return monospaceFont.getReadOnlyProperty();
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }

    protected void onMaximize() {
        setMaximized(!maximized.get());
    }

    protected void onMinimize() {
        setMinimized(!minimized.get());
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
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

    @Override
    protected WindowHistory getHistory() {
        return (WindowHistory) super.getHistory();
    }

    @Override
    protected void restorePersistentState() {
        super.restorePersistentState();
        var h = getHistory();
        setMaximized(h.isMaximized());
        if (h.getHeight() >= 0) {
            setHeight(h.getHeight());
        }
        if (h.getWidth() >= 0) {
            setWidth(h.getWidth());
        }
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
        this.density.set(density);
    }

    protected void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    protected void setRegularFont(Font font) {
        this.regularFont.set(font);
    }

    protected void setMonospaceFont(Font font) {
        this.monospaceFont.set(font);
    }

    ObservableSource<Void> closeWindowSource() {
        return closeWindowSource;
    }

    ReadOnlyDoubleWrapper widthWrapper() {
        return width;
    }

    ObservableSource<Double> widthSource() {
        return widthSource;
    }

    ReadOnlyDoubleWrapper heightWrapper() {
        return height;
    }

    ObservableSource<Double> heightSource() {
        return heightSource;
    }

    ReadOnlyDoubleWrapper xWrapper() {
        return x;
    }

    ObservableSource<Double> xSource() {
        return xSource;
    }

    ReadOnlyDoubleWrapper yWrapper() {
        return y;
    }

    ObservableSource<Double> ySource() {
        return ySource;
    }

    ReadOnlyBooleanWrapper maximizedWrapper() {
        return maximized;
    }

    ObservableSource<Boolean> maximizedSource() {
        return maximizedSource;
    }

    ReadOnlyBooleanWrapper minimizedWrapper() {
        return minimized;
    }

    ObservableSource<Boolean> minimizedSource() {
        return minimizedSource;
    }

    BooleanProperty resizingProperty() {
        return resizing;
    }

    private void checkIfNested() {
        if (windowType != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }
}
