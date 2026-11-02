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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.material.RequestSetter;
import com.techsenger.shellfx.material.icon.PlainFontIcon;
import com.techsenger.shellfx.material.style.Density;
import com.techsenger.shellfx.material.theme.Theme;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class SettingsDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C> {

    private final ReadOnlyObjectWrapper<Theme> selectedTheme = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<Theme> selectedThemeSource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<@Nullable Density> selectedDensity = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<@Nullable Density> selectedDensitySource = new SimpleObservableSource<>();

    public SettingsDialogViewModel(SettingsDialogParams params) {
        super(params);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Theme getSelectedTheme() {
        return selectedTheme.get();
    }

    @RequestSetter
    public void setSelectedTheme(Theme theme) {
        selectedThemeSource.next(theme);
    }

    public ReadOnlyObjectProperty<Theme> selectedThemeProperty() {
        return selectedTheme.getReadOnlyProperty();
    }

    public @Nullable Density getSelectedDensity() {
        return selectedDensity.get();
    }

    @RequestSetter
    public void setSelectedDensity(@Nullable Density density) {
        selectedDensitySource.next(density);
    }

    public ReadOnlyObjectProperty<@Nullable Density> selectedDensityProperty() {
        return selectedDensity.getReadOnlyProperty();
    }

    @Override
    protected SettingsDialogConfig getConfig() {
        return (SettingsDialogConfig) super.getConfig();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Settings");
        setIcon(new PlainFontIcon(0xF08BB));
        setResizable(false);
        var appearance = getSettings().getAppearance();
        setSelectedTheme(appearance.getTheme());
        setSelectedDensity(appearance.getDensity());
        setRightButtons(SettingsDialogButtons.CANCEL, SettingsDialogButtons.OK);
        setButtonDefault(SettingsDialogButtons.OK, true);
        setOnResult((buttonName) -> {
            if (buttonName == SettingsDialogButtons.OK) {
                appearance.setTheme(selectedTheme.get());
                appearance.setDensity(selectedDensity.get());
                getSettings().notifyListeners();
            }
            closeSafely();
        });
    }

    /**
     * Framework contract: written directly by the View to report the theme combo box's actual selected item,
     * intended exclusively for {@code SettingsDialogView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyObjectWrapper<Theme> selectedThemeWrapper() {
        return selectedTheme;
    }

    ObservableSource<Theme> selectedThemeSource() {
        return selectedThemeSource;
    }

    /**
     * Framework contract: written directly by the View to report the density combo box's actual selected item,
     * intended exclusively for {@code SettingsDialogView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyObjectWrapper<@Nullable Density> selectedDensityWrapper() {
        return selectedDensity;
    }

    ObservableSource<@Nullable Density> selectedDensitySource() {
        return selectedDensitySource;
    }
}
