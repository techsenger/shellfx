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
import com.techsenger.shellfx.storage.FileProviderFactory;
import com.techsenger.shellfx.storage.RefinedFieldSortPolicy;
import com.techsenger.shellfx.storage.StorageFile;
import java.net.URI;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogParams<T extends StorageFile> extends DialogParams {

    private final FileChooserType chooserType;

    private final FileProviderFactory<T> fileProviderFactory;

    private RefinedFieldSortPolicy refinedFieldSortPolicy = RefinedFieldSortPolicy.ON_REQUEST;

    private URI initialDirectory;

    private String initialFileName;

    /**
     * Creates params.
     *
     * @param config the config of the dialog
     * @param windowType the type of the window
     * @param settings the appearance settings
     * @param chooserType the type of the chooser
     * @param fileProviderFactory the factory of the providers of the files, which also give the storages the user
     *     can choose files from; the dialog asks it for a provider of its own for each list it shows
     */
    public FileChooserDialogParams(FileChooserDialogConfig config, WindowType windowType, AppearanceSettings settings,
            FileChooserType chooserType, FileProviderFactory<T> fileProviderFactory) {
        super(config, windowType, settings);
        this.chooserType = chooserType;
        this.fileProviderFactory = fileProviderFactory;
    }

    public FileChooserType getChooserType() {
        return chooserType;
    }

    public FileProviderFactory<T> getFileProviderFactory() {
        return fileProviderFactory;
    }

    public RefinedFieldSortPolicy getRefinedFieldSortPolicy() {
        return refinedFieldSortPolicy;
    }

    /**
     * Sets when the files are sorted automatically if a second phase refines them, which only the files of a network
     * storage have. It can't be changed once the dialog is opened.
     *
     * @param refinedFieldSortPolicy the policy, {@code ON_REQUEST} by default
     */
    public void setRefinedFieldSortPolicy(RefinedFieldSortPolicy refinedFieldSortPolicy) {
        this.refinedFieldSortPolicy = refinedFieldSortPolicy;
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
        Objects.requireNonNull(fileProviderFactory);
        Objects.requireNonNull(refinedFieldSortPolicy);
    }
}
