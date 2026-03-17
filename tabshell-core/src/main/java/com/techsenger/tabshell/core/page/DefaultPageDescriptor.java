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
public class DefaultPageDescriptor implements PageDescriptor {

    private final Icon<?> icon;

    private final String text;

    private final PageFactory factory;

    private PageDescriptor parent;

    private final List<PageDescriptor> children = new ArrayList<>();

    public DefaultPageDescriptor() {
        this(null, null, null);
    }

    public DefaultPageDescriptor(String text, PageFactory factory) {
        this(null, text, factory);
    }

    public DefaultPageDescriptor(Icon<?> icon, String text, PageFactory factory) {
        this.factory = factory;
        this.icon = icon;
        this.text = text;
    }

    @Override
    public PageFactory getFactory() {
        return factory;
    }

    @Override
    public PageDescriptor getParent() {
        return this.parent;
    }

    @Override
    public @Unmodifiable List<PageDescriptor> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public void addChild(DefaultPageDescriptor item) {
        this.children.add(item);
        item.setParent(this);
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    @Override
    public String getText() {
        return text;
    }

    void setParent(DefaultPageDescriptor parent) {
        this.parent = parent;
    }
}
