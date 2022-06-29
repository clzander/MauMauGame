package maumau.cards;

/**
 * The MauMau card
 */
public interface Card {
    /**
     * Get the color of the card (Clubs, Spades, Heart, Diamonds)
     * @return the color of the card
     */
    CardColor getColor();

    /**
     * Get the type of the card (1, 2, 3, 4, 5, 6, 7, 8, 9, Jack, Queen, King, As)
     * @return the type of the card
     */
    CardType getType();
}
