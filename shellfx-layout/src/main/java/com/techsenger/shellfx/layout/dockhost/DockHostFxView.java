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

package com.techsenger.shellfx.layout.dockhost;

import com.techsenger.annotations.Nullable;
import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.shellfx.core.area.AbstractAreaFxView;
import com.techsenger.shellfx.core.area.AreaFxView;
import com.techsenger.shellfx.core.area.AreaParams;
import com.techsenger.shellfx.core.area.AreaPort;
import com.techsenger.shellfx.core.tab.TabFxView;
import static com.techsenger.shellfx.layout.dockhost.DockConstants.ONE_HALF;
import static com.techsenger.shellfx.layout.dockhost.DockConstants.ONE_THIRD;
import com.techsenger.shellfx.layout.style.LayoutIcons;
import com.techsenger.shellfx.layout.tabhost.TabHostFxView;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin.TabHeaderArea;
import com.techsenger.toolkit.core.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import static javafx.geometry.Side.TOP;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.stage.Popup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DockHost is the primary component responsible for displaying and managing a docking layout. It arranges
 * {@link AbstractAreaFxView} components into a hierarchy of SplitPanes, allowing users to freely reorganize the
 * workspace by docking, undocking, and rearranging areas at runtime.
 *
 * <p>There are two types of components: MainComponent — contains the main component, and TabDock — contains a TabPane.
 * MainComponent and TabDock always acts as a leaf. The main component can never be closed or minimized into a
 * SideBar by the user while TabDock can be both closed and minimized.
 *
 * All SplitPane nodes and nodes of area components are placed inside specialized containers based on SplitPane. The use
 * of these containers is required for the following reasons:
 *
 * <ol>
 *     <li>They allow visual highlighting to be added to components.</li>
 *     <li>They allow additional JavaFX-layer properties to be assigned to a component without modifying its
 *     underlying FX node.</li>
 *     <li>They provide a unified approach for all nodes managed by the docking layout.</li>
 * </ol>
 *
 * <p>Containers have two elements: indicator, which highlights the new tab position, and eventPane, which tracks
 * mouse movement inside the component during a drag-and-drop operation.
 *
 * <p>How it works. When the user starts a drag-and-drop operation and moves the mouse, an instance of
 * {@link DropPosition} is created. If the user releases the mouse, the data in this object is used to perform the
 * relocation of either a Tab or a TabDock. The reason for creating this object so early is that, while the user is
 * moving the mouse, a potential drop position needs to be highlighted, and that also requires a {@link DropPosition}.
 *
 * <p>The user can move either a single Tab or an entire existing TabDock. It is important to note that drag-and-drop
 * support for Tab is already implemented in TabPanePro, whereas the drag-and-drop functionality for TabDock is
 * implemented in this class.
 *
 * <p>All mouse events are captured within a MainComponent and TabDock, and from the cursor position two variables are
 * determined: the nearest side of the TabDock (Side side) and whether the mouse is within EDGE_THRESHOLD
 * (boolean edgeMode).
 *
 * <p>When a new tab is docked, the operation may require wrapping an existing node with a new SplitPane. The target of
 * this wrapping can be the TabDock itself, its parent SplitPane, or its grandparent SplitPane. The decision is made
 * dynamically based on the values of side and edgeMode together with the orientation of the parent SplitPane,
 * ensuring that the system adapts correctly whether docking occurs inside a node, along its edge, or at the boundary
 * of a larger layout.
 *
 * <p>All possible cases:
 * | #  | SplitPane   | EdgeMode | Side       | Node Location | Action                                              |
 * | -- | ----------- | -------- | ---------- | ------------- | --------------------------------------------------- |
 * | 1  | Horizontal  | true     | Top/Bottom | Any           | Wrap parent or add to grandparent                   |
 * | 2  | Horizontal  | true     | Left       | First         | Add to parent or wrap parent or add to grandparent  |
 * | 3  | Horizontal  | true     | Right      | Last          | Add to parent or wrap parent or add to grandparent  |
 * | 4  | Horizontal  | true     | Left/Right | Middle        | Add to parent                                       |
 * | -- | ----------- | -------- | ---------- | ------------- | --------------------------------------------------- |
 * | 5  | Vertical    | true     | Top        | First         | Add to parent or wrap parent or add to grandparent  |
 * | 6  | Vertical    | true     | Bottom     | Last          | Add to parent or wrap parent or add to grandparent  |
 * | 7  | Vertical    | true     | Top/Bottom | Middle        | Add to parent                                       |
 * | 8  | Vertical    | true     | Left/Right | Any           | Wrap parent or add to grandparent                   |
 * | -- | ----------- | -------- | ---------- | ------------- | --------------------------------------------------- |
 * | 9  | Horizontal  | false    | Top/Bottom | Any           | Wrap node with vertical split                       |
 * | 10 | Horizontal  | false    | Left/Right | Any           | Add to parent                                       |
 * | -- | ----------- | -------- | ---------- | ------------- | --------------------------------------------------- |
 * | 11 | Vertical    | false    | Top/Bottom | Any           | Add to parent                                       |
 * | 12 | Vertical    | false    | Left/Right | Any           | Wrap node with horizontal split                     |
 *
 *
 * @author Pavel Castornii
 */
public class DockHostFxView<P extends DockHostPresenter<?>> extends AbstractAreaFxView<P> implements DockHostView {

    /**
     * Represents the type of draggable object in the docking system.
     */
    public enum DraggableType {

        /**
         * A single tab being dragged. Can be docked into existing TabDock or create new TabDock.
         */
        TAB,

        /**
         * An entire TabDock (container with multiple tabs) being dragged. Can be repositioned as a whole unit
         * or merged with other docks.
         */
        TAB_DOCK
    }

    protected static final class DragAndDropHandler {

        private final DockHostFxView<?> dockHost;

        private final DockHostFxView.Composer composer;

        /**
         * The tab is being dragged.
         */
        private Tab dragTab;

        /**
         * The dock that is being dragged.
         */
        private TabDockFxView<?> dragDock;

        private Popup dragDockPopup;

        private boolean dragInProgress;

        public DragAndDropHandler(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
            this.composer = dockHost.getComposer();
        }

        protected Node createTabDragContent(TabPaneProSkin.TabHeaderSkin tabHeader) {
            var tabParams = new SnapshotParameters();
            WritableImage tabImage = tabHeader.snapshot(tabParams, null);
            ImageView dragView = new ImageView(tabImage);
            var tab = tabHeader.getContext().getTab();
            if (tab.getTabPane().getSide() == Side.BOTTOM) {
                Rotate rotate = new Rotate(180, tabImage.getWidth() / 2, tabImage.getHeight() / 2);
                dragView.getTransforms().add(rotate);
            }
            var container = new VBox(dragView);
            //container.getStyleClass().add("tab-drag-content");
            return container;
        }

        protected Node createTabDockDragContent(TabPaneProSkin.TabHeaderArea tabHeaderArea) {
            StackPane headersRegion = (StackPane) tabHeaderArea.lookup(".headers-region");
            double totalWidth = 0;
            double snapShotWidth = tabHeaderArea.getWidth();
            Node capturedTabSkin = null;
            boolean capturedTabSkinIsLast = false;
            var tabSkins = headersRegion.getChildrenUnmodifiable();
            for (var i = 0; i < tabSkins.size(); i++) {
                StackPane tabSkin = (StackPane) tabSkins.get(i);
                totalWidth += tabSkin.getWidth();
                if (i == 0 || totalWidth <= DRAG_REGION_MAX_WIDTH) { // at least one node is required
                    capturedTabSkin = tabSkin;
                    capturedTabSkinIsLast = i + 1 == tabSkins.size();
                } else {
                    break;
                }
            }

            if (capturedTabSkin != null) {
                Bounds bounds = capturedTabSkin.getBoundsInLocal();
                Point2D rightEdge = new Point2D(bounds.getMaxX(), 0);
                Point2D rightEdgeInScene = capturedTabSkin.localToScene(rightEdge);
                Point2D rightEdgeInArea = tabHeaderArea.sceneToLocal(rightEdgeInScene);
                Bounds areaBounds = tabHeaderArea.getBoundsInLocal();
                var calculatedWidth = rightEdgeInArea.getX() - areaBounds.getMinX();
                if (calculatedWidth > 0 && calculatedWidth < snapShotWidth) {
                    snapShotWidth = calculatedWidth;
                } else {
                    capturedTabSkin = null;
                }
            }

            var snapParams = new SnapshotParameters();
            snapParams.setViewport(new Rectangle2D(0, 0, snapShotWidth, tabHeaderArea.getHeight()));

            WritableImage tabImage = tabHeaderArea.snapshot(snapParams, null);
            ImageView dragView = new ImageView(tabImage);
            dragView.setSmooth(false);
            var contentContainer = new HBox(dragView);
            if (capturedTabSkin != null && !capturedTabSkinIsLast) {
                var iconView = new FontIconView(LayoutIcons.CHEVRON_DOUBLE_RIGHT);
                // we need a bg for the icon
                StackPane headerBg = (StackPane) tabHeaderArea.lookup(".tab-header-background");
                snapParams = new SnapshotParameters();
                snapParams.setViewport(new Rectangle2D(0, 0, 10, tabHeaderArea.getHeight()));
                WritableImage bgImage = headerBg.snapshot(snapParams, null);
                BackgroundImage backgroundImage = new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.REPEAT, // horizont
                    BackgroundRepeat.NO_REPEAT, // vertical
                    BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT
                );
                contentContainer.setAlignment(Pos.CENTER);
                var iconContainer = new StackPane(iconView);
                iconContainer.setBackground(new Background(backgroundImage));
                contentContainer.getChildren().add(iconContainer);
            }
            //container.getStyleClass().add("tab-drag-content");
            return contentContainer;
        }

        void onTabDragDetected(Tab tab) {
            this.dragTab = tab;
            updateDragInProgress(true, DraggableType.TAB);
        }

        void onTabDrag(Tab tab) {
            this.dragTab = tab;
            updateDragInProgress(true, DraggableType.TAB);
        }

        void onTabDrop(Tab tab) {
            try {
                processDropInsideTabHeaderArea();
            } finally {
                clearOnDrop();
            }
        }

        void onTabHeaderAreaMouseDragOver(TabPanePro tabPane, MouseDragEvent e) {
            TabDockContainer tabDockContainer = (TabDockContainer) tabPane.getParent();
            var pos = new MousePosition(tabDockContainer, e, null, false, true);
            handleMouseDragOverOnTabHeaderArea(pos, tabDockContainer);
        }

        /**
         * For Tab we manage drag-and-drop via TabPanePro. For TabDock we do it here.
         *
         * @param dock
         * @param position
         */
        void onDockDragDetected(TabDockFxView<?> dock, FontIconView iconView, MouseEvent e) {
            this.dragDock = dock;
            TabPaneProSkin sourceSkin = (TabPaneProSkin) dock.getNode().getSkin();
            TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
            var content = createTabDockDragContent(tabHeaderArea);
            this.dragDockPopup = new Popup();
            this.dragDockPopup.setAutoHide(false);
            this.dragDockPopup.getContent().add(content);
            var scene = dockHost.getNode().getScene();
            this.dragDockPopup.show(scene.getWindow(), e.getScreenX(), e.getScreenY());
            scene.setCursor(Cursor.CLOSED_HAND);

            iconView.startFullDrag();
            updateDragInProgress(true, DraggableType.TAB_DOCK);
        }

        void onDockMouseDragged(TabDockFxView<?> dock, FontIconView iconView, MouseEvent e) {
            if (this.dragDockPopup != null) {
                this.dragDockPopup.setAnchorX(e.getScreenX());
                this.dragDockPopup.setAnchorY(e.getScreenY());
                e.consume();
            }
        }

        void onDockMouseReleased(TabDockFxView<?> dock, FontIconView iconView, MouseEvent e) {
            hideDragDockPopup();
            updateDragInProgress(false, DraggableType.TAB_DOCK);
        }

        void onContainerMouseDragExited(MousePosition mousePosition) {
            hideIndicator();
        }

        void onContainerMouseDragOver(MousePosition mousePosition) {
            hideIndicator();
            dockHost.dropPosition = dockHost.dropPositionResolver.provideBasePosition(mousePosition);
            showIndicator();
        }

        /**
         * This handler is called when the mouse is not over TabHeaderArea.
         *
         * @param mousePosition
         */
        void onContainerMouseDragReleased(MousePosition mousePosition) {
            try {
                processDropOutsideTabHeaderArea();
            } finally {
                clearOnDrop();
            }
        }

        private void handleMouseDragOverOnTabHeaderArea(MousePosition mousePosition,
                TabDockContainer tabDockContainer) {
            hideIndicator();
            dockHost.dropPosition =
                    dockHost.dropPositionResolver.provideTabAreaPosition(mousePosition, tabDockContainer);
            showIndicator();
        }

        private void processDropInsideTabHeaderArea() {
            // in this case moving the tab from one component to another
            // is handled by TabPanePro and TabHostFxView handlers.
            updateDragInProgress(false, DraggableType.TAB);
            logger.debug("{} Processed drop inside TabHeaderArea", dockHost.getDescriptor().getLogPrefix());
        }

        private void processDropOutsideTabHeaderArea() {
            hideIndicator();
            var dropPos = dockHost.dropPosition;
            if (dropPos != null && dropPos.getTransformation() != null && dropPos.isValid()) {
                hideDragDockPopup();
                if (this.dragDock == null) {
                    var tabDock = composer.createTabDock();
                    tabDock.getComposer().setDockHost(dockHost);
                    dockHost.getComposer().getModifiableChildren().add(tabDock);
                    dropPos.getTransformation().accept(tabDock);
                    dockHost.transformer.moveTab(tabDock);
                } else {
                    // Here we use a placeholder to reserve the target drop position while the TabDock
                    // is still attached to its original parent. This allows us to safely remove the
                    // TabDock from the old location without invalidating the previously calculated DropPosition,
                    // and then replace the placeholder with the actual TabDock.

                    // adding placeholder to a new location
                    dropPos.getTransformation().accept(composer.placeholder);

                    // removing tabDock
                    var oldContainer = ContainerUtils.getContainer(dragDock);

                    // it is necessary to create a new position with a new index for example,
                    // if there are new children, besides the old parent should be used
                    // var oldPosition = oldContainer.resolvePosition(oldParent);
                    dockHost.transformer.removeTabDock(oldContainer, TabDockOperation.DEPART);

                    // finally replacing the placeholder
                    var placeholderContainer = ContainerUtils.getContainer(composer.placeholder);
                    var newParentContainer = placeholderContainer.getLogicalParent();
                    DockSplitPane newParent = newParentContainer.getSplitPane();
                    // it is not possible to use dropPosition.getNewPosition().getIndex()
                    // because after removing tabDock indexes have changed
                    var placeholderPos = placeholderContainer.resolvePosition();
                    var dragDockContainer = ContainerUtils.createContainer(dockHost, dragDock);
                    newParentContainer.replace(placeholderPos.getIndex(), dragDockContainer);
                    logger.debug("{} Replaced {} with {}", dockHost.getDescriptor().getLogPrefix(),
                            composer.placeholder.getDescriptor().getFullName(),
                            dragDock.getDescriptor().getFullName());
                }
                dockHost.printTreeDebugInfo();
            }
            updateDragInProgress(false, this.dragDock == null ? DraggableType.TAB : DraggableType.TAB_DOCK);
            logger.debug("{} Processed drop outside TabHeaderArea", dockHost.getDescriptor().getLogPrefix());
        }

