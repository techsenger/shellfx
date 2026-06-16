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
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class ViewerDialogParams extends DialogParams {

    private final Element node;

    private final PropertyItem item;

    private final String declaringClassName;

    private final Consumer<String> linkOpener;

    public ViewerDialogParams(WindowType windowType, AppearanceSettings settings,
            Element node, PropertyItem item, String declaringClassName,
            Consumer<String> linkOpener) {
        super(windowType, settings);
        this.node = node;
        this.item = item;
        this.declaringClassName = declaringClassName;
        this.linkOpener = linkOpener;
    }

    public Element getNode() {
        return node;
    }

    public PropertyItem getItem() {
        return item;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public Consumer<String> getLinkOpener() {
        return linkOpener;
    }

    @Override
    protected void validate() {
        super.validate();
        Objects.requireNonNull(node);
        Objects.requireNonNull(item);
    }
}
