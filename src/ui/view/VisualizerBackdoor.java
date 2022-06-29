package ui.view;

import maumau.cards.Card;

interface VisualizerBackdoor {


    char[][] getCard(Card card);

    char[][] getCardBackside();

    int getCardWidth();

    int getCardHeight();
}
