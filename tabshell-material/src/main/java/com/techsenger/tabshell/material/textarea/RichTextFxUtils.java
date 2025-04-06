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

package com.techsenger.tabshell.material.textarea;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyledTextArea;

/**
 *
 * @author Pavel Castornii
 */
public final class RichTextFxUtils {

    public static void scrollToBottom(StyledTextArea textArea) {
        //sometimes doesn't scroll to the end leaving one line ?
        textArea.moveTo(textArea.getLength());
        textArea.requestFollowCaret();
    }

    public static void scrollToTop(StyledTextArea textArea) {
        textArea.moveTo(0);
        textArea.requestFollowCaret();
    }

    public static boolean isScrolledToTop(VirtualizedScrollPane<?> scrollPane) {
        return getScrollPaneFractionalPosition(scrollPane) == 0.0;
    }

    public static boolean isScrolledToBottom(VirtualizedScrollPane<?> scrollPane) {
        double epsilon = 0.0001d;
        double position = getScrollPaneFractionalPosition(scrollPane) + epsilon;
        //when scrollpane is not visible or is not appended then position is NaN
        return position >= 0.9995 || Double.isNaN(position);
    }

    /**
     * See https://github.com/FXMisc/RichTextFX/issues/1108 .
     * @param scrollPane
     * @return
     */
    private static double getScrollPaneFractionalPosition(VirtualizedScrollPane<?> scrollPane) {
        double currentAbsolutePos = scrollPane.estimatedScrollYProperty().getValue();
        double viewPortHeight = scrollPane.getHeight();
        double totalEstimatedHeight = (double) scrollPane.totalHeightEstimateProperty().getOrSupply(() -> 1.0);
        double fractionalPos = (double) currentAbsolutePos / (totalEstimatedHeight - viewPortHeight);
        return fractionalPos;
    }

    private RichTextFxUtils() {
        //empty
    }
}
