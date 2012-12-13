SmartWrist: An interactive android client for smart environments
================================================================

SmartWrist: connects to a server, opens up a udp socket for receiving data packages that are translated into vibrations. Calibration for azimuth ranges of appliances allows definition spaces in a room that when entered, create a device vibration and send out a udp package to the server.

Server
------------

simple NodeJS server in: server/server.js

Keeps a list of registered clients and sends udp packets on command. For test purposes examplatory http requests:

register new client: http://127.0.0.1:1337/wristband?cmd=register&port=1234

deregister client: http://127.0.0.1:1337/wristband?cmd=deregister&port=1234

trigger client vibration: http://127.0.0.1:1337/wristband?cmd=vibrate&ip=127.0.0.1&port=1234

