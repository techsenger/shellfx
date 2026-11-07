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

package com.techsenger.shellfx.core.popup;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.material.Anchors;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupManager {

    /**
     * Adds the popup to the container. This is the first phase: the popup takes part in z-order, focus and modality
     * only while it is visible, which is the second phase.
     *
     * @param view the popup to add
     * @param anchors the position of the popup in the container
     */
    void addPopup(PopupView<?> view, Anchors anchors);

    /**
     * Removes the popup, whether it is visible or not.
     *
     * @param view the popup to remove
     */
    void removePopup(PopupView<?> view);

    /**
     * Returns an unmodifiable list of popups.
     *
     * @return the list of popups, invisible ones included. If no popups are present, an empty list is returned.
     */
    @Unmodifiable ObservableList<PopupView<?>> getPopups();

    /**
     * Returns whether the container that owns this manager is blocked because a modal component is shown.
     *
     * @return true if the container is blocked by a modal component, false otherwise
     */
    boolean isContainerBlocked();
}
