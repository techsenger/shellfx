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

package com.techsenger.tabshell.hex.inspector;

import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.tab.AbstractTabViewModel;
import com.techsenger.tabshell.core.tab.TabMediator;
import com.techsenger.tabshell.hex.model.HexDocument;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.function.Consumer;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class DataInspectorTabViewModel extends AbstractTabViewModel<TabMediator> {

    private final ObservableList<ByteOrder> byteOrders = FXCollections.observableArrayList(
            ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN);

    private final ReadOnlyObjectWrapper<ByteOrder> byteOrder = new ReadOnlyObjectWrapper<>(ByteOrder.BIG_ENDIAN);

    private final ObservableList<TypeItem<?>> typeItems = FXCollections.observableArrayList(
            new TypeItem<Long>(8, "Signed Int8", (v) -> v.getSignedInt8(), NumberBaseConverters::convert),
            new TypeItem<Long>(8, "Unsigned Int8", (v) -> v.getUnsignedInt8(), NumberBaseConverters::convert),
            new TypeItem<Long>(16, "Signed Int16", (v) -> v.getSignedInt16(), NumberBaseConverters::convert),
            new TypeItem<Long>(16, "Unsigned Int16", (v) -> v.getUnsignedInt16(), NumberBaseConverters::convert),
            new TypeItem<Long>(24, "Signed Int24", (v) -> v.getSignedInt24(), NumberBaseConverters::convert),
            new TypeItem<Long>(24, "Unsigned Int24", (v) -> v.getUnsignedInt24(), NumberBaseConverters::convert),
            new TypeItem<Long>(32, "Signed Int32", (v) -> v.getSignedInt32(), NumberBaseConverters::convert),
            new TypeItem<Long>(32, "Unsigned Int32", (v) -> v.getUnsignedInt32(), NumberBaseConverters::convert),
            new TypeItem<Long>(48, "Signed Int48", (v) -> v.getSignedInt48(), NumberBaseConverters::convert),
            new TypeItem<Long>(48, "Unsigned Int48", (v) -> v.getUnsignedInt48(), NumberBaseConverters::convert),
            new TypeItem<Long>(64, "Signed Int64", (v) -> v.getSignedInt64(),
                    (v, s) -> NumberBaseConverters.convert(v, s)),
            new TypeItem<BigInteger>(64, "Unsigned Int64", (v) -> v.getUnsignedInt64(), NumberBaseConverters::convert),
            new TypeItem<Float>(32, "Float32", (v) -> v.getFloat32()),
            new TypeItem<Double>(64, "Float64", (v) -> v.getFloat64()),
            new TypeItem<Character>(8, "Char8", (v) -> v.getChar8(), (v, s) -> NumberBaseConverters.convert(v)),
            new TypeItem<Character>(16, "Char16", (v) -> v.getChar16(), (v, s) -> NumberBaseConverters.convert(v)),
            new TypeItem<String>("UTF8", (v) -> v.getUtf8Char(), NumberBaseConverters::convert,
                    (v) -> v.getUtf8Size()),
            new TypeItem<String>("UTF16", (v) -> v.getUtf16Char(), NumberBaseConverters::convert,
                    (v) -> v.getUtf16Size()),
            new TypeItem<String>(32, "UTF32", (v) -> v.getUtf32Char(), NumberBaseConverters::convert)
    );

    private final ReadOnlyObjectWrapper<TypeItem<?>> selectedTypeItem = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyStringWrapper decimal = new ReadOnlyStringWrapper();

    private final ReadOnlyStringWrapper hexadecimal = new ReadOnlyStringWrapper();

    private final ReadOnlyStringWrapper octal = new ReadOnlyStringWrapper();

    private final ReadOnlyStringWrapper binary = new ReadOnlyStringWrapper();

    private final HexDocument document;

    private final ReadOnlyIntegerProperty offset;

    private Values values;

    public DataInspectorTabViewModel(HexDocument document, ReadOnlyIntegerProperty offset) {
        this.document = document;
        this.offset = offset;
        this.offset.addListener((ov, oldV, newV) -> updateTypeItems());
        this.byteOrder.addListener((ov, oldV, newV) -> updateTypeItems());
        this.selectedTypeItem.addListener((ov, oldV, newV) -> updateBaseItems());
        setTitle("Data Inspector");
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

    public String getDecimal() {
        return decimal.get();
    }

    public ReadOnlyStringProperty decimalProperty() {
        return decimal.getReadOnlyProperty();
    }

    public String getHexadecimal() {
        return hexadecimal.get();
    }

    public ReadOnlyStringProperty hexadecimalProperty() {
        return hexadecimal.getReadOnlyProperty();
    }

    public String getOctal() {
        return octal.get();
    }

    public ReadOnlyStringProperty octalProperty() {
        return octal.getReadOnlyProperty();
    }

    public String getBinary() {
        return binary.get();
    }

    public ReadOnlyStringProperty binaryProperty() {
        return binary.getReadOnlyProperty();
    }

    @Override
    public CloseCheckResult canClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected ObservableList<ByteOrder> getByteOrders() {
        return byteOrders;
    }

    protected ObservableList<TypeItem<?>> getTypeItems() {
        return typeItems;
    }

    ReadOnlyObjectWrapper<ByteOrder> byteOrderWrapper() {
        return byteOrder;
    }

    ReadOnlyObjectWrapper<TypeItem<?>> selectedTypeItemWrapper() {
        return selectedTypeItem;
    }

    private void setDecimal(String decimal) {
        this.decimal.set(decimal);
    }

    private void setHexadecimal(String hexadecimal) {
        this.hexadecimal.set(hexadecimal);
    }

    private void setOctal(String octal) {
        this.octal.set(octal);
    }

    private void setBinary(String binary) {
        this.binary.set(binary);
    }

    private void updateBaseItems() {
        var typeItem = getSelectedTypeItem();
        NumberBases bases = null;
        if (typeItem != null) {
            bases = typeItem.createBases(values);
        }
        if (typeItem == null || bases == null) {
            setDecimal(null);
            setHexadecimal(null);
            setOctal(null);
            setBinary(null);
        } else {
            setDecimal(bases.getDecimal());
            setHexadecimal(bases.getHexadecimal());
            setOctal(bases.getOctal());
            setBinary(bases.getBinary());
        }
    }
}
