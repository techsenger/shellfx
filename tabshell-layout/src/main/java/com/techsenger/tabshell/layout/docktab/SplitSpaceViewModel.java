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

package com.techsenger.tabshell.layout.docktab;

import com.techsenger.tabshell.core.pane.AbstractPaneViewModel;
import com.techsenger.tabshell.core.pane.PaneKey;
import com.techsenger.tabshell.layout.LayoutComponentKeys;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author Pavel Castornii
 */
public class SplitSpaceViewModel extends AbstractPaneViewModel {

    private final ReadOnlyObjectWrapper<SpaceReceiver> spaceReceiver = new ReadOnlyObjectWrapper<>();

    protected SplitSpaceViewModel() {

    }

    @Override
    public PaneKey getKey() {
        return LayoutComponentKeys.SPLIT_SPACE;
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

}
