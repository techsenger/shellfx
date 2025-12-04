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

    private static Map<AttributeCategory, NodeInfo> createAttributeInfosByCategory() {
        var map = new HashMap<AttributeCategory, NodeInfo>();
        for (var c : AttributeCategory.values()) {
            map.put(c, new NodeInfo(c));
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

    private final ReadOnlyObjectWrapper<NodeInfo> rootInfo = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<NodeInfo> selectedInfo = new ReadOnlyObjectWrapper<>();

    private final Map<AttributeCategory, NodeInfo> infosByCategory = createAttributeInfosByCategory();

    private final ObservableSource<Void> attributesUpdated = new SimpleObservableSource<>();

    private int windowUid;

    private ShellViewModel shell;

    private HighlightOptions highlightOptions = new HighlightOptions(true, false, false);

    public JfxInspectorTabViewModel(ShellViewModel shell, Connector connector) {
        this.shell = shell;
        this.connector = connector;
        setTitle("Inspector");
        setClosable(false);
        setRootInfo(new NodeInfo((AttributeCategory) null));
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

    public ReadOnlyObjectProperty<NodeInfo> rootInfoProperty() {
        return rootInfo.getReadOnlyProperty();
    }

    public NodeInfo getRootInfo() {
        return rootInfo.get();
    }

    public ReadOnlyObjectProperty<NodeInfo> selectedInfoProperty() {
        return selectedInfo.getReadOnlyProperty();
    }

    public NodeInfo getSelectedInfo() {
        return selectedInfo.get();
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
                this.setSelectedInfo(null);
                getRootInfo().getChildren().clear();
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

    private void setRootInfo(NodeInfo value) {
        rootInfo.set(value);
    }

    private void setSelectedInfo(NodeInfo value) {
        selectedInfo.set(value);
    }

    private void addAttributes(AttributeListEvent event) {
        var root = getRootInfo();
        var cat = infosByCategory.get(event.category());
        cat.getChildren().clear();
        root.getChildren().add(cat);
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        for (var a : sortedList) {
            cat.getChildren().add(new NodeInfo(event.category(), a));
        }
    }
}

