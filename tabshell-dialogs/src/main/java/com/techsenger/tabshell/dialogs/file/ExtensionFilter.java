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

package com.techsenger.tabshell.dialogs.file;

import com.techsenger.toolkit.core.file.FileUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class ExtensionFilter {

    private final String description;

    private final boolean caseSensitive;

    /**
     * *.*, *.png, *.gif.
     */
    private final List<String> extensions;

    /**
     * png, gif.
     */
    private final List<String> pureExtensions;

    private final boolean matchesAllFiles;

    public ExtensionFilter(String description, boolean caseSensitive, String... extensions) {
        this(description, caseSensitive, Arrays.asList(extensions));
    }

    public ExtensionFilter(String description, boolean caseSensitive, List<String> extensions) {
        this.description = description;
        this.caseSensitive = caseSensitive;
        this.extensions = extensions;

        this.pureExtensions = new ArrayList<String>();
        var matchesAll = false;
        for (var ext : extensions) {
            if (ext.equals("*.*")) {
                matchesAll = true;
                continue;
            }
            var dotIndex = ext.lastIndexOf(".");
            pureExtensions.add(ext.substring(dotIndex + 1));
        }
        this.matchesAllFiles = matchesAll;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public List<String> getPureExtensions() {
        return pureExtensions;
    }

    public boolean matchesAllFiles() {
        return matchesAllFiles;
    }

    public boolean matches(String fileName) {
        if (matchesAllFiles) {
            return true;
        }
        String fileExt = FileUtils.getExtension(fileName);
        if (fileExt == null) {
            return false;
        }
        if (this.caseSensitive) {
            for (var ext : this.pureExtensions) {
                if (fileExt.equals(ext)) {
                    return true;
                }
            }
        } else {
            for (var ext : this.pureExtensions) {
                if (fileExt.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        var extStr = extensions.stream().collect(Collectors.joining(", "));
        var result = description + " (" + extStr + ")";
        return result;
    }


}
