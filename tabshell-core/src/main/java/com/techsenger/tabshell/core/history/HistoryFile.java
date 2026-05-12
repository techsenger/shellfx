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

package com.techsenger.tabshell.core.history;

import com.techsenger.patternfx.core.ComponentHistory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class HistoryFile {

    static class HistoryData implements Serializable {

        private Map<Class<? extends ComponentHistory>, ComponentHistory> historiesByClass;

        private Map<UUID, ComponentHistory> historiesByUuid;

        public Map<Class<? extends ComponentHistory>, ComponentHistory> getHistoriesByClass() {
            return historiesByClass;
        }

        public Map<UUID, ComponentHistory> getHistoriesByUuid() {
            return historiesByUuid;
        }

        @Override
        public String toString() {
            return "HistoryData [" + "historiesByClass:" + historiesByClass + ", historiesByUuid:" + historiesByUuid
                    + ']';
        }

        void setHistoriesByClass(Map<Class<? extends ComponentHistory>, ComponentHistory> historiesByClass) {
            this.historiesByClass = historiesByClass;
        }

        void setHistoriesByUuid(Map<UUID, ComponentHistory> historiesByUuid) {
            this.historiesByUuid = historiesByUuid;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HistoryFile.class);

    private final Path path;

    private HistoryData data;

    public HistoryFile(Path path) {
        this.path = path;
    }

    public void read() throws FileNotFoundException, IOException, ClassNotFoundException {
        var file = path.toFile();
        if (!file.exists()) {
            logger.info("No history data file at {}", path);
            this.data = new HistoryData();
            this.data.setHistoriesByClass(new ConcurrentHashMap<>());
            this.data.setHistoriesByUuid(new ConcurrentHashMap<>());
            return;
        }
        try (FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f)) {
            this.data = (HistoryData) s.readObject();
            logger.debug("Read from {} history data: {}", path, this.data);
        }
    }

    public void write() throws FileNotFoundException, IOException {
        try (FileOutputStream f = new FileOutputStream(path.toFile());
             ObjectOutputStream s = new ObjectOutputStream(f)) {
            s.writeObject(this.data);
            logger.debug("Wrote to {} history data: {}", path, this.data);
        }
    }

    public HistoryData getData() {
        return data;
    }

    public Path getPath() {
        return path;
    }
}
