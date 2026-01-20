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

import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 *
 * @author Pavel Castornii
 */
public class WebAreaFxView<P extends WebAreaPresenter<?, ?>> extends AbstractAreaFxView<P> implements WebAreaView {

    private final WebView webView = new WebView();

    private final VBox box = new VBox(webView);

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return box;
    }

    @Override
    public String getPageTitle() {
        return this.webView.getEngine().getTitle();
    }

    @Override
    public void load(String url) {
        this.webView.getEngine().load(url);
    }

    @Override
    public void reload() {
        this.webView.getEngine().reload();
    }

    @Override
    public void clear() {
        webView.getEngine().load(null);
    }

    @Override
    public int getHistoryIndex() {
        return webView.getEngine().getHistory().getCurrentIndex();
    }

    @Override
    public int getHistorySize() {
        return webView.getEngine().getHistory().getEntries().size();
    }

    @Override
    public String loadHistory(int index) {
        var webViewHistory = webView.getEngine().getHistory();
        if (index != webViewHistory.getCurrentIndex()) {
            int currentIndex = webViewHistory.getCurrentIndex();
            int offset = index - currentIndex;
            webViewHistory.go(offset);
            return webViewHistory.getEntries().get(index).getUrl();
        } else {
            return null;
        }
    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(webView, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
        this.webView.getEngine()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0");

    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.webView.getEngine().titleProperty()
                .addListener((ov, oldV, newV) -> getPresenter().handlePageTitleChanged(newV));
    }
}
