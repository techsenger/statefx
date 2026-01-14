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

package com.techsenger.statefx.core.impl;

import com.techsenger.statefx.core.impl.ScannerResult.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import net.bytebuddy.jar.asm.Opcodes;

/**
 *
 * @author Pavel Castornii
 */
final class MethodAnalyzer {

    private static final Map<Class<?>, BiFunction<ScannerResult, Method, PropertyMeta>> analyzersByType =
            Map.ofEntries(
                    Map.entry(BooleanProperty.class, MethodAnalyzer::analyzeBooleanProperty),
                    Map.entry(ReadOnlyBooleanProperty.class, MethodAnalyzer::analyzeBooleanProperty),

                    Map.entry(StringProperty.class, MethodAnalyzer::analyzeStringProperty),
                    Map.entry(ReadOnlyStringProperty.class, MethodAnalyzer::analyzeStringProperty),

                    Map.entry(IntegerProperty.class, MethodAnalyzer::analyzeIntegerProperty),
                    Map.entry(ReadOnlyIntegerProperty.class, MethodAnalyzer::analyzeIntegerProperty),

                    Map.entry(DoubleProperty.class, MethodAnalyzer::analyzeDoubleProperty),
                    Map.entry(ReadOnlyDoubleProperty.class, MethodAnalyzer::analyzeDoubleProperty),

                    Map.entry(ObjectProperty.class, MethodAnalyzer::analyzeObjectProperty),
                    Map.entry(ReadOnlyObjectProperty.class, MethodAnalyzer::analyzeObjectProperty),

                    Map.entry(LongProperty.class, MethodAnalyzer::analyzeLongProperty),
                    Map.entry(ReadOnlyLongProperty.class, MethodAnalyzer::analyzeLongProperty),

                    Map.entry(FloatProperty.class, MethodAnalyzer::analyzeFloatProperty),
                    Map.entry(ReadOnlyFloatProperty.class, MethodAnalyzer::analyzeFloatProperty)
            );

    public static AnalyzerResult analyze(ScannerResult scannerResult) {
        List<ContainerMeta<ObservableList<?>>> lists = new ArrayList<>();
        List<ContainerMeta<ObservableSet<?>>> sets = new ArrayList<>();
        List<ContainerMeta<ObservableMap<?, ?>>> maps = new ArrayList<>();

        for (var entry : scannerResult.getListGetters().entrySet()) {
            var meta = MethodAnalyzer.<ObservableList<?>>createContainer(entry,
                    scannerResult.getModifiableListGetters(), ObservableList.class);
            meta.setFactoryName("observableArrayList");
            meta.setFactoryDescriptor("()Ljavafx/collections/ObservableList;");
            meta.setRoFactoryName("unmodifiableObservableList");
            meta.setRoFactoryDescriptor("(Ljavafx/collections/ObservableList;)Ljavafx/collections/ObservableList;");
            meta.setSyncFactoryName("synchronizedObservableList");
            meta.setSyncFactoryDescriptor("(Ljavafx/collections/ObservableList;)Ljavafx/collections/ObservableList;");
            lists.add(meta);
        }

        for (var entry : scannerResult.getSetGetters().entrySet()) {
            var meta = MethodAnalyzer.<ObservableSet<?>>createContainer(entry,
                    scannerResult.getModifiableSetGetters(), ObservableSet.class);
            meta.setFactoryName("observableSet");
            meta.setFactoryDescriptor("([Ljava/lang/Object;)Ljavafx/collections/ObservableSet;");
            meta.setRoFactoryName("unmodifiableObservableSet");
            meta.setRoFactoryDescriptor("(Ljavafx/collections/ObservableSet;)Ljavafx/collections/ObservableSet;");
            meta.setSyncFactoryName("synchronizedObservableSet");
            meta.setSyncFactoryDescriptor("(Ljavafx/collections/ObservableSet;)Ljavafx/collections/ObservableSet;");
            sets.add(meta);
        }

        for (var entry : scannerResult.getMapGetters().entrySet()) {
            var meta = MethodAnalyzer.<ObservableMap<?, ?>>createContainer(entry,
                    scannerResult.getModifiableMapGetters(), ObservableMap.class);
            meta.setFactoryName("observableHashMap");
            meta.setFactoryDescriptor("()Ljavafx/collections/ObservableMap;");
            meta.setRoFactoryName("unmodifiableObservableMap");
            meta.setRoFactoryDescriptor("(Ljavafx/collections/ObservableMap;)Ljavafx/collections/ObservableMap;");
            meta.setSyncFactoryName("synchronizedObservableMap");
            meta.setSyncFactoryDescriptor("(Ljavafx/collections/ObservableMap;)Ljavafx/collections/ObservableMap;");
            maps.add(meta);
        }

        var result = new AnalyzerResult(analyzeProperties(scannerResult), lists, sets, maps);
        return result;
    }

    private static List<PropertyMeta> analyzeProperties(ScannerResult scannerResult) {
        List<PropertyMeta> result = new ArrayList<>();
        for (var method : scannerResult.getProperties().values()) {
            var meta = analyzeProperty(scannerResult, method);
            result.add(meta);
        }
        return result;
    }

