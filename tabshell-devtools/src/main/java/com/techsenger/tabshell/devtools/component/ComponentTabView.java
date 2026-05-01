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

package com.techsenger.tabshell.devtools.component;

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.tabshell.core.tab.TabView;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.dialogs.namevalue.NameValueDialogPort;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public interface ComponentTabView extends TabView {

    interface Composer extends TabView.Composer {

        ToolBarPort getComponentToolBarPort();

        ToolBarPort getInspectorToolBarPort();

        NameValueDialogPort addNameValueDialog(String nameCaption, String valueCaption);
    }

    @Override
    Composer getComposer();

    void setRootComponent(ComponentItem item);

    /**
     * Contains the indexes of the child nodes. The first index (0) is the root. The second index is the root child
     * index. The third index is the index of the child of the root child and so on. If the list is empty then no
     * item is selected.
     *
     * @param path
     */
    void selectComponent(List<Integer> path);

    void selectComponent(Element node);

    void selectRootComponent();

    void updateInspector(List<InspectorItem> items, Map<InspectorCategory, Boolean> expandedByCategory);
}
