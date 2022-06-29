package maumau.board;

import maumau.board.deck.TCPDeck;
import maumau.cards.Card;

public interface TCPNetworkBoard extends Board {
    TCPDeck getDeck();

    void setDeck(TCPDeck deck);

    void setDiscardPileCard(Card discardPileCard);
}
