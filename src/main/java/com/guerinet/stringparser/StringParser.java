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

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class, executes the main code for parsing the Google Docs file
 * @author Julien Guerinet
 * @version 2.4
 * @since 1.0
 */
public class StringParser{
    /* FILE STRINGS */
    /**
     * The URL in the file
     */
    private static final String URL = "URL:";
    /**
     * The platform in the file
     */
    private static final String PLATFORM = "Platform:";
    /**
     * Languages in the file
     */
    private static final String LANGUAGE= "Language:";

    /* ANDROID STRINGS */
    /**
     * Android XML Opener
     */
    private static final String XML_OPENER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
    /**
     * Android Resources Opener
     */
    private static final String RESOURCES_OPENER = "<resources>";
    /**
     * Android Resources Closer
     */
    private static final String RESOURCES_CLOSER = "</resources>";

    /* OTHER */
    /**
     * The English Id, used to get the English Strings for the header
     */
    private static final String ENGLISH_ID = "en";
    /**
     * The key used for the header in the Strings document
     */
    private static final String HEADER_KEY = "header";

    public static void main(String[] args) throws IOException {
        //Keep a list of all of the languages the Strings are in
        List<Language> languages = new ArrayList<>();
        //The list of language Strings
        List<LanguageString> strings = new ArrayList<>();
        //Url
        String url = null;
        //True if it's for Android, false if it's for iOS
        Boolean android = null;

        //Read from the config file
        BufferedReader configReader = null;
        try{
            configReader = new BufferedReader(new FileReader("../config.txt"));
        }
        catch(FileNotFoundException e){
            try{
                configReader = new BufferedReader(new FileReader("config.txt"));
            }
            catch(FileNotFoundException ex){
                System.out.println("Error: Config file not found");
                System.exit(-1);
            }
        }

        String line;
        while ((line = configReader.readLine()) != null) {
            //Get the URL
            if(line.startsWith(URL)){
                url = line.replace(URL, "").trim();
            }
            //Get the platform
            else if(line.startsWith(PLATFORM)){
                //Remove the header
                String platformString = line.replace(PLATFORM, "").trim();
                //Android
                if(platformString.equalsIgnoreCase("android")){
                    android = true;
                }
                //iOS
                else if(platformString.equalsIgnoreCase("ios")){
                    android = false;
                }
                //Not recognized
                else{
                    System.out.println("Error: Platform must be either Android or iOS.");
                    System.exit(-1);
                }
            }
            //Get the languages
            else if(line.startsWith(LANGUAGE)){
                //Remove the header and separate the language Id from the path
                String languageString= line.replace(LANGUAGE, "").trim();
                String[] languageInfo = languageString.split(", ");

                if(languageInfo.length != 2){
                    System.out.println("Error: The following format has too few or too many " +
                        "arguments for a language: " + languageString);
                    System.exit(-1);
                }

                //Save it as a new language in the list of languages
                languages.add(new Language(languageInfo[0], languageInfo[1]));
            }
        }
        configReader.close();

        //Make sure nothing is null
        if(url == null){
            System.out.println("Error: URL Cannot be null");
            System.exit(-1);
        }
        else if(android == null){
            System.out.println("Error: You need to input a platform");
            System.exit(-1);
        }
        else if(languages.isEmpty()){
            System.out.println("Error: You need to add at least one language");
            System.exit(-1);
        }

        //Connect to the URL
        System.out.println("Connecting to " + url);
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();

        int responseCode = response.code();
        System.out.println("Response Code: " + responseCode);

        if(responseCode == 200){
            //Set up the CSV reader
            CsvListReader reader = new CsvListReader(new InputStreamReader(
                    response.body().byteStream(), "UTF-8"), CsvPreference.EXCEL_PREFERENCE);

            //Get the header
            final String[] header = reader.getHeader(true);

            //First column will be key, so ignore it
            for(int i = 1; i < header.length; i++){
                String string = header[i];

                //Check if the string matches any of the languages parsed
                for(Language language : languages){
                    if(string.equals(language.getId())){
                        //If we find a match, set the column index for this language
                        language.setColumnIndex(i);
                        break;
                    }
                }
            }

            //Make sure that all languages have an index
            for(Language language : languages){
                if(language.getColumnIndex() == -1){
                    System.out.println("Error: " + language.getId() +
                            " does not have any translations.");
                    System.exit(-1);
                }
            }

            //Make a CellProcessor with the right length
            final CellProcessor[] processors = new CellProcessor[header.length];

            //Go through each line of the CSV document into a list of objects.
            List<Object> currentLine;
            while((currentLine = reader.read(processors)) != null){
                //Add a new language String
                LanguageString languageString =
                        new LanguageString(((String)currentLine.get(0)).trim());

                //Go through the languages, add each translation
                boolean allNull = true;
                for(Language language : languages){
                    languageString.addTranslation(language.getId(),
                            (String)currentLine.get(language.getColumnIndex()));

                    //If at least one language is not null, then they are not all null
                    if(languageString.getString(language.getId()) != null){
                        allNull = false;
                    }
                }

                //Check if all of the values are null
                if(allNull){
                    //Show a warning message
                    System.out.println("Warning: Line " + (strings.size() + 2) + " has no " +
                            "translations so it will not be parsed.");
                }
                else{
                    strings.add(languageString);
                }
            }

            //Close the CSV reader
            reader.close();

            //Check if there are any errors with the keys
            for (int i = 0; i < strings.size(); i++){
                LanguageString string1 = strings.get(i);

                //Check if there are any spaces in the keys
                if(string1.getKey().contains(" ")){
                    System.out.println("Error: Line " + getLineNumber(string1, strings) +
                            " contains a space in its key.");
                    System.exit(-1);
                }

                //Check if there are any duplicated
                for(int j = i + 1; j < strings.size(); j++){
                    LanguageString string2 = strings.get(j);

                    //If the keys are the same and it's not a header, show an error and stop
                    if(!string1.getKey().equalsIgnoreCase(HEADER_KEY) &&
                            string1.getKey().equals(string2.getKey())){
                        System.out.println("Error: Lines " + getLineNumber(string1, strings) +
                                " and " + getLineNumber(string2, strings) + " have the same key.");
                        System.exit(-1);
                    }
                }
            }

            //Go through each language, and write the file
            PrintWriter writer;
            for(Language language : languages){
                //Set up the writer for the given language, enforcing UTF-8
                writer = new PrintWriter(language.getPath(), "UTF-8");

                if(android){
                    processAndroidStrings(writer, language, strings);
                }
                else{
                    processIOSStrings(writer, language, strings);
                }

                System.out.println("Wrote " + language.getId() + " to file: " + language.getPath());

                writer.close();
            }

            //Exit message
            System.out.println("Strings parsing complete");
        }
        else{
            System.out.println("Error: Response Code not 200");
            System.out.println("Response Message: " + response.message());
        }
    }

