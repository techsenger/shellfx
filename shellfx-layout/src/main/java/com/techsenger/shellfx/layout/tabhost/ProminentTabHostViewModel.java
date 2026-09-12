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

import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.SettingsSubscription;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class ProminentTabHostViewModel<C extends TabHostComposer> extends TabHostViewModel<C> {

    private final ReadOnlyObjectWrapper<Font> regularFont = new ReadOnlyObjectWrapper<>();

    private final AppearanceSettings appearanceSettings;

    private SettingsSubscription regularFontSubscription;

    public ProminentTabHostViewModel(ProminentTabHostParams params) {
        super(params);
        this.appearanceSettings = params.getSettings();
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        setRegularFont(this.appearanceSettings.getRegularFont());
        this.regularFontSubscription =
                this.appearanceSettings.onRegularFontChanged((oldV, newV) -> setRegularFont(newV));
    }

    @Override
    protected void postDeinitialize() {
        super.postDeinitialize();
        this.regularFontSubscription.unsubscribe();
    }

    ReadOnlyObjectProperty<Font> regularFontProperty() {
        return regularFont.getReadOnlyProperty();
    }

    private void setRegularFont(Font font) {
        this.regularFont.set(font);
    }
}
