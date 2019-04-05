/*
 * Copyright 2013-2019 Julien Guerinet
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
 *
 */

package com.guerinet.weave.config

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

/**
 * Base parsed Config Json
 * @author Julien Guerinet
 * @since 5.0.0
 *
 * @param sources List of [Source]s the Strings are coming from
 * @param path Path to the file to write to
 * @param packageName Optional package name used on Android
 * @param typeColumnName Name of the column that holds the type
 * @param tagColumnName Name of the column that holds the tag
 * @param types Types that we should parse these into. Will typically be "Events" and "Screens"
 * @param tagsTabAlignment Number of tabs to do in order to align the tags. Defaults to 0 (unaligned)
 */
@Serializable
class AnalyticsConfig(
    val sources: List<Source> = listOf(),
    val path: String = "",
    @Optional val packageName: String? = null,
    @Optional val typeColumnName: String = "",
    val tagColumnName: String = "",
    val types: List<String> = listOf(),
    val tagsAlignColumn: Int = 0
)
