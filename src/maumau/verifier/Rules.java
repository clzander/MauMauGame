package maumau.verifier;

import maumau.cards.Card;
import maumau.cards.CardColor;

/**
 * The rule book which the player asks if his play is valid.
 * The rules only check if a certain card could be played based on the last played card and the color that was wished.
 * There are no checks if it's actually the players turn or if the player has this card on his hand.
 */
public interface Rules {

    /**
     * Verifies if the selected card can be played without violating the game rules
     *
     * @param cardToBePlayed the card which is tired to be play
     * @param lastPlayedCard the last card played (top card on the discard pile)
     * @param wishedColor the color that can only be played, because someone played a jack before
     *                    if no jack was played -> null
     *                    methods ignores wishedColor parameter if the last played card isn't a jack
     *
     * @return if the card can be played or not
     */
    boolean canPlayCard(Card cardToBePlayed, Card lastPlayedCard, CardColor wishedColor);

}
