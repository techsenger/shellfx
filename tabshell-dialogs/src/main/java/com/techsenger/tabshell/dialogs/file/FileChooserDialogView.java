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

import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.material.table.TableColumnInfo;
import com.techsenger.tabshell.material.table.TableColumnName;
import com.techsenger.tabshell.storage.GenericFile;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public interface FileChooserDialogView extends DialogView {

    interface Composer extends DialogView.Composer {

        DialogPort addAlertDialog(AlertDialogType type, String message);
    }

    @Override
    Composer getComposer();

    void setAppearanceSettings(AppearanceSettings settings);

    void setLocationCaption(String value);

    void setLocations(List<Location> locations);

    void setLocation(Location value);

    void setListSelected(boolean value);

    void setDetailsSelected(boolean value);

    void addColumns(Map<TableColumnName, TableColumnInfo> infosByName);

    void setFiles(List<GenericFile> files);

    void addFile(int index, GenericFile file);

    void removeFile(int index);

    void selectFile(int index);

    void scrollToFile(int index);

    void editFile(int index);

    Comparator<GenericFile> getFileComparator();

    void setFileName(String fileName);

    void setExtensionFilters(List<ExtensionFilter> filters);

    void setExtensionFilter(ExtensionFilter filter);
}
