/*
 * Copyright (c) 2026 Pavel Castornii. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. This particular file is
 * subject to the "Classpath" exception as provided in the LICENSE file
 * that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.techsenger.statefx.it;

import com.techsenger.statefx.core.StateFactory;
import com.techsenger.statefx.core.Synchronized;
import com.techsenger.statefx.states.BooleanSelectedState;
import com.techsenger.statefx.states.DoubleWidthState;
import com.techsenger.statefx.states.IntegerLengthState;
import com.techsenger.statefx.states.ListItemsState;
import com.techsenger.statefx.states.MapPropertiesState;
import com.techsenger.statefx.states.ObjectSideState;
import com.techsenger.statefx.states.ROBooleanSelectedState;
import com.techsenger.statefx.states.RODoubleWidthState;
import com.techsenger.statefx.states.ROIntegerLengthState;
import com.techsenger.statefx.states.ROObjectSideState;
import com.techsenger.statefx.states.ROStringTitleState;
import com.techsenger.statefx.states.RWListItemsState;
import com.techsenger.statefx.states.RWMapPropertiesState;
import com.techsenger.statefx.states.RWSetPseudoClassStatesState;
import com.techsenger.statefx.states.SetPseudoClassStatesState;
import com.techsenger.statefx.states.StringTitleState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Side;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Pavel Castornii
 */
public class StateFactoryIT {

    public interface Complex extends
            BooleanSelectedState,
            ROIntegerLengthState,
            LongYState,
            DoubleWidthState,
            ROFloatXState,
            StringTitleState,
            ROObjectSideState<Side> { }

    public interface WritableComplex extends Complex, IntegerLengthState, FloatXState, ObjectSideState<Side> { }

    @Test
    void getImplementation_sameInterfaceTwice_returnsCachedClass() {
        // When
        Class<?> firstCall = StateFactory.getImplementation(BooleanSelectedState.class);
        Class<?> secondCall = StateFactory.getImplementation(BooleanSelectedState.class);

        // Then
        assertThat(firstCall).isSameAs(secondCall);

        // Verify instances are different but same class
        BooleanSelectedState instance1 = StateFactory.create(BooleanSelectedState.class);
        BooleanSelectedState instance2 = StateFactory.create(BooleanSelectedState.class);
        assertThat(instance1).isNotSameAs(instance2);
        assertThat(instance1.getClass()).isSameAs(firstCall);
    }

    @Test
    void getImplementation_nullInterface_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> StateFactory.getImplementation(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getImplementation_concurrentCalls_threadSafe() throws InterruptedException {
        final int threadCount = 10;
        final Class<?>[] results = new Class<?>[threadCount];
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = StateFactory.getImplementation(BooleanSelectedState.class);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Class<?> firstResult = results[0];
        for (Class<?> result : results) {
            assertThat(result).isSameAs(firstResult);
        }
    }


    @Test
    void create_nullInterface_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> StateFactory.create(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_multipleCalls_createsIndependentInstances() {
        BooleanSelectedState state1 = StateFactory.create(BooleanSelectedState.class);
        BooleanSelectedState state2 = StateFactory.create(BooleanSelectedState.class);

        assertThat(state1).isNotSameAs(state2);

        // Test independence
        state1.setSelected(true);
        state2.setSelected(false);

        assertThat(state1.isSelected()).isTrue();
        assertThat(state2.isSelected()).isFalse();
    }

    @Test
    public void create_writableComplex_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(WritableComplex.class);
        checkBoolean(state);
        checkInteger(state);
        checkLong(state);
        checkDouble(state);
        checkFloat(state);
        checkString(state);
        checkObject(state);
    }

    @Test
    public void create_booleanState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(BooleanSelectedState.class);
        checkBoolean(state);
    }

