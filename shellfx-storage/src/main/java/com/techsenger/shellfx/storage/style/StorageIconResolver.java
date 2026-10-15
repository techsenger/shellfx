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

package com.techsenger.shellfx.storage.style;

import com.techsenger.shellfx.material.icon.FontIcon;

/**
 * Determines the icon of a storage in its current state. The resolver is given to the storage when it is created,
 * by the one who knows the kinds and the states of the storage.
 *
 * @author Pavel Castornii
 */
@FunctionalInterface
public interface StorageIconResolver {

    /**
     * Resolves the icon of the storage.
     *
     * @return the icon, never {@code null}
     */
    FontIcon<?> resolve();
}