        private void updateDragInProgress(boolean value, DraggableType type) {
            if (this.dragInProgress == value) {
                return;
            }
            this.dragInProgress = value;
            ContainerUtils.traverse(composer.rootContainer, 0, (c, level) -> {
                if (c instanceof AbstractAreaContainer<?> aac) {
                    aac.updateDragInProgress(value, type);
                } else if (c instanceof SplitPaneContainer spc) {
                    spc.updateDragInProgress(value, type);
                }
            });
        }

        private void hideIndicator() {
            var dropPos = dockHost.dropPosition;
            if (dropPos != null && dropPos.getIndicatorPosition() != null) {
                SplitPaneContainer container = (SplitPaneContainer) dropPos.getIndicatorPosition().getContainer();
                container.hideIndicator();
            }
        }

        private void showIndicator() {
            var dropPos = dockHost.dropPosition;
            if (dropPos != null && dropPos.getIndicatorPosition() != null && dropPos.isValid()) {
                SplitPaneContainer container = (SplitPaneContainer) dropPos.getIndicatorPosition().getContainer();
                container.showIndicator(dropPos.getIndicatorBounds());
            }
        }

        private void hideDragDockPopup() {
            if (this.dragDockPopup != null) {
                this.dragDockPopup.hide();
                this.dragDockPopup = null;
            }
        }

