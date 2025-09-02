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

package com.techsenger.tabshell.layout.docktab;

import com.techsenger.mvvm4fx.core.ChildView;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin.TabHeaderArea;
import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.core.tab.ComponentTab;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.core.ObjectUtils;
import com.techsenger.toolkit.core.Pair;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import static javafx.geometry.Side.BOTTOM;
import static javafx.geometry.Side.LEFT;
import static javafx.geometry.Side.RIGHT;
import static javafx.geometry.Side.TOP;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
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
 * <p>There are three types of components: MainComponent — contains the main component, Workspace — contains a
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
 * <p>When a new tab is docked, the operation may require wrapping an existing node with a new Workspace. The target of
 * this wrapping can be the TabDock itself, its parent Workspace, or its grandparent Workspace. The decision is made
 * dynamically based on the values of side and edgeMode together with the orientation of the parent Workspace,
 * ensuring that the system adapts correctly whether docking occurs inside a node, along its edge, or at the boundary
 * of a larger layout.
 *
 * <p>All possible cases:
 * | #  | Workspace   | EdgeMode | Side       | Node Location | Action                                              |
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
public class DockLayoutView<T extends DockLayoutViewModel> extends AbstractPaneView<T> {

    private static final Logger logger = LoggerFactory.getLogger(DockLayoutView.class);

    private static final Double EDGE_THRESHOLD = 20.0;

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
     */
    private abstract static class AbstractContainer<T extends AbstractPaneView<?>> extends StackPane {

        private final DockLayoutView<?> layout;

        private final T component;

        AbstractContainer(DockLayoutView<?> layout, T component) {
            this.layout = layout;
            this.component = component;
            getChildren().add(component.getNode());
        }

        DockLayoutView<?> getLayout() {
            return layout;
        }

        T getComponent() {
            return component;
        }

        abstract void updateDragInProgress(boolean value, DraggableType type);

        ContainerInfo createInfo() {
            ContainerInfo result;
            if (component != layout.getRoot()) {
                result = createInfo((WorkspaceView<?>) component.getParent());
            } else {
                result = createInfo(null);
            }
            return result;
        }

        ContainerInfo createInfo(WorkspaceView<?> parent) {
            if (parent != null) {
                var parentSplitPane = parent.getNode();
                var containerIndex = parentSplitPane.getItems().indexOf(this);
                return new ContainerInfo(this, containerIndex);
            } else {
                return new ContainerInfo(this, -1);
            }
        }

        @Override
        public String toString() {
            return "AbstractContainer [" + "dockTab:" + ObjectUtils.getIdentity(layout)
                    + ", component:" + ObjectUtils.getIdentity(component) + ']';
        }
    }

    private static class WorkspaceContainer extends AbstractContainer<WorkspaceView<?>> {

        private final Rectangle indicator = createIndicator();

