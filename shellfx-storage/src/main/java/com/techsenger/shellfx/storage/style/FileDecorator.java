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
 * Gives the files the icon and the style they are shown with, which the file provider does not do. The client
 * decorates the files the provider has loaded in the background, before they are shown, and the files the second
 * phase has refined on the UI thread, when their refinements are applied.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public interface FileDecorator<F extends StorageFile> {

    /**
     * Decorates the file on the calling thread. The icon and the style of a file that is already shown must be changed
     * on the UI thread, so call it there for such a file.
     *
     * @param file the file to decorate
     * @param type whether the decoration is provisional, as the file is still to be refined, or final
     * @param theme the theme the file is styled for
     */
    void decorate(F file, FileDecorationType type, Theme theme);
}
