package maumau.board;

import maumau.board.deck.TCPDeck;
import maumau.cards.Card;
import maumau.board.deck.MauMauDeck;
import maumau.board.deck.EmptyDeckException;

import java.util.ArrayList;
import java.util.List;

public class MauMauBoard implements Board, BoardBackdoor, TCPNetworkBoard {
    private TCPDeck deck;
    private final List<Card> discardPile;

    //for the time being it remains seven start cards with two players
    //could be easily made variable
    private final int START_CARD_NUMBER = 7;

    private final int TOP_DISCARD_PILE_CARD_INDEX = 0;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               Constructors                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public MauMauBoard() {

        this(new MauMauDeck());
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               Methods                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Card drawCard() throws ProvokedEmptyDeckException {

        //tries to draw a card - may not work
        try {
            return this.deck.drawCard();

        } catch (EmptyDeckException e) {
            //if at least one other card besides the last played card is on the discard pile
            //the discard pile gets shuffled into the deck, except the top card which will remain in the discard pile
            if (this.discardPile.size() > 1) {
                Card lastPlayedCard = this.discardPile.remove(this.TOP_DISCARD_PILE_CARD_INDEX);
                this.deck.addCardsToDeck(this.discardPile);
                this.discardPile.clear();
                this.discardPile.add(lastPlayedCard);

                //now try again. Of course, it would work now, but the compiler doesn't know that
                try {
                    return this.deck.drawCard();
                } catch (EmptyDeckException e2) {
                    //can't happen, because at least one card was added to the deck
                    return null;
                }

            } else { //if there is no card which could be added from the discard pile an exception is thrown
                throw new ProvokedEmptyDeckException();
            }
        }
    }

    @Override
    public void playCard(Card card) {
        this.discardPile.add(this.TOP_DISCARD_PILE_CARD_INDEX, card);
    }

    @Override
    public Card getLastPlayedCard() {
        return this.discardPile.get(this.TOP_DISCARD_PILE_CARD_INDEX);
    }

    @Override
    public List<Card> getStartCards() {
        //the start cards
        List<Card> startCards = new ArrayList<>();

        //first cards get drawn from the deck
        for(int i = 0; i < this.START_CARD_NUMBER; i++) {
            try {
                startCards.add(this.deck.drawCard());
            } catch (EmptyDeckException ignored) {
                //can't happen because the deck has 52 cards at the beginning
                //with a variable count of start cards this shouldn't be ignored
            }
        }
        return startCards;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                        Constructors for testing purpose                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor for Tests
     * Draws one card from the deck so that the discard pile isn't empty
     * @param deck the deck on which the board relies
     */
    public MauMauBoard(TCPDeck deck) {
        this.deck = deck;
        this.discardPile = new ArrayList<>();
        try {
            this.discardPile.add(this.deck.drawCard());
        } catch (EmptyDeckException ignored) {
        }
    }

    /**
     * Constructor for Test
     * Doesn't draw a card from the deck -> discard pile possibly empty
     * @param deck the deck on which the board relies
     * @param discardPile the discard pile on which the board relies
     */
    public MauMauBoard(TCPDeck deck, List<Card> discardPile) {
        this.deck = deck;
        this.discardPile = discardPile;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                 network methods                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public TCPDeck getDeck() {
        return deck;
    }

    @Override
    public void setDeck(TCPDeck deck) {
        this.deck = deck;
    }

    @Override
    public void setDiscardPileCard(Card card) {
        this.discardPile.set(this.TOP_DISCARD_PILE_CARD_INDEX, card);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                backdoor methods                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getStartCardNumber() {
        return this.START_CARD_NUMBER;
    }

    @Override
    public List<Card> getDiscardPile() {
        return this.discardPile;
    }
}
