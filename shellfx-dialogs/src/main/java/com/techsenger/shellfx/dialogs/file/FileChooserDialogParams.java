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

package com.techsenger.shellfx.dialogs.file;

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.StorageFile;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogParams<T extends StorageFile> extends DialogParams {

    private final FileChooserType chooserType;

    private final List<? extends FileStorage<T>> storages;

    private URI initialDirectory;

    private String initialFileName;

    public FileChooserDialogParams(FileChooserDialogConfig config, WindowType windowType, AppearanceSettings settings,
            FileChooserType chooserType, List<? extends FileStorage<T>> storages) {
        super(config, windowType, settings);
        this.chooserType = chooserType;
        this.storages = storages;
    }

    public FileChooserType getChooserType() {
        return chooserType;
    }

    public List<? extends FileStorage<T>> getStorages() {
        return storages;
    }

    public URI getInitialDirectory() {
        return initialDirectory;
    }

    public void setInitialDirectory(URI initialDirectory) {
        this.initialDirectory = initialDirectory;
    }

    public String getInitialFileName() {
        return initialFileName;
    }

    public void setInitialFileName(String initialFileName) {
        this.initialFileName = initialFileName;
    }

    @Override
    public FileChooserDialogConfig getConfig() {
        return (FileChooserDialogConfig) super.getConfig();
    }

    @Override
    public void validate() {
        super.validate();
        Objects.requireNonNull(getConfig());
        Objects.requireNonNull(chooserType);
        Objects.requireNonNull(storages);
    }
}
