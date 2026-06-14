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

import atlantafx.base.theme.Tweaks;
import com.techsenger.annotations.Nullable;
import com.techsenger.connectorfx.ConnectorOptions;
import com.techsenger.connectorfx.LocalElement;
import com.techsenger.connectorfx.event.EventSource;
import com.techsenger.connectorfx.scenegraph.Element;
import com.techsenger.connectorfx.scenegraph.attributes.Attribute;
import static com.techsenger.connectorfx.scenegraph.attributes.Attribute.DisplayHint.INSETS;
import static com.techsenger.connectorfx.scenegraph.attributes.Attribute.DisplayHint.NUMERIC;
import com.techsenger.connectorfx.scenegraph.attributes.AttributeCategory;
import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.core.window.WindowContainerFxView;
import com.techsenger.tabshell.core.window.WindowPosition;
import com.techsenger.tabshell.core.window.WindowType;
import com.techsenger.tabshell.devtools.ElementUtils;
import com.techsenger.tabshell.devtools.ToolBarFxView;
import com.techsenger.tabshell.devtools.ToolBarParams;
import com.techsenger.tabshell.devtools.ToolBarPort;
import com.techsenger.tabshell.devtools.ToolBarPresenter;
import com.techsenger.tabshell.devtools.style.DevToolsIcons;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.toolkit.fx.utils.TableUtils;
import com.techsenger.toolkit.fx.utils.TreeViewUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public class NodeTabFxView<P extends NodeTabPresenter<?>> extends AbstractTabFxView<P> implements NodeTabView {

    private static final Logger logger = LoggerFactory.getLogger(NodeTabFxView.class);

    private static final class NodeCell extends TreeCell<Element> {

        @Override
        protected void updateItem(Element item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(ElementUtils.getTitle(item));
            }
        }
    }

    private static class NodeTreeItem extends TreeItem<Element> {

        private boolean childrenCreated = false;

        NodeTreeItem(Element t) {
            super(t);
            expandedProperty().addListener((ov, oldV, newV) -> {
                if (newV) {
                    createChildren();
                }
            });
        }

        private void createChildren() {
            if (childrenCreated) {
                return;
            }
            // close the fake child item
            getChildren().clear();
            for (var child : getValue().getChildren()) {
                var childItem = createNodeItem(child);
                if (childItem != null) {
                    getChildren().add(childItem);
                }
            }
            childrenCreated = true;
        }
    }

    private static final class CategoryTreeItem extends TreeItem<PropertyItem> {

        private CategoryTreeItem(PropertyItem item) {
            setValue(item);
        }

        public void setNotifier(BiConsumer<AttributeCategory, Boolean> notifier) {
            expandedProperty().addListener((ov, oldV, newV) -> notifier
                    .accept(getValue().getCategory(), newV));
        }
    }

    private static final class PropertyTableCell extends TreeTableCell<PropertyItem, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var propertyItem = getTableRow().getItem();
                if (propertyItem.getType() == PropertyItemType.PROPERTY) {
                    var valueData = propertyItem.getValueData();
                    Label mainLabel = new Label(item);
                    HBox box = new HBox(Spacing.getHorizontalThird(), mainLabel);
                    if (valueData.isDefault()) {
                        mainLabel.getStyleClass().add("default-value");
                    }
                    if (propertyItem.getAttribute().cssProperty() != null) {
                        Label cssHint = new Label("CSS");
                        cssHint.getStyleClass().add("css-hint");
                        if (valueData.isDefault()) {
                            cssHint.getStyleClass().add("default-value");
                        }
                        var cssContainer = new HBox(cssHint);
                        cssContainer.getStyleClass().add("css-container");
                        cssContainer.setAlignment(Pos.TOP_LEFT);
                        box.getChildren().add(cssContainer);
                    }

                    setText(null);
                    setGraphic(box);
                } else {
                    setText(item);
                    setGraphic(null);
                }
            }
        }
    }

    private static final class ValueTableCell extends TreeTableCell<PropertyItem, String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                var propertyItem = getTableRow().getItem();
                var attr = propertyItem.getAttribute();
                Label mainLabel = new Label(item);
                if (propertyItem.getType() == PropertyItemType.PROPERTY) {
                    var valueData = propertyItem.getValueData();
                    if (valueData.isDefault()) {
                        mainLabel.getStyleClass().add("default-value");
                    }
                }
                setGraphic(mainLabel);
                setText(null);
            }
        }
    }

    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");

    private static NodeTreeItem createNodeItem(Element element) {
        // auxiliary nodes created by connectorfx should be added
        if (element.isNodeElement() && element.getNodeProperties().id() != null
                && element.getNodeProperties().id().startsWith(ConnectorOptions.AUX_NODE_ID_PREFIX)) {
            return null;
        }
        var item = new NodeTreeItem(element);
        // element.hasChildren() has a bug
        if (element.getChildren().size() > 0) {
            // add a fake child item
            item.getChildren().add(new TreeItem(null));
        }
        return item;
    }

    private static void refreshNodeIndex(NodeTreeItem item, Map<Element, TreeItem<Element>> treeItemsByNode) {
        treeItemsByNode.put(item.getValue(), item);
        item.createChildren();
        // Recursively traverse all children
        for (var child : item.getChildren()) {
            refreshNodeIndex((NodeTreeItem) child, treeItemsByNode);
        }
    }

    protected static String getCategoryText(AttributeCategory category) {
        return Utils.toPascalCase(category.name()) + " Properties";
    }

    private static PropertyItem.PropertyValueData createValueData(PropertyItem item) {
        Attribute<?> attr = item.getAttribute();
        switch (attr.displayHint()) {
            case NUMERIC -> {
                boolean isDefault = false;
                if (attr.valueState() == Attribute.ValueState.DEFAULT) {
                    isDefault = true;
                } else if (attr.value() instanceof Number number) {
                    isDefault = isZero(number.doubleValue());
                }
                if (attr.value() instanceof Double num) {
                    if (num == Region.USE_PREF_SIZE) {
                        return new PropertyItem.PropertyValueData("USE_PREF_SIZE", true);
                    }
                    if (num == Region.USE_COMPUTED_SIZE) {
                        return new PropertyItem.PropertyValueData("USE_COMPUTED_SIZE", true);
                    }
                    if (num == Double.MIN_VALUE) {
                        return new PropertyItem.PropertyValueData("MIN_VALUE", true);
                    }
                    if (num == Double.MAX_VALUE) {
                        return new PropertyItem.PropertyValueData("MAX_VALUE", true);
                    }
                    return new PropertyItem.PropertyValueData(FORMAT.format(num), isDefault);
                }
                return new PropertyItem.PropertyValueData(String.valueOf(attr.value()), isDefault);
            }
            case INSETS -> {
                if (attr.value() instanceof Insets insets) {
                    if (isZero(insets.getTop()) && isZero(insets.getRight())
                            && isZero(insets.getBottom()) && isZero(insets.getLeft())) {
                        return new PropertyItem.PropertyValueData("Insets.EMPTY", true);
                    }
                }
                return new PropertyItem.PropertyValueData(String
                        .valueOf(attr.value()), attr.valueState() == Attribute.ValueState.DEFAULT);
            }
            default -> {
                if (attr.value() instanceof List<?> list && list.isEmpty()) {
                    return new PropertyItem.PropertyValueData("[]", true);
                }
                boolean isDefault = false;
                var strValue = String.valueOf(attr.value());
                if (item.getAttribute().displayHint() == Attribute.DisplayHint.TRANSFORMS
                        || (item.getCategory() == AttributeCategory.REFLECTIVE
                        && (attr.name().equals("localToSceneTransform")
                        || attr.name().equals("localToParentTransform")))) {
                    strValue = strValue.replaceAll("\\R", "")
                            .replaceAll("\\[\\s+", "[")
                            .replaceAll("[\t]| {3,}", "  ");
                }
                if (attr.valueState() == Attribute.ValueState.DEFAULT
                        || strValue.equals("null") || strValue.isEmpty())  {
                    isDefault = true;
                }
                return new PropertyItem.PropertyValueData(strValue, isDefault);
            }
        }
    }

    private static boolean isZero(double v) {
        return Math.abs(v) < 1e-10;
    }

    private static @Nullable EditPropertyTask<?> createEditTask(Element element, Attribute<?> attr,
            ComponentDescriptor descriptor) {
        LocalElement local = (LocalElement) element;
        var node = local.unwrap();
        if (node != null) {
            var property = findProperty(node, attr.field());
            if (property != null) {
                return createEditTask(attr, property, descriptor);
            } else {
                logger.warn("{} Couldn't create task - property {} for {} not found", descriptor.getLogPrefix(),
                        attr.field(), node);
                return null;
            }
        }
        logger.warn("{} Couldn't create task - node is null", descriptor.getLogPrefix());
        return null;
    }

    private static @Nullable ReadOnlyProperty<?> findProperty(Node node, String propertyName) {
        Class<?> clazz = node.getClass();
        while (clazz != null) {
            try {
                Method method = clazz.getDeclaredMethod(propertyName);
                method.setAccessible(true);
                Object result = method.invoke(node);
                if (result instanceof ReadOnlyProperty<?> property) {
                    return property;
                } else {
                    return null;
                }
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Failed to invoke " + propertyName + " on " + node.getClass(), e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static EditPropertyTask<?> createEditTask(Attribute<?> attr, ReadOnlyProperty<?> property,
            ComponentDescriptor descriptor) {
        if (property instanceof BooleanProperty bp) {
            return new EditPropertyTask<>(
                attr,
                Boolean.class,
                value -> Boolean.parseBoolean(value),
                bp::setValue,
                bp::getValue
            );
        }
        if (property instanceof DoubleProperty dp) {
            return new EditPropertyTask<>(
                attr,
                Double.class,
                value -> Double.parseDouble(value),
                dp::setValue,
                dp::getValue
            );
        }
        if (property instanceof IntegerProperty ip) {
            return new EditPropertyTask<>(
                attr,
                Integer.class,
                value -> Integer.parseInt(value),
                ip::setValue,
                ip::getValue
            );
        }
        if (property instanceof FloatProperty fp) {
            return new EditPropertyTask<>(
                attr,
                Float.class,
                value -> Float.parseFloat(value),
                fp::setValue,
                fp::getValue
            );
        }
        if (property instanceof LongProperty lp) {
            return new EditPropertyTask<>(
                attr,
                Long.class,
                value -> Long.parseLong(value),
                lp::setValue,
                lp::getValue
            );
        }
        if (property instanceof StringProperty sp) {
            return new EditPropertyTask<>(
                attr,
                String.class,
                value -> value,
                sp::setValue,
                sp::getValue
            );
        }
        Type type = property.getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            if (args.length > 0 && args[0] instanceof Class<?> clazz) {
                return createObjectPropertyTask(attr, (Property<Object>) property, (Class<Object>) clazz, descriptor);
            }
        }
        Object value = property.getValue();
        if (value != null) {
            return createObjectPropertyTask(attr, (Property<Object>) property,
                    (Class<Object>) value.getClass(), descriptor);
        }
        logger.warn("{} Couldn't create task - property type is unresolved, value is null", descriptor.getLogPrefix());
        return null;
    }

    private static <T> EditPropertyTask<T> createObjectPropertyTask(Attribute<?> attr, Property<T> property,
            Class<T> clazz, ComponentDescriptor descriptor) {
        if (clazz.isEnum()) {
            return new EditPropertyTask<>(
                attr,
                clazz,
                value -> (T) Enum.valueOf((Class<Enum>) clazz, value),
                property::setValue,
                property::getValue
            );
        }
        if (clazz == Color.class) {
            return new EditPropertyTask<>(
                attr,
                clazz,
                value -> (T) Color.web(value),
                property::setValue,
                property::getValue
            );
        }
        if (clazz == Insets.class) {
            return new EditPropertyTask<>(
                attr,
                clazz,
                value -> {
                    String[] parts = value.split(","); // format: top,right,bottom,left
                    return (T) new Insets(
                        Double.parseDouble(parts[0].trim()),
                        Double.parseDouble(parts[1].trim()),
                        Double.parseDouble(parts[2].trim()),
                        Double.parseDouble(parts[3].trim())
                    );
                },
                property::setValue,
                property::getValue
            );
        }
        logger.warn("{} Couldn't create task - unknown property type", descriptor.getLogPrefix());
        return null;
    }

    private static void updateReadOnlyByProperty(Map<String, Boolean> result, Node node) {
        Class<?> clazz = node.getClass();
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().endsWith("Property")
                        && method.getParameterCount() == 0
                        && ReadOnlyProperty.class.isAssignableFrom(method.getReturnType())) {
                    boolean readOnly = !Property.class.isAssignableFrom(method.getReturnType());
                    result.putIfAbsent(method.getName(), readOnly);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public class Composer extends AbstractTabFxView<P>.Composer implements NodeTabView.Composer {

        private final NodeTabFxView<P> view = NodeTabFxView.this;

        private ToolBarFxView<?> nodeToolBar;

        private ToolBarFxView<?> propertyToolBar;

        @Override
        public void compose() {
            super.compose();

            nodeToolBar = createNodeToolBar();
            getModifiableChildren().add(nodeToolBar);
            nodeBox.getChildren().add(0, nodeToolBar.getNode());

            propertyToolBar = createPropertyToolBar();
            getModifiableChildren().add(propertyToolBar);
            propertyBox.getChildren().add(0, propertyToolBar.getNode());
        }

        public ToolBarFxView<?> getNodeToolBar() {
            return nodeToolBar;
        }

        public ToolBarFxView<?> getPropertyToolBar() {
            return propertyToolBar;
        }

        @Override
        public ToolBarPort getNodeToolBarPort() {
            return nodeToolBar == null ? null : nodeToolBar.getPresenter();
        }

        @Override
        public ToolBarPort getPropertyToolBarPort() {
            return propertyToolBar == null ? null : propertyToolBar.getPresenter();
        }

        @Override
        public DialogPort openViewerDialog(ViewerDialogParams params) {
            var dialog = createViewerDialog(params);
            dialog.getPresenter().setResizable(true);
            if (params.getWindowType() == WindowType.NESTED) {
                windowContainer.addWindow(dialog);
                windowContainer.alignWindowToStage(dialog, WindowPosition.CENTER);
            } else {
                dialog.getStage().initOwner(getNode().getContent().getScene().getWindow());
                dialog.getStage().show();
            }
            return dialog.getPresenter();
        }

        @Override
        public EditorDialogPort openEditorDialog(EditorDialogParams params) {
            AbstractEditorDialogFxView<?> dialog = null;
            if (params.getTask().getType().isEnum()) {
                dialog = createEnumEditorDialog(params);
            } else if (Insets.class.isAssignableFrom(params.getTask().getType())) {
                dialog = createInsetEditorDialog(params);
            } else {
                dialog = createTextEditorDialog(params);
            }
            if (params.getWindowType() == WindowType.NESTED) {
                windowContainer.addWindow(dialog);
                windowContainer.alignWindowToStage(dialog, WindowPosition.CENTER);
            } else {
                dialog.getStage().initOwner(getNode().getContent().getScene().getWindow());
                dialog.getStage().show();
            }
            return dialog.getPresenter();
        }

        protected ToolBarFxView<?> createNodeToolBar() {
            var view = new ToolBarFxView<>("NodeClass / StyleClass / ID", true);
            var params = new ToolBarParams(getPresenter().new NodeToolBarAwarePort());
            var presenter = new ToolBarPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected ToolBarFxView<?> createPropertyToolBar() {
            var view = new ToolBarFxView<>("Property", false);
            var params = new ToolBarParams(getPresenter().new PropertyToolBarAwarePort());
            var presenter = new ToolBarPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected ViewerDialogFxView<?> createViewerDialog(ViewerDialogParams params) {
            var view = new ViewerDialogFxView<>(getShell());
            var presenter = new ViewerDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected TextEditorDialogFxView<?> createTextEditorDialog(EditorDialogParams params) {
            var view = new TextEditorDialogFxView<>();
            var presenter = new TextEditorDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected EnumEditorDialogFxView<?> createEnumEditorDialog(EditorDialogParams params) {
            var view = new EnumEditorDialogFxView<>();
            var presenter = new EnumEditorDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
        }

        protected InsetEditorDialogFxView<?> createInsetEditorDialog(EditorDialogParams params) {
            var view = new InsetEditorDialogFxView<>();
            var presenter = new InsetEditorDialogPresenter<>(view, params);
            presenter.initialize();
            return view;
        }
    }

    private final WindowContainerFxView.Composer windowContainer;

    private final TreeView<Element> nodeTreeView = new TreeView<>();

    private final Map<Element, TreeItem<Element>> treeItemsByNode = new HashMap<>();

    private final VBox nodeBox = new VBox(nodeTreeView);

    private Map<String, Boolean> readOnlyByProperty;

    private final TreeTableView<PropertyItem> propertyTableView = new TreeTableView<>();

    private final VBox propertyBox = new VBox(propertyTableView);

    private final SplitPane splitPane = new SplitPane(nodeBox, propertyBox);

    public NodeTabFxView(ShellFxView<?> shell, WindowContainerFxView.Composer windowContainer) {
        super(shell);
        this.windowContainer = windowContainer;
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void selectNode(Element node, boolean afterDataUpdate) {
        var treeItem = this.treeItemsByNode.get(node);
        if (treeItem != null) {
            nodeTreeView.getSelectionModel().select(treeItem);
            if (afterDataUpdate) {
                nodeTreeView.scrollTo(nodeTreeView.getSelectionModel().getSelectedIndex());
            } else {
                TreeViewUtils.scrollToIfNeeded(nodeTreeView, nodeTreeView.getSelectionModel().getSelectedIndex());
            }
        }
    }

    @Override
    public void selectRoot() {
        nodeTreeView.getSelectionModel().select(0);
        nodeTreeView.scrollTo(nodeTreeView.getSelectionModel().getSelectedIndex());
    }

    @Override
    public void refreshNodes() {
        var selectedItem = this.nodeTreeView.getSelectionModel().getSelectedItem();
        Element selectedNode = null;
        if (selectedItem != null) {
            selectedNode = selectedItem.getValue();
        }
        updateNodeRoot();
        refreshNodeIndex();
        selectNode(selectedNode, true);
    }

    @Override
    public void refreshNodeIndex() {
        refreshNodeIndex((NodeTreeItem) nodeTreeView.getRoot(), treeItemsByNode);
    }

    @Override
    public void setReadOnlyByProperty(Map<String, Boolean> map) {
        this.readOnlyByProperty = map;
    }

    @Override
    public void focusProperties() {
        propertyTableView.requestFocus();
    }

    @Override
    public void clearProperties() {
        this.propertyTableView.setRoot(null);
    }

    @Override
    public void addProperties(AttributeCategory category, boolean expanded, List<PropertyItem> props) {
        var rootTreeItem = this.propertyTableView.getRoot();
        if (rootTreeItem == null) {
            rootTreeItem = new TreeItem<>(new PropertyItem());
            this.propertyTableView.setRoot(rootTreeItem);
        }
        var cat = new CategoryTreeItem(new PropertyItem(category));
        cat.setExpanded(expanded);
        // and only now
        cat.setNotifier(getPresenter()::onCategoryExpanded);
        rootTreeItem.getChildren().add(cat);
        for (var prop : props) {
            prop.setValueData(createValueData(prop));
            var treeItem = new TreeItem<>(prop);
            cat.getChildren().add(treeItem);
        }
    }

    @Override
    public void selectPropertyCategory(AttributeCategory cat) {
        for (var child : this.propertyTableView.getRoot().getChildren()) {
            if (child.getValue().getCategory() == cat) {
                this.propertyTableView.getSelectionModel().select(child);
                this.propertyTableView.scrollTo(this.propertyTableView.getSelectionModel().getSelectedIndex());
                break;
            }
        }
    }

    @Override
    public void selectProperty(PropertyItem item) {
        var treeItem = TableUtils.findTreeItem(this.propertyTableView.getRoot(), item);
        if (treeItem != null) {
            TreeItem<PropertyItem> parent = treeItem.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
            this.propertyTableView.getSelectionModel().select(treeItem);
            this.propertyTableView.scrollTo(this.propertyTableView.getSelectionModel().getSelectedIndex());
        }
    }

    @Override
    protected Composer createComposer() {
        return new NodeTabFxView.Composer();
    }

    @Override
    protected void initialize() {
        super.initialize();
        Platform.runLater(() -> getPresenter().onAdded());
    }

    @Override
    protected void build() {
        super.build();
        var styles = NodeTabFxView.class.getResource("node-tab.css").toExternalForm();
        getContentBox().getStylesheets().add(styles);

        updateNodeRoot();
        nodeTreeView.setCellFactory(tv -> new NodeCell());
        nodeTreeView.getStyleClass().add(StyleClasses.NO_BORDER);
        VBox.setVgrow(nodeTreeView, Priority.ALWAYS);

        TreeTableColumn<PropertyItem, String> propertyColumn = new TreeTableColumn<>("Property");
        propertyColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            String text;
            if (item.getType() == PropertyItemType.CATEGORY) {
                text = getCategoryText(item.getCategory());
            } else {
                text = item.getAttribute().name();
            }
            return new SimpleStringProperty(text);
        });
        propertyColumn.setCellFactory(col -> new PropertyTableCell());

        TreeTableColumn<PropertyItem, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> {
            // root is not shown
            var item = param.getValue().getValue();
            if (item.getType() == PropertyItemType.CATEGORY) {
                return new SimpleStringProperty();
            } else {
                var valueData = item.getValueData();
                return new SimpleStringProperty(valueData.text());
            }
        });
        valueColumn.setCellFactory(col -> new ValueTableCell());
        propertyTableView.getColumns().addAll(propertyColumn, valueColumn);
        propertyTableView.getStyleClass().add(Tweaks.NO_HEADER);
        propertyTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        propertyTableView.setShowRoot(false);
        propertyTableView.setPlaceholder(new Label(""));
        VBox.setVgrow(propertyTableView, Priority.ALWAYS);
        var viewMenuItem = new MenuItem("View", new FontIconView(DevToolsIcons.VIEW));
        viewMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER));
        viewMenuItem.setOnAction(e -> {
            var treeItem = this.propertyTableView.getSelectionModel().getSelectedItem();
            if (treeItem != null && treeItem.getValue() != null) {
                getPresenter().onPropertyRequested(treeItem.getValue());
            }
        });
        var editMenuItem = new MenuItem("Edit", new FontIconView(DevToolsIcons.EDIT));
        editMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN));
        EditPropertyTask<?>[] tasks = new EditPropertyTask[]{null};
        editMenuItem.setOnAction(e -> {
            if (tasks[0] != null) {
                getPresenter().onEditProperty(tasks[0]);
            }
        });
        var menu = new ContextMenu(viewMenuItem, editMenuItem);
        menu.setOnShowing((e) -> {
            tasks[0] = null;
            var treeItem = this.propertyTableView.getSelectionModel().getSelectedItem();
            var selItem = this.nodeTreeView.getSelectionModel().getSelectedItem();
            Element el = null;
            if (selItem != null) {
                el = selItem.getValue();
            } else {
                return;
            }
            if (treeItem != null && treeItem.getValue() != null && !treeItem.getValue().isReadOnly()) {
                var propItem = treeItem.getValue();
                var task = createEditTask(el, propItem.getAttribute(), getDescriptor());
                tasks[0] = task;
            }
            editMenuItem.setDisable(tasks[0] == null);
        });
        this.propertyTableView.setContextMenu(menu);
        propertyTableView.setRowFactory(ttv -> {
            TreeTableRow<PropertyItem> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    getPresenter().onPropertyRequested(row.getItem());
                }
            });
            return row;
        });

        VBox.setVgrow(splitPane, Priority.ALWAYS);
        getContentBox().getChildren().add(splitPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        nodeTreeView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) -> {
            this.readOnlyByProperty.clear();
            Element el = null;
            if (newV != null) {
                el = newV.getValue();
                LocalElement localElement = (LocalElement) el;
                updateReadOnlyByProperty(readOnlyByProperty, localElement.unwrap());
            }
            getPresenter().onNodeSelected(el);
        });
        propertyTableView.getSelectionModel().selectedItemProperty().addListener((ov, oldV, newV) ->
            getPresenter().onPropertySelected(newV == null ? null : newV.getValue()));
    }

    protected TreeTableView<PropertyItem> getPropertyTableView() {
        return propertyTableView;
    }

    protected TreeView<Element> getNodeTreeView() {
        return nodeTreeView;
    }

    protected VBox getNodeBox() {
        return nodeBox;
    }

    protected VBox getPropertyBox() {
        return propertyBox;
    }

    protected SplitPane getSplitPane() {
        return splitPane;
    }

    private void updateNodeRoot() {
        var stage = getComposer().getShell().getStage();
        var root = LocalElement.of(stage, new EventSource(null, stage.hashCode(), true));
        var rootItem = createNodeItem(root);
        nodeTreeView.setRoot(rootItem);
        getPresenter().onRootChanged(root);
    }
}
