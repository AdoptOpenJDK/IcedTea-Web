import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class Controller {
    @FXML private Text text;

    @FXML
    public void onPressButton() {
        text.setVisible(true);
    }
}
