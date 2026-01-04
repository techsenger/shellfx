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

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.web.model.UrlUtils;
import com.techsenger.tabshell.web.style.WebIcons;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.util.function.Consumer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabViewModel<T extends WebBrowserTabMediator> extends AbstractShellTabViewModel<T> {

    private final ObservableSource<String> urlSource = new SimpleObservableSource<>();

    private final StringProperty pageTitle = new SimpleStringProperty();

    private final ObservableList<String> historyEntries = FXCollections.observableArrayList();

    private final ObservableList<String> unmodifiableHistoryEntries =
            FXCollections.unmodifiableObservableList(historyEntries);

    private final IntegerProperty historyIndex = new SimpleIntegerProperty();

    private final ReadOnlyStringWrapper location = new ReadOnlyStringWrapper();

    public WebBrowserTabViewModel() {
        super();
        setIcon(WebIcons.WEB_BROWSER);
        setTitle("Web Browser");
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

    /**
     * Returns the unmodifiable list of history entries.
     *
     * @return
     */
    public ObservableList<String> getHistoryEntries() {
        return unmodifiableHistoryEntries;
    }

    public int getHistoryIndex() {
        return historyIndex.get();
    }

    public void setHistoryIndex(int index) {
        historyIndex.set(index);
    }

    public IntegerProperty historyIndexProperty() {
        return historyIndex;
    }

    public void goBack() {
        var newIndex = getHistoryIndex();
        if (newIndex > 0) {
            newIndex--;
            getMediator().getToolBar().setUrl(this.historyEntries.get(newIndex));
            setHistoryIndex(newIndex);
        }
    }

    public void goForward() {
        var newIndex = getHistoryIndex();
        if (newIndex + 1 < this.historyEntries.size()) {
            newIndex++;
            getMediator().getToolBar().setUrl(this.historyEntries.get(newIndex));
            setHistoryIndex(newIndex);
        }
    }

    public void load(String url) {
        if (url != null) {
            var normalizedUrl = UrlUtils.normalize(url);
            if (!normalizedUrl.isEmpty() && UrlUtils.isValid(normalizedUrl)) {
                url = normalizedUrl;
            } else {
                url = UrlUtils.getSearch(url);
            }
            getMediator().getToolBar().setUrl(url);
            getMediator().getToolBar().setReloadDisable(false);
            urlSource.next(url);
        }
    }

    public ReadOnlyStringProperty locationProperty() {
        return location.getReadOnlyProperty();
    }

    public String getLocation() {
        return location.get();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        var toolBar = getMediator().getToolBar();
        this.historyIndex.addListener((ov, oldV, newV) -> {
            if (newV.intValue() > 0 && !this.historyEntries.isEmpty()) {
                toolBar.setBackDisable(false);
            } else {
                toolBar.setBackDisable(true);
            }
            if (newV.intValue() + 1 < this.historyEntries.size()) {
                toolBar.setForwardDisable(false);
            } else {
                toolBar.setForwardDisable(true);
            }
        });
        location.addListener((ov, oldV, newV) -> {
            if (newV != null) {
                toolBar.setUrl(newV);
            } else {
                toolBar.setUrl("");
            }
        });
    }

    protected void load() {
        var toolBar = getMediator().getToolBar();
        var url = toolBar.getUrl();
        load(url);
    }

    ObservableSource<String> getUrlSource() {
        return urlSource;
    }

    ObservableList<String> getModifiableHistoryEntries() {
        return historyEntries;
    }

    ReadOnlyStringWrapper getLocationWrapper() {
        return location;
    }
}
