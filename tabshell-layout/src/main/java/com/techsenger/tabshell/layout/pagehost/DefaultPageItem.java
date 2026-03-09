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

    private List<PageItem> children;

    public DefaultPageItem() {

    }

    public DefaultPageItem(String text, ComponentName name, Factory<? extends PageFxView<?>> factory,
            List<PageItem> children) {
        this(null, text, name, factory, children);
    }

    public DefaultPageItem(Icon<?> icon, String text, ComponentName name, Factory<? extends PageFxView<?>> factory,
            List<PageItem> children) {
        this.icon = icon;
        this.text = text;
        this.name = name;
        this.factory = factory;
        this.children = children;
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

    public List<PageItem> getChildren() {
        return children;
    }

    public void setChildren(List<PageItem> children) {
        this.children = children;
    }
}
