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

package com.techsenger.shellfx.storage;

import com.techsenger.shellfx.material.theme.Theme;
import java.util.List;

/**
 * Gives the files the icon and the style they are shown with, which the {@link FileProvider} does not do. The client
 * decorates the files the provider has loaded, and the files the second loading phase has refined.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public interface FileDecorator<F extends StorageFile> {

    /**
     * Decorates the files on the calling thread. The icon and the style of a file that is already shown must be
     * changed on the UI thread, so call it there for such files.
     *
     * @param files the files to decorate
     * @param theme the theme the files are styled for
     */
    void decorate(List<F> files, Theme theme);

    /**
     * Decorates the file that is already shown. It can be called from any thread, and from several threads at once,
     * as the second loading phase does from its workers; the icon and the style are changed on the UI thread, where
     * the elements that show the file observe them.
     *
     * @param file the file to decorate
     * @param theme the theme the file is styled for
     */
    void decorateAsync(F file, Theme theme);
}
