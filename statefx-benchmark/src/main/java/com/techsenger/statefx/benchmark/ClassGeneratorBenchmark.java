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

package com.techsenger.statefx.benchmark;

import com.techsenger.statefx.core.Synchronized;
import com.techsenger.statefx.core.impl.ClassGenerator;
import com.techsenger.statefx.states.BooleanSelectedState;
import com.techsenger.statefx.states.DoubleWidthState;
import com.techsenger.statefx.states.DoubleXState;
import com.techsenger.statefx.states.DoubleYState;
import com.techsenger.statefx.states.IntegerIndexState;
import com.techsenger.statefx.states.IntegerLengthState;
import com.techsenger.statefx.states.ListItemsState;
import com.techsenger.statefx.states.MapPropertiesState;
import com.techsenger.statefx.states.ObjectSelectedItemState;
import com.techsenger.statefx.states.StringTitleState;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javafx.collections.ObservableList;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
public class ClassGeneratorBenchmark {

    public interface SomeState extends
            BooleanSelectedState,
            DoubleWidthState,
            StringTitleState,
            IntegerLengthState,
            DoubleXState,
            DoubleYState,
            MapPropertiesState<Object, Object>,
            ListItemsState<String>,
            ObjectSelectedItemState<String>,
            IntegerIndexState {

        @Override
        @Synchronized
        ObservableList<String> getItems();
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void generate(Blackhole blackhole) throws Exception {
        var clazz = ClassGenerator.generate(SomeState.class);
        blackhole.consume(clazz);
    }

    @Benchmark
    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    public void generateAndUse() throws Exception {
        var clazz = ClassGenerator.generate(SomeState.class);
        var state = clazz.getConstructor().newInstance();
        state.setSelected(true);
        state.setWidth(100.0);
        state.setTitle("title");
        state.setLength(10000);
        state.setX(200.15);
        state.setY(500.50);
        state.getProperties().put("key", "value");
        state.getItems().add("value");
        state.setSelectedItem("value");
        state.setIndex(50);
    }

    public static void main(String[] args) throws RunnerException, IOException {
        org.openjdk.jmh.Main.main(args);
    }
}
