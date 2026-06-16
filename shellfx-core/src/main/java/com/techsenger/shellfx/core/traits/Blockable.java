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

package com.techsenger.shellfx.core.traits;

/**
 * Defines a component that can be blocked from receiving user input. When blocked, the component remains visually
 * unchanged but does not respond to mouse or keyboard events.
 *
 * @author Pavel Castornii
 */
public interface Blockable {

    /**
     * Sets the blocked state of this component.
     *
     * @param blocked {@code true} to block user input, {@code false} to unblock
     */
    void setBlocked(boolean blocked);

    /**
     * Returns whether this component is currently blocked.
     *
     * @return {@code true} if this component is blocked, {@code false} otherwise
     */
    boolean isBlocked();
}
