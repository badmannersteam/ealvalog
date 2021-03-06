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

package com.ealva.ealvalog.core

import com.ealva.ealvalog.LogLevel
import com.ealva.ealvalog.Logger
import com.ealva.ealvalog.LoggerFilter

/**
 * Created by Eric A. Snell on 8/24/18.
 */
interface LoggerConfiguration<T: Bridge> {
  fun setLoggerFilter(logger: Logger, filter: LoggerFilter)
  fun setLogLevel(logger: Logger, logLevel: LogLevel)
  fun setLogToParent(logger: Logger, logToParent: Boolean)
  fun setIncludeLocation(logger: Logger, includeLocation: Boolean)
  fun getBridge(loggerClassName: String): T
}