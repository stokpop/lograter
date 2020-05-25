/*
 * Copyright (C) 2020 Peter Paul Bakker, Stokpop Software Solutions
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
package nl.stokpop.lograter.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read "three-letter-words.txt" from classpath. If it fails the list of three letter words is empty.
 */
public class ThreeLetterWords {

    private static final List<String> threeLetterWords;

    static {
        List<String> words;
        try {
            URL resource = ThreeLetterWords.class.getClassLoader().getResource("three-letter-words.txt");
            if (resource ==  null) {
                words = Collections.emptyList();
            }
            else {
                Path filePath = Paths.get(resource.toURI());
                words = Files.lines(filePath).map(String::toLowerCase).collect(Collectors.toList());
            }
        } catch (IOException | URISyntaxException e) {
            words = Collections.emptyList();
        }
        threeLetterWords = Collections.unmodifiableList(words);
    }

    public static boolean isInitializedCorrectly() {
        return !threeLetterWords.isEmpty();
    }
    
    public static String getThreeLetterHash(Object object) {
        return threeLetterWords.get(altAbsHashCode(object, threeLetterWords.size()));
    }

    public static int altAbsHashCode(Object object, int mod) {
        return Math.abs(object.hashCode() % mod);
    }

    public static String getThreeLetterHashSameCapitalizationOrDigits(String text) {

        boolean isAllDigits = text.chars().allMatch(Character::isDigit);
        if (isAllDigits) {
            return String.valueOf(altAbsHashCode(text, 1000));
        }

        String threeLetterWord = getThreeLetterHash(text);
        boolean isAllUpper = text.chars().allMatch(Character::isUpperCase);
        boolean startsWithUpper = text.length() > 0 && Character.isUpperCase(text.charAt(0));

        if (isAllUpper) {
            return threeLetterWord.toUpperCase();
        }
        if (startsWithUpper) {
            return capitalize(threeLetterWord);
        }

        return threeLetterWord;
    }

    private static String capitalize(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

}
