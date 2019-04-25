package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import twitter4j.Status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessTests {

  private static final String KEY_OF_PROCESS_DEFINITION_UNDER_TEST = "TweetApprovalProcess";

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Mock
  private TwitterService twitterService;

  @Before
  public void setup() {
    init(rule.getProcessEngine());

    MockitoAnnotations.initMocks(this);
    Mocks.register("CreateTweetDelegate", new CreateTweetDelegate(twitterService));
  }

  @Test
  @Deployment(resources = "tweet-approval.bpmn")
  public void testHappyPath() throws Exception {

    when(twitterService.updateStatus(any(String.class))).thenReturn(mock(Status.class));

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("approved", true);
    variables.put("content", "Exercise 7 test - Sebastian");

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST, variables);

    assertThat(processInstance).isWaitingAt("UserTask_ReviewTweet");

    List<Task> taskList = taskService().createTaskQuery().taskCandidateGroup("management").processInstanceId(processInstance.getId()).list();

    assertThat(taskList).isNotNull();
    assertThat(taskList).hasSize(1);

    Task task = taskList.get(0);

    Map<String, Object> approvedMap = new HashMap<String, Object>();
    approvedMap.put("approved", true);
    taskService().complete(task.getId(), approvedMap);

    List<Job> jobList = jobQuery().processInstanceId(processInstance.getId()).list();
    assertThat(jobList).hasSize(1);
    Job job = jobList.get(0);
    execute(job);

    assertThat(processInstance).isEnded();
  }

  @Test(expected = Exception.class)
  @Deployment(resources = "tweet-approval.bpmn")
  public void testTwice() throws Exception {

    when(twitterService.updateStatus(any(String.class))).thenReturn(mock(Status.class)).thenThrow(new Exception());

    ProcessInstance processInstance1 = runtimeService().createProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST)
        .setVariable("content", "random content").startBeforeActivity("Task_0lfmlyu").execute();
    assertThat(processInstance1).isWaitingAt("Task_0lfmlyu");
    execute(job());
    ProcessInstance processInstance2 = runtimeService().createProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST)
        .setVariable("content", "random content").startBeforeActivity("Task_0lfmlyu").execute();
    assertThat(processInstance2).isWaitingAt("Task_0lfmlyu");
    execute(job());

    assertThat(processInstance1).isNotNull();

  }

  @Test
  @Deployment(resources = "tweet-approval.bpmn")
  public void testNoApproval() {

    Map<String, Object> variables = new HashMap<>();
    variables.put("approved", false);

    ProcessInstance processInstance = runtimeService().createProcessInstanceByKey(KEY_OF_PROCESS_DEFINITION_UNDER_TEST).setVariables(variables)
        .startAfterActivity("UserTask_ReviewTweet").execute();

    assertThat(processInstance).hasPassed("Task_GiveFeedback").isEnded();
  }

}
