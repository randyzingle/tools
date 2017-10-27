var stompClient = null;
var startTime;
var running = false;

$(document).ready(function() {
  connect();
});

function connect() {
  var socket = new SockJS('websocket');
  stompClient = Stomp.over(socket);
  stompClient.connect({}, function (frame) {
      console.log('Connected: ' + frame);
      stompClient.subscribe('/topic/status', function (message) {
          showStatus(message);
      });
      stompClient.subscribe('/topic/properties', function (message) {
          showProperties(message);
      });
  });
}

function disconnect() {
  if (stompClient !== null) {
      stompClient.disconnect();
  }
  setConnected(false);
  console.log("Disconnected");
}

// tied to /topic/status
function showStatus(message) {
  console.log(message);  
}

// tied to /topic/jmx
function showProperties(message) {
  stats = JSON.parse(message.body);
  console.log(stats);
}
