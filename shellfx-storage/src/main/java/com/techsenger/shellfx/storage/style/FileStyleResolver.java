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

import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.shellfx.storage.StorageFile;

/**
 * Determines the styles a file is shown with. The file provider calls the resolver while it loads the file, and a
 * client calls it to restyle a file.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
@FunctionalInterface
public interface FileStyleResolver<F extends StorageFile> {

    /**
     * Resolves the styles of the file.
     *
     * @param file the file to resolve the styles for
     * @param type whether the decoration is provisional, as the file is still to be refined, or final
     * @param theme the theme the file is shown in
     * @return the styles, never {@code null}; a style without values clears the styles of the file
     */
    IconTextStyle resolve(F file, FileDecorationType type, Theme theme);
}
