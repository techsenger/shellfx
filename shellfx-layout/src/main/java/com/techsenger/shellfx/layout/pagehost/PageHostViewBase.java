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

package com.techsenger.shellfx.layout.pagehost;

import com.techsenger.shellfx.core.area.AreaView;
import com.techsenger.shellfx.core.page.PagePort;
import com.techsenger.shellfx.shared.find.ResultFindPort;

/**
 *
 * @author Pavel Castornii
 */
public interface PageHostViewBase extends AreaView {

    interface Composer extends AreaView.Composer {

        ResultFindPort getFindPanelPort();

        PagePort getSelectedPagePort();
    }

    @Override
    Composer getComposer();

    void setDividerPosition(double pos);

    void setForwardDisabled(boolean disabled);

    void setBackDisabled(boolean disabled);
}
