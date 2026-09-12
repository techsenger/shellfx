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
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.demo.page.PageDialogParams;
import com.techsenger.shellfx.dialogs.alert.AlertDialogParams;
import com.techsenger.shellfx.dialogs.alert.FullAlertDialogPort;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FullFileChooserDialogPort;
import com.techsenger.shellfx.dialogs.namevalue.FullNameValueDialogPort;
import com.techsenger.shellfx.dialogs.progress.FullProgressDialogPort;
import com.techsenger.shellfx.storage.GenericFile;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogsDialogComposer extends WindowComposer {

    FullAlertDialogPort openAlertDialog(AlertDialogParams params);

    FullNameValueDialogPort openNameValueDialog(DialogParams params);

    FullProgressDialogPort openProgressDialog(DialogParams params);

    FullFileChooserDialogPort<GenericFile> openFileChooserDialog(FileChooserDialogParams<GenericFile> params);

    DialogPort openPagedDialog(PageDialogParams params);
}
