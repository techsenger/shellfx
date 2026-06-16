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

package com.techsenger.shellfx.material.pane;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitPane;

/**
 * A binder that synchronizes divider positions between a JavaFX SplitPane and an external
 * {@code ObservableList<Double>}.
 *
 * @author Pavel Castornii
 */
public class SplitPaneDividerBinder {

    private class DividerListener implements ChangeListener<Number> {

        private int index;

        DividerListener(int index) {
            this.index = index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (updatingFromExternalList) {
                return;
            }
            updateExternalPosition(index, newValue.doubleValue());
        }
    }

    private final SplitPane splitPane;

    private final ObservableList<Double> externalList;

    private boolean updatingFromSplitPane = false;

    private boolean updatingFromExternalList = false;

    private final Map<SplitPane.Divider, DividerListener> listenersByDivider = new HashMap<>();

    private final ListChangeListener<SplitPane.Divider> dividersChangeListener;

    private final ListChangeListener<Double> externalChangeListener;

    public SplitPaneDividerBinder(SplitPane splitPane, ObservableList<Double> externalList) {
        this.splitPane = splitPane;
        this.externalList = externalList;

        this.dividersChangeListener = this::onDividersListChange;
        this.externalChangeListener = this::onExternalListChange;

        if (!externalList.isEmpty()) {
            copyFromExternalList();
        } else {
            copyToExternalList();
        }
        splitPane.getDividers().addListener(dividersChangeListener);
        externalList.addListener(externalChangeListener);
        var dividers = splitPane.getDividers();
        for (int i = 0; i < dividers.size(); i++) {
            addDividerListener(dividers.get(i), i);
        }
    }

    /**
     * Unbinds the synchronization and removes all listeners. Should be called when the binder is no longer needed.
     */
    public void unbind() {
        splitPane.getDividers().removeListener(dividersChangeListener);
        externalList.removeListener(externalChangeListener);

        for (Map.Entry<SplitPane.Divider, DividerListener> entry : listenersByDivider.entrySet()) {
            entry.getKey().positionProperty().removeListener(entry.getValue());
        }
        listenersByDivider.clear();
    }

    /**
     * Handles changes in the SplitPane's dividers list with precise index tracking.
     */
    private void onDividersListChange(ListChangeListener.Change<? extends SplitPane.Divider> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                onAddedDividers(change);
            }

            if (change.wasRemoved()) {
                onRemovedDividers(change);
            }

            if (change.wasPermutated()) {
                onPermutatedDividers(change);
            }

            if (change.wasReplaced()) {
                onReplacedDividers(change);
            }
        }
        copyToExternalList();
        if (splitPane.getDividers().size() != listenersByDivider.size()
                || splitPane.getDividers().size() != externalList.size()) {
            throw new IllegalStateException("Collections have different size");
        }
    }

    private void onAddedDividers(ListChangeListener.Change<? extends SplitPane.Divider> change) {
        int addIndex = change.getFrom();
        for (SplitPane.Divider divider : change.getAddedSubList()) {
            addDividerListener(divider, addIndex++);
        }

        // Update indexes of dividers that come after the added ones
        updateIndexesAfter(addIndex);
    }

    private void onRemovedDividers(ListChangeListener.Change<? extends SplitPane.Divider> change) {
        for (SplitPane.Divider divider : change.getRemoved()) {
            removeDividerListener(divider);
        }

        // Update indexes of dividers that come after the removed ones
        updateIndexesAfter(change.getFrom());
    }

    private void onPermutatedDividers(ListChangeListener.Change<? extends SplitPane.Divider> change) {
        // For permutation, we need to update all indexes according to the new order
        var dividers = splitPane.getDividers();
        for (int i = 0; i < dividers.size(); i++) {
            DividerListener listener = listenersByDivider.get(dividers.get(i));
            if (listener != null) {
                listener.setIndex(i);
            }
        }
    }

    private void onReplacedDividers(ListChangeListener.Change<? extends SplitPane.Divider> change) {
        // Replacement means some dividers were removed and others added at the same positions
        onRemovedDividers(change);
        onAddedDividers(change);
    }

    /**
     * Updates indexes of dividers starting from the specified index.
     */
    private void updateIndexesAfter(int startIndex) {
        var dividers = splitPane.getDividers();
        for (int i = startIndex; i < dividers.size(); i++) {
            DividerListener listener = listenersByDivider.get(dividers.get(i));
            if (listener != null) {
                listener.setIndex(i);
            }
        }
    }

    private void onExternalListChange(ListChangeListener.Change<? extends Double> change) {
        if (updatingFromSplitPane) {
            return;
        }
        while (change.next()) {
            if (change.wasUpdated() || change.wasReplaced()) {
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    if (i < externalList.size()) {
                        Double position = externalList.get(i);
                        if (position != null) {
                            updateDividerPosition(i, position);
                        }
                    }
                }
            } else if (change.wasAdded() || change.wasRemoved() || change.wasPermutated()) {
                copyFromExternalList();
            }
        }
    }

    private void addDividerListener(SplitPane.Divider divider, int index) {
        DividerListener listener = new DividerListener(index);
        divider.positionProperty().addListener(listener);
        listenersByDivider.put(divider, listener);
    }

    private void removeDividerListener(SplitPane.Divider divider) {
        DividerListener listener = listenersByDivider.remove(divider);
        if (listener != null) {
            divider.positionProperty().removeListener(listener);
        }
    }

    private void copyToExternalList() {
        try {
            updatingFromSplitPane = true;
            var dividers = splitPane.getDividers();
            externalList.clear();
            for (var divider : dividers) {
                externalList.add(divider.getPosition());
            }
        } finally {
            updatingFromSplitPane = false;
        }
    }

    private void updateExternalPosition(int index, double position) {
        try {
            updatingFromSplitPane = true;
            externalList.set(index, position);
        } finally {
            updatingFromSplitPane = false;
        }
    }

    private void copyFromExternalList() {
        try {
            updatingFromExternalList = true;
            var dividers = splitPane.getDividers();
            for (int i = 0; i < externalList.size(); i++) {
                Double position = externalList.get(i);
                if (position != null) {
                    dividers.get(i).setPosition(position);
                }
            }
        } finally {
            updatingFromExternalList = false;
        }
    }

    private void updateDividerPosition(int index, double position) {
        try {
            updatingFromExternalList = true;
            var dividers = splitPane.getDividers();
            if (index >= 0 && index < dividers.size()) {
                dividers.get(index).setPosition(position);
            }
        } finally {
            updatingFromExternalList = false;
        }
    }
}
