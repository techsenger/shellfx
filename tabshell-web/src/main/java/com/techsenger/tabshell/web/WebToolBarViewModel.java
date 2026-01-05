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

package com.techsenger.tabshell.web;

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.core.area.AreaMediator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class WebToolBarViewModel extends AbstractAreaViewModel<AreaMediator> {

    private final BooleanProperty backDisable = new SimpleBooleanProperty(true);

    private final BooleanProperty forwardDisable = new SimpleBooleanProperty(true);

    private final BooleanProperty reloadDisable = new SimpleBooleanProperty(true);

    private final StringProperty url = new SimpleStringProperty();

    public boolean isBackDisable() {
        return backDisable.get();
    }

    public void setBackDisable(boolean value) {
        backDisable.set(value);
    }

    public BooleanProperty backDisableProperty() {
        return backDisable;
    }

    public boolean isForwardDisable() {
        return forwardDisable.get();
    }

    public void setForwardDisable(boolean value) {
        forwardDisable.set(value);
    }

    public BooleanProperty forwardDisableProperty() {
        return forwardDisable;
    }

    public boolean isReloadDisable() {
        return reloadDisable.get();
    }

    public void setReloadDisable(boolean value) {
        reloadDisable.set(value);
    }

    public BooleanProperty reloadDisableProperty() {
        return reloadDisable;
    }

    public String getUrl() {
        return url.get();
    }

    public void setUrl(String value) {
        url.set(value);
    }

    public StringProperty urlProperty() {
        return url;
    }
}
