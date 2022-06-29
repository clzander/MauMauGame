package maumau.player;

import maumau.board.ProvokedEmptyDeckException;
import maumau.cards.Card;
import maumau.cards.CardColor;

import java.util.List;

/**
 * The player.
 * He is totally honest and like in real life always knows whose turn it is.
 * Also, he asks the rules weather he can play a certain card or not.
 * He as access to the board which is also inspired by reality.
 */
public interface VisulizablePlayer extends Player{

    /**
     * @return the last played card
     */
    Card getDiscardPileCard();

    /**
     * @return the hand of the player
     */
    List<Card> getPlayerHand();

}
