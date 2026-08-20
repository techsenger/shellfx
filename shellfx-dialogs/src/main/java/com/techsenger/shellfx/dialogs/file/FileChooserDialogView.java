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

import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.material.table.TableColumnInfo;
import com.techsenger.shellfx.storage.GenericFile;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FileChooserDialogView<T extends GenericFile> extends DialogView {

    interface Composer extends DialogView.Composer {

        DialogPort addAlertDialog(AlertDialogParams params, String message);
    }

    @Override
    Composer getComposer();

    void updateAppearanceSettings(AppearanceSettings settings);

    void addColumns(Collection<TableColumnInfo> infos);

    void updateFiles(List<T> files);

    void addFile(int index, T file);

    void removeFile(int index);

    void selectFile(int index);

    void scrollToFile(int index);

    void editFile(int index);

    void updateLocationCaption(String value);

    void updateLocations(List<Location> locations);

    void updateLocation(Location value);

    void updateMode(Mode mode);

    void updateFileName(String fileName);

    void updateExtensionFilters(List<ExtensionFilter> filters);

    void updateExtensionFilter(ExtensionFilter filter);
}
