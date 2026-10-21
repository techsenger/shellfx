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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.async.FileProvider;
import com.techsenger.shellfx.storage.async.FileRefiner;
import com.techsenger.shellfx.storage.async.TwoPhaseSortPolicy;
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

    private @Nullable FileProvider<T> fileProvider;

    private @Nullable FileRefiner<T> fileRefiner;

    private TwoPhaseSortPolicy twoPhaseSortPolicy = TwoPhaseSortPolicy.ON_REQUEST;

    private URI initialDirectory;

    private String initialFileName;

    /**
     * Creates params.
     *
     * @param config the config of the dialog
     * @param windowType the type of the window
     * @param settings the appearance settings
     * @param chooserType the type of the chooser
     * @param storages the storages the user can choose files from
     */
    public FileChooserDialogParams(FileChooserDialogConfig config, WindowType windowType, AppearanceSettings settings,
            FileChooserType chooserType, List<? extends FileStorage<T>> storages) {
        super(config, windowType, settings);
        this.chooserType = chooserType;
        this.storages = List.copyOf(storages);
    }

    public FileChooserType getChooserType() {
        return chooserType;
    }

    public List<? extends FileStorage<T>> getStorages() {
        return storages;
    }

    public @Nullable FileProvider<T> getFileProvider() {
        return fileProvider;
    }

    /**
     * Sets the provider that loads the files, for all the lists the dialog shows. It can't be changed once the dialog
     * is opened.
     *
     * @param fileProvider the provider, or {@code null} for the default one over the storages
     */
    public void setFileProvider(@Nullable FileProvider<T> fileProvider) {
        this.fileProvider = fileProvider;
    }

    public @Nullable FileRefiner<T> getFileRefiner() {
        return fileRefiner;
    }

    /**
     * Sets the refiner that does the second phase for the files that need it. It can't be changed once the dialog is
     * opened.
     *
     * @param fileRefiner the refiner, or {@code null} for the default one over the storages
     */
    public void setFileRefiner(@Nullable FileRefiner<T> fileRefiner) {
        this.fileRefiner = fileRefiner;
    }

    public TwoPhaseSortPolicy getTwoPhaseSortPolicy() {
        return twoPhaseSortPolicy;
    }

    /**
     * Sets when the files are sorted automatically if a second phase refines them, which only some files have. It
     * can't be changed once the dialog is opened.
     *
     * @param twoPhaseSortPolicy the policy, {@code ON_REQUEST} by default
     */
    public void setTwoPhaseSortPolicy(TwoPhaseSortPolicy twoPhaseSortPolicy) {
        this.twoPhaseSortPolicy = twoPhaseSortPolicy;
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
        Objects.requireNonNull(twoPhaseSortPolicy);
    }
}
