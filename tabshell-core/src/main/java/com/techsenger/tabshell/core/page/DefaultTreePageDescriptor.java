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

package com.techsenger.tabshell.core.page;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.material.icon.Icon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultTreePageDescriptor extends DefaultPageItem implements TreePageDescriptor {

    private DefaultTreePageDescriptor parent;

    private final List<DefaultTreePageDescriptor> children = new ArrayList<>();

    private final PageFactory<TreePageItem> factory;

    public DefaultTreePageDescriptor() {
        this(null, null, null);
    }

    public DefaultTreePageDescriptor(String text, PageFactory<TreePageItem> factory) {
        this(null, text, factory);
    }

    public DefaultTreePageDescriptor(Icon<?> icon, String text, PageFactory<TreePageItem> factory) {
        super(icon, text);
        this.factory = factory;
    }

    @Override
    public PageFactory<TreePageItem> getFactory() {
        return factory;
    }

    @Override
    public TreePageDescriptor getParent() {
        return this.parent;
    }

    @Override
    public @Unmodifiable List<? extends TreePageDescriptor> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public void addChild(DefaultTreePageDescriptor item) {
        this.children.add(item);
        item.setParent(this);
    }

    private void setParent(DefaultTreePageDescriptor parent) {
        this.parent = parent;
    }
}
