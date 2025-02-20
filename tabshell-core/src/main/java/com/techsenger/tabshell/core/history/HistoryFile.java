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

package com.techsenger.tabshell.core.history;

import com.techsenger.mvvm4fx.core.ComponentHistory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class HistoryFile {

    private static final Logger logger = LoggerFactory.getLogger(HistoryFile.class);

    private Map<Class<? extends ComponentHistory>, ComponentHistory> historiesByClass;

    private final Path path;

    public HistoryFile(Path path) {
        this.path = path;
    }

    public void read() {
        var file = path.toFile();
        if (!file.exists()) {
            logger.info("No history data file at {}", path);
            this.historiesByClass = new ConcurrentHashMap<>();
            return;
        }
        //we will read string keyed map
        try (FileInputStream f = new FileInputStream(file);
            ObjectInputStream s = new ObjectInputStream(f)) {
            this.historiesByClass = (Map<Class<? extends ComponentHistory>, ComponentHistory>) s.readObject();
            logger.debug("Read from {} history map: {}", path, this.historiesByClass);
        } catch (Exception ex) {
            logger.error("Error reading history from {}", path, ex);
        }
    }

    public void write() {
        try (FileOutputStream f = new FileOutputStream(path.toFile());
             ObjectOutputStream s = new ObjectOutputStream(f)) {
            s.writeObject(this.historiesByClass);
            logger.debug("Wrote to {} history map: {}", path, this.historiesByClass);
        } catch (Exception ex) {
            logger.error("Error writing history to {}", path, ex);
        }
    }

    Map<Class<? extends ComponentHistory>, ComponentHistory> getHistoriesByClass() {
        return historiesByClass;
    }
}
