package maumau.player;

import maumau.board.Board;
import maumau.board.MauMauBoard;
import maumau.board.ProvokedEmptyDeckException;
import maumau.board.TCPNetworkBoard;
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

public class UsageTests {

    /**
     * Tests if the player can't play if it's not his turn
     */
    @Test
    public void notPlayersTurnExceptionTest() {
        VisulizablePlayer alice = new MauMauPlayer("Alice", 1);
        Assert.assertThrows(NotPlayersTurnException.class, alice::drawCard);
    }

    /**
     * Tests if the player can't play a card he doesn't have on his hand
     */
    @Test
    public void playerHasNoSuchCardException() {
        final Card aliceCard = new MauMauCard(CardColor.CLUBS, CardType.ACE);
        final Card notAliceCard = new MauMauCard(CardColor.HEART, CardType.TEN);

        List<Card> aliceHandCards = new ArrayList<>();
        aliceHandCards.add(aliceCard);
        VisulizablePlayer alice = new MauMauPlayer("Alice", 0, aliceHandCards);
        Assert.assertThrows(PlayerHasNoSuchCardException.class, () -> alice.playCard(notAliceCard, null));
    }

    /**
     * Tests if a simple game would run through without problems
     * @throws PlayerHasNoSuchCardException shouldn't be thrown
     * @throws NotPlayersTurnException shouldn't be thrown
     * @throws PlayerViolatesGameRulesException shouldn't be thrown
     * @throws ProvokedEmptyDeckException shouldn't be thrown
     */
    @Test
    public void playSimpleGoodGame() throws PlayerHasNoSuchCardException, NotPlayersTurnException, PlayerViolatesGameRulesException, ProvokedEmptyDeckException {
        //define everything separately to have "full control"
        List<Card> deckList = setupDeckSimpleGoodGame();
        TCPDeck deck = new MauMauDeck(deckList);
        TCPNetworkBoard board = new MauMauBoard(deck);

        List<Card> aliceHand = new ArrayList<>();
        //alice start hand cards
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.TWO));
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.THREE));
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.FOUR));
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.FIVE));
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.SIX));
        aliceHand.add(new MauMauCard(CardColor.CLUBS, CardType.SEVEN));


        List<Card> bobHand = new ArrayList<>();
        //bob start hand cards
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.NINE));
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.TEN));
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.JACK));
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.QUEEN));
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.KING));
        bobHand.add(new MauMauCard(CardColor.CLUBS, CardType.ACE));


        PlayerBackdoor alice = new MauMauPlayer("Alice", 0, aliceHand, board);
        PlayerBackdoor bob = new MauMauPlayer("Bob", 1, bobHand, board);

        Assert.assertFalse(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.TWO), null));
        this.updatePlayerTurnIndex(bob);

        Assert.assertFalse(bob.playCard(new MauMauCard(CardColor.CLUBS, CardType.NINE), null));
        this.updatePlayerTurnIndex(alice);

        Assert.assertFalse(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.THREE), null));
        this.updatePlayerTurnIndex(bob);

        Assert.assertFalse(bob.playCard(new MauMauCard(CardColor.CLUBS, CardType.TEN), null));
        this.updatePlayerTurnIndex(alice);

        Assert.assertFalse(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.FOUR), null));
        this.updatePlayerTurnIndex(bob);

        Assert.assertFalse(bob.playCard(new MauMauCard(CardColor.CLUBS, CardType.JACK), null));
        this.updatePlayerTurnIndex(alice);

        Assert.assertFalse(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.FIVE), null));
        this.updatePlayerTurnIndex(bob);

        Assert.assertFalse(bob.playCard(new MauMauCard(CardColor.CLUBS, CardType.QUEEN), null));
        this.updatePlayerTurnIndex(alice);

        Assert.assertFalse(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.SIX), null));
        this.updatePlayerTurnIndex(bob);

        Assert.assertFalse(bob.playCard(new MauMauCard(CardColor.CLUBS, CardType.KING), null));
        this.updatePlayerTurnIndex(alice);

        Assert.assertTrue(alice.playCard(new MauMauCard(CardColor.CLUBS, CardType.SEVEN), null));
    }

    private void updatePlayerTurnIndex(PlayerBackdoor player) {
        player.increasePlayerIndex();
    }

    private List<Card> setupDeckSimpleGoodGame() {
        List<Card> cards = new ArrayList<>();

        //the first card on the discard pile
        cards.add(new MauMauCard(CardColor.SPADES, CardType.TWO));

        return cards;
    }
}