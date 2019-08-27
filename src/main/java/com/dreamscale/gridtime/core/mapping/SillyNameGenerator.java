package com.dreamscale.gridtime.core.mapping;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SillyNameGenerator {

    private List<String> nouns;
    private List<String> adjectives;
    private Random random;

    public SillyNameGenerator() throws IOException, URISyntaxException {
        adjectives = loadDictionary("adjectives.txt");
        nouns = loadDictionary("nouns.txt");

        random = new Random();
    }

    public String random() {
        int adjectiveIndex = random.nextInt(adjectives.size());
        int nounIndex = random.nextInt(nouns.size());

        String sillyAdjective = adjectives.get(adjectiveIndex);
        String sillyNoun = nouns.get(nounIndex);

        return sillyAdjective + "_" + sillyNoun;
    }

    private List<String> loadDictionary(String dictionaryFileName) throws URISyntaxException, IOException {

        List<String> dictionaryValues = new ArrayList<>();

        URL url = getClass().getResource("/dictionary/"+dictionaryFileName);

        List<String> rawLines = Resources.readLines(url, Charset.defaultCharset());

        for (String line: rawLines) {
            dictionaryValues.add(line.trim().toLowerCase());
        }

        return dictionaryValues;

    }

}
