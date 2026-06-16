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

import com.techsenger.shellfx.core.page.TreePageItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class FilteredTreePageItem {

    private final TreePageItem original;

    private final List<FilteredTreePageItem> children = new ArrayList<>();

    private final boolean matched;

    public FilteredTreePageItem(TreePageItem original, boolean matched) {
        this.original = original;
        this.matched = matched;
    }

    public TreePageItem getOriginal() {
        return original;
    }

    public List<FilteredTreePageItem> getChildren() {
        return children;
    }

    public boolean isMatched() {
        return matched;
    }
}