    @Test
    public void create_integerState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(IntegerLengthState.class);
        checkInteger(state);
    }

    @Test
    public void create_doubleState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(DoubleWidthState.class);
        checkDouble(state);
    }

    @Test
    public void create_floatState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(FloatXState.class);
        checkFloat(state);
    }

    @Test
    public void create_longState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(LongYState.class);
        checkLong(state);
    }

    @Test
    public void create_stringState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(StringTitleState.class);
        checkString(state);
    }

    @Test
    public void create_objectState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(ObjectSideState.class);
        checkObject(state);
    }

    /* ******************************* list ******************************* */

    @Test
    public void create_listState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(ListItemsState.class);
        checkList(state);
    }

    @Test
    public void create_rwListState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(RWListItemsState.class);
        checkRwList(state);
    }

    protected interface SyncListItemsState extends ListItemsState<String> {

        @Override
        @Synchronized
        ObservableList<String> getItems();
    }

    @Test
    public void create_syncListState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncListItemsState.class);
        assertThat(state.getItems().getClass()).isSameAs(
                FXCollections.synchronizedObservableList(FXCollections.observableArrayList()).getClass());
        checkList(state);
    }

    protected interface SyncRWListItemsState extends RWListItemsState<String> {

        @Override
        @Synchronized
        ObservableList<String> getModifiableItems();
    }

    @Test
    public void create_syncRwListState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncRWListItemsState.class);
        assertThat(state.getModifiableItems().getClass()).isSameAs(
                FXCollections.synchronizedObservableList(FXCollections.observableArrayList()).getClass());
        checkRwList(state);
    }

    /* ******************************* set ******************************* */

    @Test
    public void create_setState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SetPseudoClassStatesState.class);
        checkSet(state);
    }

    @Test
    public void create_rwSetState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(RWSetPseudoClassStatesState.class);
        checkRwSet(state);
    }

    protected interface SyncSetPseudoClassStatesState extends SetPseudoClassStatesState<String> {

        @Override
        @Synchronized
        ObservableSet<String> getPseudoClassStates();
    }

    @Test
    public void create_syncSetState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncSetPseudoClassStatesState.class);
        assertThat(state.getPseudoClassStates().getClass()).isSameAs(
                FXCollections.synchronizedObservableSet(FXCollections.observableSet()).getClass());
        checkSet(state);
    }

    protected interface SyncRWSetPseudoClassStatesState extends RWSetPseudoClassStatesState<String> {

        @Override
        @Synchronized
        ObservableSet<String> getModifiablePseudoClassStates();
    }

    @Test
    public void create_syncRwSetState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncRWSetPseudoClassStatesState.class);
        assertThat(state.getModifiablePseudoClassStates().getClass()).isSameAs(
                FXCollections.synchronizedObservableSet(FXCollections.observableSet()).getClass());
        checkRwSet(state);
    }

    /* ******************************* map ******************************* */

    @Test
    public void create_mapState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(MapPropertiesState.class);
        checkMap(state);
    }

    @Test
    public void create_rwMapState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(RWMapPropertiesState.class);
        checkRwMap(state);
    }

    protected interface SyncMapPropertiesState extends MapPropertiesState<String, String> {

        @Override
        @Synchronized
        ObservableMap<String, String> getProperties();
    }

    @Test
    public void create_syncMapState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncMapPropertiesState.class);
        assertThat(state.getProperties().getClass()).isSameAs(
                FXCollections.synchronizedObservableMap(FXCollections.observableHashMap()).getClass());
        checkMap(state);
    }

    protected interface SyncRWMapPropertiesState extends RWMapPropertiesState<String, String> {

        @Override
        @Synchronized
        ObservableMap<String, String> getModifiableProperties();
    }

    @Test
    public void create_syncRwMapState_createsValidStateWithAllMethods() throws Exception {
        var state = StateFactory.create(SyncRWMapPropertiesState.class);
        assertThat(state.getModifiableProperties().getClass()).isSameAs(
                FXCollections.synchronizedObservableMap(FXCollections.observableHashMap()).getClass());
        checkRwMap(state);
    }

    /* ******************************* checks ******************************* */

    private void checkList(ListItemsState<String> state) {
        assertThat(state).isNotNull();
        var items = state.getItems();
        assertThat(items).isNotNull();
        assertThat(items).isSameAs(state.getItems());
        final boolean[] listenerCalled = {false};
        var listener = (ListChangeListener<String>) (e) -> {
            listenerCalled[0] = true;
        };
        items.addListener(listener);
        items.add("Test");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(items.size()).isEqualTo(1);
        assertThat(items).isInstanceOf(ObservableList.class);
    }

    private void checkRwList(RWListItemsState<String> state) {
        assertThat(state).isNotNull();
        var items = state.getItems();
        var modifItems = state.getModifiableItems();
        assertThat(items).isNotNull();
        assertThat(modifItems).isNotNull();

        assertThat(items).isSameAs(state.getItems());
        assertThat(modifItems).isSameAs(state.getModifiableItems());
        assertThat(items).isNotSameAs(modifItems);

        boolean[] listenerCalled = {false};
        var listener = (ListChangeListener<String>) (e) -> {
            listenerCalled[0] = true;
        };
        boolean[] modifListenerCalled = {false};
        var modifListener = (ListChangeListener<String>) (e) -> {
            modifListenerCalled[0] = true;
        };
        items.addListener(listener);
        modifItems.addListener(modifListener);

        assertThatThrownBy(() -> items.add("Test")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(listenerCalled[0]).isFalse();
        assertThat(modifListenerCalled[0]).isFalse();
        assertThat(items.size()).isEqualTo(0);
        assertThat(modifItems.size()).isEqualTo(0);

        modifItems.add("Test");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(modifListenerCalled[0]).isTrue();
        assertThat(items.size()).isEqualTo(1);
        assertThat(modifItems.size()).isEqualTo(1);

        assertThat(items).isInstanceOf(ObservableList.class);
        assertThat(modifItems).isInstanceOf(ObservableList.class);
    }

    private void checkSet(SetPseudoClassStatesState<String> state) {
        assertThat(state).isNotNull();
        var classStates = state.getPseudoClassStates();
        assertThat(classStates).isNotNull();
        assertThat(classStates).isSameAs(state.getPseudoClassStates());

        final boolean[] listenerCalled = {false};
        var listener = (SetChangeListener<String>) (e) -> {
            listenerCalled[0] = true;
        };

        classStates.addListener(listener);
        classStates.add("Test");

        assertThat(listenerCalled[0]).isTrue();
        assertThat(classStates.size()).isEqualTo(1);
        assertThat(classStates).isInstanceOf(ObservableSet.class);
    }

    private void checkRwSet(RWSetPseudoClassStatesState<String> state) {
        assertThat(state).isNotNull();
        var classStates = state.getPseudoClassStates();
        var modifClassStates = state.getModifiablePseudoClassStates();

        assertThat(classStates).isNotNull();
        assertThat(modifClassStates).isNotNull();

        assertThat(classStates).isSameAs(state.getPseudoClassStates());
        assertThat(modifClassStates).isSameAs(state.getModifiablePseudoClassStates());
        assertThat(classStates).isNotSameAs(modifClassStates);

        boolean[] listenerCalled = {false};
        var listener = (SetChangeListener<String>) (e) -> {
            listenerCalled[0] = true;
        };

        boolean[] modifListenerCalled = {false};
        var modifListener = (SetChangeListener<String>) (e) -> {
            modifListenerCalled[0] = true;
        };

        classStates.addListener(listener);
        modifClassStates.addListener(modifListener);

        assertThatThrownBy(() -> classStates.add("Test")).isInstanceOf(UnsupportedOperationException.class);
        assertThat(listenerCalled[0]).isFalse();
        assertThat(modifListenerCalled[0]).isFalse();
        assertThat(classStates.size()).isEqualTo(0);
        assertThat(modifClassStates.size()).isEqualTo(0);

        modifClassStates.add("Test");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(modifListenerCalled[0]).isTrue();
        assertThat(classStates.size()).isEqualTo(1);
        assertThat(modifClassStates.size()).isEqualTo(1);

        assertThat(classStates).isInstanceOf(ObservableSet.class);
        assertThat(modifClassStates).isInstanceOf(ObservableSet.class);
    }

    private void checkMap(MapPropertiesState<String, String> state) {
        assertThat(state).isNotNull();
        var properties = state.getProperties();
        assertThat(properties).isNotNull();
        assertThat(properties).isSameAs(state.getProperties());

        final boolean[] listenerCalled = {false};
        var listener = (MapChangeListener<String, String>) (e) -> {
            listenerCalled[0] = true;
        };

        properties.addListener(listener);
        properties.put("key", "value");

        assertThat(listenerCalled[0]).isTrue();
        assertThat(properties.size()).isEqualTo(1);
        assertThat(properties).isInstanceOf(ObservableMap.class);
    }

    private void checkRwMap(RWMapPropertiesState<String, String> state) {
        assertThat(state).isNotNull();
        var properties = state.getProperties();
        var modifProperties = state.getModifiableProperties();

        assertThat(properties).isNotNull();
        assertThat(modifProperties).isNotNull();

        assertThat(properties).isSameAs(state.getProperties());
        assertThat(modifProperties).isSameAs(state.getModifiableProperties());
        assertThat(properties).isNotSameAs(modifProperties);

        boolean[] listenerCalled = {false};
        var listener = (MapChangeListener<String, String>) (e) -> {
            listenerCalled[0] = true;
        };

        boolean[] modifListenerCalled = {false};
        var modifListener = (MapChangeListener<String, String>) (e) -> {
            modifListenerCalled[0] = true;
        };

        properties.addListener(listener);
        modifProperties.addListener(modifListener);

        assertThatThrownBy(() -> properties.put("key", "value"))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThat(listenerCalled[0]).isFalse();
        assertThat(modifListenerCalled[0]).isFalse();
        assertThat(properties.size()).isEqualTo(0);
        assertThat(modifProperties.size()).isEqualTo(0);

        modifProperties.put("key", "value");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(modifListenerCalled[0]).isTrue();
        assertThat(properties.size()).isEqualTo(1);
        assertThat(modifProperties.size()).isEqualTo(1);

        assertThat(properties).isInstanceOf(ObservableMap.class);
        assertThat(modifProperties).isInstanceOf(ObservableMap.class);
    }

    private void checkRoBoolean(ROBooleanSelectedState state) {
        // Test initial value (should be false)
        assertThat(state.isSelected()).isFalse();

        // Test property() method returns ReadOnlyBooleanProperty
        ReadOnlyBooleanProperty property = state.selectedProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyBooleanProperty.class);
        assertThat(property.get()).isFalse();

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Boolean>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isFalse();
            assertThat(newVal).isTrue();
        };
        property.addListener(listener);
        ((BooleanProperty) property).set(true);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(property.get()).isTrue();
        property.removeListener(listener);

        // Test binding
        SimpleBooleanProperty other = new SimpleBooleanProperty(false);
        ((BooleanProperty) property).bind(other);
        assertThat(property.get()).isFalse();

        other.set(true);
        assertThat(property.get()).isTrue();

        ((BooleanProperty) property).unbind();
    }

    private void checkBoolean(BooleanSelectedState state) {
        // Test initial value
        assertThat(state.isSelected()).isFalse();

        // Test property() method returns BooleanProperty
        BooleanProperty property = state.selectedProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(BooleanProperty.class);
        assertThat(property.get()).isFalse();

        // Test setter
        state.setSelected(true);
        assertThat(state.isSelected()).isTrue();
        assertThat(property.get()).isTrue();

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Boolean>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isTrue();
            assertThat(newVal).isFalse();
        };
        property.addListener(listener);

        state.setSelected(false);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(property.get()).isFalse();
        property.removeListener(listener);

        // Test binding
        SimpleBooleanProperty other = new SimpleBooleanProperty(true);
        property.bind(other);
        assertThat(property.get()).isTrue();

        other.set(false);
        assertThat(property.get()).isFalse();

        property.unbind();

        // Test getter/setter consistency
        state.setSelected(true);
        assertThat(state.isSelected()).isTrue();
        assertThat(property.get()).isTrue();

        property.set(false);
        assertThat(state.isSelected()).isFalse();
        assertThat(property.get()).isFalse();
    }

    private void checkRoInteger(ROIntegerLengthState state) {
        // Test initial value (should be 0)
        assertThat(state.getLength()).isEqualTo(0);

        // Test property() method returns ReadOnlyIntegerProperty
        ReadOnlyIntegerProperty property = state.lengthProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyIntegerProperty.class);
        assertThat(property.get()).isEqualTo(0);

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.intValue()).isEqualTo(0);
            assertThat(newVal.intValue()).isEqualTo(42);
        };
        property.addListener(listener);
        ((IntegerProperty) property).set(42);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getLength()).isEqualTo(42);
        assertThat(property.get()).isEqualTo(42);
        property.removeListener(listener);

        // Test binding
        SimpleIntegerProperty other = new SimpleIntegerProperty(10);
        ((IntegerProperty) property).bind(other);
        assertThat(state.getLength()).isEqualTo(10);
        assertThat(property.get()).isEqualTo(10);

        other.set(20);
        assertThat(state.getLength()).isEqualTo(20);
        assertThat(property.get()).isEqualTo(20);

        ((IntegerProperty) property).unbind();
    }

    private void checkInteger(IntegerLengthState state) {
        // Test initial value
        assertThat(state.getLength()).isEqualTo(0);

        // Test property() method returns IntegerProperty
        IntegerProperty property = state.lengthProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(IntegerProperty.class);
        assertThat(property.get()).isEqualTo(0);

        // Test setter
        state.setLength(42);
        assertThat(state.getLength()).isEqualTo(42);
        assertThat(property.get()).isEqualTo(42);

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.intValue()).isEqualTo(42);
            assertThat(newVal.intValue()).isEqualTo(100);
        };
        property.addListener(listener);

        state.setLength(100);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getLength()).isEqualTo(100);
        assertThat(property.get()).isEqualTo(100);
        property.removeListener(listener);

        // Test binding
        SimpleIntegerProperty other = new SimpleIntegerProperty(200);
        property.bind(other);
        assertThat(state.getLength()).isEqualTo(200);
        assertThat(property.get()).isEqualTo(200);

        other.set(300);
        assertThat(state.getLength()).isEqualTo(300);
        assertThat(property.get()).isEqualTo(300);

        property.unbind();

        // Test getter/setter consistency
        state.setLength(500);
        assertThat(state.getLength()).isEqualTo(500);
        assertThat(property.get()).isEqualTo(500);

        property.set(600);
        assertThat(state.getLength()).isEqualTo(600);
        assertThat(property.get()).isEqualTo(600);
    }

    private void checkRoDouble(RODoubleWidthState state) {
        // Test initial value (should be 0.0)
        assertThat(state.getWidth()).isEqualTo(0.0);

        // Test property() method returns ReadOnlyDoubleProperty
        ReadOnlyDoubleProperty property = state.widthProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyDoubleProperty.class);
        assertThat(property.get()).isEqualTo(0.0);

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.doubleValue()).isEqualTo(0.0);
            assertThat(newVal.doubleValue()).isEqualTo(3.14);
        };
        property.addListener(listener);
        ((DoubleProperty) property).set(3.14);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getWidth()).isEqualTo(3.14);
        assertThat(property.get()).isEqualTo(3.14);
        property.removeListener(listener);

        // Test binding
        SimpleDoubleProperty other = new SimpleDoubleProperty(1.5);
        ((DoubleProperty) property).bind(other);
        assertThat(state.getWidth()).isEqualTo(1.5);
        assertThat(property.get()).isEqualTo(1.5);

        other.set(2.5);
        assertThat(state.getWidth()).isEqualTo(2.5);
        assertThat(property.get()).isEqualTo(2.5);

        ((DoubleProperty) property).unbind();
    }

    private void checkDouble(DoubleWidthState state) {
        // Test initial value
        assertThat(state.getWidth()).isEqualTo(0.0);

        // Test property() method returns DoubleProperty
        DoubleProperty property = state.widthProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(DoubleProperty.class);
        assertThat(property.get()).isEqualTo(0.0);

        // Test setter
        state.setWidth(3.14);
        assertThat(state.getWidth()).isEqualTo(3.14);
        assertThat(property.get()).isEqualTo(3.14);

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.doubleValue()).isEqualTo(3.14);
            assertThat(newVal.doubleValue()).isEqualTo(2.71);
        };
        property.addListener(listener);

        state.setWidth(2.71);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getWidth()).isEqualTo(2.71);
        assertThat(property.get()).isEqualTo(2.71);
        property.removeListener(listener);

        // Test binding
        SimpleDoubleProperty other = new SimpleDoubleProperty(10.5);
        property.bind(other);
        assertThat(state.getWidth()).isEqualTo(10.5);
        assertThat(property.get()).isEqualTo(10.5);

        other.set(20.5);
        assertThat(state.getWidth()).isEqualTo(20.5);
        assertThat(property.get()).isEqualTo(20.5);

        property.unbind();

        // Test getter/setter consistency
        state.setWidth(100.1);
        assertThat(state.getWidth()).isEqualTo(100.1);
        assertThat(property.get()).isEqualTo(100.1);

        property.set(200.2);
        assertThat(state.getWidth()).isEqualTo(200.2);
        assertThat(property.get()).isEqualTo(200.2);
    }

    private void checkRoFloat(ROFloatXState state) {
        // Test initial value (should be 0.0f)
        assertThat(state.getX()).isEqualTo(0.0f);

        // Test property() method returns ReadOnlyFloatProperty
        ReadOnlyFloatProperty property = state.xProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyFloatProperty.class);
        assertThat(property.get()).isEqualTo(0.0f);

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.floatValue()).isEqualTo(0.0f);
            assertThat(newVal.floatValue()).isEqualTo(1.5f);
        };
        property.addListener(listener);
        ((FloatProperty) property).set(1.5f);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getX()).isEqualTo(1.5f);
        assertThat(property.get()).isEqualTo(1.5f);
        property.removeListener(listener);

        // Test binding
        SimpleFloatProperty other = new SimpleFloatProperty(2.5f);
        ((FloatProperty) property).bind(other);
        assertThat(state.getX()).isEqualTo(2.5f);
        assertThat(property.get()).isEqualTo(2.5f);

        other.set(3.5f);
        assertThat(state.getX()).isEqualTo(3.5f);
        assertThat(property.get()).isEqualTo(3.5f);

        ((FloatProperty) property).unbind();
    }

    private void checkFloat(FloatXState state) {
        // Test initial value
        assertThat(state.getX()).isEqualTo(0.0f);

        // Test property() method returns FloatProperty
        FloatProperty property = state.xProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(FloatProperty.class);
        assertThat(property.get()).isEqualTo(0.0f);

        // Test setter
        state.setX(1.5f);
        assertThat(state.getX()).isEqualTo(1.5f);
        assertThat(property.get()).isEqualTo(1.5f);

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.floatValue()).isEqualTo(1.5f);
            assertThat(newVal.floatValue()).isEqualTo(2.5f);
        };
        property.addListener(listener);

        state.setX(2.5f);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getX()).isEqualTo(2.5f);
        assertThat(property.get()).isEqualTo(2.5f);
        property.removeListener(listener);

        // Test binding
        SimpleFloatProperty other = new SimpleFloatProperty(10.1f);
        property.bind(other);
        assertThat(state.getX()).isEqualTo(10.1f);
        assertThat(property.get()).isEqualTo(10.1f);

        other.set(20.2f);
        assertThat(state.getX()).isEqualTo(20.2f);
        assertThat(property.get()).isEqualTo(20.2f);

        property.unbind();

        // Test getter/setter consistency
        state.setX(100.5f);
        assertThat(state.getX()).isEqualTo(100.5f);
        assertThat(property.get()).isEqualTo(100.5f);

        property.set(200.5f);
        assertThat(state.getX()).isEqualTo(200.5f);
        assertThat(property.get()).isEqualTo(200.5f);
    }

    private void checkRoLong(ROLongYState state) {
        // Test initial value (should be 0L)
        assertThat(state.getY()).isEqualTo(0L);

        // Test property() method returns ReadOnlyLongProperty
        ReadOnlyLongProperty property = state.yProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyLongProperty.class);
        assertThat(property.get()).isEqualTo(0L);

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.longValue()).isEqualTo(0L);
            assertThat(newVal.longValue()).isEqualTo(1000L);
        };
        property.addListener(listener);
        ((LongProperty) property).set(1000L);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getY()).isEqualTo(1000L);
        assertThat(property.get()).isEqualTo(1000L);
        property.removeListener(listener);

        // Test binding
        SimpleLongProperty other = new SimpleLongProperty(500L);
        ((LongProperty) property).bind(other);
        assertThat(state.getY()).isEqualTo(500L);
        assertThat(property.get()).isEqualTo(500L);

        other.set(1500L);
        assertThat(state.getY()).isEqualTo(1500L);
        assertThat(property.get()).isEqualTo(1500L);

        ((LongProperty) property).unbind();
    }

    private void checkLong(LongYState state) {
        // Test initial value
        assertThat(state.getY()).isEqualTo(0L);

        // Test property() method returns LongProperty
        LongProperty property = state.yProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(LongProperty.class);
        assertThat(property.get()).isEqualTo(0L);

        // Test setter
        state.setY(1000L);
        assertThat(state.getY()).isEqualTo(1000L);
        assertThat(property.get()).isEqualTo(1000L);

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<Number>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal.longValue()).isEqualTo(1000L);
            assertThat(newVal.longValue()).isEqualTo(2000L);
        };
        property.addListener(listener);

        state.setY(2000L);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getY()).isEqualTo(2000L);
        assertThat(property.get()).isEqualTo(2000L);
        property.removeListener(listener);

        // Test binding
        SimpleLongProperty other = new SimpleLongProperty(3000L);
        property.bind(other);
        assertThat(state.getY()).isEqualTo(3000L);
        assertThat(property.get()).isEqualTo(3000L);

        other.set(4000L);
        assertThat(state.getY()).isEqualTo(4000L);
        assertThat(property.get()).isEqualTo(4000L);

        property.unbind();

        // Test getter/setter consistency
        state.setY(5000L);
        assertThat(state.getY()).isEqualTo(5000L);
        assertThat(property.get()).isEqualTo(5000L);

        property.set(6000L);
        assertThat(state.getY()).isEqualTo(6000L);
        assertThat(property.get()).isEqualTo(6000L);
    }

    private void checkRoString(ROStringTitleState state) {
        // Test initial value (should be null)
        assertThat(state.getTitle()).isNull();

        // Test property() method returns ReadOnlyStringProperty
        ReadOnlyStringProperty property = state.titleProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyStringProperty.class);
        assertThat(property.get()).isNull();

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<String>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isNull();
            assertThat(newVal).isEqualTo("Hello");
        };
        property.addListener(listener);
        ((StringProperty) property).set("Hello");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getTitle()).isEqualTo("Hello");
        assertThat(property.get()).isEqualTo("Hello");
        property.removeListener(listener);

        // Test binding
        SimpleStringProperty other = new SimpleStringProperty("World");
        ((StringProperty) property).bind(other);
        assertThat(state.getTitle()).isEqualTo("World");
        assertThat(property.get()).isEqualTo("World");

        other.set("JavaFX");
        assertThat(state.getTitle()).isEqualTo("JavaFX");
        assertThat(property.get()).isEqualTo("JavaFX");

        ((StringProperty) property).unbind();
    }

    private void checkString(StringTitleState state) {
        // Test initial value
        assertThat(state.getTitle()).isNull();

        // Test property() method returns StringProperty
        StringProperty property = state.titleProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(StringProperty.class);
        assertThat(property.get()).isNull();

        // Test setter
        state.setTitle("Hello");
        assertThat(state.getTitle()).isEqualTo("Hello");
        assertThat(property.get()).isEqualTo("Hello");

        // Test listeners
        final boolean[] listenerCalled = {false};
        var listener = (ChangeListener<String>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isEqualTo("Hello");
            assertThat(newVal).isEqualTo("World");
        };
        property.addListener(listener);

        state.setTitle("World");
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getTitle()).isEqualTo("World");
        assertThat(property.get()).isEqualTo("World");
        property.removeListener(listener);

        // Test binding
        SimpleStringProperty other = new SimpleStringProperty("JavaFX");
        property.bind(other);
        assertThat(state.getTitle()).isEqualTo("JavaFX");
        assertThat(property.get()).isEqualTo("JavaFX");

        other.set("Binding");
        assertThat(state.getTitle()).isEqualTo("Binding");
        assertThat(property.get()).isEqualTo("Binding");

        property.unbind();

        // Test getter/setter consistency
        state.setTitle("Test");
        assertThat(state.getTitle()).isEqualTo("Test");
        assertThat(property.get()).isEqualTo("Test");

        property.set("Property");
        assertThat(state.getTitle()).isEqualTo("Property");
        assertThat(property.get()).isEqualTo("Property");
    }

    private void checkRoObject(ROObjectSideState<Side> state) {
        // Test initial value (should be null)
        assertThat(state.getSide()).isNull();

        // Test property() method returns ReadOnlyObjectProperty
        ReadOnlyObjectProperty<Side> property = state.sideProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ReadOnlyObjectProperty.class);
        assertThat(property.get()).isNull();

        // Test listeners work
        final boolean[] listenerCalled = {false};
        var testSide = Side.BOTTOM;
        var listener = (ChangeListener<Side>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isNull();
            assertThat(newVal).isEqualTo(testSide);
        };
        property.addListener(listener);
        ((ObjectProperty<Side>) property).set(testSide);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getSide()).isEqualTo(testSide);
        assertThat(property.get()).isEqualTo(testSide);
        property.removeListener(listener);

        // Test binding
        ObjectProperty<Side> other = new SimpleObjectProperty<>();
        ((ObjectProperty<Side>) property).bind(other);
        assertThat(state.getSide()).isEqualTo(null);
        assertThat(property.get()).isEqualTo(null);

        var anotherSide = Side.LEFT;
        other.set(anotherSide);
        assertThat(state.getSide()).isEqualTo(anotherSide);
        assertThat(property.get()).isEqualTo(anotherSide);

        ((ObjectProperty<Side>) property).unbind();
    }

    private void checkObject(ObjectSideState<Side> state) {
        // Test initial value
        assertThat(state.getSide()).isNull();

        // Test property() method returns ObjectProperty
        ObjectProperty<Side> property = state.sideProperty();
        assertThat(property).isNotNull();
        assertThat(property).isInstanceOf(ObjectProperty.class);
        assertThat(property.get()).isNull();

        // Test setter
        var testSide = Side.LEFT;
        state.setSide(testSide);
        assertThat(state.getSide()).isEqualTo(testSide);
        assertThat(property.get()).isEqualTo(testSide);

        // Test listeners
        final boolean[] listenerCalled = {false};
        var newSide = Side.BOTTOM;
        var listener = (ChangeListener<Object>) (observable, oldVal, newVal) -> {
            listenerCalled[0] = true;
            assertThat(oldVal).isEqualTo(testSide);
            assertThat(newVal).isEqualTo(newSide);
        };
        property.addListener(listener);

        state.setSide(newSide);
        assertThat(listenerCalled[0]).isTrue();
        assertThat(state.getSide()).isEqualTo(newSide);
        assertThat(property.get()).isEqualTo(newSide);
        property.removeListener(listener);

        // Test binding
        SimpleObjectProperty<Side> other = new SimpleObjectProperty<>(null);
        property.bind(other);
        assertThat(state.getSide()).isEqualTo(null);
        assertThat(property.get()).isEqualTo(null);

        var boundSide = Side.TOP;
        other.set(boundSide);
        assertThat(state.getSide()).isEqualTo(boundSide);
        assertThat(property.get()).isEqualTo(boundSide);

        property.unbind();

        // Test getter/setter consistency
        var consistencySide = Side.RIGHT;
        state.setSide(consistencySide);
        assertThat(state.getSide()).isEqualTo(consistencySide);
        assertThat(property.get()).isEqualTo(consistencySide);

        var anotherSide = Side.LEFT;
        property.set(anotherSide);
        assertThat(state.getSide()).isEqualTo(anotherSide);
        assertThat(property.get()).isEqualTo(anotherSide);
    }
}
