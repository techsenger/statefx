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

import com.techsenger.statefx.core.Synchronized;
import static com.techsenger.statefx.core.impl.Constants.BOOLEAN_GETTER_PREFIX;
import static com.techsenger.statefx.core.impl.Constants.GETTER_PREFIX;
import static com.techsenger.statefx.core.impl.Constants.MODIFIABLE_GETTER_PREFIX;
import static com.techsenger.statefx.core.impl.Constants.PROPERTY_POSTFIX;
import static com.techsenger.statefx.core.impl.Constants.SETTER_PREFIX;
import static com.techsenger.statefx.core.impl.NameUtils.isUppercaseAt;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

/**
 * Resolves methods by name and parameter count.
 *
 * @author Pavel Castornii
 */
final class InterfaceScanner {

    public static ScannerResult scan(Class<?> interfaceClass) {
        Set<Class<?>> allInterfaces = collectAllInterfaces(interfaceClass);
        var result = new ScannerResult(interfaceClass);

        for (Class<?> iface : allInterfaces) {
            for (Method method : iface.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) || method.isDefault()) {
                    continue;
                }
                String methodName = method.getName();
                var returnType = method.getReturnType();
                if (methodName.endsWith(PROPERTY_POSTFIX)) {
                    if (methodName.length() > PROPERTY_POSTFIX.length()
                            && method.getParameterCount() == 0
                            // filter ReadOnlyBooleanProperty fooProperty methods
                            && Property.class.isAssignableFrom(returnType)) {
                        result.getProperties().put(methodName, method);
                    }
                } else if (methodName.startsWith(MODIFIABLE_GETTER_PREFIX)) {
                    if (methodName.length() > MODIFIABLE_GETTER_PREFIX.length()
                            && method.getParameterCount() == 0
                            && isUppercaseAt(methodName, MODIFIABLE_GETTER_PREFIX.length())) {
                        addContainerGetter(result, method, true);
                    }
                } else if (methodName.startsWith(SETTER_PREFIX)) {
                    if (methodName.length() > SETTER_PREFIX.length()
                            && method.getParameterCount() == 1
                            && isUppercaseAt(methodName, SETTER_PREFIX.length())) {
                        result.getSetters().put(methodName, method);
                    }
                } else if (methodName.startsWith(GETTER_PREFIX)) {
                    if (methodName.length() > GETTER_PREFIX.length()
                            && method.getParameterCount() == 0
                            && isUppercaseAt(methodName, GETTER_PREFIX.length())) {
                        if (!addContainerGetter(result, method, false)) {
                            result.getGetters().put(methodName, method);
                        }
                    }
                } else if (methodName.startsWith(BOOLEAN_GETTER_PREFIX)) {
                    if (methodName.length() > BOOLEAN_GETTER_PREFIX.length()
                            && method.getParameterCount() == 0
                            && isUppercaseAt(methodName, BOOLEAN_GETTER_PREFIX.length())) {
                        result.getGetters().put(methodName, method);
                    }
                }
            }
        }
        return result;
    }

    private static boolean addContainerGetter(ScannerResult result, Method method, boolean modifiable) {
        var methodName = method.getName();
        var returnType = method.getReturnType();
        String name = null;
        if (modifiable) {
            name = methodName.substring(MODIFIABLE_GETTER_PREFIX.length());
        } else {
            name = methodName.substring(GETTER_PREFIX.length());
        }
        if (returnType == ObservableList.class) {
            if (modifiable) {
                addContainerGetter(result.getModifiableListGetters(), name, method);
            } else {
                addContainerGetter(result.getListGetters(), name, method);
            }
        } else if (returnType == ObservableMap.class) {
            if (modifiable) {
                addContainerGetter(result.getModifiableMapGetters(), name, method);
            } else {
                addContainerGetter(result.getMapGetters(), name, method);
            }
        } else if (returnType == ObservableSet.class) {
            if (modifiable) {
                addContainerGetter(result.getModifiableSetGetters(), name, method);
            } else {
                addContainerGetter(result.getSetGetters(), name, method);
            }
        } else {
            return false;
        }
        return true;
    }

    private static void addContainerGetter(Map<String, ScannerResult.MethodDescriptor> map, String name,
            Method method) {
        var newDescriptor = new ScannerResult.MethodDescriptor(method);
        var oldDescriptor = map.put(name, newDescriptor);
        if (oldDescriptor == null || !oldDescriptor.isSynchronized()) {
            if (method.getAnnotation(Synchronized.class) != null) {
                newDescriptor.setSynchronized(true);
            }
        } else {
            newDescriptor.setSynchronized(true);
        }
    }

    private static Set<Class<?>> collectAllInterfaces(Class<?> iface) {
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        collectAllInterfaces(iface, interfaces);
        return interfaces;
    }

    private static void collectAllInterfaces(Class<?> iface, Set<Class<?>> collected) {
        if (!collected.add(iface)) {
            return;
        }
        for (Class<?> parent : iface.getInterfaces()) {
            collectAllInterfaces(parent, collected);
        }
    }

    private InterfaceScanner() {
        // empty
    }
}
