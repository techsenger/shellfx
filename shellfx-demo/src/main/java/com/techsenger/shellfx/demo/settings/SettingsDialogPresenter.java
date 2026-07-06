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

package com.techsenger.shellfx.demo.settings;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogPresenter;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.settings.Density;
import com.techsenger.shellfx.demo.DemoComponents;
import com.techsenger.shellfx.material.icon.PlainFontIcon;
import com.techsenger.shellfx.material.theme.Theme;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsDialogPresenter extends AbstractDialogPresenter<SettingsDialogView> {

    private final AppearanceSettings settings;

    /**
     * Important. WindowView has setDensity and setTheme methods.
     */
    private Theme selectedTheme;

    private Density selectedDensity;

    public SettingsDialogPresenter(SettingsDialogView view, SettingsDialogParams params) {
        super(view, params);
        this.settings = params.getSettings();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.SETTINGS_DIALOG);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Settings");
        setIcon(new PlainFontIcon(0xF08BB));
        setWidth(500);
        setResizable(false);
        setSelectedTheme(settings.getTheme());
        setSelectedDensity(settings.getDensity());
        getView().
        setRightButtons(SettingsDialogButtons.CANCEL, SettingsDialogButtons.OK);
        setButtonDefault(SettingsDialogButtons.OK, true);
        setOnResult((buttonName) -> {
            if (buttonName == SettingsDialogButtons.OK) {
                settings.setTheme(this.selectedTheme);
                settings.setDensity(selectedDensity);
            }
            closeSafely();
        });
    }

    void onThemeSelected(Theme theme) {
        this.selectedTheme = theme;
    }

    void onDensitySelected(Density density) {
        this.selectedDensity = density;
    }

    private void setSelectedTheme(Theme theme) {
        if (this.selectedTheme == theme) {
            return;
        }
        this.selectedTheme = theme;
        getView().setSelectedTheme(theme);
    }

    private void setSelectedDensity(Density density) {
        if (this.selectedDensity == density) {
            return;
        }
        this.selectedDensity = density;
        getView().setSelectedDensity(density);
    }
}
