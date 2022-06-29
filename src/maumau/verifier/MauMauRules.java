package maumau.verifier;

import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;

public class MauMauRules implements Rules {


    @Override
    public boolean canPlayCard(Card cardToBePlayed, Card lastPlayedCard, CardColor wishedColor) {
        boolean result = false;

        if(cardToBePlayed.getType() == CardType.JACK) {
            //it's always valid to play a jack
            result = true;

        } else if(wishedColor != null && lastPlayedCard.getType() == CardType.JACK) {
            //if a card was wished before, that color needs to be played
            //the rule: "same type is valid" isn't in action here
            if(wishedColor == cardToBePlayed.getColor()) {
                result = true;
            }

        } else if(cardToBePlayed.getColor() == lastPlayedCard.getColor()) {
            //a card can be played if the color is the same as the one from the discard pile
            result = true;

        } else if(cardToBePlayed.getType() == lastPlayedCard.getType()) {
            //it's also valid to play a card with the same type as the type of the card on the discard pile
                result = true;
        }

        return result;
    }
}
