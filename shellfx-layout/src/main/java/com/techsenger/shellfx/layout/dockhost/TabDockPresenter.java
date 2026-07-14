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

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseAwarePresenter;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.layout.LayoutComponents;
import com.techsenger.shellfx.layout.tabhost.TabHostPresenter;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockPresenter<V extends TabDockView> extends TabHostPresenter<V>
        implements TabDockPort, CloseAwarePresenter<V>  {

    private MinimizedPosition minimizedPosition;

    private TabDockState state = TabDockState.DETACHED;

    private TabDockTransitionState transitionState;

    private boolean draggable;

    private boolean minimizable;

    private boolean closable;

    private Runnable onCloseRequest = () -> closeSafely();

    private Runnable onClosed;

    public TabDockPresenter(V view, AreaParams params) {
        super(view, params);
    }

    @Override
    public MinimizedPosition getMinimizedPosition() {
        return this.minimizedPosition;
    }

    @Override
    public TabDockState getState() {
        return state;
    }

    @Override
    public boolean isDraggable() {
        return draggable;
    }

    @Override
    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        getView().setDraggable(draggable);
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
    public Runnable getOnCloseRequest() {
        return this.onCloseRequest;
    }

    @Override
    public void setOnCloseRequest(Runnable runnable) {
        this.onCloseRequest = runnable;
    }

    @Override
    public void close() {
        getView().getComposer().close();
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

    protected void setMinimizedPosition(MinimizedPosition position) {
        this.minimizedPosition = position;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponents.TAB_DOCK);
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

    private void updateState(TabDockState state) {
        this.state = state;
        transitionState = null;
    }
}
