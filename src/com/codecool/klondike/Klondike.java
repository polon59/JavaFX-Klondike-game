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
        Game game = new Game();
        game.setTableBackground(backgrounds[0]);
        
        initializeButtons(game, primaryStage);
        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

        // if (game.isGameWon()) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogBox = new VBox(50);
        dialogBox.getChildren().add(new Text("CONGRATULATIONS! YOU WON!"));
        Scene dialogScene = new Scene(dialogBox, 300, 200);
        dialog.setScene(dialogScene);
        dialogBox.setPadding(new Insets(10, 50, 50, 50));
        dialogBox.setSpacing(10);

        

        Button start = new Button("START NEW GAME");
        Button exit = new Button("EXIT");

        start.setOnAction(e -> restartGame(primaryStage));
        dialogBox.getChildren().add(start);
        dialogBox.getChildren().add(exit);
    
        dialog.show();

    }

    public void initializeButtons(Game game, Stage primaryStage){
        initializeNewGameButton(game, primaryStage);
        initializeUndoButton(game);
        initializeThemeButton(game, primaryStage);
    }

    private void initializeNewGameButton(Game game, Stage stage){
        newGame = new Button("New Game");
        newGame.setLayoutY(10);
        newGame.setLayoutX(10);
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
        undo.setLayoutX(10);
        undo.setOnAction(e -> {
            System.out.println("Undo move");
            // implement undo move action
        });
        game.getChildren().add(undo);
    }

    private void initializeThemeButton(Game game, Stage stage){
        Button changeTheme = new Button("Change theme");
        changeTheme.setLayoutY(70);
        changeTheme.setLayoutX(10);
        changeTheme.setOnAction(e -> {
            System.out.println(backgroundsChange);
            if (this.backgroundsChange == 0){
                game.setTableBackground(this.backgrounds[1]);
                this.backgroundsChange = 1;
            }
            else{
                game.setTableBackground(this.backgrounds[0]);
                this.backgroundsChange = 0;
            }
        });
        game.getChildren().add(changeTheme);
    }

    // private void backgroundsChangeSetter(int value){
    //     this.backgroundsChange = value;
    // }

}


