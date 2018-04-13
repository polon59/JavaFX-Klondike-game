package com.codecool.klondike;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;
    private final Image[] backgrounds = {new Image("/table/green.png"),new Image("/table/green1.png")};
    private int backgroundsChange;

    Button settingsButton;
    Button newGame;
    Button undo;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.backgroundsChange = 1;
        Card.loadCardImages();
        Game game = new Game(primaryStage);
        game.setTableBackground(backgrounds[0]);
        
        initializeButtons(game, primaryStage);
        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    public void initializeButtons(Game game, Stage primaryStage){
        initializeNewGameButton(game, primaryStage);
        initializeUndoButton(game);
        initializeThemeButton(game);
    }

    private void initializeNewGameButton(Game game, Stage stage){
        newGame = new Button("New Game");
        newGame.setLayoutY(10);
        newGame.setLayoutX(480);
        newGame.setOnAction(e -> restartGame(stage));
        game.getChildren().add(newGame);
    }

    public void restartGame(Stage stage){
        stage.close();
        start(stage);
    }

    private void initializeUndoButton(Game game){
        undo = new Button("Undo");
        undo.setLayoutY(40);
        undo.setLayoutX(500);
        undo.setOnAction(e -> {
            game.undoMove();
        });
        game.getChildren().add(undo);
    }

    private void initializeThemeButton(Game game){
        Button changeTheme = new Button("Change theme");
        changeTheme.setLayoutY(70);
        changeTheme.setLayoutX(470);
        changeTheme.setOnAction(e -> {
            if (this.backgroundsChange == 0){
                game.setTableBackground(this.backgrounds[0]);
                this.backgroundsChange = 1;
            }
            else{
                game.setTableBackground(this.backgrounds[1]);
                this.backgroundsChange = 0;
            }
        });
        game.getChildren().add(changeTheme);
    }
}


