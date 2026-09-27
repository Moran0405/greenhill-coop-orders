package com.greenhill.coop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Basic context smoke test backed by the H2 in-memory database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CoopOrderingApplicationTests {

    @Test
    void contextLoads() {
    }
}
