package ui.view;

import maumau.cards.Card;
import maumau.player.VisulizablePlayer;

import java.io.PrintStream;
import java.util.Objects;

public class GameVisualizer implements VisualizerBackdoor {
    private final PrintStream os;
    private final VisulizablePlayer playerView;

    //card properties
    private final int CARD_WIDTH = 15;
    private final int CARD_HEIGHT = 11;
    private final int CARD_TYPE_TOP_BOTTOM_SPACE = 0;
    private final int CARD_TYPE_SIDE_SPACE = 1;


    //card symbol properties
    private final int SYMBOL_WIDTH = 5;
    private final int SYMBOL_HEIGHT = 3;

    private final int SYMBOL_X = CARD_WIDTH / 2 - SYMBOL_WIDTH / 2;
    private final int SYMBOL_Y = CARD_HEIGHT / 2 - SYMBOL_HEIGHT / 2;


    private final int SPACE_TO_VIEW_BOARDER_TOP = 1;        //one blank-line to the top
    private final int SPACE_TO_VIEW_BOARDER_SIDES = 3;      //three blank characters to the right and left
    private final int SPACE_BETWEEN_CARDS = 2;              //between cards are two blank spaces (left and right)
    private final int NUMBER_OF_CARDS_IN_ROW = 5;           //fix number of cards that fit on the screen in a row
    private final int SPACE_BETWEEN_LAYERS = 1;

    private int VIEW_WIDTH = 1 +                                               //symbol thickness
            this.SPACE_TO_VIEW_BOARDER_SIDES +                               //space
            this.CARD_WIDTH * NUMBER_OF_CARDS_IN_ROW +                       //space for the cards
            this.SPACE_BETWEEN_CARDS * (this.NUMBER_OF_CARDS_IN_ROW - 1) +   //space between cards
            this.SPACE_TO_VIEW_BOARDER_SIDES +                               //space
            1;

    private final char VIEW_BOARDER_SYMBOL = '#';
    private final char CARD_TOP_BORDER_SYMBOL = '▄';
    private final char CARD_BOTTOM_BORDER_SYMBOL = '▀';
    private final char CARD_RIGHT_LEFT_BORDER_SYMBOL = '█';
    private final char CARD_BACK_BACKGROUND_SYMBOL = '░';
    private final char CARD_BACK_M_SYMBOL = '▓';


    private final String DISCARD_TEXT = "Discard pile:";
    private final String DECK_TEXT = "Deck:";
    private final String HAND_TEXT = "Your hand:";


    private final int TOP_LAYER_Y_INDEX = 1 + SPACE_TO_VIEW_BOARDER_TOP;
    private final int LEFT_SPACE_TO_BOARDER = 1 + SPACE_TO_VIEW_BOARDER_SIDES;

    private final int DISCARD_TEXT_X = LEFT_SPACE_TO_BOARDER;
    private final int DISCARD_TEXT_Y = TOP_LAYER_Y_INDEX;

    private final int DISCARD_START_X = DISCARD_TEXT_X + DISCARD_TEXT.length() + SPACE_BETWEEN_CARDS;
    private final int DISCARD_START_Y = TOP_LAYER_Y_INDEX;


    private final int DECK_START_X = DISCARD_START_X + CARD_WIDTH * 2 + SPACE_BETWEEN_CARDS * 2;
    private final int DECK_START_Y = TOP_LAYER_Y_INDEX;

    private final int DECK_TEXT_X = (DECK_START_X - SPACE_BETWEEN_CARDS) - DECK_TEXT.length();
    private final int DECK_TEXT_Y = TOP_LAYER_Y_INDEX;


