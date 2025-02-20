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
import com.techsenger.toolkit.core.file.FileUtils;
import java.nio.file.Paths;
import javafx.concurrent.Task;

/**
 *
 * @author Pavel Castornii
 */
public class LocalTextFileTaskProvider implements FileTaskProvider<String> {

    @Override
    public TabWorker<String> createFileReader(FileInfo fileInfo) {
        class ReaderTask extends Task<String> implements TabWorker<String> {

            @Override
            protected String call() throws Exception {
                var p = Paths.get(fileInfo.getPath());
                var f = p.toFile();
                if (!f.exists()) {
                    return null;
                }
                if (fileInfo.getSize() == null) {
                    fileInfo.setSize(f.length());
                }
                var content = FileUtils.readFile(p, fileInfo.getCharset());
                updateProgress(100, 100);
                return content;
            }

            @Override
            public boolean usesProgress() {
                return true;
            }
        }
        return new ReaderTask();
    }

    @Override
    public TabWorker<Void> createFileWriter(FileInfo fileInfo, String content) {
        class WriterTask extends Task<Void> implements TabWorker<Void> {

            @Override
            protected Void call() throws Exception {
                var p = Paths.get(fileInfo.getPath());
                FileUtils.writeFile(p, content, fileInfo.getCharset());
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
