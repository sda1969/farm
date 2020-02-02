/*
 * Скрипт выполнения в браузере
 * получает данные по WebSocket
 * которые устанавливаются в GUI
 * от GUI получает комманду на
 * установления уставок и посылает
 * их по  WebSocket
 */

"use strict"
const connection = new WebSocket('ws://'+window.location.host+'/');
const setupFields = ['blue_led','red_led','sprinkle_water_temp_max','sprinkle_water_temp_min','valve_off','valve_on'];

connection.onopen = () => {
	console.log('websocket open');
}

connection.onerror = (error) => {
	console.log(`WebSocket error: ${error}`);
}

connection.onmessage = (e) => {
	console.log(e.data);
	const received = String(e.data);
	console.log(received);
	if (received.substr(0, "setup:".length) == "setup:"){
		parseSetup(received);
	}
	else if (received.substr(0, "sensors:".length) == "sensors:"){
		parseSensors(received);
	}
	else if (received.substr(0, "isCtrlConnected:".length) == "isCtrlConnected:"){
		parseConnected(received);
	}
}

// id указывается только если оно не совпадает с field
function setField(received, field, id){
	if(id == undefined){
		id = field;
	}
	let pos = received.indexOf(field) + field.length + 1;
	let comma = received.indexOf(',', pos);
	let end = (comma == -1) ? received.indexOf(';') : comma
	document.getElementById(id).value = received.substr(pos, end - pos);
}


function parseSetup(received){
	setupFields.forEach((item, index, array) => {
		setField(received, item);
	});
}

function parseSensors(received){
	setField(received, 'blue_led', 'blue_led_sensor');
	setField(received, 'red_led', 'red_led_sensor');
	setField(received, 'air_humidity');
	setField(received, 'air_temp');
	setField(received, 'cool_water_in_temp');
	setField(received, 'cool_water_out_temp');
	setField(received, 'sprinkle_water_flow_average');
	setField(received, 'sprinkle_water_flow_instant');
	setField(received, 'sprinkle_water_temp');
	setField(received, 'tank_level');
}

function parseConnected(received){
	let pos = received.indexOf('isCtrlConnected:') + 'isCtrlConnected:'.length;
	let status = received.substr(pos, received.length - pos);
	document.getElementById("connStatus").innerHTML = (status == 'true') ? "Подключен" : (status == 'false') ? "Отключен" : "Неизвестно";
	if(status == 'false'){
		setEmptySensorsValue();
	}
	
}

function getField(item){
	return document.getElementById(item).value;
}

function sendSetup(){
	let result = 'setup:';
	setupFields.forEach((item, index, array) => {
		result = result + item + '=' + getField(item)+',';
	});
	//заменить последнюю запятую на точку с запятой
	result = result.substr(0, result.length - 1) + ';';
	connection.send(result);
}

function setEmptySensorsValue(){
	const empty = "sensors:air_humidity=,air_temp=,blue_led=,cool_water_in_temp=,cool_water_out_temp=,red_led=,sprinkle_water_flow_average=,sprinkle_water_flow_instant=,sprinkle_water_temp=,tank_level=;";
	parseSensors(empty);
}
	
	
