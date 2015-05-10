/*
 * Copyright 2013-2015 Julien Guerinet
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

package com.guerinet.stringparser;

import java.util.HashMap;

/**
 * One String with all of the translations
 * @author Julien Guerinet
 * @version 2.4
 * @since 1.0
 */
public class LanguageString {
    /**
     * The key to store the String under
     */
    private String key;
    /**
     * A HashMap of translations, with the key being the language Id and the value being the String
     */
    private HashMap<String, String> translations;

    /**
     * Default Constructor
     *
     * @param key The String key
     */
    public LanguageString(String key){
        this.key = key;
        this.translations = new HashMap<>();
    }

    /* GETTERS */

    /**
     * @return The String Key
     */
    public String getKey(){
        return this.key;
    }

    /**
     * Get the String in a given language
     *
     * @param id The language Id
     * @return The String in that language
     */
    public String getString(String id){
        return this.translations.get(id);
    }

    /* SETTERS */

    /**
     * Add a translation
     *
     * @param id     The language Id
     * @param string The String
     */
    public void addTranslation(String id, String string){
        this.translations.put(id, string);
    }
}