        private void clearOnDrop() {
            dockHost.dropPosition = null;
            this.dragTab = null;
            this.dragDock = null;
            if (this.dragDockPopup != null) {
                this.dragDockPopup.hide();
            }
            this.dragDockPopup = null;
        }
    }

    private abstract static class AbstractContainer extends StackPane {

        private final DockHostFxView<?> dockHost;

        private @Nullable SplitPaneContainer logicalParent;

        AbstractContainer(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
        }

        DockHostFxView<?> getDockHost() {
            return dockHost;
        }

        ContainerPosition resolvePosition() {
            if (logicalParent != null) {
                var containerIndex = logicalParent.getSplitPane().getItems().indexOf(this);
                return new ContainerPosition(this, containerIndex);
            } else {
                return new ContainerPosition(this, -1);
            }
        }

        @Nullable SplitPaneContainer getLogicalParent() {
            return logicalParent;
        }

        void setLogicalParent(@Nullable SplitPaneContainer parent) {
            this.logicalParent = parent;
        }

        abstract String getChildName();

        abstract String getChildFullName();

        abstract UUID getChildUuid();

        abstract String getChildShortUuid();
    }

    private static final class SplitPaneContainer extends AbstractContainer {

        private final DockSplitPane splitPane;

        private final Rectangle indicator = createIndicator();

        SplitPaneContainer(DockHostFxView<?> dockHost, DockSplitPane splitPane) {
            super(dockHost);
            this.splitPane = splitPane;
            getChildren().add(splitPane);
            this.indicator.setMouseTransparent(true);
        }

        void updateDragInProgress(boolean value, DraggableType type) {
            if (value) {
                if (indicator.getParent() == null) {
                    getChildren().add(indicator);
                }
            } else {
                if (indicator.getParent() != null) {
                    getChildren().remove(getChildren().size() - 1);
                }
            }
        }

        void showIndicator(Bounds bounds) {
            Point2D localCoords = sceneToLocal(bounds.getMinX(), bounds.getMinY());
            indicator.setX(localCoords.getX());
            indicator.setY(localCoords.getY());
            indicator.setWidth(bounds.getWidth());
            indicator.setHeight(bounds.getHeight());
            indicator.setVisible(true);
        }

        void hideIndicator() {
            indicator.setVisible(false);
        }

        @Override
        String getChildName() {
            return DockSplitPane.class.getSimpleName();
        }

        @Override
        String getChildFullName() {
            return splitPane.getFullName();
        }

        @Override
        UUID getChildUuid() {
            return this.splitPane.getUuid();
        }

        @Override
        String getChildShortUuid() {
            return this.splitPane.getShortUuid();
        }

        void insertNew(int liveIndex, AbstractContainer child) {
            splitPane.insertNew(liveIndex, child);
            child.setLogicalParent(this);
        }

        void replace(int liveIndex, AbstractContainer child) {
            splitPane.replace(liveIndex, child);
            child.setLogicalParent(this);
        }

        void minimizeChild(AbstractContainer child) {
            splitPane.minimize(child);
            // logicalParent stays unchanged — child remains a logical child of this split
        }

        void restoreChild(AbstractContainer child) {
            splitPane.restore(child);
            // logicalParent stays unchanged — child was never reparented
        }

        void removePermanently(AbstractContainer child) {
            splitPane.removePermanently(child);
            child.setLogicalParent(null);
        }

        boolean shouldBeNormalized() {
            return splitPane.shouldBeNormalized();
        }

        List<AbstractContainer> getLogicalItems() {
            return (List<AbstractContainer>) (List<?>) splitPane.getLogicalItems();
        }

        private Rectangle createIndicator() {
            Rectangle result = new Rectangle();
            result.setManaged(false);
            result.setVisible(false);
            result.getStyleClass().add("indicator");
            return result;
        }

        private DockSplitPane getSplitPane() {
            return splitPane;
        }
    }

    /**
     * Containers are not part of the components, since the main component can be any class — there is no requirement
     * that it must inherit from a component in this package.
     *
     * <p>Containers are created when components are added to the {@link DockSplitPane} and destroyed when they are
     * removed from there.
     */
    private abstract static class AbstractAreaContainer<T extends AreaFxView<?>> extends AbstractContainer {

        private static final Double EDGE_THRESHOLD = 20.0;

        private final T area;

        /**
         * Note: the event pane is not located over the {@link TabHeaderArea}. Therefore, if the mouse is over the
         * {@link TabHeaderArea} when the drag is released, the event is handled by the {@link TabHeaderArea}.
         * If the mouse is not over the {@link TabHeaderArea}, the drag released event is handled by this
         * {@link AbstractEventContainer}.
         *
         */
        private final Pane eventPane = new Pane();

        AbstractAreaContainer(DockHostFxView<?> dockHost, T area) {
            super(dockHost);
            this.area = area;
            getChildren().add(area.getNode());
            eventPane.setMouseTransparent(false);
            // eventPane.setStyle("-fx-background-color: yellow");
            var dndHandler = getDockHost().dragAndDropHandler;
            eventPane.setOnMouseDragOver(e -> dndHandler.onContainerMouseDragOver(provideMousePosition(e)));
            eventPane.setOnMouseDragExited(e -> dndHandler.onContainerMouseDragExited(provideMousePosition(e)));
            eventPane.setOnMouseDragReleased(e -> dndHandler.onContainerMouseDragReleased(provideMousePosition(e)));
        }

        T getArea() {
            return area;
        }

        @Override
        String getChildName() {
            return area.getDescriptor().getName().getText();
        }

        @Override
        String getChildFullName() {
            return area.getDescriptor().getFullName();
        }

        @Override
        UUID getChildUuid() {
            return area.getDescriptor().getUuid();
        }

        @Override
        String getChildShortUuid() {
            return area.getDescriptor().getShortUuid();
        }

        void updateDragInProgress(boolean value, DraggableType type) {
            if (value) {
                if (eventPane.getParent() == null) {
                    getChildren().add(eventPane);
                    if (type == DraggableType.TAB) {
                        StackPane.setMargin(eventPane, provideEventPanePadding());
                    } else {
                        StackPane.setMargin(eventPane, Insets.EMPTY);
                    }
                }
            } else {
                if (eventPane.getParent() != null) {
                    getChildren().remove(getChildren().size() - 1);
                }
            }
        }

        Insets provideEventPanePadding() {
            return Insets.EMPTY;
        }

        private MousePosition provideMousePosition(MouseEvent e) {
            double width = eventPane.getWidth();
            double height = eventPane.getHeight();
            double leftDistance = e.getX();
            double rightDistance = width - e.getX();
            double topDistance = e.getY();
            double bottomDistance = height - e.getY();

            double minDistance = topDistance;
            var side = Side.TOP;
            boolean edgeMode = false;
            edgeMode = topDistance < EDGE_THRESHOLD;

            if (rightDistance < minDistance) {
                minDistance = rightDistance;
                side = Side.RIGHT;
                edgeMode = rightDistance < EDGE_THRESHOLD;
            }
            if (bottomDistance < minDistance) {
                minDistance = bottomDistance;
                side = Side.BOTTOM;
                edgeMode = bottomDistance < EDGE_THRESHOLD;
            }
            if (leftDistance < minDistance) {
                minDistance = leftDistance;
                side = Side.LEFT;
                edgeMode = leftDistance < EDGE_THRESHOLD;
            }
            return new MousePosition(this, e, side, edgeMode, false);
        }
    }

    private static final class MainAreaContainer extends AbstractAreaContainer<AreaFxView<?>> {

        MainAreaContainer(DockHostFxView<?> dockHost, AreaFxView<?> area) {
            super(dockHost, area);
        }

    }

    private static final class TabDockContainer extends AbstractAreaContainer<TabDockFxView<?>> {

        TabDockContainer(DockHostFxView<?> dockHost, TabDockFxView<?> tabDock) {
            super(dockHost, tabDock);
        }

        @Override
        Insets provideEventPanePadding() {
            TabPanePro tabPane = getArea().getNode();
            TabPaneProSkin sourceSkin = (TabPaneProSkin) tabPane.getSkin();
            TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();
            return new Insets(tabHeaderArea.getHeight(), 0, 0, 0);
        }
    }

    private static final class ContainerUtils {

        /**
         * Returns the path from the root container to the given container, inclusive of both ends. Used only for
         * live containers (main area, currently-live TabDocks) — walks the actual JavaFX scene graph, so it does not
         * (and must not) apply to minimized containers, which have no scene-graph parent.
         *
         * @param start the container to start from
         * @return the path, ordered from the root container to {@code start}
         */
        private static List<AbstractContainer> toRoot(AbstractContainer start) {
            var dockHost = start.getDockHost();
            var result = new ArrayList<AbstractContainer>();
            AbstractContainer current = start;
            while (current != null) {
                result.add(0, current);
                if (current == dockHost.getComposer().rootContainer) {
                    break;
                }
                current = current.getLogicalParent();
            }
            if (logger.isTraceEnabled()) {
                var names = result.stream().map(AbstractContainer::getChildFullName).collect(Collectors.joining(", "));
                logger.trace("{} {} path to root: {}", dockHost.getDescriptor().getLogPrefix(),
                        start.getChildFullName(), names);
            }
            return result;
        }

        private static SplitPaneContainer createContainer(DockHostFxView<?> dockHost, DockSplitPane splitPane) {
            var container = new SplitPaneContainer(dockHost, splitPane);
            SplitPane.setResizableWithParent(container, false);
            return container;
        }

        private static AbstractAreaContainer<?> createContainer(DockHostFxView<?> dockHost, AreaFxView<?> child) {
            AbstractAreaContainer<?> container;
            if (child instanceof TabDockFxView<?>) {
                var tabDock = (TabDockFxView<?>) child;
                container = new TabDockContainer(dockHost, tabDock);
                SplitPane.setResizableWithParent(container, false);
            } else {
                container = new MainAreaContainer(dockHost, child);
                SplitPane.setResizableWithParent(container, true);
            }
            return container;
        }

        /**
         * Returns the existing container node for a component that is currently on the scene.
         * @param view
         * @return
         */
        private static AbstractAreaContainer<?> getContainer(AreaFxView<?> view) {
            return (AbstractAreaContainer<?>) view.getNode().getParent();
        }

        private static boolean hasContainer(AreaFxView<?> view) {
            return view.getNode().getParent() instanceof  AbstractAreaContainer<?>;
        }

        private static TabDockContainer getContainer(TabDockFxView<?> view) {
            return (TabDockContainer) view.getNode().getParent();
        }

        private static SplitPaneContainer getContainer(SplitPane splitPane) {
            return (SplitPaneContainer) splitPane.getParent();
        }

        /**
         * Resolves a {@link ModelNode} to the live container it corresponds to in this docking layout.
         * <p>
         * Only nodes obtained from {@link Composer#getModelNode(AreaFxView)} — or reached from it by navigating via
         * {@link ModelNode#getParent()} or {@link GroupNode#getChildren()} — correspond to an actual live position and
         * can be resolved. Nodes built via {@link ModelNodeBuilder} are independent, unattached snapshots and have no
         * corresponding live container.
         *
         * @param node the node to resolve
         * @return the live container the node corresponds to
         * @throws IllegalArgumentException if {@code node} is not a live node obtained from this docking layout
         */
        private static AbstractContainer resolveContainer(ModelNode node) {
            if (node instanceof AreaNodeImpl a) {
                return a.getContainer();
            } else if (node instanceof GroupNodeImpl g) {
                return g.getContainer();
            } else {
                throw new IllegalArgumentException("node must be a live node obtained from "
                        + "Composer#getModelNode(AreaFxView) (or reached by navigating from it via "
                        + "ModelNode#getParent() or GroupNode#getChildren()); nodes built via ModelNodeBuilder do not "
                        + "correspond to a live position in the docking layout");
            }
        }

        private static void traverse(SplitPaneContainer splitPaneContainer, int level,
                BiConsumer<AbstractContainer, Integer> visitor) {
            visitor.accept(splitPaneContainer, level);
            var splitPane = splitPaneContainer.getSplitPane();
            for (Node item : splitPane.getItems()) {
                if (item instanceof SplitPaneContainer nestedContainer) {
                    traverse(nestedContainer, level + 1, visitor);
                } else {
                    visitor.accept((AbstractContainer) item, level + 1);
                }
            }
        }

        /**
         * Computes the given container's current relative size among its siblings, based on its live bounds and its
         * parent split pane's orientation and size.
         *
         * @param container the container to compute the proportion for
         * @return a value between {@code 0} and {@code 1}, or {@link ModelNodeBuilder#UNSET_PROPORTION} if the
         *         container is currently the root (it has no siblings to be proportional to) or its parent's size is
         *         not yet known
         */
        private static double resolveProportion(AbstractContainer container) {
            var parent = container.getLogicalParent();
            if (parent == null) {
                return ModelNodeBuilder.UNSET_PROPORTION;
            }
            var splitPane = parent.getSplitPane();
            var totalSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                    ? splitPane.getWidth() : splitPane.getHeight();
            if (totalSize <= 0) {
                return ModelNodeBuilder.UNSET_PROPORTION;
            }
            var containerSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                    ? container.getWidth() : container.getHeight();
            return containerSize / totalSize;
        }

        private ContainerUtils() {
            // empty
        }
    }

    private static final class ContainerPosition {

        private final AbstractContainer container;

        private final int index;

        private double fraction;

        ContainerPosition(int index, double fraction) {
            this(null, index);
            this.fraction = fraction;
        }

        ContainerPosition(AbstractContainer container, int index) {
            this.container = container;
            this.index = index;
        }

        public AbstractContainer getContainer() {
            return container;
        }

        public int getIndex() {
            return index;
        }

        public void setFraction(double fraction) {
            this.fraction = fraction;
        }

        public double getFraction() {
            return fraction;
        }

        public boolean isFirst() {
            return index == 0;
        }

        public boolean isLast() {
            if (this.index == -1) { // root
                return false;
            }
            var splitPane = container.getLogicalParent().getSplitPane();
            return index == splitPane.getItems().size() - 1;
        }

        @Override
        public String toString() {
            return "ContainerPosition [" + "container:" + container + ", index:" + index
                    + ", fraction:" + fraction + ']';
        }
    }

    /**
     * Represents a potential drop position for a {@link TabFxView} or {@link TabDockFxView} during a drag-and-drop
     * operation.
     */
    private static final class DropPosition {

        private MousePosition mousePosition;

        /**
         * Information about the container that got mouse drag event.
         */
        private ContainerPosition eventPosition;

        /**
         * Information about event container parent.
         */
        private ContainerPosition parentPosition;

        private ContainerPosition grandparentPosition;

        private ContainerPosition greatGrandparentPosition;

        /**
         * Information about new container (container is null).
         */
        private ContainerPosition newPosition;

        /**
         * Information about the container that shows indicator.
         */
        private ContainerPosition indicatorPosition;

        private Bounds indicatorBounds;

        /**
         * Contains the transformation that will be applied if the drop is completed.
         */
        private Consumer<TabDockFxView<?>> transformation;

        private boolean valid;

        DropPosition() {

        }

        public MousePosition getMousePosition() {
            return mousePosition;
        }

        public void setMousePosition(MousePosition mousePosition) {
            this.mousePosition = mousePosition;
        }

        public Bounds getIndicatorBounds() {
            return indicatorBounds;
        }

        public void setIndicatorBounds(Bounds indicatorBounds) {
            this.indicatorBounds = indicatorBounds;
        }

        public Consumer<TabDockFxView<?>> getTransformation() {
            return transformation;
        }

        public void setTransformation(Consumer<TabDockFxView<?>> transformation) {
            this.transformation = transformation;
        }

        public ContainerPosition getEventPosition() {
            return eventPosition;
        }

        public void setEventPosition(ContainerPosition eventPosition) {
            this.eventPosition = eventPosition;
        }

        public ContainerPosition getParentPosition() {
            return parentPosition;
        }

        public void setParentPosition(ContainerPosition parentPosition) {
            this.parentPosition = parentPosition;
        }

        public ContainerPosition getGrandparentPosition() {
            return grandparentPosition;
        }

        public void setGrandparentPosition(ContainerPosition grandparentPosition) {
            this.grandparentPosition = grandparentPosition;
        }

        public ContainerPosition getGreatGrandparentPosition() {
            return greatGrandparentPosition;
        }

        public void setGreatGrandparentPosition(ContainerPosition greatGrandparentPosition) {
            this.greatGrandparentPosition = greatGrandparentPosition;
        }

        public ContainerPosition getNewPosition() {
            return newPosition;
        }

        public void setNewPosition(ContainerPosition newPosition) {
            this.newPosition = newPosition;
        }

        public ContainerPosition getIndicatorPosition() {
            return indicatorPosition;
        }

        public void setIndicatorPosition(ContainerPosition indicatorPosition) {
            this.indicatorPosition = indicatorPosition;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public String toString() {
            return "DropPosition [" + "mousePosition:" + mousePosition + ", eventPosition:" + eventPosition
                    + ", parentPosition:" + parentPosition + ", grandparentPosition:" + grandparentPosition
                    + ", greatGrandparentPosition:" + greatGrandparentPosition + ", newPosition:" + newPosition
                    + ", indicatorPosition:" + indicatorPosition + ", indicatorBounds:" + indicatorBounds
                    + ", transformation:" + transformation + ", valid:" + valid + ']';
        }
    }

    private static final class DropPositionResolver {

        private static Bounds createTabPaneIndicatorBounds(MousePosition position, TabDockContainer tabDockContainer) {
            var eventContainer = position.getEventContainer();
            var eventSceneXY = getSceneXY(eventContainer);
            TabPaneProSkin skin = (TabPaneProSkin) tabDockContainer.getArea().getNode().getSkin();
            TabPaneProSkin.TabHeaderArea tabHeaderArea = skin.getTabHeaderArea();
            return new BoundingBox(
                    Math.floor(eventSceneXY.getFirst()),
                    Math.floor(eventSceneXY.getSecond() + tabHeaderArea.getHeight()),
                    Math.floor(eventContainer.getWidth()),
                    Math.floor(eventContainer.getHeight() - tabHeaderArea.getHeight()));
        }

        private static Bounds createHalfIndicatorBounds(Side side, ContainerPosition anchorPos) {
            var anchorContainer = anchorPos.getContainer();
            var ratio = ONE_HALF;
            double width;
            double height;
            var anchorSceneXY = getSceneXY(anchorContainer);
            double x = anchorSceneXY.getFirst();
            double y = anchorSceneXY.getSecond();
            switch (side) {
                case TOP:
                    width = anchorContainer.getWidth();
                    height = anchorContainer.getHeight() * ratio;
                    break;
                case RIGHT:
                    height = anchorContainer.getHeight();
                    width = anchorContainer.getWidth() * ratio;
                    x += anchorContainer.getWidth() - width;
                    break;
                case BOTTOM:
                    width = anchorContainer.getWidth();
                    height = anchorContainer.getHeight() * ratio;
                    y += anchorContainer.getHeight() - height;
                    break;
                case LEFT:
                    height = anchorContainer.getHeight();
                    width = anchorContainer.getWidth() * ratio;
                    break;
                default:
                    throw new AssertionError();
            }
            // Math.floor() gives correct results while snapSize doesn't
            return new BoundingBox(Math.floor(x), Math.floor(y), Math.floor(width), Math.floor(height));
        }

        private static Bounds createThirdIndicatorBounds(Side side, ContainerPosition anchorPos,
                ContainerPosition ancestorPos) {
            var anchorContainer = anchorPos.getContainer();
            var ancestorContainer = ancestorPos.getContainer();
            var ratio = ONE_THIRD;
            double width;
            double height;
            var anchorSceneXY = getSceneXY(anchorContainer);
            var ancestorSceneXY = getSceneXY(ancestorContainer);
            double x = ancestorSceneXY.getFirst();
            double y = ancestorSceneXY.getSecond();
            switch (side) {
                case TOP:
                    width = ancestorContainer.getWidth();
                    height = anchorContainer.getHeight() * ratio;
                    break;
                case RIGHT:
                    height = ancestorContainer.getHeight();
                    width = anchorContainer.getWidth() * ratio;
                    x = anchorSceneXY.getFirst() + anchorContainer.getWidth() - width;
                    break;
                case BOTTOM:
                    width = ancestorContainer.getWidth();
                    height = anchorContainer.getHeight() * ratio;
                    y = anchorSceneXY.getSecond() + anchorContainer.getHeight() - height;
                    break;
                case LEFT:
                    height = ancestorContainer.getHeight();
                    width = anchorContainer.getWidth() * ratio;
                    x = anchorSceneXY.getFirst();
                    break;
                default:
                    throw new AssertionError();
            }
            // Math.floor() gives correct results while snapSize doesn't
            return new BoundingBox(Math.floor(x), Math.floor(y), Math.floor(width), Math.floor(height));
        }

        private static Bounds createIntermediateIndicatorBounds(Side side, ContainerPosition parentPos,
                ContainerPosition anchorPos, ContainerPosition siblingPos) {
            SplitPaneContainer splitSpaceContainer = (SplitPaneContainer) parentPos.getContainer();
            var splitPane = splitSpaceContainer.getSplitPane();
            var anchorContainer = anchorPos.getContainer();
            double width;
            double height;
            var anchorSceneXY = getSceneXY(anchorContainer);
            double x = anchorSceneXY.getFirst();
            double y = anchorSceneXY.getSecond();
            AbstractContainer siblingContainer = siblingPos.getContainer();
            double anchorHeight = splitPane.snapSizeY(anchorContainer.getHeight());
            double siblingHeight = splitPane.snapSizeY(siblingContainer.getHeight());
            double anchorWidth = splitPane.snapSizeX(anchorContainer.getWidth());
            double siblingtWidth = splitPane.snapSizeX(siblingContainer.getWidth());
            switch (side) {
                case TOP:
                    width = anchorContainer.getWidth();
                    height = computeIntermediateContainerSize(anchorHeight, siblingHeight);
                    height = splitPane.snapSizeY(height);
                    var siblingSceneXY = getSceneXY(siblingContainer);
                    y = siblingSceneXY.getSecond() + siblingContainer.getHeight()
                            - (height * siblingPos.getFraction());
                    break;
                case RIGHT:
                    height = anchorContainer.getHeight();
                    width = computeIntermediateContainerSize(anchorWidth, siblingtWidth);
                    width = splitPane.snapSizeX(width);
                    x += (anchorWidth - width * anchorPos.getFraction());
                    break;
                case BOTTOM:
                    width = anchorContainer.getWidth();
                    height = computeIntermediateContainerSize(anchorHeight, siblingHeight);
                    height = splitPane.snapSizeY(height);
                    y += (anchorContainer.getHeight() - height * anchorPos.getFraction());
                    break;
                case LEFT:
                    height = anchorContainer.getHeight();
                    width = computeIntermediateContainerSize(anchorWidth, siblingtWidth);
                    width = splitPane.snapSizeX(width);
                    siblingSceneXY = getSceneXY(siblingContainer);
                    x = siblingSceneXY.getFirst() + siblingContainer.getWidth()
                            - (width * siblingPos.getFraction());
                    break;
                default:
                    throw new AssertionError();
            }
            // Math.floor() gives correct results while snapSize doesn't
            var result = new BoundingBox(Math.floor(x), Math.floor(y), Math.floor(width), Math.floor(height));
            return result;
        }

        private static void setContainerFractions(Side side, ContainerPosition anchorPos,
                ContainerPosition siblingPos) {
            double anchorContainerSize;
            double anchorContainerFraction = 0.0;
            double siblingContainerSize;
            double siblingContainerFraction = 0.0;
            var anchorContainer = anchorPos.getContainer();
            var siblingContainer = siblingPos.getContainer();
            if (side.isHorizontal()) {
                anchorContainerSize = anchorContainer.getHeight();
                siblingContainerSize = siblingContainer.getHeight();
            } else {
                anchorContainerSize = anchorContainer.getWidth();
                siblingContainerSize = siblingContainer.getWidth();
            }
            double totalSize = anchorContainerSize + siblingContainerSize;
            anchorContainerFraction = anchorContainerSize / totalSize;
            siblingContainerFraction = siblingContainerSize / totalSize;
            anchorPos.setFraction(anchorContainerFraction);
            siblingPos.setFraction(siblingContainerFraction);
        }

        private static double computeIntermediateContainerSize(double firstContainerSize, double secondContainerSize) {
            return ONE_THIRD * (firstContainerSize + secondContainerSize);
        }

        private static ContainerPosition createSiblingPosition(ContainerPosition parentPosition,
                ContainerPosition anchorPosition, int siblingIndex) {
            SplitPane splitPane = ((SplitPaneContainer) parentPosition.getContainer()).getSplitPane();
            var siblingContainer = ((AbstractContainer) splitPane.getItems().get(siblingIndex));
            var singlingPosition = siblingContainer.resolvePosition();
            return singlingPosition;
        }

        private static Pair<Double, Double> getSceneXY(Region container) {
            Bounds mainBounds = container.getBoundsInLocal();
            Point2D mainTopLeftCorner = new Point2D(mainBounds.getMinX(), mainBounds.getMinY());
            Point2D mainTopLeftInScene = container.localToScene(mainTopLeftCorner);
            return new Pair<>(container.snapSizeX(mainTopLeftInScene.getX()),
                    container.snapSizeY(mainTopLeftInScene.getY()));
        }

        private final DockHostFxView<?> dockHost;

        private final Transformer transformer;

        DropPositionResolver(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
            this.transformer = dockHost.transformer;
        }

        /**
         * Resolves the {@link TabDockOperation} to attach to the transformation about to be prepared, for logging
         * purposes only.
         * <p>
         * Whether the user is dragging a single Tab (which will become a brand-new TabDock, {@link
         * TabDockOperation#ADD}) or an existing TabDock (which is being relocated, {@link TabDockOperation#ARRIVE})
         * is already known via {@link DragAndDropHandler#dragDock} at this point in the gesture. Neither value
         * causes {@link SpaceResolver} to be consulted here — see {@link TabDockOperation} and {@link SpaceResolver}
         * for the full rule.
         */
        private TabDockOperation resolveOperation() {
            return dockHost.dragAndDropHandler.dragDock == null ? TabDockOperation.ADD : TabDockOperation.ARRIVE;
        }

        /**
         * Provides a drop position during drag-and-drop when the mouse is over the tab header area.
         *
         * @param mousePos
         * @param tabDockContainer
         * @return
         */
        private DropPosition provideTabAreaPosition(MousePosition mousePos, TabDockContainer tabDockContainer) {
            var dropPosition = tryReusePreviousPosition(mousePos);
            if (dropPosition == null) {
                dropPosition = createTabAreaPosition(mousePos, tabDockContainer);
            }
            return dropPosition;
        }

        /**
         * Creates a drop position during drag-and-drop when the mouse is over the tab header area.
         *
         * @param mousePos
         * @param tabDockContainer
         * @return
         */
        private DropPosition createTabAreaPosition(MousePosition mousePos, TabDockContainer tabDockContainer) {
            DropPosition dropPosition = new DropPosition();
            dropPosition.setMousePosition(mousePos);
            var indicatorBounds = createTabPaneIndicatorBounds(mousePos, tabDockContainer);
            dropPosition.setIndicatorBounds(indicatorBounds);
            DockSplitPane splitPane = tabDockContainer.getLogicalParent().getSplitPane();
            SplitPaneContainer splitPaneContainer = ContainerUtils.getContainer(splitPane);
            dropPosition.setIndicatorPosition(splitPaneContainer.resolvePosition());
            dropPosition.setValid(true);
            return dropPosition;
        }

        /**
         * Provides a dock position properties during drag-and-drop when the mouse is outside the tab header area.
         *
         * @param position
         * @return
         */
        private DropPosition provideBasePosition(MousePosition position) {
            var dropPos = tryReusePreviousPosition(position);
            if (dropPos == null) {
                dropPos = createBasePosition(position);
            }
            return dropPos;
        }

        /**
         * Creates a dock position properties during drag-and-drop when the mouse is outside
         * the tab header area.
         *
         * @param position
         * @return
         */
        private DropPosition createBasePosition(MousePosition position) {
            DropPosition dropPos = new DropPosition();
            dropPos.setMousePosition(position);
            AbstractAreaContainer<?> eventContainer = (AbstractAreaContainer<?>) position.getEventContainer();
            var eventPosition = eventContainer.resolvePosition();
            dropPos.setEventPosition(eventPosition);
            var parentContainer = eventPosition.getContainer().getLogicalParent();
            var parentSplitPane = parentContainer.getSplitPane();
            var parentPosition = parentContainer.resolvePosition();
            DockSplitPane grandparentSplitPane = null;
            ContainerPosition grandparentPosition = null;
            DockSplitPane greatGrandparentSplitPane = null;
            ContainerPosition greatGrandparentPosition = null;
            if (parentPosition.getContainer() != dockHost.getComposer().rootContainer) {
                var grandparentContainer = parentContainer.getLogicalParent();
                grandparentSplitPane = grandparentContainer.getSplitPane();
                grandparentPosition = grandparentContainer.resolvePosition();
                if (grandparentPosition.getContainer() != dockHost.getComposer().rootContainer) {
                    var greatGrandparentContainer = grandparentContainer.getLogicalParent();
                    greatGrandparentSplitPane = greatGrandparentContainer.getSplitPane();
                    greatGrandparentPosition = greatGrandparentContainer.resolvePosition();
                }
            }
            dropPos.setParentPosition(parentPosition);
            dropPos.setGrandparentPosition(grandparentPosition);
            dropPos.setGreatGrandparentPosition(greatGrandparentPosition);
            if (parentSplitPane.getOrientation() == Orientation.HORIZONTAL) {
                prepareForHorizontalSpace(dropPos);
            } else {
                prepareForVerticalSpace(dropPos);
            }
            validate(dropPos);
            return dropPos;
        }

        private @Nullable DropPosition tryReusePreviousPosition(MousePosition position) {
            if (dockHost.dropPosition != null && dockHost.dropPosition.getMousePosition().equals(position)) {
                return dockHost.dropPosition;
            }
            return null;
        }

        private void prepareForHorizontalSpace(final DropPosition dropPos) {
            var eventPosition = dropPos.getEventPosition();
            var mousePosition = dropPos.getMousePosition();
            if (mousePosition.isEdgeMode()) {
                switch (mousePosition.getSide()) {
                    case TOP:
                        prepareForOppositeOrientationOnEdge(dropPos, TOP);
                        break;
                    case RIGHT:
                        prepareForSameOrientationOnEdge(dropPos, RIGHT);
                        break;
                    case BOTTOM:
                        prepareForOppositeOrientationOnEdge(dropPos, BOTTOM);
                        break;
                    case LEFT:
                        prepareForSameOrientationOnEdge(dropPos, LEFT);
                        break;
                    default:
                        throw new AssertionError();
                }
            } else {
                dropPos.setIndicatorPosition(dropPos.getParentPosition());
                dropPos.setIndicatorBounds(createHalfIndicatorBounds(mousePosition.getSide(), eventPosition));
                switch (mousePosition.getSide()) {
                    case TOP:
                        prepareForOppositeOrientationOffEdge(dropPos, TOP);
                        break;
                    case RIGHT:
                        prepareForSameOrientationOffEdge(dropPos, RIGHT);
                        break;
                    case BOTTOM:
                        prepareForOppositeOrientationOffEdge(dropPos, BOTTOM);
                        break;
                    case LEFT:
                        prepareForSameOrientationOffEdge(dropPos, LEFT);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        private void prepareForVerticalSpace(DropPosition dropPos) {
            var eventPosition = dropPos.getEventPosition();
            var mousePosition = dropPos.getMousePosition();
            if (mousePosition.isEdgeMode()) {
                switch (mousePosition.getSide()) {
                    case TOP:
                        prepareForSameOrientationOnEdge(dropPos, TOP);
                        break;
                    case RIGHT:
                        prepareForOppositeOrientationOnEdge(dropPos, RIGHT);
                        break;
                    case BOTTOM:
                        prepareForSameOrientationOnEdge(dropPos, BOTTOM);
                        break;
                    case LEFT:
                        prepareForOppositeOrientationOnEdge(dropPos, LEFT);
                        break;
                    default:
                        throw new AssertionError();
                }
            } else {
                dropPos.setIndicatorPosition(dropPos.getParentPosition());
                dropPos.setIndicatorBounds(createHalfIndicatorBounds(mousePosition.getSide(), eventPosition));
                switch (mousePosition.getSide()) {
                    case TOP:
                        prepareForSameOrientationOffEdge(dropPos, TOP);
                        break;
                    case RIGHT:
                        prepareForOppositeOrientationOffEdge(dropPos, RIGHT);
                        break;
                    case BOTTOM:
                        prepareForSameOrientationOffEdge(dropPos, BOTTOM);
                        break;
                    case LEFT:
                        prepareForOppositeOrientationOffEdge(dropPos, LEFT);
                        break;
                    default:
                        throw new AssertionError();
                }
            }
        }

        private void prepareForSameOrientationOnEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;
            var eventPosition = dropPos.getEventPosition();
            var operation = resolveOperation();
            Consumer<TabDockFxView<?>> transformation = null;
            boolean isBoundary = isFirst ? eventPosition.isFirst() : eventPosition.isLast();
            int indexDelta = isFirst ? 0 : 1;
            int siblingDelta = isFirst ? -1 : 1;
            int caseIndex;
            if (isBoundary) {
                if (dropPos.getGrandparentPosition() == null) {
                    transformation = (newTabDock) -> {
                        transformer.addTabDock(side, eventPosition, null, dropPos.getNewPosition(), newTabDock,
                                operation);
                    };
                    dropPos.setNewPosition(new ContainerPosition(eventPosition.getIndex() + indexDelta, ONE_THIRD));
                    dropPos.setIndicatorPosition(dropPos.getParentPosition());
                    dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, dropPos.getEventPosition(),
                            dropPos.getParentPosition()));
                    caseIndex = 0;
                } else {
                    if (dropPos.getGreatGrandparentPosition() == null) {
                        Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                        transformation = (tabDock) -> {
                            transformer.wrapAndAddTabDock(orientation, dropPos.getGrandparentPosition(),
                                    dropPos.getNewPosition(), tabDock, operation);
                        };
                        dropPos.setNewPosition(new ContainerPosition(isFirst ? 0 : 1, ONE_THIRD));
                        dropPos.setIndicatorPosition(dropPos.getGrandparentPosition());
                        dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, dropPos.getEventPosition(),
                                dropPos.getGrandparentPosition()));
                        caseIndex = 1;
                    } else {
                        transformation = (newTabDock) -> {
                            transformer.addTabDock(side, dropPos.getGrandparentPosition(), null,
                                    dropPos.getNewPosition(), newTabDock, operation);
                        };
                        dropPos.setNewPosition(new ContainerPosition(dropPos.getGrandparentPosition().getIndex()
                                + indexDelta, ONE_THIRD));
                        dropPos.setIndicatorPosition(dropPos.getGreatGrandparentPosition());
                        dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, dropPos.getEventPosition(),
                                dropPos.getGreatGrandparentPosition()));
                        caseIndex = 2;
                    }
                }
            } else {
                var siblingIndex = eventPosition.getIndex() + siblingDelta;
                var siblingPosition = createSiblingPosition(dropPos.getParentPosition(), eventPosition, siblingIndex);
                dropPos.setIndicatorPosition(dropPos.getParentPosition());
                setContainerFractions(side, eventPosition, siblingPosition);
                dropPos.setIndicatorBounds(createIntermediateIndicatorBounds(side, dropPos.getParentPosition(),
                        eventPosition, siblingPosition));
                dropPos.setNewPosition(new ContainerPosition(eventPosition.getIndex() + indexDelta, ONE_THIRD));
                transformation = (newTabDock) -> {
                    transformer.addTabDock(side, eventPosition, siblingPosition, dropPos.getNewPosition(), newTabDock,
                            operation);
                };
                caseIndex = 3;
            }
            dropPos.setTransformation(transformation);
            logger.trace("{} Prepared drop position for same orientation on edge; side: {}, case: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, caseIndex, dropPos);
        }

        private void prepareForOppositeOrientationOnEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;
            var eventPosition = dropPos.getEventPosition();
            var operation = resolveOperation();
            Consumer<TabDockFxView<?>> transformation = null;
            dropPos.setIndicatorPosition(dropPos.getParentPosition());
            dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, eventPosition, dropPos.getParentPosition()));
            int caseIndex;
            if (dropPos.getGrandparentPosition() == null) {
                caseIndex = 0;
                Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                transformation = (tabDock) -> {
                    transformer.wrapAndAddTabDock(orientation, dropPos.getParentPosition(), dropPos.getNewPosition(),
                            tabDock, operation);
                };
                dropPos.setNewPosition(new ContainerPosition(isFirst ? 0 : 1, ONE_THIRD));
            } else {
                caseIndex = 1;
                int index = dropPos.getParentPosition().getIndex() + (isFirst ? 0 : 1);
                transformation = (newTabDock) -> {
                    transformer.addTabDock(side, dropPos.getParentPosition(), null, dropPos.getNewPosition(),
                            newTabDock, operation);
                };
                dropPos.setNewPosition(new ContainerPosition(index, ONE_THIRD));
            }
            dropPos.setTransformation(transformation);
            logger.trace("{} Prepared drop position for opposite orientation on edge; side: {}, case: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, caseIndex, dropPos);
        }

        private void prepareForSameOrientationOffEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;
            var operation = resolveOperation();
            Consumer<TabDockFxView<?>> transformation = (newTabDock) -> {
                transformer.addTabDock(dropPos.getMousePosition().getSide(), dropPos.getEventPosition(), null,
                        dropPos.getNewPosition(), newTabDock, operation);
            };
            dropPos.setTransformation(transformation);
            int index = dropPos.getEventPosition().getIndex() + (isFirst ? 0 : 1);
            dropPos.setNewPosition(new ContainerPosition(index, ONE_HALF));
            logger.trace("{} Prepared drop position for same orientation off edge; side: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, dropPos);
        }

        private void prepareForOppositeOrientationOffEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;
            var operation = resolveOperation();
            Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            Consumer<TabDockFxView<?>> transformation = (tabDock) -> {
                transformer.wrapAndAddTabDock(orientation, dropPos.getEventPosition(), dropPos.getNewPosition(),
                        tabDock, operation);
            };
            dropPos.setTransformation(transformation);
            int index = isFirst ? 0 : 1;
            dropPos.setNewPosition(new ContainerPosition(index, ONE_HALF));
            logger.trace("{} Prepared drop position for opposite orientation off edge; side: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, dropPos);
        }

        private void validate(DropPosition info) {
            info.setValid(true);
            if (dockHost.dragAndDropHandler.dragDock != null) {
                var dragDockContainer = ContainerUtils.getContainer(dockHost.dragAndDropHandler.dragDock);
                DockSplitPane splitPane = dragDockContainer.getLogicalParent().getSplitPane();
                if (info.getEventPosition().getContainer().getLogicalParent().getSplitPane()
                        == dragDockContainer.getLogicalParent().getSplitPane()) {
                    var dragDockPos = dragDockContainer.resolvePosition();
                    var dragDockIndex = dragDockPos.getIndex();
                    var eventIndex = info.getEventPosition().getIndex();
                    var side = info.getMousePosition().getSide();
                    var edgeMd = info.getMousePosition().isEdgeMode();
                    var same = dragDockIndex == eventIndex;
                    same = same && ((splitPane.getOrientation() == Orientation.HORIZONTAL && side.isVertical())
                            || (splitPane.getOrientation() == Orientation.HORIZONTAL && side.isHorizontal() && !edgeMd)
                            || (splitPane.getOrientation() == Orientation.VERTICAL && side.isHorizontal())
                            || (splitPane.getOrientation() == Orientation.VERTICAL && side.isVertical() && !edgeMd));
                    var previous = dragDockIndex == eventIndex + 1
                            && ((splitPane.getOrientation() == Orientation.HORIZONTAL && side == RIGHT)
                                || (splitPane.getOrientation() == Orientation.VERTICAL && side == BOTTOM));
                    var next = dragDockIndex == eventIndex - 1
                            && ((splitPane.getOrientation() == Orientation.HORIZONTAL && side == LEFT)
                                || (splitPane.getOrientation() == Orientation.VERTICAL && side == TOP));
                    if (same || previous || next) {
                        info.setValid(false);
                    }
                }
            }
        }
    }

    private static final class Transformer {

        /**
         * Converts a live container into the {@link ModelNode} that represents it — an {@link AreaNode} for a leaf,
         * or a {@link GroupNode} for a nested SplitPane.
         */
        private static ModelNode toModelNode(AbstractContainer container) {
            return container instanceof SplitPaneContainer spc
                    ? new GroupNodeImpl(spc)
                    : new AreaNodeImpl((AbstractAreaContainer<?>) container);
        }

        private static Set<Integer> toOldIndices(List<AbstractContainer> chosen, List<Node> oldOrder) {
            var indices = new HashSet<Integer>();
            for (var c : chosen) {
                indices.add(oldOrder.indexOf(c));
            }
            return indices;
        }

        private final DockHostFxView<?> dockHost;

        private final DockHostFxView<?>.Composer composer;

        Transformer(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
            this.composer = dockHost.getComposer();
        }

        /**
         * Wraps {@code container} in a new split pane with the opposite orientation, replacing it at its current
         * position in its logical parent (or as root, if it has none).
         *
         * @param container the container to wrap; must currently be live
         * @param index the index of {@code container} within its parent's live items
         */
        private SplitPaneContainer wrap(AbstractContainer container, int index) {
            var oldLogicalParent = container.getLogicalParent(); // null if container is currently root
            Orientation currentOrientation;
            if (composer.rootContainer != container) {
                currentOrientation = oldLogicalParent.getSplitPane().getOrientation();
            } else {
                currentOrientation = composer.rootContainer.getSplitPane().getOrientation();
            }
            var newOrientation =
                    currentOrientation == Orientation.HORIZONTAL ? Orientation.VERTICAL : Orientation.HORIZONTAL;
            var newSplitPane = new DockSplitPane(dockHost.getDescriptor().getLogPrefix());
            newSplitPane.setOrientation(newOrientation);
            var newSplitPaneContainer = ContainerUtils.createContainer(dockHost, newSplitPane);
            double[] parentOldPositions = null;
            if (oldLogicalParent == null) {
                composer.setRoot(newSplitPaneContainer);
            } else {
                parentOldPositions = oldLogicalParent.getSplitPane().getDividerPositions();
                // container leaves its old parent entirely — it's moving into the new split, not just its
                // live items — so this is a permanent removal from the old parent's perspective
                oldLogicalParent.removePermanently(container);
                oldLogicalParent.insertNew(index, newSplitPaneContainer);
            }
            newSplitPaneContainer.insertNew(0, container);
            if (parentOldPositions != null) {
                oldLogicalParent.getSplitPane().setDividerPositions(parentOldPositions);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Wrapped {} into {}", dockHost.getDescriptor().getLogPrefix(),
                        container.getChildFullName(), newSplitPane.getFullName());
                dockHost.printTreeDebugInfo();
            }
            return newSplitPaneContainer;
        }

        /**
         * Collapses {@code splitPaneContainer}, which has at most one logical child left, by splicing that child's
         * own logical children (or the child itself, if it is a leaf) directly into {@code splitPaneContainer}'s
         * former place in its parent — preserving each spliced child's live/minimized state and its logical parent
         * reference.
         * <p>
         * Called only when {@link SplitPaneContainer#shouldBeNormalized()} is {@code true}.
         */
        private void unwrap(SplitPaneContainer splitPaneContainer) {
            var logicalItems = splitPaneContainer.getLogicalItems();
            AbstractContainer onlyChild = logicalItems.isEmpty() ? null : logicalItems.get(0);
            if (splitPaneContainer != composer.rootContainer) {
                var grandparentContainer = splitPaneContainer.getLogicalParent();
                var grandparentSplitPane = grandparentContainer.getSplitPane();
                var index = grandparentContainer.getLogicalItems().indexOf(splitPaneContainer);
                List<AbstractContainer> spliced;
                double[] childPositions;
                DockSplitPane sourceSplitPane;
                if (onlyChild == null) {
                    spliced = List.of();
                    childPositions = new double[0];
                    sourceSplitPane = null;
                } else if (onlyChild instanceof SplitPaneContainer nestedSplit) {
                    spliced = nestedSplit.getLogicalItems();
                    childPositions = nestedSplit.getSplitPane().getDividerPositions();
                    sourceSplitPane = nestedSplit.getSplitPane();
                } else {
                    spliced = List.of(onlyChild);
                    childPositions = new double[0];
                    sourceSplitPane = splitPaneContainer.getSplitPane();
                }
                var oldPositions = grandparentSplitPane.getDividerPositions();
                grandparentContainer.removePermanently(splitPaneContainer);
                for (var i = 0; i < spliced.size(); i++) {
                    var child = spliced.get(i);
                    var wasLive = sourceSplitPane.isLive(child);
                    grandparentContainer.insertNew(index + i, child);
                    if (!wasLive) {
                        grandparentContainer.minimizeChild(child);
                    }
                }
                refresh();
                grandparentSplitPane.updateDividersOnUnwrap(index, oldPositions, childPositions);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} Unwrapped {} into {}", dockHost.getDescriptor().getLogPrefix(),
                            spliced.stream().map(AbstractContainer::getChildFullName).collect(Collectors.joining(", ")),
                            grandparentSplitPane.getFullName());
                }
            } else {
                if (onlyChild instanceof SplitPaneContainer spc) {
                    spc.setLogicalParent(null);
                    composer.setRoot(spc);
                    logger.debug("{} Unwrapped {} and set it as a root", dockHost.getDescriptor().getLogPrefix(),
                            spc.getChildFullName());

                }
            }
            dockHost.printTreeDebugInfo();
        }

        /**
         * Walks up from {@code start}, collapsing any split pane that has at most one logical child left, stopping
         * at the first ancestor that still has more than one (or at the root).
         */
        private void normalizeUpward(SplitPaneContainer start) {
            var current = start;
            while (current != null && current.shouldBeNormalized()) {
                var parent = current.getLogicalParent(); // null once current is root
                unwrap(current);
                current = parent;
            }
        }

        /**
         * Resolves which container(s) participate in a space change, consulting {@code dock}'s {@link SpaceResolver}
         * only when more than one candidate exists among {@code previousItems}/{@code nextItems}.
         * <p>
         * Not called at all for {@link TabDockOperation#ADD}/{@link TabDockOperation#ARRIVE} when the insertion
         * position was determined by a drag-and-drop drop indicator — those call sites keep the donor implied by
         * the indicator geometry instead. See {@link SpaceResolver} for the full rule.
         *
         * @param operation why space is being resolved; passed through to the resolver and used for logging
         * @param dock the TabDock (or, for donation, the area being added) whose {@link SpaceResolver} governs this
         *         resolution
         * @param previousItems live siblings before the insertion/removal point, in order
         * @param nextItems live siblings after the insertion/removal point, in order
         * @return the chosen container(s); empty only if both {@code previousItems} and {@code nextItems} are empty
         * @throws IllegalArgumentException if the resolver returns an empty list, a duplicate, or a node not offered
         */
        private List<AbstractContainer> resolveParticipants(TabDockOperation operation, TabDockFxView<?> dock,
                List<AbstractContainer> previousItems, List<AbstractContainer> nextItems) {
            if (previousItems.isEmpty() && nextItems.isEmpty()) {
                return List.of(); // nothing to donate/receive from — e.g. the very first item in an empty SplitPane
            }
            if (previousItems.size() + nextItems.size() == 1) {
                return previousItems.isEmpty() ? nextItems : previousItems;
            }
            var previousNodes = previousItems.stream().map(Transformer::toModelNode).toList();
            var nextNodes = nextItems.stream().map(Transformer::toModelNode).toList();
            var resolver = dock.getComposer().getSpaceResolver();
            var chosen = resolver.resolve(operation, previousNodes, nextNodes);
            if (chosen.isEmpty()) {
                throw new IllegalArgumentException("SpaceResolver for " + operation + " on "
                        + dock.getDescriptor().getFullName() + " returned an empty list");
            }
            var offered = new LinkedHashSet<AbstractContainer>();
            offered.addAll(previousItems);
            offered.addAll(nextItems);
            var seen = new HashSet<AbstractContainer>();
            var result = new ArrayList<AbstractContainer>(chosen.size());
            for (var node : chosen) {
                var container = ContainerUtils.resolveContainer(node);
                if (!offered.contains(container)) {
                    throw new IllegalArgumentException("SpaceResolver for " + operation + " on "
                            + dock.getDescriptor().getFullName() + " returned a node that was not offered: "
                            + container.getChildFullName());
                }
                if (!seen.add(container)) {
                    throw new IllegalArgumentException("SpaceResolver for " + operation + " on "
                            + dock.getDescriptor().getFullName() + " returned a duplicate node: "
                            + container.getChildFullName());
                }
                result.add(container);
            }
            return result;
        }

        /**
         * This method adds a new TabDock when the user selects the area using mouse.
         * <p>
         * The donor is fully determined by the drop indicator geometry already shown to the user — {@link
         * SpaceResolver} is never consulted here. {@code operation} is passed through only for logging. See {@link
         * SpaceResolver} for the full rule.
         */
        private void addTabDock(Side side, ContainerPosition anchorInfo, ContainerPosition siblingInfo,
                ContainerPosition newInfo, TabDockFxView<?> newTabDock, TabDockOperation operation) {
            newTabDock.getComposer().setDockHost(dockHost);
            var parentContainer = anchorInfo.getContainer().getLogicalParent();
            var splitPane = parentContainer.getSplitPane();
            double[] oldPositions = splitPane.getDividerPositions();
            parentContainer.insertNew(newInfo.getIndex(), ContainerUtils.createContainer(dockHost, newTabDock));
            if (siblingInfo == null) {
                if (newInfo.getFraction() == ONE_HALF) {
                    splitPane.updateDividersOnHalfSplit(anchorInfo.getIndex(), oldPositions);
                } else if (newInfo.getFraction() == ONE_THIRD) {
                    splitPane.updateDividersOnThirdSplit(anchorInfo.getIndex(), oldPositions, side);
                } else {
                    throw new AssertionError();
                }
            } else {
                double beforeProportion;
                double afterProportion;
                if (side == RIGHT || side == BOTTOM) {
                    beforeProportion = anchorInfo.getFraction();
                    afterProportion = siblingInfo.getFraction();
                } else {
                    afterProportion = anchorInfo.getFraction();
                    beforeProportion = siblingInfo.getFraction();
                }
                splitPane.updateDividersOnInsertBetween(newInfo.getIndex(), beforeProportion, afterProportion,
                        oldPositions);
            }

            logger.debug("{} Added {} into {}; operation: {}", dockHost.getDescriptor().getLogPrefix(),
                    newTabDock.getDescriptor().getFullName(), splitPane.getFullName(), operation);

        }

        /**
         * Adds a new TabDock at the given side of an existing container, which may be either a leaf ({@link
         * AbstractAreaContainer}) or a group ({@link SplitPaneContainer}).
         * <p>
         * If the container's logical parent already has the orientation implied by {@code side}, the new TabDock is
         * inserted as a direct sibling next to the container within that same parent. Otherwise, the container is
         * first {@linkplain #wrap(AbstractContainer, int) wrapped} in a new group with the orientation {@code side}
         * implies, and the new TabDock is inserted into that new group instead — in which case the wrapped anchor
         * ends up as the sole live sibling in the target SplitPane, so {@link #resolveParticipants} resolves to it
         * directly without consulting {@link SpaceResolver}.
         *
         * @param anchorContainer the container to add the new TabDock next to; must currently be live
         * @param side the side of {@code anchorContainer} the new TabDock should occupy
         * @param dock the TabDock to add
         * @param size the desired size (width for {@code LEFT}/{@code RIGHT}, height for {@code TOP}/{@code BOTTOM})
         *         of the new TabDock
         * @param operation why this TabDock is being added; passed to {@link SpaceResolver} and used for logging
         */
        private void addTabDock(AbstractContainer anchorContainer, Side side, TabDockFxView<?> dock, double size,
                TabDockOperation operation) {
            dock.getComposer().setDockHost(dockHost);
            var neededOrientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            var parentContainer = anchorContainer.getLogicalParent();
            SplitPaneContainer targetParent;
            int index;
            if (parentContainer != null && parentContainer.getSplitPane().getOrientation() == neededOrientation) {
                targetParent = parentContainer;
                var anchorIndex = targetParent.getSplitPane().getItems().indexOf(anchorContainer);
                index = (side == LEFT || side == TOP) ? anchorIndex : anchorIndex + 1;
            } else {
                var anchorIndex = anchorContainer.resolvePosition().getIndex();
                targetParent = wrap(anchorContainer, anchorIndex);
                // let the freshly created group pick up the anchor's current size before we measure it below
                refresh();
                index = (side == LEFT || side == TOP) ? 0 : 1;
            }
            var splitPane = targetParent.getSplitPane();
            var oldItems = List.copyOf(splitPane.getItems());
            var previousItems = (List<AbstractContainer>) (List<?>) oldItems.subList(0, index);
            var nextItems = (List<AbstractContainer>) (List<?>) oldItems.subList(index, oldItems.size());
            var participants = resolveParticipants(operation, dock, previousItems, nextItems);
            var donorIndices = toOldIndices(participants, oldItems);
            var oldSplitPaneSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                    ? splitPane.getWidth() : splitPane.getHeight();
            var dividerSize = splitPane.computeDividerSize();
            var oldPositions = splitPane.getDividerPositions();
            var dockContainer = ContainerUtils.createContainer(dockHost, dock);
            targetParent.insertNew(index, dockContainer);
            refresh();
            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainIndex = indexOfMain(targetParent);
            if (mainIndex >= 0) {
                splitPane.updateDividersOnAddWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainIndex, index,
                        size, donorIndices);
            } else {
                splitPane.updateDividersOnAddWithoutMain(oldSplitPaneSize, oldPositions, dividerSize, index, size,
                        donorIndices);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} at {} of {}; donors: {}, operation: {}",
                        dockHost.getDescriptor().getLogPrefix(), dock.getDescriptor().getFullName(), side,
                        anchorContainer.getChildFullName(),
                        participants.stream().map(AbstractContainer::getChildFullName).toList(), operation);
                dockHost.printTreeDebugInfo();
            }
        }

        /**
         * This method adds a new TabDock during restoring or adding a tab dock to a specific side.
         */
        private void addTabDock(SplitPaneContainer parentContainer, TabDockFxView<?> dock, int index, Side side,
                boolean sideShouldBeChecked, double size, TabDockOperation operation) {
            dock.getComposer().setDockHost(dockHost);
            if (sideShouldBeChecked && !checkNewSide(parentContainer, index, side)) {
                boolean wrapParent = false;
                if (parentContainer != composer.rootContainer) {
                    parentContainer = composer.rootContainer;
                    index = resolveNewIndex(parentContainer, side);
                    if (!checkNewSide(parentContainer, index, side)) {
                        wrapParent = true;
                    }
                } else {
                    wrapParent = true;
                }
                if (wrapParent) {
                    parentContainer = wrap(parentContainer, parentContainer.resolvePosition().getIndex());
                    index = resolveNewIndex(parentContainer, side);
                }
            }
            final var splitPane = parentContainer.getSplitPane();
            var oldItems = List.copyOf(splitPane.getItems());
            var previousItems = (List<AbstractContainer>)
                    (List<?>) oldItems.subList(0, Math.min(index, oldItems.size()));
            var nextItems = (List<AbstractContainer>) (List<?>) oldItems.subList(Math.min(index, oldItems.size()),
                    oldItems.size());
            var participants = resolveParticipants(operation, dock, previousItems, nextItems);
            var donorIndices = toOldIndices(participants, oldItems);
            var oldSplitPaneSize = splitPane.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
            }
            var dividerSize = splitPane.computeDividerSize();
            var oldPositions = splitPane.getDividerPositions();
            var dockContainer = ContainerUtils.createContainer(dockHost, dock);
            parentContainer.insertNew(index, dockContainer);
            refresh();
            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainIndex = indexOfMain(parentContainer);
            if (mainIndex >= 0) {
                splitPane.updateDividersOnAddWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainIndex, index,
                        size, donorIndices);
            } else {
                splitPane.updateDividersOnAddWithoutMain(oldSplitPaneSize, oldPositions, dividerSize, index, size,
                        donorIndices);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} into {}; donors: {}, operation: {}", dockHost.getDescriptor().getLogPrefix(),
                        dock.getDescriptor().getFullName(), dockContainer.getLogicalParent().getChildFullName(),
                        participants.stream().map(AbstractContainer::getChildFullName).toList(), operation);
                dockHost.printTreeDebugInfo();
            }
        }

        /**
         * This method adds a new TabDock when the user drops it via drag-and-drop and insertion requires wrapping.
         * <p>
         * The wrapped anchor is always the sole donor, and the split fraction is fully determined by the drop
         * indicator geometry already shown to the user — {@link SpaceResolver} is never consulted here. {@code
         * operation} is passed through only for logging. See {@link SpaceResolver} for the full rule.
         */
        private TabDockFxView<?> wrapAndAddTabDock(Orientation newOrientation, ContainerPosition anchorInfo,
                ContainerPosition newInfo, TabDockFxView<?> newTabDock, TabDockOperation operation) {
            newTabDock.getComposer().setDockHost(dockHost);
            var newSplitPaneContainer = wrap(anchorInfo.getContainer(), anchorInfo.getIndex());
            newSplitPaneContainer.insertNew(newInfo.getIndex(), ContainerUtils.createContainer(dockHost, newTabDock));
            if (newInfo.getFraction() == ONE_THIRD) {
                var splitPane = newSplitPaneContainer.getSplitPane();
                if (newOrientation == Orientation.HORIZONTAL) {
                    var pos = dockHost.dropPosition.getIndicatorBounds().getWidth()
                            / anchorInfo.getContainer().getWidth();
                    splitPane.setDividerPositions(newInfo.getIndex() == 0 ? pos : 1 - pos);
                } else {
                    var pos = dockHost.dropPosition.getIndicatorBounds().getHeight()
                            / anchorInfo.getContainer().getHeight();
                    splitPane.setDividerPositions(newInfo.getIndex() == 0 ? pos : 1 - pos);
                }
            }

            logger.debug("{} Added {} to {}; wrap, sole donor: {}, operation: {}",
                    dockHost.getDescriptor().getLogPrefix(), newTabDock.getDescriptor().getFullName(),
                    newSplitPaneContainer.getChildFullName(), anchorInfo.getContainer().getChildFullName(),
                    operation);

            return newTabDock;
        }

        /**
         * The start point for permanently removing a TabDock (close or move-departure) — not used for minimizing,
         * which keeps the dock as a logical (just non-live) child of its parent instead.
         *
         * @param operation must be {@link TabDockOperation#REMOVE} or {@link TabDockOperation#DEPART}
         */
        private void removeTabDock(TabDockFxView<?> tabDock, TabDockOperation operation) {
            var tabDockContainer = ContainerUtils.getContainer(tabDock);
            removeTabDock(tabDockContainer, operation);
        }

        private void removeTabDock(TabDockContainer tabDockContainer, TabDockOperation operation) {
            if (operation != TabDockOperation.REMOVE && operation != TabDockOperation.DEPART) {
                throw new AssertionError("removeTabDock only handles REMOVE or DEPART, got: " + operation);
            }
            var parentContainer = tabDockContainer.getLogicalParent();
            var splitPane = parentContainer.getSplitPane();
            var oldItems = List.copyOf(splitPane.getItems());
            var index = oldItems.indexOf(tabDockContainer);
            var componentToRemove = tabDockContainer.getArea();
            var previousItems = (List<AbstractContainer>) (List<?>) oldItems.subList(0, index);
            var nextItems = (List<AbstractContainer>) (List<?>) oldItems.subList(index + 1, oldItems.size());
            var participants = resolveParticipants(operation, componentToRemove, previousItems, nextItems);
            var receiverIndices = toOldIndices(participants, oldItems);
            var oldPositions = splitPane.getDividerPositions();
            var oldSplitPaneSize = splitPane.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Removing {}; receivers: {}, operation: {}", dockHost.getDescriptor().getLogPrefix(),
                    componentToRemove.getDescriptor().getFullName(),
                    participants.stream().map(AbstractContainer::getChildFullName).toList(), operation);
            }
            parentContainer.removePermanently(tabDockContainer);
            if (operation == TabDockOperation.REMOVE) {
                componentToRemove.getPresenter().deinitializeTree();
                composer.getModifiableChildren().remove(componentToRemove);
            }
            var dividerSize = splitPane.computeDividerSize();
            refresh();
            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainChildIndex = indexOfMain(parentContainer);
            if (mainChildIndex != -1) {
                splitPane.updateDividersOnRemoveWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainChildIndex,
                        index, receiverIndices);
            } else {
                splitPane.updateDividersOnRemoveWithoutMain(oldSplitPaneSize, oldPositions, dividerSize, index,
                        receiverIndices);
            }

            logger.debug("{} Removed {} from {}; operation: {}", dockHost.getDescriptor().getLogPrefix(),
                    componentToRemove.getDescriptor().getFullName(), parentContainer.getChildFullName(), operation);

            normalizeUpward(parentContainer);
        }

        private void moveTab(TabDockFxView<?> newTabDock) {
            var dragTab = dockHost.dragAndDropHandler.dragTab;
            TabPaneProSkin skin = (TabPaneProSkin) dragTab.getTabPane().getSkin();
            TabPaneProSkin.TabHeaderArea tabHeaderArea = skin.getTabHeaderArea();
            TabFxView<?> tabFxView = (TabFxView<?>) FxViewUtils.getView(dragTab);
            TabHostFxView<?> oldTabHost = (TabHostFxView<?>) tabFxView.getComposer().getParent();
            oldTabHost.getComposer().removeTab(tabFxView);
            newTabDock.getComposer().addTab(tabFxView);
            dockHost.dragAndDropHandler.dragTab = null;
            tabHeaderArea.cleanupAfterDrop();
        }

        /**
         * Minimizes a TabDock into its side bar. Unlike closing or moving a TabDock, this does not discard it from
         * its parent's logical structure — it stays a logical child of {@code parent}, just no longer live, so it
         * can be restored later without needing to reconstruct its position.
         */
        private void minimizeTabDock(TabDockFxView<?> dock) {
            var operation = TabDockOperation.MINIMIZE;
            var tabDockContainer = ContainerUtils.getContainer(dock);
            var side = resolveSide(tabDockContainer);
            var pos = new MinimizedPosition(side, dock.getNode().getWidth(), dock.getNode().getHeight());
            dock.getPresenter().setMinimizedPosition(pos);

            logger.debug("{} Minimized position for {}: {}; operation: {}", dockHost.getDescriptor().getLogPrefix(),
                    dock.getDescriptor().getFullName(), pos, operation);

            var parentContainer = tabDockContainer.getLogicalParent();
            var splitPane = parentContainer.getSplitPane();
            var oldItems = List.copyOf(splitPane.getItems());
            var index = oldItems.indexOf(tabDockContainer);
            var previousItems = (List<AbstractContainer>) (List<?>) oldItems.subList(0, index);
            var nextItems = (List<AbstractContainer>) (List<?>) oldItems.subList(index + 1, oldItems.size());
            var participants = resolveParticipants(operation, dock, previousItems, nextItems);
            var receiverIndices = toOldIndices(participants, oldItems);
            var oldPositions = splitPane.getDividerPositions();
            var oldSplitPaneSize = splitPane.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
            }
            parentContainer.minimizeChild(tabDockContainer);
            var dividerSize = splitPane.computeDividerSize();
            refresh();
            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainChildIndex = indexOfMain(parentContainer);
            if (mainChildIndex != -1) {
                splitPane.updateDividersOnRemoveWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainChildIndex,
                        index, receiverIndices);
            } else {
                splitPane.updateDividersOnRemoveWithoutMain(oldSplitPaneSize, oldPositions, dividerSize, index,
                        receiverIndices);
            }
            composer.showBar(side);
            var sideBar = dockHost.getComposer().resolveBarWrapper(side).get();
            dock.getComposer().detachTabs();
            sideBar.getComposer().addTabDock(dock);
            if (logger.isDebugEnabled()) {
                logger.debug("{} Minimized {}; receivers: {}, operation: {}", dockHost.getDescriptor().getLogPrefix(),
                        dock.getDescriptor().getFullName(),
                        participants.stream().map(AbstractContainer::getChildFullName).toList(), operation);
            }
            dockHost.printTreeDebugInfo();
        }

        /**
         * Restores a minimized TabDock back into the live tree. Its logical parent was never lost — the container
         * was only removed from live items, not from its parent's logical structure — so this is a direct re-insert,
         * with no need to search for a surviving ancestor or verify the resulting side.
         */
        private void restoreTabDock(TabDockContainer tabDockContainer) {
            var operation = TabDockOperation.RESTORE;
            var dock = tabDockContainer.getArea();
            dock.getComposer().attachTabs();
            var position = dock.getPresenter().getMinimizedPosition();
            dock.getPresenter().setMinimizedPosition(null);
            var parentContainer = tabDockContainer.getLogicalParent();
            var splitPane = parentContainer.getSplitPane();
            // The dimension to restore is dictated by the immediate parent SplitPane's own orientation, not by
            // position.getSide() — that side identifies which global SideBar (LEFT/RIGHT/BOTTOM) the dock was
            // minimized into, which is a window-relative direction and can diverge from the local parent's
            // orientation once TabDocks are nested more than one level deep (e.g. a VERTICAL split that itself sits
            // as the leftmost item of an outer HORIZONTAL split resolves to a LEFT SideBar globally, even though its
            // own children only ever vary in height).
            var dockSize = splitPane.getOrientation() == Orientation.HORIZONTAL
                    ? position.getWidth() : position.getHeight();
            var oldItems = List.copyOf(splitPane.getItems());
            var index = parentContainer.splitPane.resolveLiveInsertIndex(tabDockContainer);
            var previousItems = (List<AbstractContainer>) (List<?>) oldItems.subList(0, index);
            var nextItems = (List<AbstractContainer>) (List<?>) oldItems.subList(index, oldItems.size());
            var participants = resolveParticipants(operation, dock, previousItems, nextItems);
            var donorIndices = toOldIndices(participants, oldItems);
            var oldSplitPaneSize = splitPane.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
            }
            var dividerSize = splitPane.computeDividerSize();
            var oldPositions = splitPane.getDividerPositions();
            parentContainer.restoreChild(tabDockContainer);
            refresh();
            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainIndex = indexOfMain(parentContainer);
            if (mainIndex >= 0) {
                splitPane.updateDividersOnAddWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainIndex, index,
                        dockSize, donorIndices);
            } else {
                splitPane.updateDividersOnAddWithoutMain(oldSplitPaneSize, oldPositions, dividerSize, index, dockSize,
                        donorIndices);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Restored {} into {}; donors: {}, operation: {}",
                        dockHost.getDescriptor().getLogPrefix(), dock.getDescriptor().getFullName(),
                        parentContainer.getChildFullName(),
                        participants.stream().map(AbstractContainer::getChildFullName).toList(), operation);
                dockHost.printTreeDebugInfo();
            }
        }

        private void refresh() {
            dockHost.getNode().applyCss();
            dockHost.getNode().layout();
        }

        private int resolveNewIndex(SplitPaneContainer container, Side side) {
            int index = container.getSplitPane().getItems().size();
            if (side == LEFT || side == TOP) {
                var mainIndex = indexOfMain(container);
                if (mainIndex >= 0 && index >= mainIndex) {
                    index = mainIndex;
                }
            }
            return index;
        }

        /**
         * Checks whether inserting a new child at {@code index} within {@code parentContainer}'s live items would
         * actually place it on {@code side} relative to the main area.
         */
        private boolean checkNewSide(SplitPaneContainer parentContainer, int index, Side side) {
            var items = parentContainer.getSplitPane().getItems();
            var tempIndex = index;
            var isLastPosition = false;
            if (items.size() - 1 < tempIndex) {
                tempIndex--;
                isLastPosition = true;
            }
            var child = (AbstractContainer) items.get(tempIndex);
            Side resolvedSide;
            var main = composer.getMain();
            var mainContainer = main != null ? ContainerUtils.getContainer(main) : null;
            if (child == mainContainer) {
                var orientation = parentContainer.getSplitPane().getOrientation();
                if (orientation == Orientation.HORIZONTAL) {
                    resolvedSide = isLastPosition ? RIGHT : LEFT;
                } else {
                    resolvedSide = isLastPosition ? BOTTOM : TOP;
                }
            } else {
                resolvedSide = resolveSide(child);
            }

            logger.debug("{} If tabDock is added into {} at {} its side will be {}, when {} is required",
                    dockHost.getDescriptor().getLogPrefix(), parentContainer.getChildFullName(), index,
                    resolvedSide, side);

            return resolvedSide == side;
        }

        /**
         * Finds the index of the child of the specified split pane container that contains the main area in its
         * hierarchy.
         */
        private int indexOfMain(SplitPaneContainer splitPaneContainer) {
            var main = composer.getMain();
            if (main == null) {
                return -1;
            }
            var mainContainer = ContainerUtils.getContainer(main);
            var path = ContainerUtils.toRoot(mainContainer);
            var pathSet = new HashSet<AbstractContainer>(path);
            var items = splitPaneContainer.getSplitPane().getItems();
            for (var i = 0; i < items.size(); i++) {
                var child = (AbstractContainer) items.get(i);
                if (pathSet.contains(child)) {
                    return i;
                }
            }
            return -1;
        }

        private Side resolveSide(TabDockContainer tabDockContainer) {
            return resolveSide((AbstractContainer) tabDockContainer);
        }

        private Side resolveSide(AbstractContainer container) {
            if (composer.getMain() == null) {
                return resolveSideWithoutMain(dockHost.centerStackPane, container);
            } else {
                return resolveSideWithMain(container);
            }
        }

        /**
         * Resolves the side of the given container relative to the main area.
         */
        private Side resolveSideWithMain(AbstractContainer container) {
            var main = composer.getMain();
            var mainContainer = ContainerUtils.getContainer(main);
            if (container == mainContainer) {
                throw new IllegalArgumentException("Cannot resolve the side of the main area itself");
            }
            var componentPath = ContainerUtils.toRoot(container);
            var mainPath = ContainerUtils.toRoot(mainContainer);
            AbstractContainer lca = null;
            int i = 0;
            while (i < componentPath.size() && i < mainPath.size() && componentPath.get(i) == mainPath.get(i)) {
                lca = componentPath.get(i);
                i++;
            }
            if (lca == null || i >= componentPath.size() || i >= mainPath.size()) {
                throw new AssertionError("container and main container must diverge at some point");
            }
            var componentAncestor = componentPath.get(i);
            var mainAncestor = mainPath.get(i);
            var lcaSplitPane = ((SplitPaneContainer) lca).getSplitPane();
            var items = lcaSplitPane.getItems();
            var componentIndex = items.indexOf(componentAncestor);
            var mainIndex = items.indexOf(mainAncestor);
            Side result;
            boolean isBeforeMain = componentIndex < mainIndex;
            if (lcaSplitPane.getOrientation() == Orientation.HORIZONTAL) {
                result = isBeforeMain ? LEFT : RIGHT;
            } else {
                result = isBeforeMain ? TOP : BOTTOM;
            }

            logger.debug("{} Resolved side with main for {} is {}; lowest common ancestor: {}",
                    dockHost.getDescriptor().getLogPrefix(), container.getChildFullName(), result,
                    lca.getChildFullName());

            return result;
        }

        /**
         * Resolves the side of a {@code TabDock} when the docking layout does not contain a main area.
         */
        private Side resolveSideWithoutMain(StackPane centerStackPane, AbstractContainer container) {
            var layoutCenterX = centerStackPane.getWidth() / 2.0;
            var layoutCenterY = centerStackPane.getHeight() / 2.0;
            var bounds = centerStackPane.sceneToLocal(container.localToScene(container.getBoundsInLocal()));
            var tabCenterX = bounds.getMinX() + bounds.getWidth() / 2.0;
            var tabCenterY = bounds.getMinY() + bounds.getHeight() / 2.0;
            var dx = (tabCenterX - layoutCenterX) / layoutCenterX;
            var dy = (tabCenterY - layoutCenterY) / layoutCenterY;
            Side result = null;
            if (Math.abs(dx) >= Math.abs(dy)) {
                result = dx < 0 ? Side.LEFT : Side.RIGHT;
            } else {
                result = dy < 0 ? Side.TOP : Side.BOTTOM;
            }

            logger.debug("{} Resolved side wihout main for {} is {};", dockHost.getDescriptor().getLogPrefix(),
                    container.getChildFullName(), result);

            return result;
        }
    }

    /**
     * Live {@link AreaNode} implementation backed directly by the docking layout's actual current state. Every method
     * call is resolved afresh against the live tree — nothing is cached — so results always reflect the tree as it is
     * at the moment of the call, not a snapshot taken earlier.
     *
     * @see DockHostFxView.Composer#getModelNode(AreaFxView)
     */
    private static final class AreaNodeImpl implements AreaNode {

        private final AbstractAreaContainer<?> container;

        AreaNodeImpl(AbstractAreaContainer<?> container) {
            this.container = container;
        }

        @Override
        public AreaFxView<?> getArea() {
            return container.getArea();
        }

        @Override
        public boolean isMain() {
            return container instanceof MainAreaContainer;
        }

        @Override
        public double getProportion() {
            return ContainerUtils.resolveProportion(container);
        }

        @Override
        public @Nullable GroupNode getParent() {
            var parent = container.getLogicalParent();
            return parent == null ? null : new GroupNodeImpl(parent);
        }

        AbstractAreaContainer<?> getContainer() {
            return container;
        }
    }

    /**
     * Live {@link GroupNode} implementation backed directly by the docking layout's actual current state. Every method
     * call is resolved afresh against the live tree — nothing is cached — so results always reflect the tree as it is
     * at the moment of the call, not a snapshot taken earlier. In particular, two calls to {@link #getChildren()} on
     * the same instance may return different results if the tree changed in between.
     *
     * @see DockHostFxView.Composer#getModelNode(AreaFxView)
     */
    private static final class GroupNodeImpl implements GroupNode {

        private final SplitPaneContainer container;

        GroupNodeImpl(SplitPaneContainer container) {
            this.container = container;
        }

        @Override
        public Orientation getOrientation() {
            return container.getSplitPane().getOrientation();
        }

        @Override
        public List<ModelNode> getChildren() {
            return container.getLogicalItems().stream()
                    .<ModelNode>map(c -> c instanceof SplitPaneContainer spc
                            ? new GroupNodeImpl(spc)
                            : new AreaNodeImpl((AbstractAreaContainer<?>) c))
                    .toList();
        }

        @Override
        public double getProportion() {
            return ContainerUtils.resolveProportion(container);
        }

        @Override
        public @Nullable GroupNode getParent() {
            var parent = container.getLogicalParent();
            return parent == null ? null : new GroupNodeImpl(parent);
        }

        @Override
        public Iterator<ModelNode> iterator() {
            return getChildren().iterator();
        }

        SplitPaneContainer getContainer() {
            return container;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DockHostFxView.class);

    private static final Double DRAG_REGION_MAX_WIDTH = 200.0;

    public class Composer extends AbstractAreaFxView<P>.Composer implements DockHostView.Composer {

        private final DockHostFxView<P> view = DockHostFxView.this;

        private PlaceholderFxView placeholder;

        private SplitPaneContainer rootContainer = null;

        private final ReadOnlyObjectWrapper<AreaFxView<?>> main = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> rightBar = new ReadOnlyObjectWrapper<>();

        /**
         * Popups are DockHost children because they are added to DockHost center pane.
         */
        private final ReadOnlyObjectWrapper<TabPopupFxView<?>> rightPopup = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> bottomBar = new ReadOnlyObjectWrapper<>();

        /**
         * Popups are DockHost children because they are added to DockHost center pane.
         */
        private final ReadOnlyObjectWrapper<TabPopupFxView<?>> bottomPopup = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> leftBar = new ReadOnlyObjectWrapper<>();

        /**
         * Popups are DockHost children because they are added to DockHost center pane.
         */
        private final ReadOnlyObjectWrapper<TabPopupFxView<?>> leftPopup = new ReadOnlyObjectWrapper<>();

        private final ObjectProperty<SideBarPolicy> rightBarPolicy =
                new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

        private final ObjectProperty<SideBarPolicy> bottomBarPolicy =
                new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

        private final ObjectProperty<SideBarPolicy> leftBarPolicy =
                new SimpleObjectProperty<>(SideBarPolicy.EXISTS_WHEN_TABS_PRESENT);

        public Composer() {

        }

        @Override
        public void compose() {
            super.compose();
            var placeholderV = createPlaceholder();
            placeholder = placeholderV;

            addListenerToBarPolicy(rightBarPolicy, RIGHT);
            addListenerToBarPolicy(bottomBarPolicy, BOTTOM);
            addListenerToBarPolicy(leftBarPolicy, LEFT);

            if (getRightBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                showBar(Side.RIGHT);
            }
            if (getBottomBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                showBar(Side.BOTTOM);
            }
            if (getLeftBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                showBar(Side.LEFT);
            }
        }

        /**
         * Applies the given model, replacing the docking layout's current structure entirely.
         * <p>
         * The tree is walked recursively: each {@link GroupNode} becomes a nested group in the live layout, arranged
         * along its {@linkplain GroupNode#getOrientation() orientation}, and each {@link AreaNode} contributes its
         * {@linkplain AreaNode#getArea() area} as a leaf, added as this docking layout's main area if
         * {@linkplain AreaNode#isMain() marked as main}. Relative sizes are taken from
         * {@link ModelNode#getProportion()} where set; children without an explicit proportion have their space
         * distributed automatically.
         * <p>
         * Use this method when initializing a workspace, restoring a previously saved layout, or otherwise replacing
         * the layout wholesale. For incremental changes to an already-live layout, use the anchor-based API instead
         * (see {@link #getModelNode(AreaFxView)}).
         *
         * @param root the root node of the model to apply
         */
        public void applyModel(GroupNode root) {
            var rootContainer = (SplitPaneContainer) build(root);
            setRoot(rootContainer);
            view.printTreeDebugInfo();
        }

        /**
         * Captures the docking layout's current structure as an immutable model tree, suitable for persisting and later
         * restoring via {@link #applyModel(GroupNode)}.
         * <p>
         * Unlike the live nodes returned by {@link #getModelNode(AreaFxView)}, the returned tree is an independent
         * snapshot: it does not change if the docking layout is subsequently modified, and does not support the
         * {@link ModelNode#getParent()} navigation live nodes provide beyond the root, which has no parent.
         *
         * @return the root node of the captured model
         */
        public GroupNode captureModel() {
            return null;
        }

        /**
         * Returns a live view of the given area's position in the docking layout, as an {@link AreaNode}.
         * <p>
         * Unlike a node obtained from {@link #captureModel()} or built via {@link ModelNodeBuilder}, the returned node
         * is not an independent snapshot: {@link AreaNode#getParent()}, and any {@link GroupNode#getChildren()} reached
         * by navigating from it, are resolved afresh against the docking layout's actual current state on every call.
         * This makes it suitable for anchor-based navigation — for example, walking upward via repeated
         * {@link ModelNode#getParent()} calls to locate an ancestor group to pass to an anchor-based
         * {@code addTabDock(...)} overload.
         * <p>
         * The returned node should not be held onto across structural changes to the layout — request a fresh one via
         * this method instead.
         *
         * @param area the area to get the node for; must currently be part of this docking layout
         * @return a live node representing the area's current position in the docking layout
         * @throws ClassCastException if {@code area} is not currently part of this docking layout
         */
        public AreaNode getModelNode(AreaFxView<?> area) {
            return new AreaNodeImpl(ContainerUtils.getContainer(area));
        }

        /**
         * Identifies which of the docking layout's children is the main area.
         * <p>
         * Setting this property does not change the layout's structure — it does not move, add, or remove any area
         * from the tree. It only tells the system which existing area is considered main, which is used, for example,
         * to determine the side a {@link TabDockFxView} should be minimized to relative to it.
         *
         * @return the main area property
         */
        public final ReadOnlyObjectProperty<AreaFxView<?>> mainProperty() {
            return main.getReadOnlyProperty();
        }

        /**
         * Returns the value of {@link #mainProperty()}.
         *
         * @return the main area
         */
        public final @Nullable AreaFxView<?> getMain() {
            return main.get();
        }

        @Override
        public @Nullable AreaPort getMainPort() {
            return getMain() == null ? null : getMain().getPresenter();
        }

        public TabDockFxView<?> createTabDock() {
            var v = new TabDockFxView<>();
            v.getComposer().setDockHost(view);
            var p = new TabDockPresenter<>(v, new AreaParams());
            p.initialize();
            return v;
        }

        /**
         * Adds a new TabDock to one of the sides of the layout with the specified width/height. For example, this
         * method can be used to add some tools to the bottom of the layout.
         * <p>
         * The space for {@code dock} is taken from siblings resolved via {@code dock}'s {@link SpaceResolver},
         * consulted only if the target position offers more than one candidate — see {@link SpaceResolver} for the
         * full rule.
         *
         * @param dock the TabDock to add
         * @param side the side of the layout to add it to
         * @param size the desired size (width for {@code LEFT}/{@code RIGHT}, height for {@code TOP}/{@code BOTTOM}) of
         *         the new TabDock
         */
        public void addTabDock(TabDockFxView<?> dock, Side side, double size) {
            getModifiableChildren().add(dock);
            dock.getComposer().setDockHost(view);
            var index = view.transformer.resolveNewIndex(rootContainer, side);
            view.transformer.addTabDock(rootContainer, dock, index, side, true, size, TabDockOperation.ADD);
        }

        /**
         * Adds a new TabDock at the given side of an existing node, with the specified size.
         * <p>
         * {@code node} identifies the anchor — either a single area or an entire group — next to which the new TabDock
         * is placed. Pass a leaf {@link AreaNode} to add relative to one specific area, or a {@link GroupNode} (for
         * example, one reached via repeated {@link ModelNode#getParent()} calls) to add relative to an entire nested
         * group instead, regardless of how deeply that group is nested in the layout.
         * <p>
         * {@code node} must be a live node — obtained from {@link #getModelNode(AreaFxView)}, or reached from it via
         * {@link ModelNode#getParent()} or {@link GroupNode#getChildren()} — since it must correspond to an actual
         * current position in this docking layout. A node built via {@link ModelNodeBuilder} does not qualify.
         * <p>
         * The space for {@code tabDock} is taken from siblings resolved via {@code tabDock}'s {@link SpaceResolver},
         * consulted only if the anchor position offers more than one candidate — see {@link SpaceResolver} for the full
         * rule.
         *
         * @param tabDock the TabDock to add
         * @param node the live node to add the new TabDock next to
         * @param side the side of {@code node} the new TabDock should occupy
         * @param size the desired size (width for {@code LEFT}/{@code RIGHT}, height for {@code TOP}/{@code BOTTOM}) of
         *         the new TabDock
         * @throws IllegalArgumentException if {@code node} is not a live node obtained from this docking layout
         */
        public void addTabDock(TabDockFxView<?> tabDock, ModelNode node, Side side, double size) {
            getModifiableChildren().add(tabDock);
            var anchorContainer = ContainerUtils.resolveContainer(node);
            view.transformer.addTabDock(anchorContainer, side, tabDock, size, TabDockOperation.ADD);
        }

        /**
         * Removes {@code dock} from the layout without closing it — used when the TabDock is departing as part of a
         * move rather than being permanently discarded. The space it freed is given to siblings resolved via
         * {@code dock}'s {@link SpaceResolver}, consulted only if the removal point offers more than one candidate.
         *
         * @param dock the TabDock to remove
         */
        public void removeTabDock(TabDockFxView<?> dock) {
            view.transformer.removeTabDock(dock, TabDockOperation.DEPART);
            view.printTreeDebugInfo();
            dock.getComposer().setDockHost(null);
            logger.debug("{} Removed TabDock", getDescriptor().getLogPrefix());
        }

        /**
         * Permanently closes {@code dock}, deinitializing it. The space it freed is given to siblings resolved via
         * {@code dock}'s {@link SpaceResolver}, consulted only if the removal point offers more than one candidate.
         *
         * @param dock the TabDock to close
         */
        public void closeTabDock(TabDockFxView<?> dock) {
            view.transformer.removeTabDock(dock, TabDockOperation.REMOVE);
            view.printTreeDebugInfo();
            logger.debug("{} Closed TabDock", getDescriptor().getLogPrefix());
        }

        public void showBar(Side side) {
            var layoutHistory = view.getPresenter().getHistory();
            SideBarHistory barHistory = null;
            switch (side) {
                case RIGHT -> {
                    barHistory = layoutHistory.getOrCreateRightSideBar();
                    showBar(side, barHistory, rightBar, v -> view.getNode().setRight(v.getNode()));
                }
                case LEFT -> {
                    barHistory = layoutHistory.getOrCreateLeftSideBar();
                    showBar(side, barHistory, leftBar, v -> view.getNode().setLeft(v.getNode()));
                }
                case TOP, BOTTOM -> {
                    side = Side.BOTTOM;
                    barHistory = layoutHistory.getOrCreateBottomSideBar();
                    showBar(side, barHistory, bottomBar, v -> view.getNode().setBottom(v.getNode()));
                }
                default -> throw new AssertionError();
            }
        }

        public void hideBar(Side side) {
            SideBarFxView<?> sideBar = null;
            switch (side) {
                case RIGHT -> {
                    view.getNode().setRight(null);
                    sideBar = getRightBar();
                    rightBar.set(null);
                }
                case BOTTOM -> {
                    view.getNode().setBottom(null);
                    sideBar = getBottomBar();
                    bottomBar.set(null);
                }
                case LEFT -> {
                    view.getNode().setLeft(null);
                    sideBar = getLeftBar();
                    leftBar.set(null);
                }
                default -> throw new AssertionError();
            }
            getModifiableChildren().remove(sideBar);
            sideBar.getPresenter().deinitializeTree();
        }

        @Override
        public @Nullable SideBarPort getRightBarPort() {
            return getRightBar() == null ? null : getRightBar().getPresenter();
        }

        public final @Nullable SideBarFxView<?> getRightBar() {
            return rightBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> rightBarProperty() {
            return rightBar.getReadOnlyProperty();
        }

        @Override
        public @Nullable SideBarPort getBottomBarPort() {
            return getBottomBar() == null ? null : getBottomBar().getPresenter();
        }

        public final @Nullable SideBarFxView<?> getBottomBar() {
            return bottomBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> bottomBarProperty() {
            return bottomBar.getReadOnlyProperty();
        }

        @Override
        public @Nullable SideBarPort getLeftBarPort() {
            return getLeftBar() == null ? null : getLeftBar().getPresenter();
        }

        public final @Nullable SideBarFxView<?> getLeftBar() {
            return leftBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> leftBarProperty() {
            return leftBar.getReadOnlyProperty();
        }

        @Override
        public @Nullable SideBarPort getBarPort(Side side) {
            var barFxView = view.getComposer().resolveBarWrapper(side).get();
            if (barFxView != null) {
                return barFxView.getPresenter();
            } else {
                return null;
            }
        }

        public final @Nullable SideBarFxView<?> getBar(Side side) {
            var wrapper = resolveBarWrapper(side);
            return wrapper.get();
        }

        public ReadOnlyObjectProperty<SideBarFxView<?>> resolveBarProperty(Side side) {
            return resolveBarWrapper(side).getReadOnlyProperty();
        }

        public ObjectProperty<SideBarPolicy> rightBarPolicyProperty() {
            return rightBarPolicy;
        }

        @Override
        public SideBarPolicy getRightBarPolicy() {
            return rightBarPolicy.get();
        }

        @Override
        public void setRightBarPolicy(SideBarPolicy policy) {
            rightBarPolicy.set(policy);
        }

        public ObjectProperty<SideBarPolicy> bottomBarPolicyProperty() {
            return bottomBarPolicy;
        }

        @Override
        public SideBarPolicy getBottomBarPolicy() {
            return bottomBarPolicy.get();
        }

        @Override
        public void setBottomBarPolicy(SideBarPolicy policy) {
            bottomBarPolicy.set(policy);
        }

        public ObjectProperty<SideBarPolicy> leftBarPolicyProperty() {
            return leftBarPolicy;
        }

        @Override
        public SideBarPolicy getLeftBarPolicy() {
            return leftBarPolicy.get();
        }

        @Override
        public void setLeftBarPolicy(SideBarPolicy policy) {
            leftBarPolicy.set(policy);
        }

        @Override
        public SideBarPolicy getBarPolicy(Side side) {
            return switch (side) {
                case RIGHT -> getRightBarPolicy();
                case BOTTOM -> getBottomBarPolicy();
                case LEFT -> getLeftBarPolicy();
                default -> throw new AssertionError();
            };
        }

        @Override
        public @Nullable TabPopupPort getRightPopupPort() {
            return getRightPopup() == null ? null : getRightPopup().getPresenter();
        }

        @Override
        public @Nullable TabPopupPort getBottomPopupPort() {
            return getBottomPopup() == null ? null : getBottomPopup().getPresenter();
        }

        @Override
        public @Nullable TabPopupPort getLeftPopupPort() {
            return getLeftPopup() == null ? null : getLeftPopup().getPresenter();
        }

        public final @Nullable TabPopupFxView<?> getRightPopup() {
            return rightPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> rightPopupProperty() {
            return rightPopup.getReadOnlyProperty();
        }

        public final @Nullable TabPopupFxView<?> getBottomPopup() {
            return bottomPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> bottomPopupProperty() {
            return bottomPopup.getReadOnlyProperty();
        }

        public final @Nullable TabPopupFxView<?> getLeftPopup() {
            return leftPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> leftPopupProperty() {
            return leftPopup.getReadOnlyProperty();
        }

        public final @Nullable TabPopupFxView<?> getPopup(Side side) {
            var wrapper = resolvePopupWrapper(side);
            return wrapper.get();
        }

        public ReadOnlyObjectProperty<TabPopupFxView<?>> resolvePopupProperty(Side side) {
            return resolvePopupWrapper(side).getReadOnlyProperty();
        }

        @Override
        public TabPopupPort getPopupPort(Side side) {
            var popup = getPopup(side);
            return popup == null ? null : popup.getPresenter();
        }

        protected SideBarFxView<?> createBar(Side side, SideBarHistory history) {
            var v = new SideBarFxView<>(view);
            var p = new SideBarPresenter<>(v, new SideBarParams(side, () -> history));
            p.initialize();
            return v;
        }

        protected PlaceholderFxView createPlaceholder() {
            var v = new PlaceholderFxView();
            v.getComposer().setDockHost(view);
            var p = new PlaceholderPresenter(v, new AreaParams());
            p.initialize();
            return v;
        }

        @Override
        protected ObservableList<ChildFxView<?>> getModifiableChildren() {
            return super.getModifiableChildren();
        }

        void addTabPopup(TabPopupFxView<?> popup) {
            getModifiableChildren().add(popup);
            var popupNode = popup.getNode();
            view.centerStackPane.getChildren().add(popupNode);
            var wrapper = view.getComposer().resolvePopupWrapper(popup.getPresenter().getSide());
            wrapper.set(popup);
        }

        void closeTabPopup(Side side) {
            var wrapper = view.getComposer().resolvePopupWrapper(side);
            var popup = wrapper.get();
            if (popup != null) {
                view.centerStackPane.getChildren().remove(popup.getNode());
                getModifiableChildren().remove(popup);
                popup.getPresenter().deinitializeTree();
                wrapper.set(null);
            }
        }

        void restoreTabDock(TabDockFxView<?> tabDock) {
            view.transformer.restoreTabDock(ContainerUtils.getContainer(tabDock));
        }

        void minimizeTabDock(TabDockFxView<?> tabDock) {
            view.transformer.minimizeTabDock(tabDock);
        }

        /**
         * Sets the value of {@link #mainProperty()}.
         *
         * @param value the main area
         */
        private void setMain(AreaFxView<?> value) {
            this.main.set(value);
        }

        private AbstractContainer build(ModelNode node) {
            if (node instanceof AreaNode areaNode) {
                getModifiableChildren().add(areaNode.getArea());
                if (areaNode.isMain()) {
                    setMain(areaNode.getArea());
                    return new MainAreaContainer(view, areaNode.getArea());
                } else {
                    return new TabDockContainer(view, (TabDockFxView<?>) areaNode.getArea());
                }
            } else if (node instanceof GroupNode groupNode) {
                var splitPane = new DockSplitPane(getDescriptor().getLogPrefix());
                var splitPaneContainer = new SplitPaneContainer(view, splitPane);
                splitPane.setOrientation(groupNode.getOrientation());
                for (ModelNode child : groupNode.getChildren()) {
                    var childContainer = build(child);
                    splitPaneContainer.insertNew(splitPane.getItems().size(), childContainer);
                }
                applyDividerPositions(splitPane, groupNode.getChildren());
                return splitPaneContainer;
            } else {
                throw new IllegalArgumentException("Unknown node type: " + node.getClass());
            }
        }

        private void setRoot(SplitPaneContainer root) {
            if (this.rootContainer != null) {
                centerStackPane.getChildren().remove(0);  // there can be tab popup
                this.rootContainer = null;
            }
            this.rootContainer = root;
            centerStackPane.getChildren().add(0, rootContainer);
        }

        private void applyDividerPositions(SplitPane splitPane, List<ModelNode> children) {
            for (ModelNode child : children) {
                if (child.getProportion() == ModelNodeBuilder.UNSET_PROPORTION) {
                    return; // leave SplitPane's default equal distribution in place
                }
            }
            var positions = new double[children.size() - 1];
            double cumulative = 0;
            for (int i = 0; i < positions.length; i++) {
                cumulative += children.get(i).getProportion();
                positions[i] = cumulative;
            }
            splitPane.setDividerPositions(positions);
        }

        private SideBarFxView<?> showBar(Side side, SideBarHistory sideBarHistory,
            ReadOnlyObjectWrapper<SideBarFxView<?>> wrapper, Consumer<SideBarFxView<?>> viewAdder) {
            if (wrapper.get() != null) {
                return wrapper.get();
            }
            var sideBar = createBar(side, sideBarHistory);
            getModifiableChildren().add(sideBar);
            wrapper.set(sideBar);
            viewAdder.accept(sideBar);
            return sideBar;
        }

        private void addListenerToBarPolicy(ObjectProperty<SideBarPolicy> policy, Side side) {
            policy.addListener((ov, oldV, newV) -> {
                if (newV == SideBarPolicy.EXISTS_ALWAYS) {
                    if (view.getComposer().resolveBarWrapper(side).get() == null) {
                        showBar(side);
                    }
                } else if (newV == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    var bar = view.getComposer().resolveBarWrapper(side).get();
                    if (bar != null && bar.getComposer().getTabDockPorts().isEmpty()) {
                        hideBar(side);
                    }
                } else {
                    throw new AssertionError();
                }
            });
        }

        private void setRightBar(SideBarFxView<?> value) {
            this.rightBar.set(value);
        }

        private void setRightPopup(TabPopupFxView<?> value) {
            this.rightPopup.set(value);
        }

        private void setBottomBar(SideBarFxView<?> value) {
            this.bottomBar.set(value);
        }

        private void setBottomPopup(TabPopupFxView<?> value) {
            this.bottomPopup.set(value);
        }

        private void setLeftBar(SideBarFxView<?> value) {
            this.leftBar.set(value);
        }

        private void setLeftPopup(TabPopupFxView<?> value) {
            this.leftPopup.set(value);
        }

        private ReadOnlyObjectWrapper<TabPopupFxView<?>> resolvePopupWrapper(Side side) {
            return switch (side) {
                case RIGHT -> rightPopup;
                case BOTTOM -> bottomPopup;
                case LEFT -> leftPopup;
                default -> throw new AssertionError();
            };
        }

        private ReadOnlyObjectWrapper<SideBarFxView<?>> resolveBarWrapper(Side side) {
            return switch (side) {
                case RIGHT -> rightBar;
                case BOTTOM -> bottomBar;
                case LEFT -> leftBar;
                default -> throw new AssertionError();
            };
        }
    }

    /**
     * Contains the root {@link SplitSpaceFxView} and the {@link TabPopupFxView}.
     */
    private final StackPane centerStackPane = new StackPane();

    /**
     * Contains the {@link DockHostFxView#centerStackPane} and three {@link SideBarView}.
     */
    private final BorderPane node = new BorderPane();

    private final DragAndDropContext dragAndDropContext = new DragAndDropContext();

    private final Transformer transformer = new Transformer(this);

    /**
     * Resolver uses transformer when transformation is set.
     */
    private final DropPositionResolver dropPositionResolver = new DropPositionResolver(this);

    private final DragAndDropHandler dragAndDropHandler = createDragAndDropHandler();

    private DropPosition dropPosition;

    @Override
    public void requestFocus() {

    }

    @Override
    public BorderPane getNode() {
        return this.node;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DockHostFxView.Composer();
    }

    protected DragAndDropContext getDragAndDropContext() {
        return dragAndDropContext;
    }

    protected DragAndDropHandler createDragAndDropHandler() {
        return new DragAndDropHandler(this);
    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(node, Priority.ALWAYS);

        this.node.getStyleClass().add("dock-host");
        var css = DockHostFxView.class.getResource("dock-host.css").toExternalForm();
        this.node.getStylesheets().add(css);
        this.node.setCenter(centerStackPane);
        centerStackPane.setMinSize(0, 0);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        centerStackPane.widthProperty().addListener((ov2, oldV2, newV2) -> {
            getPresenter().onCenterWidthChanged(newV2.doubleValue());

        });
        centerStackPane.heightProperty().addListener((ov2, oldV2, newV2) -> {
            getPresenter().onCenterHeightChanged(newV2.doubleValue());

        });
    }

    Dimension2D getCenterDimension() {
        return new Dimension2D(this.centerStackPane.getWidth(), this.centerStackPane.getHeight());
    }

    DragAndDropHandler getDragAndDropHandler() {
        return dragAndDropHandler;
    }

    private SplitPaneContainer createContainer(DockSplitPane splitPane) {
        return ContainerUtils.createContainer(this, splitPane);
    }

    private AbstractAreaContainer<?> createContainer(AreaFxView<?> child) {
        return ContainerUtils.createContainer(this, child);
    }

    private void printTreeDebugInfo() {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Docking layout tree: {}", getDescriptor().getLogPrefix(), getTreeDebugInfo());
        }
    }

    private String getTreeDebugInfo() {
        StringBuilder builder = new StringBuilder();
        ContainerUtils.traverse(getComposer().rootContainer, 0, (container, level) -> {
            builder.append("\n");
            builder.append("    ".repeat(level));
            builder.append(container.getChildName());
            builder.append(" [");
            builder.append("shortUuid: ");
            builder.append(container.getChildShortUuid());
            if (container instanceof SplitPaneContainer spc) {
                builder.append(", orientation: ");
                builder.append(spc.getSplitPane().getOrientation().name());
                builder.append(", itemCount: ");
                builder.append(spc.getSplitPane().getItems().size());
                builder.append(", logicalItemCount: ");
                builder.append(spc.getLogicalItems().size());
            } else if (container instanceof AbstractAreaContainer<?> aac) {
                builder.append(", state: ");
                builder.append(aac.getArea().getDescriptor().getState().name());
            }
            builder.append("]");
        });
        return builder.toString();
    }
}
