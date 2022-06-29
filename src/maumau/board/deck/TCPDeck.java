package maumau.board.deck;

import maumau.cards.Card;

import java.util.List;

public interface TCPDeck extends Deck {
    List<Card> getDeckAsList();
}
