package javaFxGUI;

import model.SensorsData;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class SensorIndicator {
	private final TextField tf;
	private final ObservableBooleanValue connEvent;
	private final ObservableValue<SensorsData> updEvent;
	private DataToText dataToText;

	public interface DataToText {
		public String onUpdate(SensorsData d);
	}

	public SensorIndicator(TextField tf, ObservableBooleanValue connEvent, ObservableValue<SensorsData> updEvent) {
		this.tf = tf;
		this.connEvent = connEvent;
		this.updEvent = updEvent;

		tf.setEditable(false);
		
		// установить реакцию на изменение состояния соединения
		connEvent.addListener((ChangeListener<Boolean>) (o, oldVal, newVal) -> {
			// to guarantie that executed from FX thread
			Platform.runLater(() -> {
				if (connEvent.get()) { // fire current state
					tf.setStyle("-fx-background-color: lightgreen");
				} else {
					tf.setText("---");
					tf.setStyle("-fx-background-color: lightgrey");
				}
			});
		});

		// установить реакцию на изменение данных
		updEvent.addListener((ChangeListener<SensorsData>) (o, oldVal, newVal) -> {
			// to guarantie that executed from FX thread
			Platform.runLater(() -> {
				if (dataToText != null) {
					tf.setText(dataToText.onUpdate(newVal));
				}
			});
		});

	}

	// установить метод для печати значения данных сенсора
	public void setDataToTextMethod(DataToText d2t) {
		this.dataToText = d2t;
	}

}
