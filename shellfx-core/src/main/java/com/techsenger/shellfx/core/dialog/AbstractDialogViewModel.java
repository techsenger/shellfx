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

package com.techsenger.shellfx.core.dialog;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.window.AbstractWindowViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.material.button.ResultButtonName;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractDialogViewModel<C extends WindowComposer> extends AbstractWindowViewModel<C>
        implements DialogViewModel<C> {

    private final ObservableList<ResultButtonName> modifiableLeftButtons = FXCollections.observableArrayList();

    private final ObservableList<ResultButtonName> leftButtons =
            FXCollections.unmodifiableObservableList(modifiableLeftButtons);

    private final ObservableList<ResultButtonName> modifiableRightButtons = FXCollections.observableArrayList();

    private final ObservableList<ResultButtonName> rightButtons =
            FXCollections.unmodifiableObservableList(modifiableRightButtons);

    private final ObservableMap<ResultButtonName, Boolean> modifiableDisabledByName =
            FXCollections.observableHashMap();

    private final ObservableMap<ResultButtonName, Boolean> disabledByName =
            FXCollections.unmodifiableObservableMap(modifiableDisabledByName);

    private final ObservableMap<ResultButtonName, Boolean> modifiableDefaultByName =
            FXCollections.observableHashMap();

    private final ObservableMap<ResultButtonName, Boolean> defaultByName =
            FXCollections.unmodifiableObservableMap(modifiableDefaultByName);

    private Consumer<ResultButtonName> onResult = (name) -> closeSafely();

    public AbstractDialogViewModel(DialogParams params) {
        super(params);
    }

    @Override
    public @Unmodifiable ObservableList<ResultButtonName> getLeftButtons() {
        return leftButtons;
    }

    @Override
    public @Unmodifiable ObservableList<ResultButtonName> getRightButtons() {
        return rightButtons;
    }

    @Override
    public void setLeftButtons(ResultButtonName... names) {
        List<ResultButtonName> foundNames = new ArrayList<>();
        for (var name : names) {
            if (isRegistered(name)) {
                foundNames.add(name);
            }
        }
        modifiableLeftButtons.setAll(foundNames);
    }

    @Override
    public void setRightButtons(ResultButtonName... names) {
        List<ResultButtonName> foundNames = new ArrayList<>();
        for (var name : names) {
            if (isRegistered(name)) {
                foundNames.add(name);
            }
        }
        modifiableRightButtons.setAll(foundNames);
    }

    @Override
    public void setButtonDisabled(ResultButtonName name, boolean value) {
        if (isRegistered(name)) {
            modifiableDisabledByName.put(name, value);
        }
    }

    @Override
    public void setButtonDefault(ResultButtonName name, boolean value) {
        if (isRegistered(name)) {
            modifiableDefaultByName.put(name, value);
        }
    }

    @Override
    public Optional<Boolean> getButtonDisabled(ResultButtonName name) {
        return Optional.ofNullable(disabledByName.get(name));
    }

    @Override
    public Optional<Boolean> getButtonDefault(ResultButtonName name) {
        return Optional.ofNullable(defaultByName.get(name));
    }

    @Override
    public Consumer<ResultButtonName> getOnResult() {
        return onResult;
    }

    @Override
    public void setOnResult(Consumer<ResultButtonName> resultAction) {
        this.onResult = resultAction;
    }

    @Override
    protected DialogHistory getHistory() {
        return (DialogHistory) super.getHistory();
    }

    protected void onResult(ResultButtonName name) {
        if (this.onResult != null) {
            this.onResult.accept(name);
        }
    }

    void onButtonRegistered(ResultButtonName name, boolean isDefault, boolean disabled) {
        modifiableDisabledByName.put(name, disabled);
        modifiableDefaultByName.put(name, isDefault);
    }

    void onButtonUnregistered(ResultButtonName name) {
        modifiableDisabledByName.remove(name);
        modifiableDefaultByName.remove(name);
    }

    ObservableMap<ResultButtonName, Boolean> getDisabledByName() {
        return disabledByName;
    }

    ObservableMap<ResultButtonName, Boolean> getDefaultByName() {
        return defaultByName;
    }

    private boolean isRegistered(ResultButtonName name) {
        return disabledByName.containsKey(name);
    }
}
