/*
 */

#include <WiFi.h>
#include <WiFiClient.h>

//-------- customize this section -----------
const char* ssid     = "xxxxx";
const char* password = "yyyyyyy";

String cloudIP = "zzz.zzz.zzz.zzz";
uint16_t cloudPort = 8080;
//-------------------------------------------

//------------- work with sensors -----------
//initial default values 
String sensors = "sensors:air_humidity=66,air_temp=69,blue_led=63,cool_water_in_temp=26,cool_water_out_temp=17,red_led=75,sprinkle_water_flow_average=54,sprinkle_water_flow_instant=32,sprinkle_water_temp=23,tank_level=2;";

String get_blue_led(){
	return String(random(300,400));
}
String get_red_led(){
	return String(random(300,400));
}
String get_air_humidity(){
	return String(random(80,95));
}
String get_air_temp(){
	return String(random(15,30));
}
String get_cool_water_in_temp(){
	return String(random(8,12));
}
String get_cool_water_out_temp(){
	return String(random(15,18));
}
String get_sprinkle_water_flow_average(){
	return String(random(190,210));
}
String get_sprinkle_water_flow_instant(){
	return String(random(100,300));
}
String get_sprinkle_water_temp(){
	return String(random(18,24));
}
String get_tank_level(){
	return String(random(5,100));
}


String readSensors(){
	String result = "sensors:";
	result += "blue_led=" + get_blue_led() + ",";
	result += "red_led=" + get_red_led() + ",";
	result += "air_humidity=" + get_air_humidity() + ",";
	result += "air_temp=" + get_air_temp() + ",";
	result += "cool_water_in_temp=" + get_cool_water_in_temp() + ",";
	result += "cool_water_out_temp=" + get_cool_water_out_temp() + ",";
	result += "sprinkle_water_flow_average=" + get_sprinkle_water_flow_average() + ",";
	result += "sprinkle_water_flow_instant=" + get_sprinkle_water_flow_instant() + ",";
	result += "sprinkle_water_temp=" + get_sprinkle_water_temp() + ",";
	result += "tank_level=" + get_tank_level() + ";";
	
	return result;
}
	
//------------work with setup----------------
//initial default values 
String setups = "setup:blue_led=301,red_led=506,sprinkle_water_temp_max=27,sprinkle_water_temp_min=25,valve_off=15,valve_on=10;";
void apply_setup(String s){
	//break apart the line s
	//convert parts to int
	//apply to the appropriate functions
}
//-------------------------------------------
	
// Use WiFiClient class to create TCP connections
WiFiClient client;


//Initiate WiFi connection to an access point (Phone, Router, ...)
void setup()
{
    Serial.begin(115200);
    delay(10);

    // We start by connecting to a WiFi network

    Serial.println();
    Serial.println();
    Serial.print("Connecting to ");
    Serial.println(ssid);

    WiFi.begin(ssid, password);

    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());
    
    delay(1000);
       
}

void loop()
{
    //cycled attempts to connect Commutator or HTTP server
    // connection is a must, controller doesn't work else
    if (!client.connect(cloudIP.c_str(), cloudPort)) {
        Serial.println("connection failed");
        //try it again in a second
        delay(1000);
        return;  //from loop()
    } 
    //we are connected, listen socket for requests
    //we doesn't send anything without it
    int t = 0; //timeout counter in ms
    
    //--- work cycle
    while(true){
      delay(100); t +=100;
      if(client.available()) {
        
          String line = client.readStringUntil('\n'); // end of line "\n" is removed from string
          Serial.println(line);
          //line my contain several commands inside, lets split it
          int lastIdx = 0;
          int idx = 0;
          while ((idx = line.indexOf(";", lastIdx)) != -1){
            String cmd = line.substring(lastIdx, idx + 1);
            lastIdx = idx + 1;           
            if(cmd == "get_sensors;"){
              Serial.println("treated as get_sensors");
              sensors = readSensors();
              client.print(sensors+"\n"); // println gives \r\n !
              t = 0; //clear timeout upon a valid command
            }
            else if (cmd == "get_setup;"){
              Serial.println("treated as get_setup");
              client.print(setups +"\n");
              t = 0; //clear timeout upon a valid command
            }
            else if (cmd.substring(0,6) == "setup:"){
              Serial.println("treated as set_setup");
              setups = cmd;
              apply_setup(setups);
              t = 0; //clear timeout upon a valid command
            }
          }
      }
      if (t > 5000){
      	//timeout triggered
        client.stop(); //drop current connection
        setup();
        break; // leave cycle and go to client.connect()
      }
    } 
}
