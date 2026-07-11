///*
// * Copyright 2024-2026 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.shellfx.layout.dockhost;
//
//import com.techsenger.shellfx.core.area.AbstractAreaFxView;
//import com.techsenger.shellfx.core.area.AreaFxView;
//import static com.techsenger.shellfx.layout.dockhost.DockConstants.ONE_THIRD;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import javafx.beans.value.ChangeListener;
//import javafx.collections.ListChangeListener;
//import javafx.geometry.Orientation;
//import javafx.geometry.Side;
//import static javafx.geometry.Side.LEFT;
//import javafx.scene.Node;
//import javafx.scene.control.SplitPane;
//import javafx.scene.layout.Region;
//import javafx.scene.layout.StackPane;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author Pavel Castornii
// */
//public class SplitSpaceFxView<P extends SplitSpacePresenter<?>> extends AbstractAreaFxView<P>
//        implements SplitSpaceView {
//
//    public class Composer extends AbstractAreaFxView<P>.Composer {
//
//        private final SplitSpaceFxView<P> view = SplitSpaceFxView.this;
//
//        private DockHostFxView<?> dockHost;
//
//        public void addChild(AreaFxView<?> child) {
//            Node container = dockHost.createContainer(child);
//            splitPane.getItems().add(container);
//            getModifiableChildren().add(child);
//        }
//
//        public void addChild(int index, AreaFxView<?> child) {
//            Node container = dockHost.createContainer(child);
//            splitPane.getItems().add(index, container);
//            getModifiableChildren().add(index, child);
//        }
//
//        public void removeChild(int index) {
//            var child = removeChildFromView(index);
//            var childName = child.getDescriptor().getFullName();
//            logger.debug("{} Removed {}; index: {}", getDescriptor().getLogPrefix(), childName, index);
//        }
//
//        void replacePlaceholder(int index, TabDockFxView<?> tabDock) {
//            removeChildFromView(index);
//            addChild(index, tabDock);
//        }
//
//        AreaFxView<?> getChild(int index) {
//            return (AreaFxView<?>) getModifiableChildren().get(index);
//        }
//
//        protected DockHostFxView<?> getDockHost() {
//            return dockHost;
//        }
//
//        protected void setDockHost(DockHostFxView<?> dockHost) {
//            if (this.dockHost == null) {
//                this.dockHost = dockHost;
//            }
//        }
//
//        private AreaFxView<?> removeChildFromView(int index) {
//            StackPane container = (StackPane) splitPane.getItems().remove(index);
//            dockHost.destroyContainer(container);
//            var child = (AreaFxView<?>) getModifiableChildren().remove(index);
//            return child;
//        }
//    }
//
//    private static final Logger logger = LoggerFactory.getLogger(SplitSpaceFxView.class);
//
//    private final SplitPane splitPane = new SplitPane();
//
//    protected SplitSpaceFxView() {
//        super();
//    }
//
//    @Override
//    public void requestFocus() {
//
//    }
//
//    @Override
//    public SplitPane getNode() {
//        return splitPane;
//    }
//
//    @Override
//    public void setOrientation(Orientation orientation) {
//        this.splitPane.setOrientation(orientation);
//    }
//
//    @Override
//    public void setDividerPositions(List<Double> positions) {
//        double[] doubleArray = positions.stream()
//                .mapToDouble(Double::doubleValue)
//                .toArray();
//        this.splitPane.setDividerPositions(doubleArray);
//    }
//
//    @Override
//    protected void addListeners() {
//        super.addListeners();
//        ChangeListener<Number> listener = (obs, oldPos, newPos) -> {
//            getPresenter().onDividerPositionsChanged(splitPane.getDividerPositions());
//        };
//        splitPane.getDividers().addListener((ListChangeListener<SplitPane.Divider>) change -> {
//            while (change.next()) {
//                if (change.wasAdded()) {
//                    change.getAddedSubList()
//                          .forEach(d -> d.positionProperty().addListener(listener));
//                }
//                if (change.wasRemoved()) {
//                    change.getRemoved()
//                          .forEach(d -> d.positionProperty().removeListener(listener));
//                }
//            }
//        });
//    }
//
//    @Override
//    public Composer getComposer() {
//        return (Composer) super.getComposer();
//    }
//
//    @Override
//    protected Composer createComposer() {
//        return new Composer();
//    }
//
//}
