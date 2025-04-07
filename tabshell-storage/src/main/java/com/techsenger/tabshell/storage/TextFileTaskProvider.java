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

package com.techsenger.tabshell.storage;

import com.techsenger.tabshell.core.tab.TabWorker;
import java.nio.charset.Charset;
import javafx.concurrent.Task;

/**
 *
 * @author Pavel Castornii
 */
public class TextFileTaskProvider implements FileTaskProvider<String> {

    private final Charset charset;

    public TextFileTaskProvider(Charset charset) {
        this.charset = charset;
    }

    @Override
    public TabWorker<String> createFileReader(GenericFile file) {
        class ReaderTask extends Task<String> implements TabWorker<String> {

            @Override
            protected String call() throws Exception {
                var storage = file.getStorage();
                var content = storage.readFile(file.getUri(), charset);
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
    public TabWorker<Void> createFileWriter(GenericFile file, String content) {
        class WriterTask extends Task<Void> implements TabWorker<Void> {

            @Override
            protected Void call() throws Exception {
                var storage = file.getStorage();
                storage.writeFile(file.getUri(), content, charset);
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
