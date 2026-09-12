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

/**
 * Contract for components that can be closed immediately, without any readiness checks or preparation steps.
 *
 * @author Pavel Castornii
 */
public interface ForceClosePort extends CloseCallbackPort {

    /**
     * Closes this component immediately using a force close strategy.
     *
     * <p>This method immediately deinitializes the component and all its descendants and removes this component
     * from the component tree, without performing any readiness checks or preparation steps. This method should
     * only be used in exceptional situations where a forced shutdown is required.
     */
    void close();
}
