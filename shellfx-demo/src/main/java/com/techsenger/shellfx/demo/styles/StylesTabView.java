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

package com.techsenger.shellfx.demo.styles;

import atlantafx.base.theme.Styles;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.page.DefaultPageDescriptor;
import com.techsenger.shellfx.core.page.PageDescriptor;
import com.techsenger.shellfx.core.page.PageFactory;
import com.techsenger.shellfx.core.page.PageItem;
import com.techsenger.shellfx.core.page.PageParams;
import com.techsenger.shellfx.core.page.PageView;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.layout.pagehost.PageHostParams;
import com.techsenger.shellfx.layout.pagehost.PageHostView;
import com.techsenger.shellfx.layout.pagehost.PageHostViewModel;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class StylesTabView<VM extends StylesTabViewModel<?>> extends AbstractTabView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer {

        private static final class VariantPageFactory implements PageFactory<PageItem> {

            @Override
            public PageView<?> create(PageItem t) {
                var viewModel = new VariantPageViewModel<>(new PageParams(t));
                var view = new VariantPageView<>(viewModel);
                view.initialize();
                return view;
            }
        }

        private static final class SetPageFactory implements PageFactory<PageItem> {

            private final String styleName;

            SetPageFactory(String styleName) {
                this.styleName = styleName;
            }

            @Override
            public PageView<?> create(PageItem t) {
                var viewModel = new SetPageViewModel<>(new PageParams(t));
                var view = new SetPageView<>(viewModel, styleName);
                view.initialize();
                return view;
            }
       }

        private final List<PageDescriptor> pages = List.of(
                new DefaultPageDescriptor("Density Variants", new VariantPageFactory()),
                new DefaultPageDescriptor("Default Set", new SetPageFactory(null)),
                new DefaultPageDescriptor("Medium Set", new SetPageFactory(StyleClasses.DENSITY_M)),
                new DefaultPageDescriptor("Small Set", new SetPageFactory(StyleClasses.DENSITY_S)),
                new DefaultPageDescriptor("Extra Small Set", new SetPageFactory(StyleClasses.DENSITY_XS)));

        @Override
        public void compose() {
            super.compose();

            var params = new PageHostParams(null);
            var pageHostViewModel = new PageHostViewModel<>(params);
            var pageHost = new PageHostView<>(pageHostViewModel) {
                {
                    getPageListView().getStyleClass().add(Styles.DENSE);
                }
            };
            pageHost.initialize();

            pageHost.getComposer().setPages(pages);
            getModifiableChildren().add(pageHost);

            getContentBox().getChildren().add(pageHost.getNode());
            VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
            pageHostViewModel.selectPage(0);
            pageHostViewModel.setDividerPosition(0.25);
        }
    }

    public StylesTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
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
        return new StylesTabView.Composer();
    }
}
