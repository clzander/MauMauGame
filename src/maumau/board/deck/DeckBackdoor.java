package maumau.board.deck;

import maumau.cards.Card;

import java.util.List;

/**
 * Backdoor for testing the deck and related classes
 */
interface DeckBackdoor extends Deck {

    /**
     * Returns the deck as list
     * @return
     */
    List<Card> getDeck();
}
