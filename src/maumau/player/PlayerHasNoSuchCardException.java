package maumau.player;

/**
 * Thrown when the player want's to play a card he doesn't have on his hand.
 */
public class PlayerHasNoSuchCardException extends Exception {
    public PlayerHasNoSuchCardException() {
        super();
    }
}
