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

package com.techsenger.shellfx.dialogs.file;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.storage.GenericFile;
import java.net.URI;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FileChooserDialogPort<T extends GenericFile> extends DialogPort {

    T getResult();

    AppearanceSettings getAppearanceSettings();

    FileChooserType getChooserType();

    URI getInitialDirectory();

    String getInitialFileName();

    String getLocationCaption();

    ReadOnlyStringProperty locationCaptionProperty();

    @Unmodifiable ObservableList<Location> getLocations();

    Location getLocation();

    ReadOnlyObjectProperty<Location> locationProperty();

    Mode getMode();

    ReadOnlyObjectProperty<Mode> modeProperty();

    T getDirectory();

    ReadOnlyObjectProperty<T> directoryProperty();

    @Unmodifiable ObservableList<T> getFiles();

    T getFile();

    ReadOnlyObjectProperty<T> fileProperty();

    int getFileIndex();

    ReadOnlyIntegerProperty fileIndexProperty();

    @Unmodifiable ObservableList<ExtensionFilter> getExtensionFilters();

    ExtensionFilter getExtensionFilter();

    ReadOnlyObjectProperty<ExtensionFilter> extensionFilterProperty();

    String getFileName();

    ReadOnlyStringProperty fileNameProperty();
}
