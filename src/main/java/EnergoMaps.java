import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class EnergoMaps extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/EnergoMaps.fxml"));
        double width = 800;
        double height = 600;

        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 2.3;
            height = bounds.getHeight() / 2.0;
        } catch (Exception e) {
        }

        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);
        Scene scene = new Scene(decorator, width, height);
        scene.getStylesheets().add(EnergoMaps.class.getResource("/css/styles.css").toExternalForm());
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/icon/icon.png")));
        primaryStage.setTitle("");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
