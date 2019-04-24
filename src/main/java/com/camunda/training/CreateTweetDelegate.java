package com.camunda.training;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.Status;

@Component("CreateTweetDelegate")
public class CreateTweetDelegate implements JavaDelegate {

  private final Logger log = LoggerFactory.getLogger(CreateTweetDelegate.class);

  private final TwitterService twitterService;

  @Autowired
  public CreateTweetDelegate(TwitterService twitterService) {
    this.twitterService = twitterService;
  }

  public void execute(DelegateExecution execution) throws Exception {

    String content = (String) execution.getVariable("content");

    log.info("Publishing tweet: " + content);

    Status status = twitterService.updateStatus(content);
    
    execution.setVariable("status", status);

  }
}
