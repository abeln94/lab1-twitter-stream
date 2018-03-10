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
import javafx.util.Pair;
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

    public SearchResults search(String query) {
        Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        return twitter.searchOperations().search(query);
    }

    public SearchResults emptyAnswer() {
        return new SearchResults(Collections.emptyList(), new SearchMetadata(0, 0));
    }
        
        
    private final HashMap<String, Pair<Integer,Stream>> queries = new HashMap<>();
    
    
    public void unregisterQuery(String query){
        if ( queries.containsKey(query) ){
            Pair<Integer, Stream> pair = queries.remove(query);
            if(pair.getKey()>1){
                queries.put(query, new Pair<>(pair.getKey()-1,pair.getValue()));
            }else{
                pair.getValue().close();
            }
        }else{
            System.out.println("Query '"+query+"' not found, couldn't remove");
        }
    }
    
    public void registerQuery(String query){
        
        if ( queries.containsKey(query) ){
            
            Pair<Integer, Stream> pair = queries.remove(query);
            queries.put(query, new Pair<>(pair.getKey()+1,pair.getValue()));
            
        }else{
        
            Twitter twitter = new TwitterTemplate(consumerKey, consumerSecret, accessToken, accessTokenSecret);
            List<StreamListener> list = new ArrayList<>();
            list.add(new SimpleStreamListener(query, smso));
            Stream filter = twitter.streamingOperations().filter(query, list);
            queries.put(query,new Pair<>(1,filter));
            System.out.println("Register new query ("+query+")");
            
            
            
        }
    }
    
}

