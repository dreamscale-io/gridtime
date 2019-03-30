package com.dreamscale.ideaflow.core.mapping;

public abstract class BucketMatcher {

    private final String bucketName;
    private final String bucketRegex;

    public BucketMatcher(String bucketName, String bucketRegex) {
            this.bucketName = bucketName;
            this.bucketRegex = bucketRegex;
    }

    boolean matches(String pathInsideBucket) {
        if (pathInsideBucket != null) {
            return pathInsideBucket.matches(bucketRegex);
        } else {
            return false;
        }
    }

    public String getBucketName() {
        return bucketName;
    }
}
