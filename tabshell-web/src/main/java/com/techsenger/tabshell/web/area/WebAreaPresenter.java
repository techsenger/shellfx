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
import com.techsenger.tabshell.material.icon.ImageIcon;
import com.techsenger.tabshell.web.WebComponents;
import com.techsenger.tabshell.web.model.UrlUtils;
import com.techsenger.tabshell.web.style.WebIcons;
import com.techsenger.tabshell.web.toolbar.WebToolBarPort;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class WebAreaPresenter<V extends WebAreaView, C extends AreaComposer> extends AbstractAreaPresenter<V, C> {

    private enum FavIconType {
        ICO, PNG
    }

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
        return new Descriptor(WebComponents.WEB_AREA);
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

    /**
     * We don't use location because it can contain redirects.
     *
     * @param url
     */
    protected void handleHistoryChanged(String url) {
        browser.get().setIcon(null);
        var toolBar = this.toolBar.get();
        toolBar.setBackDisable(getView().getHistoryIndex() == 0);
        toolBar.setForwardDisable(getView().getHistoryIndex() + 1 == getView().getHistorySize());
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        toolBar.setUrl(decodedUrl);
        if (url != null && !url.isBlank() && !url.equals("about:blank")) {
            toolBar.setReloadDisable(false);
        } else {
            toolBar.setReloadDisable(true);
        }
    }

    protected void handleFaviconExtracted(List<String> urls) {
        record Icon(FavIconType type, String url) { }
        class IconRegistry {
            private String ico;
            private String png;
            private String png32x32;
            private String png48x48;
            private String png64x64;

            private Icon getIcon() {
                if (this.png32x32 != null) {
                    return new Icon(FavIconType.PNG, this.png32x32);
                }
                if (this.png48x48 != null) {
                    return new Icon(FavIconType.PNG, this.png48x48);
                }
                if (this.png64x64 != null) {
                    return new Icon(FavIconType.PNG, this.png64x64);
                }
                if (this.png != null) {
                    return new Icon(FavIconType.PNG, this.png);
                }
                return new Icon(FavIconType.ICO, this.ico);
            }
        }
        var registry = new IconRegistry();
        for (var url : urls) {
            var lastDotIndex = url.lastIndexOf(".");
            var ext = url.substring(lastDotIndex + 1);
            if (ext.equals("ico")) {
                registry.ico = url;
            } else if (ext.equals("png")) {
                if (url.endsWith("/favicon.png")) {
                    registry.png = url;
                } else if (url.endsWith("32x32.png")) {
                    registry.png32x32 = url;
                } else if (url.endsWith("48x48.png")) {
                    registry.png48x48 = url;
                } else if (url.endsWith("64x64.png")) {
                    registry.png64x64 = url;
                }
            }
        }
        var icon = registry.getIcon();
        logger.debug("{} Extracted favicon info; type: {}, url: {}", getDescriptor().getLogPrefix(), icon.type(),
                icon.url());
        loadFavicon(icon.type(), icon.url());
    }

    private void loadFavicon(FavIconType iconType, String iconUrl) {
        var location = getView().getLocation();
        Thread.ofVirtual().start(() -> {
            try {
                Image image = null;
                if (iconUrl != null) {
                    if (iconType == FavIconType.PNG) {
                        image = FaviconLoader.loadPng(iconUrl);
                    } else {
                        image = FaviconLoader.loadIco(iconUrl);
                    }
                } else {
                    image = FaviconLoader.resolveAndLoad(new URI(location).toURL(), 32);
                }
                if (image != null) {
                    logger.debug("{} Loaded favicon from {} for {}", getDescriptor().getLogPrefix(),
                            iconUrl == null ? "resolved url" : iconUrl, location);
                    if (getView().getLocation().equals(location)) { // it hasn't changed)
                        var icon = new ImageIcon(image);
                        browser.get().setIcon(icon);
                    }
                }
            } catch (Exception ex) {
                logger.error("{} Error loading favicon", getDescriptor().getLogPrefix(),  ex);
            }
        });
    }

    private void setDefaultTitleAndIcon() {
        browser.get().setTitle("Web Browser");
        browser.get().setIcon(WebIcons.WEB_BROWSER);
    }
}
