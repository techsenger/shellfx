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

package com.techsenger.tabshell.core.page;

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.material.icon.Icon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageViewModel<T extends PageMediator> extends AbstractAreaViewModel<T>
        implements PageViewModel<T> {

    private final StringProperty title = new SimpleStringProperty();

    private final ObjectProperty<Icon<?>> icon = new SimpleObjectProperty<>();

    private final ReadOnlyBooleanWrapper selected = new ReadOnlyBooleanWrapper();

    @Override
    public StringProperty titleProperty() {
        return this.title;
    }

    @Override
    public String getTitle() {
        return this.title.get();
    }

    @Override
    public void setTitle(String title) {
        this.title.set(title);
    }

    @Override
    public ObjectProperty<Icon<?>> iconProperty() {
        return icon;
    }

    @Override
    public Icon<?> getIcon() {
        return this.icon.get();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        this.icon.set(icon);
    }

    @Override
    public ReadOnlyBooleanProperty selectedProperty() {
        return this.selected;
    }

    @Override
    public boolean isSelected() {
        return this.selected.get();
    }

    @Override
    protected AbstractPageHistory getHistory() {
        return (AbstractPageHistory) super.getHistory();
    }

    ReadOnlyBooleanWrapper selectedWrapper() {
        return selected;
    }
}
