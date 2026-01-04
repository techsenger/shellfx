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

package com.techsenger.tabshell.jfx.stylesheet;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class StylesheetDataItem implements StylesheetItem {

    private final String name;

    private List<StylesheetDataItem> children = Collections.EMPTY_LIST;

    private List<String> stylesheets = Collections.EMPTY_LIST;

    public StylesheetDataItem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public List<StylesheetDataItem> getChildren() {
        return children;
    }

    public void setChildren(List<StylesheetDataItem> children) {
        this.children = children;
    }

    public List<String> getStylesheets() {
        return stylesheets;
    }

    public void setStylesheets(List<String> stylesheets) {
        this.stylesheets = stylesheets;
    }
}
