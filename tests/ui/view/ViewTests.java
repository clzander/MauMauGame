package ui.view;

import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import maumau.player.MauMauPlayer;
import maumau.player.VisulizablePlayer;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ViewTests {

    /**
     * This test doesn't test anything. But you can have a look at what the cards look like.
     * So you could say: "I'm testing how they look", but the test doesn't test anything anyway.
     */
    @Test
    public void testCardView() {
        VisulizablePlayer player = new MauMauPlayer("Alice", 0);
        VisualizerBackdoor visualizer = new GameVisualizer(System.out, player);

        List<char[][]> cards = new ArrayList<>();
        cards.add(visualizer.getCard(new MauMauCard(CardColor.CLUBS, CardType.TWO)));
        cards.add(visualizer.getCard(new MauMauCard(CardColor.SPADES, CardType.KING)));
        cards.add(visualizer.getCard(new MauMauCard(CardColor.HEART, CardType.TEN)));
        cards.add(visualizer.getCard(new MauMauCard(CardColor.DIAMONDS, CardType.ACE)));

        for(char[][] card : cards) {
            for (int i = 0; i < visualizer.getCardHeight(); i++) {
                for (int j = 0; j < visualizer.getCardWidth(); j++) {
                    System.out.print(card[j][i]);
                }
                System.out.print("\n");
            }
            System.out.print("\n\n\n");
        }
    }


    @Test
    public void testViewPrinting() {
        VisulizablePlayer player = new MauMauPlayer("Alice", 0);
        GameVisualizer visualizer = new GameVisualizer(System.out, player);

        visualizer.printView();
    }

}
