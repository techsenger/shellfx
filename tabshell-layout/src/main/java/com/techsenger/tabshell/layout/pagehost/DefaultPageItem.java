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

package com.techsenger.tabshell.layout.pagehost;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.toolkit.core.function.Factory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultPageItem implements PageItem {

    private Icon<?> icon;

    private String text;

    private ComponentName name;

    private Factory<? extends PageFxView<?>> factory;

    private PageItem parent;

    private final List<PageItem> children = new ArrayList<>();

    public DefaultPageItem() {

    }

    public DefaultPageItem(String text, ComponentName name, Factory<? extends PageFxView<?>> factory) {
        this(null, text, name, factory);
    }

    public DefaultPageItem(Icon<?> icon, String text, ComponentName name, Factory<? extends PageFxView<?>> factory) {
        this.icon = icon;
        this.text = text;
        this.name = name;
        this.factory = factory;
    }

    @Override
    public Icon<?> getIcon() {
        return icon;
    }

    public void setIcon(Icon<?> icon) {
        this.icon = icon;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public ComponentName getName() {
        return name;
    }

    public void setName(ComponentName name) {
        this.name = name;
    }

    @Override
    public Factory<? extends PageFxView<?>> getFactory() {
        return factory;
    }

    public void setFactory(Factory<? extends PageFxView<?>> factory) {
        this.factory = factory;
    }

    @Override
    public List<PageItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public PageItem getParent() {
        return this.parent;
    }

    public void addChild(DefaultPageItem item) {
        this.children.add(item);
        item.setParent(this);
    }

    void setParent(PageItem parent) {
        this.parent = parent;
    }
}
