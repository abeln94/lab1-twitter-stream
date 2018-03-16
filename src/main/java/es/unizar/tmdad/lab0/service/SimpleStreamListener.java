package es.unizar.tmdad.lab0.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.connect.TwitterServiceProvider;
import org.springframework.util.MimeTypeUtils;

public class SimpleStreamListener implements StreamListener{

    
    private final SimpMessageSendingOperations smso;
    private final String topic;
    
    SimpleStreamListener(String topic, SimpMessageSendingOperations smso) throws UnsupportedEncodingException {
        this.topic = topic;
        this.smso = smso;
    }

    @Override
    public void onTweet(Tweet tweet) {
        System.out.println("Received tweet, sending ("+topic+")");
        
        Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE,MimeTypeUtils.APPLICATION_JSON);
        
        smso.convertAndSend("/topic/search/"+topic, tweet, map);
        
    }
    

    @Override
    public void onDelete(StreamDeleteEvent deleteEvent) {
        
    }

    @Override
    public void onLimit(int numberOfLimitedTweets) {
        System.out.println("SimpleStreamListener#onLimit");
    }

    @Override
    public void onWarning(StreamWarningEvent warningEvent) {
        System.out.println("SimpleStreamListener#onWarning");
    }

}
