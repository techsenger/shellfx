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

package com.techsenger.tabshell.jfx.inspector;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
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
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 *
 * @author Pavel Castornii
 */
public class JfxInspectorTabViewModel<T extends JfxInspectorTabMediator> extends AbstractTabViewModel<T> {

    private static Map<AttributeCategory, PropertyItem> createAttributeInfosByCategory() {
        var map = new HashMap<AttributeCategory, PropertyItem>();
        for (var c : AttributeCategory.values()) {
            map.put(c, new PropertyItem(c));
        }
        return Collections.unmodifiableMap(map);
    }

    protected static String getCategoryText(AttributeCategory category) {
        return Utils.toPascalCase(category.name()) + " Properties";
    }

    private final Connector connector;

    private final ReadOnlyObjectWrapper<Element> rootElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<Element> selectedElement = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<PropertyItem> rootItem = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<PropertyItem> selectedItem = new ReadOnlyObjectWrapper<>();

    private final Map<AttributeCategory, PropertyItem> itemsByCategory = createAttributeInfosByCategory();

    private final ObservableSource<Void> attributesUpdated = new SimpleObservableSource<>();

    private int windowUid;

    private HighlightOptions highlightOptions = new HighlightOptions(true, false, false);

    public JfxInspectorTabViewModel(Connector connector) {
        this.connector = connector;
        setTitle("Inspector");
        setClosable(false);
        setRootInfo(new PropertyItem((AttributeCategory) null));
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

    public ReadOnlyObjectProperty<PropertyItem> rootInfoProperty() {
        return rootItem.getReadOnlyProperty();
    }

    public PropertyItem getRootInfo() {
        return rootItem.get();
    }

    public ReadOnlyObjectProperty<PropertyItem> selectedItemProperty() {
        return selectedItem.getReadOnlyProperty();
    }

    public PropertyItem getSelectedItem() {
        return selectedItem.get();
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

    protected void handlePropertyClick() {
        Element element = getSelectedElement();
        if (element == null) {
            return;
        }
        var item = getSelectedItem();
        if (item == null) {
            return;
        }
        var field = item.getAttribute().field();
        String declaringClassName = this.connector.getDeclaringClass(element.getClassInfo().className(), field);
        var vm = new PropertyDialogViewModel(element, item, declaringClassName);
        getMediator().addPropertyDialog(vm);
    }

    void setRootElement(Element element) {
        rootElement.set(element);
    }

    void setSelectedElement(Element element) {
        selectedElement.set(element);
    }

    void setSelectedInfo(PropertyItem value) {
        selectedItem.set(value);
    }

    private void setRootInfo(PropertyItem value) {
        rootItem.set(value);
    }

    ObservableSource<Void> getAttributesUpdated() {
        return attributesUpdated;
    }

    private void addAttributes(AttributeListEvent event) {
        var root = getRootInfo();
        var cat = itemsByCategory.get(event.category());
        cat.getChildren().clear();
        root.getChildren().add(cat);
        var sortedList = new ArrayList<>(event.attributes());
        sortedList.sort(Comparator.comparing(Attribute::name));
        for (var a : sortedList) {
            cat.getChildren().add(new PropertyItem(event.category(), a));
        }
    }
}

