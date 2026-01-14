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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pavel Castornii
 */
class ScannerResult {

    static class MethodDescriptor {

        private final Method method;

        private boolean isSynchronized;

        MethodDescriptor(Method method) {
            this.method = method;
        }

        public boolean isSynchronized() {
            return isSynchronized;
        }

        public void setSynchronized(boolean isSynchronized) {
            this.isSynchronized = isSynchronized;
        }

        public Method getMethod() {
            return method;
        }
    }

    private final Class<?> type;

    private final Map<String, Method> properties = new HashMap<>();

    /**
     * Name without prefix "get".
     */
    private final Map<String, MethodDescriptor> listGetters = new HashMap<>();

    /**
     * Name without prefix "getModifiable".
     */
    private final Map<String, MethodDescriptor> modifiableListGetters = new HashMap<>();

    /**
     * Name without prefix "get".
     */
    private final Map<String, MethodDescriptor> setGetters = new HashMap<>();

    /**
     * Name without prefix "getModifiable".
     */
    private final Map<String, MethodDescriptor> modifiableSetGetters = new HashMap<>();

    /**
     * Name without prefix "get".
     */
    private final Map<String, MethodDescriptor> mapGetters = new HashMap<>();

    /**
     * Name without prefix "getModifiable".
     */
    private final Map<String, MethodDescriptor> modifiableMapGetters = new HashMap<>();

    // all other getters
    private final Map<String, Method> getters = new HashMap<>();

    // all other setters
    private final  Map<String, Method> setters = new HashMap<>();

    ScannerResult(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }

    public Map<String, Method> getProperties() {
        return properties;
    }

    public Map<String, MethodDescriptor> getListGetters() {
        return listGetters;
    }

    public Map<String, MethodDescriptor> getModifiableListGetters() {
        return modifiableListGetters;
    }

    public Map<String, MethodDescriptor> getSetGetters() {
        return setGetters;
    }

    public Map<String, MethodDescriptor> getModifiableSetGetters() {
        return modifiableSetGetters;
    }

    public Map<String, MethodDescriptor> getMapGetters() {
        return mapGetters;
    }

    public Map<String, MethodDescriptor> getModifiableMapGetters() {
        return modifiableMapGetters;
    }

    public Map<String, Method> getGetters() {
        return getters;
    }

    public Map<String, Method> getSetters() {
        return setters;
    }
}
