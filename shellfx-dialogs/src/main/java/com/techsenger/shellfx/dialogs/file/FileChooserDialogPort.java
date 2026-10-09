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

import com.techsenger.annotations.Nullable;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.storage.FileStyleResolver;
import com.techsenger.shellfx.storage.GenericFile;
import java.net.URI;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the component's client API.
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

    void setLocationCaption(String value);

    StringProperty locationCaptionProperty();

    @Unmodifiable ObservableList<Location> getLocations();

    void setLocations(List<Location> locations);

    Location getLocation();

    /**
     * Sets the current location. This is a request, not a guarantee — the underlying selection control may
     * adjust or ignore it; observe {@link #locationProperty()} for the value actually applied.
     *
     * @param value the requested location
     */
    @RequestSetter
    void setLocation(Location value);

    ReadOnlyObjectProperty<Location> locationProperty();

    Mode getMode();

    void setMode(Mode mode);

    ReadOnlyObjectProperty<Mode> modeProperty();

    T getDirectory();

    ReadOnlyObjectProperty<T> directoryProperty();

    @Unmodifiable ObservableList<T> getFiles();

    T getFile();

    ReadOnlyObjectProperty<T> fileProperty();

    int getFileIndex();

    ReadOnlyIntegerProperty fileIndexProperty();

    @Unmodifiable ObservableList<ExtensionFilter> getExtensionFilters();

    void setExtensionFilters(List<ExtensionFilter> filters);

    ExtensionFilter getExtensionFilter();

    /**
     * Sets the current extension filter. This is a request, not a guarantee — the underlying selection control
     * may adjust or ignore it; observe {@link #extensionFilterProperty()} for the value actually applied.
     *
     * @param filter the requested extension filter
     */
    @RequestSetter
    void setExtensionFilter(ExtensionFilter filter);

    ReadOnlyObjectProperty<ExtensionFilter> extensionFilterProperty();

    String getFileName();

    void setFileName(String fileName);

    StringProperty fileNameProperty();

    /**
     * Returns the resolver of the styles files are shown with.
     *
     * @return the resolver, or {@code null} if files are shown with the default styles
     */
    @Nullable FileStyleResolver<T> getStyleResolver();

    void setStyleResolver(@Nullable FileStyleResolver<T> styleResolver);

    ObjectProperty<@Nullable FileStyleResolver<T>> styleResolverProperty();
}
