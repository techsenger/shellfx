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
import com.techsenger.tabshell.core.ShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
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

    private static Map<AttributeCategory, AttributeInfo> createAttributeInfosByCategory() {
        var map = new HashMap<AttributeCategory, AttributeInfo>();
        for (var c : AttributeCategory.values()) {
            map.put(c, new AttributeInfo(c));
        }
        return Collections.unmodifiableMap(map);
    }

    protected static String getElementText(Element element) {
        var text = element.getClassInfo().simpleClassName();
        if (element.getNodeProperties() != null) {
            var styleClasses = element.getNodeProperties().styleClass();
            if (styleClasses != null && !styleClasses.isEmpty()) {
                text += " class=\"" + styleClasses + "\"";
            }
        }
        return text;
    }

    private final Connector connector;

    private final ReadOnlyObjectWrapper<Element> rootElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<AttributeInfo> rootAttribute = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<AttributeInfo> selectedAttribute = new ReadOnlyObjectWrapper<>();

    private final Map<AttributeCategory, AttributeInfo> attributeInfosByCategory = createAttributeInfosByCategory();

    private final ObservableSource<Void> attributesUpdated = new SimpleObservableSource<>();

    private int windowUid;

    private ShellViewModel shell;

    private HighlightOptions highlightOptions = new HighlightOptions(true, false, false);

    public JfxInspectorTabViewModel(ShellViewModel shell, Connector connector) {
        this.shell = shell;
        this.connector = connector;
        setTitle("Inspector");
        setClosable(false);
        setRootAttribute(new AttributeInfo((AttributeCategory) null));
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

    public ReadOnlyObjectProperty<AttributeInfo> rootAttributeProperty() {
        return rootAttribute.getReadOnlyProperty();
    }

    public AttributeInfo getRootAttribute() {
        return rootAttribute.get();
    }

    public ReadOnlyObjectProperty<AttributeInfo> selectedAttributeProperty() {
        return selectedAttribute.getReadOnlyProperty();
    }

    public AttributeInfo getSelectedAttribute() {
        return selectedAttribute.get();
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(JfxComponentNames.JFX_INSPECTOR);
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
                this.setSelectedAttribute(null);
                getRootAttribute().getChildren().clear();
                this.connector.selectNode(windowUid, newV, highlightOptions);
            }
        });
    }

    void setRootElement(Element element) {
        rootElement.set(element);
    }

    void setSelectedElement(Element element) {
        selectedElement.set(element);
    }

    ObservableSource<Void> getAttributesUpdated() {
        return attributesUpdated;
    }

    private void setRootAttribute(AttributeInfo value) {
        rootAttribute.set(value);
    }

    private void setSelectedAttribute(AttributeInfo value) {
        selectedAttribute.set(value);
    }

    private void addAttributes(AttributeListEvent event) {
        var root = getRootAttribute();
        var cat = attributeInfosByCategory.get(event.category());
        cat.getChildren().clear();
        root.getChildren().add(cat);
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        for (var a : sortedList) {
            cat.getChildren().add(new AttributeInfo(a));
        }
    }
}

