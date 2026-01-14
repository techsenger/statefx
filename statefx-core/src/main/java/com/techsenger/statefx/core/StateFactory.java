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

package com.techsenger.statefx.core;

import com.techsenger.statefx.core.impl.ClassGenerator;

/**
 *
 * @author Pavel Castornii
 */
public final class StateFactory {

    private static final Cache cache = new Cache();

    /**
     * Returns the generated implementation <b>class</b> for the given state interface.
     * The class is generated on first call and cached for subsequent calls.
     *
     * @param interfaceClass the state interface class
     * @param <T> the state interface type
     * @return the generated implementation class
     */
    public static <T extends State> Class<? extends T> getImplementation(Class<T> interfaceClass) {
        if (interfaceClass == null) {
            throw new IllegalArgumentException("Interface class can't be null");
        }
        Class<? extends T> implClass = null;
        if (cache.isEnabled()) {
            implClass = (Class<? extends T>) cache.getMap().computeIfAbsent(interfaceClass, k -> {
                return ClassGenerator.generate(interfaceClass);
            });
        } else {
            implClass = ClassGenerator.generate(interfaceClass);
        }
        return implClass;
    }

    /**
     * Creates and returns a new <b>instance</b> of the state implementation. Each call returns a new instance.
     *
     * @param interfaceClass the state interface class
     * @param <T> the state interface type
     * @return a new instance of the state implementation
     * @throws InstantiationException if the state implementation cannot be instantiated.
     */
    public static <T extends State> T create(Class<T> interfaceClass) {
        var implClass = getImplementation(interfaceClass);
        try {
            return implClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new InstantiationException(
                "Failed to instantiate state: " + interfaceClass.getName(), e);
        }
    }

    /**
     * Returns the internal cache used by the factory.
     * <p>
     * The cache maps state interface classes to their generated implementations. It is thread-safe and uses
     * {@link java.util.concurrent.ConcurrentHashMap} internally.
     *
     * @return the factory's cache
     */
    public static Cache getCache() {
        return cache;
    }

    private StateFactory() {
        // empty
    }
}
