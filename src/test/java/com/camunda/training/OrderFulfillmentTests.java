package com.camunda.training;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.init;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.taskService;

import java.util.List;

import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class OrderFulfillmentTests {

  private static final String ORDER_MESSAGE = "order-message";
  private static final String PAYMENT_MESSAGE = "payment-message";
  private static final String CANCELLATION_MESSAGE = "cancellation-message";

  private static final String EVENT_PAYMENT_RECEIVED = "PaymentReceivedEvent";
  private static final String TASK_HANDLE_CANCELLATION = "HandleCancellationTask";
  private static final String TASK_FULFILL_ORDER = "FulfillOrderTask";

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources = "order-fulfillment.bpmn")
  public void orderFulfillmentWorks() {

    MessageCorrelationResult correlationResult = runtimeService().createMessageCorrelation(ORDER_MESSAGE).correlateWithResult();
    ProcessInstance processInstance = correlationResult.getProcessInstance();

    assertThat(processInstance).isNotNull();
    assertThat(processInstance).isStarted().isWaitingAt(EVENT_PAYMENT_RECEIVED);

    runtimeService().correlateMessage(CANCELLATION_MESSAGE);

    assertThat(processInstance).hasPassed(TASK_HANDLE_CANCELLATION).isEnded();

  }

  @Test
  @Deployment(resources = "order-fulfillment.bpmn")
  public void testWithTwoProcessInstances() {
    runtimeService().correlateMessage(ORDER_MESSAGE, "bk1");
    runtimeService().correlateMessage(ORDER_MESSAGE, "bk2");

    List<EventSubscription> subscriptionsList = runtimeService().createEventSubscriptionQuery().eventName(PAYMENT_MESSAGE).list();
    assertThat(subscriptionsList).hasSize(2);

    runtimeService().correlateMessage(PAYMENT_MESSAGE, "bk1");

    List<Task> tasksList = taskService().createTaskQuery().taskDefinitionKey(TASK_FULFILL_ORDER).list();
    assertThat(tasksList).hasSize(1);

  }
}
