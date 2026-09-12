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

package com.techsenger.shellfx.devtools.node;

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogViewModel;
import com.techsenger.shellfx.core.window.WindowComposer;
import com.techsenger.shellfx.devtools.UrlUtils;
import com.techsenger.shellfx.devtools.style.DevToolsIcons;
import java.util.function.Consumer;
import com.techsenger.shellfx.core.dialog.ClosableDialogPort;

/**
 *
 * @author Pavel Castornii
 */
public class ViewerDialogViewModel<C extends WindowComposer> extends AbstractDialogViewModel<C>
        implements ClosableDialogPort {

    private final Element node;

    private final PropertyItem item;

    private final String declaringClassName;

    private final Consumer<String> linkOpener;

    public ViewerDialogViewModel(ViewerDialogParams params) {
        super(params);
        this.node = params.getNode();
        this.item = params.getItem();
        this.declaringClassName = params.getDeclaringClassName();
        this.linkOpener = params.getLinkOpener();
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Element getNode() {
        return node;
    }

    public PropertyItem getItem() {
        return item;
    }

    public String getName() {
        return item.getAttribute().name();
    }

    public String getValue() {
        return item.getValueData().text();
    }

    public String getCss() {
        var css = item.getAttribute().cssProperty();
        return css != null ? css : "-";
    }

    public String getState() {
        return item.getAttribute().valueState().name();
    }

    public String getNameUrl() {
        return resolveNameUrl();
    }

    public String getCssUrl() {
        return resolveCssPropertyUrl();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setIcon(DevToolsIcons.VIEW);
        setTitle("Property Viewer");
        setOnResult((button) -> closeSafely());
        setRightButtons(ViewerDialogButtons.OK);
    }

    @Override
    protected void applyPersistentState() {
        super.applyPersistentState();
        setWidth(600);
        setHeight(350);
    }

    protected void onFollowLink(String url) {
        this.linkOpener.accept(url);
    }

    protected String resolveNameUrl() {
        if (declaringClassName == null) {
            return UrlUtils.getFieldJavadocUrl(item);
        }
        return UrlUtils.getPropertyJavadocUrl(declaringClassName, item);
    }

    protected String resolveCssPropertyUrl() {
        if (declaringClassName == null || item.getAttribute().cssProperty() == null) {
            return null;
        }
        return UrlUtils.getCssPropertyJavadocUrl(node.getSimpleClassName());
    }
}
