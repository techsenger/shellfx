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

import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.area.AreaView;
import javafx.geometry.Side;

/**
 *
 * @author Pavel Castornii
 */
public interface DockHostView extends AreaView {

    interface Composer extends AreaView.Composer {

        SplitSpacePort getRootPort();

        AreaPort getMainPort();

        void showBar(Side side);

        void hideBar(Side side);

        SideBarPort getRightBarPort();

        SideBarPort getBottomBarPort();

        SideBarPort getLeftBarPort();

        SideBarPort getBarPort(Side side);

        SideBarPolicy getRightBarPolicy();

        void setRightBarPolicy(SideBarPolicy policy);

        SideBarPolicy getBottomBarPolicy();

        void setBottomBarPolicy(SideBarPolicy policy);

        SideBarPolicy getLeftBarPolicy();

        void setLeftBarPolicy(SideBarPolicy policy);

        SideBarPolicy getBarPolicy(Side side);

        TabPopupPort getRightPopupPort();

        TabPopupPort getBottomPopupPort();

        TabPopupPort getLeftPopupPort();
    }

    @Override
    Composer getComposer();
}
