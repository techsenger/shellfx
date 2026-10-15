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

import com.techsenger.shellfx.dialogs.file.FileChooserDialogComposer;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogParams;
import com.techsenger.shellfx.dialogs.file.FileChooserDialogViewModel;
import com.techsenger.shellfx.storage.StorageFile;

/**
 * A file chooser over the demo storage, whose button refines the pending files, so the state between the two
 * loading phases can be looked at.
 *
 * @author Pavel Castornii
 */
final class DemoFileChooserDialogViewModel extends FileChooserDialogViewModel<FileChooserDialogComposer, StorageFile> {

    private final DemoFileStorage demoStorage;

    DemoFileChooserDialogViewModel(FileChooserDialogParams<StorageFile> params, DemoFileStorage demoStorage) {
        super(params);
        this.demoStorage = demoStorage;
    }

    void onRefineFiles() {
        demoStorage.release();
    }
}
