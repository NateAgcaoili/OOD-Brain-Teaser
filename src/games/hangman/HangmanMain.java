package games.hangman;

//import com.sun.org.apache.xerces.internal.xinclude.XPointerSchema;
import games.GameOptions;
import games.directions;
import javafx.event.ActionEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import screens.FXMLDirectionsController;
import screens.FXMLGameScreenController;
import screens.FXMLMainscreenController;
import store.MoneyManager;

import java.util.EventObject;

import javax.swing.*;

public class HangmanMain extends Application {

    private static final int APP_W = 1280;
    private static final int APP_H = 767;
    private static final Font DEFAULT_FONT = new Font("Courier", 36);

    private static final int POINTS_PER_LETTER = 100;
    private static final float BONUS_MODIFIER = 0.2f;

    public HangmanMain() throws IOException {
        wordReader = new WordReader();
        wordReader.addToDict();
    }
    public HangmanMain(String dict_file) {
        wordReader = new WordReader(dict_file);
    }


    /**
     * The word to guess
     */
    private SimpleStringProperty word = new SimpleStringProperty();

    /**
     * How many letters left to guess
     */
    private SimpleIntegerProperty lettersToGuess = new SimpleIntegerProperty();

    /**
     * Current score
     */
    private SimpleIntegerProperty score = new SimpleIntegerProperty();

    public SimpleIntegerProperty highScore = new SimpleIntegerProperty();



    /**
     * How many points next correct letter is worth
     */
    private float scoreModifier = 1.0f;

    /**
     * Is game playable
     */
    private SimpleBooleanProperty playable = new SimpleBooleanProperty();

    /**
     * List for letters of the word {@link #word}
     * It is backed up by the HBox children list,
     * so changes to this list directly affect the GUI
     */
    private ObservableList<Node> letters;

    /**
     * K - characters [A..Z] and '-'
     * V - javafx.scene.Text representation of K
     */
    private HashMap<Character, Text> alphabet = new HashMap<Character, Text>();

    private HangmanImage hangman = new HangmanImage();

    private WordReader wordReader;

