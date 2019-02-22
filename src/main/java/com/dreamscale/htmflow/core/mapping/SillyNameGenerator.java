package com.dreamscale.htmflow.core.mapping;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

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

        Path path = Paths.get(getClass().getResource("/dictionary/"+dictionaryFileName).toURI());

        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(s -> dictionaryValues.add(s.trim().toLowerCase()));
        }

        return dictionaryValues;

    }

}
