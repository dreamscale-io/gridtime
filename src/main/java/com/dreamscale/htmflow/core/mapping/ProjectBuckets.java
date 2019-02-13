package com.dreamscale.htmflow.core.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectBuckets {

    private Map<String, String> packageToBucketMap;
    private List<BucketMatcher> bucketMatchers;

    public ProjectBuckets() {
        packageToBucketMap = new HashMap<>();
        bucketMatchers = new ArrayList<>();

    }

    public String identifyBucket(String filePath) {

        String folderPath = extractFolderPath(filePath);

        String bucketName = packageToBucketMap.get(folderPath);

        if (bucketName == null) {
            for (BucketMatcher bucketMatcher : bucketMatchers) {
                if (bucketMatcher.matches(filePath)) {
                    bucketName = bucketMatcher.getBucketName();
                    packageToBucketMap.put(folderPath, bucketName);
                    break;
                }
            }
        }

        if (bucketName == null) {
            bucketName = "default";
        }

        return bucketName;
    }

    private String extractFolderPath(String filePath) {
        if (filePath != null) {
            int indexOfLastSlash = filePath.lastIndexOf("/");

            return filePath.subSequence(0, indexOfLastSlash + 1).toString();
        } else {
            return "";
        }
    }

    public void configureBucket(String bucketName, String bucketMatcherWithStars) {
        String bucketRegex = bucketMatcherWithStars.replace("*", "\\S*");

        bucketMatchers.add(new RegexBucketMatcher(bucketName, bucketRegex));
    }

    public void configureBucketWithExclusions(String bucketName, String bucketMatcherWithStars, String ... exclusionMatchersWithStars) {
        String bucketRegex = bucketMatcherWithStars.replace("*", "\\S*");

        List<String> exclusionRegexMatchers = new ArrayList<>();
        for (String exclusionMatcher : exclusionMatchersWithStars) {
            exclusionRegexMatchers.add(exclusionMatcher.replace("*", "\\S*"));
        }

        bucketMatchers.add(new BucketMatcherWithExclusions(bucketName, bucketRegex, exclusionRegexMatchers));

    }
}
