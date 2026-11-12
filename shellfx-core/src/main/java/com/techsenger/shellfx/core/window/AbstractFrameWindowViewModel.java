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

import com.techsenger.shellfx.core.config.ConfigUtils;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.icon.Icon;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The base view model of a window with a title bar: it adds the title, the icon, the maximized and minimized states,
 * the closable and resizable flags and the blocking of the window.
 *
 * @param <C> the composer of the window
 * @author Pavel Castornii
 */
public abstract class AbstractFrameWindowViewModel<C extends WindowComposer> extends AbstractWindowViewModel<C>
        implements FrameWindowViewModel<C> {

    private final BooleanProperty resizable = new SimpleBooleanProperty(true);

    private final BooleanProperty outOfBoundsAllowed = new SimpleBooleanProperty();

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

    private final BooleanProperty resizing = new SimpleBooleanProperty();

    public AbstractFrameWindowViewModel(FrameWindowParams params) {
        super(params);
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
    public boolean isBlocked() {
        return blocked.get();
    }

    @Override
    public void setBlocked(boolean blocked) {
        this.blocked.set(blocked);
    }

    @Override
    public BooleanProperty blockedProperty() {
        return blocked;
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

    protected void onMaximize() {
        setMaximized(!maximized.get());
    }

    protected void onMinimize() {
        setMinimized(!minimized.get());
    }

    @Override
    protected void loadStateFromConfig() {
        var config = Objects.requireNonNull(getConfig());
        setMaximized(config.isMaximized());
        super.loadStateFromConfig();
    }

    @Override
    protected void observeStateForConfig() {
        super.observeStateForConfig();
        ConfigUtils.observe(this.maximized, Objects.requireNonNull(getConfig()), WindowConfig::setMaximized);
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
        if (getWindowType() != WindowType.NESTED) {
            throw new UnsupportedOperationException("The operation is not supported for " + WindowType.TOP_LEVEL
                    + " Window");
        }
    }
}
