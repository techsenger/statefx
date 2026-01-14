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

/**
 *
 * @author Pavel Castornii
 */
class ContainerMeta<T> extends AbstractMeta {

    private String capitalizedName;

    private Method getter;

    private Method modifiableGetter;

    private String factoryName;

    private String factoryDescriptor;

    private String roFactoryName;

    private String roFactoryDescriptor;

    private String syncFactoryName;

    private String syncFactoryDescriptor;

    private boolean isSynchronized;

    ContainerMeta(String name, String capitalizedName, Class<?> type) {
        super(name, type);
        this.capitalizedName = capitalizedName;
    }

    public String getCapitalizedName() {
        return capitalizedName;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getModifiableGetter() {
        return modifiableGetter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public void setModifiableGetter(Method modifiableGetter) {
        this.modifiableGetter = modifiableGetter;
    }

    public boolean isSynchronized() {
        return isSynchronized;
    }

    public void setSynchronized(boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public String getRoFactoryName() {
        return roFactoryName;
    }

    public void setRoFactoryName(String roFactoryName) {
        this.roFactoryName = roFactoryName;
    }

    public String getSyncFactoryName() {
        return syncFactoryName;
    }

    public void setSyncFactoryName(String syncFactoryName) {
        this.syncFactoryName = syncFactoryName;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getFactoryDescriptor() {
        return factoryDescriptor;
    }

    public void setFactoryDescriptor(String factoryDescriptor) {
        this.factoryDescriptor = factoryDescriptor;
    }

    public boolean isIsSynchronized() {
        return isSynchronized;
    }

    public void setIsSynchronized(boolean isSynchronized) {
        this.isSynchronized = isSynchronized;
    }

    public String getRoFactoryDescriptor() {
        return roFactoryDescriptor;
    }

    public void setRoFactoryDescriptor(String roFactoryDescriptor) {
        this.roFactoryDescriptor = roFactoryDescriptor;
    }

    public String getSyncFactoryDescriptor() {
        return syncFactoryDescriptor;
    }

    public void setSyncFactoryDescriptor(String syncFactoryDescriptor) {
        this.syncFactoryDescriptor = syncFactoryDescriptor;
    }
}
