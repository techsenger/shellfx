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

package com.techsenger.tabshell.core.popup;

import com.techsenger.tabshell.core.area.AreaView;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupView extends AreaView {

    double getPrefWidth();

    void setPrefWidth(double value);

    double getPrefHeight();

    void setPrefHeight(double value);

    /**
     * Enables or disables the waiting state of the dialog.
     * <p>
     * When the waiting state is enabled, user interaction is temporarily disabled
     * and the dialog indicates that a background operation is in progress.
     *
     * @param waiting {@code true} to enable the waiting state, {@code false} to restore normal interaction
     */
    void setWaiting(boolean waiting);

    /**
     * Returns whether the dialog is currently in the waiting state.
     *
     * @return {@code true} if the dialog is waiting and user interaction is disabled,
     *         {@code false} otherwise
     */
    boolean isWaiting();
}
