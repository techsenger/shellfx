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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.tab.TabView;
import com.techsenger.shellfx.devtools.ToolBarPort;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public interface NodeTabView extends TabView {

    interface Composer extends TabView.Composer {

        ToolBarPort getNodeToolBarPort();

        ToolBarPort getPropertyToolBarPort();

        DialogPort openViewerDialog(ViewerDialogParams params);

        EditorDialogPort openEditorDialog(EditorDialogParams params);
    }

    @Override
    Composer getComposer();

    void selectWindow(int uid);

    void selectRoot();

    void refreshNodes();

    void selectNode(Element node, boolean afterDataUpdate);

    void refreshNodeIndex();

    void focusProperties();

    void clearProperties();

    void setReadOnlyByProperty(Map<String, Boolean> map);

    void addProperties(AttributeCategory category, boolean expanded, List<PropertyItem> items);

    void selectPropertyCategory(AttributeCategory cat);

    void selectProperty(PropertyItem item);
}
