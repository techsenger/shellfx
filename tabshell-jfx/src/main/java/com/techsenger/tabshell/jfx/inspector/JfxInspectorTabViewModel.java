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

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.core.tab.ShellTabViewModel;
import com.techsenger.tabshell.jfx.JfxComponentNames;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import devtoolsfx.connector.Connector;
import devtoolsfx.connector.HighlightOptions;
import devtoolsfx.event.AttributeListEvent;
import devtoolsfx.event.ConnectorEvent;
import devtoolsfx.event.EventSource;
import devtoolsfx.scenegraph.Element;
import devtoolsfx.scenegraph.attributes.Attribute;
import devtoolsfx.scenegraph.attributes.AttributeCategory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabViewModel extends AbstractTabViewModel {

    private static Map<AttributeCategory, PropertyInfo> createAttributeInfosByCategory() {
        var map = new HashMap<AttributeCategory, PropertyInfo>();
        for (var c : AttributeCategory.values()) {
            map.put(c, new PropertyInfo(c));
        }
        return Collections.unmodifiableMap(map);
    }

    protected static String getNodeText(Element element) {
        var text = element.getClassInfo().simpleClassName();
        if (element.getNodeProperties() != null) {
            var styleClasses = element.getNodeProperties().styleClass();
            if (styleClasses != null && !styleClasses.isEmpty()) {
                text += " class=\"" + styleClasses + "\"";
            }
        }
        return text;
    }

    protected static String getCategoryText(AttributeCategory category) {
        return Utils.toPascalCase(category.name()) + " Properties";
    }

    private final Connector connector;

    private final ReadOnlyObjectWrapper<Element> rootElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<PropertyInfo> rootInfo = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<PropertyInfo> selectedInfo = new ReadOnlyObjectWrapper<>();

    private final Map<AttributeCategory, PropertyInfo> infosByCategory = createAttributeInfosByCategory();

    private final ObservableSource<Void> attributesUpdated = new SimpleObservableSource<>();

    private int windowUid;

    private ShellTabViewModel shellTab;

    private HighlightOptions highlightOptions = new HighlightOptions(true, false, false);

    public JfxInspectorTabViewModel(ShellTabViewModel shellTab, Connector connector) {
        this.shellTab = shellTab;
        this.connector = connector;
        setTitle("Inspector");
        setClosable(false);
        setRootInfo(new PropertyInfo((AttributeCategory) null));
    }

    public Connector getConnector() {
        return connector;
    }

    public ReadOnlyObjectProperty<Element> rootElementProperty() {
        return rootElement.getReadOnlyProperty();
    }

    public Element getRootElement() {
        return rootElement.get();
    }

    public ReadOnlyObjectProperty<Element> selectedElementProperty() {
        return selectedElement.getReadOnlyProperty();
    }

    public Element getSelectedElement() {
        return selectedElement.get();
    }

    public ReadOnlyObjectProperty<PropertyInfo> rootInfoProperty() {
        return rootInfo.getReadOnlyProperty();
    }

    public PropertyInfo getRootInfo() {
        return rootInfo.get();
    }

    public ReadOnlyObjectProperty<PropertyInfo> selectedInfoProperty() {
        return selectedInfo.getReadOnlyProperty();
    }

    public PropertyInfo getSelectedInfo() {
        return selectedInfo.get();
    }

    @Override
    public JfxInspectorMediator getMediator() {
        return (JfxInspectorMediator) super.getMediator();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(JfxComponentNames.JFX_INSPECTOR_TAB);
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.windowUid = connector.getEventSources().stream().map(EventSource::uid).toList().get(0);
        connector.getEventBus().subscribe(ConnectorEvent.class, event -> {
            switch (event) {
                case AttributeListEvent ale -> {
                    addAttributes(ale);
                    this.attributesUpdated.next(null);
                }
                default -> { }
            }
        });
        selectedElement.addListener((ov, oldV, newV) -> {
            if (newV == null) {
                this.connector.clearSelection(windowUid);
            } else {
                this.setSelectedInfo(null);
                getRootInfo().getChildren().clear();
                this.connector.selectNode(windowUid, newV, highlightOptions);
            }
        });
    }

    protected void handleInfoClick() {
        Element element = getSelectedElement();
        if (element == null) {
            return;
        }
        var info = getSelectedInfo();
        if (info == null) {
            return;
        }
        var field = info.getAttribute().field();
        String declaringClassName = this.connector.getDeclaringClass(element.getClassInfo().className(), field);
        var vm = new PropertyDialogViewModel(shellTab, element, info, declaringClassName);
        getMediator().openPropertyDialog(vm);
    }

    protected ShellTabViewModel getShellTab() {
        return shellTab;
    }

    void setRootElement(Element element) {
        rootElement.set(element);
    }

    void setSelectedElement(Element element) {
        selectedElement.set(element);
    }

    void setSelectedInfo(PropertyInfo value) {
        selectedInfo.set(value);
    }

    private void setRootInfo(PropertyInfo value) {
        rootInfo.set(value);
    }

    ObservableSource<Void> getAttributesUpdated() {
        return attributesUpdated;
    }

    private void addAttributes(AttributeListEvent event) {
        var root = getRootInfo();
        var cat = infosByCategory.get(event.category());
        cat.getChildren().clear();
        root.getChildren().add(cat);
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        for (var a : sortedList) {
            cat.getChildren().add(new PropertyInfo(event.category(), a));
        }
    }
}

