package com.junit.starter;

import com.junit.starter.extension.GlobalExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({
        GlobalExtension.class
})
public abstract class TestBase {
}
