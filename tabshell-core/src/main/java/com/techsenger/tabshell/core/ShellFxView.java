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
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.window.WindowFxView;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * There can be only one instance of Shell in VirtualMachine.
 *
 * @author Pavel Castornii
 */
public interface ShellFxView<P extends ShellPresenter<?>> extends WindowFxView<P>, ShellView {

    interface Composer extends WindowFxView.Composer, ShellView.Composer {

        void addWorkspace(AreaFxView<?> workspace);

        void removeWorkspace();

        AreaFxView<?> getWorkspace();

        <T extends AreaFxView<?>> T getWorkspace(Class<T> clazz);

        /**
         * Defines the component that is currently forms the menu in the Shell.
         * @return
         */
        ReadOnlyObjectProperty<ParentFxView<?>> menuAwareProperty();

        /**
         * Returns the value of {@link #menuAwareProperty()}.
         * @return
         */
        ParentFxView<?> getMenuAware();
    }

    @Override
    Composer getComposer();
}
