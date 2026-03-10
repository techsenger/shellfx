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

package com.techsenger.tabshell.demo.page;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.layout.pagehost.DefaultPageDescriptor;
import com.techsenger.tabshell.layout.pagehost.PageDescriptor;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.toolkit.core.function.Factory;
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
final class MenuFactory {

    enum PageType {
        DIALOG, TAB
    }

    static PageDescriptor create(PageType parentType) {
        // for demo one component is used, so this factory is required
        class PageFactoryImpl implements Factory<PageFxView<?>> {

            private final ComponentName name;

            private final int index;

            PageFactoryImpl(ComponentName name, int index) {
                this.name = name;
                this.index = index;
            }

            @Override
            public PageFxView<?> create() {
                Insets padding;
                if (parentType == PageType.DIALOG) {
                    padding = new Insets(0, Spacing.HORIZONTAL, 0, Spacing.HORIZONTAL);
                } else {
                    padding = new Insets(0, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL);
                }
                var view = new DemoPageFxView(padding, index);
                var presenter = new DemoPagePresenter(view) {
                    @Override
                    protected Descriptor createDescriptor() {
                        return new Descriptor(name);
                    }
                };
                return view;
            }
        }

        // items for menu
        var root = new DefaultPageDescriptor();
        var item0 = new DefaultPageDescriptor("Page 0", DemoComponents.PAGE_0,
                new PageFactoryImpl(DemoComponents.PAGE_0, 0));
        root.addChild(item0);
        var item1 = new DefaultPageDescriptor("Page 1", DemoComponents.PAGE_1,
                new PageFactoryImpl(DemoComponents.PAGE_1, 1));
        item0.addChild(item1);
        var item2 = new DefaultPageDescriptor("Page 2", DemoComponents.PAGE_2,
                new PageFactoryImpl(DemoComponents.PAGE_2, 2));
        item1.addChild(item2);
        return root;
    }

    private MenuFactory() {
        // empty
    }
}
