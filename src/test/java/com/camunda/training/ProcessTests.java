package com.camunda.training;

import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.*;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
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

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("approved", true);
    variables.put("content", "Exercise 4 test - Sebastian");
    
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST, variables);

    assertThat(processInstance).isWaitingAt("UserTask_ReviewTweet");
    
    List<Task> taskList = taskService().createTaskQuery()
        .taskCandidateGroup("management")
        .processInstanceId(processInstance.getId())
        .list();
    
    assertThat(taskList).isNotNull();
    assertThat(taskList).hasSize(1);
    
    Task task = taskList.get(0);
    
    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);
    taskService().complete(task.getId(), approvedMap);
    
  }
}
