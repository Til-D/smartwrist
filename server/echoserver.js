var net = require('net');

ip = '127.0.0.1'
port = 1337

var server = net.createServer(function(socket) {
	socket.write('Echo server\r\n');
	socket.pipe(socket);
});

server.listen(port, ip);
console.log('server listening at ' + ip + ':' + port);
