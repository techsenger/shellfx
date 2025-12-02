/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.web;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.ComponentViewModel;
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.web.model.UrlUtils;
import com.techsenger.tabshell.web.style.WebIcons;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabViewModel extends AbstractShellTabViewModel {

    public interface Composer extends ComponentViewModel.Composer {

        WebToolBarViewModel getToolBar();
    }

    private final ObservableSource<String> urlSource = new SimpleObservableSource<>();

    private final StringProperty pageTitle = new SimpleStringProperty();

    public WebBrowserTabViewModel(ShellViewModel shell) {
        super(shell);
        setIcon(WebIcons.WEB_BROWSER);
        setTitle("Web Browser");
    }

    @Override
    public WebBrowserTabViewModel.Composer getComposer() {
        return (WebBrowserTabViewModel.Composer) super.getComposer();
    }

    public String getPageTitle() {
        return pageTitle.get();
    }

    public void setPageTitle(String title) {
        pageTitle.set(title);
    }

    public StringProperty pageTitleProperty() {
        return pageTitle;
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(WebComponentNames.WEB_BROWSER_TAB);
    }

    @Override
    protected void initialize() {
        super.initialize();
        pageTitle.addListener((ov, oldV, newV) -> {
            if (newV != null) {
                setTitle(newV);
                setTooltip(newV);
            }
        });
    }

    protected void load() {
        var toolBar = getComposer().getToolBar();
        var url = toolBar.getUrl();
        if (url != null) {
            var normalizedUrl = UrlUtils.normalize(url);
            if (!normalizedUrl.isEmpty() && UrlUtils.isValid(normalizedUrl)) {
                url = normalizedUrl;
            } else {
                url = UrlUtils.getSearch(url);
            }
            getComposer().getToolBar().setUrl(url);
            urlSource.next(url);
        }
    }

    ObservableSource<String> getUrlSource() {
        return urlSource;
    }
}
