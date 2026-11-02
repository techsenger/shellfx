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

package com.techsenger.shellfx.layout.tabhost;

import com.techsenger.shellfx.core.settings.SettingsListener;
import com.techsenger.shellfx.core.settings.ShellSettings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class ProminentTabHostViewModel<C extends TabHostComposer> extends TabHostViewModel<C> {

    private final ReadOnlyObjectWrapper<Font> regularFont = new ReadOnlyObjectWrapper<>();

    private final ShellSettings settings;

    private final SettingsListener settingsListener = this::applySettings;

    public ProminentTabHostViewModel(ProminentTabHostParams params) {
        super(params);
        this.settings = params.getSettings();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        applySettings();
        this.settings.addListener(this.settingsListener);
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.settings.removeListener(this.settingsListener);
    }

    ReadOnlyObjectProperty<Font> regularFontProperty() {
        return regularFont.getReadOnlyProperty();
    }

    private void applySettings() {
        this.regularFont.set(this.settings.getAppearance().getRegularFont());
    }
}
