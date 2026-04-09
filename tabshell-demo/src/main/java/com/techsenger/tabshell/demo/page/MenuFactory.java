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
import com.techsenger.tabshell.core.page.DefaultPageDescriptor;
import com.techsenger.tabshell.core.page.PageDescriptor;
import com.techsenger.tabshell.core.page.PageFactory;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
final class MenuFactory {

    enum PageType {
        DIALOG, TAB
    }

    /**
     * For demo one component is used, besides the page can be shown differently, so this factory is required.
     */
    static class PageFactoryImpl implements PageFactory {

        private final PageType parentType;

        private final ComponentName name;

        PageFactoryImpl(PageType parentType, ComponentName name) {
            this.parentType = parentType;
            this.name = name;
        }

        @Override
        public PageFxView<?> createAndInitialize(PageItem<?> item) {
            Insets padding;
            if (parentType == PageType.DIALOG) {
                padding = new Insets(0, Spacing.HORIZONTAL, 0, Spacing.HORIZONTAL);
            } else {
                padding = new Insets(0, Spacing.HORIZONTAL, Spacing.VERTICAL, Spacing.HORIZONTAL);
            }
            var view = new DemoPageFxView(padding);
            var presenter = new DemoPagePresenter(view, item) {
                @Override
                protected Descriptor createDescriptor() {
                    return new Descriptor(name);
                }
            };
            presenter.initialize();
            return view;
        }
    }

    static PageDescriptor create(PageType parentType) {

        // items for menu
        var root = new DefaultPageDescriptor();
        var item0 = new DefaultPageDescriptor("Page 0", new PageFactoryImpl(parentType, DemoComponents.PAGE_0));
        root.addChild(item0);
        var item1 = new DefaultPageDescriptor("Page 1", new PageFactoryImpl(parentType, DemoComponents.PAGE_1));
        item0.addChild(item1);
        var item2 = new DefaultPageDescriptor("Page 2", new PageFactoryImpl(parentType, DemoComponents.PAGE_2));
        item1.addChild(item2);

        var item3 = new DefaultPageDescriptor("Page 3", new PageFactoryImpl(parentType, DemoComponents.PAGE_3));
        root.addChild(item3);
        var item4 = new DefaultPageDescriptor("Page 4", new PageFactoryImpl(parentType, DemoComponents.PAGE_4));
        item3.addChild(item4);
        return root;
    }

    private MenuFactory() {
        // empty
    }
}
