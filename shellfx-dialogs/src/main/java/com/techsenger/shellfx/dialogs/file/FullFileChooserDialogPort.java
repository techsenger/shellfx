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

import com.techsenger.shellfx.core.dialog.FullDialogPort;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.storage.GenericFile;
import java.util.List;
import javafx.beans.property.StringProperty;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface FullFileChooserDialogPort<T extends GenericFile> extends FileChooserDialogPort<T>, FullDialogPort {

    void setLocationCaption(String value);

    @Override
    StringProperty locationCaptionProperty();

    void setLocations(List<Location> locations);

    /**
     * Sets the current location. This is a request, not a guarantee — the underlying selection control may
     * adjust or ignore it; observe {@link #locationProperty()} for the value actually applied.
     *
     * @param value the requested location
     */
    @RequestSetter
    void setLocation(Location value);

    void setMode(Mode mode);

    void setExtensionFilters(List<ExtensionFilter> filters);

    /**
     * Sets the current extension filter. This is a request, not a guarantee — the underlying selection control
     * may adjust or ignore it; observe {@link #extensionFilterProperty()} for the value actually applied.
     *
     * @param filter the requested extension filter
     */
    @RequestSetter
    void setExtensionFilter(ExtensionFilter filter);

    void setFileName(String fileName);

    @Override
    StringProperty fileNameProperty();
}
