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
 * Owns the control one registration puts into a slot for one component view. A builder creates a provider per
 * control, initializes it, takes its control and, when the controls are no longer needed, the owner of the controls
 * deinitializes it - this is where a provider unhooks the control from the long-living state it was bound to.
 *
 * <p>A provider is a one-shot object: it is initialized once and deinitialized once, and neither can be repeated.
 * A provider and its control are never reused; a new provider is created for every build.
 *
 * @param <V> the view type of the component the control is built for
 * @param <C> the type of the control
 * @author Pavel Castornii
 */
public interface ControlProvider<V extends ParentView<?>, C> {

    /**
     * Returns the slot this provider was registered for: the slot of the control itself for a menu bar, a menu,
     * a tool bar or a group, and the slot of the group for a control put into a group.
     *
     * @throws IllegalStateException if a builder has not put the provider into a slot yet
     */
    Slot<V> getSlot();

    /**
     * Puts the provider into a slot. It is called by the registry when it creates the provider; user code has no
     * reason to call it.
     *
     * @param slot the slot the provider was registered for
     */
    void setSlot(Slot<V> slot);

    /**
     * Creates the control, if it is not created yet, and hooks it onto the view. Called once, by the builder.
     *
     * @param view the view of the component the control is built for
     */
    void initialize(V view);

    /**
     * Unhooks the control from the view and from everything else {@link #initialize(ParentView)} bound it to. Called
     * once, by the owner of the controls, after {@link #initialize(ParentView)}.
     *
     * @param view the view of the component the control was built for
     */
    void deinitialize(V view);

    /**
     * Returns the control of this provider.
     *
     * @throws IllegalStateException if the control has not been created yet
     */
    C getControl();
}
