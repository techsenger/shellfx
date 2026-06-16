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

package com.techsenger.shellfx.demo.ide;

import com.techsenger.shellfx.core.popup.PopupContainerPort;
import com.techsenger.shellfx.core.tab.TabPort;

/**
 *
 * @author Pavel Castornii
 */
public interface IdeMainTabPort extends TabPort, PopupContainerPort {

    interface Composer extends TabPort.Composer, PopupContainerPort.Composer {

    }

    interface ViewAccess extends TabPort.ViewAccess, PopupContainerPort.ViewAccess {

        Composer getComposer();
    }

    ViewAccess getViewAccess();
}
