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

package com.techsenger.statefx.mavenplugin;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Pavel Castornii
 */
@Mojo(name = "generate-interfaces")
public class GenerateInterfacesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(required = true)
    private String path;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            var preparedPath = preparePath();
            var scanner = new ClassScanner();
            scanner.scan();
            AbstractMeta meta = null;
            try {
                var properties = scanner.getProperties();
                var collections = scanner.getCollections();
                var maps = scanner.getMaps();
                for (var property : properties) {
                    meta = property;
                    StateGenerator.generateProperty(preparedPath, property);
                }
                for (var collection: collections) {
                    meta = collection;
                    StateGenerator.generateCollection(preparedPath, collection);
                }
                for (var map : maps) {
                    meta = map;
                    StateGenerator.generateMap(preparedPath, map);
                }
                getLog().info("Generated "
                        + ((properties.size() + maps.size() + collections.size()) * 2) + " interfaces");
            } catch (Exception ex) {
                throw new Exception("Couldn't generate interfaces for " + meta.getOwnerType().getName()
                + "#" + meta.getName(), ex);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("Couldn't generate interfaces", ex);
        }
    }

    private Path preparePath() throws IOException {
        Path preparedPath = Paths.get(path);
        if (Files.exists(preparedPath)) {
            try (Stream<Path> files = Files.list(preparedPath)) {
                files.filter(Files::isRegularFile).forEach(file -> {
                     try {
                         Files.delete(file);
                     } catch (IOException e) {
                         throw new UncheckedIOException("Failed to delete: " + file, e);
                     }
                 });
            }
        } else {
            Files.createDirectories(preparedPath);
        }
        getLog().info("Prepared path for interfaces: " + preparedPath);
        return preparedPath;
    }
}

