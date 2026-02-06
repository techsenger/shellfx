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

import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.material.table.TableHistory;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FileChooserDialogView extends DialogView {

    void setAppearanceSettings(AppearanceSettings settings);

    void setLocationCaption(String value);

    String getLocationCaption();

    List<Location> getLocations();

    void setLocations(List<Location> locations);

    Location getLocation();

    void setLocation(Location value);

    boolean isListSelected();

    void setListSelected(boolean value);

    boolean isDetailsSelected();

    void setDetailsSelected(boolean value);

    void setTableHistory(TableHistory history);

    TableHistory getTableHistory();

    List<GenericFile> getFiles();

    void setFiles(List<GenericFile> files);

    void addFile(int index, GenericFile file);

    void removeFile(int index);

    void selectFile(int index);

    void scrollToFile(int index);

    GenericFile getSelectedFile();

    void editFile(int index);

    void sortFiles();

    void setFileName(String fileName);

    String getFileName();

    List<ExtensionFilter> getExtensionFilters();

    void setExtensionFilters(List<ExtensionFilter> filters);

    ExtensionFilter getExtensionFilter();

    void setExtensionFilter(ExtensionFilter filter);

    void setupFor(FileChooserType type);
}
