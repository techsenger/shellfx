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

import com.techsenger.shellfx.material.list.ColumnListView;
import com.techsenger.shellfx.storage.GenericFile;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;

/**
 *
 * @author Pavel Castornii
 */
class FileListView extends ColumnListView<GenericFile> {

    private final FileStringConverter stringConverter = new FileStringConverter();

    FileListView(ObservableList<GenericFile> files, ContextMenu cellContextMenu) {
        setItems(files);
        setManualRefresh(true);
        setEditable(true);
    }
}
