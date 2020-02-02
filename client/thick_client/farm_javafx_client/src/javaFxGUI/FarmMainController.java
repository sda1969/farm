package javaFxGUI;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import buttonOnEvent.ButtonOnEvent;
import connectButton.ConnectButton01;
import model.Model;
import model.SensorsData;
import model.SensorsSetup;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import textFieldSmart.TextFieldSmart;
import textFieldSmart.TextFieldSmart.TFSException;
import textFieldSmart.validators.IntRangeValidator;
import textFieldSmart.validators.Ip4Validator;

public class FarmMainController implements Initializable {
	private final Preferences localPrefs = Preferences.userRoot().node("FarmMainController.class");
	private BooleanProperty connEvent = new SimpleBooleanProperty(false);
	private ObjectProperty<SensorsData> updSensorsEvent = new SimpleObjectProperty<>();
	private ObjectProperty<SensorsSetup> updSetupEvent = new SimpleObjectProperty<>();
	
	@FXML
	private TextFieldSmart ipAddrTfs;
	@FXML
	private TextFieldSmart red_led_Tfs;
	@FXML
	private TextFieldSmart blue_led_Tfs;
	@FXML
	private TextFieldSmart sprinkle_water_temp_min_Tfs;
	@FXML
	private TextFieldSmart sprinkle_water_temp_max_Tfs;
	@FXML
	private TextFieldSmart valve_on_Tfs;
	@FXML
	private TextFieldSmart valve_off_Tfs;
	@FXML
	private ConnectButton01 connBtn;
	@FXML
	private TextField red_led_Tf;
	@FXML
	private TextField blue_led_Tf;
	@FXML
	private TextField air_temp_Tf;
	@FXML
	private TextField cool_water_in_temp_Tf;
	@FXML
	private TextField cool_water_out_temp_Tf;
	@FXML
	private TextField sprinkle_water_temp_Tf;
	@FXML
	private TextField tank_level_Tf;
	@FXML
	private TextField sprinkle_water_flow_instant_Tf;
	@FXML
	private TextField sprinkle_water_flow_average_Tf;
	@FXML
	private TextField air_humidity_Tf;
	@FXML
	private ButtonOnEvent uploadBtn;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Model model = new Model(); 
		
		connBtn.setConnAction(() -> {
			System.out.println("Action Connect");
			try {
				model.connect(ipAddrTfs.getValidText());
			} catch (Exception e) {
				//что-то пошло не так коннект не случился, не сигнализируем
				return;
			}
			connEvent.setValue(true);
			
		});
		connBtn.setDisconnAction(() -> {
			System.out.println("Action Disconnect");
			model.disconnect();
		});
		connBtn.setStatesName("Подключить Контроллер ", "Отключить Контроллер");
		connBtn.setConnEventInverted(false);
		connBtn.setConnEvent(connEvent);

		ipAddrTfs.setValidator(new Ip4Validator());
		ipAddrTfs.setPreference("1.2.3.4", localPrefs);
		ipAddrTfs.setConnEventInverted(true);
		ipAddrTfs.setConnEvent(connEvent);

