package com.dreamscale.htmflow.core.mapping;

import java.util.List;

public class BucketMatcherWithExclusions extends BucketMatcher {

    private final List<String> exclusionRegexMatchers;

    public BucketMatcherWithExclusions(String bucketName, String bucketRegex, List<String> exclusionRegexMatchers) {
        super(bucketName, bucketRegex);
        this.exclusionRegexMatchers = exclusionRegexMatchers;
    }

    @Override
    boolean matches(String pathInsideBucket) {

        boolean bucketMatches = false;

        if (super.matches(pathInsideBucket)) {
            bucketMatches = true;

            for (String exclusionRegex : exclusionRegexMatchers) {
                if (pathInsideBucket.matches(exclusionRegex)) {
                    bucketMatches = false;
                }
            }

        }
        return bucketMatches;
    }

}
