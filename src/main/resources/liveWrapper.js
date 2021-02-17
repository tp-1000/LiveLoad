/**
 * taken from https://dev.to/adamcoster/create-a-live-reload-server-for-front-end-development-3gnp
 * @file site/client-websocket.js
 */
(()=>{
	const socketUrl = 'ws://127.0.0.1:8080/'
	let socket = new WebSocket(socketUrl);
	socket.onopen = function(event){
	    console.log(socket.readyState);
	}

	socket.onerror = function(event) {
		console.error("WebSocket error observed:", event);
	};

	socket.addEventListener('onopen', ()=>{
		console.log("opened");
	})

	socket.onmessage = function (event) {
		console.log(event.data);
	}

	socket.addEventListener('close', ()=>{
		console.log("closed");
		location.reload();
	})
})()
