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

import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.shellfx.core.tab.TabConfig;
import com.techsenger.shellfx.devtools.shared.ToolBarConfig;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabConfig extends TabConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private Map<AttributeCategory, Boolean> categoryExpansion = new HashMap<>();

    private ToolBarConfig nodeToolBar = new ToolBarConfig();

    private ToolBarConfig propertyToolBar = new ToolBarConfig();

    private EditorDialogConfig editorDialog = new EditorDialogConfig();

    public NodeTabConfig() {
        for (var category : AttributeCategory.values()) {
            categoryExpansion.put(category, Boolean.FALSE);
        }
    }

    public Map<AttributeCategory, Boolean> getCategoryExpansion() {
        return categoryExpansion;
    }

    public void setCategoryExpansion(Map<AttributeCategory, Boolean> categoryExpansion) {
        this.categoryExpansion = categoryExpansion;
    }

    public ToolBarConfig getNodeToolBar() {
        return nodeToolBar;
    }

    public void setNodeToolBar(ToolBarConfig nodeToolBar) {
        this.nodeToolBar = nodeToolBar;
    }

    public ToolBarConfig getPropertyToolBar() {
        return propertyToolBar;
    }

    public void setPropertyToolBar(ToolBarConfig propertyToolBar) {
        this.propertyToolBar = propertyToolBar;
    }

    public EditorDialogConfig getEditorDialog() {
        return editorDialog;
    }

    public void setEditorDialog(EditorDialogConfig editorDialog) {
        this.editorDialog = editorDialog;
    }
}