		SensorController redSensorCtrl = new SensorController(red_led_Tfs, connEvent, updSetupEvent, new ValidRange(0, 999));
		redSensorCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.red_led);
		});
		
		SensorController blueSensorCtrl = new SensorController(blue_led_Tfs, connEvent, updSetupEvent, new ValidRange(0, 999));
		blueSensorCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.blue_led);
		});
		
		SensorController waterTempMinCtrl = new SensorController(sprinkle_water_temp_min_Tfs, connEvent, updSetupEvent, new ValidRange(0, 50));
		waterTempMinCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.sprinkle_water_temp_min);
		});
		
		SensorController waterTempMaxCtrl = new SensorController(sprinkle_water_temp_max_Tfs, connEvent, updSetupEvent, new ValidRange(0, 50));
		waterTempMaxCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.sprinkle_water_temp_max);
		});
		
		SensorController valveOnCtrl = new SensorController(valve_on_Tfs, connEvent, updSetupEvent, new ValidRange(0, 99));
		valveOnCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.valve_on);
		});
		
		SensorController valveOffCtrl = new SensorController(valve_off_Tfs, connEvent, updSetupEvent, new ValidRange(0, 99));
		valveOffCtrl.setDataToTextMethod((SensorsSetup data)->{
			return Integer.toString(data.valve_off);
		});
		
        uploadBtn.setButtonAction(()->{
        	System.out.println("Upload is pressed");
        	//собрать все данные уставок в переменную и послать в модель
        	SensorsSetup set = new SensorsSetup();
        	set.red_led = redSensorCtrl.getValue();
        	set.blue_led = blueSensorCtrl.getValue();
        	set.sprinkle_water_temp_max = waterTempMaxCtrl.getValue();
        	set.sprinkle_water_temp_min = waterTempMinCtrl.getValue();
        	set.valve_on = valveOnCtrl.getValue();
        	set.valve_off = valveOffCtrl.getValue();
        	
        	model.sendSetup(set);
        });
        uploadBtn.setText("Установить");
        uploadBtn.setConnEvent(connEvent);
        
		SensorIndicator redSensorInd = new SensorIndicator(red_led_Tf, connEvent, updSensorsEvent);
		redSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.red_led);
		});
		
		SensorIndicator blueSensorInd = new SensorIndicator(blue_led_Tf, connEvent, updSensorsEvent);
		blueSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.blue_led);
		});
		
		SensorIndicator airSensorInd = new SensorIndicator(air_temp_Tf, connEvent, updSensorsEvent);
		airSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.air_temp);
		});
		
		SensorIndicator waterInSensorInd = new SensorIndicator(cool_water_in_temp_Tf, connEvent, updSensorsEvent);
		waterInSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.cool_water_in_temp);
		});
		
		SensorIndicator waterOutSensorInd = new SensorIndicator(cool_water_out_temp_Tf, connEvent, updSensorsEvent);
		waterOutSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.cool_water_out_temp);
		});
		
		SensorIndicator waterTempSensorInd = new SensorIndicator(sprinkle_water_temp_Tf, connEvent, updSensorsEvent);
		waterTempSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.sprinkle_water_temp);
		});
		
		SensorIndicator waterLevelSensorInd = new SensorIndicator(tank_level_Tf, connEvent, updSensorsEvent);
		waterLevelSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.tank_level);
		});
		
		
		SensorIndicator waterFlowImmSensorInd = new SensorIndicator(sprinkle_water_flow_instant_Tf, connEvent, updSensorsEvent);
		waterFlowImmSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.sprinkle_water_flow_instant);
		});
		
		SensorIndicator waterFlowMeanSensorInd = new SensorIndicator(sprinkle_water_flow_average_Tf, connEvent, updSensorsEvent);
		waterFlowMeanSensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.sprinkle_water_flow_average);
		});
		
		SensorIndicator airHumiditySensorInd = new SensorIndicator(air_humidity_Tf, connEvent, updSensorsEvent);
		airHumiditySensorInd.setDataToTextMethod((SensorsData data)->{
			return Integer.toString(data.air_humidity);
		});
		
		// ----------------Model CallBack------------
		Model.Callback cbk = new Model.Callback() {
			//пришли новые данные для UI датчика
			@Override
			public void onUpdateSensors(SensorsData data) {
				Platform.runLater(() -> {
					updSensorsEvent.setValue(data);
				});	
			}
			//пришли текущие данные уставок контроллера
			@Override
			public void onUpdateSetup(SensorsSetup data) {
				Platform.runLater(() -> {
					updSetupEvent.setValue(data);
				});	
			}
			//соединение завершилось по внешним причинам
			@Override
			public void onEnd(String msg) {
				connEvent.setValue(false);
			}
		};
		model.registerCallback(cbk);
		
	}
}
