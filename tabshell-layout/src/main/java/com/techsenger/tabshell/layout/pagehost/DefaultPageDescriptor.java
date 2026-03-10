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
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class DefaultPageDescriptor extends AbstractPageInfo implements PageDescriptor {

    private Factory<? extends PageFxView<?>> factory;

    private PageDescriptor parent;

    private final List<PageDescriptor> children = new ArrayList<>();

    public DefaultPageDescriptor() {
        super(null, null, null);
    }

    public DefaultPageDescriptor(String text, ComponentName name, Factory<? extends PageFxView<?>> factory) {
        this(null, text, name, factory);
    }

    public DefaultPageDescriptor(Icon<?> icon, String text, ComponentName name,
            Factory<? extends PageFxView<?>> factory) {
        super(icon, text, name);
        this.factory = factory;
    }

    @Override
    public Factory<? extends PageFxView<?>> getFactory() {
        return factory;
    }

    public void setFactory(Factory<? extends PageFxView<?>> factory) {
        this.factory = factory;
    }

    @Override
    public PageDescriptor getParent() {
        return this.parent;
    }

    @Override
    public List<PageDescriptor> getChildren() {
        return this.children;
    }

    public void addChild(DefaultPageDescriptor item) {
        this.children.add(item);
        item.setParent(this);
    }

    void setParent(DefaultPageDescriptor parent) {
        this.parent = parent;
    }
}
