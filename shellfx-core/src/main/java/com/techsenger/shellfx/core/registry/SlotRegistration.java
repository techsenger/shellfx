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

import com.techsenger.shellfx.material.slot.Slot;

/**
 * Puts a slot into another one at a position among its siblings.
 *
 * @author Pavel Castornii
 */
final class SlotRegistration extends AbstractRegistration {

    private final Slot<?> parent;

    private final int position;

    private final Slot<?> child;

    SlotRegistration(Slot<?> parent, int position, Slot<?> child) {
        this.parent = parent;
        this.position = position;
        this.child = child;
    }

    Slot<?> getParent() {
        return parent;
    }

    int getPosition() {
        return position;
    }

    Slot<?> getChild() {
        return child;
    }
}
