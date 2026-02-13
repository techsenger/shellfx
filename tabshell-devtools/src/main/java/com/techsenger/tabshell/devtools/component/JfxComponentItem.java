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

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.ParentFxView;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Pavel Castornii
 */
public class JfxComponentItem implements ComponentItem {

    private final ParentFxView<?> view;

    public JfxComponentItem(ParentFxView<?> view) {
        this.view = view;
    }

    @Override
    public ComponentName getName() {
        return view.getDescriptor().getName();
    }

    @Override
    public String getText() {
        return getName() + " uuid=\"" + getUuid() + "\"";
    }

    @Override
    public UUID getUuid() {
        return view.getDescriptor().getUuid();
    }

    @Override
    public List<ComponentItem> getChildren() {
        return view.getChildren().stream().map(v -> new JfxComponentItem(v)).map(t -> (ComponentItem) t).toList();
    }

    /**
     * Only Shell doesn't provide node.
     *
     * @return
     */
    Object getNode() {
        if (this.view instanceof ChildFxView<?> child) {
            return child.getNode();
        } else {
            return null;
        }
    }
}
