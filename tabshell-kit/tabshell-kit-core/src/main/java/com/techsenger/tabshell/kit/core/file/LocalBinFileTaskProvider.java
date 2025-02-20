/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.kit.core.file;

import com.techsenger.tabshell.kit.core.workertab.TabWorker;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.concurrent.Task;

/**
 *
 * @author Pavel Castornii
 */
public class LocalBinFileTaskProvider implements FileTaskProvider<byte[]> {

    @Override
    public TabWorker<byte[]> createFileReader(FileInfo fileInfo) {
        class ReaderTask extends Task<byte[]> implements TabWorker<byte[]> {

            @Override
            protected byte[] call() throws Exception {
                var p = Paths.get(fileInfo.getPath());
                var f = p.toFile();
                if (!f.exists()) {
                    return null;
                }
                if (fileInfo.getSize() == null) {
                    fileInfo.setSize(f.length());
                }
                byte[] array = Files.readAllBytes(p);
                updateProgress(100, 100);
                return array;
            }

            @Override
            public boolean usesProgress() {
                return true;
            }
        }
        return new ReaderTask();
    }

    @Override
    public TabWorker<Void> createFileWriter(FileInfo fileInfo, byte[] content) {
        class WriterTask extends Task<Void> implements TabWorker<Void> {

            @Override
            protected Void call() throws Exception {
                var p = Paths.get(fileInfo.getPath());
                Files.write(p, content);
                updateProgress(100, 100);
                return null;
            }

            @Override
            public boolean usesProgress() {
                return true;
            }
        }
        return new WriterTask();
    }

}
