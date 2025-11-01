/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import com.techsenger.tabshell.layout.tabhost.TabHostViewModel;
import java.util.UUID;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockViewModel extends TabHostViewModel {

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);

    private final ReadOnlyObjectWrapper<UUID> uuid = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<ComponentPosition> minimizedPosition = new ReadOnlyObjectWrapper();

    /**
     * Returns the value of {@link #draggableProperty()}.
     *
     * @return the current draggable state of this component
     */
    public final boolean isDraggable() {
        return draggable.get();
    }

    /**
     * The property that defines whether this component can be dragged by the user.
     * When {@code true}, the component can be dragged and docked to different locations.
     * When {@code false}, the component remains fixed in its current position.
     *
     * @return the {@link BooleanProperty} representing the draggable state
     */
    public final BooleanProperty draggableProperty() {
        return draggable;
    }

    /**
     * Sets the value of {@link #draggableProperty()}.
     *
     * @param value the new draggable state for this component
     */
    public final void setDraggable(boolean value) {
        draggable.set(value);
    }

    /**
     * Returns the value of {@link #uuidProperty()}.
     *
     * @return the current {@link UUID} of this component
     */
    public final UUID getUuid() {
        return uuid.get();
    }

    /**
     * The property that defines the unique identifier of this component.
     *
     * @return the {@link ReadOnlyObjectProperty} of the {@link UUID}
     */
    public final ReadOnlyObjectProperty<UUID> uuidProperty() {
        return uuid.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #minimizedPositionProperty()}.
     *
     * @return the original {@link ComponentPosition} of this component before it was minimized to the SideBar,
     *         or {@code null} if the component is not minimized.
     */
    public final ComponentPosition getMinimizedPosition() {
        return minimizedPosition.get();
    }

    /**
     * The property that stores the original position of this component in the layout before it was minimized
     * to the SideBar. This position is used to restore the component to its original place when expanded
     * from the SideBar.
     *
     * @return the {@link ReadOnlyProperty} of the {@link ComponentPosition}
     */
    public final ReadOnlyProperty<ComponentPosition> minimizedPositionProperty() {
        return minimizedPosition.getReadOnlyProperty();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.TAB_DOCK);
    }

    /**
     * Sets the value of {@link #uuidProperty()}.
     *
     * @param value the new {@link UUID} for this component
     */
    protected final void setUuid(UUID value) {
        uuid.set(value);
    }

    /**
    * Sets the value of {@link #minimizedPositionProperty()}.
    *
    * @param position the original {@link ComponentPosition} of the component before minimization
    */
    protected final void setMinimizedPosition(ComponentPosition position) {
        minimizedPosition.set(position);
    }
}