        WorkspaceContainer(DockLayoutView<?> layout, WorkspaceView<?> component) {
            super(layout, component);
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
    private abstract static class AbstractEventContainer<T extends AbstractPaneView<?>>  extends AbstractContainer<T> {

        private final Pane eventPane = new Pane();

        AbstractEventContainer(DockLayoutView<?> layout, T component) {
            super(layout, component);
            eventPane.setMouseTransparent(false);
            // eventPane.setStyle("-fx-background-color: yellow");
            eventPane.setOnMouseDragOver(e -> getLayout().handleMouseDragOverOnContainer(provideMousePosition(e)));
            eventPane.setOnMouseDragExited(e -> getLayout().handleMouseDragExitedOnContainer(provideMousePosition(e)));
            eventPane.setOnMouseDragReleased(e -> getLayout()
                    .handleMouseDragReleasedOnContainer(provideMousePosition(e)));
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

    private static class MainContainer extends AbstractEventContainer<AbstractPaneView<?>> {

        MainContainer(DockLayoutView<?> layout, AbstractPaneView<?> component) {
            super(layout, component);
        }

    }

    private static class TabDockContainer extends AbstractEventContainer<TabDockView<?>> {

        TabDockContainer(DockLayoutView<?> layout, TabDockView<?> component) {
            super(layout, component);
        }

        @Override
        Insets provideEventPanePadding() {
            TabPanePro tabPane = getComponent().getNode();
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
            var splitPane = ((WorkspaceView<?>) container.getComponent().getParent()).getNode();
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
         * Creates and returns a new TabDock.
         */
        private Function<DockInfo, TabDockView<?>> factory;

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

        public Function<DockInfo, TabDockView<?>> getFactory() {
            return factory;
        }

        public void setFactory(Function<DockInfo, TabDockView<?>> factory) {
            this.factory = factory;
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
                    + ", factory:" + factory + ", valid:" + valid + ']';
        }
    }

    private static final double ONE_THIRD = 1.0 / 3.0;

    private static final double TWO_THIRDS = 2.0 / 3.0;

    private static final double ONE_HALF = 1.0 / 2.0;

    /**
     * Returns the existing container node for a component that is currently on the scene.
     * @param component
     * @return
     */
    private static AbstractContainer<?> getContainer(AbstractPaneView<?> component) {
        return (AbstractContainer<?>) component.getNode().getParent();
    }

    private static TabDockContainer getContainer(TabDockView<?> component) {
        return (TabDockContainer) component.getNode().getParent();
    }

    private static WorkspaceContainer getContainer(WorkspaceView<?> component) {
        return (WorkspaceContainer) component.getNode().getParent();
    }

    private static SpaceReceiver getSpaceReceiver(AbstractContainer<?> container) {
        var component = container.getComponent();
        if (component instanceof WorkspaceView<?> c) {
            return c.getViewModel().getSpaceReceiver();
        } else if (component instanceof TabDockView<?> c) {
            return c.getViewModel().getSpaceReceiver();
        } else {
            return null; // for main component for debug info
        }
    }

    private static void setSpaceReceiver(AbstractContainer<?> container, SpaceReceiver receiver) {
        var component = container.getComponent();
        if (component instanceof WorkspaceView<?> c) {
            c.getViewModel().setSpaceReceiver(receiver);
        } else if (component instanceof TabDockView<?> c) {
             c.getViewModel().setSpaceReceiver(receiver);
        }
    }

    private static Bounds createTabPaneIndicatorBounds(MousePosition position, TabDockContainer tabDockContainer) {
        var eventContainer = position.getEventContainer();
        var eventSceneXY = getSceneXY(eventContainer);
        TabPaneProSkin skin = (TabPaneProSkin) tabDockContainer.getComponent().getNode().getSkin();
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
        WorkspaceContainer workspaceContainer = (WorkspaceContainer) parentInfo.getContainer();
        var splitPane = workspaceContainer.getComponent().getNode();
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

    private static void updateHalfDividersOnAdd(SplitPane splitPane, int anchorContainerIndex,
            double[] oldPositions) {
        int newDividerCount = splitPane.getDividers().size();
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = 0.5;
        } else {
            double leftBound = (anchorContainerIndex == 0) ? 0.0 : oldPositions[anchorContainerIndex - 1];
            double rightBound =
                    (anchorContainerIndex == oldPositions.length) ? 1.0 : oldPositions[anchorContainerIndex];
            double middle = (leftBound + rightBound) / 2;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < anchorContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == anchorContainerIndex) {
                    newPositions[i] = middle;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        splitPane.setDividerPositions(newPositions);
        logger.debug("Updated half dividers on add; oldPositions: {}, newPositions: {}", oldPositions, newPositions);
    }

    public static void updateThirdDividersOnAdd(SplitPane splitPane, int anchorContainerIndex, double[] oldPositions,
            Side side) {
        double firstFraction = 1 - ONE_THIRD;
        double secondFraction = ONE_THIRD;
        if (side == Side.TOP || side == LEFT) {
            firstFraction = ONE_THIRD;
            secondFraction = 1 - ONE_THIRD;
        }
        int newDividerCount = splitPane.getDividers().size();
        double[] newPositions = new double[newDividerCount];

        if (oldPositions.length == 0) {
            newPositions[0] = firstFraction;
        } else {
            double leftBound = (anchorContainerIndex == 0) ? 0.0 : oldPositions[anchorContainerIndex - 1];
            double rightBound =
                    (anchorContainerIndex == oldPositions.length) ? 1.0 : oldPositions[anchorContainerIndex];
            double firstPart = leftBound + (rightBound - leftBound) * firstFraction;
            for (int i = 0; i < newDividerCount; i++) {
                if (i < anchorContainerIndex) {
                    newPositions[i] = oldPositions[i];
                } else if (i == anchorContainerIndex) {
                    newPositions[i] = firstPart;
                } else {
                    newPositions[i] = oldPositions[i - 1];
                }
            }
        }
        splitPane.setDividerPositions(newPositions);
        logger.debug("Updated third dividers on add; oldPositions: {}, newPositions: {}", oldPositions, newPositions);
    }

    /**
     * Updates divider positions with custom proportions from neighbors.
     *
     * @param splitPane the SplitPane containing the containers
     * @param newContainerIndex the index where new container was inserted
     * @param beforeProportion proportion taken from the container before insertion point
     * @param afterProportion proportion taken from the container after insertion point
     * @param oldPositions divider positions before the insertion
     */
    private static void updateIntermediateDividersOnAdd(SplitPane splitPane, int newContainerIndex,
            double beforeProportion, double afterProportion, double[] oldPositions) {
        int oldCount = oldPositions.length + 1;
        double[] oldSizes = new double[oldCount];
        oldSizes[0] = (oldPositions.length > 0) ? oldPositions[0] : 1.0;
        for (int i = 1; i < oldPositions.length; i++) {
            oldSizes[i] = oldPositions[i] - oldPositions[i - 1];
        }
        if (oldPositions.length > 0) {
            oldSizes[oldSizes.length - 1] = 1.0 - oldPositions[oldPositions.length - 1];
        }

        int left = newContainerIndex - 1;
        int right = newContainerIndex;
        double leftWidth = oldSizes[left];
        double rightWidth = oldSizes[right];

        double newWidth = (leftWidth + rightWidth) * ONE_THIRD;
        double takenFromLeft = newWidth * beforeProportion;
        double takenFromRight = newWidth * afterProportion;

        double[] newSizes = new double[oldSizes.length + 1];
        int j = 0;
        for (int i = 0; i < newSizes.length; i++) {
            if (i == newContainerIndex) {
                newSizes[i] = newWidth;
            } else if (i == left) {
                newSizes[i] = oldSizes[j++] - takenFromLeft;
            } else if (i == right + 1) {
                newSizes[i] = oldSizes[j++] - takenFromRight;
            } else {
                newSizes[i] = oldSizes[j++];
            }
        }

        // adjust newSizes so that the sum is 1.0
        double total = 0;
        for (double s : newSizes) {
            total += s;
        }
        for (int i = 0; i < newSizes.length; i++) {
            newSizes[i] /= total;
        }

        // convert to divider positions
        double[] newPositions = new double[newSizes.length - 1];
        double sum = 0;
        for (int i = 0; i < newPositions.length; i++) {
            sum += newSizes[i];
            newPositions[i] = sum;
        }
        splitPane.setDividerPositions(newPositions);
        logger.debug("Updated intermediate dividers on add; oldPositions: {}, newPositions: {}", oldPositions,
                newPositions);
    }

    /**
     * Updates divider positions after removing a dock.
     *
     * @param splitPane        SplitPane where the removal occurs
     * @param oldPositions     divider positions before removal (length N-1, for N docks before removal)
     * @param removedIndex     index of the removed container (0-based)
     * @param spaceReceiver    strategy for redistributing freed space
     */
    public static void updateDividersOnRemove(SplitPane splitPane, double[] oldPositions,
            int removedIndex, SpaceReceiver spaceReceiver) {
        int oldCount = oldPositions.length + 1;
        int newCount = oldCount - 1;

        // Calculate initial dock sizes from divider positions
        double[] sizes = new double[oldCount];
        if (oldPositions.length > 0) {
            sizes[0] = oldPositions[0];
        } else {
            sizes[0] = 1.0;
        }
        for (int i = 1; i < oldCount - 1; i++) {
            sizes[i] = oldPositions[i] - oldPositions[i - 1];
        }
        if (oldPositions.length > 0) {
            sizes[oldCount - 1] = 1.0 - oldPositions[oldCount - 2];
        }

        double removedSize = sizes[removedIndex];

        // Redistribute freed space
        if (spaceReceiver == SpaceReceiver.PREVIOUS) {
            if (removedIndex > 0) {
                sizes[removedIndex - 1] += removedSize;
            }
        } else if (spaceReceiver == SpaceReceiver.NEXT) {
            if (removedIndex < sizes.length - 1) {
                sizes[removedIndex + 1] += removedSize;
            }
        } else if (spaceReceiver == SpaceReceiver.BOTH) {
            int left = removedIndex - 1;
            int right = removedIndex + 1;
            double total = 0.0;
            if (left >= 0) {
                total += sizes[left];
            }
            if (right < sizes.length) {
                total += sizes[right];
            }
            if (total > 0.0) {
                if (left >= 0) {
                    sizes[left] += removedSize * (sizes[left] / total);
                }
                if (right < sizes.length) {
                    sizes[right] += removedSize * (sizes[right] / total);
                }
            }
        }

        // Build new sizes array (skip removed dock)
        double[] newSizes = new double[newCount];
        int idx = 0;
        for (int i = 0; i < sizes.length; i++) {
            if (i != removedIndex) {
                newSizes[idx] = sizes[i];
                idx++;
            }
        }

        // Calculate new divider positions (cumulative sum)
        double[] newPositions = new double[newCount - 1];
        double acc = 0.0;
        for (int i = 0; i < newPositions.length; i++) {
            acc += newSizes[i];
            newPositions[i] = acc;
        }
        splitPane.setDividerPositions(newPositions);
        logger.debug("Updated dividers on remove; spaceReceiver: {}, oldPositions: {}, newPositions: {}", spaceReceiver,
                oldPositions, newPositions);
    }

    /**
     * Updates divider positions for a parent SplitPane after unwrapping a child SplitPane.
     *
     * @param splitPane The SplitPane whose divider positions need to be updated.
     * @param oldPositions The divider positions (from 0 to 1) of the parent SplitPane BEFORE unwrapping.
     * @param unwrapIndex The index in the parent SplitPane where the unwrapped SplitPane was located
     * @param childPositions The divider positions (from 0 to 1) of the removed child SplitPane.
     */
    public static void updateDividersOnUnwrap(SplitPane splitPane, double[] oldPositions,
            int unwrapIndex, double[] childPositions) {
        double[] newPositions;

        if (childPositions == null || childPositions.length == 0) {
            // If childPositions is empty, the child SplitPane had only one dock.
            // After unwrap, the number of children in parent stays the same.
            // If parent has 2 children, divider should be kept.
            // If parent has 1 child, no divider.
            if (oldPositions.length == 1) {
                newPositions = new double[] {oldPositions[0]};
            } else {
                newPositions = new double[0];
            }
        } else {
            // Number of new dividers: oldPositions.length + childPositions.length
            newPositions = new double[oldPositions.length + childPositions.length];

            int pos = 0;
            // Copy old dividers before unwrapIndex
            for (int i = 0; i < unwrapIndex; i++) {
                newPositions[pos++] = oldPositions[i];
            }

            // Calculate bounds for childPositions
            double left = unwrapIndex == 0 ? 0.0 : oldPositions[unwrapIndex - 1];
            double right = unwrapIndex == oldPositions.length ? 1.0 : oldPositions[unwrapIndex];

            // Insert mapped child dividers
            for (double p : childPositions) {
                newPositions[pos++] = left + (right - left) * p;
            }

            // Copy old dividers after unwrapIndex
            for (int i = unwrapIndex; i < oldPositions.length; i++) {
                newPositions[pos++] = oldPositions[i];
            }
        }
        splitPane.setDividerPositions(newPositions);
        logger.debug("Updated dividers on unwrap; oldPositions: {}, childPositions: {}, newPositions: {}",
                oldPositions, childPositions, newPositions);
    }

    private static ContainerInfo createSiblingInfo(ContainerInfo parentInfo,
            ContainerInfo anchorInfo, int siblingIndex) {
        SplitPane splitPane = (SplitPane) parentInfo.getContainer().getComponent().getNode();
        var siblingContainer = ((AbstractContainer<?>) splitPane.getItems().get(siblingIndex));
        var singlingInfo = siblingContainer.createInfo();
        return singlingInfo;
    }

    private final BorderPane borderPane = new BorderPane();

    private final DragAndDropContext dragAndDropContext = new DragAndDropContext();

    private final ObjectProperty<WorkspaceView<?>> root = new SimpleObjectProperty<>();

    private ComponentTab dragTab;

    private TabDockView<?> dragDock;

    private Popup dragDockPopup;

    private DockInfo dockInfo;

    private boolean dragInProgress;

    private final ObjectProperty<AbstractPaneView<?>> main  = new SimpleObjectProperty<>();

    public DockLayoutView(T viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public BorderPane getNode() {
        return this.borderPane;
    }

    public final ObjectProperty<WorkspaceView<?>> rootProperty() {
        return root;
    }

    public final WorkspaceView<?> getRoot() {
        return root.get();
    }

    public final void setRoot(WorkspaceView root) {
        this.root.set(root);
    }

    public WorkspaceView<?> createWorkspace() {
        var viewModel = getViewModel().createWorkspace();
        var view = new WorkspaceView<WorkspaceViewModel>(this, viewModel);
        view.initialize();
        return view;
    }

    public TabDockView<?> createTabDock() {
        var tabDockViewModel = getViewModel().createTabDock();
        var tabDockView = new TabDockView<TabDockViewModel>(this, tabDockViewModel);
        tabDockView.initialize();
        return tabDockView;
    }

    public SideBarView<?> createSideBar(Side side) {
        var barViewModel = getViewModel().createSideBar(side);
        var barView = new SideBarView<SideBarViewModel>(barViewModel);
        barView.initialize();
        return barView;
    }

    public DragAndDropContext getDragAndDropContext() {
        return dragAndDropContext;
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        this.borderPane.getStyleClass().add("dock-tab");
        var css = DockLayoutView.class.getResource("docktab.css").toExternalForm();
        this.borderPane.getStylesheets().add(css);
        VBox.setVgrow(borderPane, Priority.ALWAYS);

        var sideBar = createSideBar(RIGHT);
        this.borderPane.setRight(sideBar.getNode());

        sideBar = createSideBar(BOTTOM);
        this.borderPane.setBottom(sideBar.getNode());

        sideBar = createSideBar(LEFT);
        this.borderPane.setLeft(sideBar.getNode());
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        this.root.addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                borderPane.setCenter(null);
            }
            if (newV != null) {
                WorkspaceContainer container = new WorkspaceContainer(this, newV);
                VBox.setVgrow(container, Priority.ALWAYS);
                borderPane.setCenter(container);
            }
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
        var tabParams = new SnapshotParameters();
        WritableImage tabImage = tabHeaderArea.snapshot(tabParams, null);
        ImageView dragView = new ImageView(tabImage);
        var container = new VBox(dragView);
        //container.getStyleClass().add("tab-drag-content");
        return container;
    }

//    protected Side resolveTabDockSide(TabDockView<?> tabDock) {
//        var iterator = breadthFirstIterator();
//        while (iterator.hasNext()) {
//            ChildView<?> component = (ChildView<?>) iterator.next();
//            c
//        }
//    }

    protected Deque<AbstractPaneView<?>> pathToRoot(AbstractPaneView<?> node) {
        var result = new LinkedList<AbstractPaneView<?>>();
        var current = node;
        while (current != null) {
            result.addFirst(current);
            if (current != getRoot()) {
                current = (AbstractPaneView<?>) current.getParent();
            } else {
                current = null;
            }
        }
        return result;
    }

    Node createContainerFor(AbstractPaneView<?> child) {
        AbstractContainer container;
        if (child instanceof WorkspaceView<?>) {
            var workspace = (WorkspaceView<?>) child;
            container = new WorkspaceContainer(this, workspace);
            SplitPane.setResizableWithParent(container, false);
        } else if (child instanceof TabDockView<?>) {
            var tabDock = (TabDockView<?>) child;
            container = new TabDockContainer(this, tabDock);
            SplitPane.setResizableWithParent(container, false);
        } else {
            container = new MainContainer(this, child);
        }
        return container;
    }

    void processEmptyTabPane(TabPanePro tabPane) {
        removeTabDock(tabPane);
        printTreeDebugInfo();
        logger.debug("Removed empty TabDock");
    }

    void handleDragDetectedOnTab(ComponentTab tab) {
        this.dragTab = tab;
        updateDragInProgress(true, DraggableType.TAB);
    }

    void handleDragOnTab(ComponentTab tab) {
        this.dragTab = tab;
        updateDragInProgress(true, DraggableType.TAB);
    }

    void handleDropOnTab(ComponentTab tab) {
        try {
            processDropInsideTabHeaderArea();
        } finally {
            clearOnDrop();
        }
    }

    void handleMouseDragOverOnTabHeaderArea(TabPanePro tabPane, MouseDragEvent e) {
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
    void handleDragDetectedOnDock(TabDockView<?> dock, FontIconView iconView, MouseEvent e) {
        this.dragDock = dock;
        TabPaneProSkin sourceSkin = (TabPaneProSkin) dock.getNode().getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = sourceSkin.getTabHeaderArea();

        var content = createTabDockDragContent(tabHeaderArea);
        this.dragDockPopup = new Popup();
        this.dragDockPopup.setAutoHide(false);
        this.dragDockPopup.getContent().add(content);
        this.dragDockPopup.show(getScene().getWindow(), e.getScreenX(), e.getScreenY());
        var scene = getScene();
        scene.setCursor(Cursor.CLOSED_HAND);

        iconView.startFullDrag();
        updateDragInProgress(true, DraggableType.TAB_DOCK);
    }

    void handleMouseDraggedOnDock(TabDockView<?> dock, FontIconView iconView, MouseEvent e) {
        if (this.dragDockPopup != null) {
            this.dragDockPopup.setAnchorX(e.getScreenX());
            this.dragDockPopup.setAnchorY(e.getScreenY());
            e.consume();
        }
    }

    void handleMouseReleasedOnDock(TabDockView<?> dock, FontIconView iconView, MouseEvent e) {
        hideDragDockPopup();
        updateDragInProgress(false, DraggableType.TAB_DOCK);
    }

    void handleMouseDragExitedOnContainer(MousePosition mousePosition) {
        hideIndicator();
    }

    void handleMouseDragOverOnContainer(MousePosition mousePosition) {
        hideIndicator();
        this.dockInfo = createBaseDockInfo(mousePosition);
        showIndicator();
    }

    /**
     * This handler is called when the mouse is not over TabHeaderArea.
     *
     * @param mousePosition
     */
    void handleMouseDragReleasedOnContainer(MousePosition mousePosition) {
        try {
            processDropOutsideTabHeaderArea();
        } finally {
            clearOnDrop();
        }
    }

    void minimizeTabDock(TabDockView<?> dock) {

    }

    private void handleMouseDragOverOnTabHeaderArea(MousePosition mousePosition, TabDockContainer tabDockContainer) {
        hideIndicator();
        this.dockInfo = createTabAreaDockInfo(mousePosition, tabDockContainer);
        showIndicator();
    }

    private void processDropInsideTabHeaderArea() {
        updateDragInProgress(false, DraggableType.TAB);
        logger.debug("Processed drop inside TabHeaderArea");
    }

    private void processDropOutsideTabHeaderArea() {
        hideIndicator();
        if (dockInfo != null && dockInfo.getFactory() != null && dockInfo.isValid()) {
            hideDragDockPopup();
            if (this.dragDock == null) {
                var tabDock = dockInfo.getFactory().apply(dockInfo);
                moveTab(tabDock);
            } else {
                // Here we use a small hack: first we add the TabDock to the new location, and only then remove it
                // from the old one. The reason is that if we remove the TabDock first, the created DockInfo will no
                // longer be actual, and it will therefore be very difficult to understand what the user intended.

                // saving TabDock old parent and old container
                WorkspaceView<?> oldParent = (WorkspaceView<?>) this.dragDock.getParent();
                var oldContainer = getContainer(dragDock);
                // adding TabDock to a new location
                dockInfo.getFactory().apply(dockInfo);
                // saving the new parent, because it will be null after removal
                WorkspaceView<?> newParent = (WorkspaceView<?>) this.dragDock.getParent();
                printTreeDebugInfo();
                // it is necessary to create a new info with a new index for example,
                // if there are new children, besides the old parent should be used
                var oldInfo = oldContainer.createInfo(oldParent);
                // removing
                removeTabDock(oldParent, oldInfo);
                // finally setting the new parent
                this.dragDock.setParent(newParent);
            }
            printTreeDebugInfo();
        }
        updateDragInProgress(false, this.dragDock == null ? DraggableType.TAB : DraggableType.TAB_DOCK);
        logger.debug("Processed drop outside TabHeaderArea");
    }

    /**
     * Creates dock info during drag-and-drop when the mouse is over the tab header area.
     *
     * @param position
     * @param tabDockContainer
     * @return
     */
    private DockInfo createTabAreaDockInfo(MousePosition position, TabDockContainer tabDockContainer) {
        DockInfo info = new DockInfo();
        info.setMousePosition(position);
        var indicatorBounds = createTabPaneIndicatorBounds(position, tabDockContainer);
        info.setIndicatorBounds(indicatorBounds);
        WorkspaceView<?> workspace = (WorkspaceView<?>) tabDockContainer.getComponent().getParent();
        WorkspaceContainer workspaceContainer = (WorkspaceContainer) workspace.getNode().getParent();
        info.setIndicatorInfo(workspaceContainer.createInfo());
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
        DockInfo info = new DockInfo();
        info.setMousePosition(position);

        AbstractEventContainer<?> eventContainer = (AbstractEventContainer<?>) position.getEventContainer();
        var eventInfo = eventContainer.createInfo();
        info.setEventInfo(eventInfo);

        WorkspaceView<?> parentComponent = (WorkspaceView<?>) eventInfo.getContainer().getComponent().getParent();
        var parentInfo = getContainer(parentComponent).createInfo();

        WorkspaceView<?> grandparentComponent = null;
        ContainerInfo grandparentInfo = null;

        WorkspaceView<?> greatGrandparentComponent = null;
        ContainerInfo greatGrandparentInfo = null;

        if (parentComponent != getRoot()) {
            grandparentComponent = (WorkspaceView<?>) parentComponent.getParent();
            grandparentInfo = getContainer(grandparentComponent).createInfo();

            if (grandparentComponent != getRoot()) {
                greatGrandparentComponent = (WorkspaceView<?>) grandparentComponent.getParent();
                greatGrandparentInfo = getContainer(greatGrandparentComponent).createInfo();
            }
        }
        info.setParentInfo(parentInfo);
        info.setGrandparentInfo(grandparentInfo);
        info.setGreatGrandparentInfo(greatGrandparentInfo);

        final SplitPane splitPane = parentComponent.getNode();
        if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
            prepareDockInfoForHorizontalWorkspace(info);
        } else {
            prepareDockInfoForVerticalWorkspace(info);
        }
        validateDockInfo(info);
        return info;
    }

    private void prepareDockInfoForHorizontalWorkspace(final DockInfo info) {
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

    private void prepareDockInfoForVerticalWorkspace(DockInfo info) {
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
        Function<DockInfo, TabDockView<?>> factory = null;
        boolean isBoundary = isFirst ? eventInfo.isFirst() : eventInfo.isLast();
        int indexDelta = isFirst ? 0 : 1;
        int siblingDelta = isFirst ? -1 : 1;
        int caseIndex;
        if (isBoundary) {
            if (info.getGrandparentInfo() == null) {
                factory = dockInfo -> {
                    var newTabDock = provideTabDockOnDrop();
                    addTabDock(side, eventInfo, null, info.getNewInfo(), newTabDock);
                    return newTabDock;
                };
                info.setNewInfo(new ContainerInfo(eventInfo.getIndex() + indexDelta, ONE_THIRD));
                info.setIndicatorInfo(info.getParentInfo());
                info.setIndicatorBounds(createThirdIndicatorBounds(side, info.getEventInfo(), info.getParentInfo()));
                caseIndex = 0;
            } else {
                if (info.getGreatGrandparentInfo() == null) {
                    Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                    factory = dockInfo -> {
                        var tabDock = provideTabDockOnDrop();
                        wrapAndAddTabDock(orientation, info.getGrandparentInfo(), info.getNewInfo(), tabDock);
                        return tabDock;
                    };
                    info.setNewInfo(new ContainerInfo(isFirst ? 0 : 1, ONE_THIRD));
                    info.setIndicatorInfo(info.getGrandparentInfo());
                    info.setIndicatorBounds(createThirdIndicatorBounds(side, info.getEventInfo(),
                            info.getGrandparentInfo()));
                    caseIndex = 1;
                } else {
                    factory = dockInfo -> {
                        var tabDock = provideTabDockOnDrop();
                        addTabDock(side, info.getGrandparentInfo(), null, info.getNewInfo(), tabDock);
                        return tabDock;
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
            factory = dockInfo -> {
                var newTabDock = provideTabDockOnDrop();
                addTabDock(side, eventInfo, siblingInfo, info.getNewInfo(), newTabDock);
                return newTabDock;
            };
            caseIndex = 3;
        }
        info.setFactory(factory);
        logger.trace("Prepared dock info for same orientation on edge; side: {}, case: {}, info: {}", side,
                caseIndex, info);
    }

    private void prepareDockInfoForOppositeOrientationOnEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        var eventInfo = info.getEventInfo();
        Function<DockInfo, TabDockView<?>> factory = null;

        info.setIndicatorInfo(info.getParentInfo());
        info.setIndicatorBounds(createThirdIndicatorBounds(side, eventInfo, info.getParentInfo()));
        int caseIndex;
        if (info.getGrandparentInfo() == null) {
            caseIndex = 0;
            Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
            factory = dockInfo -> {
                var tabDock = provideTabDockOnDrop();
                wrapAndAddTabDock(orientation, info.getParentInfo(), info.getNewInfo(), tabDock);
                return tabDock;
            };
            info.setNewInfo(new ContainerInfo(isFirst ? 0 : 1, ONE_THIRD));
        } else {
            caseIndex = 1;
            int index = info.getParentInfo().getIndex() + (isFirst ? 0 : 1);
            factory = dockInfo -> {
                var newTabDock = provideTabDockOnDrop();
                addTabDock(side, info.getParentInfo(), null, info.getNewInfo(), newTabDock);
                return newTabDock;
            };
            info.setNewInfo(new ContainerInfo(index, ONE_THIRD));
        }
        info.setFactory(factory);
        logger.trace("Prepared dock info for opposite orientation on edge; side: {}, case: {}, info: {}", side,
                caseIndex, info);
    }

    private void prepareDockInfoForSameOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;

        Function<DockInfo, TabDockView<?>> addFactory = dockInfo -> {
            var newTabDock = provideTabDockOnDrop();
            addTabDock(info.getMousePosition().getSide(), info.getEventInfo(), null, info.getNewInfo(), newTabDock);
            return newTabDock;
        };
        info.setFactory(addFactory);

        int index = info.getEventInfo().getIndex() + (isFirst ? 0 : 1);
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("Prepared dock info for same orientation off edge; side: {}, info: {}", side, info);
    }

    private void prepareDockInfoForOppositeOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        Function<DockInfo, TabDockView<?>> wrapFactory = dockInfo -> {
            var tabDock = provideTabDockOnDrop();
            wrapAndAddTabDock(orientation, info.getEventInfo(), info.getNewInfo(), tabDock);
            return tabDock;
        };
        info.setFactory(wrapFactory);

        int index = isFirst ? 0 : 1;
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("Prepared dock info for opposite orientation off edge; side: {}, info: {}", side, info);
    }

    private void validateDockInfo(DockInfo info) {
        info.setValid(true);
        if (this.dragDock != null) {
            if (info.getEventInfo().getContainer().getComponent().getParent() == this.dragDock.getParent()) {
                var dragDockInfo = getContainer(this.dragDock).createInfo();
                var dragDockIndex = dragDockInfo.getIndex();
                var eventIndex = info.getEventInfo().getIndex();
                SplitPane splitPane = (SplitPane) info.getParentInfo().getContainer().getComponent().getNode();
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

    private TabDockView<?> provideTabDockOnDrop() {
        return this.dragDock == null ? createTabDock() : this.dragDock;
    }

    private void hideIndicator() {
        if (dockInfo != null && dockInfo.getIndicatorInfo() != null) {
            WorkspaceContainer container = (WorkspaceContainer) dockInfo.getIndicatorInfo().getContainer();
            container.hideIndicator();
        }
    }

    private void showIndicator() {
        if (dockInfo != null && dockInfo.getIndicatorInfo() != null && dockInfo.isValid()) {
            WorkspaceContainer container = (WorkspaceContainer) dockInfo.getIndicatorInfo().getContainer();
            container.showIndicator(dockInfo.getIndicatorBounds());
        }
    }

    private void hideDragDockPopup() {
        if (this.dragDockPopup != null) {
            this.dragDockPopup.hide();
            this.dragDockPopup = null;
        }
    }

    /**
     * The start point for removing tabDock.
     *
     * @param tabPane
     */
    private void removeTabDock(TabPanePro tabPane) {
        TabDockContainer tabDockContainer = (TabDockContainer) tabPane.getParent();
        ContainerInfo tabDockInfo = tabDockContainer.createInfo();
        WorkspaceView<?> parent = (WorkspaceView<?>) tabDockContainer.getComponent().getParent();
        removeTabDock(parent, tabDockInfo);
    }

    private void removeTabDock(WorkspaceView<?> parent, ContainerInfo tabDockInfo) {
        var parentInfo = getContainer(parent).createInfo();
        if (parent.getChildren().size() == 2) {
            removeTabDockAndUnwrap(parentInfo, tabDockInfo);
        } else {
            removeTabDock(parentInfo, tabDockInfo);
        }
    }

    private TabDockView<?> wrapAndAddTabDock(Orientation newOrientation, ContainerInfo anchorInfo,
            ContainerInfo newInfo, TabDockView<?> newTabDock) {
        var newWorkspace = createWorkspace();

        newWorkspace.getNode().setOrientation(newOrientation);
        var anchorContainerIndex = anchorInfo.getIndex();
        var acnhorSpaceReceiver = getSpaceReceiver(anchorInfo.getContainer()); // saving value

        double[] parentOldPositions = null;
        SplitPane parentSplitPane = null;

        var anchorComponent = anchorInfo.getContainer().getComponent();
        WorkspaceView<?> parentComponent = null;
        if (anchorComponent != getRoot()) {
            parentComponent = (WorkspaceView<?>) anchorComponent.getParent();
        }

        //removing wrapped component and adding a wrapper component
        if (parentComponent == null) {
            setRoot(newWorkspace);
        } else {
            parentSplitPane = (SplitPane) parentComponent.getNode();
            parentOldPositions = parentSplitPane.getDividerPositions();
            parentComponent.getChildren().remove(anchorContainerIndex);
            parentComponent.getChildren().add(anchorContainerIndex, newWorkspace);
        }

        AbstractContainer<?> newWorkspaceContainer = getContainer(newWorkspace);
        // the new workspace container will inherit the space provider of the wrapped container
        setSpaceReceiver(newWorkspaceContainer, acnhorSpaceReceiver);

        //adding the wrapped component to the wrapper component
        newWorkspace.getChildren().add(anchorInfo.getContainer().getComponent());
        newWorkspace.getChildren().add(newInfo.getIndex(), newTabDock);
        if (parentOldPositions != null) {
            parentSplitPane.setDividerPositions(parentOldPositions);
        }
        AbstractContainer<?> firstContainer = (AbstractContainer<?>) newWorkspace.getNode().getItems().get(0);
        AbstractContainer<?> secondContainer = (AbstractContainer<?>) newWorkspace.getNode().getItems().get(1);
        setSpaceReceiver(firstContainer, SpaceReceiver.NEXT);
        setSpaceReceiver(secondContainer, SpaceReceiver.PREVIOUS);

        if (newInfo.getFraction() == ONE_THIRD) {
            var splitPane = newWorkspace.getNode();
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
            logger.debug("Wrapped {} into {} and added {}",
                ObjectUtils.getIdentity(anchorInfo.getContainer().getComponent()),
                ObjectUtils.getIdentity(newWorkspace),
                ObjectUtils.getIdentity(newTabDock));
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
    private void removeTabDockAndUnwrap(ContainerInfo parentInfo, ContainerInfo anchorInfo) {
        // parent has only two children
        var parentComponent = parentInfo.getContainer().getComponent();
        // removing empty tabdock
        parentComponent.getChildren().remove(anchorInfo.getIndex());
        double[] childPositions;
        // now parent has only one child
        AbstractPaneView<?> otherChild = (AbstractPaneView<?>) parentComponent.getChildren().get(0);
        if (parentComponent != getRoot()) {
            WorkspaceView<?> grandparentComponent = (WorkspaceView<?>) parentComponent.getParent();
            List<ChildView<?>> otherTabDocks;
            if (otherChild instanceof WorkspaceView<?>) {
                WorkspaceView<?> otherWorkspace = (WorkspaceView<?>) otherChild;
                otherTabDocks = otherWorkspace.getChildren();
                childPositions = otherWorkspace.getNode().getDividerPositions();
            } else {
                otherTabDocks = List.of(otherChild);
                childPositions = new double[0];
            }
            var oldPositions = grandparentComponent.getNode().getDividerPositions();
            // removing parent
            grandparentComponent.getChildren().remove(parentInfo.getIndex());
            var parentSpaceReceiver = getSpaceReceiver(parentInfo.getContainer());
            // adding tab docks
            grandparentComponent.getChildren().addAll(parentInfo.getIndex(), otherTabDocks);
            // last child has parent space provider
            var lastOtherChild = (AbstractPaneView<?>) otherTabDocks.get(otherTabDocks.size() - 1);
            setSpaceReceiver(getContainer(lastOtherChild), parentSpaceReceiver);
            updateDividersOnUnwrap(grandparentComponent.getNode(), oldPositions, parentInfo.getIndex(), childPositions);
            if (logger.isDebugEnabled()) {
                logger.debug("Removed {} and unwrapped {} into {}",
                    ObjectUtils.getIdentity(anchorInfo.getContainer().getComponent()),
                    otherTabDocks.stream().map(e -> ObjectUtils.getIdentity(e)).collect(Collectors.joining(", ")),
                    ObjectUtils.getIdentity(grandparentComponent));
            }
        } else {
            if (otherChild instanceof WorkspaceView) {
                setRoot((WorkspaceView<?>) otherChild);
                if (logger.isDebugEnabled()) {
                logger.debug("Removed {} and set {} as a root",
                        ObjectUtils.getIdentity(anchorInfo.getContainer().getComponent()),
                        ObjectUtils.getIdentity(otherChild));
                }
            } // otherwise there is a workspace with one main component
        }
    }

    private void addTabDock(Side side, ContainerInfo anchorInfo, ContainerInfo siblingInfo,
            ContainerInfo newInfo, TabDockView<?> newTabDock) {
        WorkspaceView<?> parentComponent = (WorkspaceView<?>) anchorInfo.getContainer().getComponent().getParent();
        var splitPane = parentComponent.getNode();
        double[] oldPositions = splitPane.getDividerPositions();
        parentComponent.getChildren().add(newInfo.getIndex(), newTabDock);
        var newTabDockContainer = getContainer(newTabDock);
        if (siblingInfo == null) {
            if (newInfo.getFraction() == ONE_HALF) {
                updateHalfDividersOnAdd(splitPane, anchorInfo.getIndex(), oldPositions);
            } else if (newInfo.getFraction() == ONE_THIRD) {
                updateThirdDividersOnAdd(splitPane, anchorInfo.getIndex(), oldPositions, side);
            } else {
                throw new AssertionError();
            }
            if (side == TOP || side == LEFT) {
                setSpaceReceiver(newTabDockContainer, SpaceReceiver.NEXT);
            } else {
                setSpaceReceiver(newTabDockContainer, SpaceReceiver.PREVIOUS);
            }
        } else {
            setSpaceReceiver(newTabDockContainer, SpaceReceiver.BOTH);
            double beforeProportion;
            double afterProportion;
            if (side == RIGHT || side == BOTTOM) {
                beforeProportion = anchorInfo.getFraction();
                afterProportion = siblingInfo.getFraction();
            } else {
                afterProportion = anchorInfo.getFraction();
                beforeProportion = siblingInfo.getFraction();
            }
            updateIntermediateDividersOnAdd(splitPane, newInfo.getIndex(), beforeProportion, afterProportion,
                    oldPositions);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Added {} into {}",
                ObjectUtils.getIdentity(newTabDock), ObjectUtils.getIdentity(parentComponent));
        }
    }

    /**
     * Important: This function cannot retrieve the parent from tabDockInfo, as it may return incorrect results when
     * the tabDock has been added to a new parent.
     *
     * @param tabDockInfo
     */
    private void removeTabDock(ContainerInfo parent, ContainerInfo tabDockInfo) {
        var tabDockContainer = tabDockInfo.getContainer();
        AbstractPaneView<?> componentToRemove = tabDockContainer.getComponent();
        WorkspaceView<?> workspace = (WorkspaceView<?>) parent.getContainer().getComponent();
        var splitPane = workspace.getNode();
        var oldPositions = splitPane.getDividerPositions();
        workspace.getChildren().remove(tabDockInfo.getIndex());
        updateDividersOnRemove(workspace.getNode(), oldPositions, tabDockInfo.getIndex(),
                getSpaceReceiver(tabDockContainer));
        if (logger.isDebugEnabled()) {
            logger.debug("Removed {} from {}", ObjectUtils.getIdentity(componentToRemove),
                    ObjectUtils.getIdentity(parent));
        }
    }

    private void moveTab(TabDockView<?> newTabDock) {
        TabPaneProSkin skin = (TabPaneProSkin) this.dragTab.getTabPane().getSkin();
        TabPaneProSkin.TabHeaderArea tabHeaderArea = skin.getTabHeaderArea();
        this.dragTab.getTabPane().getTabs().remove(this.dragTab);
        newTabDock.getNode().getTabs().add(this.dragTab);
        this.dragTab = null;
        tabHeaderArea.cleanupAfterDrop();
    }

    private void updateDragInProgress(boolean value, DraggableType type) {
        if (this.dragInProgress == value) {
            return;
        }
        this.dragInProgress = value;
        var iterator = getRoot().depthFirstIterator();
        while (iterator.hasNext()) {
            AbstractPaneView<?> child = (AbstractPaneView<?>) iterator.next();
            AbstractContainer container = getContainer(child);
            container.updateDragInProgress(value, type);
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
            logger.debug("Tab: '{}' dock component tree: {}", getViewModel().getKey(), getTreeDebugInfo());
        }
    }

    private String getTreeDebugInfo() {
        StringBuilder builder = new StringBuilder();
        var iterator = getRoot().depthFirstIterator();
        while (iterator.hasNext()) {
            var component = iterator.next();
            var container = getContainer((AbstractPaneView<?>) component);
            String orientation = "";
            if (component instanceof WorkspaceView<?>) {
                orientation = ((WorkspaceView<?>) component).getNode().getOrientation().name().toLowerCase();
            }
            builder.append("\n");
            builder.append("    ".repeat(iterator.getDepth()));
            builder.append(ObjectUtils.getIdentity(component));
            builder.append(" (");
            if (orientation.length() > 0) {
                builder.append("orientation: ");
                builder.append(orientation);
                builder.append(", ");
            }
            builder.append("spaceReceiver: ");
            var spaceReceiver = getSpaceReceiver(container);
            builder.append(spaceReceiver != null ? spaceReceiver.toString().toLowerCase() : null);
            builder.append(")");
        }
        return builder.toString();
    }
}
