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

package com.techsenger.shellfx.core.tab;

import com.techsenger.patternfx.mvvm.AbstractChildViewModel;
import com.techsenger.shellfx.core.ShellContext;
import com.techsenger.shellfx.material.icon.Icon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTabViewModel<C extends TabComposer> extends AbstractChildViewModel<C>
        implements TabViewModel<C> {

    private final BooleanProperty closable = new SimpleBooleanProperty(true);

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    private final BooleanProperty waiting = new SimpleBooleanProperty();

    private final StringProperty tooltip = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    private final StringProperty title = new SimpleStringProperty();

    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper();

    public AbstractTabViewModel(TabParams params) {
        super(params);
    }

    @Override
    public void close() {
        getComposer().close();
        if (this.onClosed != null) {
            this.onClosed.run();
        }
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
    public boolean isSelected() {
        return this.selected.get();
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    @Override
    public ReadOnlyBooleanProperty selectedProperty() {
        return this.selected.getReadOnlyProperty();
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
    public Runnable getOnCloseRequest() {
        return this.onCloseRequest;
    }

    @Override
    public void setOnCloseRequest(Runnable runnable) {
        this.onCloseRequest = runnable;
    }

    @Override
    public boolean isWaiting() {
        return waiting.get();
    }

    @Override
    public void setWaiting(boolean waiting) {
        this.waiting.set(waiting);
    }

    @Override
    public BooleanProperty waitingProperty() {
        return waiting;
    }

    @Override
    public String getTooltip() {
        return tooltip.get();
    }

    @Override
    public void setTooltip(String tooltip) {
        this.tooltip.set(tooltip);
    }

    @Override
    public StringProperty tooltipProperty() {
        return tooltip;
    }

    @Override
    public String getTitle() {
        return this.title.get();
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
        return this.icon.get();
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
    public TabPort.ComposerAccess getComposerAccess() {
        return getComposer();
    }

    @Override
    protected TabHistory getHistory() {
        return (TabHistory) super.getHistory();
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }

    /**
     * Convenience method that returns the current shell context.
     *
     * @return the current {@link ShellContext}
     */
    protected ShellContext getShellContext() {
        return getComposer().getShellPort().getContext();
    }

    /**
     * Convenience method that returns the current shell context cast to the specified type.
     *
     * @param clazz the expected context type
     * @param <T> the context type
     * @return the current shell context as the specified type
     */
    protected <T extends ShellContext> T getShellContext(Class<T> clazz) {
        return getComposer().getShellPort().getContext(clazz);
    }
}
