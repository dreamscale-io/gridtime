package com.dreamscale.htmflow.core.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectBucketMapper {

    private Map<String, String> packageToBucketMap;
    private List<BucketMatcher> bucketMatchers;

    public ProjectBucketMapper() {
        packageToBucketMap = new HashMap<>();
        bucketMatchers = new ArrayList<>();

    }

    public String identifyBucket(String filePath) {

        String folderPath = extractFolderPath(filePath);

        String bucketName = packageToBucketMap.get(folderPath);

        if (bucketName == null) {
            for (BucketMatcher bucketMatcher : bucketMatchers) {
                if (bucketMatcher.matches(folderPath)) {
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
        int indexOfLastSlash = filePath.lastIndexOf("/");

        return filePath.subSequence(0, indexOfLastSlash + 1).toString();
    }

    public void configureBucket(String bucketName, String bucketMatcherWithStars) {
        String bucketRegex = bucketMatcherWithStars.replace("*", "\\S*");

        bucketMatchers.add(new BucketMatcher(bucketName, bucketRegex));
    }
}
