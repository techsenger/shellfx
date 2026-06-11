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

package com.techsenger.tabshell.demo.settings;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.material.icon.FontIcon;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsDialogPresenter extends AbstractDialogPresenter<SettingsDialogView> {

    private final AppearanceSettings settings;

    private Theme theme;

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
        setIcon(new FontIcon(0xF08BB));
        setWidth(500);
        setResizable(false);
        getView().setThemes(Arrays.stream(AtlantaFxTheme.values()).collect(Collectors.toList()));
        getView().setTheme(settings.getTheme());
        setRightButtons(SettingsDialogButtons.CANCEL, SettingsDialogButtons.OK);
        setButtonDefault(SettingsDialogButtons.OK, true);
        setOnResult((buttonName) -> {
            if (buttonName == SettingsDialogButtons.OK) {
                settings.setTheme(this.theme);
            }
            closeSafely();
        });
    }

    void onThemeSelected(Theme theme) {
        this.theme = theme;
    }
}
