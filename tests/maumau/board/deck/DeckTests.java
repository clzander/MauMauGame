package maumau.board.deck;

import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class DeckTests {

    /**
     * Tests if the right card was drawn when calling the draw method
     * @throws EmptyDeckException shouldn't be thrown
     */
    @Test
    public void drawTest() throws EmptyDeckException {
        DeckBackdoor deck = new MauMauDeck();

        final int LIST_LENGTH_DIFFERENCE = 1;
        final int TOP_CARD_INDEX = deck.getDeck().size() - LIST_LENGTH_DIFFERENCE;
        Card topCard = deck.getDeck().get(TOP_CARD_INDEX);

        Card drawnCard = deck.drawCard();

        Assert.assertEquals(topCard.getColor(), drawnCard.getColor());
        Assert.assertEquals(topCard.getType(), drawnCard.getType());
    }

    /**
     * Tests if the empty deck exception is thrown when no cards are left on the deck
     */
    @Test
    public void emptyDeckExceptionThrownTest() {
        DeckBackdoor deck = new MauMauDeck();
        //size needs to be stored, because it's constantly changing in the for loop
        final int deckSize = deck.getDeck().size();

        for (int i = 0; i < deckSize; i++) {
            try {
                deck.drawCard();
            } catch (EmptyDeckException e) {
                //shouldn't end here
                //check test or implementation again
                System.out.println("Deck was already empty?");
                Assert.fail();
            }
        }
        Assert.assertThrows(EmptyDeckException.class, deck::drawCard);
    }

    /**
     * Tests if the add cards method really adds a card
     */
    @Test
    public void addCardsTest() {
        List<Card> cardsToAdd = new ArrayList<>();
        Card card = new MauMauCard(CardColor.SPADES, CardType.ACE);
        cardsToAdd.add(card);

        //the deck gets an empty list, so no other cards are left except the one added
        DeckBackdoor deck = new MauMauDeck(new ArrayList<>());
        deck.addCardsToDeck(cardsToAdd);

        Assert.assertEquals(card.getColor(), deck.getDeck().get(0).getColor());
        Assert.assertEquals(card.getType(), deck.getDeck().get(0).getType());
    }
}
