package maumau.player;

/**
 * Thrown when the player wants to play a card, but the play would violate the rules.
 */
public class PlayerViolatesGameRulesException extends Exception {
    public PlayerViolatesGameRulesException() {
        super();
    }
}
