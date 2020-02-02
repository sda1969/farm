/*скрипт имитирует работу ESP контроллера
и его подключение по обычному TCP сокету
к серверу farm.js
*/

"use strict"
const PORT_CTRL = 8080;
let setup = "setup:blue_led=309,red_led=506,sprinkle_water_temp_max=27,sprinkle_water_temp_min=25,valve_off=15,valve_on=10;";
const sensorsFields = ['air_humidity','air_temp','blue_led','red_led','cool_water_in_temp','cool_water_out_temp','sprinkle_water_flow_average','sprinkle_water_flow_instant','sprinkle_water_temp','tank_level'];
const net = require('net');

console.log('connect loop started ...');
	
// бесконечная петля попыток соединенния с сервером
// обернуто в анонимную функцию  из-за async
(async () => {
	
	while (true) {
		console.log("new connection");
		
		await new Promise((resolve, reject) => {
			let cnt = 0; //счетчик входящих запросов используется только для теста разрыва соединения
			const client = net.connect({port: PORT_CTRL}, function() {
				console.log('connected to server!');  
			});
		 
			client.on('data', (data) => {
			   cnt++;
			   console.log(data.toString());
			   // разбираем комманды
			   const received = data.toString();
			   if(received == 'get_setup;'){
				   client.write(setup);
			   }
			   else if (received == 'get_sensors;'){
				   //просто перестаем отвечать через 10 запросов
				   if (cnt < 10)
					   client.write(readSensors());
			   }
			   else if (received.substr(0,6) == 'setup:'){
				   console.log("new setup received: %s\n", received);
				   setup = received;
			   }
			});
		
//			client.on('end', () => { 
//				console.log('end - disconnected from server');
//				resolve('end');
//			});
			client.on('close', () => { 
				console.log('close - disconnected from server');
				resolve('close');
			});
			client.on('error', () => { 
				console.log('error - disconnected from server');
				resolve('error');
			});
	
		});
		
		console.log("connection closed");
		//подождем 3 секунды прежде чем начинать новое
		await new Promise((resolve, reject) => {
			setTimeout(() => {
				resolve('done'); //parameter does not matter 
			}, 3000);
			
		});
	}
}) ();

function getSensorValue(item){
	return Math.floor(Math.random() * 100); 
}

function readSensors(){
	let result = 'sensors:';
	sensorsFields.forEach((item, index, array) => {
		result = result + item + '=' + getSensorValue(item)+',';
	});
	// заменить последнюю запятую на точку с запятой
	return result.substr(0, result.length - 1) + ';';
}