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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.dialog.DialogParams;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.storage.FileStorage;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class FileChooserDialogParams extends DialogParams {

    private final FileChooserType type;

    private final List<FileStorage> storages;

    private final AppearanceSettings settings;

    private final HistoryManager historyManager;

    private URI initialDirectory;

    private String initialFileName;

    public FileChooserDialogParams(FileChooserType type, List<FileStorage> storages, AppearanceSettings settings,
            HistoryManager historyManager) {
        this.type = type;
        this.storages = storages;
        this.settings = settings;
        this.historyManager = historyManager;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(FileChooserDialogHistory.class,
                FileChooserDialogHistory::new));
    }

    public FileChooserType getType() {
        return type;
    }

    public List<FileStorage> getStorages() {
        return storages;
    }

    public AppearanceSettings getSettings() {
        return settings;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
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
    protected void validate() {
        super.validate();
        Objects.requireNonNull(type);
        Objects.requireNonNull(storages);
        Objects.requireNonNull(settings);
        Objects.requireNonNull(historyManager);
    }
}
