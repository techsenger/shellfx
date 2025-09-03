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

import com.techsenger.tabshell.layout.LayoutComponentKeys;
import com.techsenger.tabshell.layout.tabhost.TabHostViewModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author Pavel Castornii
 */
public class TabDockViewModel extends TabHostViewModel {

    private final ReadOnlyObjectWrapper<SpaceReceiver> spaceReceiver = new ReadOnlyObjectWrapper<>();

    private final BooleanProperty draggable = new SimpleBooleanProperty(true);

    protected TabDockViewModel() {
        super(LayoutComponentKeys.TAB_DOCK);
    }

    /**
     * Returns the value of {@link #spaceReceiverProperty()}.
     *
     * @return the current {@link SpaceReceiver} of this component
     */
    public final SpaceReceiver getSpaceReceiver() {
        return spaceReceiver.get();
    }

    /**
     * The property that defines which neighboring component will receive this component's space if it is removed
     * from the layout.
     *
     * @return the {@link ReadOnlyProperty} of the {@link SpaceReceiver}
     */
    public final ReadOnlyProperty<SpaceReceiver> spaceReceiverProperty() {
        return spaceReceiver.getReadOnlyProperty();
    }

    /**
     * Sets the value of {@link #spaceReceiverProperty()}.
     *
     * @param value the new {@link SpaceReceiver} for this component
     */
    public final void setSpaceReceiver(SpaceReceiver value) {
        spaceReceiver.set(value);
    }

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
}
