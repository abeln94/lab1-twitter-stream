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
    //$("#resultsBlock").empty();
    
    var query = $("#q").val();
    
    if(subscription !== null){
        subscription.unsubscribe();
    }
    
    if(query!==null && query!==""){
        var topic = encodeURI(query).replace(/,/g,"%2C");
        stompClient.send("/app/register",{topic:topic},query);
        subscription = stompClient.subscribe("/topic/search/"+topic, onTweetReceived);
        console.log("subscribed to >>"+topic);
        $("#loader").show();
    }else{
        stompClient.send("/app/unregister",{},"");
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