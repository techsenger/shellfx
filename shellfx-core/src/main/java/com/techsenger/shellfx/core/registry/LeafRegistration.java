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

package com.techsenger.shellfx.core.registry;

import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.slot.Slot;

/**
 * Puts the control a provider creates (a menu item, a button) into a group slot at a position among its siblings.
 *
 * @author Pavel Castornii
 */
final class LeafRegistration extends AbstractControlRegistration {

    private final int position;

    LeafRegistration(Slot<?> group, int position,  ControlProviderFactory<? extends ParentView<?>, ?> factory) {
        super(group, factory);
        this.position = position;
    }

    int getPosition() {
        return position;
    }
}
