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

package com.techsenger.tabshell.core.window;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.popup.PopupContainerFxView;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface WindowContainerFxView<P extends WindowContainerPresenter<?>> extends PopupContainerFxView<P> {

    interface Composer extends PopupContainerFxView.Composer, WindowContainerView.Composer {

        /**
         * Adds the specified window component to the component tree.
         *
         * @param window the window component to add
         */
        void addWindow(WindowFxView<?> window);

        /**
         * Removes the specified window component from the component tree.
         *
         * @param window the window component to remove
         */
        void removeWindow(WindowFxView<?> window);

        /**
         * Removes the specified window component from the component tree and deinitializes it.
         *
         * @param window the window component to close
         */
        void closeWindow(WindowFxView<?> window);

        /**
         * Returns an unmodifiable list of windows.
         * @return
         */
        @Unmodifiable List<? extends WindowFxView<?>> getWindows();

        /**
         * Arranges all managed windows according to the specified arrangement strategy.
         * <p>
         * This operation repositions and resizes windows within the available
         * container area based on the given arrangement mode.
         *
         * @param arrangement the arrangement strategy to apply to all windows
         */
        void arrangeWindows(WindowArrangement arrangement);

        /**
         * Maximizes the specified window to fill the available container area.
         * <p>
         * If the window is already maximized, this method has no effect.
         *
         * @param window the window component to maximize
         */
        void maximizeWindow(WindowFxView<?> window);

        /**
         * Restores the specified window to its previous size and position before it was maximized or minimized.
         * <p>
         * If the window is already in its normal state, this method has no effect.
         *
         * @param window the window component to restore
         */
        void restoreWindow(WindowFxView<?> window);
    }

    @Override
    Composer getComposer();
}
