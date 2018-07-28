
var socket = null;


function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    socket = new WebSocket("wss://lavitatea-api.herokuapp.com/socket");
    socket.onopen = (msg) => {
        setConnected(msg);
      console.log("connected:"+msg);
    };
    socket.onmessage = (msg) => {
        console.log(msg);
    };
}

function disconnect() {
    socket.close();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    socket.send(JSON.stringify({'id': 1}));
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
});