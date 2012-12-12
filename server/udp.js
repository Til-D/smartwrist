var dgram = require('dgram');

var msg = new Buffer("ping");
var client = dgram.createSocket("udp4");
var ip = '40.162.4.255';
var port = 1337;

client.send(msg, 0, msg.length, 1337, ip, function(err, bytes) {
	if(err) {
		console.log(err);
	} else {
		console.log('msg sent.');
	}
	client.close();
});
