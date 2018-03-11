package es.unizar.tmdad.lab0.controller;

import es.unizar.tmdad.lab0.service.TwitterLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.social.UncategorizedApiException;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Controller
public class SearchController {

    @Autowired
    TwitterLookupService twitter;

    @RequestMapping("/")
    public String greeting() {
        return "index";
    }

    @ResponseStatus(value= HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UncategorizedApiException.class)
    @ResponseBody
    public SearchResults handleUncategorizedApiException() {
        return twitter.emptyAnswer();
    }
    
    @RequestMapping("/template")
    public String template() {
        return "template";
    }
    
    
    @MessageMapping(/*app*/"/register")
    public void register(String query, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        System.out.println("Received search query ("+query+") from "+headerAccessor.getSessionId());
        twitter.registerUser(headerAccessor.getSessionId(), query);
    }
    
    @MessageMapping(/*app*/"/unregister")
    public void unregister(String query, SimpMessageHeaderAccessor headerAccessor) throws Exception {
        System.out.println("Received unregister query ("+query+") from "+headerAccessor.getSessionId());
        twitter.unregisterUser(headerAccessor.getSessionId());
    }
    
    
    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        twitter.unregisterUser(event.getSessionId());
        System.out.println("user "+event.getSessionId()+" disconnected");
    }
    
}