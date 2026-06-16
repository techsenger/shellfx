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
import com.techsenger.shellfx.material.table.TableColumnName;
import com.techsenger.shellfx.storage.GenericFile;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public interface FileChooserDialogView extends DialogView, FileChooserDialogShared {

    interface Composer extends DialogView.Composer {

        DialogPort addAlertDialog(AlertDialogParams params, String message);
    }

    @Override
    Composer getComposer();

    void setAppearanceSettings(AppearanceSettings settings);

    void addColumns(Map<TableColumnName, TableColumnInfo> infosByName);

    void addFile(int index, GenericFile file);

    void removeFile(int index);

    void selectFile(int index);

    void scrollToFile(int index);

    void editFile(int index);
}
