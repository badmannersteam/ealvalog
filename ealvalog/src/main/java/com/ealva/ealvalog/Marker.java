/*
 * Copyright 2017 Eric A. Snell
 *
 * This file is part of eAlvaLog.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ealva.ealvalog;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Formattable;
import java.util.Iterator;

/**
 * A Marker is extra data passed to the underlying logging system and it's up to that implementation on if/how a Marker is used. Examples
 * might be that the Marker is output along with the log message or the Marker might be used to route the log message.
 *
 * Created by Eric A. Snell on 2/28/17.
 */
public interface Marker extends Serializable, Iterable<Marker>, Formattable {
  @NotNull String getName();

  boolean add(@NotNull Marker marker);

  boolean remove(@NotNull Marker marker);

  /**
   * @param marker determine if this is a child
   * @return true if this instance is {@code marker} or this instance has {@code marker} as a child
   */
  boolean isOrContains(@NotNull Marker marker);

  /**
   * @param markerName see if there is a child with this name
   * @return true if this instance is named {@code markerName} or this instance has a child named {@code markName}
   */
  boolean isOrContains(@NotNull String markerName);

  /**
   * @return an iterator over child markers
   */
  Iterator<Marker> iterator();

  /**
   * Essentially the same as {@code toString()} except the information is appended to the {@link StringBuilder} parameter. This is a
   * {@code toString()} variant that works with inheritance and contained objects that support this pattern.
   *
   * This method's contract is that it accepts a non-null builder, appends information to it, and then returns the same StringBuilder it
   * was passed as a parameter.
   *
   * @param builder the builder to append {@code toString()} information to
   * @param includeContained if string should include contained Markers
   * @return the {@code builder} parameter to allow call chaining
   */
  @NotNull StringBuilder toStringBuilder(@NotNull StringBuilder builder, final boolean includeContained);
}