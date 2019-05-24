import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("helloworld.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 500, 200));
        primaryStage.show();
        System.out.println("jnlp-javafx started");
        System.out.println("jnlp-javafx can be terminated");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
