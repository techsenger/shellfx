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

package com.techsenger.shellfx.storage.style;

/**
 * The styles of the kinds of files a storage knows for one theme: directories, files, links of every kind and other
 * entries, plus the masks of the states (error, hidden) that are laid over the style of the kind. A scheme can be
 * extended by an application that knows more kinds of files.
 *
 * @author Pavel Castornii
 */
public class FileStyleScheme {

    private FileStyleSettings directory = new FileStyleSettings();

    private FileStyleSettings directoryLink = new FileStyleSettings();

    private FileStyleSettings fileLink = new FileStyleSettings();

    private FileStyleSettings linkLink = new FileStyleSettings();

    private FileStyleSettings otherLink = new FileStyleSettings();

    private FileStyleSettings other = new FileStyleSettings();

    private FileStyleSettings file = new FileStyleSettings();

    private FileStyleSettings error = new FileStyleSettings();

    private FileStyleSettings hidden = new FileStyleSettings();

    public FileStyleSettings getDirectory() {
        return this.directory;
    }

    public void setDirectory(FileStyleSettings directory) {
        this.directory = directory;
    }

    public FileStyleSettings getDirectoryLink() {
        return this.directoryLink;
    }

    public void setDirectoryLink(FileStyleSettings directoryLink) {
        this.directoryLink = directoryLink;
    }

    public FileStyleSettings getFileLink() {
        return this.fileLink;
    }

    public void setFileLink(FileStyleSettings fileLink) {
        this.fileLink = fileLink;
    }

    public FileStyleSettings getLinkLink() {
        return this.linkLink;
    }

    public void setLinkLink(FileStyleSettings linkLink) {
        this.linkLink = linkLink;
    }

    public FileStyleSettings getOtherLink() {
        return this.otherLink;
    }

    public void setOtherLink(FileStyleSettings otherLink) {
        this.otherLink = otherLink;
    }

    public FileStyleSettings getOther() {
        return this.other;
    }

    public void setOther(FileStyleSettings other) {
        this.other = other;
    }

    public FileStyleSettings getFile() {
        return this.file;
    }

    public void setFile(FileStyleSettings file) {
        this.file = file;
    }

    public FileStyleSettings getError() {
        return this.error;
    }

    public void setError(FileStyleSettings error) {
        this.error = error;
    }

    public FileStyleSettings getHidden() {
        return this.hidden;
    }

    public void setHidden(FileStyleSettings hidden) {
        this.hidden = hidden;
    }
}
