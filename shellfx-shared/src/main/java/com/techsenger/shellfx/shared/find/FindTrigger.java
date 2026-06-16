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

package com.techsenger.shellfx.shared.find;

/**
 * Defines when a search operation should be triggered.
 *
 * <p>This enumeration specifies the execution policy of the search component: whether the search is performed
 * explicitly by the user (e.g. pressing Enter or clicking a search button), or automatically while the user is
 * typing.
 *
 * @author Pavel Castornii
 */
public enum FindTrigger {

    /**
     * The search is executed only after an explicit user action, such as pressing the Enter key or clicking a
     * search button.
     *
     * <p>This mode is typically used when search execution is expensive, remote, or should be performed
     * intentionally by the user.
     */
    ON_SUBMIT,

    /**
     * The search is executed automatically as the user types.
     *
     * <p>In this mode, the search is triggered on text changes, with debouncing to avoid excessive executions.
     */
    ON_TYPE
}
