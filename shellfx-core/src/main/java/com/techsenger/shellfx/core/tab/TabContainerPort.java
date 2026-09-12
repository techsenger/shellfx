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

package com.techsenger.shellfx.core.tab;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.core.ChildPort;
import java.util.List;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface TabContainerPort extends ChildPort {

    interface ComposerAccess extends ChildPort.ComposerAccess {

        /**
         * Returns an unmodifiable list of tabs. A new list instance is created on each call.
         *
         * @return
         */
        @Unmodifiable List<? extends TabPort> getTabPorts();

        /**
         * Returns selected tab port.
         */
        TabPort getSelectedTabPort();

        ReadOnlyObjectProperty<? extends TabPort> selectedTabPortProperty();

        /**
         * Returns the index of the currently selected tab.
         *
         * @return zero-based index of the selected tab, or {@code -1} if no tab is selected
         */
        int getSelectedTabIndex();

        ReadOnlyIntegerProperty selectedTabIndexProperty();

        /**
         * Returns the number of tabs currently contained in the tab container.
         *
         * @return number of tabs
         */
        int getTabCount();

        ReadOnlyIntegerProperty tabCountProperty();
    }

    @Override
    ComposerAccess getComposerAccess();
}
