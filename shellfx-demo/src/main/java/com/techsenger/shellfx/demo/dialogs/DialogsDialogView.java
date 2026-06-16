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

package com.techsenger.shellfx.demo.dialogs;

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.window.WindowView;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogPort;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.NameValueDialogPort;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogsDialogView extends DialogView {

    interface Composer extends WindowView.Composer {

        AlertDialogPort openAlertDialog(AlertDialogParams params);

        NameValueDialogPort openNameValueDialog(DialogParams params);

        FileChooserDialogPort openFileChooserDialog(FileChooserDialogParams params);

        DialogPort openPagedDialog(PageDialogParams params);
    }

    @Override
    Composer getComposer();

    void setDialogTypes(List<DialogType> types);
}
