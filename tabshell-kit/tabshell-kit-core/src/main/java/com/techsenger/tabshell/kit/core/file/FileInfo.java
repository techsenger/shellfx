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

import java.nio.charset.Charset;

/**
 * File path has type String, but not Path to make it more universal, for example, to work with files over FTP.
 *
 * @author Pavel Castornii
 */
public class FileInfo {

    /**
     * Path of the file.
     */
    private String path;

    /**
     * Name of the file including extension.
     */
    private String name;

    /**
     * Extension of the file.
     */
    private String extension;

    /**
     * The size of the file in bytes.
     */
    private Long size;

    /**
     * Remote file is a file outside local file system (for example ftp etc files).
     */
    private boolean remote;

    /**
     * Charset of the file. It can be used only for text files.
     */
    private Charset charset;

    public FileInfo() {

    }

    public FileInfo(String path, String name, String extension) {
        this(path, name, extension, null, false, null);
    }

    public FileInfo(String path, String name, String extension, Long size) {
        this(path, name, extension, size, false, null);
    }

    public FileInfo(String path, String name, String extension, Long size, boolean remote, Charset charset) {
        this.path = path;
        this.name = name;
        this.extension = extension;
        this.remote = remote;
        this.size = size;
        this.charset = charset;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public boolean isRemote() {
        return remote;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
