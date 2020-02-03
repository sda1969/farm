const String get_setup = "get_setup";
const String get_sensors = "get_sensors";
const String setupPrfx = "setup:";

int counter = 0;

void setup() {
  Serial.begin(9600);
  Serial.setTimeout(10);
}

void loop() {
  if (Serial.available() > 0){
    String rmsg = Serial.readStringUntil(';');
    parseCmd(rmsg);
  }
  delay(100);//можно потом убрать имитирует загрузку проца

}

void parseCmd(const String& cmd){
  
    if(++counter > 999){
      counter = 0;
    }
  
  if (cmd.equals(get_setup)){
    String reply = "setup:";
    reply += "red_led=102,";
    reply += "blue_led=103,";
    reply += "sprinkle_water_temp_max=26,";
    reply += "sprinkle_water_temp_min=24,";
    reply += "valve_on=12,";
    reply += "valve_off=15;";
    Serial.write(reply.c_str(), reply.length());
    //Serial.write("setup:red_led=102,blue_led=103,sprinkle_water_temp_max=26,sprinkle_water_temp_min=24,valve_on=12,valve_off=15;");
  }
  else if (cmd.equals(get_sensors)){
    String reply = "sensors:";
    reply += "red_led="+ String(counter)+",";
    reply += "blue_led=103,";
    reply += "air_temp=35,";
    reply += "cool_water_in_temp=10,";
    reply += "cool_water_out_temp=14,";
    reply += "sprinkle_water_temp=20,";
    reply += "tank_level=80,";
    reply += "sprinkle_water_flow_instant=17,";     
    reply += "sprinkle_water_flow_average=13,";
    reply += "air_humidity=98;";
    Serial.write(reply.c_str(), reply.length());
    //Serial.write("sensors:red_led=402,blue_led=103,air_temp=35,cool_water_in_temp=10,cool_water_out_temp=14,sprinkle_water_temp=20,tank_level=80,sprinkle_water_flow_instant=17;");
  }
  else if( cmd.substring(0, setupPrfx.length()).equals(setupPrfx)){
    //Serial.write("c\n");
    //нужно разобрать строчку cmd вытащить из нее уставки и приложить куда надо
    //setup:red_led=102,blue_led=103,sprinkle_water_temp_max=26,sprinkle_water_temp_min=24,valve_on=12,valve_off=15;
    
  }
  else{
    //игнорируем
  }
}
