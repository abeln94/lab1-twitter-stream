var stompClient = null;
var mustacheTemplate = "-unloaded-";
var subscription = null;

$(document).ready(function() {
    
    $("#search").submit(streamOnClick);
    
    $.get('template', function(template) {
        Mustache.parse(template);
        mustacheTemplate = template;
    });
    
    
    stompClient = Stomp.over(new SockJS("/twitter"));//endpoint
    stompClient.connect({}, function(frame) {
        stompClient.debug = null;
        console.log("Connected");
    });
});



function streamOnClick(event){
    event.preventDefault();
    $("#resultsBlock").empty();
    $("#loader").show();
    
    var query = $("#q").val();
    
    
    if(subscription !== null){
        stompClient.send("/app/unregister",{},subscription[1]);
        subscription[0].unsubscribe();
    }
    
    stompClient.send("/app/register",{},query);
    subscription = [stompClient.subscribe("/topic/search/"+query, onTweetReceived),query];
    console.log("subscribed to >>"+query);
    
}


function onTweetReceived(tweet){
    console.log("received tweet");
    var rendered = Mustache.render(mustacheTemplate, JSON.parse(tweet.body));
    
    $("#loader").hide();
    $("#resultsBlock").append(rendered);
    $("#resultsBlock").scrollTop();
}