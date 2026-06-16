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
 * Represents the result of a component readiness check when it is queried whether it can be closed.
 *
 * @author Pavel Castornii
 */
public enum CloseCheckResult {

    /**
     * The component is ready to be closed immediately without any preparation.
     */
    READY,

    /**
     * The component is not immediately ready to close and requires some preparation steps, such as saving data,
     * stopping background tasks, or cleaning up temporary resources. Once the preparation is complete, the component
     * can be safely closed.
     */
    PREPARATION_REQUIRED,

    /**
     * Component is not ready to be closed. Closing is disallowed.
     */
    NOT_READY
}
