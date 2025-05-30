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

package com.techsenger.tabshell.hex.data;

import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.core.tab.TabKey;
import com.techsenger.tabshell.hex.HexComponentKeys;
import com.techsenger.tabshell.hex.HexDocument;
import java.math.BigInteger;
import java.nio.ByteOrder;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class DataInspectorViewModel extends AbstractTabViewModel {

    private final ObservableList<ByteOrder> byteOrders = FXCollections.observableArrayList(
            ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN);

    private final ReadOnlyObjectWrapper<ByteOrder> byteOrder = new ReadOnlyObjectWrapper<>(ByteOrder.BIG_ENDIAN);

    private final ObservableList<TypeItem<?>> typeItems = FXCollections.observableArrayList(
            new TypeItem<Long>(1, "Signed Int 8", (v) -> v.getSignedInt8(), BaseConverters::convert),
            new TypeItem<Long>(1, "Unsigned Int 8", (v) -> v.getUnsignedInt8(), BaseConverters::convert),
            new TypeItem<Long>(2, "Signed Int 16", (v) -> v.getSignedInt16(), BaseConverters::convert),
            new TypeItem<Long>(2, "Unsigned Int 16", (v) -> v.getUnsignedInt16(), BaseConverters::convert),
            new TypeItem<Long>(3, "Signed Int 24", (v) -> v.getSignedInt24(), BaseConverters::convert),
            new TypeItem<Long>(3, "Unsigned Int 24", (v) -> v.getUnsignedInt24(), BaseConverters::convert),
            new TypeItem<Long>(4, "Signed Int 32", (v) -> v.getSignedInt32(), BaseConverters::convert),
            new TypeItem<Long>(4, "Unsigned Int 32", (v) -> v.getUnsignedInt32(), BaseConverters::convert),
            new TypeItem<Long>(6, "Signed Int 48", (v) -> v.getSignedInt48(), BaseConverters::convert),
            new TypeItem<Long>(6, "Unsigned Int 48", (v) -> v.getUnsignedInt48(), BaseConverters::convert),
            new TypeItem<Long>(8, "Signed Int 64", (v) -> v.getSignedInt64(), (v, s) -> BaseConverters.convert(v, s)),
            new TypeItem<BigInteger>(8, "Unsigned Int 64", (v) -> v.getUnsignedInt64(), BaseConverters::convert),
            new TypeItem<Float>(4, "Float 32", (v) -> v.getFloat32()),
            new TypeItem<Double>(8, "Float 64", (v) -> v.getFloat64()),
            new TypeItem<Character>(1, "Char 8", (v) -> v.getChar8(), (v, s) -> BaseConverters.convert(v)),
            new TypeItem<Character>(2, "Char 16", (v) -> v.getChar16(), (v, s) -> BaseConverters.convert(v)),
            new TypeItem<String>("UTF 8", (v) -> v.getUtf8Char(), BaseConverters::convert, (v) -> v.getUtf8Length()),
            new TypeItem<String>("UTF 16", (v) -> v.getUtf16Char(), BaseConverters::convert, (v) -> v.getUtf16Length()),
            new TypeItem<String>(4, "UTF 32", (v) -> v.getUtf32Char(), BaseConverters::convert)
    );

    private final ReadOnlyObjectWrapper<TypeItem<?>> selectedTypeItem = new ReadOnlyObjectWrapper<>();

    private final ObservableList<BaseItem> baseItems = FXCollections.observableArrayList(
            new BaseItem("Decimal", (c) -> c.getDecimal()),
            new BaseItem("Hexadecimal", (c) -> c.getHexadecimal()),
            new BaseItem("Octal", (c) -> c.getOctal()),
            new BaseItem("Binary", (c) -> c.getBinary())
    );

    private final ReadOnlyObjectWrapper<BaseItem> selectedBaseItem = new ReadOnlyObjectWrapper<>();

    private final HexDocument document;

    private final ReadOnlyIntegerProperty offset;

    private Values values;

    public DataInspectorViewModel(HexDocument document, ReadOnlyIntegerProperty offset) {
        this.document = document;
        this.offset = offset;
        this.offset.addListener((ov, oldV, newV) -> updateTypeItems());
        this.byteOrder.addListener((ov, oldV, newV) -> updateTypeItems());
        this.selectedTypeItem.addListener((ov, oldV, newV) -> updateBaseItems());
        setTitle("Data Inspector");
    }

    @Override
    public TabKey getKey() {
        return HexComponentKeys.DATA_INSPECTOR;
    }

    public void updateTypeItems() {
        this.values = new Values();
        ValueCalculator.calculateInt(values, this.document.getContent(), this.offset.get(), getByteOrder());
        ValueCalculator.calculateFloat(values, this.document.getContent(), this.offset.get(), getByteOrder());
        ValueCalculator.calculateChars(values, this.document.getContent(), this.offset.get(), getByteOrder());
        ValueCalculator.calculateUnicode(values, this.document.getContent(), this.offset.get(), getByteOrder());
        for (var item : typeItems) {
            var provider = item.getValueProvider();
            var value = provider.provide(values);
            if (value == null) {
                item.setValue(null);
            } else {
                var str = value.toString();
                item.setValue(str);
            }
        }
        updateBaseItems();
    }

    public ReadOnlyObjectProperty<ByteOrder> byteOrderProperty() {
        return byteOrder.getReadOnlyProperty();
    }

    public ByteOrder getByteOrder() {
        return byteOrder.get();
    }

    public ReadOnlyObjectProperty<TypeItem<?>> selectedTypeItemProperty() {
        return selectedTypeItem.getReadOnlyProperty();
    }

    public TypeItem<?> getSelectedTypeItem() {
        return selectedTypeItem.get();
    }

    public ReadOnlyObjectProperty<BaseItem> selectedBaseItemProperty() {
        return selectedBaseItem.getReadOnlyProperty();
    }

    public BaseItem getSelectedBaseItem() {
        return selectedBaseItem.get();
    }

    protected ObservableList<ByteOrder> getByteOrders() {
        return byteOrders;
    }

    protected ObservableList<TypeItem<?>> getTypeItems() {
        return typeItems;
    }

    protected ObservableList<BaseItem> getBaseItems() {
        return baseItems;
    }

    ReadOnlyObjectWrapper<ByteOrder> byteOrderWrapper() {
        return byteOrder;
    }

    ReadOnlyObjectWrapper<TypeItem<?>> selectedTypeItemWrapper() {
        return selectedTypeItem;
    }

    ReadOnlyObjectWrapper<BaseItem> selectedBaseItemWrapper() {
        return selectedBaseItem;
    }

    private void updateBaseItems() {
        var typeItem = getSelectedTypeItem();
        NumberBases bases = null;
        if (typeItem != null) {
            bases = typeItem.createBases(values);
        }
        if (typeItem == null || bases == null) {
            for (var b : baseItems) {
                b.setValue(null);
            }
        } else {
            for (var b: this.baseItems) {
                var converted = b.getBaseSelector().apply(bases);
                b.setValue(converted);
            }
        }
    }
}
