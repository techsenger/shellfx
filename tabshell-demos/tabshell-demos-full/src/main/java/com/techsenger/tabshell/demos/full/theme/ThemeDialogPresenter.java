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

package com.techsenger.tabshell.demos.full.theme;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.demos.full.DemoComponents;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeDialogPresenter extends AbstractDialogPresenter<ThemeDialogView, DialogComposer> {

    private final AppearanceSettings settings;

    public ThemeDialogPresenter(ThemeDialogView view, AppearanceSettings settings) {
        super(view, OverlayScope.SHELL);
        this.settings = settings;
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.DEMO_THEME_DIALOG);
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
        getView().setThemes(Arrays.stream(AtlantaFxTheme.values()).collect(Collectors.toList()));
        getView().setTheme(settings.getTheme());
        setResultAction((buttonName) -> {
            if (buttonName == ThemeDialogButtons.OK) {
                settings.setTheme(getView().getTheme());
            }
            requestClose();
        });
    }
}
