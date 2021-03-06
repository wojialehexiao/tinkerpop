/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.groovy.jsr223;

import org.apache.tinkerpop.gremlin.jsr223.Customizer;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class GroovyCompilerGremlinPluginTest {

    @Test
    public void shouldConfigureForGroovyOnly() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                compilation(GroovyCompilerGremlinPlugin.Compilation.COMPILE_STATIC).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-not-real");
        assertThat(customizers.isPresent(), is(false));
    }

    @Test
    public void shouldConfigureWithCompileStatic() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                compilation(GroovyCompilerGremlinPlugin.Compilation.COMPILE_STATIC).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof CompileStaticGroovyCustomizer).count());
    }

    @Test
    public void shouldConfigureWithTypeChecked() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                compilation(GroovyCompilerGremlinPlugin.Compilation.TYPE_CHECKED).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof TypeCheckedGroovyCustomizer).count());
    }

    @Test
    public void shouldConfigureWithCustomCompilerConfigurations() {
        final Map<String,Object> conf = new HashMap<>();
        conf.put("Debug", true);
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                compilerConfigurationOptions(conf).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof ConfigurationGroovyCustomizer).count());

        final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
        assertThat(compilerConfiguration.getDebug(), is(false));

        final ConfigurationGroovyCustomizer provider = (ConfigurationGroovyCustomizer) customizers.get()[0];
        provider.applyCustomization(compilerConfiguration);

        assertThat(compilerConfiguration.getDebug(), is(true));
    }

    @Test
    public void shouldConfigureWithInterpreterMode() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                enableInterpreterMode(true).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof InterpreterModeGroovyCustomizer).count());
    }

    @Test
    public void shouldConfigureWithThreadInterrupt() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                enableThreadInterrupt(true).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof ThreadInterruptGroovyCustomizer).count());
    }

    @Test
    public void shouldConfigureWithTimedInterrupt() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                timedInterrupt(60000).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, Stream.of(customizers.get()).filter(c -> c instanceof TimedInterruptGroovyCustomizer).count());
    }

    @Test
    public void shouldConfigureWithCompilationOptions() {
        final GroovyCompilerGremlinPlugin plugin = GroovyCompilerGremlinPlugin.build().
                classMapCacheSpecification("initialCapacity=1000,maximumSize=1000").
                expectedCompilationTime(30000).create();
        final Optional<Customizer[]> customizers = plugin.getCustomizers("gremlin-groovy");
        assertThat(customizers.isPresent(), is(true));
        assertEquals(1, customizers.get().length);
        assertThat(customizers.get()[0], instanceOf(CompilationOptionsCustomizer.class));
        assertEquals(30000, ((CompilationOptionsCustomizer) customizers.get()[0]).getExpectedCompilationTime());
        assertEquals("initialCapacity=1000,maximumSize=1000", ((CompilationOptionsCustomizer) customizers.get()[0]).getClassMapCacheSpecification());
    }
}
