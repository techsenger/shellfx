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

package com.techsenger.tabshell.demos.full;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.core.theme.ShellTheme;
import com.techsenger.tabshell.dialogs.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.material.icon.FontIcon;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeDialogViewModel extends AbstractSimpleDialogViewModel {

    private final ObservableList<ShellTheme> themes = FXCollections.observableArrayList(Arrays
            .stream(ShellTheme.values()).filter(t -> t.isSupported()).collect(Collectors.toList()));

    private final ObjectProperty<ShellTheme> theme = new SimpleObjectProperty<>();

    public ThemeDialogViewModel(ShellTheme currentTheme) {
        super(DialogScope.SHELL, false);
        setPrefWidth(500);
        setTitle("Select Theme");
        setIcon(new FontIcon(984334)); //theme-light-dark
        theme.set(currentTheme);
        setCancelVisible(true);
        setButtonWidthEqual(true);
    }

    public ObservableList<ShellTheme> getThemes() {
        return themes;
    }

    public ObjectProperty<ShellTheme> themeProperty() {
        return theme;
    }

    public ShellTheme getTheme() {
        return theme.get();
    }

    public void setTheme(ShellTheme theme) {
        this.theme.set(theme);
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponentNames.THEME_DIALOG);
    }
}
