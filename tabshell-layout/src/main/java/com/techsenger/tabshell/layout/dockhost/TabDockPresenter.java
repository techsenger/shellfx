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

package com.techsenger.tabshell.layout.dockhost;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseAwarePresenter;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.layout.LayoutComponents;
import com.techsenger.tabshell.layout.tabhost.TabHostPresenter;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockPresenter<V extends TabDockView> extends TabHostPresenter<V>
        implements TabDockPort, CloseAwarePresenter<V>  {

    private ComponentPosition minimizedPosition;

    private TabDockState state = TabDockState.DETACHED;

    private TabDockTransitionState transitionState;

    private boolean draggable;

    private boolean minimizable;

    private boolean closable;

    public TabDockPresenter(V view) {
        super(view);
    }

    @Override
    public ComponentPosition getMinimizedPosition() {
        return this.minimizedPosition;
    }

    @Override
    public TabDockState getState() {
        return state;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
        getView().setDraggable(draggable);
    }

    public boolean isMinimizable() {
        return minimizable;
    }

    public void setMinimizable(boolean minimizable) {
        this.minimizable = minimizable;
        getView().setMinimizable(minimizable);
    }

    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
        getView().setClosable(closable);
    }

    @Override
    public void close() {
        getView().getComposer().remove();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void setMinimizedPosition(ComponentPosition position) {
        this.minimizedPosition = position;
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponents.TAB_DOCK);
    }

    protected void onMinimize() {
        this.transitionState = TabDockTransitionState.TO_MINIMIZED;
    }

    protected void onMinimized() {
        updateState(TabDockState.MINIMIZED);
    }

    TabDockTransitionState getTransitionState() {
        return transitionState;
    }

    private void updateState(TabDockState state) {
        this.state = state;
        transitionState = null;
    }
}
