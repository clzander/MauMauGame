package maumau.player;

import maumau.board.deck.Deck;
import maumau.board.deck.TCPDeck;
import network.BoardChangedListener;
import network.TCPProtocolEngine;

public interface TCPNetworkPlayer extends VisulizablePlayer {

    void setProtocolEngine(TCPProtocolEngine protocolEngine);

    void subscribeChangeListener(BoardChangedListener listener);

    boolean hasFirstTurn();

    TCPDeck getDeck();
}
