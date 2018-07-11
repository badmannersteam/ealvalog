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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents the logging level
 * <p>
 * Created by Eric A. Snell on 2/28/17.
 */
@SuppressWarnings("unused") public enum LogLevel {
  ALL(Integer.MIN_VALUE, java.util.logging.Level.ALL),
  TRACE(1000, java.util.logging.Level.FINEST),
  DEBUG(2000, java.util.logging.Level.FINER),
  INFO(3000, java.util.logging.Level.FINE),
  WARN(4000, java.util.logging.Level.WARNING),
  ERROR(5000, java.util.logging.Level.SEVERE),
  CRITICAL(6000, new Level("CRITICAL", 1100) {}),
  NONE(Integer.MAX_VALUE, java.util.logging.Level.OFF);

  private static final Map<Level, LogLevel> levelToLogLevelMap;

  static {
    levelToLogLevelMap = new HashMap<>(8);
    final LogLevel[] logLevels = values();
    for (LogLevel logLevel : logLevels) {
      levelToLogLevelMap.put(logLevel.getJdkLevel(), logLevel);
    }
  }

  private final int value;
  private final Level level;

  public static @NotNull LogLevel fromLevel(@NotNull final Level level) {
    final LogLevel logLevel = levelToLogLevelMap.get(level);
    return logLevel == null ? NONE : logLevel;
  }

  LogLevel(final int value, final Level level) {
    this.value = value;
    this.level = level;
  }

  public Level getJdkLevel() {
    return level;
  }

  public boolean shouldNotLogAtLevel(final int jdkLevel) {
    return value < jdkLevel || jdkLevel == Level.OFF.intValue();
  }

  public boolean isAtLeast(final @NotNull LogLevel level) {
    return value >= level.value;
  }
}