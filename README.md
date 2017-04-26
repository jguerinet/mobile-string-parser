# Mobile String Parser

## Summary
The Mobile String Parser takes an online CSV file and parses the content to produce the Strings files for an Android or iOS app.
It also parses the Strings for Web in the form of a JSON: an on object with a bunch of key-value pairs, the key being the String key and the value being the translated String for that language.   
It will also show any warnings or errors that the Strings file might have. 
Please note that it assumes that the file is in UTF-8 and also produces UTF-8 files. 

## Instructions
To use this: 

* Download the [jar][1] and the [sample config](sample-config.txt)
* Set up your config file (follow the instructions in sample-config.txt)
* The first column of your CSV file must be the keys with the header 'key' 
* Any columns containing translations in your CSV file must have a header with the 2 character language Id (ex: en, fr)
* Keys must be unique, not have spaces, and not be null (the parser will inform of any of these errors when you run it) 
* You can add headers (which will be parsed as comments) in your Strings file by adding or surrounding your header with '###'. 
For example, if you put ### General ### (or ### General), it will be parsed as `/* General */` on iOS and `<!-- General -->` on Android
* You can add formatted Strings by putting either the normal placeholder (i.e. %s for a String) if there is one argument, or numbering them 
like this for multiple: %1$s, %2$s...
* Run the jar

[1]:https://github.com/jguerinet/mobile-string-parser/releases/download/3.1.0/mobile-string-parser-3.1.0.jar

## Gradle Dependencies
* [okhttp](http://square.github.io/okhttp/)
* [super-csv](http://super-csv.github.io/super-csv/)

## Contributors
* [Julien Guerinet](https://github.com/jguerinet)
* [Yulric Sequeira](https://github.com/yulric)  

## Version History
See the [Change Log](CHANGELOG.md).

##Copyright 
    Copyright 2013-2017 Julien Guerinet

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.