    /* HELPERS */

    /**
     * Get the line number of a given String for any warnings or errors shown to the user
     *
     * @param string The String
     * @return The line number of the String
     */
    private static int getLineNumber(LanguageString string, List<LanguageString> strings){
        //+2 to account for the header and the fact that Google Drive starts numbering at 1
        return strings.indexOf(string) + 2;
    }

    /**
     * Processes a given String with the common changes to make between the platforms
     *
     * @param string The String to process
     */
    private static String processString(String string){
        //Unescaped quotations
        string = string.replace("\"", "\\" + "\"");

        //Copyright
        string = string.replace("(c)", "\u00A9");

        //New Lines
        string = string.replace("\n", "");

        return string;
    }

    /**
     * Add a language String
     *
     * @param android  True if this is for Android, false if this is for iOS
     * @param string   The LanguageString object
     * @param language The language to parse the String for
     * @return The formatted String for the given language and platform
     */
    private static String getLanguageString(boolean android, LanguageString string,
                                            Language language){
        String key = string.getKey();
        String value;
        //Check if we are parsing a header, use the English translation for the value
        if(string.getKey().equalsIgnoreCase(HEADER_KEY)){
            value = string.getString(ENGLISH_ID);
        }
        else{
            value = string.getString(language.getId());
        }

        //Check if value is or null empty: if it is, return null
        if(value == null || value.isEmpty()){
            return null;
        }

        //Process the value with the general methods first
        value = processString(value);

        //Use the right platform method
        return android ? getAndroidString(key, value) : getIOSString(key, value);
    }

    /* ANDROID STRING PARSING */

