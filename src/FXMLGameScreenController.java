import games.hangman.HangmanMain;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class FXMLGameScreenController {

    public void backButtonPushed(ActionEvent event) throws IOException {
        Parent aboutParent = FXMLLoader.load(getClass().getResource("FXMLMainscreen.fxml"));
        Scene mainScene = new Scene(aboutParent);
        // getting stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        window.setScene(mainScene);
        window.setResizable(false);
        window.show();
    }
    //Hangman Start
    public void startHangman(ActionEvent event) throws IOException {
        HangmanMain hangman = new HangmanMain();

        // getting stage information
        Stage window = (Stage)((Node)event.getSource()).getScene().getWindow();
        try {
            hangman.start(window);
        } catch (Exception e) {
            e.printStackTrace();
        };
        window.show();
    }
}