/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros2.internal.message;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.ros2.exception.RosMessageRuntimeException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class StringFileProvider {

    private final Set<File> directories;
    private final Map<File, String> stringMap;
    private final StringFileDirectoryWalker<?> stringFileDirectoryWalker;

    private final class StringFileDirectoryWalker<T> extends DirectoryWalker<T> {

        private final Set<File> directories;

        private StringFileDirectoryWalker(final FileFilter filter, final int depthLimit) {
            super(filter, depthLimit);
            directories = Sets.newHashSet();
        }

        // TODO(damonkohler): Update Apache Commons IO to the latest version.
        @Override
        protected final boolean handleDirectory(File directory, int depth, Collection<T> results)
                throws IOException {
            File canonicalDirectory = directory.getCanonicalFile();
            if (directories.contains(canonicalDirectory)) {
                return false;
            }
            directories.add(canonicalDirectory);
            return true;
        }

        @Override
        protected final void handleFile(File file, int depth, Collection<T> results) {

            try {
                final String content = FileUtils.readFileToString(file, "US-ASCII");
                stringMap.put(file, content);
            } catch (IOException e) {
                throw new RosMessageRuntimeException(e);
            }

        }

        public final void update(final File directory) {
            try {
                this.walk(directory, null);
            } catch (IOException e) {
                throw new RosMessageRuntimeException(e);
            }
        }
    }

    public StringFileProvider(final IOFileFilter ioFileFilter) {
        this.directories = new CopyOnWriteArraySet<>();
        this.stringMap = Maps.newConcurrentMap();
        final IOFileFilter directoryFilter = FileFilterUtils.directoryFileFilter();
        final FileFilter fileFilter = directoryFilter.or(ioFileFilter);
        this.stringFileDirectoryWalker = new StringFileDirectoryWalker<>(fileFilter, -1);
    }

    public final void update() {
        for (final File directory : this.directories) {
            this.stringFileDirectoryWalker.update(directory);
        }
    }

    /**
     * Adds a new directory to be scanned for topic definition files.
     *
     * @param directory the directory to add
     */
    public final void addDirectory(final File directory) {
        Preconditions.checkArgument(directory.isDirectory());
        this.directories.add(directory);
    }

    public final Map<File, String> getStringMap() {
        return ImmutableMap.copyOf(this.stringMap);
    }


}
