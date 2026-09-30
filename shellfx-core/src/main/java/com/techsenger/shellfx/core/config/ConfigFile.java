/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.core.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads and writes the configs in a single file using Java serialization. Classes are
 * resolved with the given class loader, so configs whose classes come from plugins can be read back.
 *
 * @author Pavel Castornii
 */
public class ConfigFile {

    private static final Logger logger = LoggerFactory.getLogger(ConfigFile.class);

    private final Path path;

    private final ClassLoader classLoader;

    private ConfigData data = new ConfigData();

    public ConfigFile(Path path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }

    /**
     * Replaces the data with what the file holds, or with empty data if the file does not exist yet.
     *
     * @throws IOException if the file exists but can't be read.
     */
    public void read() throws IOException {
        if (!Files.exists(path)) {
            logger.info("No config file at {}", path);
            this.data = new ConfigData();
            return;
        }
        try (var in = new ObjectInputStream(Files.newInputStream(path)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), false, classLoader);
            }
        }) {
            this.data = (ConfigData) in.readObject();
            logger.debug("Read from {} config data: {}", path, this.data);
        } catch (ClassNotFoundException ex) {
            throw new IOException("Couldn't read config data from " + path, ex);
        }
    }

    /**
     * Writes the data, replacing the file as a whole so a failure never leaves a half-written one.
     *
     * @throws IOException if the file can't be written.
     */
    public synchronized void write() throws IOException {
        var parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        var temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (var out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(this.data);
        }
        try {
            Files.move(temp, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
        logger.debug("Wrote to {} config data: {}", path, this.data);
    }

    ConfigData getData() {
        return data;
    }

    public Path getPath() {
        return path;
    }
}
