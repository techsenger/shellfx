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

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.layout.LayoutComponentNames;

/**
 * Placeholder is a temporary component inserted into the layout at the calculated drop position while the dock is
 * still attached to its original parent. It preserves the structure of the layout tree so that the dock can be safely
 * removed from its old position and then inserted into the reserved place without losing the insertion context.
 *
 * @author Pavel Castornii
 */
class PlaceholderComponent extends TabDockComponent<PlaceholderView> {

    PlaceholderComponent(PlaceholderView view, DockLayoutComponent<?> layout) {
        super(view, layout);
    }

     @Override
    public Name getName() {
        return LayoutComponentNames.PLACEHOLDER;
    }
}
