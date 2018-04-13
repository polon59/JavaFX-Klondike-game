package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private Boolean won;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private Stage primaryStage;

    private Card lastUsedCard;
    private Pile lastUsedCardPile;

    private List<Card> usedCards = new ArrayList<>();

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        if (MouseUtil.duringAnimation) {
            return;
        }
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card = stockPile.getTopCard();
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }

        if (e.getClickCount() == 2 && draggedCards.isEmpty() && !card.isFaceDown()) {
            doubleClick(card);
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        if (stockPile.isEmpty())
            refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        if (MouseUtil.duringAnimation) {
            return;
        }
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown())
            return;
        if (activePile.getPileType() == Pile.PileType.DISCARD)
            card = discardPile.getTopCard();
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();

        if (activePile.getPileType() == Pile.PileType.TABLEAU) {
            for (Card tableauCard : activePile.getCards()) {
                if (!tableauCard.isFaceDown()) {
                    if (isDraggedCardSmaller(tableauCard, card)) {
                        draggedCards.add(tableauCard);
                    } else {
                        draggedCards.add(card);
                    }
                }
            }
        } else {
            draggedCards.add(card);
        }

        for (Card drag : draggedCards) {
            drag.getDropShadow().setRadius(20);
            drag.getDropShadow().setOffsetX(10);
            drag.getDropShadow().setOffsetY(10);
            drag.toFront();
            drag.setTranslateX(offsetX);
            drag.setTranslateY(offsetY);
        }

    };


    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (MouseUtil.duringAnimation) {
            return;
        }
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        lastUsedCard = card;
        Pile tableauPile = getValidIntersectingPile(card, tableauPiles);
        Pile foundationPile = getValidIntersectingPile(card, foundationPiles);
        
        try {

            if (usedCards != null) {
                usedCards.clear();

                for (int a = 0; a < draggedCards.size(); a++) {
                    usedCards.add(draggedCards.get(a));

                }
            }

            lastUsedCardPile = card.getContainingPile();
        }
            catch (NullPointerException a) {
                System.out.println("Intercepted Null Pointer Error");
                invalidCardMove(draggedCards);
            }
            cardPlacementLogic(card, tableauPile, foundationPile);

            
    };

    public void cardPlacementLogic(Card card, Pile tableauPile, Pile foundationPile){

        if (MouseUtil.duringAnimation) {
            return;
        }

        try {
        if (tableauPile != null) {

            Card topCard = tableauPile.getTopCard();

            if (topCard == null && checkCardRank(card, 13)) {
                handleValidMove(card, tableauPile);
            }

            else if (card.isOppositeColor(card, topCard) && isRankSmaller(card, topCard)) {
                handleValidMove(card, tableauPile);
            }

            else {
                draggedCards.forEach(MouseUtil::slideBack);
            }
        } else if (foundationPile != null) {

            Card topCard = foundationPile.getTopCard();
            if (topCard == null && checkCardRank(card, 1)) {
                handleValidMove(card, foundationPile);
            } else if (card.isSameSuit(card, topCard) && isRankSmaller(topCard, card)) {
                handleValidMove(card, foundationPile);
            } else {
                draggedCards.forEach(MouseUtil::slideBack);
            }
        }

        else

        {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    }

     catch (NullPointerException a) {
        System.out.println("Intercepted Null Pointer Error");
        invalidCardMove(draggedCards);
    }
    draggedCards.clear();
    }
    public void doubleClick(Card card) {
        if (MouseUtil.duringAnimation) {
            return;
        }
        if (card.getRank() == 1) {
            for (Pile pile : foundationPiles) {
                if (pile.isEmpty()) {
                    draggedCards.add(card);
                    handleValidMove(card, pile);
                    break;
                }
            }
        } else {
            for (Pile pile : foundationPiles) {
                if (pile.isEmpty() == false) {
                    if (pile.getTopCard().getSuit() == card.getSuit()
                            && pile.getTopCard().getRank() + 1 == card.getRank()) {
                        draggedCards.add(card);
                        handleValidMove(card, pile);
                        break;
                    }
                }
            }
        }
    }

    protected void undoMove() {
        if (usedCards != null && lastUsedCard != null) {
            System.out.println("SIZE" + usedCards.size());
            lastUsedCardPile.getTopCard().flip();
            for (int i = 0; i < usedCards.size(); i++) {
                usedCards.get(i).moveToPile(lastUsedCardPile);
                System.out.println("undo card");
            }
            usedCards.clear();
            lastUsedCard = null;
        }

    }

    public boolean isRankSmaller(Card card, Card topCard) {
        if (card.getRank() == topCard.getRank() - 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkCardRank(Card card, int rank) {
        if (card.getRank() == rank) {
            return true;
        } else {
            return false;
        }
    }

    public void invalidCardMove(List<Card> draggedCards) {
        draggedCards.forEach(MouseUtil::slideBack);
        draggedCards.clear();
        System.out.println("INVALIID MOVE");
    }

    public boolean isGameWon() {
        int win = 0;
        for (Pile foundationPile : foundationPiles) {
            if (foundationPile.numOfCards() == 13)
                win++;
        }
        return (win == 4);
    }

    public Game(Stage primaryStage) {
        this.primaryStage = primaryStage;
        deck = Card.createNewDeck();
        won = false;
        initPiles();
        dealCards();

    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {

        FXCollections.reverse(discardPile.getCards());
        for (Card card : discardPile.getCards()) {
            card.flip();
            stockPile.addCard(card);
        }
        discardPile.clear();

        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        //TODO
        return true;
    }

    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) && isOverPile(card, pile) && isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        if (draggedCards.isEmpty())
            draggedCards.add(card);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
        flipDownedCards();

        if (isPossibleEnd()) {
            automaticEnd();
        }
        this.won = isGameWon();
        if (won) {
            initializeDialogBox();
            System.out.println("gowno dziala");
        }

    }

    private void initPiles() {
        int amount;
        int firstElement = 0;
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);

            amount = i + 1;
            for (int j = amount; j > 0; j--) {
                if (j == 1) {
                    deck.get(firstElement).flip();
                }

                tableauPile.addCard(deck.get(firstElement));
                addMouseEventHandlers(deck.get(firstElement));
                getChildren().add(deck.get(firstElement));
                deck.remove(deck.get(firstElement));
            }
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground, BackgroundRepeat.REPEAT,
                BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

    public void flipDownedCards() {
        Iterator<Pile> fliper = tableauPiles.iterator();
        fliper.forEachRemaining(tableauPile -> {
            if (!tableauPile.isEmpty()) {
                if (tableauPile.getTopCard().isFaceDown()) {
                    tableauPile.getTopCard().flip();
                }
            }
        });
        if (!discardPile.isEmpty() && discardPile.getTopCard().isFaceDown()) {
            discardPile.getTopCard().flip();
        }

    }

    public boolean isDraggedCardSmaller(Card card, Card topCard) {
        if (card.getRank() < topCard.getRank()) {
            return true;
        } else {
            return false;
        }
    }

    private void automaticEnd() {
        for (Pile foundationPile : foundationPiles) {
            Card topCard = foundationPile.getTopCard();
            for (Pile tableuPile : tableauPiles) {
                for (Card card : tableuPile.getCards()) {
                    if (isRankSmaller(topCard, card) && card.isSameSuit(card, topCard)) {
                        draggedCards.add(card);
                        handleValidMove(card, topCard.getContainingPile());
                    }
                }
            }
        }

    }

    private Boolean isPossibleEnd() {
        if (stockPile.isEmpty() && discardPile.isEmpty()) {
            for (Pile tableauPile : tableauPiles) {
                for (Card card : tableauPile.getCards()) {
                    if (card.isFaceDown())
                        return false;
                }
            }
            return true;
        } else
            return false;
    }

    public void initializeDialogBox() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        VBox dialogBox = new VBox(50);
        dialogBox.getChildren().add(new Text("CONGRATULATIONS! YOU WON!"));
        Scene dialogScene = new Scene(dialogBox, 300, 100);
        dialog.setScene(dialogScene);
        dialogBox.setPadding(new Insets(10, 50, 50, 50));
        dialogBox.setSpacing(10);

        Button start = new Button("START NEW GAME");
        Button exit = new Button("EXIT");

        start.setOnAction(e -> {
            Klondike newGame = new Klondike();
            newGame.start(primaryStage);
            dialog.hide();});
        exit.setOnAction(e -> primaryStage.close());
        dialogBox.getChildren().add(start);
        dialogBox.getChildren().add(exit);
        dialog.initStyle(StageStyle.UNDECORATED);

        dialog.show();
    }
}