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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * <p>Model-based API is intended for complete layout construction, restoration, and serialization. A layout is
 * described as an immutable {@link ModelNode} tree, which can be applied to the docking layout or generated from its
 * current state.
 *
 * <p>In general, use the model-based API when creating, restoring, or persisting a layout, and use the anchor-based
 * API for runtime layout modifications initiated by the application or user interactions.
 *
 * <p>Anchor-based API is intended for incremental modifications of an existing layout. Instead of rebuilding the
 * entire model, it performs targeted operations relative to an existing anchor component, such as inserting, replacing,
 * or removing areas.
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
 * <p>How it works. When the user starts a drag-and-drop operation and moves the mouse, an instance of {@link DropPosition}
 * is created. If the user releases the mouse, the data in this object is used to perform the relocation of either a Tab
 * or a TabDock. The reason for creating this object so early is that, while the user is moving the mouse, a potential
 * drop position needs to be highlighted, and that also requires a {@link DropPosition}.
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

        private void handleMouseDragOverOnTabHeaderArea(MousePosition mousePosition, TabDockContainer tabDockContainer) {
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
                    var oldContainer = getContainer(dragDock);
                    DockSplitPane oldParent = oldContainer.getParentSplitPane();

                    // it is necessary to create a new position with a new index for example,
                    // if there are new children, besides the old parent should be used
                    var oldPosition = oldContainer.resolvePosition(oldParent);
                    dockHost.transformer.removeTabDock(getContainer(oldParent), oldPosition, TabDockOperation.MOVE);

                    // finally replacing the placeholder
                    var placeholderContainer = getContainer(composer.placeholder);
                    DockSplitPane newParent = placeholderContainer.getParentSplitPane();
                    // it is not possible to use dropPosition.getNewPosition().getIndex()
                    // because after removing tabDock indexes have changed
                    var placeholderIndex = newParent.getItems().indexOf(placeholderContainer);
                    newParent.getItems().set(placeholderIndex, createContainer(dockHost, dragDock));
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
            traverse(composer.rootContainer, 0, (c, level) -> {
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

    private enum TabDockOperation {
        CLOSE, MOVE, MINIMIZE
    }

    private static abstract class AbstractContainer extends StackPane {

        private final DockHostFxView<?> dockHost;

        public AbstractContainer(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
        }

        DockHostFxView<?> getDockHost() {
            return dockHost;
        }

        abstract ContainerPosition resolvePosition();

        ContainerPosition resolvePosition(DockSplitPane parentSplitPane) {
            if (parentSplitPane != null) {
                var containerIndex = parentSplitPane.getItems().indexOf(this);
                return new ContainerPosition(this, containerIndex);
            } else {
                return new ContainerPosition(this, -1);
            }
        }

        DockSplitPane getParentSplitPane() {
            if (getParent() != null) {
                return (DockSplitPane) getParent().getParent();  // SplitPane keeps items in Content node
            } else {
                return null;
            }
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

        ContainerPosition resolvePosition() {
            ContainerPosition result;
            if (this != getDockHost().getComposer().rootContainer) {
                result = resolvePosition(this.getParentSplitPane());
            } else {
                result = resolvePosition(null);
            }
            return result;
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

        ContainerPosition resolvePosition() {
            ContainerPosition result = resolvePosition(this.getParentSplitPane());
            return result;
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
            var splitPane = container.getParentSplitPane();
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
                    // Due to the divider, the position is calculated based on the adjacent panel, so that when
                    // the left/right side is the same, the result is consistent.
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
                    // Due to the divider, the position is calculated based on the adjacent panel, so that when
                    // the left/right side is the same, the result is consistent.
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

        public DropPositionResolver(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
            this.transformer = dockHost.transformer;
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
            DockSplitPane splitPane = tabDockContainer.getParentSplitPane();
            SplitPaneContainer splitPaneContainer = getContainer(splitPane);
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

            var parentSplitPane = eventPosition.getContainer().getParentSplitPane();
            var parentPosition = getContainer(parentSplitPane).resolvePosition();

            DockSplitPane grandparentSplitPane = null;
            ContainerPosition grandparentPosition = null;

            DockSplitPane greatGrandparentSplitPane = null;
            ContainerPosition greatGrandparentPosition = null;

            if (parentPosition.getContainer() != dockHost.getComposer().rootContainer) {
                grandparentSplitPane = parentPosition.getContainer().getParentSplitPane();
                grandparentPosition = getContainer(grandparentSplitPane).resolvePosition();

                if (grandparentPosition.getContainer() != dockHost.getComposer().rootContainer) {
                    greatGrandparentSplitPane = grandparentPosition.getContainer().getParentSplitPane();
                    greatGrandparentPosition = getContainer(greatGrandparentSplitPane).resolvePosition();
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
            Consumer<TabDockFxView<?>> transformation = null;
            boolean isBoundary = isFirst ? eventPosition.isFirst() : eventPosition.isLast();
            int indexDelta = isFirst ? 0 : 1;
            int siblingDelta = isFirst ? -1 : 1;
            int caseIndex;
            if (isBoundary) {
                if (dropPos.getGrandparentPosition() == null) {
                    transformation = (newTabDock) -> {
                        transformer.addTabDock(side, eventPosition, null, dropPos.getNewPosition(), newTabDock);
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
                                    dropPos.getNewPosition(), tabDock);
                        };
                        dropPos.setNewPosition(new ContainerPosition(isFirst ? 0 : 1, ONE_THIRD));
                        dropPos.setIndicatorPosition(dropPos.getGrandparentPosition());
                        dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, dropPos.getEventPosition(),
                                dropPos.getGrandparentPosition()));
                        caseIndex = 1;
                    } else {
                        transformation = (tabDock) -> {
                            transformer.addTabDock(side, dropPos.getGrandparentPosition(), null,
                                    dropPos.getNewPosition(), tabDock);
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
                    transformer.addTabDock(side, eventPosition, siblingPosition, dropPos.getNewPosition(), newTabDock);
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
            Consumer<TabDockFxView<?>> transformation = null;

            dropPos.setIndicatorPosition(dropPos.getParentPosition());
            dropPos.setIndicatorBounds(createThirdIndicatorBounds(side, eventPosition, dropPos.getParentPosition()));
            int caseIndex;
            if (dropPos.getGrandparentPosition() == null) {
                caseIndex = 0;
                Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                transformation = (tabDock) -> {
                    transformer.wrapAndAddTabDock(orientation, dropPos.getParentPosition(), dropPos.getNewPosition(),
                            tabDock);
                };
                dropPos.setNewPosition(new ContainerPosition(isFirst ? 0 : 1, ONE_THIRD));
            } else {
                caseIndex = 1;
                int index = dropPos.getParentPosition().getIndex() + (isFirst ? 0 : 1);
                transformation = (newTabDock) -> {
                    transformer.addTabDock(side, dropPos.getParentPosition(), null, dropPos.getNewPosition(),
                            newTabDock);
                };
                dropPos.setNewPosition(new ContainerPosition(index, ONE_THIRD));
            }
            dropPos.setTransformation(transformation);
            logger.trace("{} Prepared drop position for opposite orientation on edge; side: {}, case: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, caseIndex, dropPos);
        }

        private void prepareForSameOrientationOffEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;

            Consumer<TabDockFxView<?>> transformation = (newTabDock) -> {
                transformer.addTabDock(dropPos.getMousePosition().getSide(), dropPos.getEventPosition(), null,
                        dropPos.getNewPosition(), newTabDock);
            };
            dropPos.setTransformation(transformation);

            int index = dropPos.getEventPosition().getIndex() + (isFirst ? 0 : 1);
            dropPos.setNewPosition(new ContainerPosition(index, ONE_HALF));
            logger.trace("{} Prepared drop position for same orientation off edge; side: {}, position: {}",
                    dockHost.getDescriptor().getLogPrefix(), side, dropPos);
        }

        private void prepareForOppositeOrientationOffEdge(DropPosition dropPos, Side side) {
            boolean isFirst = side == Side.TOP || side == Side.LEFT;
            Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            Consumer<TabDockFxView<?>> transformation = (tabDock) -> {
                transformer.wrapAndAddTabDock(orientation, dropPos.getEventPosition(), dropPos.getNewPosition(),
                        tabDock);
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
                var dragDockContainer = getContainer(dockHost.dragAndDropHandler.dragDock);
                DockSplitPane splitPane = dragDockContainer.getParentSplitPane();
                if (info.getEventPosition().getContainer().getParentSplitPane() ==
                        dragDockContainer.getParentSplitPane()) {
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
         * Returns the path from the root container to the given container, inclusive of both ends.
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
                var parentSplitPane = current.getParentSplitPane();
                current = parentSplitPane != null ? getContainer(parentSplitPane) : null;
            }
            if (logger.isTraceEnabled()) {
                var names = result.stream().map(AbstractContainer::getChildFullName).collect(Collectors.joining(", "));
                logger.trace("{} {} path to root: {}", dockHost.getDescriptor().getLogPrefix(),
                        start.getChildFullName(), names);
            }
            return result;
        }

        private final DockHostFxView<?> dockHost;

        private final DockHostFxView<?>.Composer composer;

        public Transformer(DockHostFxView<?> dockHost) {
            this.dockHost = dockHost;
            this.composer = dockHost.getComposer();
        }

        private SplitPaneContainer wrap(AbstractContainer container, int index) {
            DockSplitPane parentSplitPane = null;
            Orientation currentOrientation;
            if (composer.rootContainer != container) {
                parentSplitPane = container.getParentSplitPane();
                currentOrientation = parentSplitPane.getOrientation();
            } else {
                currentOrientation = composer.rootContainer.splitPane.getOrientation();
            }
            var newOrientation = Orientation.HORIZONTAL;
            if (currentOrientation == Orientation.HORIZONTAL) {
                newOrientation = Orientation.VERTICAL;
            }
            var newSplitPane = new DockSplitPane(dockHost.getDescriptor().getLogPrefix());
            newSplitPane.setOrientation(newOrientation);
            var newSplitPaneContainer = createContainer(dockHost, newSplitPane);

            double[] parentOldPositions = null;

            //removing wrapped component and adding a container component
            if (parentSplitPane == null) {
                composer.setRoot(newSplitPaneContainer);
            } else {
                parentOldPositions = parentSplitPane.getDividerPositions();
                parentSplitPane.getItems().remove(index);
                parentSplitPane.getItems().add(index, newSplitPaneContainer);
            }

            newSplitPane.getItems().add(container);
            if (parentOldPositions != null) {
                parentSplitPane.setDividerPositions(parentOldPositions);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Wrapped {} into {}", dockHost.getDescriptor().getLogPrefix(),
                        container.getChildFullName(), newSplitPane.getFullName());
            }

            return newSplitPaneContainer;
        }

        private void unwrap(SplitPaneContainer splitPaneContainer, int index) {
            double[] childPositions;
            // now it has only one child
            var splitPane = splitPaneContainer.splitPane;
            AbstractContainer childContainer = (AbstractContainer) splitPane.getItems().get(0);

            if (splitPaneContainer != composer.rootContainer) {
                DockSplitPane grandparentSplitPane = splitPaneContainer.getParentSplitPane();
                List<? extends AreaFxView<?>> otherAreas;
                if (childContainer instanceof SplitPaneContainer spc) {
                    otherAreas = spc.splitPane.getItems().stream()
                            .filter(c -> c instanceof AbstractAreaContainer)
                            .map(c -> ((AbstractAreaContainer<?>) c).getArea())
                            .toList();
                    childPositions = spc.splitPane.getDividerPositions();
                } else if (childContainer instanceof AbstractAreaContainer<?> aac) {
                    otherAreas = List.of(aac.getArea());
                    childPositions = new double[0];
                } else {
                    throw new IllegalArgumentException();
                }
                var oldPositions = grandparentSplitPane.getDividerPositions();
                // removing parent
                grandparentSplitPane.getItems().remove(index);
                // adding tab docks
                for (var i = 0; i < otherAreas.size(); i++) {
                    var area = otherAreas.get(i);
                    var areaContainer = createContainer(dockHost, area);
                    grandparentSplitPane.getItems().add(index + i, areaContainer);
                }

                // last child has parent space provider
                refresh();
                grandparentSplitPane.updateDividersOnUnwrap(index, oldPositions, childPositions);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} Unwrapped {} into {}",
                        dockHost.getDescriptor().getLogPrefix(),
                        otherAreas.stream().map(e -> e.getDescriptor().getFullName())
                                .collect(Collectors.joining(", ")),
                        grandparentSplitPane.getFullName());
                }
            } else {
                if (childContainer instanceof SplitPaneContainer spc) {
                    composer.setRoot(spc);
                    if (logger.isDebugEnabled()) {
                    logger.debug("{} Unwrapped {} and set it as a root", dockHost.getDescriptor().getLogPrefix(),
                            spc.getChildFullName());
                    }
                } // otherwise there is a splitSpace with one main component
            }
        }

        /**
         * The start point for removing tabDock.
         *
         * @param tabPane
         */
        private void removeTabDock(TabDockFxView<?> tabDock, TabDockOperation operation) {
            TabDockContainer tabDockContainer = (TabDockContainer) tabDock.getNode().getParent();
            ContainerPosition tabDockInfo = tabDockContainer.resolvePosition();
            DockSplitPane parent = tabDockContainer.getParentSplitPane();
            removeTabDock(getContainer(parent), tabDockInfo, operation);
        }

        private void removeTabDock(SplitPaneContainer parentContainer, ContainerPosition tabDockInfo,
                TabDockOperation operation) {
            var parentInfo = parentContainer.resolvePosition();
            if (parentContainer.getSplitPane().getItems().size() == 2) {
                removeTabDockAndUnwrap(parentInfo, tabDockInfo, operation);
            } else {
                removeTabDock(parentInfo, tabDockInfo, operation);
            }
        }

        private TabDockFxView<?> wrapAndAddTabDock(Orientation newOrientation, ContainerPosition anchorInfo,
                ContainerPosition newInfo, TabDockFxView<?> newTabDock) {
            newTabDock.getComposer().setDockHost(dockHost);
            var newSplitPaneContainer = wrap(anchorInfo.getContainer(), anchorInfo.getIndex());
            newSplitPaneContainer.getSplitPane().getItems().add(newInfo.getIndex(),
                    createContainer(dockHost, newTabDock));

            if (newInfo.getFraction() == ONE_THIRD) {
                var splitPane = newSplitPaneContainer.getSplitPane();
                if (newOrientation == Orientation.HORIZONTAL) {
                    var pos = dockHost.dropPosition.getIndicatorBounds().getWidth()
                            / anchorInfo.getContainer().getWidth();
                    if (newInfo.getIndex() == 0) {
                        splitPane.setDividerPositions(pos);
                    } else {
                        splitPane.setDividerPositions(1 - pos);
                    }
                } else {
                    var pos = dockHost.dropPosition.getIndicatorBounds().getHeight()
                            / anchorInfo.getContainer().getHeight();
                    if (newInfo.getIndex() == 0) {
                        splitPane.setDividerPositions(pos);
                    } else {
                        splitPane.setDividerPositions(1 - pos);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} to {}", dockHost.getDescriptor().getLogPrefix(),
                        newTabDock.getDescriptor().getFullName(),
                        newSplitPaneContainer.getChildFullName());
            }
            return newTabDock;
        }

        /**
         * Important: This function cannot retrieve the parent from anchorInfo, as it may return incorrect results when
         * the tabDock has been added to a new parent.
         *
         * @param parentInfo
         * @param anchorInfo
         */
        private void removeTabDockAndUnwrap(ContainerPosition parentInfo, ContainerPosition anchorInfo,
                TabDockOperation operation) {
            // parent has only two children
            SplitPaneContainer parentContainer = (SplitPaneContainer) parentInfo.getContainer();
            var parentSplitPane = parentContainer.getSplitPane();
            // removing empty tabdock
            TabDockContainer dockContainer = (TabDockContainer) parentSplitPane.getItems().get(anchorInfo.getIndex());
            var tabDock = dockContainer.getArea();
            parentSplitPane.getItems().remove(anchorInfo.getIndex());
            if (operation == TabDockOperation.CLOSE) {
                tabDock.getPresenter().deinitializeTree();
            }

            if (logger.isDebugEnabled()) {
                logger.debug("{} Removed {} from {}", dockHost.getDescriptor().getLogPrefix(),
                        anchorInfo.getContainer().getChildFullName(),
                        parentSplitPane.getFullName());
            }
            unwrap(parentContainer, parentInfo.getIndex());
        }

        /**
         * This method adds a new TabDock when the user selects the area using mouse.
         *
         * @param side
         * @param anchorInfo
         * @param siblingInfo
         * @param newInfo
         * @param newTabDock
         */
        private void addTabDock(Side side, ContainerPosition anchorInfo, ContainerPosition siblingInfo,
                ContainerPosition newInfo, TabDockFxView<?> newTabDock) {
            newTabDock.getComposer().setDockHost(dockHost);
            DockSplitPane splitPane = anchorInfo.getContainer().getParentSplitPane();
            double[] oldPositions = splitPane.getDividerPositions();
            splitPane.getItems().add(newInfo.getIndex(), createContainer(dockHost, newTabDock));
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
                splitPane.updateDividersOnInsertBetween(newInfo.getIndex(), beforeProportion,
                        afterProportion, oldPositions);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} into {}", dockHost.getDescriptor().getLogPrefix(),
                    newTabDock.getDescriptor().getFullName(), splitPane.getFullName());
            }
        }

        /**
         * This method adds a new TabDock during restoring or adding a tab dock to a specific side.
         *
         * @param grandParentPositions
         * @param parent
         * @param dock
         * @param index
         * @param side
         * @param sideShouldBeChecked
         * @param size
         */
        private void addTabDock(double[] grandParentPositions, SplitPaneContainer parentContainer, TabDockFxView<?> dock,
                int index, Side side, boolean sideShouldBeChecked, double size) {
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
            var oldSplitPaneSize = splitPane.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
            }
            var dividerSize = splitPane.computeDividerSize();

            var oldPositions = splitPane.getDividerPositions();
            var dockContainer = createContainer(dockHost, dock);
            splitPane.getItems().add(index, dockContainer);

            refresh();

            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            if (parentContainer.getParent() != null && grandParentPositions != null) {
                (parentContainer.getParentSplitPane()).setDividerPositions(grandParentPositions);
            }
            var mainIndex = indexOfMain(parentContainer);
            if (mainIndex >= 0) {
                splitPane.updateDividersOnAddWithMain(oldSplitPaneSize, oldPositions, dividerSize, mainIndex,
                        index, size);
            } else {
                splitPane.updateDividersOnAddWithoutMain(oldSplitPaneSize, oldPositions, dividerSize,
                        index, size);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} into {}", dockHost.getDescriptor().getLogPrefix(),
                        dock.getDescriptor().getFullName(),
                        dockContainer.getParentSplitPane().getFullName());
                dockHost.printTreeDebugInfo();
            }
        }

        /**
         * Important: This function cannot retrieve the parent from tabDockInfo, as it may return incorrect results when
         * the tabDock has been added to a new parent.
         *
         * @param tabDockInfo
         */
        private void removeTabDock(ContainerPosition parent, ContainerPosition tabDockInfo, TabDockOperation operation) {
            TabDockContainer tabDockContainer = (TabDockContainer) tabDockInfo.getContainer();
            AreaFxView<?> componentToRemove = tabDockContainer.getArea();
            var splitPaneContainer = ((SplitPaneContainer) parent.getContainer());
            DockSplitPane splitPane = splitPaneContainer.getSplitPane();
            var oldPositions = splitPane.getDividerPositions();
            var oldSplitPaneSize = splitPane.getWidth();
            var removedChildSize = tabDockContainer.getArea().getNode().getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                oldSplitPaneSize = splitPane.getHeight();
                removedChildSize = tabDockContainer.getArea().getNode().getHeight();
            }

            logger.debug("{} Removing tabDock; operation: {}", dockHost.getDescriptor().getLogPrefix(), operation);
            splitPane.getItems().remove(tabDockInfo.getIndex());
            if (operation == TabDockOperation.CLOSE) {
                componentToRemove.getPresenter().deinitializeTree();
                dockHost.getComposer().getModifiableChildren().remove(componentToRemove);
            }
            var dividerSize = splitPane.computeDividerSize();

            // refresh
            refresh();

            if (dividerSize < 0) {
                dividerSize = splitPane.computeDividerSize();
            }
            var mainChildIndex = indexOfMain(splitPaneContainer);
            if (mainChildIndex != -1) {
                splitPane.updateDividersOnRemoveWithMain(oldSplitPaneSize, oldPositions, dividerSize,
                        mainChildIndex, tabDockInfo.getIndex());
            } else {
                splitPane.updateDividersOnRemoveWithoutMain(oldSplitPaneSize, oldPositions, dividerSize,
                        tabDockInfo.getIndex());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("{} Removed {} from {}", dockHost.getDescriptor().getLogPrefix(),
                        componentToRemove.getDescriptor().getFullName(),
                        splitPaneContainer.getChildFullName());
            }
        }

        private void moveTab(TabDockFxView<?> newTabDock) {
            var dragTab = dockHost.dragAndDropHandler.dragTab;
            TabPaneProSkin skin = (TabPaneProSkin) dragTab.getTabPane().getSkin();
            TabPaneProSkin.TabHeaderArea tabHeaderArea = skin.getTabHeaderArea();
            // we don't know if it is a tab dock
            TabFxView<?> tabFxView = (TabFxView<?>) FxViewUtils.getView(dragTab);
            TabHostFxView<?> oldTabHost = (TabHostFxView<?>) tabFxView.getComposer().getParent();
            oldTabHost.getComposer().removeTab(tabFxView);
            newTabDock.getComposer().addTab(tabFxView);
            dockHost.dragAndDropHandler.dragTab = null;
            tabHeaderArea.cleanupAfterDrop();
        }

        private void minimizeTabDock(TabDockFxView<?> dock) {
            var tabDockContainer = getContainer(dock);
            var side = resolveSide(tabDockContainer);

            var parentSplitPane = tabDockContainer.getParentSplitPane();
            var siblings = parentSplitPane.getItems().stream()
                    .map(item -> (AbstractContainer) item)
                    .filter(c -> c != tabDockContainer)
                    .map(AbstractContainer::getChildUuid)
                    .collect(Collectors.toList());

            var pathFromRoot = toRoot(tabDockContainer).stream()
                    .map(AbstractContainer::getChildUuid)
                    .collect(Collectors.toList());

            var index = tabDockContainer.resolvePosition(parentSplitPane).getIndex();

            var pos = new ComponentPosition(pathFromRoot, siblings, parentSplitPane.getOrientation(), side,
                    dock.getDescriptor().getUuid(), index, dock.getNode().getWidth(), dock.getNode().getHeight());
            pos.buildMaps();
            dock.getPresenter().setMinimizedPosition(pos);
            if (logger.isDebugEnabled()) {
                logger.debug("{} Minimized position: {}", dockHost.getDescriptor().getLogPrefix(), pos);
            }

            // removing the dock
            removeTabDock(dock, TabDockOperation.MINIMIZE);

            // creating a sidebar if it is null
            composer.showBar(side);

            var sideBar = dockHost.resolveBar(side).get();
            dock.getComposer().detachTabs();
            // adding the dock to the sidebar
            sideBar.getComposer().addTabDock(dock);
            dockHost.printTreeDebugInfo();
        }

        private void restoreTabDock(TabDockContainer tabDockContainer) {
            var dock = tabDockContainer.getArea();
            dock.getComposer().attachTabs();

            // attempt 0 - find the parent by UUID
            var position = dock.getPresenter().getMinimizedPosition();
            var pathFromRoot = position.getPathFromRoot();
            dock.getPresenter().setMinimizedPosition(null);

            var containersByUuid = collectContainersByUuid();

            var parentUuid = pathFromRoot.get(pathFromRoot.size() - 2); // excluding the node itself
            var parent = asSplitPaneContainer(containersByUuid.get(parentUuid));
            double[] grandParentPositions = null;

            var side = position.getSide(); // the position side can differ from the side bar side
            var sideShouldBeChecked = false;
            int index;
            if (parent != null) {
                index = position.getIndex();
                var childCount = parent.getSplitPane().getItems().size();
                if (index > childCount) {
                    index = childCount;
                }
                logger.debug("{} Original parent split pane available", dockHost.getDescriptor().getLogPrefix());
            } else {
                // attempt 1 - find the nearest living ancestor
                if (pathFromRoot.size() > 2) {
                    for (var i = pathFromRoot.size() - 3; i >= 0; i--) {
                        var uuid = pathFromRoot.get(i);
                        parent = asSplitPaneContainer(containersByUuid.get(uuid));
                        if (parent != null) {
                            if (i == pathFromRoot.size() - 3) { // grandparent
                                var sibling = findSibling(parent, position.getSiblings(), side);
                                if (sibling != null) {
                                    grandParentPositions = parent.getSplitPane().getDividerPositions();
                                    var oldParentUuid = parent.getChildUuid();
                                    parent = wrap(sibling, sibling.resolvePosition().getIndex());
                                    // we created a new container in place of a removed one, so now it is
                                    // necessary to update all UUID collections in all minimized TabDocks.
                                    updateUuidInPositions(oldParentUuid, parent.getChildUuid());
                                }
                            }
                            logger.debug("{} Original parent split pane not available; nearest living ancestor {} is used",
                                    dockHost.getDescriptor().getLogPrefix(), parent.getChildFullName());
                            break;
                        }
                    }
                }
                // attempt 2 - use the root as parent
                if (parent == null) {
                    parent = composer.rootContainer;
                    logger.debug("{} Original parent split pane not available; root is used",
                            dockHost.getDescriptor().getLogPrefix());
                }
                index = resolveNewIndex(parent, side);
                sideShouldBeChecked = true;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("{} Minimized position: {}", dockHost.getDescriptor().getLogPrefix(), position);
            }

            var splitPane = parent.getSplitPane();
            var dockSize = position.getWidth();
            if (splitPane.getOrientation() == Orientation.VERTICAL) {
                dockSize = position.getHeight();
            }
            addTabDock(grandParentPositions, parent, dock, index, side, sideShouldBeChecked, dockSize);
        }

        private Map<UUID, AbstractContainer> collectContainersByUuid() {
            var result = new HashMap<UUID, AbstractContainer>();
            traverse(composer.rootContainer, 0, (c, level) -> result.put(c.getChildUuid(), c));
            return result;
        }

        private @Nullable SplitPaneContainer asSplitPaneContainer(@Nullable AbstractContainer container) {
            return container instanceof SplitPaneContainer spc ? spc : null;
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
         * Checks whether inserting a new child at {@code index} within {@code parentContainer} would actually place it
         * on {@code side} relative to the main area.
         * <p>
         * {@code index} is a purely structural position (a slot within the split), which does not by itself guarantee
         * a particular side relative to the main area — for example, the last slot in a vertically oriented split is
         * {@code BOTTOM}, not {@code RIGHT}, even though "last" might be the natural index to use when adding to the
         * right. This method resolves what side the resulting position would actually be on, by inspecting the
         * existing child that would end up adjacent to the new one, and compares it against the requested {@code side}.
         * <p>
         * This is used by callers that pick a candidate {@code parentContainer}/{@code index} pair as a first guess
         * (e.g. always trying the root split first) and need to verify that the guess is actually correct before
         * committing to it, falling back to a different parent or wrapping the container otherwise.
         *
         * @param parentContainer the split pane container the new child would be inserted into
         * @param index the index the new child would be inserted at
         * @param side the side the new child is required to end up on
         * @return {@code true} if inserting at {@code index} would place the new child on {@code side}
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
            var mainContainer = main != null ? getContainer(main) : null;
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
            if (logger.isDebugEnabled()) {
                logger.debug("{} If tabDock is added into {} at {} its side will be {}, when {} is required",
                        dockHost.getDescriptor().getLogPrefix(), parentContainer.getChildFullName(), index, resolvedSide,
                        side);
            }
            return resolvedSide == side;
        }

        /**
         * Finds a live sibling among the given (possibly stale) sibling UUIDs, to use as an anchor for restoring a
         * minimized {@link TabDockFxView}.
         * <p>
         * Siblings are checked in order; the first one matching {@code side} is returned immediately. If the main area
         * is encountered along the way but does not match {@code side}, it is remembered and returned as a fallback if
         * no better match is found — since the main area is always present, it is always a valid, if imprecise, anchor.
         *
         * @param splitPaneContainer the split pane container whose live children are searched
         * @param siblingUuids the UUIDs of the original siblings, in their original order
         * @param side the preferred side to match
         * @return a live sibling container, preferably matching {@code side}, or {@code null} if none is found
         */
        private @Nullable AbstractContainer findSibling(SplitPaneContainer splitPaneContainer, List<UUID> siblingUuids,
                Side side) {
            var siblingsByUuid = splitPaneContainer.getSplitPane().getItems().stream()
                    .map(item -> (AbstractContainer) item)
                    .collect(Collectors.toMap(AbstractContainer::getChildUuid, c -> c));

            var main = composer.getMain();
            var mainContainer = main != null ? getContainer(main) : null;

            AbstractContainer mainFallback = null;
            for (var uuid : siblingUuids) {
                var sibling = siblingsByUuid.get(uuid);
                if (sibling == null) {
                    continue;
                }
                if (sibling == mainContainer) {
                    mainFallback = sibling;
                } else if (sibling instanceof TabDockContainer tdc && resolveSide(tdc) == side) {
                    return sibling;
                }
            }
            return mainFallback;
        }

        /**
         * Finds the index of the child of the specified split pane container that contains the main area in its
         * hierarchy.
         * <p>
         * The main area lives somewhere deeper in the tree, inside one specific branch descending from
         * {@code splitPaneContainer}. This method finds which of {@code splitPaneContainer}'s direct children is the
         * root of that branch — i.e. which child either is the main area itself, or is a nested split that eventually
         * contains it.
         * <p>
         * It works by computing the path from the docking layout's root down to the main area
         * ({@link #toRoot(AbstractContainer)}), then checking each direct child of {@code splitPaneContainer} against
         * that path: the child that appears on the path is the one leading to the main area.
         * <p>
         * This is used to determine which side of a split the main area is on, so that when dividers are added,
         * removed, or resized, the main area's on-screen position and proportions stay stable while the surrounding
         * docks are the ones that grow or shrink.
         *
         * @param splitPaneContainer the split pane container to search in
         * @return the index of the child leading to the main area, or {@code -1} if there is no main area, or this
         *         split pane container is not on its path
         */
        private int indexOfMain(SplitPaneContainer splitPaneContainer) {
            var main = composer.getMain();
            if (main == null) {
                return -1;
            }
            var mainContainer = getContainer(main);
            var path = toRoot(mainContainer);
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

        /**
         * Updates the UUID in minimized {@link TabDockFxView}s. This is required when a new {@link SplitSpaceFxView}
         * is created in place of a removed {@link SplitSpaceFxView}.
         *
         * @param oldUuid
         * @param newUuid
         */
        private void updateUuidInPositions(UUID oldUuid, UUID newUuid) {
            updateUuidInPositions(composer.getRightBar(), oldUuid, newUuid);
            updateUuidInPositions(composer.getLeftBar(), oldUuid, newUuid);
            updateUuidInPositions(composer.getBottomBar(), oldUuid, newUuid);
            logger.debug("{} Replaced UUID {} with {} in position lists of all minimized TabDocks",
                    dockHost.getDescriptor().getLogPrefix(), oldUuid, newUuid);
        }

        private void updateUuidInPositions(SideBarFxView<?> sideBar, UUID oldUuid, UUID newUuid) {
            if (sideBar != null) {
                for (var tabDock : sideBar.getComposer().getTabDocks()) {
                    var position = tabDock.getPresenter().getMinimizedPosition();
                    position.updateUuid(oldUuid, newUuid);
                }
            }
        }

        /**
         * Resolves the side of the given container relative to the main area. See
         * {@link #resolveSide(TabDockContainer)} for the algorithm; this overload additionally accepts
         * {@link SplitPaneContainer}, for cases where a nested split itself needs to be compared against the main area.
         *
         * @param container the container whose side relative to the main area is resolved; must not be the main
         *         area's own container
         * @return the resolved side; one of {@code LEFT}, {@code RIGHT}, {@code TOP}, or {@code BOTTOM}
         * @throws IllegalStateException if the layout has no main area
         * @throws IllegalArgumentException if {@code container} is the main area's own container
         */
        private Side resolveSide(AbstractContainer container) {
            var main = composer.getMain();
            if (main == null) {
                throw new IllegalStateException("Cannot resolve side without a main area");
            }
            var mainContainer = getContainer(main);
            if (container == mainContainer) {
                throw new IllegalArgumentException("Cannot resolve the side of the main area itself");
            }

            var componentPath = toRoot(container);
            var mainPath = toRoot(mainContainer);

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

            if (logger.isDebugEnabled()) {
                logger.debug("{} Resolved side for {} is {}; lowest common ancestor: {}",
                        dockHost.getDescriptor().getLogPrefix(), container.getChildFullName(), result,
                        lca.getChildFullName());
            }
            return result;
        }

        /**
         * Resolves the side of the given {@link TabDockFxView} relative to the main area.
         * <p>
         * The docking layout always has a main area — it is the anchor around which every other {@link TabDockFxView}
         * is positioned, and this method requires it to be present; there is currently no meaningful way to resolve a
         * side without it.
         *
         * @param tabDockContainer the container whose side relative to the main area is resolved
         * @return the resolved side; one of {@code LEFT}, {@code RIGHT}, {@code TOP}, or {@code BOTTOM}
         * @throws IllegalStateException if the layout has no main area
         */
        private Side resolveSide(TabDockContainer tabDockContainer) {
            return resolveSide((AbstractContainer) tabDockContainer);
        }
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

    private static void destroyContainer(AbstractContainer container) {
        // it is necessary to remove the component node from the container
        // because we will continue to use the component, but not the container
        container.getChildren().clear();
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

    private static final Logger logger = LoggerFactory.getLogger(DockHostFxView.class);

    private static final Double DRAG_REGION_MAX_WIDTH = 200.0;

    public class Composer extends AbstractAreaFxView<P>.Composer implements DockHostView.Composer {

        private final DockHostFxView<P> view = DockHostFxView.this;

        private PlaceholderFxView placeholder;

        private SplitPaneContainer rootContainer = null;

        private final ObjectProperty<AreaFxView<?>> main = new SimpleObjectProperty<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> rightBar = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<TabPopupFxView<?>> rightPopup = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> bottomBar = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<TabPopupFxView<?>> bottomPopup = new ReadOnlyObjectWrapper<>();

        private final ReadOnlyObjectWrapper<SideBarFxView<?>> leftBar = new ReadOnlyObjectWrapper<>();

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

        public void applyModel(SplitModelNode root) {
            var rootContainer = (SplitPaneContainer) build(root);
            setRoot(rootContainer);
            view.printTreeDebugInfo();
        }

        public ModelNode captureModel() {
            return null;
        }

        @Override
        public AreaPort getMainPort() {
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
         *
         * @param dock
         * @param side
         * @param size
         */
        public void addTabDock(TabDockFxView<?> dock, Side side, double size) {
            dock.getComposer().setDockHost(view);
            var index = view.transformer.resolveNewIndex(rootContainer, side);
            view.transformer.addTabDock(null, rootContainer, dock, index, side, true, size);
        }

        public void removeTabDock(TabDockFxView<?> dock) {
            view.transformer.removeTabDock(dock, TabDockOperation.MOVE);
            view.printTreeDebugInfo();
            dock.getComposer().setDockHost(null);
            logger.debug("{} Removed TabDock", getDescriptor().getLogPrefix());
        }

        public void closeTabDock(TabDockFxView<?> dock) {
            view.transformer.removeTabDock(dock, TabDockOperation.CLOSE);
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
        public SideBarPort getRightBarPort() {
            return getRightBar() == null ? null : getRightBar().getPresenter();
        }

        @Override
        public SideBarPort getBottomBarPort() {
            return getBottomBar() == null ? null : getBottomBar().getPresenter();
        }

        @Override
        public SideBarPort getLeftBarPort() {
            return getLeftBar() == null ? null : getLeftBar().getPresenter();
        }

        @Override
        public SideBarPort getBarPort(Side side) {
            var barFxView = view.resolveBar(side).get();
            if (barFxView != null) {
                return barFxView.getPresenter();
            } else {
                return null;
            }
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
        public TabPopupPort getRightPopupPort() {
            return getRightPopup() == null ? null : getRightPopup().getPresenter();
        }

        @Override
        public TabPopupPort getBottomPopupPort() {
            return getBottomPopup() == null ? null : getBottomPopup().getPresenter();
        }

        @Override
        public TabPopupPort getLeftPopupPort() {
            return getLeftPopup() == null ? null : getLeftPopup().getPresenter();
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
        public final ObjectProperty<AreaFxView<?>> mainProperty() {
            return main;
        }

        /**
         * Returns the value of {@link #mainProperty()}.
         *
         * @return the main area
         */
        public final AreaFxView<?> getMain() {
            return main.get();
        }

        /**
         * Sets the value of {@link #mainProperty()}.
         *
         * @param value the main area
         */
        public void setMain(AreaFxView<?> value) {
            this.main.set(value);
        }

        public final SideBarFxView<?> getRightBar() {
            return rightBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> rightBarProperty() {
            return rightBar.getReadOnlyProperty();
        }

        public final TabPopupFxView<?> getRightPopup() {
            return rightPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> rightPopupProperty() {
            return rightPopup.getReadOnlyProperty();
        }

        public final SideBarFxView<?> getBottomBar() {
            return bottomBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> bottomBarProperty() {
            return bottomBar.getReadOnlyProperty();
        }

        public final TabPopupFxView<?> getBottomPopup() {
            return bottomPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> bottomPopupProperty() {
            return bottomPopup.getReadOnlyProperty();
        }

        public final SideBarFxView<?> getLeftBar() {
            return leftBar.get();
        }

        public final ReadOnlyObjectProperty<SideBarFxView<?>> leftBarProperty() {
            return leftBar.getReadOnlyProperty();
        }

        public final TabPopupFxView<?> getLeftPopup() {
            return leftPopup.get();
        }

        public final ReadOnlyObjectProperty<TabPopupFxView<?>> leftPopupProperty() {
            return leftPopup.getReadOnlyProperty();
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
            var wrapper = view.resolvePopup(popup.getPresenter().getSide());
            wrapper.set(popup);
        }

        void closeTabPopup(Side side) {
            var wrapper = view.resolvePopup(side);
            var popup = wrapper.get();
            if (popup != null) {
                view.centerStackPane.getChildren().remove(popup.getNode());
                getModifiableChildren().remove(popup);
                popup.getPresenter().deinitializeTree();
                wrapper.set(null);
            }
        }

        void restoreTabDock(TabDockFxView<?> tabDock) {
            view.transformer.restoreTabDock(getContainer(tabDock));
        }

        void minimizeTabDock(TabDockFxView<?> tabDock) {
            view.transformer.minimizeTabDock(tabDock);
        }

        private Node build(ModelNode node) {
            if (node instanceof AreaModelNode areaNode) {
                getModifiableChildren().add(areaNode.getArea());
                if (areaNode.isMain()) {
                    setMain(areaNode.getArea());
                    return new MainAreaContainer(view, areaNode.getArea());
                } else {
                    return new TabDockContainer(view, (TabDockFxView<?>) areaNode.getArea());
                }
            } else if (node instanceof SplitModelNode splitNode) {
                var splitPane = new DockSplitPane(getDescriptor().getLogPrefix());
                splitPane.setOrientation(splitNode.getOrientation());
                for (ModelNode child : splitNode.getChildren()) {
                    splitPane.getItems().add(build(child));
                }
                applyDividerPositions(splitPane, splitNode.getChildren());
                return new SplitPaneContainer(view, splitPane);
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
                    if (view.resolveBar(side).get() == null) {
                        showBar(side);
                    }
                } else if (newV == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    var bar = view.resolveBar(side).get();
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

    public final TabPopupFxView<?> getPopup(Side side) {
        var wrapper = resolvePopup(side);
        return wrapper.get();
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
            var w = centerStackPane.getWidth();
            var h = centerStackPane.getHeight();
            updatePopupSize(getComposer().getRightBar(), w, h);
            updatePopupSize(getComposer().getBottomBar(), w, h);
            updatePopupSize(getComposer().getLeftBar(), w, h);
        });
        centerStackPane.heightProperty().addListener((ov2, oldV2, newV2) -> {
            var w = centerStackPane.getWidth();
            var h = centerStackPane.getHeight();
            updatePopupSize(getComposer().getRightBar(), w, h);
            updatePopupSize(getComposer().getBottomBar(), w, h);
            updatePopupSize(getComposer().getLeftBar(), w, h);
        });
    }

    Dimension2D getCenterDimension() {
        return new Dimension2D(this.centerStackPane.getWidth(), this.centerStackPane.getHeight());
    }

    DragAndDropHandler getDragAndDropHandler() {
        return dragAndDropHandler;
    }

    private SplitPaneContainer createContainer(DockSplitPane splitPane) {
        return createContainer(this, splitPane);
    }

    private AbstractAreaContainer<?> createContainer(AreaFxView<?> child) {
        return createContainer(this, child);
    }

    private void updatePopupSize(SideBarFxView<?> sideBar, double width, double height) {
        if (sideBar != null) {
            var popup = getPopup(sideBar.getPresenter().getSide());
            if (popup != null) {
                popup.updateSize(width, height);
            }
        }
    }

    private ReadOnlyObjectWrapper<TabPopupFxView<?>> resolvePopup(Side side) {
        return switch (side) {
            case RIGHT -> getComposer().rightPopup;
            case BOTTOM -> getComposer().bottomPopup;
            case LEFT -> getComposer().leftPopup;
            default -> throw new AssertionError();
        };
    }

    private ReadOnlyObjectWrapper<SideBarFxView<?>> resolveBar(Side side) {
        return switch (side) {
            case RIGHT -> getComposer().rightBar;
            case BOTTOM -> getComposer().bottomBar;
            case LEFT -> getComposer().leftBar;
            default -> throw new AssertionError();
        };
    }

    private void printTreeDebugInfo() {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Docking layout tree: {}", getDescriptor().getLogPrefix(), getTreeDebugInfo());
        }
    }

    private String getTreeDebugInfo() {
        StringBuilder builder = new StringBuilder();
        traverse(getComposer().rootContainer, 0, (container, level) -> {
            String orientation = "";
            String state = null;
            String uuid = container.getChildShortUuid();
            if (container instanceof SplitPaneContainer spc) {
                orientation = spc.getSplitPane().getOrientation().name();
            } else if (container instanceof AbstractAreaContainer<?> aac) {
                state = aac.getArea().getDescriptor().getState().name();
            }
            builder.append("\n");
            builder.append("    ".repeat(level));
            builder.append(container.getChildName());
            builder.append(" [");
            builder.append("shortUuid: ");
            builder.append(uuid);
            if (orientation.length() > 0) {
                builder.append(", orientation: ");
                builder.append(orientation);
            }
            if (state != null) {
                builder.append(", state: ");
                builder.append(state);
            }
            builder.append("]");
        });
        return builder.toString();
    }
}