    private final int HAND_TEXT_X = LEFT_SPACE_TO_BOARDER;
    private final int HAND_TEXT_Y = TOP_LAYER_Y_INDEX + CARD_HEIGHT + SPACE_BETWEEN_LAYERS;



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           constructor                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public GameVisualizer(PrintStream os, VisulizablePlayer player) {
        this.os = os;
        this.playerView = player;
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                  printing methods                                              //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Prints the board with the discard pile, deck, and the hand from the local player
     */
    public void printView() {
        //determine the height of the array based on the number of cards on the player hand
        final int NUMBER_OF_CARD_ROWS = this.playerView.getPlayerHand().size() / this.NUMBER_OF_CARDS_IN_ROW + 1;

        final int BORDER_HEIGHT = 1 +                                            //symbol thickness
                this.SPACE_TO_VIEW_BOARDER_TOP +                                 //space
                this.CARD_HEIGHT +                                               //for discard pile card and deck plus two extra spaces for the hand
                2 +
                NUMBER_OF_CARD_ROWS * this.CARD_HEIGHT +                         //space for card rows
                this.SPACE_TO_VIEW_BOARDER_TOP +                                 //space
                1;                                                               //symbol thickness


        final char[][] view = new char[VIEW_WIDTH][BORDER_HEIGHT];


        this.setSimpleBoarderSymbols(view);

        this.insertArrayToArrayAtPosition(view, this.getCard(this.playerView.getDiscardPileCard()), this.DISCARD_START_X, this.DISCARD_START_Y, this.CARD_WIDTH, this.CARD_HEIGHT);
        this.insertArrayToArrayAtPosition(view, this.getCardBackside(), this.DECK_START_X, this.DECK_START_Y, this.CARD_WIDTH, this.CARD_HEIGHT);
        this.insertTextToArray(view, this.DISCARD_TEXT_X, this.DISCARD_TEXT_Y, this.DISCARD_TEXT);
        this.insertTextToArray(view, this.DECK_TEXT_X, this.DECK_TEXT_Y, this.DECK_TEXT);
        this.insertTextToArray(view, this.HAND_TEXT_X, this.HAND_TEXT_Y, this.HAND_TEXT);

        //Point where the first card should be inserted
        int startXHandCards = this.LEFT_SPACE_TO_BOARDER;
        int startYHandCards = this.HAND_TEXT_Y + 1;

        //Add all hand cards
        for(int i = 0; i < this.playerView.getPlayerHand().size(); i++) {
            this.insertArrayToArrayAtPosition(view, this.getCard(this.playerView.getPlayerHand().get(i)), startXHandCards, startYHandCards, this.CARD_WIDTH, this.CARD_HEIGHT);


            if(i % 5 == 4) {
                startXHandCards = this.LEFT_SPACE_TO_BOARDER;
                startYHandCards += this.CARD_HEIGHT;
            } else {
                startXHandCards += this.SPACE_BETWEEN_CARDS + this.CARD_WIDTH;
            }
        }

        //PRINTING
        this.printArray(view);
        System.out.println(this.playerView.getDiscardPileCard().getColor().toString());
        System.out.println(this.playerView.getDiscardPileCard().getType().toString());
    }

    /**
     * Prints the box where log messages are listed
     */

    private void printArray(char[][] array) {
        final int baseArrayWidth = array.length;
        final int baseArrayHeight = array[0].length;

        for(int h = 0; h < baseArrayHeight; h++) {
            for(int w = 0; w < baseArrayWidth; w++) {
                this.os.print(array[w][h]);
            }
            this.os.print("\n");
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                  card sides view                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public char[][] getCard(Card card) {
        char[][] symbol = null;
        char color = 0;
        char type = 0;

        switch(card.getColor()) {
            case CLUBS -> {
                symbol = this.doClubsSymbol();
                color = '♣';
            }
            case HEART -> {
                symbol = this.doHeartSymbol();
                color = '♥';
            }
            case SPADES -> {
                symbol = this.doSpadesSymbol();
                color = '♠';
            }
            case DIAMONDS -> {
                symbol = this.doDiamondsSymbol();
                color = '♦';
            }
        }

        switch (card.getType()) {

            case TWO ->     type = '2';
            case THREE ->   type = '3';
            case FOUR ->    type = '4';
            case FIVE ->    type = '5';
            case SIX ->     type = '6';
            case SEVEN ->   type = '7';
            case EIGHT ->   type = '8';
            case NINE ->    type = '9';
            case TEN ->     type = 'T'; //special case for the 10 (T for ten) because it has two characters
            case JACK ->    type = 'J';
            case QUEEN ->   type = 'Q';
            case KING ->    type = 'K';
            case ACE ->     type = 'A';
        }


        final char[][] cardArray = new char[this.CARD_WIDTH][this.CARD_HEIGHT];

        for(int w = 0; w < this.CARD_WIDTH; w++) {
            for(int h = 0; h < this.CARD_HEIGHT; h++) {


                if(h == 0) {
                    //card top border
                    cardArray[w][h] = this.CARD_TOP_BORDER_SYMBOL;

                } else if (h == this.CARD_HEIGHT - 1) {
                    //card bottom border
                    cardArray[w][h] = this.CARD_BOTTOM_BORDER_SYMBOL;

                } else if (w == 0 || w == this.CARD_WIDTH - 1) {
                    //card left/right border
                    cardArray[w][h] = this.CARD_RIGHT_LEFT_BORDER_SYMBOL;

                } else {
                    cardArray[w][h] = ' ';
                }
            }
        }


        //insert card type
        if(Objects.equals(type, 'T')) {
            //top
            cardArray[1 + this.CARD_TYPE_SIDE_SPACE][this.CARD_TYPE_TOP_BOTTOM_SPACE + 1] = '1';
            cardArray[2 + this.CARD_TYPE_SIDE_SPACE][this.CARD_TYPE_TOP_BOTTOM_SPACE + 1] = '0';

            //bottom
            cardArray[this.CARD_WIDTH - (2 + this.CARD_TYPE_SIDE_SPACE)][this.CARD_HEIGHT - (2 + this.CARD_TYPE_TOP_BOTTOM_SPACE)] = '0';
            cardArray[this.CARD_WIDTH - (3 + this.CARD_TYPE_SIDE_SPACE)][this.CARD_HEIGHT - (2 + this.CARD_TYPE_TOP_BOTTOM_SPACE)] = '1';
        } else {
            //top
            cardArray[1 + this.CARD_TYPE_SIDE_SPACE][this.CARD_TYPE_TOP_BOTTOM_SPACE + 1] = type;

            //bottom
            cardArray[this.CARD_WIDTH - (2 + this.CARD_TYPE_SIDE_SPACE)][this.CARD_HEIGHT - (2 + this.CARD_TYPE_TOP_BOTTOM_SPACE)] = type;
        }

        //insert card color symbol
        cardArray[1 + this.CARD_TYPE_SIDE_SPACE][2 + CARD_TYPE_TOP_BOTTOM_SPACE] = color;
        cardArray[this.CARD_WIDTH - (2 + this.CARD_TYPE_SIDE_SPACE)][this.CARD_HEIGHT - (3 + this.CARD_TYPE_TOP_BOTTOM_SPACE)] = color;

        //insert middle card symbol
        this.insertArrayToArrayAtPosition(cardArray, symbol, this.SYMBOL_X, this.SYMBOL_Y, this.SYMBOL_WIDTH, this.SYMBOL_HEIGHT);

        return cardArray;
    }

    @Override
    public char[][] getCardBackside() {
        final char[][] cardBacksideArray = new char[this.CARD_WIDTH][this.CARD_HEIGHT];

        for(int x = 0; x < this.CARD_WIDTH; x++) {
            for(int y = 0; y < this.CARD_HEIGHT; y++) {
                if(y == 0) {
                    //card top border
                    cardBacksideArray[x][y] = this.CARD_TOP_BORDER_SYMBOL;

                } else if (y == this.CARD_HEIGHT - 1) {
                    //card bottom border
                    cardBacksideArray[x][y] = this.CARD_BOTTOM_BORDER_SYMBOL;

                } else if (x == 0 || x == this.CARD_WIDTH - 1) {
                    //card left/right border
                    cardBacksideArray[x][y] = this.CARD_RIGHT_LEFT_BORDER_SYMBOL;
                } else {
                    //background symbol
                    cardBacksideArray[x][y] = this.CARD_BACK_BACKGROUND_SYMBOL;
                }
            }
        }

        //set the both M symbols
        cardBacksideArray[6][2] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[8][2] = this.CARD_BACK_M_SYMBOL;

        cardBacksideArray[5][3] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[7][3] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[9][3] = this.CARD_BACK_M_SYMBOL;


        cardBacksideArray[5][7] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[7][7] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[9][7] = this.CARD_BACK_M_SYMBOL;

        cardBacksideArray[6][8] = this.CARD_BACK_M_SYMBOL;
        cardBacksideArray[8][8] = this.CARD_BACK_M_SYMBOL;


        return cardBacksideArray;
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          insertion methods                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void insertArrayToArrayAtPosition(char[][] baseArray, char[][] insertArray, int startX, int startY, int width, int height) {

        //the new calculated indexes for the array to insert the symbol at the correct position
        int newX;
        int newY;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                newX = x + startX;
                newY = y + startY;

                baseArray[newX][newY] = insertArray[x][y];
            }
        }
    }

    private void insertTextToArray(char[][] baseArray, int startX, int startY, String text) {
        final char[] textArray = text.toCharArray();

        for(char character : textArray) {
            baseArray[startX++][startY] = character;
        }
    }

    private void setSimpleBoarderSymbols(char[][] baseArray) {
        final int baseArrayWidth = baseArray.length;
        final int baseArrayHeight = baseArray[0].length;

        for(int i = 0; i < baseArrayWidth; i++) {
            for(int j = 0; j < baseArrayHeight; j++) {
                if(i == 0 || i == baseArrayWidth - 1 || j == 0 || j == baseArrayHeight - 1) {
                    baseArray[i][j] = this.VIEW_BOARDER_SYMBOL;
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                   symbols                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private char[][] doDiamondsSymbol() {
        char[][] symbol = new char[this.SYMBOL_WIDTH][this.SYMBOL_HEIGHT];
        symbol[0][0] = ' '; symbol[1][0] = '/';  symbol[2][0] = ' '; symbol[3][0] = '\\'; symbol[4][0] = ' ';
        symbol[0][1] = ' '; symbol[1][1] = '(';  symbol[2][1] = ' '; symbol[3][1] = ')';  symbol[4][1] = ' ';
        symbol[0][2] = ' '; symbol[1][2] = '\\'; symbol[2][2] = ' '; symbol[3][2] = '/';  symbol[4][2] = ' ';

        return symbol;
    }

    private char[][] doSpadesSymbol() {
        char[][] symbol = new char[this.SYMBOL_WIDTH][this.SYMBOL_HEIGHT];
        symbol[0][0] = ' '; symbol[1][0] = '/';  symbol[2][0] = ' '; symbol[3][0] = '\\'; symbol[4][0] = ' ';
        symbol[0][1] = '('; symbol[1][1] = ' ';  symbol[2][1] = ' '; symbol[3][1] = ' ';  symbol[4][1] = ')';
        symbol[0][2] = ' '; symbol[1][2] = ' ';  symbol[2][2] = '┴'; symbol[3][2] = ' ';  symbol[4][2] = ' ';

        return symbol;
    }

    private char[][] doHeartSymbol() {
        char[][] symbol = new char[this.SYMBOL_WIDTH][this.SYMBOL_HEIGHT];
        symbol[0][0] = '('; symbol[1][0] = '‾';  symbol[2][0] = 'V'; symbol[3][0] = '‾'; symbol[4][0] = ')';
        symbol[0][1] = ' '; symbol[1][1] = '\\'; symbol[2][1] = ' '; symbol[3][1] = '/'; symbol[4][1] = ' ';
        symbol[0][2] = ' '; symbol[1][2] = ' ';  symbol[2][2] = 'V'; symbol[3][2] = ' '; symbol[4][2] = ' ';

        return symbol;
    }

    private char[][] doClubsSymbol() {
        char[][] symbol = new char[this.SYMBOL_WIDTH][this.SYMBOL_HEIGHT];
        symbol[0][0] = ' '; symbol[1][0] = '(';  symbol[2][0] = ' '; symbol[3][0] = ')'; symbol[4][0] = ' ';
        symbol[0][1] = '('; symbol[1][1] = ' ';  symbol[2][1] = ' '; symbol[3][1] = ' '; symbol[4][1] = ')';
        symbol[0][2] = ' '; symbol[1][2] = ' ';  symbol[2][2] = '┴'; symbol[3][2] = ' '; symbol[4][2] = ' ';

        return symbol;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                   backdoor getters                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int getCardWidth() {
        return this.CARD_WIDTH;
    }

    @Override
    public int getCardHeight() {
        return this.CARD_HEIGHT;
    }

}
