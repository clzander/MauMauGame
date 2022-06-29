package maumau.player;

import maumau.board.ProvokedEmptyDeckException;
import maumau.cards.Card;
import maumau.cards.CardColor;

public interface Player {

    /**
     * Draw a card and add it to players hand
     * @throws NotPlayersTurnException if it's not the players turn
     * @throws ProvokedEmptyDeckException if the players draw so many cards, that no cards are left on the deck.
     *                                      If this exception is thrown, even the discard pile was tried to be shuffled into the deck with no success
     *                                      The exception has further information if the player has a playable card on his hand which he then must play,
     *                                      or if his turn must end, because he can neither draw nor play a card.
     */
    void drawCard() throws ProvokedEmptyDeckException, NotPlayersTurnException;


    /**
     * Play a card
     * @param card the card the player wants to play
     * @param wishedColor the color that the player wished by playing a jack
     *                    should be null if the player didn't play a jack
     * @throws NotPlayersTurnException if it's not the players turn
     * @throws PlayerHasNoSuchCardException if the player has no such card on his hand
     * @throws PlayerViolatesGameRulesException if the card can't be discarded because it would violate the rules
     */
    boolean playCard(Card card, CardColor wishedColor) throws NotPlayersTurnException, PlayerHasNoSuchCardException, PlayerViolatesGameRulesException, ProvokedEmptyDeckException;
}
