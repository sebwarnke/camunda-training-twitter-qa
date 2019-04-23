package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.cmmn.CmmnAwareTests.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessTests {

  private static final String KEY_OF_PROCESS_DEFINITION_UNDER_TEST = "TweetApprovalProcess";
  
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources="tweet-approval.bpmn")
  public void testHappyPath() {
    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("approved", true);
    variables.put("content", "Exercise 4 test - Sebastian");
    
    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST, variables);
    // Make assertions on the process instance
    assertThat(processInstance).isEnded();
  }
}
