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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.tabshell.core.area.AreaMediator;
import com.techsenger.tabshell.core.area.AreaViewModel;
import javafx.beans.property.ReadOnlyObjectProperty;

/**
 *
 * @author Pavel Castornii
 */
public interface DockLayoutMediator extends AreaMediator {

    ReadOnlyObjectProperty<SplitSpaceViewModel> rootProperty();

    SplitSpaceViewModel getRoot();

    ReadOnlyObjectProperty<AreaViewModel<?>> mainProperty();

    AreaViewModel<?> getMain();

    ReadOnlyObjectProperty<SideBarViewModel<?>> rightSideBarProperty();

    SideBarViewModel<?> getRightBar();

    ReadOnlyObjectProperty<SideBarViewModel<?>> bottomSideBarProperty();

    SideBarViewModel<?> getBottomBar();

    ReadOnlyObjectProperty<SideBarViewModel<?>> leftSideBarProperty();

    SideBarViewModel<?> getLeftBar();

}
