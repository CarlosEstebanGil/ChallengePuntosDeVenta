package com.carlos.challenge.bootstrap;

import com.carlos.challenge.config.TestProfiles;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(TestProfiles.TEST)
class QuickBootstrapProfileTest {

    @Test
    void nothing() {
    }
}
