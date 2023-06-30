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

package org.ros.internal.message;

import org.ros.exception.RosMessageRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public final class StringResourceProvider {

  private final Map<String, String> cache= new ConcurrentHashMap<>();


  public final String get(final String resourceName) {
    if (!has(resourceName)) {
      throw new NoSuchElementException("Resource does not exist: " + resourceName);
    }
    if (!cache.containsKey(resourceName)) {
      final InputStream in = getClass().getResourceAsStream(resourceName);
      final StringBuilder out = new StringBuilder();
      final Charset charset = Charset.forName("US-ASCII");
      final byte[] buffer = new byte[8192];
      try {
        for (int bytesRead; (bytesRead = in.read(buffer)) != -1;) {
          out.append(new String(buffer, 0, bytesRead, charset));
        }
      } catch (final IOException e) {
        throw new RosMessageRuntimeException("Failed to read resource: " + resourceName, e);
      }
      this.cache.put(resourceName, out.toString());
    }
    return this.cache.get(resourceName);
  }

  public final boolean has(final String resourceName) {
    return this.cache.containsKey(resourceName) || getClass().getResource(resourceName) != null;
  }

  public final void addStringToCache(final String resourceName,final  String resourceContent) {
    this.cache.put(resourceName, resourceContent);
  }
}
