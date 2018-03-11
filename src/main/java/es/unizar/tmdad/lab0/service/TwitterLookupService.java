package es.unizar.tmdad.lab0.service;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.social.twitter.api.SearchMetadata;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.social.twitter.api.Stream;
import org.springframework.social.twitter.api.StreamListener;

@Service
public class TwitterLookupService {
    
    
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
    private final HashMap<String, Stream> query_stream = new HashMap<>();
    
    public void unregisterUser(String user) {
        if ( user_query.containsKey(user) ){
            String query = user_query.remove(user);
            if(!user_query.containsValue(query)){
                query_stream.remove(query)
                        .close();
                System.out.println("Closed stream with query: "+query);
            }
        }
    }
    
    public void registerUser(String user, String query) {
        
        unregisterUser(user);
        
        if(query_stream.size() >= 10){
            System.out.println("Oh oh, exceded number of queries, too bad, lets try anyway");
        }
        
        if(!query_stream.containsKey(query)){
            Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
            List<StreamListener> list = new ArrayList<>();
            list.add(new SimpleStreamListener(query, smso));
            Stream filter = twitter.streamingOperations().filter(query, list);
            query_stream.put(query, filter);
            System.out.println("Opened stream with query "+query);
        }
        
        user_query.put(user, query);
            
    }

    
}

