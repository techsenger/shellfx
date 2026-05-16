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

package com.techsenger.tabshell.demo.dialogs;

import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.demo.page.PageDialogParams;
import com.techsenger.tabshell.dialogs.alert.AlertDialogParams;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogParams;
import com.techsenger.tabshell.dialogs.file.FileChooserDialogPort;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPort;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogsDialogView extends DialogView {

    interface Composer extends DialogView.Composer {

        DialogPort addAlertDialog(AlertDialogParams params, String message);

        NameValueDialogPort addNameValueDialog();

        FileChooserDialogPort addFileChooserDialog(FileChooserDialogParams params);

        DialogPort addPagedDialog(PageDialogParams params);
    }

    @Override
    Composer getComposer();

    void setDialogTypes(List<DialogType> types);
}
