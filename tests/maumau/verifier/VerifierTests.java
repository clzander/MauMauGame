package maumau.verifier;

import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VerifierTests {

    @Test
    public void rightColorCardAccepted() {
        Rules ruleVerifier = new MauMauRules();

        //foreach card color
        for(CardColor cardColor : CardColor.values()) {
            //just an example card with the current color and no jack
            Card currentCard = new MauMauCard(cardColor, CardType.ACE);
            //foreach card type from this color
            for (CardType cardType : CardType.values()) {
                //see if true is returned
                //wished color is null because every card is an ace and not a jack
                Assert.assertTrue(ruleVerifier.canPlayCard(currentCard, new MauMauCard(cardColor, cardType), null));
            }
        }
    }

    @Test
    public void rightTypeCardAccepted() {
        Rules ruleVerifier = new MauMauRules();

        //foreach card color
        for(CardType cardType : CardType.values()) {
            //just an example card with the current type and any color
            Card currentCard = new MauMauCard(CardColor.CLUBS, cardType);
            //foreach card color from this type
            for (CardColor cardColor : CardColor.values()) {
                //see if true is returned
                //the wished card color is always the color of the card, that should be played
                Assert.assertTrue(ruleVerifier.canPlayCard(new MauMauCard(cardColor, cardType), currentCard, cardColor));
            }
        }
    }

    /**
     * Tests if a jack can be played if the current card is any but a jack (-> wished color == null)
     */
    @Test
    public void anyJackAccepted() {
        Rules ruleVerifier = new MauMauRules();
        //foreach card color
        for(CardColor cardColor : CardColor.values()) {
            //just an example card with the current color and no jack
            Card currentCard = new MauMauCard(cardColor, CardType.ACE);

            //Select a different card color then the current one to make sure it was checked for a jack
            CardColor differentCardColor = null;
            switch (cardColor) {
                case CLUBS, HEART, SPADES -> differentCardColor = CardColor.DIAMONDS;
                case DIAMONDS -> differentCardColor = CardColor.CLUBS;
            }

            //the wished card color is not the card color of to be played Jack, to make sure that a jack can be played even if another color was wished
            Assert.assertTrue(ruleVerifier.canPlayCard(new MauMauCard(differentCardColor, CardType.JACK), currentCard, null));
        }
    }

    /**
     * Tests if a jack can be played even if the last played card is a jack and the wished color isn't the one from
     *      the jack which is now played.
     */
    @Test
    public void jackOnJackAccepted() {
        Rules ruleVerifier = new MauMauRules();

        //foreach card color
        for(CardColor lastJackCardColor : CardColor.values()) {

            Card lastJack = new MauMauCard(lastJackCardColor, CardType.JACK);

            //foreach
            for(CardColor currentJackCardColor : CardColor.values()) {

                Card currentJack = new MauMauCard(currentJackCardColor, CardType.JACK);

                if(lastJackCardColor != currentJackCardColor) {
                    //check if the current jack can be played even if he is from another color than the wished color is
                    Assert.assertTrue(ruleVerifier.canPlayCard(currentJack, lastJack, lastJackCardColor));
                }
            }
        }
    }

    /**
     * Tests if a card can't be played if its play would violate the rules
     */
    @Test
    public void wrongCardRejected() {
        Rules ruleVerifier = new MauMauRules();

        //foreach card color
        for(CardColor cardColor : CardColor.values()) {
            //foreach card type from this color
            for (CardType cardType : CardType.values()) {
                //just an example card with the current color and type representing any card on the discard pile
                Card currentCard = new MauMauCard(cardColor, cardType);


                for(CardColor differentCardColor : CardColor.values()) {
                    if(differentCardColor == cardColor) {
                        continue; //skip this color if it's already the color from the card on the discard pile
                    }
                    for (CardType differentCardType : CardType.values()) {
                        if(differentCardType == cardType || differentCardType == CardType.JACK ) {
                            continue; //skip this type if it's the type of the card on the discard pile or a jack
                        }
                        Assert.assertFalse(ruleVerifier.canPlayCard(new MauMauCard(differentCardColor, differentCardType), currentCard, null));
                    }
                }
            }
        }
    }
}
