package com.kelvin.api.util;

public class StringUtil {

    public static String fromCamelCaseToSeparatedWordsWhenFirstWordStartsWithCapitalLetter(String classSimpleName) {
        String[] words = classSimpleName.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(words[0]);
        for (int i = 1; i < words.length; i++){
            stringBuilder.append(" ");
            stringBuilder.append(words[i].toLowerCase());
        }
        String entityNameWithSpacesBettweenWords = stringBuilder.toString();
        return entityNameWithSpacesBettweenWords;
    }
}
