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

package com.techsenger.tabshell.material;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Pavel Castornii
 */
public final class FxUtils {

    public static boolean isItemVisible(ListView listView, int itemIndex) {
        if (itemIndex < 0) {
            return false;
        }
        ListViewSkin<?> ts = (ListViewSkin<?>) listView.getSkin();
        if (ts == null) {
            return false;
        }
        VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
        int first = vf.getFirstVisibleCell().getIndex();
        int last = vf.getLastVisibleCell().getIndex();
        return first <= itemIndex && itemIndex <= last;
    }

    /**
     * When user enters value in editable ComboBox and presses enter for default button action then ComboBox doesn't
     * update its value. This method solves this problem. See https://bugs.openjdk.org/browse/JDK-8332938
     *
     * @param comboBox
     */
    public static void makeValueUpdateOnEnter(ComboBox<?> comboBox) {
        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER) {
              comboBox.commitValue();
            }
        });
    }

    private FxUtils() {
        //empty
    }
}
