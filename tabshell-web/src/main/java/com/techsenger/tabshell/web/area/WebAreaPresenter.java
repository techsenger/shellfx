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

package com.techsenger.tabshell.web.area;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaComposer;
import com.techsenger.tabshell.core.shelltab.ShellTabPort;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.ImageIcon;
import com.techsenger.tabshell.web.WebComponentNames;
import com.techsenger.tabshell.web.model.UrlUtils;
import com.techsenger.tabshell.web.style.WebIcons;
import com.techsenger.tabshell.web.toolbar.WebToolBarPort;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class WebAreaPresenter<V extends WebAreaView, C extends AreaComposer> extends AbstractAreaPresenter<V, C> {

    private static final Logger logger = LoggerFactory.getLogger(WebAreaPresenter.class);

    protected class Port extends AbstractAreaPresenter.Port implements WebAreaPort {

        private final WebAreaPresenter<?, ?> presenter = WebAreaPresenter.this;

        @Override
        public void navigateBack() {
            var newIndex = getView().getHistoryIndex() - 1;
            if (newIndex >= 0) {
                getView().loadHistory(newIndex);
            }
        }

        @Override
        public void navigateForward() {
            var newIndex = getView().getHistoryIndex() + 1;
            if (newIndex < getView().getHistorySize()) {
                getView().loadHistory(newIndex);
            }
        }

        @Override
        public void reload() {
            presenter.getView().reload();
        }

        @Override
        public void load(String urlStr) {
            if (urlStr != null) {
                var url = UrlUtils.normalize(urlStr);
                if (url == null || !UrlUtils.isValid(url)) {
                    url = UrlUtils.getSearch(urlStr);
                }
                if (url != null) {
                    getView().load(url.toString());
                } else {
                    setDefaultTitleAndIcon();
                }
            }
        }

        @Override
        public String getLocation() {
            return presenter.getView().getLocation();
        }
    }

    private String pageTitle;

    private final Supplier<ShellTabPort> browser;

    private final Supplier<WebToolBarPort> toolBar;

    public WebAreaPresenter(V view, Supplier<ShellTabPort> browser, Supplier<WebToolBarPort> toolBar) {
        super(view);
        this.browser = browser;
        this.toolBar = toolBar;
    }

    @Override
    public WebAreaPort getPort() {
        return (WebAreaPort) super.getPort();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setDefaultTitleAndIcon();
    }

    @Override
    protected void preDeinitialize() {
        super.preDeinitialize();
        getView().clear();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(WebComponentNames.WEB_AREA);
    }

    @Override
    protected Port createPort() {
        return new WebAreaPresenter.Port();
    }

    protected void handlePageTitleChanged(String title) {
        if (title != null && !title.isEmpty()) {
            this.pageTitle = title;
            var browser = this.browser.get();
            browser.setTitle(title);
            browser.setTooltip(title);
        }
    }

    protected void handleLocationChanged(String url) {
        var toolBar = this.toolBar.get();
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        toolBar.setUrl(decodedUrl);
        if (url != null && !url.isBlank() && !url.equals("about:blank")) {
            loadFavicon(url);
            toolBar.setReloadDisable(false);
        } else {
            toolBar.setReloadDisable(true);
        }
    }

    protected void handleHistoryChanged() {
        var toolBar = this.toolBar.get();
        toolBar.setBackDisable(getView().getHistoryIndex() == 0);
        toolBar.setForwardDisable(getView().getHistoryIndex() + 1 == getView().getHistorySize());
    }

    private void loadFavicon(String url) {
        Thread.ofVirtual().start(() -> {
            try {
                var image = FaviconLoader.loadFavicon(new URI(url).toURL(), 32);
                if (getView().getLocation().equals(url)) { // it hasn't changed
                    Icon<?> icon = null;
                    if (image != null) {
                        icon = new ImageIcon(image);
                    } else {
                        icon = WebIcons.WEB_BROWSER;
                    }
                    browser.get().setIcon(icon);
                }
            } catch (Exception ex) {
                logger.error("Error loading favicon", ex);
            }
        });
    }

    private void setDefaultTitleAndIcon() {
        browser.get().setTitle("Web Browser");
        browser.get().setIcon(WebIcons.WEB_BROWSER);
    }
}
