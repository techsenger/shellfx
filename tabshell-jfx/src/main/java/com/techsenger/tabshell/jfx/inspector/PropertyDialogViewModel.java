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

package com.techsenger.tabshell.jfx.inspector;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.DialogScope;
import com.techsenger.tabshell.dialogs.simple.AbstractSimpleDialogViewModel;
import com.techsenger.tabshell.dialogs.simple.SimpleDialogMediator;
import com.techsenger.tabshell.jfx.UrlUtils;
import com.techsenger.tabshell.web.WebBrowserTabViewModel;
import devtoolsfx.scenegraph.Element;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogViewModel extends AbstractSimpleDialogViewModel<SimpleDialogMediator> {

    private final Element element;

    private final PropertyInfo info;

    private final String declaringClassName;

    public PropertyDialogViewModel(Element element, PropertyInfo info, String declaringClassName) {
        super(DialogScope.TAB, true);
        this.element = element;
        this.info = info;
        this.declaringClassName = declaringClassName;
        setPrefWidth(650);
        setPrefHeight(400);
        setOkText("Ok");
        setTitle("Property Dialog");
    }

    public Element getElement() {
        return element;
    }

    public PropertyInfo getInfo() {
        return info;
    }

    public String getPropertyUrl() {
        if (!declaringClassName.startsWith("javafx.")) {
            return null;
        }
        return UrlUtils.getPropertyJavadocUrl(declaringClassName, info);
    }

    public String getCssPropertyUrl() {
        if (!declaringClassName.startsWith("javafx.")) {
            return null;
        }
        return UrlUtils.getCssPropertyJavadocUrl(element.getSimpleClassName());
    }

    @Override
    public PropertyDialogMediator getMediator() {
        return (PropertyDialogMediator) super.getMediator();
    }

    @Override
    public CloseCheckResult canClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void openUrl(String ulr) {
        var vm = new WebBrowserTabViewModel();
        getMediator().addBrowser(vm);
        vm.load(ulr);
    }
}
