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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.layout.LayoutComponents;
import com.techsenger.tabshell.layout.tabhost.TabHostComposer;

/**
 * Placeholder is a temporary component inserted into the layout at the calculated drop position while the dock is
 * still attached to its original parent. It preserves the structure of the layout tree so that the dock can be safely
 * removed from its old position and then inserted into the reserved place without losing the insertion context.
 *
 * @author Pavel Castornii
 */
class PlaceholderPresenter extends TabDockPresenter<PlaceholderView, TabHostComposer> {

    PlaceholderPresenter(PlaceholderView view) {
        super(view);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(LayoutComponents.PLACEHOLDER);
    }
}