    private static <T> ContainerMeta<T> createContainer(Map.Entry<String, MethodDescriptor> entry,
        Map<String, MethodDescriptor> modifiableGetter, Class<?> type) {
        var descriptor = entry.getValue();
        var modifDescriptor = modifiableGetter.get(entry.getKey());
        var meta = new ContainerMeta<>(NameUtils.firstToLowerCase(entry.getKey()), entry.getKey(), type);
        meta.setGetter(descriptor.getMethod());
        meta.setSynchronized(descriptor.isSynchronized());
        if (modifDescriptor != null) {
            meta.setModifiableGetter(modifDescriptor.getMethod());
            meta.setSynchronized(modifDescriptor.isSynchronized());
        }

        return (ContainerMeta<T>) meta;
    }

    private static PropertyMeta analyzeProperty(ScannerResult scannerResult, Method method) {
        var function = analyzersByType.get(method.getReturnType());
        var meta = function.apply(scannerResult, method);
        String capitalized = Character.toUpperCase(meta.getName().charAt(0)) + meta.getName().substring(1);
        meta.setMethod(method);

        Method getter = null;
        if (ReadOnlyBooleanProperty.class.isAssignableFrom(method.getReturnType())) {
            getter = scannerResult.getGetters().get(Constants.BOOLEAN_GETTER_PREFIX + capitalized);
        } else {
            getter = scannerResult.getGetters().get(Constants.GETTER_PREFIX + capitalized);
        }
        meta.setGetter(getter);

        Method setter = scannerResult.getSetters().get(Constants.SETTER_PREFIX + capitalized);
        meta.setSetter(setter);

        if (getter == null) {
            throw new RuntimeException("No getter for "
                    + scannerResult.getType().getName() + "#" + method.getName());
        }
        if (setter == null) {
            throw new RuntimeException("No setter for "
                    + scannerResult.getType().getName() + "#" + method.getName());
        }
        return meta;
    }

    private static PropertyMeta analyzeStringProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), StringProperty.class, SimpleStringProperty.class);
        meta.setSetDescriptor("(Ljava/lang/Object;)V");
        meta.setGetDescriptor("()Ljava/lang/Object;");
        meta.setReferenceType(true);
        meta.setGetterReturnOpcode(Opcodes.ARETURN);
        meta.setSetterLoadOpcode(Opcodes.ALOAD);
        return meta;
    }

    private static PropertyMeta analyzeObjectProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), ObjectProperty.class, SimpleObjectProperty.class);
        meta.setSetDescriptor("(Ljava/lang/Object;)V");
        meta.setGetDescriptor("()Ljava/lang/Object;");
        meta.setReferenceType(true);
        meta.setGetterReturnOpcode(Opcodes.ARETURN);
        meta.setSetterLoadOpcode(Opcodes.ALOAD);
        return meta;
    }

    private static PropertyMeta analyzeBooleanProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), BooleanProperty.class, SimpleBooleanProperty.class);
        meta.setSetDescriptor("(Z)V");
        meta.setGetDescriptor("()Z");
        meta.setGetterReturnOpcode(Opcodes.IRETURN);
        meta.setSetterLoadOpcode(Opcodes.ILOAD);
        return meta;
    }

    private static PropertyMeta analyzeIntegerProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), IntegerProperty.class, SimpleIntegerProperty.class);
        meta.setSetDescriptor("(I)V");
        meta.setGetDescriptor("()I");
        meta.setGetterReturnOpcode(Opcodes.IRETURN);
        meta.setSetterLoadOpcode(Opcodes.ILOAD);
        return meta;
    }

    private static PropertyMeta analyzeDoubleProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), DoubleProperty.class, SimpleDoubleProperty.class);
        meta.setSetDescriptor("(D)V");
        meta.setGetDescriptor("()D");
        meta.setGetterReturnOpcode(Opcodes.DRETURN);
        meta.setSetterLoadOpcode(Opcodes.DLOAD);
        return meta;
    }

    private static PropertyMeta analyzeLongProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), LongProperty.class, SimpleLongProperty.class);
        meta.setSetDescriptor("(J)V");
        meta.setGetDescriptor("()J");
        meta.setGetterReturnOpcode(Opcodes.LRETURN);
        meta.setSetterLoadOpcode(Opcodes.LLOAD);
        return meta;
    }

    private static PropertyMeta analyzeFloatProperty(ScannerResult scannerResult, Method method) {
        var meta = new PropertyMeta(resolvePropertyName(method), FloatProperty.class, SimpleFloatProperty.class);
        meta.setSetDescriptor("(F)V");
        meta.setGetDescriptor("()F");
        meta.setGetterReturnOpcode(Opcodes.FRETURN);
        meta.setSetterLoadOpcode(Opcodes.FLOAD);
        return meta;
    }

    private static String resolvePropertyName(Method method) {
        var methodName = method.getName();
        String name = methodName.substring(0, methodName.length() - Constants.PROPERTY_POSTFIX.length());
        return name;
    }

    private MethodAnalyzer() {
        // empty
    }
}
