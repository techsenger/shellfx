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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.devtools.DevToolsComponents;
import com.techsenger.tabshell.devtools.UrlUtils;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogPresenter<V extends PropertyDialogView>  extends AbstractDialogPresenter<V> {

    private final Element node;

    private final PropertyItem item;

    private final String declaringClassName;

    private final Consumer<String> linkOpener;

    public PropertyDialogPresenter(V view, PropertyDialogParams params) {
        super(view, params);
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

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DevToolsComponents.PROPERTY_DIALOG);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setPrefWidth(600);
        setPrefHeight(350);
        setTitle("Property Dialog");
        getView().setName(item.getAttribute().name());
        var nameUrl = resolveNameUrl();
        if (nameUrl != null) {
            getView().addNameUrl(nameUrl);
        }
        getView().setValue(item.getValueData().text());
        if (item.getAttribute().cssProperty() != null) {
            getView().setCss(item.getAttribute().cssProperty());
        } else {
            getView().setCss("-");
        }
        var cssUrl = resolveCssPropertyUrl();
        if (cssUrl != null) {
            getView().addCssUrl(cssUrl);
        }
        getView().setState(item.getAttribute().valueState().name());
        setResultAction((button) -> requestClose());
        setRightButtons(PropertyDialogButtons.OK);
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
