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

package com.techsenger.tabshell.layout.dockhost;

import com.techsenger.patternfx.mvp.ChildFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin.TabHeaderArea;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.area.AreaFxView;
import com.techsenger.tabshell.core.area.AreaParams;
import com.techsenger.tabshell.core.area.AreaPort;
import com.techsenger.tabshell.core.tab.TabFxView;
import static com.techsenger.tabshell.layout.dockhost.DockConstants.ONE_HALF;
import static com.techsenger.tabshell.layout.dockhost.DockConstants.ONE_THIRD;
import com.techsenger.tabshell.layout.style.LayoutIcons;
import com.techsenger.tabshell.layout.tabhost.TabHostFxView;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.core.Pair;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
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
 * How it works. When the user starts a drag-and-drop operation and moves the mouse, an instance of {@link DockInfo} is
 * created. If the user releases the mouse, the data in this object is used to perform the relocation of either a Tab
 * or a TabDock. The reason for creating this object so early is that, while the user is moving the mouse, a potential
 * target region needs to be highlighted, and that also requires a {@link DockInfo}.
 *
 * <p>The user can move either a single Tab or an entire existing TabDock. It is important to note that drag-and-drop
 * support for Tab is already implemented in TabPanePro, whereas the drag-and-drop functionality for TabDock is
 * implemented in this class.
 *
 * <p>There are three types of components: MainComponent — contains the main component, SplitSpace — contains a
 * SplitPane, and TabDock — contains a TabPane. MainComponent and TabDock always acts as a leaf. The main component can
 * never be closed or minimized into a TabDockBar by the user while TabDock can be both closed and minimized.
 * All components are wrapped in containers that provide additional functionality for the docking system.
 *
 * <p>Containers have two elements: indicator, which highlights the new tab position, and eventPane, which tracks
 * mouse movement inside the component during a drag-and-drop operation.
 *
 * <p>All mouse events are captured within a MainComponent and TabDock, and from the cursor position two variables are
 * determined: the nearest side of the TabDock (Side side) and whether the mouse is within EDGE_THRESHOLD
 * (boolean edgeMode).
 *
 * <p>When a new tab is docked, the operation may require wrapping an existing node with a new SplitSpace. The target of
 * this wrapping can be the TabDock itself, its parent SplitSpace, or its grandparent SplitSpace. The decision is made
 * dynamically based on the values of side and edgeMode together with the orientation of the parent SplitSpace,
 * ensuring that the system adapts correctly whether docking occurs inside a node, along its edge, or at the boundary
 * of a larger layout.
 *
 * <p>All possible cases:
 * | #  | SplitSpace  | EdgeMode | Side       | Node Location | Action                                              |
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

    /**
     * Containers are not part of the components, since the main component can be any class — there is no requirement
     * that it must inherit from a component in this package.
     *
     * <p>Containers are created when components are added to the {@link SplitSpaceFxView} and destroyed when they are
     * removed from there.
     */
    private abstract static class AbstractContainer<T extends AreaFxView<?>> extends StackPane {

        private final DockHostFxView<?> dockHost;

        private final T area;

        AbstractContainer(DockHostFxView<?> dockHost, T area) {
            this.dockHost = dockHost;
            this.area = area;
            getChildren().add(area.getNode());
        }

        DockHostFxView<?> getDockHost() {
            return dockHost;
        }

        T getArea() {
            return area;
        }

        abstract void updateDragInProgress(boolean value, DraggableType type);

        ContainerInfo createInfo() {
            ContainerInfo result;
            if (area != dockHost.getComposer().getRoot()) {
                result = createInfo((SplitSpaceFxView<?>) area.getParent());
            } else {
                result = createInfo(null);
            }
            return result;
        }

        ContainerInfo createInfo(SplitSpaceFxView<?> parent) {
            if (parent != null) {
                var parentSplitPane = parent.getNode();
                var containerIndex = parentSplitPane.getItems().indexOf(this);
                return new ContainerInfo(this, containerIndex);
            } else {
                return new ContainerInfo(this, -1);
            }
        }
    }

    private static class SplitSpaceContainer extends AbstractContainer<SplitSpaceFxView<?>> {

        private final Rectangle indicator = createIndicator();

        SplitSpaceContainer(DockHostFxView<?> dockHost, SplitSpaceFxView<?> view) {
            super(dockHost, view);
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

        private Rectangle createIndicator() {
            Rectangle result = new Rectangle();
            result.setManaged(false);
            result.setVisible(false);
            result.getStyleClass().add("indicator");
            return result;
        }
    }

    /**
     * Note: the event pane is not located over the {@link TabHeaderArea}. Therefore, if the mouse is over the
     * {@link TabHeaderArea} when the drag is released, the event is handled by the {@link TabHeaderArea}.
     * If the mouse is not over the {@link TabHeaderArea}, the drag released event is handled by this
     * {@link AbstractEventContainer}.
     *
     */
    private abstract static class AbstractEventContainer<T extends AreaFxView<?>>
            extends AbstractContainer<T> {

        private final Pane eventPane = new Pane();

        AbstractEventContainer(DockHostFxView<?> dockHost, T area) {
            super(dockHost, area);
            eventPane.setMouseTransparent(false);
            // eventPane.setStyle("-fx-background-color: yellow");
            eventPane.setOnMouseDragOver(e -> getDockHost().onContainerMouseDragOver(provideMousePosition(e)));
            eventPane.setOnMouseDragExited(e -> getDockHost().onContainerMouseDragExited(provideMousePosition(e)));
            eventPane.setOnMouseDragReleased(e -> getDockHost()
                    .onContainerMouseDragReleased(provideMousePosition(e)));
        }

        @Override
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

    private static class MainContainer extends AbstractEventContainer<AreaFxView<?>> {

        MainContainer(DockHostFxView<?> dockHost, AreaFxView<?> area) {
            super(dockHost, area);
        }

    }

    private static class TabDockContainer extends AbstractEventContainer<TabDockFxView<?>> {

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

    private static class ContainerInfo {

        private final AbstractContainer<?> container;

        private final int index;

        private double fraction;

        ContainerInfo(int index, double fraction) {
            this(null, index);
            this.fraction = fraction;
        }

        ContainerInfo(AbstractContainer<?> container, int index) {
            this.container = container;
            this.index = index;
        }

        public AbstractContainer<?> getContainer() {
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
            var splitPane = ((SplitSpaceFxView<?>) container.getArea().getParent()).getNode();
            return index == splitPane.getItems().size() - 1;
        }

        @Override
        public String toString() {
            return "ContainerInfo [" + "container:" + container + ", index:" + index + ", fraction:" + fraction + ']';
        }
    }

    /**
     * Represents a potential dock info during drag-and-drop operations.
     */
    private static class DockInfo {

        private MousePosition mousePosition;

        /**
         * Information about the container that got mouse drag event.
         */
        private ContainerInfo eventInfo;

        /**
         * Information about event container parent.
         */
        private ContainerInfo parentInfo;

        private ContainerInfo grandparentInfo;

        private ContainerInfo greatGrandparentInfo;

        /**
         * Information about new container (container is null).
         */
        private ContainerInfo newInfo;

        /**
         * Information about the container that shows indicator.
         */
        private ContainerInfo indicatorInfo;

        private Bounds indicatorBounds;

        /**
         * Adds area to split space.
         */
        private BiConsumer<DockInfo, TabDockFxView<?>> composer;

        private boolean valid;

        DockInfo() {

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

        public BiConsumer<DockInfo, TabDockFxView<?>> getComposer() {
            return composer;
        }

        public void setComposer(BiConsumer<DockInfo, TabDockFxView<?>> composer) {
            this.composer = composer;
        }

        public ContainerInfo getEventInfo() {
            return eventInfo;
        }

        public void setEventInfo(ContainerInfo eventInfo) {
            this.eventInfo = eventInfo;
        }

        public ContainerInfo getParentInfo() {
            return parentInfo;
        }

        public void setParentInfo(ContainerInfo parentInfo) {
            this.parentInfo = parentInfo;
        }

        public ContainerInfo getGrandparentInfo() {
            return grandparentInfo;
        }

        public void setGrandparentInfo(ContainerInfo grandparentInfo) {
            this.grandparentInfo = grandparentInfo;
        }

        public ContainerInfo getGreatGrandparentInfo() {
            return greatGrandparentInfo;
        }

        public void setGreatGrandparentInfo(ContainerInfo greatGrandparentInfo) {
            this.greatGrandparentInfo = greatGrandparentInfo;
        }

        public ContainerInfo getIndicatorInfo() {
            return indicatorInfo;
        }

        public void setIndicatorInfo(ContainerInfo indicatorInfo) {
            this.indicatorInfo = indicatorInfo;
        }

        public ContainerInfo getNewInfo() {
            return newInfo;
        }

        public void setNewInfo(ContainerInfo newInfo) {
            this.newInfo = newInfo;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public String toString() {
            return "DockInfo [" + "mousePosition:" + mousePosition + ", eventInfo:" + eventInfo
                    + ", parentInfo:" + parentInfo + ", grandparentInfo:" + grandparentInfo
                    + ", greatGrandparentInfo:" + greatGrandparentInfo + ", newInfo:" + newInfo
                    + ", indicatorInfo:" + indicatorInfo + ", indicatorBounds:" + indicatorBounds
                    + ", composer:" + composer + ", valid:" + valid + ']';
        }
    }

    /**
     * Returns the existing container node for a component that is currently on the scene.
     * @param view
     * @return
     */
    private static AbstractContainer<?> getContainer(AreaFxView<?> view) {
        return (AbstractContainer<?>) view.getNode().getParent();
    }

    private static boolean hasContainer(AreaFxView<?> view) {
        return view.getNode().getParent() instanceof  AbstractContainer<?>;
    }

    private static TabDockContainer getContainer(TabDockFxView<?> view) {
        return (TabDockContainer) view.getNode().getParent();
    }

    private static SplitSpaceContainer getContainer(SplitSpaceFxView<?> view) {
        return (SplitSpaceContainer) view.getNode().getParent();
    }

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

    private static Bounds createHalfIndicatorBounds(Side side, ContainerInfo anchorInfo) {
        var anchorContainer = anchorInfo.getContainer();
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

    private static Bounds createThirdIndicatorBounds(Side side, ContainerInfo anchorInfo, ContainerInfo ancestorInfo) {
        var anchorContainer = anchorInfo.getContainer();
        var ancestorContainer = ancestorInfo.getContainer();
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

    private static Bounds createIntermediateIndicatorBounds(Side side, ContainerInfo parentInfo,
            ContainerInfo anchorInfo, ContainerInfo siblingInfo) {
        SplitSpaceContainer splitSpaceContainer = (SplitSpaceContainer) parentInfo.getContainer();
        var splitPane = splitSpaceContainer.getArea().getNode();
        var anchorContainer = anchorInfo.getContainer();

        double width;
        double height;
        var anchorSceneXY = getSceneXY(anchorContainer);
        double x = anchorSceneXY.getFirst();
        double y = anchorSceneXY.getSecond();

        AbstractContainer<?> siblingContainer = siblingInfo.getContainer();
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
                        - (height * siblingInfo.getFraction());
                break;
            case RIGHT:
                height = anchorContainer.getHeight();
                width = computeIntermediateContainerSize(anchorWidth, siblingtWidth);
                width = splitPane.snapSizeX(width);
                x += (anchorWidth - width * anchorInfo.getFraction());
                break;
            case BOTTOM:
                width = anchorContainer.getWidth();
                height = computeIntermediateContainerSize(anchorHeight, siblingHeight);
                height = splitPane.snapSizeY(height);
                y += (anchorContainer.getHeight() - height * anchorInfo.getFraction());
                break;
            case LEFT:
                height = anchorContainer.getHeight();
                width = computeIntermediateContainerSize(anchorWidth, siblingtWidth);
                width = splitPane.snapSizeX(width);
                // Due to the divider, the position is calculated based on the adjacent panel, so that when
                // the left/right side is the same, the result is consistent.
                siblingSceneXY = getSceneXY(siblingContainer);
                x = siblingSceneXY.getFirst() + siblingContainer.getWidth()
                        - (width * siblingInfo.getFraction());
                break;
            default:
                throw new AssertionError();
        }
        // Math.floor() gives correct results while snapSize doesn't
        var result = new BoundingBox(Math.floor(x), Math.floor(y), Math.floor(width), Math.floor(height));
        return result;
    }

    private static Pair<Double, Double> getSceneXY(Region container) {
        Bounds mainBounds = container.getBoundsInLocal();
        Point2D mainTopLeftCorner = new Point2D(mainBounds.getMinX(), mainBounds.getMinY());
        Point2D mainTopLeftInScene = container.localToScene(mainTopLeftCorner);
        return new Pair<>(container.snapSizeX(mainTopLeftInScene.getX()),
                container.snapSizeY(mainTopLeftInScene.getY()));
    }

    private static void setContainerFractions(Side side, ContainerInfo anchorInfo, ContainerInfo siblingInfo) {
        double anchorContainerSize;
        double anchorContainerFraction = 0.0;
        double siblingContainerSize;
        double siblingContainerFraction = 0.0;
        var anchorContainer = anchorInfo.getContainer();
        var siblingContainer = siblingInfo.getContainer();

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

        anchorInfo.setFraction(anchorContainerFraction);
        siblingInfo.setFraction(siblingContainerFraction);
    }

    private static double computeIntermediateContainerSize(double firstContainerSize, double secondContainerSize) {
        return ONE_THIRD * (firstContainerSize + secondContainerSize);
    }

    private static ContainerInfo createSiblingInfo(ContainerInfo parentInfo,
            ContainerInfo anchorInfo, int siblingIndex) {
        SplitPane splitPane = (SplitPane) parentInfo.getContainer().getArea().getNode();
        var siblingContainer = ((AbstractContainer<?>) splitPane.getItems().get(siblingIndex));
        var singlingInfo = siblingContainer.createInfo();
        return singlingInfo;
    }

    private static final Logger logger = LoggerFactory.getLogger(DockHostFxView.class);

    private static final Double EDGE_THRESHOLD = 20.0;

    private static final Double DRAG_REGION_MAX_WIDTH = 200.0;

    public class Composer extends AbstractAreaFxView<P>.Composer implements DockHostView.Composer {

        private final DockHostFxView<P> view = DockHostFxView.this;

        private PlaceholderFxView placeholder;

        private final ObjectProperty<SplitSpaceFxView<?>> root = new SimpleObjectProperty<>();

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

        @Override
        public void compose() {
            super.compose();
            var placeholderV = createPlaceholder();
            placeholder = placeholderV;

            addListenerToBarPolicy(rightBarPolicy, RIGHT);
            addListenerToBarPolicy(bottomBarPolicy, BOTTOM);
            addListenerToBarPolicy(leftBarPolicy, LEFT);

            if (getRightBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                addBar(Side.RIGHT);
            }
            if (getBottomBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                addBar(Side.BOTTOM);
            }
            if (getLeftBarPolicy() == SideBarPolicy.EXISTS_ALWAYS) {
                addBar(Side.LEFT);
            }
        }

        public void setRoot(SplitSpaceFxView<?> value) {
            this.root.set(value);
        }

        public void setMain(AreaFxView<?> value) {
            this.main.set(value);
        }

        @Override
        public SplitSpacePort getRootPort() {
            return getRoot() == null ? null : getRoot().getPresenter();
        }

        @Override
        public AreaPort getMainPort() {
            return getMain() == null ? null : getMain().getPresenter();
        }

        public TabDockFxView<?> createTabDock() {
            var v = new TabDockFxView<>();
            v.setDockHost(view);
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
            dock.setDockHost(view);
            var index = resolveNewIndex(getRoot(), side);
            view.addTabDock(null, getRoot(), dock, index, side, true, size);
        }

        public void removeTabDock(TabDockFxView<?> dock) {
            view.removeTabDock(dock, true);
            view.printTreeDebugInfo();
            logger.debug("{} Removed TabDock", getDescriptor().getLogPrefix());
        }

        public SplitSpaceFxView<?> createSplitSpace() {
            var v = new SplitSpaceFxView<>();
            v.getComposer().setDockHost(view);
            var p = new SplitSpacePresenter<>(v, new AreaParams());
            p.initialize();
            return v;
        }

        public void addBar(Side side) {
            var layoutHistory = view.getPresenter().getHistory();
            SideBarHistory barHistory = null;
            switch (side) {
                case RIGHT -> {
                    barHistory = layoutHistory.getOrCreateRightSideBar();
                    addBar(side, barHistory, rightBar, v -> view.getNode().setRight(v.getNode()));
                }
                case LEFT -> {
                    barHistory = layoutHistory.getOrCreateLeftSideBar();
                    addBar(side, barHistory, leftBar, v -> view.getNode().setLeft(v.getNode()));
                }
                case TOP, BOTTOM -> {
                    side = Side.BOTTOM;
                    barHistory = layoutHistory.getOrCreateBottomSideBar();
                    addBar(side, barHistory, bottomBar, v -> view.getNode().setBottom(v.getNode()));
                }
                default -> throw new AssertionError();
            }
        }

        public void removeBar(Side side) {
            switch (side) {
                case RIGHT -> {
                    view.getNode().setRight(null);
                    var sideBar = getRightBar();
                    rightBar.set(null);
                    sideBar.getPresenter().deinitializeTree();
                    getModifiableChildren().remove(sideBar);
                }
                case BOTTOM -> {
                    view.getNode().setBottom(null);
                    var sideBar = getBottomBar();
                    bottomBar.set(null);
                    sideBar.getPresenter().deinitializeTree();
                    getModifiableChildren().remove(sideBar);
                }
                case LEFT -> {
                    view.getNode().setLeft(null);
                    var sideBar = getLeftBar();
                    leftBar.set(null);
                    sideBar.getPresenter().deinitializeTree();
                    getModifiableChildren().remove(sideBar);
                }
                default -> throw new AssertionError();
            }
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

        public final SplitSpaceFxView<?> getRoot() {
            return root.get();
        }

        public final ObjectProperty<SplitSpaceFxView<?>> rootProperty() {
            return root;
        }

        public final AreaFxView<?> getMain() {
            return main.get();
        }

        public final ObjectProperty<AreaFxView<?>> mainProperty() {
            return main;
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
            v.setDockHost(view);
            var p = new PlaceholderPresenter(v, new AreaParams());
            p.initialize();
            return v;
        }

        void addTabPopup(TabPopupFxView<?> popup) {
            getModifiableChildren().add(popup);
            var popupNode = popup.getNode();
            view.centerStackPane.getChildren().add(popupNode);
            var wrapper = view.resolvePopup(popup.getPresenter().getSide());
            wrapper.set(popup);
        }

        void removeTabPopup(Side side) {
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
            view.restoreTabDock(tabDock);
        }

        void minimizeTabDock(TabDockFxView<?> tabDock) {
            view.minimizeTabDock(tabDock);
        }

        private SideBarFxView<?> addBar(Side side, SideBarHistory sideBarHistory,
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
                        addBar(side);
                    }
                } else if (newV == SideBarPolicy.EXISTS_WHEN_TABS_PRESENT) {
                    var bar = view.resolveBar(side).get();
                    if (bar != null && bar.getComposer().getTabDockPorts().isEmpty()) {
                        removeBar(side);
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

    /**
     * The tab is being dragged.
     */
    private Tab dragTab;

    /**
     * The dock that is being dragged.
     */
    private TabDockFxView<?> dragDock;

    private Popup dragDockPopup;

    private DockInfo dockInfo;

    private boolean dragInProgress;

    @Override
    public void requestFocus() {

    }

    @Override
    public BorderPane getNode() {
        return this.node;
    }

    public DragAndDropContext getDragAndDropContext() {
        return dragAndDropContext;
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
        // the root is a child, the main one is not
        getComposer().root.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                centerStackPane.getChildren().remove(0);
                getModifiableChildren().remove(oldV);
            }
            if (newV != null) {
                SplitSpaceContainer container = new SplitSpaceContainer(this, newV);
                centerStackPane.getChildren().add(0, container); // there can be tab popup
                getModifiableChildren().add(newV);
            }
        });
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

    StackPane createContainer(AreaFxView<?> child) {
        AbstractContainer container;
        if (child instanceof SplitSpaceFxView<?>) {
            var splitSpace = (SplitSpaceFxView<?>) child;
            container = new SplitSpaceContainer(this, splitSpace);
            SplitPane.setResizableWithParent(container, false);
        } else if (child instanceof TabDockFxView<?>) {
            var tabDock = (TabDockFxView<?>) child;
            container = new TabDockContainer(this, tabDock);
            SplitPane.setResizableWithParent(container, false);
        } else {
            container = new MainContainer(this, child);
            SplitPane.setResizableWithParent(container, true);
        }
        return container;
    }

    void destroyContainer(StackPane container) {
        // it is necessary to remove the component node from the container
        // because we will continue to use the component, but not the container
        container.getChildren().clear();
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
        var scene = getNode().getScene();
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
        this.dockInfo = createBaseDockInfo(mousePosition);
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

    private void minimizeTabDock(TabDockFxView<?> dock) {
        // saving current position
        var side = resolveSide(dock);
        SplitSpaceFxView<?> parent = (SplitSpaceFxView<?>) dock.getParent();
        var index = parent.getChildren().indexOf(dock);
        var siblings = parent.getChildren().stream().filter(c -> c != dock)
                .map(c -> c.getDescriptor().getUuid()).collect(Collectors.toList());
        var pathFromRoot = findPathFromRoot(dock)
                .stream()
                .map(v -> v.getDescriptor().getUuid())
                .collect(Collectors.toList());
        var pos = new ComponentPosition(pathFromRoot, siblings, parent.getNode().getOrientation(), side,
                dock.getDescriptor().getUuid(), index, dock.getNode().getWidth(), dock.getNode().getHeight());
        pos.buildMaps();
        dock.getPresenter().setMinimizedPosition(pos);
        if (logger.isDebugEnabled()) {
            parent.logState("Before minimizing");
            logger.debug("{} Minimized position: {}", getDescriptor().getLogPrefix(), pos);
        }
        // removing the dock
        removeTabDock(dock, false);
        // createaing a sidebar if it is null
        getComposer().addBar(side);

        var sideBar = resolveBar(side).get();
        dock.getComposer().detachTabs();
        // adding the dock to the sidebar
        sideBar.getComposer().addTabDock(dock);
        printTreeDebugInfo();
    }

    private void restoreTabDock(TabDockFxView<?> dock) {
        dock.getComposer().attachTabs();

        // attempt 0 - find the parent by UUID
        var position = dock.getPresenter().getMinimizedPosition();
        var pathFromRoot = position.getPathFromRoot();
        dock.getPresenter().setMinimizedPosition(null);
        var splitSpacesByUuid = new HashMap<UUID, SplitSpaceFxView<?>>();
        var iterator = getComposer().getRoot().breadthFirstIterator();
        while (iterator.hasNext()) {
            ChildFxView<?> component = (ChildFxView<?>) iterator.next();
            if (component instanceof SplitSpaceFxView<?> c) {
                splitSpacesByUuid.put(c.getDescriptor().getUuid(), c);
            }
        }
        var parentUuid = pathFromRoot.get(pathFromRoot.size() - 2); // excluding the node itself
        SplitSpaceFxView<?> parent = splitSpacesByUuid.get(parentUuid);
        double[] grandParentPositions = null;

        var side = position.getSide(); // the position side can differ from the side bar side
        var sideShouldBeChecked = false;
        int index;
        if (parent != null) {
            index = position.getIndex();
            var childCount = parent.getChildren().size();
            if (index > childCount) {
                index = childCount;
            }
            logger.debug("{} Original parent SplitSpace available", getDescriptor().getLogPrefix());
        } else {
            // attempt 1 - find the nearest living ancestor
            if (pathFromRoot.size() > 2) {
                for (var i = pathFromRoot.size() - 3; i >= 0; i--) {
                    var uuid = pathFromRoot.get(i);
                    parent = splitSpacesByUuid.get(uuid);
                    if (parent != null) {
                        if (i == pathFromRoot.size() - 3) { // grandparent
                            var sibling = findSibling(parent, position.getSiblings(), side);
                            if (sibling != null) {
                                grandParentPositions = parent.getNode().getDividerPositions();
                                parent = wrap(sibling, getContainer(sibling).createInfo().getIndex());
                                // we created a new component in place of a removed one, so, now it is
                                // necessary to update all UUID collections in all minimized TabDocks.
                                updateUuidInPositions(parentUuid, parent.getDescriptor().getUuid());
                            }
                        }
                        logger.debug("{} Original parent SplitSpace not available; nearest living ancestor {} is used",
                                getDescriptor().getLogPrefix(), parent.getDescriptor().getFullName());
                        break;
                    }
                }
            }
            // attempt 2 - use the root as parent
            if (parent == null) {
                parent = getComposer().getRoot();
                logger.debug("{} Original parent SplitSpace not available; root is used",
                        getDescriptor().getLogPrefix());
            }
            index = resolveNewIndex(parent, side);
            sideShouldBeChecked = true;
        }

        if (logger.isDebugEnabled()) {
            parent.logState("Before restoring");
            logger.debug("{} Minimized position: {}", getDescriptor().getLogPrefix(), position);
        }

        var splitPane = parent.getNode();
        var dockSize = position.getWidth();
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            dockSize = position.getHeight();
        }
        addTabDock(grandParentPositions, parent, dock, index, side, sideShouldBeChecked, dockSize);
    }

    Dimension2D getCenterDimension() {
        return new Dimension2D(this.centerStackPane.getWidth(), this.centerStackPane.getHeight());
    }

    private int resolveNewIndex(SplitSpaceFxView<?> parent, Side side) {
        int index = parent.getChildren().size();
        if (side == LEFT || side == TOP) {
            var mainIndex = indexOfMain(parent);
            if (mainIndex >= 0 && index >= mainIndex) {
                index = mainIndex;
            }
        }
        return index;
    }

    private boolean checkNewSide(SplitSpaceFxView<?> parent, int index, Side side) {
        var tempIndex = index;
        boolean isLastPosition = false;
        if (parent.getChildren().size() - 1 < tempIndex) {
            tempIndex--;
            isLastPosition = true;
        }
        var child = (AreaFxView<?>) parent.getChildren().get(tempIndex);

        Side resolvedSide;
        if (child == getComposer().getMain()) {
            SplitSpaceFxView<?> parentSplitSpace = (SplitSpaceFxView<?>) child.getParent();
            if (parentSplitSpace.getPresenter().getOrientation() == Orientation.HORIZONTAL) {
                resolvedSide = LEFT;
                if (isLastPosition) {
                    resolvedSide = RIGHT;
                }
            } else {
                resolvedSide = TOP;
                if (isLastPosition) {
                    resolvedSide = BOTTOM;
                }
            }
        } else {
            resolvedSide = resolveSide(child);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} If tabDock is added into {} at {} its side will be {}, when {} is required",
                    getDescriptor().getLogPrefix(), parent.getDescriptor().getFullName(), index, resolvedSide, side);
        }
        return resolvedSide == side;
    }

    private AreaFxView<?> findSibling(SplitSpaceFxView<?> splitSpace, List<UUID> siblingUuids, Side side) {
        var siblingsByUuid = splitSpace.getChildren()
                .stream()
                .map(v -> (AreaFxView<?>) v)
                .collect(Collectors.toMap(v -> v.getPresenter().getDescriptor().getUuid(), v -> v));
        boolean mainFound = false;
        AreaFxView<?> sibling = null;
        for (var uuid : siblingUuids) {
            sibling = siblingsByUuid.get(uuid);
            if (sibling != null) {
                if (sibling == getComposer().getMain()) {
                    mainFound = true;
                } else if (resolveSide(sibling) == side) {
                    break;
                }
            }
        }
        if (sibling == null && mainFound) {
            return getComposer().getMain();
        }
        return sibling;
    }

    private void handleMouseDragOverOnTabHeaderArea(MousePosition mousePosition, TabDockContainer tabDockContainer) {
        hideIndicator();
        this.dockInfo = createTabAreaDockInfo(mousePosition, tabDockContainer);
        showIndicator();
    }

    private void processDropInsideTabHeaderArea() {
        // in this case moving the tab from one component to another
        // is handled by TabPanePro and TabHostFxView handlers.
        updateDragInProgress(false, DraggableType.TAB);
        logger.debug("{} Processed drop inside TabHeaderArea", getDescriptor().getLogPrefix());
    }

    private void processDropOutsideTabHeaderArea() {
        hideIndicator();
        if (dockInfo != null && dockInfo.getComposer() != null && dockInfo.isValid()) {
            hideDragDockPopup();
            if (this.dragDock == null) {
                var tabDock = getComposer().createTabDock();
                dockInfo.getComposer().accept(dockInfo, tabDock);
                moveTab(tabDock);
            } else {
                // Here we use a placeholder to reserve the target drop position while the TabDock
                // is still attached to its original parent. This allows us to safely remove the
                // TabDock from the old location without invalidating the previously calculated DockInfo,
                // and then replace the placeholder with the actual TabDock.

                // adding placeholder to a new location
                dockInfo.getComposer().accept(dockInfo, getComposer().placeholder);

                // removing tabDock
                SplitSpaceFxView<?> oldParent =
                        (SplitSpaceFxView<?>) this.dragDock.getParent();
                var oldContainer = getContainer(dragDock);
                // it is necessary to create a new info with a new index for example,
                // if there are new children, besides the old parent should be used
                var oldInfo = oldContainer.createInfo(oldParent);
                removeTabDock(oldParent, oldInfo, true);

                // finally replacing the placeholder
                SplitSpaceFxView<?> newParent = (SplitSpaceFxView<?>) getComposer().placeholder.getParent();
                newParent.getComposer().replacePlaceholder(dockInfo.getNewInfo().getIndex(), dragDock);
                logger.debug("{} Replaced {} with {}", getDescriptor().getLogPrefix(),
                        getComposer().placeholder.getDescriptor().getFullName(),
                        dragDock.getDescriptor().getFullName());
            }
            printTreeDebugInfo();
        }
        updateDragInProgress(false, this.dragDock == null ? DraggableType.TAB : DraggableType.TAB_DOCK);
        logger.debug("{} Processed drop outside TabHeaderArea", getDescriptor().getLogPrefix());
    }

    /**
     * Creates dock info during drag-and-drop when the mouse is over the tab header area.
     *
     * @param position
     * @param tabDockContainer
     * @return
     */
    private DockInfo createTabAreaDockInfo(MousePosition position, TabDockContainer tabDockContainer) {
        DockInfo info;
        info = tryReusePreviousDockInfo(position);
        if (info != null) {
            return info;
        }
        info = new DockInfo();
        info.setMousePosition(position);
        var indicatorBounds = createTabPaneIndicatorBounds(position, tabDockContainer);
        info.setIndicatorBounds(indicatorBounds);
        SplitSpaceFxView<?> splitSpace = (SplitSpaceFxView<?>) tabDockContainer.getArea().getParent();
        SplitSpaceContainer splitSpaceContainer = (SplitSpaceContainer) splitSpace.getNode().getParent();
        info.setIndicatorInfo(splitSpaceContainer.createInfo());
        info.setValid(true);
        return info;
    }

    /**
     * The core method that determines the dock position properties during drag-and-drop when the mouse is outside
     * the tab header area.
     *
     * @param position
     * @return
     */
    private DockInfo createBaseDockInfo(MousePosition position) {
        DockInfo info;
        info = tryReusePreviousDockInfo(position);
        if (info != null) {
            return info;
        }
        info = new DockInfo();
        info.setMousePosition(position);

        AbstractEventContainer<?> eventContainer = (AbstractEventContainer<?>) position.getEventContainer();
        var eventInfo = eventContainer.createInfo();
        info.setEventInfo(eventInfo);

        var parentComponent = (SplitSpaceFxView<?>) eventInfo.getContainer().getArea().getParent();
        var parentInfo = getContainer(parentComponent).createInfo();

        SplitSpaceFxView<?> grandparentComponent = null;
        ContainerInfo grandparentInfo = null;

        SplitSpaceFxView<?> greatGrandparentComponent = null;
        ContainerInfo greatGrandparentInfo = null;

        if (parentComponent != getComposer().getRoot()) {
            grandparentComponent = (SplitSpaceFxView<?>) parentComponent.getParent();
            grandparentInfo = getContainer(grandparentComponent).createInfo();

            if (grandparentComponent != getComposer().getRoot()) {
                greatGrandparentComponent = (SplitSpaceFxView<?>) grandparentComponent.getParent();
                greatGrandparentInfo = getContainer(greatGrandparentComponent).createInfo();
            }
        }
        info.setParentInfo(parentInfo);
        info.setGrandparentInfo(grandparentInfo);
        info.setGreatGrandparentInfo(greatGrandparentInfo);

        final SplitPane splitPane = parentComponent.getNode();
        if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
            prepareDockInfoForHorizontalSpace(info);
        } else {
            prepareDockInfoForVerticalSpace(info);
        }
        validateDockInfo(info);
        return info;
    }

    private DockInfo tryReusePreviousDockInfo(MousePosition position) {
        if (this.dockInfo != null && this.dockInfo.getMousePosition().equals(position)) {
            return this.dockInfo;
        }
        return null;
    }

    private void prepareDockInfoForHorizontalSpace(final DockInfo info) {
        var eventInfo = info.getEventInfo();
        var position = info.getMousePosition();
        if (position.isEdgeMode()) {
            switch (position.getSide()) {
                case TOP:
                    prepareDockInfoForOppositeOrientationOnEdge(info, TOP);
                    break;
                case RIGHT:
                    prepareDockInfoForSameOrientationOnEdge(info, RIGHT);
                    break;
                case BOTTOM:
                    prepareDockInfoForOppositeOrientationOnEdge(info, BOTTOM);
                    break;
                case LEFT:
                    prepareDockInfoForSameOrientationOnEdge(info, LEFT);
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            info.setIndicatorInfo(info.getParentInfo());
            info.setIndicatorBounds(createHalfIndicatorBounds(position.getSide(), eventInfo));
            switch (position.getSide()) {
                case TOP:
                    prepareDockInfoForOppositeOrientationOffEdge(info, TOP);
                    break;
                case RIGHT:
                    prepareDockInfoForSameOrientationOffEdge(info, RIGHT);
                    break;
                case BOTTOM:
                    prepareDockInfoForOppositeOrientationOffEdge(info, BOTTOM);
                    break;
                case LEFT:
                    prepareDockInfoForSameOrientationOffEdge(info, LEFT);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private void prepareDockInfoForVerticalSpace(DockInfo info) {
        var eventInfo = info.getEventInfo();
        var position = info.getMousePosition();
        if (position.isEdgeMode()) {
            switch (position.getSide()) {
                case TOP:
                    prepareDockInfoForSameOrientationOnEdge(info, TOP);
                    break;
                case RIGHT:
                    prepareDockInfoForOppositeOrientationOnEdge(info, RIGHT);
                    break;
                case BOTTOM:
                    prepareDockInfoForSameOrientationOnEdge(info, BOTTOM);
                    break;
                case LEFT:
                    prepareDockInfoForOppositeOrientationOnEdge(info, LEFT);
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            info.setIndicatorInfo(info.getParentInfo());
            info.setIndicatorBounds(createHalfIndicatorBounds(position.getSide(), eventInfo));
            switch (position.getSide()) {
                case TOP:
                    prepareDockInfoForSameOrientationOffEdge(info, TOP);
                    break;
                case RIGHT:
                    prepareDockInfoForOppositeOrientationOffEdge(info, RIGHT);
                    break;
                case BOTTOM:
                    prepareDockInfoForSameOrientationOffEdge(info, BOTTOM);
                    break;
                case LEFT:
                    prepareDockInfoForOppositeOrientationOffEdge(info, LEFT);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }

    private void prepareDockInfoForSameOrientationOnEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;

        var eventInfo = info.getEventInfo();
        BiConsumer<DockInfo, TabDockFxView<?>> composer = null;
        boolean isBoundary = isFirst ? eventInfo.isFirst() : eventInfo.isLast();
        int indexDelta = isFirst ? 0 : 1;
        int siblingDelta = isFirst ? -1 : 1;
        int caseIndex;
        if (isBoundary) {
            if (info.getGrandparentInfo() == null) {
                composer = (dockInfo, newTabDock) -> {
                    addTabDock(side, eventInfo, null, info.getNewInfo(), newTabDock);
                };
                info.setNewInfo(new ContainerInfo(eventInfo.getIndex() + indexDelta, ONE_THIRD));
                info.setIndicatorInfo(info.getParentInfo());
                info.setIndicatorBounds(createThirdIndicatorBounds(side, info.getEventInfo(), info.getParentInfo()));
                caseIndex = 0;
            } else {
                if (info.getGreatGrandparentInfo() == null) {
                    Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                    composer = (dockInfo, tabDock) -> {
                        wrapAndAddTabDock(orientation, info.getGrandparentInfo(), info.getNewInfo(), tabDock);
                    };
                    info.setNewInfo(new ContainerInfo(isFirst ? 0 : 1, ONE_THIRD));
                    info.setIndicatorInfo(info.getGrandparentInfo());
                    info.setIndicatorBounds(createThirdIndicatorBounds(side, info.getEventInfo(),
                            info.getGrandparentInfo()));
                    caseIndex = 1;
                } else {
                    composer = (dockInfo, tabDock) -> {
                        addTabDock(side, info.getGrandparentInfo(), null, info.getNewInfo(), tabDock);
                    };
                    info.setNewInfo(new ContainerInfo(info.getGrandparentInfo().getIndex() + indexDelta, ONE_THIRD));
                    info.setIndicatorInfo(info.getGreatGrandparentInfo());
                    info.setIndicatorBounds(createThirdIndicatorBounds(side, info.getEventInfo(),
                            info.getGreatGrandparentInfo()));
                    caseIndex = 2;
                }
            }
        } else {
            var siblingIndex = eventInfo.getIndex() + siblingDelta;
            var siblingInfo = createSiblingInfo(info.getParentInfo(), eventInfo, siblingIndex);
            info.setIndicatorInfo(info.getParentInfo());
            setContainerFractions(side, eventInfo, siblingInfo);
            info.setIndicatorBounds(createIntermediateIndicatorBounds(side, info.getParentInfo(),
                    eventInfo, siblingInfo));
            info.setNewInfo(new ContainerInfo(eventInfo.getIndex() + indexDelta, ONE_THIRD));
            composer = (dockInfo, newTabDock) -> {
                addTabDock(side, eventInfo, siblingInfo, info.getNewInfo(), newTabDock);
            };
            caseIndex = 3;
        }
        info.setComposer(composer);
        logger.trace("{} Prepared dock info for same orientation on edge; side: {}, case: {}, info: {}",
                getDescriptor().getLogPrefix(), side, caseIndex, info);
    }

    private void prepareDockInfoForOppositeOrientationOnEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        var eventInfo = info.getEventInfo();
        BiConsumer<DockInfo, TabDockFxView<?>> composer = null;

        info.setIndicatorInfo(info.getParentInfo());
        info.setIndicatorBounds(createThirdIndicatorBounds(side, eventInfo, info.getParentInfo()));
        int caseIndex;
        if (info.getGrandparentInfo() == null) {
            caseIndex = 0;
            Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            composer = (dockInfo, tabDock) -> {
                wrapAndAddTabDock(orientation, info.getParentInfo(), info.getNewInfo(), tabDock);
            };
            info.setNewInfo(new ContainerInfo(isFirst ? 0 : 1, ONE_THIRD));
        } else {
            caseIndex = 1;
            int index = info.getParentInfo().getIndex() + (isFirst ? 0 : 1);
            composer = (dockInfo, newTabDock) -> {
                addTabDock(side, info.getParentInfo(), null, info.getNewInfo(), newTabDock);
            };
            info.setNewInfo(new ContainerInfo(index, ONE_THIRD));
        }
        info.setComposer(composer);
        logger.trace("{} Prepared dock info for opposite orientation on edge; side: {}, case: {}, info: {}",
                getDescriptor().getLogPrefix(), side, caseIndex, info);
    }

    private void prepareDockInfoForSameOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;

        BiConsumer<DockInfo, TabDockFxView<?>> consumer = (dockInfo, newTabDock) -> {
            addTabDock(info.getMousePosition().getSide(), info.getEventInfo(), null, info.getNewInfo(), newTabDock);
        };
        info.setComposer(consumer);

        int index = info.getEventInfo().getIndex() + (isFirst ? 0 : 1);
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("{} Prepared dock info for same orientation off edge; side: {}, info: {}",
                getDescriptor().getLogPrefix(), side, info);
    }

    private void prepareDockInfoForOppositeOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        BiConsumer<DockInfo, TabDockFxView<?>> wrapFactory = (dockInfo, tabDock) -> {
            wrapAndAddTabDock(orientation, info.getEventInfo(), info.getNewInfo(), tabDock);
        };
        info.setComposer(wrapFactory);

        int index = isFirst ? 0 : 1;
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("{} Prepared dock info for opposite orientation off edge; side: {}, info: {}",
                getDescriptor().getLogPrefix(), side, info);
    }

    private void validateDockInfo(DockInfo info) {
        info.setValid(true);
        if (this.dragDock != null) {
            if (info.getEventInfo().getContainer()
                    .getArea().getParent() == this.dragDock.getParent()) {
                var dragDockInfo = getContainer(this.dragDock).createInfo();
                var dragDockIndex = dragDockInfo.getIndex();
                var eventIndex = info.getEventInfo().getIndex();
                SplitPane splitPane = (SplitPane) info.getParentInfo().getContainer().getArea().getNode();
                var side = info.getMousePosition().getSide();
                var edgeMode = info.getMousePosition().isEdgeMode();

                var same = dragDockIndex == eventIndex;
                same = same && ((splitPane.getOrientation() == Orientation.HORIZONTAL && side.isVertical())
                        || (splitPane.getOrientation() == Orientation.HORIZONTAL && side.isHorizontal() && !edgeMode)
                        || (splitPane.getOrientation() == Orientation.VERTICAL && side.isHorizontal())
                        || (splitPane.getOrientation() == Orientation.VERTICAL && side.isVertical() && !edgeMode));
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

    private void hideIndicator() {
        if (dockInfo != null && dockInfo.getIndicatorInfo() != null) {
            SplitSpaceContainer container = (SplitSpaceContainer) dockInfo.getIndicatorInfo().getContainer();
            container.hideIndicator();
        }
    }

    private void showIndicator() {
        if (dockInfo != null && dockInfo.getIndicatorInfo() != null && dockInfo.isValid()) {
            SplitSpaceContainer container = (SplitSpaceContainer) dockInfo.getIndicatorInfo().getContainer();
            container.showIndicator(dockInfo.getIndicatorBounds());
        }
    }

    private void hideDragDockPopup() {
        if (this.dragDockPopup != null) {
            this.dragDockPopup.hide();
            this.dragDockPopup = null;
        }
    }

    private SplitSpaceFxView<?> wrap(AreaFxView<?> area, int index) {
        SplitSpaceFxView<?> parentComponent = null;
        Orientation currentOrientation;
        if (area != getComposer().getRoot()) {
            parentComponent = (SplitSpaceFxView<?>) area.getParent();
            currentOrientation = parentComponent.getNode().getOrientation();
        } else {
            currentOrientation = getComposer().getRoot().getNode().getOrientation();
        }
        var newOrientation = Orientation.HORIZONTAL;
        if (currentOrientation == Orientation.HORIZONTAL) {
            newOrientation = Orientation.VERTICAL;
        }
        var newSplitSpace = getComposer().createSplitSpace();
        newSplitSpace.getPresenter().setOrientation(newOrientation);

        double[] parentOldPositions = null;
        SplitPane parentSplitPane = null;

        //removing wrapped component and adding a wrapper component
        if (parentComponent == null) {
            getComposer().setRoot(newSplitSpace);
        } else {
            parentSplitPane = (SplitPane) parentComponent.getNode();
            parentOldPositions = parentSplitPane.getDividerPositions();
            parentComponent.getComposer().removeChild(index, false);
            parentComponent.getComposer().addChild(index, newSplitSpace);
        }

        //adding the wrapped component to the wrapper component
        newSplitSpace.getComposer().addChild(area);
        if (parentOldPositions != null) {
            parentSplitPane.setDividerPositions(parentOldPositions);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} Wrapped {} into {}", getDescriptor().getLogPrefix(),
                    area.getDescriptor().getFullName(), newSplitSpace.getDescriptor().getFullName());
        }

        return newSplitSpace;
    }

    private void unwrap(SplitSpaceFxView<?> splitSpace, int index) {
        double[] childPositions;
        // now it has only one child
        AreaFxView<?> child = (AreaFxView<?>) splitSpace.getChildren().get(0);

        if (splitSpace != getComposer().getRoot()) {
            SplitSpaceFxView<?> grandparentComponent = (SplitSpaceFxView<?>) splitSpace.getParent();
            List<AreaFxView<?>> otherTabDocks;
            if (child instanceof SplitSpaceFxView<?>) {
                SplitSpaceFxView<?> otherSplitSpace = (SplitSpaceFxView<?>) child;
                otherTabDocks = (List) otherSplitSpace.getChildren();
                childPositions = otherSplitSpace.getNode().getDividerPositions();
            } else {
                otherTabDocks = List.of(child);
                childPositions = new double[0];
            }
            var oldPositions = grandparentComponent.getNode().getDividerPositions();
            // removing parent
            grandparentComponent.getComposer().removeChild(index, true);
            // adding tab docks
            for (var i = 0; i < otherTabDocks.size(); i++) {
                grandparentComponent.getComposer().addChild(index + i, otherTabDocks.get(i));
            }

            // last child has parent space provider
            getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
                grandparentComponent.updateDividersOnUnwrap(index, oldPositions, childPositions);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} Unwrapped {} into {}",
                        getDescriptor().getLogPrefix(),
                        otherTabDocks.stream().map(e -> e.getDescriptor().getFullName())
                                .collect(Collectors.joining(", ")),
                        grandparentComponent.getDescriptor().getFullName());
                }
                return false;
            });

        } else {
            if (child instanceof SplitSpaceFxView<?> c) {
                getComposer().setRoot(c);
                if (logger.isDebugEnabled()) {
                logger.debug("{} Unwrapped {} and set it as a root", getDescriptor().getLogPrefix(),
                        c.getDescriptor().getFullName());
                }
            } // otherwise there is a splitSpace with one main component
        }
    }

    /**
     * The start point for removing tabDock.
     *
     * @param tabPane
     */
    private void removeTabDock(TabDockFxView<?> tabDock, boolean deinitialize) {
        TabDockContainer tabDockContainer = (TabDockContainer) tabDock.getNode().getParent();
        ContainerInfo tabDockInfo = tabDockContainer.createInfo();
        SplitSpaceFxView<?> parent = (SplitSpaceFxView<?>) tabDock.getParent();
        removeTabDock(parent, tabDockInfo, deinitialize);
    }

    private void removeTabDock(SplitSpaceFxView<?> parent, ContainerInfo tabDockInfo, boolean deinitialize) {
        var parentInfo = getContainer(parent).createInfo();
        if (parent.getChildren().size() == 2) {
            removeTabDockAndUnwrap(parentInfo, tabDockInfo, deinitialize);
        } else {
            removeTabDock(parentInfo, tabDockInfo, deinitialize);
        }
    }

    private TabDockFxView<?> wrapAndAddTabDock(Orientation newOrientation, ContainerInfo anchorInfo,
            ContainerInfo newInfo, TabDockFxView<?> newTabDock) {
        newTabDock.setDockHost(this);
        var newSplitSpace = wrap(anchorInfo.getContainer().getArea(), anchorInfo.getIndex());
        newSplitSpace.getComposer().addChild(newInfo.getIndex(), newTabDock);

        if (newInfo.getFraction() == ONE_THIRD) {
            var splitPane = newSplitSpace.getNode();
            if (newOrientation == Orientation.HORIZONTAL) {
                var pos = this.dockInfo.getIndicatorBounds().getWidth() / anchorInfo.getContainer().getWidth();
                if (newInfo.getIndex() == 0) {
                    splitPane.setDividerPositions(pos);
                } else {
                    splitPane.setDividerPositions(1 - pos);
                }
            } else {
                var pos = this.dockInfo.getIndicatorBounds().getHeight() / anchorInfo.getContainer().getHeight();
                if (newInfo.getIndex() == 0) {
                    splitPane.setDividerPositions(pos);
                } else {
                    splitPane.setDividerPositions(1 - pos);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} Added {} to {}", getDescriptor().getLogPrefix(),
                    newTabDock.getDescriptor().getFullName(),
                    newSplitSpace.getDescriptor().getFullName());
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
    private void removeTabDockAndUnwrap(ContainerInfo parentInfo, ContainerInfo anchorInfo, boolean deinitialize) {
        // parent has only two children
        SplitSpaceFxView<?> parentComponent = (SplitSpaceFxView<?>) parentInfo.getContainer().getArea();
        // removing empty tabdock
        parentComponent.getComposer().removeChild(anchorInfo.getIndex(), deinitialize);
        if (logger.isDebugEnabled()) {
            logger.debug("{} Removed {} from {}",
                    getDescriptor().getLogPrefix(),
                    anchorInfo.getContainer().getArea().getDescriptor().getFullName(),
                    parentComponent.getDescriptor().getFullName());
        }
        unwrap((SplitSpaceFxView<?>) parentComponent, parentInfo.getIndex());
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
    private void addTabDock(Side side, ContainerInfo anchorInfo, ContainerInfo siblingInfo,
            ContainerInfo newInfo, TabDockFxView<?> newTabDock) {
        newTabDock.setDockHost(this);
        var parentComponent = (SplitSpaceFxView<?>) anchorInfo.getContainer().getArea().getParent();
        var splitPane = parentComponent.getNode();
        double[] oldPositions = splitPane.getDividerPositions();
        parentComponent.getComposer().addChild(newInfo.getIndex(), newTabDock);
        if (siblingInfo == null) {
            if (newInfo.getFraction() == ONE_HALF) {
                parentComponent.updateDividersOnHalfSplit(anchorInfo.getIndex(), oldPositions);
            } else if (newInfo.getFraction() == ONE_THIRD) {
                parentComponent.updateDividersOnThirdSplit(anchorInfo.getIndex(), oldPositions, side);
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
            parentComponent.updateDividersOnInsertBetween(newInfo.getIndex(), beforeProportion,
                    afterProportion, oldPositions);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} Added {} into {}", getDescriptor().getLogPrefix(),
                newTabDock.getDescriptor().getFullName(), parentComponent.getDescriptor().getFullName());
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
    private void addTabDock(double[] grandParentPositions, SplitSpaceFxView<?> parent, TabDockFxView<?> dock,
            int index, Side side, boolean sideShouldBeChecked, double size) {
        dock.setDockHost(this);
        if (sideShouldBeChecked && !checkNewSide(parent, index, side)) {
            boolean wrapParent = false;
            if (parent != getComposer().getRoot()) {
                parent = getComposer().getRoot();
                index = resolveNewIndex(parent, side);
                if (!checkNewSide(parent, index, side)) {
                    wrapParent = true;
                }
            } else {
                wrapParent = true;
            }
            if (wrapParent) {
                parent = wrap(parent, getContainer(parent).createInfo().getIndex());
                index = resolveNewIndex(parent, side);
            }
        }

        final var splitPane = parent.getNode();
        var oldSplitPaneSize = splitPane.getWidth();
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            oldSplitPaneSize = splitPane.getHeight();
        }
        final var dividerSize = parent.computeDividerSize();

        var oldPositions = parent.getNode().getDividerPositions();
        parent.getComposer().addChild(index, dock);

        final var finalOldSplitPaneSize = oldSplitPaneSize;
        final var finalIndex = index;
        final var finalParent = parent;
        getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
            var divSize = dividerSize;
            if (divSize < 0) {
                divSize = finalParent.computeDividerSize();
            }
            if (finalParent.getParent() != null && grandParentPositions != null) {
                ((SplitSpaceFxView<?>) finalParent.getParent()).getNode().setDividerPositions(grandParentPositions);
            }
            var mainIndex = indexOfMain(finalParent);
            if (mainIndex >= 0) {
                finalParent.updateDividersOnAddWithMain(finalOldSplitPaneSize, oldPositions, divSize, mainIndex,
                        finalIndex, size);
            } else {
                finalParent.updateDividersOnAddWithoutMain(finalOldSplitPaneSize, oldPositions, divSize,
                        finalIndex, size);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} Added {} into {}", getDescriptor().getLogPrefix(),
                        dock.getDescriptor().getFullName(),
                        dock.getParent().getDescriptor().getFullName());
                printTreeDebugInfo();
            }
            return false;
        });
    }

    /**
     * Important: This function cannot retrieve the parent from tabDockInfo, as it may return incorrect results when
     * the tabDock has been added to a new parent.
     *
     * @param tabDockInfo
     */
    private void removeTabDock(ContainerInfo parent, ContainerInfo tabDockInfo, boolean deinitialize) {
        var tabDockContainer = tabDockInfo.getContainer();
        AreaFxView<?> componentToRemove = tabDockContainer.getArea();
        SplitSpaceFxView<?> splitSpace = (SplitSpaceFxView<?>) parent.getContainer().getArea();
        var splitPane = splitSpace.getNode();
        var oldPositions = splitPane.getDividerPositions();
        var oldSplitPaneSize = splitPane.getWidth();
        var removedChildSize = tabDockInfo.getContainer().getArea().getNode().getWidth();
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            oldSplitPaneSize = splitPane.getHeight();
            removedChildSize = tabDockInfo.getContainer().getArea().getNode().getHeight();
        }

        splitSpace.getComposer().removeChild(tabDockInfo.getIndex(), deinitialize);
        final var dividerSize = splitSpace.computeDividerSize();

        final var finalOldSplitSpaneSize = oldSplitPaneSize;
        getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
            var divSize = dividerSize;
            if (divSize < 0) {
                divSize = splitSpace.computeDividerSize();
            }
            var mainChildIndex = indexOfMain(splitSpace);
            if (mainChildIndex != -1) {
                splitSpace.updateDividersOnRemoveWithMain(finalOldSplitSpaneSize, oldPositions, divSize,
                        mainChildIndex, tabDockInfo.getIndex());
            } else {
                splitSpace.updateDividersOnRemoveWithoutMain(finalOldSplitSpaneSize, oldPositions, divSize,
                        tabDockInfo.getIndex());
            }
            return false;
        });
        if (logger.isDebugEnabled()) {
            logger.debug("{} Removed {} from {}", getDescriptor().getLogPrefix(),
                    componentToRemove.getDescriptor().getFullName(),
                    parent.getContainer().getArea().getDescriptor().getFullName());
        }
    }

    private void moveTab(TabDockFxView<?> newTabDock) {
        TabPaneProSkin skin = (TabPaneProSkin) this.dragTab.getTabPane().getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = skin.getTabHeaderArea();
        // we don't know if it is a tab dock
        TabFxView<?> tabFxView = (TabFxView<?>) FxViewUtils.getView(this.dragTab);
        TabHostFxView<?> oldTabHost = (TabHostFxView<?>) tabFxView.getParent();
        oldTabHost.getComposer().removeTab(tabFxView);
        newTabDock.getComposer().addTab(tabFxView);
        this.dragTab = null;
        tabHeaderArea.cleanupAfterDrop();
    }

    private void updateDragInProgress(boolean value, DraggableType type) {
        if (this.dragInProgress == value) {
            return;
        }
        this.dragInProgress = value;
        var iterator = getComposer().getRoot().depthFirstIterator();
        while (iterator.hasNext()) {
            ChildFxView<?> child = (ChildFxView<?>) iterator.next();
            if (child instanceof AreaFxView<?> area && hasContainer(area)) {
                AbstractContainer<?> container = getContainer(area);
                container.updateDragInProgress(value, type);
            }
        }
    }

    private void clearOnDrop() {
        this.dockInfo = null;
        this.dragTab = null;
        this.dragDock = null;
        if (this.dragDockPopup != null) {
            this.dragDockPopup.hide();
        }
        this.dragDockPopup = null;
    }

    private void printTreeDebugInfo() {
        if (logger.isDebugEnabled()) {
            logger.debug("{} Component tree: {}", getDescriptor().getLogPrefix(), getTreeDebugInfo());
        }
    }

    private String getTreeDebugInfo() {
        StringBuilder builder = new StringBuilder();
        var iterator = depthFirstIterator();
        while (iterator.hasNext()) {
            ChildFxView<?> view = (ChildFxView<?>) iterator.next();
            String orientation = "";
            String uuid = view.getDescriptor().getShortUuid();
            if (view instanceof SplitSpaceFxView<?> spaceV) {
                orientation = spaceV.getNode().getOrientation().name().toLowerCase();
            }
            builder.append("\n");
            builder.append("    ".repeat(iterator.getDepth()));
            builder.append(view.getDescriptor().getName().getText());
            builder.append(" [");
            if (uuid != null) {
                builder.append("shortUuid: ");
                builder.append(uuid);
            }
            if (orientation.length() > 0) {
                builder.append(", orientation: ");
                builder.append(orientation);
            }
            builder.append("]");
        }
        return builder.toString();
    }

    /**
     * Resolves the side of the tabDock or splitSpace. The resolved side will be one of four values, though
     * there is no top side bar. This method can't be used if the specified component is the main one.
     *
     * @param view
     * @return
     */
    private Side resolveSide(AreaFxView<?> view) {
        var composer = getComposer();
        if (view == composer.getMain()) {
            throw new IllegalArgumentException("Can't resolve the side of the main component");
        }
        var componentPath = findPathFromRoot(view);
        var mainPath = findPathFromRoot(composer.getMain());
        // we must find Lowest Common Ancestor
        SplitSpaceFxView<?> lca = composer.getRoot();

        var componentIterator = componentPath.iterator();
        var mainIterator = mainPath.iterator();
        AreaFxView<?> componentAncestor = null;
        AreaFxView<?> mainAncestor = null;
        while (componentIterator.hasNext() && mainIterator.hasNext()) {
            componentAncestor = componentIterator.next();
            mainAncestor = mainIterator.next();
            if (mainAncestor == componentAncestor) {
                lca = (SplitSpaceFxView<?>) mainAncestor;
            } else {
                break;
            }
        }

        Side result = null;
        var componentAncestorIndex = lca.getChildren().indexOf(componentAncestor);
        var mainAncestorIndex = lca.getChildren().indexOf(mainAncestor);
        if (componentAncestorIndex < mainAncestorIndex) {
            if (lca.getNode().getOrientation() == Orientation.HORIZONTAL) {
                result = LEFT;
            } else {
                result = TOP;
            }
        } else {
            if (lca.getNode().getOrientation() == Orientation.HORIZONTAL) {
                result = RIGHT;
            } else {
                result = BOTTOM;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} Resolved side for {} is {}; lowest common ancestor: {}", getDescriptor().getLogPrefix(),
                    view.getDescriptor().getFullName(), result, lca.getDescriptor().getFullName());
        }
        return result;
    }

    /**
     * Returns the path to the root including startNode.
     *
     * @param startNode
     * @return
     */
    private List<AreaFxView<?>> findPathFromRoot(AreaFxView<?> startNode) {
        var result = new ArrayList<AreaFxView<?>>();
        var current = startNode;
        while (current != null && current != this) {
            result.add(0, current);
            current = (AreaFxView<?>) current.getParent();
        }
        if (logger.isTraceEnabled()) {
            var nodes = result.stream().map(v -> v.getDescriptor().getFullName()).collect(Collectors.joining(", "));
            logger.trace("{} {} path to root: {}", getDescriptor().getLogPrefix(),
                    startNode.getDescriptor().getFullName(), nodes);
        }
        return result;
    }

    /**
     * Finds the index of the child component of the specified {@link SplitSpaceFxView} that contains the main
     * component in its hierarchy.
     *
     * @param splitSpace the SplitSpaceView to search in
     * @return the index of the component or -1.
     */
    private int indexOfMain(SplitSpaceFxView<?> splitSpaceView) {
        var deque = findPathFromRoot(getComposer().getMain());
        var set = new HashSet<AreaFxView<?>>(deque);
        for (var i = 0; i < splitSpaceView.getChildren().size(); i++) {
            var child = splitSpaceView.getChildren().get(i);
            if (set.contains(child)) {
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
        updateUuidInPositions(getComposer().getRightBar(), oldUuid, newUuid);
        updateUuidInPositions(getComposer().getLeftBar(), oldUuid, newUuid);
        updateUuidInPositions(getComposer().getBottomBar(), oldUuid, newUuid);
        logger.debug("{} Replaced UUID {} with {} in position lists of all minimized TabDocks",
                getDescriptor().getLogPrefix(), oldUuid, newUuid);
    }

    private void updateUuidInPositions(SideBarFxView<?> sideBar, UUID oldUuid, UUID newUuid) {
        if (sideBar != null) {
            for (var tabDock : sideBar.getComposer().getTabDocks()) {
                var position = tabDock.getPresenter().getMinimizedPosition();
                position.updateUuid(oldUuid, newUuid);
            }
        }
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
}
