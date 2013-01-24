var http = require('http'),
	url = require('url'),
	dgram = require('dgram'),
	port = 1337,
	debug = true;

var clients = {}, 		//used for wristband
	appliances = {};	//used for appliances (TV, radio,..)

if (debug) { //add some dummy data
	console.log("*** DEBUG MODE ***");
	var client = '127.0.0.1:1234';
	clients[client] = 1234;

	var appliance = '127.0.0.1:4321';
	appliances[appliance] = 4321;
}


/** 

@params: {cmd, port, ip}
cmd: [register, deregister, vibrate]
port: int
ip: String

EXAMPLE HTTP CALLS:
===========================================================================
register new client: http://127.0.0.1:1337/wristband?cmd=register&port=1234
deregister client: http://127.0.0.1:1337/wristband?cmd=deregister&port=1234
trigger client vibration: http://127.0.0.1:1337/wristband?cmd=vibrate&ip=127.0.0.1&port=1234

**/
http.createServer(function (req, res) {
	
	var resp = '',
		reqUrl = url.parse(req.url, true),
		path = reqUrl.pathname,
		params = reqUrl.query;

	console.log('path: ' + path);
	console.log('params: ' + params);

	if(path==='/wristband') {
		if(params.cmd==='register') {
			resp = register(req.connection.remoteAddress, params.port, clients);
		}
		else if(params.cmd==='deregister') {
			resp = deregister(req.connection.remoteAddress, params.port, clients);
		}
		else if(params.cmd==='vibrate') {
			var client = params.ip + ':' + params.port;
			if(clients[client]) {
				resp = 'ok:vibrate:' + client;
				resp += sendVibrateCommand(client);
			} else {
				resp  = 'error:client not registered:' + client;
			}
		}
		else {
			resp = 'error:cmd parameter not recognized';
		}
	}

	//for triggering audio
	else if(path==='/smartdevice') {
		if(params.cmd==='register') {
			resp = register(req.connection.remoteAddress, params.port, appliances);
		}
		else if(params.cmd==='deregister') {
			resp = deregister(req.connection.remoteAddress, params.port, appliances);
		}
		else if(params.cmd==='startaudio') {
			if(params.device!=null) {
				setAudio(params.device, true);
				resp = 'Audio target device: ' + params.device + ' status: on';
			} else {
				resp = 'ERR: No target device selected for audio.';
			}
		}
		else if(params.cmd==='stopaudio') {
			if(params.device!=null) {
				setAudio(params.device, false);
				resp = 'Audio target device: ' + params.device + ' status: off';
			} else {
				resp = 'ERR: No target device selected for audio.';
			}
		}
		else {
			resp = 'error:cmd parameter not recognized';
		}
	}
	else {
		resp = 'path not recognized: ' + path;
	}
	
	if(debug) {
		console.log("**************");
		console.log("clients:");
		console.log(clients);
		console.log("applicances:");
		console.log(appliances);
		console.log("**************");
	}

	res.writeHead(200, {'Content-Type': 'text/plain'});
	console.log("[Response]: " + resp);
  	res.end(resp);

}).listen(port);

/**
adds client to pool
*/
function register(ip, port, pool) {
	if(port) {
				
		var client = ip + ':' + port;
		if(!pool[client]) {
			pool[client] = port
			resp = 'ok:newly registered:' + client;
		} else {
			resp = 'ok:already registered:' + client;
		}
	}
	else {
		resp = 'error:port not specified';
	}
	return resp;
}

/**
removes client from pool
*/
function deregister(ip, port, pool) {
	var client = ip + ':' + port;
	if(pool[client]) {
		delete pool[client];
		resp = 'ok:deregistered:' + client;
	}
	else {
		resp = 'error:client not registered:' + client;
	}
	return resp;
}

/**
return: String (status string)
*/
function sendVibrateCommand(client) {
	var resp = '',
		target = client.split(':'),
		deviceIp,
		devicePort,
		message,
		conn;

	if (target.length >= 2) {
		deviceIp = target[0];
		devicePort = target[1];
		
		console.log('sendVibrateCommand(ip:' + deviceIp + ', port: ' + devicePort + ')');
		message = new Buffer("vibrate");
		conn = dgram.createSocket("udp4");
		conn.send(message, 0, message.length, devicePort, deviceIp, function(err, bytes) {
			console.log('[Success] ' + 'message sent to ' + deviceIp + ':' + devicePort + ':' + message);	
		  	conn.close();
		});
		resp = ':message sent to ' + deviceIp + ':' + devicePort + ':' + message;

	} else {
		resp = ':[ERROR] sendVibrateCommand: Could not resolve client: ' + client;
	}

	return resp;
}

/**
sends a status (true/false) to selected appliance
**/
function setAudio(device, status) {
	console.log("setAudio()");
	console.log(device);
	console.log(status);
	switch(status) {
		case true:
			status = 'on';
			break;
		default:
			status = 'off';
			break;
	}
	for(var key in appliances) {
		var resp = '',
			target = key.split(":"),
			deviceIp,
			devicePort,
			message,
			conn;

		if (target.length >= 2) {
		deviceIp = target[0];
		devicePort = target[1];		
		
		console.log('setAudio(ip:' + deviceIp + ', port: ' + devicePort + ': ' + status + ')');
		message = new Buffer(device + ':' + status);
		conn = dgram.createSocket("udp4");
		conn.send(message, 0, message.length, devicePort, deviceIp, function(err, bytes) {
			console.log('[Success] ' + 'message sent to ' + deviceIp + ':' + devicePort + ':' + message);	
		});
		resp = ':message sent to ' + deviceIp + ':' + devicePort + ':' + message;

		} else {
			resp = ':[ERROR] sendVibrateCommand: Could not resolve client: ' + client;
		}
		console.log(resp);
	}
}

console.log('http server running at: ' + port);