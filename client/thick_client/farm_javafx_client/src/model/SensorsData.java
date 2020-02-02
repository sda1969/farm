package model;

//данные от датчиков идут к клиенту если он подключен
public class SensorsData {
	public final static String prefix = "sensors:";
	public int red_led;
	public int blue_led;
	public int air_temp;
	public int cool_water_in_temp;
	public int cool_water_out_temp;
	public int sprinkle_water_temp;
	public int tank_level;
	public int sprinkle_water_flow_instant;
	public int sprinkle_water_flow_average;
	public int air_humidity;
}
