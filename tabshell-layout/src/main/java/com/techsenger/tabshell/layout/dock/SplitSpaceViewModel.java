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
import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.layout.LayoutComponentNames;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceViewModel extends AbstractPaneViewModel {

    private final ObjectProperty<UUID> uuid = new SimpleObjectProperty();

    private final Orientation orientation;

    private final ObservableList<Double> dividerPositions = FXCollections.observableArrayList();

    protected SplitSpaceViewModel(Orientation orientation) {
        this.orientation = orientation;
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
     * @return the {@link ObjectProperty} of the {@link UUID}
     */
    public final ObjectProperty<UUID> uuidProperty() {
        return uuid;
    }

    /**
     * Returns the orientation of the component..
     *
     * @return the current {@link Orientation} of this component
     */
    public final Orientation getOrientation() {
        return orientation;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(LayoutComponentNames.SPLIT_SPACE);
    }

    /**
     * Sets the value of {@link #uuidProperty()}.
     *
     * @param value the new {@link UUID} for this component
     */
    protected final void setUuid(UUID value) {
        uuid.set(value);
    }

    protected ObservableList<Double> getDividerPositions() {
        return dividerPositions;
    }
}
