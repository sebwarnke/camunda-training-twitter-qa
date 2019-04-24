package com.camunda.training;

import org.springframework.stereotype.Service;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Service
public class TwitterService {

  private final Twitter twitter;
  
  public TwitterService() {
    AccessToken accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
    
    twitter = new TwitterFactory().getInstance();
    twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
    twitter.setOAuthAccessToken(accessToken);
  }
  
  public Status updateStatus(String tweet) throws Exception {
    return twitter.updateStatus(tweet);
  }
}
