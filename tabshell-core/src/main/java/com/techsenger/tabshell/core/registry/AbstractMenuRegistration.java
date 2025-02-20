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

package com.techsenger.tabshell.core.registry;

/**
 *
 * @author Pavel Castornii
 */
abstract class AbstractMenuRegistration<T> implements ControlRegistry.Registration {

    private final MenuElementType type;

    private final int position;

    private final ControlFactory<T> factory;

    private Runnable unregister;

    AbstractMenuRegistration(MenuElementType type, ControlFactory<T> factory, int position) {
        this.type = type;
        this.position = position;
        this.factory = factory;
    }

    @Override
    public void unregister() {
        unregister.run();
    }

    public MenuElementType getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public ControlFactory<T> getFactory() {
        return factory;
    }

    public void setUnregister(Runnable unregister) {
        this.unregister = unregister;
    }
}
