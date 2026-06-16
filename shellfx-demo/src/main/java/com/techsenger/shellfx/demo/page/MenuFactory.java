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

package com.techsenger.shellfx.demo.page;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.page.DefaultPageDescriptor;
import com.techsenger.shellfx.core.page.DefaultTreePageDescriptor;
import com.techsenger.shellfx.core.page.PageDescriptor;
import com.techsenger.shellfx.core.page.PageFactory;
import com.techsenger.shellfx.core.page.PageFxView;
import com.techsenger.shellfx.core.page.PageItem;
import com.techsenger.shellfx.core.page.PageParams;
import com.techsenger.shellfx.core.page.TreePageDescriptor;
import com.techsenger.shellfx.demo.DemoComponents;
import com.techsenger.shellfx.material.style.Spacing;
import java.util.List;
import javafx.geometry.Insets;

/**
 *
 * @author Pavel Castornii
 */
final class MenuFactory {

    /**
     * For demo one component is used both for tab and dialog. Besides the page can be shown differently, so this
     * factory is required.
     */
    private static class PageFactoryImpl implements PageFactory {

        private final PageHostParent pageHostParent;

        private final ComponentName name;

        PageFactoryImpl(PageHostParent pageHostParent, ComponentName name) {
            this.pageHostParent = pageHostParent;
            this.name = name;
        }

        @Override
        public PageFxView<?> createAndInitialize(PageItem item) {
            Insets padding;
            if (pageHostParent == PageHostParent.DIALOG) {
                padding = new Insets(0, Spacing.getHorizontal(), 0, Spacing.getHorizontal());
            } else {
                padding = new Insets(0, Spacing.getHorizontal(), Spacing.getVertical(), Spacing.getHorizontal());
            }
            var view = new DemoPageFxView(padding);
            var params = new PageParams(item);
            var presenter = new DemoPagePresenter(view, params) {
                @Override
                protected ComponentDescriptor createDescriptor() {
                    return new ComponentDescriptor(name);
                }
            };
            presenter.initialize();
            return view;
        }
    }

    static List<PageDescriptor> createMenu(PageHostParent parentType) {
        return List.of(
                new DefaultPageDescriptor("Page 0", new PageFactoryImpl(parentType, DemoComponents.PAGE_0)),
                new DefaultPageDescriptor("Page 1", new PageFactoryImpl(parentType, DemoComponents.PAGE_1)),
                new DefaultPageDescriptor("Page 2", new PageFactoryImpl(parentType, DemoComponents.PAGE_2)),
                new DefaultPageDescriptor("Page 3", new PageFactoryImpl(parentType, DemoComponents.PAGE_3)),
                new DefaultPageDescriptor("Page 4", new PageFactoryImpl(parentType, DemoComponents.PAGE_4)),
                new DefaultPageDescriptor("Page 5", new PageFactoryImpl(parentType, DemoComponents.PAGE_5))
        );
    }

    static TreePageDescriptor createTreeMenu(PageHostParent parentType) {
        var root = new DefaultTreePageDescriptor();
        var item0 = new DefaultTreePageDescriptor("Page 0", new PageFactoryImpl(parentType, DemoComponents.PAGE_0));
        root.addChild(item0);
        var item1 = new DefaultTreePageDescriptor("Page 1", new PageFactoryImpl(parentType, DemoComponents.PAGE_1));
        item0.addChild(item1);
        var item2 = new DefaultTreePageDescriptor("Page 2", new PageFactoryImpl(parentType, DemoComponents.PAGE_2));
        item1.addChild(item2);

        var item3 = new DefaultTreePageDescriptor("Page 3", new PageFactoryImpl(parentType, DemoComponents.PAGE_3));
        root.addChild(item3);
        var item4 = new DefaultTreePageDescriptor("Page 4", new PageFactoryImpl(parentType, DemoComponents.PAGE_4));
        item3.addChild(item4);

        var item5 = new DefaultTreePageDescriptor("Page 5", new PageFactoryImpl(parentType, DemoComponents.PAGE_5));
        root.addChild(item5);
        return root;
    }

    private MenuFactory() {
        // empty
    }
}
