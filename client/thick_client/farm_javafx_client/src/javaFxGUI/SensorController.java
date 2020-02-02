package javaFxGUI;

import model.SensorsSetup;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import textFieldSmart.TextFieldSmart;
import textFieldSmart.TextFieldSmart.TFSException;
import textFieldSmart.validators.IntRangeValidator;

public class SensorController {
	private final TextFieldSmart tfs;
	private final ObservableValue<SensorsSetup> updEvent;
	private DataToText dataToText;

	public interface DataToText {
		public String onUpdate(SensorsSetup d);
	}

	public SensorController(TextFieldSmart tfs, ObservableBooleanValue connEvent, 
			ObservableValue<SensorsSetup> updEvent, ValidRange vr) {
		this.tfs = tfs;
		tfs.setValidator(new IntRangeValidator(vr.lo, vr.hi));
		tfs.setConnEvent(connEvent);
		
		this.updEvent = updEvent;
		// установить реакцию на изменение данных
		updEvent.addListener((ChangeListener<SensorsSetup>) (o, oldVal, newVal) -> {
			// to guarantie that executed from FX thread
			Platform.runLater(() -> {
				if (dataToText != null) {
					tfs.setText(dataToText.onUpdate(newVal));
				}
			});
		});
		
	}

	// установить метод для печати значения данных сенсора
	public void setDataToTextMethod(DataToText d2t) {
		this.dataToText = d2t;
	}

	public int getValue() {
		int res = -1;
		try {
			String valStr = tfs.getValidText();
			res = Integer.parseInt(valStr);
		} catch (TFSException e) {

		}
		return res;
	}
}
