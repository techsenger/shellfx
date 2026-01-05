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

package com.techsenger.tabshell.layout.dock;

import com.techsenger.patternfx.mvvmx.ChildComponent;
import com.techsenger.patternfx.mvvmx.ChildView;
import com.techsenger.tabpanepro.core.TabPanePro;
import com.techsenger.tabpanepro.core.skin.DragAndDropContext;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin;
import com.techsenger.tabpanepro.core.skin.TabPaneProSkin.TabHeaderArea;
import com.techsenger.tabshell.core.area.AbstractAreaView;
import com.techsenger.tabshell.core.area.AreaComponent;
import com.techsenger.tabshell.core.area.AreaView;
import com.techsenger.tabshell.core.tab.ComponentTab;
import static com.techsenger.tabshell.layout.dock.DockConstants.ONE_HALF;
import static com.techsenger.tabshell.layout.dock.DockConstants.ONE_THIRD;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.toolkit.core.Pair;
import com.techsenger.toolkit.fx.pulse.LayoutPhase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
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
public class DockLayoutView<T extends DockLayoutViewModel<?>, S extends DockLayoutComponent<?>>
        extends AbstractAreaView<T, S> {

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
     *
     * <p>Containers are created when components are added to the {@link SplitSpaceView} and destroyed when they are
     * removed from there.
     */
    private abstract static class AbstractContainer<T extends AreaView<?, ?>> extends StackPane {

        private final DockLayoutView<?, ?> layout;

        private final T area;

        AbstractContainer(DockLayoutView<?, ?> layout, T area) {
            this.layout = layout;
            this.area = area;
            getChildren().add(area.getNode());
        }

        DockLayoutView<?, ?> getLayout() {
            return layout;
        }

        T getArea() {
            return area;
        }

        abstract void updateDragInProgress(boolean value, DraggableType type);

        ContainerInfo createInfo() {
            ContainerInfo result;
            if (area != layout.getComponent().getRoot().getView()) {
                result = createInfo((SplitSpaceView<?, ?>) area.getComponent().getParent().getView());
            } else {
                result = createInfo(null);
            }
            return result;
        }

        ContainerInfo createInfo(SplitSpaceView<?, ?> parent) {
            if (parent != null) {
                var parentSplitPane = parent.getNode();
                var containerIndex = parentSplitPane.getItems().indexOf(this);
                return new ContainerInfo(this, containerIndex);
            } else {
                return new ContainerInfo(this, -1);
            }
        }
    }

    private static class SplitSpaceContainer extends AbstractContainer<SplitSpaceView<?, ?>> {

        private final Rectangle indicator = createIndicator();

        SplitSpaceContainer(DockLayoutView<?, ?> layout, SplitSpaceView<?, ?> view) {
            super(layout, view);
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
    private abstract static class AbstractEventContainer<T extends AreaView<?, ?>>
            extends AbstractContainer<T> {

        private final Pane eventPane = new Pane();

        AbstractEventContainer(DockLayoutView<?, ?> layout, T area) {
            super(layout, area);
            eventPane.setMouseTransparent(false);
            // eventPane.setStyle("-fx-background-color: yellow");
            eventPane.setOnMouseDragOver(e -> getLayout().handleContainerMouseDragOver(provideMousePosition(e)));
            eventPane.setOnMouseDragExited(e -> getLayout().handleContainerMouseDragExited(provideMousePosition(e)));
            eventPane.setOnMouseDragReleased(e -> getLayout()
                    .handleContainerMouseDragReleased(provideMousePosition(e)));
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

    private static class MainContainer extends AbstractEventContainer<AreaView<?, ?>> {

        MainContainer(DockLayoutView<?, ?> layout, AreaView<?, ?> area) {
            super(layout, area);
        }

    }

    private static class TabDockContainer extends AbstractEventContainer<TabDockView<?, ?>> {

        TabDockContainer(DockLayoutView<?, ?> layout, TabDockView<?, ?> tabDock) {
            super(layout, tabDock);
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
            var splitPane = ((SplitSpaceView<?, ?>) container.getArea().getComponent().getParent().getView()).getNode();
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
        private BiConsumer<DockInfo, TabDockView<?, ?>> composer;

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

        public BiConsumer<DockInfo, TabDockView<?, ?>> getComposer() {
            return composer;
        }

        public void setComposer(BiConsumer<DockInfo, TabDockView<?, ?>> composer) {
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
    private static AbstractContainer<?> getContainer(AreaView<?, ?> view) {
        return (AbstractContainer<?>) view.getNode().getParent();
    }

    private static TabDockContainer getContainer(TabDockView<?, ?> view) {
        return (TabDockContainer) view.getNode().getParent();
    }

    private static SplitSpaceContainer getContainer(SplitSpaceView<?, ?> view) {
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

    /**
     * Contains the root {@link SplitSpaceView} and the {@link TabPopupView}.
     */
    private final StackPane centerStackPane = new StackPane();

    /**
     * Contains the {@link DockLayoutView#centerStackPane} and three {@link SideBarView}.
     */
    private final BorderPane node = new BorderPane();

    private final DragAndDropContext dragAndDropContext = new DragAndDropContext();

    private ComponentTab dragTab;

    private TabDockView<?, ?> dragDock;

    private Popup dragDockPopup;

    private DockInfo dockInfo;

    private boolean dragInProgress;

    public DockLayoutView(T viewModel) {
        super(viewModel);
    }

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

    void addTabDock(TabDockView<?, ?> dock, Side side, double size) {
        var index = resolveNewIndex(getRoot(), side);
        addTabDock(null, getRoot(), dock, index, side, true, size);
    }

    @Override
    protected void build() {
        super.build();
        VBox.setVgrow(node, Priority.ALWAYS);

        this.node.getStyleClass().add("dock-layout");
        var css = DockLayoutView.class.getResource("dock-layout.css").toExternalForm();
        this.node.getStylesheets().add(css);
        this.node.setCenter(centerStackPane);
        centerStackPane.setMinSize(0, 0);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getComponent().rootProperty().addListener((ov, oldV, newV) -> {
            if (oldV != null) {
                centerStackPane.getChildren().remove(0);
            }
            if (newV != null) {
                SplitSpaceContainer container = new SplitSpaceContainer(this, newV.getView());
                centerStackPane.getChildren().add(0, container); // there can be tab popup
            }
        });
        centerStackPane.widthProperty().addListener((ov2, oldV2, newV2) -> {
            var w = centerStackPane.getWidth();
            var h = centerStackPane.getHeight();
            updatePopupSize(getComponent().getRightSideBar(), w, h);
            updatePopupSize(getComponent().getBottomSideBar(), w, h);
            updatePopupSize(getComponent().getLeftSideBar(), w, h);
        });
        centerStackPane.heightProperty().addListener((ov2, oldV2, newV2) -> {
            var w = centerStackPane.getWidth();
            var h = centerStackPane.getHeight();
            updatePopupSize(getComponent().getRightSideBar(), w, h);
            updatePopupSize(getComponent().getBottomSideBar(), w, h);
            updatePopupSize(getComponent().getLeftSideBar(), w, h);
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

    StackPane createContainer(AreaView<?, ?> child) {
        AbstractContainer container;
        if (child instanceof SplitSpaceView<?, ?>) {
            var splitSpace = (SplitSpaceView<?, ?>) child;
            container = new SplitSpaceContainer(this, splitSpace);
            SplitPane.setResizableWithParent(container, false);
        } else if (child instanceof TabDockView<?, ?>) {
            var tabDock = (TabDockView<?, ?>) child;
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

    void processEmptyTabPane(TabPanePro tabPane) {
        removeTabDock(tabPane);
        printTreeDebugInfo();
        logger.debug("{} Removed empty TabDock", getComponent().getLogPrefix());
    }

    void handleTabDragDetected(ComponentTab tab) {
        this.dragTab = tab;
        updateDragInProgress(true, DraggableType.TAB);
    }

    void handleTabDrag(ComponentTab tab) {
        this.dragTab = tab;
        updateDragInProgress(true, DraggableType.TAB);
    }

    void handleTabDrop(ComponentTab tab) {
        try {
            processDropInsideTabHeaderArea();
        } finally {
            clearOnDrop();
        }
    }

    void handleTabHeaderAreaMouseDragOver(TabPanePro tabPane, MouseDragEvent e) {
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
    void handleDockDragDetected(TabDockView<?, ?> dock, FontIconView iconView, MouseEvent e) {
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

    void handleDockMouseDragged(TabDockView<?, ?> dock, FontIconView iconView, MouseEvent e) {
        if (this.dragDockPopup != null) {
            this.dragDockPopup.setAnchorX(e.getScreenX());
            this.dragDockPopup.setAnchorY(e.getScreenY());
            e.consume();
        }
    }

    void handleDockMouseReleased(TabDockView<?, ?> dock, FontIconView iconView, MouseEvent e) {
        hideDragDockPopup();
        updateDragInProgress(false, DraggableType.TAB_DOCK);
    }

    void handleContainerMouseDragExited(MousePosition mousePosition) {
        hideIndicator();
    }

    void handleContainerMouseDragOver(MousePosition mousePosition) {
        hideIndicator();
        this.dockInfo = createBaseDockInfo(mousePosition);
        showIndicator();
    }

    /**
     * This handler is called when the mouse is not over TabHeaderArea.
     *
     * @param mousePosition
     */
    void handleContainerMouseDragReleased(MousePosition mousePosition) {
        try {
            processDropOutsideTabHeaderArea();
        } finally {
            clearOnDrop();
        }
    }

    void minimizeTabDock(TabDockView<?, ?> dock) {
        // saving current position
        var side = resolveSide(dock);
        SplitSpaceComponent<?> parent = (SplitSpaceComponent<?>) dock.getComponent().getParent();
        var parentPos = parent.getView().getNode().getDividerPositions();
        var index = parent.getChildren().indexOf(dock.getComponent());
        var siblings = parent.getChildren().stream().filter(c -> c != dock.getComponent())
                .map(c -> c.getUuid()).collect(Collectors.toList());
        var pathFromRoot = findPathFromRoot(dock)
                .stream()
                .map(v -> v.getComponent().getUuid())
                .collect(Collectors.toList());
        var pos = new ComponentPosition(pathFromRoot, siblings, parent.getView().getNode().getOrientation(), side,
                dock.getComponent().getUuid(), index, dock.getNode().getWidth(), dock.getNode().getHeight());
        pos.buildMaps();
        dock.getViewModel().setMinimizedPosition(pos);
        if (logger.isDebugEnabled()) {
            parent.getView().logState("Before minimizing");
            logger.debug("{} Minimized position: {}", getComponent().getLogPrefix(), pos);
        }
        // removing the dock
        removeTabDock(dock.getNode());
        // createaing a sidebar if it is null
        var sideBar = getComponent().addSideBar(side);

        dock.detachTabs();
        // adding the dock to the sidebar
        sideBar.addTabDock(dock.getComponent());
        printTreeDebugInfo();
    }

    void restoreTabDock(TabDockView<?, ?> dock) {
        dock.attachTabs();

        // attempt 0 - find the parent by UUID
        var position = dock.getViewModel().getMinimizedPosition();
        var pathFromRoot = position.getPathFromRoot();
        dock.getViewModel().setMinimizedPosition(null);
        var splitSpacesByUuid = new HashMap<UUID, SplitSpaceView<?, ?>>();
        var iterator = getComponent().getRoot().breadthFirstIterator();
        while (iterator.hasNext()) {
            ChildComponent<?> component = (ChildComponent<?>) iterator.next();
            if (component instanceof SplitSpaceComponent<?> c) {
                splitSpacesByUuid.put(c.getUuid(), c.getView());
            }
        }
        var parentUuid = pathFromRoot.get(pathFromRoot.size() - 2); // excluding the node itself
        SplitSpaceView<?, ?> parent = splitSpacesByUuid.get(parentUuid);
        double[] grandParentPositions = null;

        var side = position.getSide(); // the position side can differ from the side bar side
        var sideShouldBeChecked = false;
        int index;
        if (parent != null) {
            index = position.getIndex();
            var childCount = parent.getComponent().getChildren().size();
            if (index > childCount) {
                index = childCount;
            }
            logger.debug("{} Original parent SplitSpace available", getComponent().getLogPrefix());
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
                                updateUuidInPositions(parentUuid, parent.getComponent().getUuid());
                            }
                        }
                        logger.debug("{} Original parent SplitSpace not available; nearest living ancestor {} is used",
                                getComponent().getLogPrefix(), parent.getComponent().getFullName());
                        break;
                    }
                }
            }
            // attempt 2 - use the root as parent
            if (parent == null) {
                parent = getRoot();
                logger.debug("{} Original parent SplitSpace not available; root is used",
                        getComponent().getLogPrefix());
            }
            index = resolveNewIndex(parent, side);
            sideShouldBeChecked = true;
        }

        if (logger.isDebugEnabled()) {
            parent.logState("Before restoring");
            logger.debug("{} Minimized position: {}", getComponent().getLogPrefix(), position);
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

    void addTabPopup(TabPopupView<?, ?> popup) {
        var popupNode = popup.getNode();
        this.centerStackPane.getChildren().add(popupNode);
    }

    void removeTabPopup(TabPopupView<?, ?> popup) {
        this.centerStackPane.getChildren().remove(popup.getNode());
    }

    private int resolveNewIndex(SplitSpaceView<?, ?> parent, Side side) {
        int index = parent.getComponent().getChildren().size();
        if (side == LEFT || side == TOP) {
            var mainIndex = indexOfMain(parent);
            if (mainIndex >= 0 && index >= mainIndex) {
                index = mainIndex;
            }
        }
        return index;
    }

    private boolean checkNewSide(SplitSpaceView<?, ?> parent, int index, Side side) {
        var tempIndex = index;
        boolean isLastPosition = false;
        if (parent.getComponent().getChildren().size() - 1 < tempIndex) {
            tempIndex--;
            isLastPosition = true;
        }
        var component = (AreaComponent<?>) parent.getComponent().getChildren().get(tempIndex);

        Side resolvedSide;
        if (component == getComponent().getMain()) {
            SplitSpaceView<?, ?> parentSplitSpace = (SplitSpaceView<?, ?>) component.getParent().getView();
            if (parentSplitSpace.getViewModel().getOrientation() == Orientation.HORIZONTAL) {
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
            resolvedSide = resolveSide(component.getView());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} If tabDock is added into {} at {} its side will be {}, when {} is required",
                    getComponent().getLogPrefix(), parent.getComponent().getFullName(), index, resolvedSide, side);
        }
        return resolvedSide == side;
    }

    private AreaView<?, ?> findSibling(SplitSpaceView<?, ?> splitSpace, List<UUID> siblingUuids, Side side) {
        var siblingsByUuid = splitSpace.getComponent().getChildren()
                .stream()
                .map(c -> (AreaComponent<?>) c)
                .collect(Collectors.toMap(c -> c.getUuid(), c -> c.getView()));
        boolean mainFound = false;
        AreaView<?, ?> sibling = null;
        for (var uuid : siblingUuids) {
            sibling = siblingsByUuid.get(uuid);
            if (sibling != null) {
                if (sibling == getComponent().getMain().getView()) {
                    mainFound = true;
                } else if (resolveSide(sibling) == side) {
                    break;
                }
            }
        }
        if (sibling == null && mainFound) {
            return getComponent().getMain().getView();
        }
        return sibling;
    }

    private void handleMouseDragOverOnTabHeaderArea(MousePosition mousePosition, TabDockContainer tabDockContainer) {
        hideIndicator();
        this.dockInfo = createTabAreaDockInfo(mousePosition, tabDockContainer);
        showIndicator();
    }

    private void processDropInsideTabHeaderArea() {
        updateDragInProgress(false, DraggableType.TAB);
        logger.debug("{} Processed drop inside TabHeaderArea", getComponent().getLogPrefix());
    }

    private void processDropOutsideTabHeaderArea() {
        hideIndicator();
        if (dockInfo != null && dockInfo.getComposer() != null && dockInfo.isValid()) {
            hideDragDockPopup();
            if (this.dragDock == null) {
                var tabDock = getComponent().createTabDock();
                tabDock.initialize();
                dockInfo.getComposer().accept(dockInfo, tabDock.getView());
                moveTab(tabDock.getView());
            } else {
                // Here we use a placeholder to reserve the target drop position while the TabDock
                // is still attached to its original parent. This allows us to safely remove the
                // TabDock from the old location without invalidating the previously calculated DockInfo,
                // and then replace the placeholder with the actual TabDock.

                // adding placeholder to a new location
                var placeholder = getComponent().getPlaceholder();
                dockInfo.getComposer().accept(dockInfo, placeholder.getView());

                // removing tabDock
                SplitSpaceView<?, ?> oldParent =
                        (SplitSpaceView<?, ?>) this.dragDock.getComponent().getParent().getView();
                var oldContainer = getContainer(dragDock);
                // it is necessary to create a new info with a new index for example,
                // if there are new children, besides the old parent should be used
                var oldInfo = oldContainer.createInfo(oldParent);
                removeTabDock(oldParent, oldInfo);

                // finally replacing the placeholder
                SplitSpaceComponent<?> newParent = (SplitSpaceComponent<?>) placeholder.getParent();
                newParent.replacePlaceholder(dockInfo.getNewInfo().getIndex(), dragDock.getComponent());
                logger.debug("{} Replaced {} with {}", getComponent().getLogPrefix(), placeholder.getFullName(),
                        dragDock.getComponent().getFullName());
            }
            printTreeDebugInfo();
        }
        updateDragInProgress(false, this.dragDock == null ? DraggableType.TAB : DraggableType.TAB_DOCK);
        logger.debug("{} Processed drop outside TabHeaderArea", getComponent().getLogPrefix());
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
        SplitSpaceView<?, ?> splitSpace =
                (SplitSpaceView<?, ?>) tabDockContainer.getArea().getComponent().getParent().getView();
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

        var parentComponent =
                (SplitSpaceView<?, ?>) eventInfo.getContainer().getArea().getComponent().getParent().getView();
        var parentInfo = getContainer(parentComponent).createInfo();

        SplitSpaceView<?, ?> grandparentComponent = null;
        ContainerInfo grandparentInfo = null;

        SplitSpaceView<?, ?> greatGrandparentComponent = null;
        ContainerInfo greatGrandparentInfo = null;

        if (parentComponent != getRoot()) {
            grandparentComponent = (SplitSpaceView<?, ?>) parentComponent.getComponent().getParent().getView();
            grandparentInfo = getContainer(grandparentComponent).createInfo();

            if (grandparentComponent != getRoot()) {
                greatGrandparentComponent =
                        (SplitSpaceView<?, ?>) grandparentComponent.getComponent().getParent().getView();
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
        BiConsumer<DockInfo, TabDockView<?, ?>> composer = null;
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
                getComponent().getLogPrefix(), side, caseIndex, info);
    }

    private void prepareDockInfoForOppositeOrientationOnEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        var eventInfo = info.getEventInfo();
        BiConsumer<DockInfo, TabDockView<?, ?>> composer = null;

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
                getComponent().getLogPrefix(), side, caseIndex, info);
    }

    private void prepareDockInfoForSameOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;

        BiConsumer<DockInfo, TabDockView<?, ?>> consumer = (dockInfo, newTabDock) -> {
            addTabDock(info.getMousePosition().getSide(), info.getEventInfo(), null, info.getNewInfo(), newTabDock);
        };
        info.setComposer(consumer);

        int index = info.getEventInfo().getIndex() + (isFirst ? 0 : 1);
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("{} Prepared dock info for same orientation off edge; side: {}, info: {}",
                getComponent().getLogPrefix(), side, info);
    }

    private void prepareDockInfoForOppositeOrientationOffEdge(DockInfo info, Side side) {
        boolean isFirst = side == Side.TOP || side == Side.LEFT;
        Orientation orientation = side.isVertical() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        BiConsumer<DockInfo, TabDockView<?, ?>> wrapFactory = (dockInfo, tabDock) -> {
            wrapAndAddTabDock(orientation, info.getEventInfo(), info.getNewInfo(), tabDock);
        };
        info.setComposer(wrapFactory);

        int index = isFirst ? 0 : 1;
        info.setNewInfo(new ContainerInfo(index, ONE_HALF));
        logger.trace("{} Prepared dock info for opposite orientation off edge; side: {}, info: {}",
                getComponent().getLogPrefix(), side, info);
    }

    private void validateDockInfo(DockInfo info) {
        info.setValid(true);
        if (this.dragDock != null) {
            if (info.getEventInfo().getContainer()
                    .getArea().getComponent().getParent() == this.dragDock.getComponent().getParent()) {
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

    private SplitSpaceView<?, ?> wrap(AreaView<?, ?> area, int index) {
        SplitSpaceView<?, ?> parentComponent = null;
        Orientation currentOrientation;
        if (area != getRoot()) {
            parentComponent = (SplitSpaceView<?, ?>) area.getComponent().getParent().getView();
            currentOrientation = parentComponent.getNode().getOrientation();
        } else {
            currentOrientation = getRoot().getNode().getOrientation();
        }
        var newOrientation = Orientation.HORIZONTAL;
        if (currentOrientation == Orientation.HORIZONTAL) {
            newOrientation = Orientation.VERTICAL;
        }
        var newSplitSpace = getComponent().createSplitSpace(newOrientation);
        newSplitSpace.initialize();

        double[] parentOldPositions = null;
        SplitPane parentSplitPane = null;

        //removing wrapped component and adding a wrapper component
        if (parentComponent == null) {
            getComponent().setRoot(newSplitSpace);
        } else {
            parentSplitPane = (SplitPane) parentComponent.getNode();
            parentOldPositions = parentSplitPane.getDividerPositions();
            parentComponent.getComponent().removeChild(index);
            parentComponent.getComponent().addChild(index, newSplitSpace);
        }

        //adding the wrapped component to the wrapper component
        newSplitSpace.addChild(area.getComponent());
        if (parentOldPositions != null) {
            parentSplitPane.setDividerPositions(parentOldPositions);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} Wrapped {} into {}", getComponent().getLogPrefix(),
                    area.getComponent().getFullName(), newSplitSpace.getFullName());
        }

        return newSplitSpace.getView();
    }

    private void unwrap(SplitSpaceView<?, ?> splitSpace, int index) {
        double[] childPositions;
        // now it has only one child
        AreaComponent<?> child =
                (AreaComponent<?>) splitSpace.getComponent().getChildren().get(0);

        if (splitSpace != getRoot()) {
            SplitSpaceView<?, ?> grandparentComponent =
                    (SplitSpaceView<?, ?>) splitSpace.getComponent().getParent().getView();
            List<AreaComponent<?>> otherTabDocks;
            if (child instanceof SplitSpaceView<?, ?>) {
                SplitSpaceView<?, ?> otherSplitSpace = (SplitSpaceView<?, ?>) child;
                otherTabDocks = (List) otherSplitSpace.getComponent().getChildren();
                childPositions = otherSplitSpace.getNode().getDividerPositions();
            } else {
                otherTabDocks = List.of(child);
                childPositions = new double[0];
            }
            var oldPositions = grandparentComponent.getNode().getDividerPositions();
            // removing parent
            grandparentComponent.getComponent().removeChild(index);
            // adding tab docks
            for (var i = 0; i < otherTabDocks.size(); i++) {
                grandparentComponent.getComponent().addChild(index + i, otherTabDocks.get(i));
            }

            // last child has parent space provider
            getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
                grandparentComponent.updateDividersOnUnwrap(index, oldPositions, childPositions);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} Unwrapped {} into {}",
                        getComponent().getLogPrefix(),
                        otherTabDocks.stream().map(e -> e.getFullName()).collect(Collectors.joining(", ")),
                        grandparentComponent.getComponent().getFullName());
                }
                return false;
            });

        } else {
            if (child instanceof SplitSpaceView<?, ?> c) {
                getComponent().setRoot(c.getComponent());
                if (logger.isDebugEnabled()) {
                logger.debug("{} Unwrapped {} and set it as a root", getComponent().getLogPrefix(),
                        c.getComponent().getFullName());
                }
            } // otherwise there is a splitSpace with one main component
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
        SplitSpaceView<?, ?> parent =
                (SplitSpaceView<?, ?>) tabDockContainer.getArea().getComponent().getParent().getView();
        removeTabDock(parent, tabDockInfo);
    }

    private void removeTabDock(SplitSpaceView<?, ?> parent, ContainerInfo tabDockInfo) {
        var parentInfo = getContainer(parent).createInfo();
        if (parent.getComponent().getChildren().size() == 2) {
            removeTabDockAndUnwrap(parentInfo, tabDockInfo);
        } else {
            removeTabDock(parentInfo, tabDockInfo);
        }
    }

    private TabDockView<?, ?> wrapAndAddTabDock(Orientation newOrientation, ContainerInfo anchorInfo,
            ContainerInfo newInfo, TabDockView<?, ?> newTabDock) {
        var newSplitSpace = wrap(anchorInfo.getContainer().getArea(), anchorInfo.getIndex());
        newSplitSpace.getComponent().addChild(newInfo.getIndex(), newTabDock.getComponent());

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
            logger.debug("{} Added {} to {}", getComponent().getLogPrefix(),
                    newTabDock.getComponent().getFullName(),
                    newSplitSpace.getComponent().getFullName());
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
        SplitSpaceView<?, ?> parentComponent = (SplitSpaceView<?, ?>) parentInfo.getContainer().getArea();
        // removing empty tabdock
        parentComponent.getComponent().removeChild(anchorInfo.getIndex());
        if (logger.isDebugEnabled()) {
            logger.debug("{} Removed {} from {}",
                    getComponent().getLogPrefix(),
                    anchorInfo.getContainer().getArea().getComponent().getFullName(),
                    parentComponent.getComponent().getFullName());
        }
        unwrap((SplitSpaceView<?, ?>) parentComponent, parentInfo.getIndex());
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
            ContainerInfo newInfo, TabDockView<?, ?> newTabDock) {
        var parentComponent =
                (SplitSpaceView<?, ?>) anchorInfo.getContainer().getArea().getComponent().getParent().getView();
        var splitPane = parentComponent.getNode();
        double[] oldPositions = splitPane.getDividerPositions();
        parentComponent.getComponent().addChild(newInfo.getIndex(), newTabDock.getComponent());
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
            logger.debug("{} Added {} into {}", getComponent().getLogPrefix(),
                newTabDock.getComponent().getFullName(), parentComponent.getComponent().getFullName());
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
    private void addTabDock(double[] grandParentPositions, SplitSpaceView<?, ?> parent, TabDockView<?, ?> dock,
            int index, Side side, boolean sideShouldBeChecked, double size) {
        if (sideShouldBeChecked && !checkNewSide(parent, index, side)) {
            boolean wrapParent = false;
            if (parent != getRoot()) {
                parent = getRoot();
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
        parent.getComponent().addChild(index, dock.getComponent());

        final var finalOldSplitPaneSize = oldSplitPaneSize;
        final var finalIndex = index;
        final var finalParent = parent;
        getPulseListenerManager().addListener(LayoutPhase.POST, () -> {
            var divSize = dividerSize;
            if (divSize < 0) {
                divSize = finalParent.computeDividerSize();
            }
            if (finalParent.getComponent().getParent() != null && grandParentPositions != null) {
                ((SplitSpaceComponent<?>) finalParent.getComponent().getParent())
                        .getView().getNode().setDividerPositions(grandParentPositions);
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
                logger.debug("{} Added {} into {}", getComponent().getLogPrefix(),
                        dock.getComponent().getFullName(),
                        dock.getComponent().getParent().getFullName());
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
    private void removeTabDock(ContainerInfo parent, ContainerInfo tabDockInfo) {
        var tabDockContainer = tabDockInfo.getContainer();
        AreaView<?, ?> componentToRemove = tabDockContainer.getArea();
        SplitSpaceView<?, ?> splitSpace = (SplitSpaceView<?, ?>) parent.getContainer().getArea();
        var splitPane = splitSpace.getNode();
        var oldPositions = splitPane.getDividerPositions();
        var oldSplitPaneSize = splitPane.getWidth();
        var removedChildSize = tabDockInfo.getContainer().getArea().getNode().getWidth();
        if (splitPane.getOrientation() == Orientation.VERTICAL) {
            oldSplitPaneSize = splitPane.getHeight();
            removedChildSize = tabDockInfo.getContainer().getArea().getNode().getHeight();
        }

        splitSpace.getComponent().removeChild(tabDockInfo.getIndex());
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
            logger.debug("{} Removed {} from {}", getComponent().getLogPrefix(),
                    componentToRemove.getComponent().getFullName(),
                    parent.getContainer().getArea().getComponent().getFullName());
        }
    }

    private void moveTab(TabDockView<?, ?> newTabDock) {
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
        var iterator = getComponent().getRoot().depthFirstIterator();
        while (iterator.hasNext()) {
            ChildComponent<?> child = (ChildComponent<?>) iterator.next();
            if (child instanceof AreaComponent<?> area) {
                AbstractContainer container = getContainer(area.getView());
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
            logger.debug("{} Component tree: {}", getComponent().getLogPrefix(), getTreeDebugInfo());
        }
    }

    private String getTreeDebugInfo() {
        StringBuilder builder = new StringBuilder();
        var iterator = getComponent().depthFirstIterator();
        while (iterator.hasNext()) {
            ChildView<?, ?> view = (ChildView<?, ?>) iterator.next().getView();
            String orientation = "";
            UUID uuid = view.getComponent().getUuid();
            if (view instanceof SplitSpaceView<?, ?> spaceV) {
                orientation = spaceV.getNode().getOrientation().name().toLowerCase();
            }
            builder.append("\n");
            builder.append("    ".repeat(iterator.getDepth()));
            builder.append(view.getComponent().getFullName());
            builder.append(" [");
            if (uuid != null) {
                builder.append("uuid: ");
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
    private Side resolveSide(AreaView<?, ?> view) {
        if (view == getComponent().getMain().getView()) {
            throw new IllegalArgumentException("Can't resolve the side of the main component");
        }
        var componentPath = findPathFromRoot(view);
        var mainPath = findPathFromRoot(getComponent().getMain().getView());
        // we must find Lowest Common Ancestor
        SplitSpaceView<?, ?> lca = getRoot();

        var componentIterator = componentPath.iterator();
        var mainIterator = mainPath.iterator();
        AreaView<?, ?> componentAncestor = null;
        AreaView<?, ?> mainAncestor = null;
        while (componentIterator.hasNext() && mainIterator.hasNext()) {
            componentAncestor = componentIterator.next();
            mainAncestor = mainIterator.next();
            if (mainAncestor == componentAncestor) {
                lca = (SplitSpaceView<?, ?>) mainAncestor;
            } else {
                break;
            }
        }

        Side result = null;
        var componentAncestorIndex = lca.getComponent().getChildren().indexOf(componentAncestor.getComponent());
        var mainAncestorIndex = lca.getComponent().getChildren().indexOf(mainAncestor.getComponent());
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
            logger.debug("{} Resolved side for {} is {}; lowest common ancestor: {}", getComponent().getLogPrefix(),
                    view.getComponent().getFullName(), result, lca.getComponent().getFullName());
        }
        return result;
    }

    /**
     * Returns the path to the root including startNode.
     *
     * @param startNode
     * @return
     */
    private List<AreaView<?, ?>> findPathFromRoot(AreaView<?, ?> startNode) {
        var result = new ArrayList<AreaView<?, ?>>();
        var current = startNode;
        while (current != null && current != this) {
            result.add(0, current);
            current = (AbstractAreaView<?, ?>) current.getComponent().getParent().getView();
        }
        if (logger.isTraceEnabled()) {
            var nodes = result.stream().map(v -> v.getComponent().getFullName()).collect(Collectors.joining(", "));
            logger.trace("{} {} path to root: {}", getComponent().getLogPrefix(),
                    startNode.getComponent().getFullName(), nodes);
        }
        return result;
    }

    /**
     * Finds the index of the child component of the specified {@link SplitSpaceView} that contains the main
     * component in its hierarchy.
     *
     * @param splitSpace the SplitSpaceView to search in
     * @return the index of the component or -1.
     */
    private int indexOfMain(SplitSpaceView<?, ?> splitSpaceView) {
        var deque = findPathFromRoot(getComponent().getMain().getView());
        var set = new HashSet<AreaView<?, ?>>(deque);
        for (var i = 0; i < splitSpaceView.getComponent().getChildren().size(); i++) {
            var child = splitSpaceView.getComponent().getChildren().get(i);
            if (set.contains(child.getView())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Updates the UUID in minimized {@link TabDockView}s. This is required when a new {@link SplitSpaceView} is created
     * in place of a removed {@link SplitSpaceView}.
     *
     * @param oldUuid
     * @param newUuid
     */
    private void updateUuidInPositions(UUID oldUuid, UUID newUuid) {
        updateUuidInPositions(getComponent().getRightSideBar(), oldUuid, newUuid);
        updateUuidInPositions(getComponent().getLeftSideBar(), oldUuid, newUuid);
        updateUuidInPositions(getComponent().getBottomSideBar(), oldUuid, newUuid);
        logger.debug("{} Replaced UUID {} with {} in position lists of all minimized TabDocks",
                getComponent().getLogPrefix(), oldUuid, newUuid);
    }

    private void updateUuidInPositions(SideBarComponent<?> sideBar, UUID oldUuid, UUID newUuid) {
        if (sideBar != null) {
            for (var tabDock : sideBar.getTabDocks()) {
                var position = tabDock.getView().getViewModel().getMinimizedPosition();
                position.updateUuid(oldUuid, newUuid);
            }
        }
    }

    private void updatePopupSize(SideBarComponent<?> sideBar, double width, double height) {
        if (sideBar != null) {
            sideBar.getView().updatePopupSize(width, height);
        }
    }

    private SplitSpaceView<?, ?> getRoot() {
        return getComponent().getRoot().getView();
    }
}
