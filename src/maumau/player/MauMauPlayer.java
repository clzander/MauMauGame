package maumau.player;

import maumau.board.Board;
import maumau.board.MauMauBoard;
import maumau.board.ProvokedEmptyDeckException;
import maumau.board.TCPNetworkBoard;
import maumau.board.deck.Deck;
import maumau.board.deck.TCPDeck;
import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.verifier.MauMauRules;
import maumau.verifier.Rules;
import network.*;

import java.util.ArrayList;
import java.util.List;

public class MauMauPlayer implements VisulizablePlayer, TCPNetworkPlayer, PlayerBackdoor,
        GameSessionEstablishedListener, RemoteChangeablePlayer {
    private final String name;
    private final List<Card> hand;
    private final int turnIndex;
    private final Rules verifier;
    private final TCPNetworkBoard board;

    private int currentPlayerIndex;
    private int sevenStackCount;
    private CardColor latestColorWish;


    private TCPProtocolEngine protocolEngine;
    private List<BoardChangedListener> boardChangedListenerList;
    private String partnerName;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              constructors                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Just for testing purposes
     */
    public MauMauPlayer(String name, int turnPosition, List<Card> hand, TCPNetworkBoard board) {
        this.name = name;
        this.hand = hand;
        this.turnIndex = turnPosition;
        this.verifier = new MauMauRules();
        this.board = board;

        this.currentPlayerIndex = 0;
        this.sevenStackCount = 0;

        this.boardChangedListenerList = new ArrayList<>();
    }

    /**
     * Just for testing purposes
     */
    public MauMauPlayer(String name, int turnPosition, List<Card> hand ) {
        this(name, turnPosition, hand, new MauMauBoard());
    }

    public MauMauPlayer(String name, int turnPosition) {
        this(name, turnPosition, new ArrayList<>(), new MauMauBoard());
        this.drawStartCards();
    }


    private void drawStartCards() {
        this.hand.addAll(this.board.getStartCards());
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               methods                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void drawCard() throws NotPlayersTurnException, ProvokedEmptyDeckException {
        //if it's the players turn
        if(this.isPlayersTurn()) {
            //try to draw a card from the deck
            try {
                this.hand.add(this.board.drawCard());
                this.increasePlayerIndex();
                //this.increasePlayerIndex();
            } catch (ProvokedEmptyDeckException e) {
                //if the deck is empty and can't be refilled the player either has to play a card or his turn ends
                //because ha can't play a card.
                //This needs to be checked here, so that the turn of the player can be ended if necessary.
                throw new ProvokedEmptyDeckException(this.hasFittingCard());
            }
        } else {
            //if it's not the players turn an exception gets thrown
            throw new NotPlayersTurnException();
        }

        this.protocolEngine.drawCard();
    }

    @Override
    public boolean playCard(Card card, CardColor wishedColor) throws NotPlayersTurnException, PlayerHasNoSuchCardException, PlayerViolatesGameRulesException, ProvokedEmptyDeckException {
        boolean hasWon = false;
        //if it's the players turn
        if(this.isPlayersTurn()) {
            //try play this card. Fails if the player doesn't have this card
            try {
                //may throw an exception when the card can't be found in the players hand
                int cardIndex = this.getCardIndex(card);

                if(this.verifier.canPlayCard(card, this.board.getLastPlayedCard(), this.latestColorWish)) {
                    //if the card is allowed to play

                    this.board.playCard(card);
                    this.hand.remove(cardIndex);

                    //here are special rules implemented

                    switch(card.getType()) {
                        //if the card is a seven the seven stack count is incremented
                        case SEVEN -> this.sevenStackCount++;

                        //if the card is an eight the player index is incremented yet already so that on the end of the
                        //method it was incremented twice
                        case EIGHT -> this.increasePlayerIndex();

                        case JACK -> this.latestColorWish = wishedColor;


                        default -> {
                            //if it's not a seven this play may end a seven streak
                            if(this.sevenStackCount > 0) {
                                //foreach seven stack count must be drawn two cards
                                final int numCardsForSeven = 2;
                                for(int i = 0; i < sevenStackCount * numCardsForSeven; i++) {
                                    try {
                                        drawCard();
                                    } catch (ProvokedEmptyDeckException e) {
                                        //if the player is lucky, there are not enough cards left to draw
                                        throw new ProvokedEmptyDeckException();
                                    }
                                }
                                this.sevenStackCount = 0;
                            }
                        }
                    }

                    //at the end the player index gets incremented
                    this.increasePlayerIndex();

                    //check if the player has won
                    if(this.hasWon()) {
                        hasWon = true;
                    }
                } else {
                    throw new PlayerViolatesGameRulesException();
                }

            } catch (PlayerHasNoSuchCardException e) {
                //if the card the player wished to play wasn't found in his hand
                throw new PlayerHasNoSuchCardException();
            }
        } else {
            //if it's not the players turn an exception gets thrown
            throw new NotPlayersTurnException();
        }


        this.protocolEngine.playCard(card, wishedColor);



        return hasWon;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               network                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void setProtocolEngine(TCPProtocolEngine protocolEngine) {
        this.protocolEngine = protocolEngine;
        this.protocolEngine.subscribeGameSessionEstablishedListener(this);
    }

    @Override
    public void gameSessionEstablished(String partnerName) {
        this.partnerName = partnerName;
    }

    @Override
    public void subscribeChangeListener(BoardChangedListener listener) {
        this.boardChangedListenerList.add(listener);
    }



    @Override
    public boolean hasFirstTurn() {
        return this.turnIndex == 0;
    }

    @Override
    public TCPDeck getDeck() {
        return this.board.getDeck();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               changes from remote player                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void updateDeck() {
        try {
            this.board.drawCard();
        } catch (ProvokedEmptyDeckException ignored) {
        }
    }

    @Override
    public void updateDiscardPile(Card card) {
        this.board.playCard(card);
    }

    @Override
    public void updateLastWishedColor(CardColor latestColorWish) {
        this.latestColorWish = latestColorWish;
    }

    @Override
    public void incrementSevenStackCount() {
        this.sevenStackCount++;
    }

    @Override
    public void resetSevenStackCount() {
        this.sevenStackCount = 0;
    }

    @Override
    public void incrementCurrentPlayerIndex() {
        this.increasePlayerIndex();
    }

    @Override
    public void notifyBoardChanged() {
        //if there are any listeners
        if(this.boardChangedListenerList.size() > 0) {
            new Thread(() -> {
                for(BoardChangedListener listener : boardChangedListenerList) {
                    listener.boardChanged();
                }
            }).start();
        }
    }

    @Override
    public void synchronizeDeck(TCPDeck deck) {
        this.board.setDeck(deck);
    }

    @Override
    public void synchronizeFirstDiscardPileCard(Card discardPileCard) {
        this.board.setDiscardPileCard(discardPileCard);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               private helper                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private int getCardIndex(Card card) throws PlayerHasNoSuchCardException {
        for (int i = 0; i < this.hand.size(); i++) {
            if(card.getColor() == this.hand.get(i).getColor() && card.getType() == this.hand.get(i).getType()){
                return i;
            }
        }
        throw new PlayerHasNoSuchCardException();
    }

    private boolean isPlayersTurn() {
        return this.turnIndex == this.currentPlayerIndex;
    }

    private boolean hasWon() {
        return this.hand.size() == 0;
    }

    private boolean hasFittingCard() {
        final Card topCard = this.board.getLastPlayedCard();
        boolean hasCard = false;
        boolean doFurtherSearch = true;
        int index = 0;
        int LIST_INDEX_DIFFERENCE = 1;

        while(doFurtherSearch) {
            if(topCard.getColor() == this.hand.get(index).getColor() || this.hand.get(index).getType() == CardType.JACK) {
                doFurtherSearch = false;
                hasCard = true;
            } else {
                //if the last card from the players hand was searched
                if (index == this.hand.size() - LIST_INDEX_DIFFERENCE) {
                    doFurtherSearch = false;
                }
            }
            index++;
        }
        return hasCard;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               view methods                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Card getDiscardPileCard() {
        return this.board.getLastPlayedCard();
    }

    @Override
    public List<Card> getPlayerHand() {
        return this.hand;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               backdoor methods                                                 //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void increasePlayerIndex() {
        this.currentPlayerIndex = (this.currentPlayerIndex == 1) ? 0 : 1;
    }


}
