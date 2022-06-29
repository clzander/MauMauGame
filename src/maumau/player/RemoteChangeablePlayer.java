package maumau.player;

import maumau.board.deck.Deck;
import maumau.board.deck.TCPDeck;
import maumau.cards.Card;
import maumau.cards.CardColor;

public interface RemoteChangeablePlayer extends TCPNetworkPlayer {

    void updateDeck();

    void updateDiscardPile(Card card);

    void updateLastWishedColor(CardColor latestColorWish);

    void incrementSevenStackCount();

    void resetSevenStackCount();

    void incrementCurrentPlayerIndex();

    void notifyBoardChanged();

    void synchronizeDeck(TCPDeck deck);

    void synchronizeFirstDiscardPileCard(Card discardPileCard);
}
