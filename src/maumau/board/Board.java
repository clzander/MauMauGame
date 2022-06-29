package maumau.board;

import maumau.cards.Card;

import java.util.List;


/**
 * The board of a MauMau game
 * It consists of the deck and the discard pile. The board doesn't check the rules like in reality.
 * It's just the place where deck and discard pile are "stored" and game actions can be made by players
 */
public interface Board {
    /**
     * Draw a card.
     * @return the drawn card
     */
    Card drawCard() throws ProvokedEmptyDeckException;

    /**
     * Play a card.
     * @param card the card to play
     */
    void playCard(Card card);

    /**
     * Get the last played card from the discard pile.
     * In real life this would be having a look at the discard pile.
     * @return the last played card
     */
    Card getLastPlayedCard();

    /**
     * Get the seven start cards every player starts with
     * @return the cards as a list
     */
    List<Card> getStartCards();
}
