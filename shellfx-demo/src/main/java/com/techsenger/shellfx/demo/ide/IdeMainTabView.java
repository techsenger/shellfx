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

import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.popup.PopupContainerView;
import com.techsenger.shellfx.core.popup.PopupPort;
import com.techsenger.shellfx.core.tab.TabView;

/**
 *
 * @author Pavel Castornii
 */
public interface IdeMainTabView extends TabView, PopupContainerView, IdeMainTabPort.ViewAccess {

    interface Composer extends TabView.Composer, PopupContainerView.Composer, IdeMainTabPort.Composer {

        DialogPort openDemoDialog(boolean resizable, DialogParams params);

        PopupPort openDemoPopup(OverlayScope scope);
    }

    @Override
    Composer getComposer();
}
