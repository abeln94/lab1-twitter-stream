var stompClient = null;
var mustacheTemplate = "-unloaded-";
var subscription = null;

$(document).ready(function() {
    
    $("#navBar").hide();
    $("#loader").show();
    
    $("#search").submit(streamOnClick);
    
    $.get('template', function(template) {
        Mustache.parse(template);
        mustacheTemplate = template;
    });
    
    
    stompClient = Stomp.over(new SockJS("/twitter"));//endpoint
    stompClient.connect({}, function(frame) {
        stompClient.debug = null;
        console.log("Connected");
        $("#navBar").show();
        $("#loader").hide();
    });
});



function streamOnClick(event){
    event.preventDefault();
    $("#resultsBlock").empty();
    
    var query = $("#q").val();
    
    if(subscription !== null){
        stompClient.send("/app/unregister",{},subscription[1]);
        subscription[0].unsubscribe();
    }
    
    if(query!==null && query!==""){
        var encodedQuery = encodeURI(query).replaceAll(",","%2C");
        stompClient.send("/app/register",{encodedQuery:encodedQuery},query);
        subscription = [stompClient.subscribe("/topic/search/"+encodedQuery, onTweetReceived),query];
        console.log("subscribed to >>"+encodedQuery);
        $("#loader").show();
    }else{
        $("#loader").hide();
    }
}


function onTweetReceived(tweet){
    console.log("received tweet");
    var rendered = Mustache.render(mustacheTemplate, JSON.parse(tweet.body));
    
    $("#loader").hide();
    $("#resultsBlock").prepend(rendered);
    if($("#resultsBlock").get(0).childElementCount > 100){
        $("#resultsBlock").children().last().remove();
    }
}