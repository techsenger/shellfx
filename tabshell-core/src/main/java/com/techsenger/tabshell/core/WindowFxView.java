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

package com.techsenger.tabshell.core;

import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.dialog.DialogContainerFxView;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowFxView<P extends WindowPresenter<?>> extends ParentFxView<P>, DialogContainerFxView<P>,
        WindowView {

    interface Composer extends ParentFxView.Composer, DialogContainerFxView.Composer, WindowView.Composer {

        /**
         * Defines the component that currently has the focus.
         *
         * @return
         */
        ReadOnlyObjectProperty<ParentFxView<?>> focusedProperty();

        /**
         * Returns the value of {@link #focusedProperty()}.
         *
         * @return
         */
        ParentFxView<?> getFocused();
    }

    @Override
    Composer getComposer();

    Stage getWindow();
}
