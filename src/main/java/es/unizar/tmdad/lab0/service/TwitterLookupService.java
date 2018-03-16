package es.unizar.tmdad.lab0.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.SearchMetadata;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamDeleteEvent;
import org.springframework.social.twitter.api.StreamListener;
import org.springframework.social.twitter.api.StreamWarningEvent;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.util.MimeTypeUtils;

@Service
public class TwitterLookupService implements StreamListener{
    
    
    @Autowired
    private SimpMessageSendingOperations smso;
    
    
    @Value("${twitter.consumerKey}")
    private String consumerKey;

    @Value("${twitter.consumerSecret}")
    private String consumerSecret;

    @Value("${twitter.accessToken}")
    private String accessToken;

    @Value("${twitter.accessTokenSecret}")
    private String accessTokenSecret;

    public SearchResults emptyAnswer() {
        return new SearchResults(Collections.emptyList(), new SearchMetadata(0, 0));
    }
        
        
    private final HashMap<String, String> user_query = new HashMap<>();
    private final HashMap<String, String> queries = new HashMap<>();
    private Stream stream = null;
    private String activeQuery = "";
    
    public void unregisterUser(String user, boolean update) {
        if ( user_query.containsKey(user) ){
            String query = user_query.remove(user);
            if(!user_query.containsValue(query)){
                queries.remove(query);
                if(update) updateStream();
            }
        }
    }
    
    private void updateStream(){
        String newActiveQuery = String.join(",", queries.keySet());
        
        if(newActiveQuery.equals(activeQuery)){
            return;
        }
        activeQuery = newActiveQuery;
        
        if(stream!=null){
            stream.close();
            stream = null;
        }
        if(!newActiveQuery.isEmpty()){
            Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
            List<StreamListener> list = new ArrayList<>();
            list.add(this);
            stream = twitter.streamingOperations().filter(activeQuery, list);
            System.out.println("Updated stream with query="+activeQuery);
        }else{
            System.out.println("Stream closed, nothing to search");
        }
    }
    
    public void registerUser(String user, String query, String encodedQuery ) {
        
        unregisterUser(user, false);
        
        if(!queries.containsKey(query)){
            
            if(queries.size() >= 10){
                System.out.println("Oh oh, exceded number of queries, can't search that, sorry");
                updateStream();
                return;
            }
            
            queries.put(query,encodedQuery);
        }
        
        user_query.put(user, query);
        updateStream();
    }
    
    
      
    
    private boolean matchesTweet(Tweet tweet, String querie) {
        String tweetText = tweet.getUnmodifiedText().toLowerCase();
        
        for( String word : querie.split("\\s") ){
            if(!tweetText.contains(word.toLowerCase())){
                return false;
            }
        }
        
        return true;
    }
    
    
    

    
    //----------   Stream Listener -------------//
    
    
    @Override
    public void onTweet(Tweet tweet) {
        System.out.println("Received tweet, sent to:");
        
        Map<String, Object> map = new HashMap<>();
        map.put(MessageHeaders.CONTENT_TYPE,MimeTypeUtils.APPLICATION_JSON);
        
        boolean sent = false;
        
        for (Map.Entry<String, String> querySet : queries.entrySet()) {
            String querie = querySet.getKey();
            String topic = querySet.getValue();
            
            if(matchesTweet(tweet, querie)){
                smso.convertAndSend("/topic/search/"+topic, tweet, map);
                System.out.println("               >>"+topic);
                sent = true;
            }
        }
        
        if(!sent){
            System.out.println("     >>-nothing-\n"+tweet.getUnmodifiedText());
        }
        
    }

    @Override
    public void onDelete(StreamDeleteEvent deleteEvent) { }

    @Override
    public void onLimit(int numberOfLimitedTweets) {
        System.out.println("StreamListener#onLimit");
    }

    @Override
    public void onWarning(StreamWarningEvent warningEvent) {
        System.out.println("StreamListener#onWarning");
    }


    
}

