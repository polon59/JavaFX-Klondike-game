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

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> { 
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }

        if(e.getClickCount() == 2 && draggedCards.isEmpty()){
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
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        if (activePile.getPileType() == Pile.PileType.TABLEAU && card.isFaceDown())
            return;
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


    // public void handle(MouseEvent mouseEvent) {
    //     if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
    //         if(mouseEvent.getClickCount() == 2){
    //             System.out.println("Double clicked");
    //         }
    //     }
    // }

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        try {
            Pile tableauPile = getValidIntersectingPile(card, tableauPiles);
            Pile foundationPile = getValidIntersectingPile(card, foundationPiles);
                
                if (tableauPile != null) {

                    Card topCard = tableauPile.getTopCard();
                    
                    if (topCard == null && checkCardRank(card, 13)) {
                        handleValidMove(card, tableauPile);
                    }
        
                    else if (card.isOppositeColor(card, topCard) && isRankSmaller(card, topCard)) {
                        handleValidMove(card, tableauPile);
                    }
        
                    else{
                        draggedCards.forEach(MouseUtil::slideBack);
                        draggedCards.clear();
                    }
                }
                else if (foundationPile != null){
                    
                    Card topCard = foundationPile.getTopCard();

                    if (topCard == null && checkCardRank(card, 1) && draggedCards.size()==1) {
                        handleValidMove(card, foundationPile);
                    }
                    else if (card.isSameSuit(card, topCard) && isRankSmaller(topCard, card) && draggedCards.size()==1){
                        handleValidMove(card, foundationPile);
                    }
                    else{
                        draggedCards.forEach(MouseUtil::slideBack);
                        draggedCards.clear();
                    }
                }

                else{
                    draggedCards.forEach(MouseUtil::slideBack);
                    draggedCards.clear();
                }
        } catch (NullPointerException a) {
            System.out.println("Intercepted Null Pointer Error");
            invalidCardMove(draggedCards);
        }

    };

    public void doubleClick(Card card){
            System.out.println(card.getRank());
            if (card.getRank() == 1){
                for (Pile pile : foundationPiles){
                    if (pile.isEmpty()){
                        draggedCards.add(card);
                        handleValidMove(card, pile);
                        break;
                    }
                }
            }
            else {
                for (Pile pile : foundationPiles){
                    if (pile.isEmpty() == false){
                        if (pile.getTopCard().getSuit() == card.getSuit() && pile.getTopCard().getRank()+1 == card.getRank()){
                            draggedCards.add(card);
                            handleValidMove(card, pile);
                            break;
                        }
                        
                    }
                }
            }
    }

    public boolean isRankSmaller(Card card, Card topCard) {
        if (card.getRank() == topCard.getRank() - 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkCardRank(Card card , int rank) {
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
        for (Pile foundationPile : foundationPiles){
            if (foundationPile.numOfCards() == 1)
                win ++;
        }
        return (win == 1); 
    }

    public Game() {
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
        this.won = isGameWon();
        if (won)
            System.out.println("gowno dziala");

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
            if (tableauPile.numOfCards() != 0){
                if (tableauPile.getTopCard().isFaceDown()) {
                    tableauPile.getTopCard().flip();
                }
            }
        });
    }
    public boolean isDraggedCardSmaller(Card card, Card topCard) {
        if (card.getRank() < topCard.getRank()) {
            return true;
        } else {
            return false;
        }
    }
}