    public Parent createContent() {
        //Image background = new Image("assets/images/backgrounds/game_bg.png");
        //ImageView bgView = new ImageView(background);
        //bgView.setFitHeight(APP_H);
        //bgView.setFitWidth(APP_W);
        //Group backgroundImage = new Group(bgView);
        HBox options = new HBox();
        Button optionsButton = new Button("OPTIONS");
        optionsButton.setOnAction(e -> {
            try {
                openOptions(e);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        options.getChildren().add(optionsButton);
        //options.setAlignment(Pos.BOTTOM_CENTER);
        options.setPadding(new Insets(10, 10 ,10, 10));

        HBox rowLetters = new HBox();
        rowLetters.setAlignment(Pos.CENTER);
        letters = rowLetters.getChildren();

        playable.bind(hangman.lives.greaterThan(0).and(lettersToGuess.greaterThan(0)));
        playable.addListener((obs, old, newValue) -> {
            if (!newValue.booleanValue())
                stopGame();
        });

        Button btnAgain = new Button("NEW GAME");
        btnAgain.disableProperty().bind(playable);
        btnAgain.setOnAction(event -> {
            try {
                startGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        // layout
        HBox row1 = new HBox();
        HBox row3 = new HBox();
        row1.setAlignment(Pos.CENTER);
        row3.setAlignment(Pos.CENTER);
        for (int i = 0 ; i < 20; i++) {
            row1.getChildren().add(new Letter(' '));
            row3.getChildren().add(new Letter(' '));
        }

        HBox rowAlphabet = new HBox(5);
        rowAlphabet.setAlignment(Pos.CENTER);
        for (char c = 'A'; c <= 'Z'; c++) {
            Text t = new Text(String.valueOf(c));
            t.setFont(DEFAULT_FONT);
            alphabet.put(c, t);
            rowAlphabet.getChildren().add(t);
        }

        Text hyphen = new Text("-");
        hyphen.setFont(DEFAULT_FONT);
        alphabet.put('-', hyphen);
        rowAlphabet.getChildren().add(hyphen);

        Text textScore = new Text();
        textScore.textProperty().bind(score.asString().concat(" Points"));

        HBox rowHangman = new HBox(10, btnAgain, textScore, hangman);
        rowHangman.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(10);
        // vertical layout
        vBox.getChildren().addAll(
                options,
                row1,
                rowLetters,
                row3,
                rowAlphabet,
                rowHangman);
        vBox.setStyle(
                "-fx-background-image: url(" +
                        "'/assets/images/backgrounds/hangman_bg.png'" +
                        "); " +
                        "-fx-background-size: stretch;" +
                        "-fx-background-color:  #ffe8ab;"
        );
        return vBox;
    }
    private void openOptions(ActionEvent event) throws IOException {
        int result = GameOptions.display();
        switch (result) {
            case 0:
                System.out.println("Returned");
                break;
            case 1:
                Parent gameParent = FXMLLoader.load(getClass().getResource("/screens/GameScreen.fxml"));
                Scene gameScene = new Scene(gameParent);

                // getting stage information
                Stage gameWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
                gameWindow.setScene(gameScene);
                gameWindow.show();
                break;
            case 2:
                Parent root = FXMLLoader.load(getClass().getResource("/screens/FXMLMainscreen.fxml"));
                Stage homeWindow = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene home = new Scene(root);
                homeWindow.setScene(home);
                homeWindow.show();
                break;
            default:
                System.out.println("Unknown");
        }
    }


    private void stopGame() {
       /* if(checkHighScore(score) == false){
            JOptionPane.showMessageDialog(null, "You beat your high score of " + highScore.intValue() + " with a score of " + score.intValue(), "High Score!", JOptionPane.PLAIN_MESSAGE);
            highScore.set(score.get());
        }
        else{
            JOptionPane.showMessageDialog(null, "You didn't beat your high score " + highScore.intValue(), "Maybe Next Time!", JOptionPane.PLAIN_MESSAGE);
        }*/

        int money = 200;
        for (Node n : letters) {
            Letter letter = (Letter) n;
            letter.show();
            money += 100;
        }

        if(score.intValue() >= highScore.intValue()){
            JOptionPane.showMessageDialog(null, "You beat your high score of " + highScore.intValue() + " with a score of " + score.intValue(), "High Score!", JOptionPane.PLAIN_MESSAGE);
            highScore.set(score.get());
            write_highscore_to_file(highScore);
            scoreModifier = 1.0f;
        }
        else{
            JOptionPane.showMessageDialog(null, "You didn't beat your high score " + highScore.intValue() + " with a score of " + score.intValue(), "High Score!", JOptionPane.PLAIN_MESSAGE);
        }

        FXMLMainscreenController.moneyManager.add(money);
        JOptionPane.showMessageDialog(null, "You earned: " + money, "Money", JOptionPane.PLAIN_MESSAGE);

    }

    private void write_highscore_to_file(SimpleIntegerProperty highScore) {
        FXMLGameScreenController controller = new FXMLGameScreenController();
        int score = highScore.intValue();
        String[] info = {"hangman", String.valueOf(score)};
        controller.write_highscores(info);

    }
    private int read_highScore_from_file() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("src/scoreboard/highscores.txt"));
        int savedHighScore = 0;
        String s = br.readLine();
        int ind = s.indexOf("-");
        if(ind != -1){
            String value = s.substring(ind+1);
            savedHighScore += Integer.parseInt(value);
        }

        return savedHighScore;
    }

    private void startGame() throws IOException {
        score.set(0);
        highScore.set(read_highScore_from_file());
        for (Text t : alphabet.values()) {
            score.set(0);
            t.setStrikethrough(false);
            t.setFill(Color.BLACK);
        }

        hangman.reset();
        word.set(wordReader.getRandomWord().toUpperCase());
        lettersToGuess.set(word.length().get());

        letters.clear();
        for (char c : word.get().toCharArray()) {
            letters.add(new Letter(c));
        }
    }

   /* private boolean checkHighScore(SimpleIntegerProperty score){
        if(score.lessThan(highScore).get() == false ){
            return false;
        }
        else
            return true;
    }*/

    private static class HangmanImage extends Parent {
        private static final int SPINE_START_X = 100;
        private static final int SPINE_START_Y = 20;
        private static final int SPINE_END_X = SPINE_START_X;
        private static final int SPINE_END_Y = SPINE_START_Y + 50;

        /**
         * How many lives left
         */
        private SimpleIntegerProperty lives = new SimpleIntegerProperty();

        public HangmanImage() {
            Circle head = new Circle(20);
            head.setTranslateX(SPINE_START_X);

            Line spine = new Line();
            spine.setStartX(SPINE_START_X);
            spine.setStartY(SPINE_START_Y);
            spine.setEndX(SPINE_END_X);
            spine.setEndY(SPINE_END_Y);

            Line leftArm = new Line();
            leftArm.setStartX(SPINE_START_X);
            leftArm.setStartY(SPINE_START_Y);
            leftArm.setEndX(SPINE_START_X + 40);
            leftArm.setEndY(SPINE_START_Y + 10);

            Line rightArm = new Line();
            rightArm.setStartX(SPINE_START_X);
            rightArm.setStartY(SPINE_START_Y);
            rightArm.setEndX(SPINE_START_X - 40);
            rightArm.setEndY(SPINE_START_Y + 10);

            Line leftLeg = new Line();
            leftLeg.setStartX(SPINE_END_X);
            leftLeg.setStartY(SPINE_END_Y);
            leftLeg.setEndX(SPINE_END_X + 25);
            leftLeg.setEndY(SPINE_END_Y + 50);

            Line rightLeg = new Line();
            rightLeg.setStartX(SPINE_END_X);
            rightLeg.setStartY(SPINE_END_Y);
            rightLeg.setEndX(SPINE_END_X - 25);
            rightLeg.setEndY(SPINE_END_Y + 50);

            getChildren().addAll(head, spine, leftArm, rightArm, leftLeg, rightLeg);
            lives.set(getChildren().size());
        }

        public void reset() {
            getChildren().forEach(node -> node.setVisible(false));
            lives.set(getChildren().size());
        }

        public void takeAwayLife() {
            for (Node n : getChildren()) {
                if (!n.isVisible()) {
                    n.setVisible(true);
                    lives.set(lives.get() - 1);
                    break;
                }
            }
        }
    }

    private static class Letter extends StackPane {
        private Rectangle bg = new Rectangle(40, 60);
        private Text text;

        public Letter(char letter) {
            bg.setFill(letter == ' ' ? Color.LIGHTGOLDENRODYELLOW : Color.WHITE);
            bg.setStroke(Color.ORANGE);

            text = new Text(String.valueOf(letter).toUpperCase());
            text.setFont(DEFAULT_FONT);
            text.setVisible(false);

            setAlignment(Pos.CENTER);
            getChildren().addAll(bg, text);
        }

        public void show() {
            RotateTransition rt = new RotateTransition(Duration.seconds(1), bg);
            rt.setAxis(Rotate.Y_AXIS);
            rt.setToAngle(180);
            rt.setOnFinished(event -> text.setVisible(true));
            rt.play();
        }

        public boolean isEqualTo(char other) {
            return text.getText().equals(String.valueOf(other).toUpperCase());
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(createContent());
        scene.setOnKeyPressed((KeyEvent event) -> {
            if (event.getText().isEmpty())
                return;

            char pressed = event.getText().toUpperCase().charAt(0);
            if ((pressed < 'A' || pressed > 'Z') && pressed != '-')
                return;

            if (playable.get()) {
                Text t = alphabet.get(pressed);
                if (t.isStrikethrough())
                    return;

                // mark the letter 'used'
                t.setFill(Color.BLUE);
                t.setStrikethrough(true);

                boolean found = false;

                for (Node n : letters) {
                    Letter letter = (Letter) n;
                    if (letter.isEqualTo(pressed)) {
                        found = true;
                        score.set(score.get() + (int)(scoreModifier * POINTS_PER_LETTER));
                        lettersToGuess.set(lettersToGuess.get() - 1);
                        letter.show();
                    }
                }

                if (!found) {
                    hangman.takeAwayLife();
                    scoreModifier = 1.0f;
                }
                else {
                    scoreModifier += BONUS_MODIFIER;
                }
            }
        });

        primaryStage.setResizable(false);
        primaryStage.setWidth(APP_W);
        primaryStage.setHeight(APP_H);
        primaryStage.setTitle("Hangman");
        primaryStage.setScene(scene);
        primaryStage.show();
        startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
