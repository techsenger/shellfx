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

import com.techsenger.shellfx.core.close.CloseCheckResult;
import com.techsenger.shellfx.core.close.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowComposer;
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

    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<Theme> themeSource = new SimpleObservableSource<>();

    private final ReadOnlyObjectWrapper<Density> density = new ReadOnlyObjectWrapper<>();

    private final ObservableSource<Density> densitySource = new SimpleObservableSource<>();

    private final AppearanceSettings settings;

    public SettingsDialogViewModel(SettingsDialogParams params) {
        super(params);
        this.settings = params.getSettings();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Theme getTheme() {
        return theme.get();
    }

    public void setTheme(Theme theme) {
        themeSource.next(theme);
    }

    public ReadOnlyObjectProperty<Theme> themeProperty() {
        return theme.getReadOnlyProperty();
    }

    public Density getDensity() {
        return density.get();
    }

    public void setDensity(Density density) {
        densitySource.next(density);
    }

    public ReadOnlyObjectProperty<Density> densityProperty() {
        return density.getReadOnlyProperty();
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(500);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setTitle("Settings");
        setIcon(new PlainFontIcon(0xF08BB));
        setResizable(false);
        setTheme(settings.getTheme());
        setDensity(settings.getDensity());
        setRightButtons(SettingsDialogButtons.CANCEL, SettingsDialogButtons.OK);
        setButtonDefault(SettingsDialogButtons.OK, true);
        setOnResult((buttonName) -> {
            if (buttonName == SettingsDialogButtons.OK) {
                settings.setTheme(theme.get());
                settings.setDensity(density.get());
            }
            closeSafely();
        });
    }

    /**
     * Framework contract: written directly by the View to report the theme combo box's actual selected item,
     * intended exclusively for {@code SettingsDialogView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyObjectWrapper<Theme> themeWrapper() {
        return theme;
    }

    ObservableSource<Theme> themeSource() {
        return themeSource;
    }

    /**
     * Framework contract: written directly by the View to report the density combo box's actual selected item,
     * intended exclusively for {@code SettingsDialogView}. Direct invocation by user code results in undefined
     * behavior.
     */
    ReadOnlyObjectWrapper<Density> densityWrapper() {
        return density;
    }

    ObservableSource<Density> densitySource() {
        return densitySource;
    }
}
