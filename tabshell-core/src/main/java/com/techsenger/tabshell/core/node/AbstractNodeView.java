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

package com.techsenger.tabshell.core.node;

import com.techsenger.mvvm4fx.core.AbstractChildView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Scene;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractNodeView<T extends AbstractNodeViewModel> extends AbstractChildView<T>
        implements NodeView<T> {

    public AbstractNodeView(T viewModel) {
        super(viewModel);
    }

    @Override
    protected ReadOnlyObjectProperty<Scene> sceneProperty() {
        return getNode().sceneProperty();
    }

    protected Scene getScene() {
        return sceneProperty().get();
    }
}
