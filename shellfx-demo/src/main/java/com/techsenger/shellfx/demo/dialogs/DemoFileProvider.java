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

import com.techsenger.shellfx.storage.DefaultFileProvider;
import com.techsenger.shellfx.storage.FileStorage;
import com.techsenger.shellfx.storage.StorageFile;
import java.io.IOException;
import java.util.List;

/**
 * Refines the pending files of the demo storage, but only after the user presses the button of the dialog, so the
 * state between the two loading phases can be looked at.
 *
 * @author Pavel Castornii
 */
final class DemoFileProvider extends DefaultFileProvider<StorageFile> {

    DemoFileProvider(List<? extends FileStorage<StorageFile>> storages) {
        super(storages);
    }

    @Override
    protected boolean isRefinable(FileStorage<StorageFile> storage, StorageFile file) {
        return super.isRefinable(storage, file) || (file instanceof DemoFile demoFile && demoFile.isPending());
    }

    @Override
    protected void refine(StorageFile file) throws IOException {
        if (file instanceof DemoFile demoFile && file.getStorage() instanceof DemoFileStorage storage) {
            storage.awaitRelease();
            storage.simulateLatency();
            demoFile.complete();
        } else {
            super.refine(file);
        }
    }
}
