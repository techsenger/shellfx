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

package com.techsenger.shellfx.storage;

import javafx.util.StringConverter;


/**
 *
 * @author Pavel Castornii
 */
public class FileStringConverter<F extends GenericFile> extends StringConverter<F> {

    /**
     * It is supposed that toString is called always before fromString.
     */
    private F file;

    @Override
    public String toString(F object) {
        this.file = object;
        return object != null ? object.getName() : "";
    }

    @Override
    public F fromString(String string) {
        ((DefaultGenericFile) file).setName(string);
        return file;
    }
}
