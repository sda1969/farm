package javaFxGUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FarmMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Клиент для управления Фермой");       
		FXMLLoader graphTestLoader = new FXMLLoader();
		graphTestLoader.setLocation(getClass().getResource("FarmMain.fxml"));
		Parent root = graphTestLoader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