    /**
     * Get the formatted String for the Android Strings document
     *
     * @param key    The String key
     * @param string The String
     * @return The formatted String
     */
    private static String getAndroidString(String key, String string){
        //Add initial indentation
        String xmlString = "    ";

        //Check if it's a header section
        if(key.equalsIgnoreCase(HEADER_KEY)){
            //Leave a space before it, add the header as a comment
            xmlString = "\n" + xmlString + "<!-- " + string + " -->";
        }
        //If not, treat is as a normal string
        else{
            /* Character checks */
            //Ampersands
            string = string.replace("&", "&amp;");

            //Apostrophes
            string = string.replace("'", "\\'");

            //Unescaped @ signs
            string = string.replace("@", "\\" + "@");

            //Ellipses
            string = string.replace("...", "&#8230;");

            //HTML content
            string = string.replace("<html>", "<![CDATA[");
            string = string.replace("</html>", "]]>");
            string = string.replace("<HTML>", "<![CDATA[");
            string = string.replace("</HTML>", "]]>");

            //Add the XML tag
            xmlString = xmlString + "<string name=\"" + key + "\">" + string + "</string>";
        }

        return xmlString;
    }

    /**
     * Processes the list of parsed Strings into the Android XML document
     *
     * @param writer   The writer to use to write to the file
     * @param language The language to parse the Strings for
     * @param strings  The list of Strings
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    private static void processAndroidStrings(PrintWriter writer, Language language,
                                              List<LanguageString> strings)
            throws FileNotFoundException, UnsupportedEncodingException{
        //Add the header
        writer.println(XML_OPENER);
        writer.println(RESOURCES_OPENER);

        //Go through the strings
        for(LanguageString currentString : strings){
            try{
                //If there is no key, we cannot parse it so show a warning and move on
                if(currentString.getKey() == null || currentString.getKey().isEmpty()){
                    System.out.println("Warning: Line " + getLineNumber(currentString, strings) +
                            " has no key, and therefore cannot be parsed");
                    continue;
                }

                //Get the String
                String androidString = getLanguageString(true, currentString, language);

                //If it is null, there is no value so don't add it
                if(androidString != null){
                    writer.println(androidString);
                }
            }
            catch (Exception e){
                System.out.println("Error on Line " + getLineNumber(currentString, strings));
                e.printStackTrace();
            }
        }

        //Add the resources closing to android files
        writer.println(RESOURCES_CLOSER);
    }

    /* IOS STRING PARSING */

    /**
     * Get the formatted String for the iOS Strings document
     *
     * @param key    The String key
     * @param string The String
     * @return The formatted String
     */
    private static String getIOSString(String key, String string){
        //Replace %s format specifiers with %@
        string = string.replace("%s","%@");
        string = string.replace("$s", "$@");

        //Remove <html> </html>tags
        string = string.replace("<html>", "");
        string = string.replace("</html>", "");
        string = string.replace("<HTML>", "");
        string = string.replace("</HTML>", "");

        //Unescaped quotations
        string = string.replace("\"", "\\" + "\"");

        //Check if it's a header section
        if(key.equalsIgnoreCase(HEADER_KEY)){
            return "\n" + "/*  " + string + " */";
        }
        //If not, treat is as a normal string
        else{
            return "\"" + key + "\" = \"" + string + "\";";
        }
    }

    /**
     * Processes the list of parsed Strings into the iOS Strings document
     *
     * @param writer   The writer to use to write to file
     * @param language The language to parse the Strings for
     * @param strings  The list of Strings
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public static void processIOSStrings(PrintWriter writer, Language language,
                                         List<LanguageString> strings)
            throws FileNotFoundException, UnsupportedEncodingException{
        //Go through the strings
        for(LanguageString currentString : strings){
            try{
                //If there is no Id, we cannot parse it so show a warning and continue
                if(currentString.getKey() == null){
                    System.out.println("Warning: Line " + getLineNumber(currentString, strings) +
                            " has no Id, and therefore cannot be parsed");
                    continue;
                }

                //Get the iOS String
                String iOSString = getLanguageString(false, currentString, language);

                //If the String is null, there is no value so do not add it
                if(iOSString != null) {
                    writer.println(iOSString);
                }
            }
            catch (Exception e){
                System.out.println("Error on Line " + getLineNumber(currentString, strings));
                e.printStackTrace();
            }
        }
    }
}
