package maumau.board;

/**
 * Thrown when a MauMau deck is empty and no cards can be added from the discard pile
 */
public class ProvokedEmptyDeckException extends Exception {
    private boolean couldHavePlayed;

    public ProvokedEmptyDeckException() {
        super();
    }

    /**
     * Extended constructor
     * @param couldHavePlayed if the played could have played a card instead of drawing one
     */
    public ProvokedEmptyDeckException(boolean couldHavePlayed) {
        super();
        this.couldHavePlayed = couldHavePlayed;
    }

    public boolean getCouldHavePlayed() {
        return this.couldHavePlayed;
    }
}
