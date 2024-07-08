package com.nictas.reviews;

import org.testcontainers.containers.PostgreSQLContainer;

public class FairReviewsPostgreSQLContainer extends PostgreSQLContainer<FairReviewsPostgreSQLContainer> {

    private static final String IMAGE_VERSION = "postgres:16";

    private static FairReviewsPostgreSQLContainer container;

    private FairReviewsPostgreSQLContainer() {
        super(IMAGE_VERSION);
    }

    public static FairReviewsPostgreSQLContainer getInstance() {
        if (container == null) {
            container = new FairReviewsPostgreSQLContainer();
        }
        return container;
    }

    @Override
    public void stop() {
        // JVM will handle the shut down.
    }

}
