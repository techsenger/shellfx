/*
 * Copyright 2024-2025 Pavel Castornii.
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

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.DialogMediator;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.material.icon.FontIcon;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeDialogViewModel extends AbstractSimpleDialogViewModel<DialogMediator> {

    private final ObservableList<Theme> themes = FXCollections.observableArrayList(Arrays
            .stream(AtlantaFxTheme.values()).collect(Collectors.toList()));

    private final ObjectProperty<Theme> theme = new SimpleObjectProperty<>();

    public ThemeDialogViewModel(Theme currentTheme) {
        super(DialogScope.SHELL, false);
        setPrefWidth(500);
        setTitle("Select Theme");
        setIcon(new FontIcon(984334)); //theme-light-dark
        theme.set(currentTheme);
        setCancelVisible(true);
        setButtonWidthEqual(true);
    }

    public ObservableList<Theme> getThemes() {
        return themes;
    }

    public ObjectProperty<Theme> themeProperty() {
        return theme;
    }

    public Theme getTheme() {
        return theme.get();
    }

    public void setTheme(Theme theme) {
        this.theme.set(theme);
    }

    @Override
    public CloseCheckResult canClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
