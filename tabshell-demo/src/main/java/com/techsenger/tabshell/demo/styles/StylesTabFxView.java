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

package com.techsenger.tabshell.demo.styles;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.page.DefaultPageDescriptor;
import com.techsenger.tabshell.core.page.PageDescriptor;
import com.techsenger.tabshell.core.page.PageFactory;
import com.techsenger.tabshell.core.page.PageFxView;
import com.techsenger.tabshell.core.page.PageItem;
import com.techsenger.tabshell.core.page.PageParams;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.devtools.stylesheet.StylesheetTabPresenter;
import com.techsenger.tabshell.layout.pagehost.PageHostFxView;
import com.techsenger.tabshell.layout.pagehost.PageHostParams;
import com.techsenger.tabshell.layout.pagehost.PageHostPresenter;
import com.techsenger.tabshell.material.style.StyleClasses;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesTabFxView extends AbstractTabFxView<StylesheetTabPresenter<?>> implements StylesTabView {

    public class Composer extends AbstractTabFxView<StylesheetTabPresenter<?>>.Composer {

        private static final class VariantPageFactory implements PageFactory<PageItem> {

            @Override
            public PageFxView<?> createAndInitialize(PageItem t) {
                var view = new VariantPageFxView();
                var presenter = new VariantPagePresenter(view, new PageParams(t));
                presenter.initialize();
                return view;
            }
        }

        private static final class SetPageFactory implements PageFactory<PageItem> {

            private final String styleName;

            private final ComponentName name;

            SetPageFactory(String styleName, ComponentName name) {
                this.styleName = styleName;
                this.name = name;
            }

            @Override
            public PageFxView<?> createAndInitialize(PageItem t) {
                var view = new SetPageFxView(styleName);
                var presenter = new SetPagePresenter(view, new PageParams(t)) {
                    @Override
                    protected ComponentDescriptor createDescriptor() {
                        return new ComponentDescriptor(name);
                    }
                };
                presenter.initialize();
                return view;
            }
       }

        private final List<PageDescriptor> pages = List.of(
                new DefaultPageDescriptor("Style Variants", new VariantPageFactory()),
                new DefaultPageDescriptor("Default Set", new SetPageFactory(null, DemoComponents.SET_PAGE_1)),
                new DefaultPageDescriptor("Dense Set",
                        new SetPageFactory(StyleClasses.DENSE, DemoComponents.SET_PAGE_2)),
                new DefaultPageDescriptor("Compact Set",
                        new SetPageFactory(StyleClasses.COMPACT, DemoComponents.SET_PAGE_3)),
                new DefaultPageDescriptor("Compressed Set",
                        new SetPageFactory(StyleClasses.COMPRESSED, DemoComponents.SET_PAGE_4)));

        @Override
        public void compose() {
            super.compose();
            var pageHost = new PageHostFxView<>();
            var params = new PageHostParams(null);
            params.setHistoryPolicy(HistoryPolicy.NONE);
            var hostPresenter = new PageHostPresenter<>(pageHost, params);
            hostPresenter.initialize();
            hostPresenter.setDividerPosition(0.275);
            pageHost.getComposer().setPages(pages);
            getModifiableChildren().add(pageHost);
            getContentBox().getChildren().add(pageHost.getNode());
            VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
            pageHost.getPresenter().selectPage(0);
            pageHost.getPresenter().setDividerPosition(0.25);
        }
    }

    public StylesTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new StylesTabFxView.Composer();
    }
}
