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

package com.techsenger.tabshell.hex.model;

import com.techsenger.tabshell.storage.GenericFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class HexDocument {

    private static final Logger logger = LoggerFactory.getLogger(HexDocument.class);

    private GenericFile file;

    private byte[] content;

    public HexDocument(GenericFile file) {
        this.file = file;
    }

    public byte[] getContent() {
        return content;
    }

    public GenericFile getFile() {
        return file;
    }

    public boolean readFile() {
        try {
            this.content = this.file.getStorage().readFile(this.file.getUri());
            return true;
        } catch (Exception ex) {
            logger.error("Error reading file at {}", this.file.getUri(), ex);
            return false;
        }
    }

    public boolean writeFile() {
        try {
            this.file.getStorage().writeFile(this.file.getUri(), content);
            return true;
        } catch (Exception ex) {
            logger.error("Error writing file at {}", this.file.getUri(), ex);
            return false;
        }
    }

    public void setFile(GenericFile file) {
        this.file = file;
    }
}
