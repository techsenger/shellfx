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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.area.AreaPort;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Side;

/**
 * Provides minimal, read-only access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface DockHostPort extends AreaPort {

    interface ComposerAccess extends AreaPort.ComposerAccess {

        @Nullable AreaPort getMainPort();

        @Nullable SideBarPort getRightBarPort();

        @Nullable SideBarPort getBottomBarPort();

        @Nullable SideBarPort getLeftBarPort();

        @Nullable SideBarPort getBarPort(Side side);

        SideBarPolicy getRightBarPolicy();

        SideBarPolicy getBottomBarPolicy();

        SideBarPolicy getLeftBarPolicy();

        SideBarPolicy getBarPolicy(Side side);

        @Nullable TabPopupPort getRightPopupPort();

        @Nullable TabPopupPort getBottomPopupPort();

        @Nullable TabPopupPort getLeftPopupPort();

        @Nullable TabPopupPort getPopupPort(Side side);
    }

    @Override
    ComposerAccess getComposerAccess();

    double getCenterWidth();

    ReadOnlyDoubleProperty centerWidthProperty();

    double getCenterHeight();

    ReadOnlyDoubleProperty centerHeightProperty();
}
