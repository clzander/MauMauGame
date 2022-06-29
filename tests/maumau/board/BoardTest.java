package maumau.board;

import maumau.board.deck.Deck;
import maumau.board.deck.MauMauDeck;
import maumau.board.deck.TCPDeck;
import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class BoardTest {

    /**
     * Tests if drawing a card returns the right card
     * @throws ProvokedEmptyDeckException shouldn't be thrown
     */
    @Test
    public void drawCardTest() throws ProvokedEmptyDeckException {
        List<Card> deckCards = new ArrayList<>();
        Card deckCard = new MauMauCard(CardColor.CLUBS, CardType.TWO);
        deckCards.add(deckCard);

        List<Card> discardPile = new ArrayList<>();
        //just a card, so no card is drawn
        discardPile.add(new MauMauCard(CardColor.SPADES, CardType.TWO));

        TCPDeck deck = new MauMauDeck(deckCards);

        Board board  = new MauMauBoard(deck, discardPile);

        Card drawnCard = board.drawCard();

        Assert.assertEquals(deckCard.getColor(), drawnCard.getColor());
        Assert.assertEquals(deckCard.getType(), drawnCard.getType());
    }


    /**
     * Tests if the right amount of cards are drawn at the beginning
     */
    @Test
    public void rightStartCardNumberCardsDrawn() {
        BoardBackdoor board = new MauMauBoard();
        List<Card> startCards = board.getStartCards();

        Assert.assertEquals(board.getStartCardNumber(), startCards.size());
    }


    /**
     * Tests if a played card is added to the discard pile
     */
    @Test
    public void cardIsPlayed() {
        Board board = new MauMauBoard();
        Card cardToBePlayed = new MauMauCard(CardColor.SPADES, CardType.TWO);
        board.playCard(cardToBePlayed);

        Card lastPlayedCard = board.getLastPlayedCard();

        Assert.assertEquals(cardToBePlayed.getColor(), lastPlayedCard.getColor());
        Assert.assertEquals(cardToBePlayed.getType(), lastPlayedCard.getType());
    }


    /**
     * Tests if all but the last played card from the discard pile are added when the deck is empty
     * @throws ProvokedEmptyDeckException shouldn't be thrown
     */
    @Test
    public void discardPileCorrectlyAddedToDeck() throws ProvokedEmptyDeckException {
        List<Card> discardPile = new ArrayList<>();
        //the card which should remain after the discard pile is partially added to the deck
        Card remainingCard = new MauMauCard(CardColor.SPADES, CardType.TWO);
        discardPile.add(remainingCard);

        //this card is currently on the discard pile, but it should be added to the deck later
        Card deckCard = new MauMauCard(CardColor.CLUBS, CardType.THREE);
        discardPile.add(deckCard);

        //new board with empty deck
        BoardBackdoor board = new MauMauBoard(new MauMauDeck(new ArrayList<>()), discardPile);

        //this method call should add the cards (except the last played card) to the deck
        Card drawnCard = board.drawCard();

        //only one card should be played
        Assert.assertEquals(board.getDiscardPile().size(), 1);

        //the drawn card should be the deckCard
        Assert.assertEquals(drawnCard.getColor(), deckCard.getColor());
        Assert.assertEquals(drawnCard.getType(), deckCard.getType());

        Card actualRemainingCard = board.getLastPlayedCard();

        //the card left should be the remainingCard
        Assert.assertEquals(actualRemainingCard.getColor(), remainingCard.getColor());
        Assert.assertEquals(actualRemainingCard.getType(), remainingCard.getType());
    }

    /**
     * Tests if a provoked empty deck exception is thrown when the deck is empty can can't be refilled with cards
     *      from the discard pile
     */
    @Test
    public void provokedEmptyDeckExceptionThrown() {
        List<Card> deckCards = new ArrayList<>();
        //two cards are on the deck
        //one is drawn so that the discard pile isn't empty
        //the second on can be drawn to make sure that the exception isn't thrown to early
        deckCards.add(new MauMauCard(CardColor.SPADES, CardType.FOUR));
        deckCards.add(new MauMauCard(CardColor.CLUBS, CardType.FIVE));
        TCPDeck deck = new MauMauDeck(deckCards);
        Board board = new MauMauBoard(deck);

        try {
            board.drawCard();
        } catch (ProvokedEmptyDeckException e) {
            //if already thrown here the implementation is wrong
            Assert.fail();
        }

        Assert.assertThrows(ProvokedEmptyDeckException.class, board::drawCard);
    }
}
