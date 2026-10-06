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

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvvm.ParentView;
import com.techsenger.shellfx.material.slot.Slot;

/**
 * Base of the providers: keeps the slot and the control, which a subclass creates in {@link #initialize} and
 * gives to {@link #setControl(Object)}. A provider is one-shot: {@code initialize} and {@code deinitialize} allow
 * it to be initialized once and deinitialized once and throw {@link IllegalStateException} otherwise, so a provider
 * that overrides them calls {@code super} first.
 *
 * @param <V> the view type of the component the control is built for
 * @param <C> the type of the control
 * @author Pavel Castornii
 */
public class SimpleControlProvider<V extends ParentView<?>, C> implements ControlProvider<V, C> {

    private @Nullable Slot<V> slot;

    private @Nullable C control;

    private boolean initialized;

    private boolean deinitialized;

    /**
     * Creates a provider whose control is set later with {@link #setControl(Object)}.
     */
    public SimpleControlProvider() {
        // empty
    }

    /**
     * Creates a provider of a control that needs nothing to be hooked onto the view.
     *
     * @param control the control to be returned by {@link #getControl()}
     */
    public SimpleControlProvider(C control) {
        this.control = control;
    }

    @Override
    public C getControl() {
        if (control == null) {
            throw new IllegalStateException("Control of the provider " + getClass().getSimpleName() + " is not set");
        }
        return control;
    }

    @Override
    public Slot<V> getSlot() {
        if (slot == null) {
            throw new IllegalStateException("Slot of the provider " + getClass().getSimpleName() + " is not set");
        }
        return slot;
    }

    @Override
    public void setSlot(Slot<V> slot) {
        this.slot = slot;
    }

    @Override
    public void initialize(V view) {
        if (initialized) {
            throw new IllegalStateException("The provider " + getClass().getSimpleName()
                    + " has already been initialized");
        }
        initialized = true;
    }

    @Override
    public void deinitialize(V view) {
        if (deinitialized) {
            throw new IllegalStateException("The provider " + getClass().getSimpleName()
                    + " has already been deinitialized");
        }
        deinitialized = true;
    }

    /**
     * Sets the control of this provider.
     *
     * @param control the control to be returned by {@link #getControl()}
     */
    protected void setControl(C control) {
        this.control = control;
    }
}
