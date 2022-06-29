package maumau.board;


import maumau.cards.Card;

import java.util.List;

interface BoardBackdoor extends Board {

    int getStartCardNumber();

    List<Card> getDiscardPile();
}
