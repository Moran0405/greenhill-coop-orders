package com.greenhill.coop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Greenhill Food Co-op Weekly Ordering System.
 *
 * A volunteer-run community food co-operative in Moorooka, Brisbane.
 * Members place orders during the weekly open window (Monday open,
 * Sunday 8pm close) and pick up their food boxes on Thursday.
 */
@SpringBootApplication
public class CoopOrderingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoopOrderingApplication.class, args);
    }
}
