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

import com.techsenger.shellfx.dialogs.file.FileChooserDialogView;
import com.techsenger.shellfx.storage.StorageFile;
import javafx.scene.control.Button;

/**
 * The view of the chooser over the demo storage, with the button that refines the pending files.
 *
 * @author Pavel Castornii
 */
final class DemoFileChooserDialogView extends FileChooserDialogView<DemoFileChooserDialogViewModel, StorageFile> {

    DemoFileChooserDialogView(DemoFileChooserDialogViewModel viewModel) {
        super(viewModel);
    }

    @Override
    protected void build() {
        super.build();
        var button = new Button("Refine files");
        button.setOnAction(e -> getViewModel().onRefineFiles());
        getLeftBottomBox().getChildren().add(button);
    }
}
