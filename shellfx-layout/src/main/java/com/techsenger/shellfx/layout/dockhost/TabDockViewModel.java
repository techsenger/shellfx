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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.shellfx.core.CloseAwareViewModel;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.layout.tabhost.TabHostViewModel;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockViewModel<C extends TabDockComposer> extends TabHostViewModel<C>
        implements FullTabDockPort, CloseAwareViewModel<C> {

    private final BooleanProperty draggable = new SimpleBooleanProperty();

    private final BooleanProperty minimizable = new SimpleBooleanProperty();

    private final BooleanProperty closable = new SimpleBooleanProperty();

    private final ReadOnlyObjectWrapper<MinimizedPosition> minimizedPosition = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<TabDockState> state = new ReadOnlyObjectWrapper<>(TabDockState.DETACHED);

    private TabDockTransitionState transitionState;

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    public TabDockViewModel(AreaParams params) {
        super(params);
    }

    @Override
    public MinimizedPosition getMinimizedPosition() {
        return this.minimizedPosition.get();
    }

    @Override
    public ReadOnlyObjectProperty<MinimizedPosition> minimizedPositionProperty() {
        return minimizedPosition.getReadOnlyProperty();
    }

    @Override
    public TabDockState getState() {
        return state.get();
    }

    @Override
    public ReadOnlyObjectProperty<TabDockState> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @Override
    public boolean isDraggable() {
        return draggable.get();
    }

    @Override
    public void setDraggable(boolean draggable) {
        this.draggable.set(draggable);
    }

    @Override
    public BooleanProperty draggableProperty() {
        return draggable;
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
        getComposer().close();
        if (this.onClosed != null) {
            this.onClosed.run();
        }
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
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void onMinimize() {
        this.transitionState = TabDockTransitionState.TO_MINIMIZED;
    }

    protected void onMinimized() {
        updateState(TabDockState.MINIMIZED);
    }

    protected void onCloseRequest() {
        if (this.onCloseRequest != null) {
            this.onCloseRequest.run();
        }
    }

    TabDockTransitionState getTransitionState() {
        return transitionState;
    }

    /**
     * Framework contract: written directly by the View to report the actual position this component was minimized
     * from, intended exclusively for {@code DockHostFxView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyObjectWrapper<MinimizedPosition> minimizedPositionWrapper() {
        return minimizedPosition;
    }

    private void updateState(TabDockState newState) {
        this.state.set(newState);
        transitionState = null;
    }
}
