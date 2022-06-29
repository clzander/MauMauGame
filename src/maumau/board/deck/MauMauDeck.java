package maumau.board.deck;

import maumau.cards.Card;
import maumau.cards.MauMauCard;
import maumau.cards.CardColor;
import maumau.cards.CardType;

import java.util.ArrayList;
import java.util.List;

public class MauMauDeck implements Deck, DeckBackdoor, TCPDeck {
    private final List<Card> deck;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               Constructors                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The standard constructor which should be called to initialize a standard MauMau deck.
     * It consists of 52 cards (2-10, Jack, Queen, King, Ace) * (4 card colors) = 13 * 4 = 52.
     * The deck is shuffled after it's filled with all cards.
     */
    public MauMauDeck() {
        this(new ArrayList<>());

        //add every card to the deck
        for(int i = 0; i < CardColor.values().length; i++) {
            for(int j = 0; j < CardType.values().length; j++) {
                this.deck.add(new MauMauCard(CardColor.values()[i], CardType.values()[j]));
            }
        }

        //shuffle the deck
        this.shuffleDeck();
    }

    /**
     * Shuffles the deck
     */
    private void shuffleDeck() {
        Card copiedCard;

        for(int i = 0; i < this.deck.size(); i++) {
            int randomIndex = (int) (Math.random() * this.deck.size());
            copiedCard = this.deck.get(randomIndex);
            this.deck.set(randomIndex,this.deck.get(i));
            this.deck.set(i,copiedCard);
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               Methods                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Card drawCard() throws EmptyDeckException {
        //because the deck is always upside-down, the card to draw is the last card in the list
        final int TOP_CARD = this.deck.size() - 1;

        //if at least one card is left a card can be drawn
        if(this.deck.size() > 0) {
            return this.deck.remove(TOP_CARD);

        } else { //if the deck is empty an exception is thrown
            throw new EmptyDeckException();
        }
    }

    @Override
    public void addCardsToDeck(List<Card> cards) {
        this.deck.addAll(cards);
        this.shuffleDeck();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      network                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<Card> getDeckAsList() {
        return this.deck;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Constructors for testing purposes                                         //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This constructor should only be called from a test environment
     * @param deck The list of cards making up the deck.
     *             The last card in the list is the first card drawn.
     *             The list won't get shuffled.
     */
    public MauMauDeck(List<Card> deck) {
        this.deck = deck;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              Backdoor Methods                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public List<Card> getDeck() {
        return this.deck;
    }
}
