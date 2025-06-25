package com.mylearning.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // This test will fail if the application context cannot start
        assertThat(applicationContext).isNotNull();
    }
    
    @Test
    void applicationContextTest() {
        // Verify that specific beans are loaded in the context
        assertThat(applicationContext.containsBean("productServiceApplication")).isTrue();
    }
}
