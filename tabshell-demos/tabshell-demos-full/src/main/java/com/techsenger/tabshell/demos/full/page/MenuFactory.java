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

package com.techsenger.tabshell.demos.full.page;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.demos.full.DemoComponents;
import com.techsenger.tabshell.layout.pagehost.Page;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.toolkit.core.function.Factory;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;

/**
 *
 * @author Pavel Castornii
 */
final class MenuFactory {

    enum PageType {
        DIALOG, TAB
    }

    private static final class PageItem implements Page {

        private final ComponentName name;

        private final String title;

        private final Factory<PageFxView<?>> factory;

        private PageItem(String title, ComponentName name, Factory<PageFxView<?>> factory) {
            this.name = name;
            this.title = title;
            this.factory = factory;
        }

        @Override
        public ComponentName getName() {
            return name;
        }

        @Override
        public String toString() {
            return title;
        }

        @Override
        public Factory<? extends PageFxView<?>> getFactory() {
            return this.factory;
        }
    }

    static TreeItem<Page> create(PageType parentType) {
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
                    padding = new Insets(SizeConstants.INSET, SizeConstants.INSET, 0, SizeConstants.INSET);
                } else {
                    padding = new Insets(SizeConstants.INSET);
                }
                var view = new DemoPageFxView("Title " + index, padding);
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
        var rootItem = new TreeItem<Page>(null);
        var item0 = new TreeItem<Page>(new PageItem("Page 0", DemoComponents.DEMO_PAGE_0,
                new PageFactoryImpl(DemoComponents.DEMO_PAGE_0, 0)));
        var item1 = new TreeItem<Page>(new PageItem("Page 1", DemoComponents.DEMO_PAGE_1,
                new PageFactoryImpl(DemoComponents.DEMO_PAGE_1, 1)));
        item1.setExpanded(true);
        var item2 = new TreeItem<Page>(new PageItem("Page 2", DemoComponents.DEMO_PAGE_2,
                new PageFactoryImpl(DemoComponents.DEMO_PAGE_2, 2)));
        rootItem.getChildren().addAll(item0, item1);
        item1.getChildren().add(item2);
        return rootItem;
    }

    private MenuFactory() {
        // empty
    }
}
