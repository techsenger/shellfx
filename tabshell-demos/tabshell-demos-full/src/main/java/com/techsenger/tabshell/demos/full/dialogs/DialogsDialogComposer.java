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

package com.techsenger.tabshell.demos.full.dialogs;

import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.dialogs.alert.AlertDialogType;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogPort;
import com.techsenger.tabshell.dialogs.file.FileChooserType;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPort;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogsDialogComposer extends DialogComposer {

    DialogPort addAlertDialog(AlertDialogType type, String message);

    NameValueDialogPort addNameValueDialog();

    FileChooserDialogPort addFileChooserDialog(FileChooserType type, AppearanceSettings settings,
            HistoryManager manager);

    DialogPort addPagedDialog(HistoryManager manager);
}
