$(document).ready(function() {
    
    $("#search").submit(searchOnClick);
    
    mustacheTemplate = "-unloaded-";
    
    $.get('template', function(template) {
        Mustache.parse(template);
        mustacheTemplate = template;
    });
});

function searchOnClick(event) {
    event.preventDefault();
    
    $("#resultsBlock").empty();
    $("#loader").show();
    
    var target = $(this).attr('action');
    var query = $("#q").val();
    $.get(target, { q: query } )
            .done( onPostQuery )
            .fail( onFailQuery );
}

function onPostQuery(data){
    
    var rendered = Mustache.render(mustacheTemplate, data);
    
    $("#loader").hide();
    $("#resultsBlock").append(rendered);
}

function onFailQuery(){
    $("#loader").hide();
    $("#resultsBlock").append("-error-");
}