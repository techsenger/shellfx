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

/**
 *
 * @author Pavel Castornii
 */
public final class Anchors {

    private final Double top;

    private final Double right;

    private final Double bottom;

    private final Double left;

    private Anchors(Double top, Double right, Double bottom, Double left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public static Anchors center() {
        return new Anchors(null, null, null, null);
    }

    public static Anchors fill() {
        return new Anchors(0.0, 0.0, 0.0, 0.0);
    }

    public static Anchors top(double top) {
        return new Anchors(top, null, null, null);
    }

    public static Anchors bottom(double bottom) {
        return new Anchors(null, null, bottom, null);
    }

    public static Anchors topRight(double top, double right) {
        return new Anchors(top, right, null, null);
    }

    public static Anchors topLeft(double top, double left) {
        return new Anchors(top, null, null, left);
    }

    public static Anchors bottomRight(double bottom, double right) {
        return new Anchors(null, right, bottom, null);
    }

    public static Anchors bottomLeft(double bottom, double left) {
        return new Anchors(null, null, bottom, left);
    }

    public static Anchors of(Double top, Double right, Double bottom, Double left) {
        return new Anchors(top, right, bottom, left);
    }

    public Double getTop() {
        return top;
    }

    public Double getRight() {
        return right;
    }

    public Double getBottom() {
        return bottom;
    }

    public Double getLeft() {
        return left;
    }
}
