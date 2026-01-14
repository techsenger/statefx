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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender.Size;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Generates runtime implementations for StateFX interfaces using Byte Buddy.
 *
 * <p>The generator creates both fields and methods for JavaFX properties and observable containers without relying on
 * reflection at runtime.
 *
 * <p>Interceptor-based methods. Methods that simply expose an underlying field are implemented using standard
 * Byte Buddy interceptors:
 * <ul>
 *   <li>Container getters (lists, sets, maps) that directly return a field</li>
 *   <li>{@code fooProperty()} methods that return the property field itself</li>
 * </ul>
 * These methods are implemented via {@link net.bytebuddy.implementation.FieldAccessor}, as they do not require
 * any additional logic beyond loading and returning a field.
 *
 * <p>Bytecode-generated methods. The following elements are implemented using explicit {@link Implementation}
 * instances and custom bytecode:
 * <ul>
 *   <li>Initialization of property and container fields inside the generated constructor</li>
 *   <li>Property value getters (e.g. {@code getFoo()}, {@code isFoo()})</li>
 *   <li>Property value setters (e.g. {@code setFoo(...)})</li>
 * </ul>
 * This is required because JavaFX properties expose their values indirectly through the property instance itself.
 * Value access therefore involves invoking methods such as {@code get()}, {@code set(...)} or
 * {@code getValue()} on the property, rather than directly accessing a backing field.
 *
 * @author Pavel Castornii
 */
public final class ClassGenerator {

    private static class ContainerFieldInitializerBase {

        protected static final String FACTORY_OWNER = "javafx/collections/FXCollections";

        /**
         * Generates bytecode to call the factory method and create a collection.
         */
        protected static void generateFactoryCall(MethodVisitor mv, ContainerMeta<?> meta) {
            if (meta.getType() == ObservableSet.class) {
                // Create empty Object array for varargs
                mv.visitInsn(Opcodes.ICONST_0);  // array length = 0
                mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            }
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    FACTORY_OWNER,
                    meta.getFactoryName(),
                    meta.getFactoryDescriptor(),
                    false);
        }

