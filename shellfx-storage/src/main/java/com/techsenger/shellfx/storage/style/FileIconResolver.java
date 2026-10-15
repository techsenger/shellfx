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
import com.techsenger.shellfx.storage.StorageFile;

/**
 * Determines the icon of a file in any of its states: a state icon while the file is not ready or has failed, its
 * own icon otherwise. An application that knows more kinds of files gives its own resolver to the file provider; such
 * a resolver can extend {@link DefaultFileIconResolver} and give the default icon for the rest.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
@FunctionalInterface
public interface FileIconResolver<F extends StorageFile> {

    /**
     * Resolves the icon of the file.
     *
     * @param file the file to resolve the icon for
     * @return the icon, never {@code null}
     */
    FontIcon<?> resolve(F file);
}
