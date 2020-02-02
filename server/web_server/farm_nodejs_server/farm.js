/**
 * основной скрипт выполняющийся на сервере
 */
"use strict"

const PORT_HTTP = 8000;
const PORT_CTRL = 8080;

// значение по умолчанию, должно быть переписано актуальным значением из файла
const setupDefault = "setup:blue_led=309,red_led=506,sprinkle_water_temp_max=27,sprinkle_water_temp_min=25,valve_off=15,valve_on=10;";
const setupFname = './setup.txt';
let setup = '';
let setupID = 0;
let sensors = '';
let isCtrlConnected = false;

const http = require('http');
const fs = require('fs');
const PeriodicLoop = require('./PeriodicLoop.js');

// не удобно при отладке - надо перезапускать Node.js при изменении компонента
//const index = fs.readFileSync('./index.html');
//const script = fs.readFileSync('./script.js');
//const favicon = fs.readFileSync('./favicon.ico');
//const style = fs.readFileSync('./style.css');

//читаем из файла значения актуальные значения уставок,
// если не удалось оставляем значения по умолчанию
if (fs.existsSync(setupFname)) {
	setup = fs.readFileSync(setupFname).toString();
}
else {
	setup = setupDefault;
	fs.writeFile(setupFname, setup, (err) => { });
}

const server = http.createServer(function(req, res) {
	console.log(req.url);
	switch (req.url) {
		case '/script.js':
			res.writeHead(200, { 'Content-Type': 'application/javascript' });
			const script = fs.readFileSync('./script.js');
			res.write(script);
			res.end();
			break;
		case '/favicon.ico?v=0':
			res.writeHead(200, { 'Content-Type': 'image/png' });
			const favicon = fs.readFileSync('./favicon.ico');
			res.write(favicon);
			res.end();
			break;
		case '/style.css':
			res.writeHead(200, { 'Content-Type': 'text/css' });
			const style = fs.readFileSync('./style.css');
			res.write(style);
			res.end();
			break;
		default:
			res.writeHead(200, { 'Content-Type': 'text/html' });
			const index = fs.readFileSync('./index.html');
			res.write(index);
			res.end();
	}
}).listen(PORT_HTTP);


// ------------- ws -------------
class WSConnection extends PeriodicLoop {
	constructor(ws) {
		super(1000);
		this.ws = ws;
		this.isCtrlConnected = false;
		this.setupID = 0;
		this.counter = 0;
	}

	loop() {
		if (this.ws != null) {
			if (this.isCtrlConnected != isCtrlConnected) {
				console.log('controller connection status changed to ' + isCtrlConnected);
				this.isCtrlConnected = isCtrlConnected;
				this.counter = 0;
			}
			if (setupID != this.setupID) {
				console.log('setup on server changed');
				this.counter = 0;
				this.setupID = setupID;
			}
			switch (this.counter) {
				case 0:
					// статус подключен ли контроллер
					this.ws.send("isCtrlConnected:" + String(isCtrlConnected));
					console.log('controller status send to websocket');
					break;
				case 1:
					//всегда посылаем актуальные уставки
					this.ws.send(setup);
					console.log('setup send to websocket');
					break;
				default:
					//данные датчиков посылаем только при подключенном контроллере
					if (isCtrlConnected) {
						this.ws.send(sensors);
					}
			}
			if (++this.counter > 2) {
				this.counter = 2;
			}
		}
		else {
			console.log('WSConnection ws == null, requestLoop exit');
			stop();
		}
	}
}

const WebSocket = require('ws');
const wss = new WebSocket.Server({ server });

wss.on('connection', (ws) => {
	console.log('websocket connected..');
	let wsConn = new WSConnection(ws);
	wsConn.start();


	ws.on('message', (message) => {
		//от браузера может придти только new setup  
		console.log(`websocket received message : ${message}`);
		let received = String(message);
		if (received.substr(0, 6) == "setup:") {
			console.log('new setup on server');
			setup = received;
			fs.writeFile(setupFname, setup, (err) => { });
			setupID++;
		}
	});

	ws.on('close', () => {
		console.log('current websocket connection close');
		wsConn.stop();
	});


});

//--------------- net ---------------
// потенциально возможно несколько контроллеров 

class CtrlConnection extends PeriodicLoop {
	constructor(ns) {
		super(1000);
		this.ns = ns;
		this.counter = 0;
	}

	loop() {
		if (this.ns != null) {
			switch (this.counter) {
				case 0:
					// посылаем текущий setup
					this.ns.write(setup)
					break;
				case 1:
					//запрашиваем текущий setup для проверки
					this.ns.write('get_setup;');
					break;
				case 2:
				default:
					//запрашиваем данные датчиков 
					this.ns.write('get_sensors;');
					break;
			}
			if (++this.counter > 2) {
				this.counter = 2;
			}
		}
		else {
			console.log('CtrlConnection this.ns == null, requestLoop exit');
			this.stop();
		}
	}
}

const net = require('net');
const TimeoutManager = require('./TimeoutManager.js');
const nss = net.createServer((ns) => {
	console.log('controller connected from: ' + ns.remoteAddress);
	//установка таймаута по активности сокета
	//ns.setTimeout(5000);
	isCtrlConnected = true;
	let ctrlConn = new CtrlConnection(ns);
	ctrlConn.start();

	//проверка на зависание по приему
	let timeout = new TimeoutManager(5000, () => {
		console.log("controller rececieve timeout -> disconnected");
		timeout.stop();
		isCtrlConnected = false;
		ctrlConn.stop();
		ns.end();
	});
	timeout.start();

	ns.on('end', () => {
		console.log('controller socket end');
	});
	
	ns.on('close', () => {
		console.log('controller socket closed');
	});

	ns.on('error', (error) => {
		console.log("controller socket handled error:");
		console.log(error);
	});

	ns.on('timeout', () => {
		console.log('controller socket timeout');
	});

	ns.on('data', (data) => {
		//console.log(data.toString());
		let receivedRaw = data.toString();

		//комманды могут слипаться в одну, надо разобрать receivedRaw 
		let lastIdx = 0;
		let idx = 0;
		while ((idx = receivedRaw.indexOf(";", lastIdx)) != -1) {
			let cmd = receivedRaw.substring(lastIdx, idx + 1);
			lastIdx = idx + 1;


			if (cmd.substr(0, 6) == "setup:") {
				// проверка что уставки дошли до контроллера правильно
				if (setup != cmd) {
					console.log('уставки на сервере не совпадают с контроллером, что-то пошло не так!!');
					console.log('SERVER side : ' + setup);
					console.log('CONTRL side : ' + cmd);
					console.log('----------------------------------------------------------------------');
				}
				else {
					console.log('уставки на сервере совпадают с контроллером, OK');
				}
			}
			else if (cmd.substr(0, 8) == "sensors:") {
				sensors = cmd;
				console.log(sensors);
			}
			else {
				console.log('unrecognized cmd : ' + cmd);
			}
			isCtrlConnected = true; //статус надо актуализировать
			timeout.kick();
		}
	});
});

nss.listen(PORT_CTRL, () => {
	console.log('controller server is listening');
});

nss.on('close', () => {
	console.log('controller server is closed');
});