        /**
         * Generates bytecode to wrap a collection with synchronized wrapper.
         */
        protected static void generateSyncWrapperCall(MethodVisitor mv, ContainerMeta<?> meta) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    FACTORY_OWNER,
                    meta.getSyncFactoryName(),
                    meta.getSyncFactoryDescriptor(),
                    false);
        }
    }

    /**
     * Creates bytecode implementation for initializing a single container field in constructor.
     */
    private static class ContainerFieldInitializer extends ContainerFieldInitializerBase implements Implementation {

        private final String fieldName;

        private final ContainerMeta<?> meta;

        ContainerFieldInitializer(String fieldName, ContainerMeta<?> meta) {
            this.fieldName = fieldName;
            this.meta = meta;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                String owner = target.getInstrumentedType().getInternalName();
                String fieldDescriptor = Type.getDescriptor(meta.getType());

                // aload 0 (this)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                // Create the collection based on synchronization setting
                if (meta.isSynchronized()) {
                    // Call factory to create base collection
                    generateFactoryCall(mv, meta);
                    // Wrap with synchronized wrapper
                    generateSyncWrapperCall(mv, meta);
                } else {
                    // Call factory directly
                    generateFactoryCall(mv, meta);
                }
                // Store in field: this.field = collection
                mv.visitFieldInsn(Opcodes.PUTFIELD, owner, fieldName, fieldDescriptor);

                return new Size(0, 0);
            };
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

    }

    /**
     * Creates bytecode implementation for initializing dual container fields in constructor.
     */
    private static final class WRContainerFieldInitializer extends ContainerFieldInitializerBase
            implements Implementation {

        /**
         * Generates bytecode to wrap a collection with read-only wrapper.
         */
        private static void generateReadOnlyWrapperCall(MethodVisitor mv, ContainerMeta<?> meta) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    FACTORY_OWNER,
                    meta.getRoFactoryName(),
                    meta.getRoFactoryDescriptor(),
                    false);
        }

        private final String modifiableFieldName;

        private final String roFieldName;

        private final ContainerMeta<?> meta;

        private WRContainerFieldInitializer(String modifiableFieldName, String roFieldName, ContainerMeta<?> meta) {
            this.modifiableFieldName = modifiableFieldName;
            this.roFieldName = roFieldName;
            this.meta = meta;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                String owner = target.getInstrumentedType().getInternalName();
                String fieldDesc = Type.getDescriptor(meta.getType());

                // Initialize modifiable collection field
                // aload 0 (this)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                // Create modifiable collection
                if (meta.isSynchronized()) {
                    // Call factory to create base collection
                    generateFactoryCall(mv, meta);
                    // Wrap with synchronized wrapper
                    generateSyncWrapperCall(mv, meta);
                } else {
                    // Call factory directly
                    generateFactoryCall(mv, meta);
                }
                // Store in modifiable field: this.modifiableField = collection
                mv.visitFieldInsn(Opcodes.PUTFIELD, owner, modifiableFieldName, fieldDesc);
                // Initialize read-only collection field
                // aload 0 (this)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                // Load modifiable field
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, owner, modifiableFieldName, fieldDesc);
                // Wrap with read-only wrapper
                generateReadOnlyWrapperCall(mv, meta);
                // Store in read-only field: this.roField = readOnlyCollection
                mv.visitFieldInsn(Opcodes.PUTFIELD, owner, roFieldName, fieldDesc);

                return new Size(0, 0);
            };
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }
    }

    /**
     * Bytecode implementation for initializing a property field in constructor.
     */
    private static class PropertyFieldInitializer implements Implementation {

        private final String fieldName;
        private final Class<?> propertyType;
        private final Class<?> propertyImplClass;

        PropertyFieldInitializer(String fieldName, Class<?> propertyType, Class<?> propertyImplClass) {
            this.fieldName = fieldName;
            this.propertyType = propertyType;
            this.propertyImplClass = propertyImplClass;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                String owner = target.getInstrumentedType().getInternalName();
                String fieldDescriptor = Type.getDescriptor(propertyType);

                // aload 0 (this)
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                // Create new property instance: new propertyImplClass()
                mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(propertyImplClass));
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    Type.getInternalName(propertyImplClass),
                    "<init>", "()V", false);
                // Store in field: this.field = property
                mv.visitFieldInsn(Opcodes.PUTFIELD, owner, fieldName, fieldDescriptor);

                return new Size(0, 0);
            };
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }
    }

    /**
     * Bytecode implementation of property value getter WITHOUT reflection.
     */
    private static class PropertyGetterImpl implements Implementation {
        private final PropertyMeta meta;
        private final Class<?> returnType;
        private final String fieldName;

        PropertyGetterImpl(PropertyMeta meta, String fieldName) {
            this.meta = meta;
            this.returnType = meta.getGetter().getReturnType();
            this.fieldName = fieldName;
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                String owner = target.getInstrumentedType().getInternalName();

                // Load the property field directly
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, owner, fieldName,
                    Type.getDescriptor(meta.getType()));
                if (meta.isReferenceType()) {
                    String observableOwner = Type.getInternalName(ObservableValue.class);
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                            observableOwner,
                            "getValue",
                            "()Ljava/lang/Object;",
                            true);

                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(returnType));
                    mv.visitInsn(Opcodes.ARETURN);

                    return new Size(4, 1);
                } else {
                    String propertyInternalName = Type.getInternalName(meta.getType());
                    boolean isInterface = meta.getType().isInterface();
                    String getDesc = meta.getGetDescriptor();
                    mv.visitMethodInsn(isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                            propertyInternalName,
                            "get",
                            getDesc,
                            isInterface);

                    int returnOp = meta.getGetterReturnOpcode();
                    mv.visitInsn(returnOp);

                    return new Size(4, 1);
                }
            };
        }
    }

    private static class PropertySetterImpl implements Implementation {
        private final PropertyMeta meta;
        private final Method setterMethod;
        private final String fieldName;

        PropertySetterImpl(PropertyMeta meta, String fieldName) {
            this.meta = meta;
            this.setterMethod = meta.getSetter();
            this.fieldName = fieldName;
        }

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                String owner = target.getInstrumentedType().getInternalName();

                // Load the property field directly
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, owner, fieldName,
                    Type.getDescriptor(meta.getType()));

                Class<?> paramType = setterMethod.getParameterTypes()[0];
                int loadOpcode = meta.getSetterLoadOpcode();
                mv.visitVarInsn(loadOpcode, 1);

                if (meta.isReferenceType()) {
                    String propertyOwner = Type.getInternalName(Property.class);
                    mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                            propertyOwner,
                            "setValue",
                            "(Ljava/lang/Object;)V",
                            true);
                } else {
                    String propertyInternalName = Type.getInternalName(meta.getType());
                    boolean isInterface = meta.getType().isInterface();
                    String setDesc = meta.getSetDescriptor();

                    mv.visitMethodInsn(isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                            propertyInternalName,
                            "set",
                            setDesc,
                            isInterface);
                }

                mv.visitInsn(Opcodes.RETURN);

                int maxLocals = 2;
                if (paramType == long.class || paramType == double.class) {
                    maxLocals = 3;
                }
                return new Size(6, maxLocals);
            };
        }
    }

    /**
     * Final bytecode fragment used to explicitly terminate the generated constructor with a {@code RETURN} instruction.
     * <p>
     * When multiple {@link Implementation} instances are combined using {@link Implementation.Compound}, their
     * bytecode is concatenated sequentially without any control-flow analysis. In this scenario, each initializer
     * contributes only a bytecode fragment and must <strong>not</strong> emit a {@code RETURN} instruction on its own.
     * <p>
     * However, the JVM verifier requires every constructor ({@code <init>}) to end with an explicit {@code RETURN}
     * instruction. Falling off the end of the method without a return results in a {@link VerifyError}.
     * <p>
     * This implementation is therefore appended as the <em>final</em> element in the {@link Implementation.Compound}
     * chain to ensure that the generated constructor is properly terminated, while still allowing all initializer
     * fragments to be executed sequentially.
     */
    private static final Implementation returnImpl = new Implementation() {

        @Override
        public InstrumentedType prepare(InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(Implementation.Target target) {
            return (mv, context, method) -> {
                mv.visitInsn(Opcodes.RETURN);
                return new Size(3, 1);
            };
        }
    };

    public static <T> Class<? extends T> generate(Class<T> interfaceClass) {
        var scannerResult = InterfaceScanner.scan(interfaceClass);
        var analyzerResult = MethodAnalyzer.analyze(scannerResult);

        DynamicType.Builder<?> builder = new ByteBuddy()
                .subclass(Object.class)
                .implement(interfaceClass)
                .name(generateClassName(interfaceClass));

        // Collect all initializers
        List<Implementation> initializers = new ArrayList<>();

        // Handle properties - only define fields and methods
        for (var meta : analyzerResult.getProperties()) {
            builder = implementProperty(builder, meta);
            // Add property initializer to list
            initializers.add(new PropertyFieldInitializer(
                meta.getName(),
                meta.getType(),
                meta.getImplType()
            ));
        }

        // Handle containers - only define fields and methods
        for (var meta: analyzerResult.getLists()) {
            builder = implementContainer(builder, meta);
            String fieldName = meta.getName();
            if (meta.getModifiableGetter() != null) {
                // For RW containers, use WRContainerImpl
                String modifiableFieldName = "modifiable" + meta.getCapitalizedName();
                initializers.add(new WRContainerFieldInitializer(modifiableFieldName, fieldName, meta));
            } else {
                initializers.add(new ContainerFieldInitializer(fieldName, meta));
            }
        }

        for (var meta: analyzerResult.getSets()) {
            builder = implementContainer(builder, meta);
            String fieldName = meta.getName();
            if (meta.getModifiableGetter() != null) {
                String modifiableFieldName = "modifiable" + meta.getCapitalizedName();
                initializers.add(new WRContainerFieldInitializer(modifiableFieldName, fieldName, meta));
            } else {
                initializers.add(new ContainerFieldInitializer(fieldName, meta));
            }
        }

        for (var meta: analyzerResult.getMaps()) {
            builder = implementContainer(builder, meta);
            String fieldName = meta.getName();
            if (meta.getModifiableGetter() != null) {
                String modifiableFieldName = "modifiable" + meta.getCapitalizedName();
                initializers.add(new WRContainerFieldInitializer(modifiableFieldName, fieldName, meta));
            } else {
                initializers.add(new ContainerFieldInitializer(fieldName, meta));
            }
        }

        initializers.add(returnImpl);

        // Add constructor ONCE with all initializers
        if (!initializers.isEmpty()) {
            builder = builder
                .constructor(ElementMatchers.isDefaultConstructor())
                .intercept(
                    SuperMethodCall.INSTANCE
                    .andThen(new Implementation.Compound(initializers))
                );
        }

        DynamicType.Unloaded<?> unloaded = builder.make();
        var clazz = unloaded.load(interfaceClass.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        return (Class<? extends T>) clazz;
    }

    private static String generateClassName(Class<?> interfaceClass) {
        String packageName = interfaceClass.getPackage() != null ? interfaceClass.getPackage().getName() + "." : "";
        String simpleName = interfaceClass.getSimpleName();
        return packageName + simpleName + "Impl$$ByteBuddy$$" + System.currentTimeMillis()
                + "_" + Integer.toHexString(interfaceClass.hashCode());
    }

    private static DynamicType.Builder<?> implementProperty(DynamicType.Builder<?> builder, PropertyMeta meta) {
        String fieldName = meta.getName();

        // Define field as FINAL (will be initialized in constructor)
        builder = builder.defineField(fieldName, meta.getType(),
            Visibility.PRIVATE, FieldManifestation.FINAL);

        // Property method (getProperty())
        if (meta.getMethod() != null) {
            Method pm = meta.getMethod();
            builder = builder.defineMethod(pm.getName(), pm.getReturnType(), Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(fieldName));
        }

        // Value getter method (is/get method)
        if (meta.getGetter() != null) {
            Method gm = meta.getGetter();
            builder = builder.defineMethod(gm.getName(), gm.getReturnType(), Visibility.PUBLIC)
                .intercept(new PropertyGetterImpl(meta, fieldName));
        }

        // Setter method
        if (meta.getSetter() != null) {
            Method sm = meta.getSetter();
            builder = builder.defineMethod(sm.getName(), sm.getReturnType(), Visibility.PUBLIC)
                .withParameter(sm.getParameterTypes()[0], "value")
                .intercept(new PropertySetterImpl(meta, fieldName));
        }

        return builder;
    }

    private static DynamicType.Builder<?> implementContainer(DynamicType.Builder<?> builder, ContainerMeta<?> meta) {
        boolean hasModifiableMethod = meta.getModifiableGetter() != null;

        if (!hasModifiableMethod) {
            // Case 1: Without RW interface - only one getter and one FINAL field
            builder = builder.defineField(meta.getName(), meta.getType(),
                Visibility.PRIVATE, FieldManifestation.FINAL);

            // Generate getter
            Method getter = meta.getGetter();
            builder = builder.defineMethod(getter.getName(), getter.getReturnType(), Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(meta.getName()));

        } else {
            // Case 2: With RW interface - two getters and two FINAL fields
            String modifiableFieldName = "modifiable" + meta.getCapitalizedName();

            builder = builder
                .defineField(modifiableFieldName, meta.getType(),
                    Visibility.PRIVATE, FieldManifestation.FINAL)
                .defineField(meta.getName(), meta.getType(),
                    Visibility.PRIVATE, FieldManifestation.FINAL);

            // Generate standard getter (returns read-only collection)
            Method getter = meta.getGetter();
            builder = builder.defineMethod(getter.getName(), getter.getReturnType(), Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(meta.getName()));

            // Generate modifiable getter
            Method modifiableGetter = meta.getModifiableGetter();
            builder = builder.defineMethod(modifiableGetter.getName(),
                    modifiableGetter.getReturnType(), Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField(modifiableFieldName));
        }

        return builder;
    }

    private ClassGenerator() {
        // empty
    }
}
