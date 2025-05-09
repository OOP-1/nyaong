package org.example.boundary;

import org.example.control.TestRepository;
import org.junit.jupiter.api.Test;

class TestRepositoryTest {

    @Test
    void test() {
        TestRepository repo = new TestRepository();
        repo.test();
    }
}