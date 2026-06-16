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

import com.techsenger.shellfx.core.CloseAwarePort;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.traits.Waitable;

/**
 *
 * @author Pavel Castornii
 */
public interface PopupPort extends AreaPort, PopupShared, CloseAwarePort, Waitable {

    /**
     * Returns {@code true} if the popup blocks interaction with underlying content (modal) and {@code false} otherwise.
     *
     * @return
     */
    boolean isModal();

    double getPrefWidth();

    double getPrefHeight();
}
