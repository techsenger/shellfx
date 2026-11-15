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

package com.techsenger.shellfx.storage.base;

import com.techsenger.shellfx.base.icon.FontIcon;

/**
 * Gives the icon of a storage. A storage is loaded dynamically, so only it knows what it looks like; the way the icon
 * is colored and shown is up to the view, which knows nothing of the kinds of storages.
 *
 * @author Pavel Castornii
 */
public interface StorageIconProvider {

    /**
     * Returns the icon of the storage that does not depend on its state.
     *
     * @return the icon, never {@code null}
     */
    FontIcon<?> getBaseIcon();
}
