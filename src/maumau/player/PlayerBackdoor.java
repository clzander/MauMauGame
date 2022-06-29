package maumau.player;

/**
 * Backdoor for testing the player and other related classes
 */
interface PlayerBackdoor extends VisulizablePlayer {

    /**
     * Increase the players turn index manually
     * SHOULD NEVER BE CALLED OUTSIDE TESTING ENVIRONMENT! THE INCREMENTING IS DONE AUTOMATICALLY!
     */
    void increasePlayerIndex();
}
