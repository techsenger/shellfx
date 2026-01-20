/*
 * Copyright 2025 Pavel Castornii.
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

package com.techsenger.tabshell.core.settings;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

/**
 *
 * @author Pavel Castornii
 */
public final class SubscriptionUtils {

    private static final class SettingsSubscriptionImpl<T> implements SettingsSubscription {

        private final Property<T> property;

        private final ChangeListener<T> listener;

        private final WeakChangeListener<T> weakListener;

        private SettingsSubscriptionImpl(Property<T> property, ChangeListener<T> listener,
                WeakChangeListener<T> weakListener) {
            this.property = property;
            this.listener = listener;
            this.weakListener = weakListener;
        }

        @Override
        public void unsubscribe() {
            property.removeListener(weakListener);
        }
    }

    public static SettingsSubscription observe(IntegerProperty property, SettingsObserver<Integer> observer) {
        ChangeListener<Number> listener = (ov, oldV, newV) -> {
            observer.onChanged(oldV.intValue(), newV.intValue());
        };
        var weakListener = new WeakChangeListener<Number>(listener);
        property.addListener(weakListener);
        return new SettingsSubscriptionImpl<>(property, listener, weakListener);
    }

    public static SettingsSubscription observe(LongProperty property, SettingsObserver<Long> observer) {
        ChangeListener<Number> listener = (ov, oldV, newV) -> {
            observer.onChanged(oldV.longValue(), newV.longValue());
        };
        var weakListener = new WeakChangeListener<Number>(listener);
        property.addListener(weakListener);
        return new SettingsSubscriptionImpl<>(property, listener, weakListener);
    }

    public static SettingsSubscription observe(DoubleProperty property, SettingsObserver<Double> observer) {
        ChangeListener<Number> listener = (ov, oldV, newV) -> {
            observer.onChanged(oldV.doubleValue(), newV.doubleValue());
        };
        var weakListener = new WeakChangeListener<Number>(listener);
        property.addListener(weakListener);
        return new SettingsSubscriptionImpl<>(property, listener, weakListener);
    }

    public static SettingsSubscription observe(FloatProperty property, SettingsObserver<Float> observer) {
        ChangeListener<Number> listener = (ov, oldV, newV) -> {
            observer.onChanged(oldV.floatValue(), newV.floatValue());
        };
        var weakListener = new WeakChangeListener<Number>(listener);
        property.addListener(weakListener);
        return new SettingsSubscriptionImpl<>(property, listener, weakListener);
    }

    public static <T> SettingsSubscription observe(Property<T> property, SettingsObserver<T> observer) {
        ChangeListener<T> listener = (ov, oldV, newV) -> {
            observer.onChanged(oldV, newV);
        };
        var weakListener = new WeakChangeListener<T>(listener);
        property.addListener(weakListener);
        return new SettingsSubscriptionImpl<>(property, listener, weakListener);
    }

    private SubscriptionUtils() {
        // empty
    }
}
