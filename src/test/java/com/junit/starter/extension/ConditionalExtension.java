package com.junit.starter.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ConditionalExtension implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return System.getProperty("Skip") != null
                ? ConditionEvaluationResult.disabled("Test is skipped")
                : ConditionEvaluationResult.enabled("enabled by default");
    }
}
