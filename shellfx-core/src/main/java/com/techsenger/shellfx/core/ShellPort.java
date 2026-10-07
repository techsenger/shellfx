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

package com.techsenger.shellfx.core;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.window.HostWindowPort;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface ShellPort extends HostWindowPort, MenuAwarePort {

    interface ComposerAccess extends HostWindowPort.ComposerAccess {

        AreaPort getWorkspacePort();

        @Nullable MenuAwarePort getMenuAwarePort();

        /**
         * Defines the port of the current menu aware component, the one the menu controls may observe; {@code null}
         * when there is none.
         *
         * @return
         */
        ReadOnlyObjectProperty<MenuAwarePort> menuAwarePortProperty();
    }

    @Override
    ComposerAccess getComposerAccess();

    /**
     * Returns the part of the shell context that is available to view models.
     *
     * @return
     */
    ShellViewModelContext getContext();

    /**
     * Returns the context of the shell as an instance of the specified class using type casting.
     *
     * @return
     */
    <T extends ShellViewModelContext> T getContext(Class<T> contextClass);
}
