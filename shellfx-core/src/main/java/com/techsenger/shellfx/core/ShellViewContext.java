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

package com.techsenger.shellfx.core;

import com.techsenger.shellfx.core.registry.ControlRegistry;
import com.techsenger.shellfx.core.registry.SlotRegistry;

/**
 * The part of the shell context that is available to views: the shell uses its registries on the view side only.
 * The view model part is {@link ShellViewModelContext}.
 *
 * @author Pavel Castornii
 */
public interface ShellViewContext {

    /**
     * Returns the registry of the slot tree. There can be only one registry in the application.
     *
     * @return
     */
    SlotRegistry getSlotRegistry();

    /**
     * Returns the control registry. There can be only one registry in the application.
     *
     * @return
     */
    ControlRegistry getControlRegistry();
}
