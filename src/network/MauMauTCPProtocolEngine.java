package network;

import maumau.board.ProvokedEmptyDeckException;
import maumau.board.deck.MauMauDeck;
import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import maumau.player.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MauMauTCPProtocolEngine implements TCPProtocolEngine, Runnable {

    private final RemoteChangeablePlayer player;
    private final String playerName;
    private final List<GameSessionEstablishedListener> gameSessionEstablishedListenerList;

    private InputStream is;
    private OutputStream os;

    private Thread protocolThread;

    private String partnerName;

    public MauMauTCPProtocolEngine(RemoteChangeablePlayer player, String playerName) {
        this.player = player;
        this.playerName = playerName;
        this.gameSessionEstablishedListenerList = new ArrayList<>();
    }

    @Override
    public void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener listener) {
        this.gameSessionEstablishedListenerList.add(listener);
    }

    private void notifyListeners(String partnerName) {
        for(GameSessionEstablishedListener listener : this.gameSessionEstablishedListenerList) {
            new Thread(() -> listener.gameSessionEstablished(partnerName)).start();
        }
    }

    @Override
    public void handleConnection(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;

        this.protocolThread = new Thread(this);
        this.protocolThread.start();
    }

    @Override
    public void run() {
        this.log("protocol engine started");

        DataInputStream dis = new DataInputStream(this.is);
        DataOutputStream dos = new DataOutputStream(this.os);

        //exchange names
        try {
            dos.writeUTF(this.playerName);
            this.partnerName = dis.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //the first player sends his board to the other player
            if(this.player.hasFirstTurn()) {
                System.out.println("I'm here");
                //send the deck
                final int numberOfCardsToSend = this.player.getDeck().getDeckAsList().size();
                dos.writeInt(numberOfCardsToSend);
                for(int i = 0; i < numberOfCardsToSend; i++) {
                    dos.writeInt(this.getIntForColor(this.player.getDeck().getDeckAsList().get(i).getColor()));
                    System.out.print("Sending: " + this.player.getDeck().getDeckAsList().get(i).getColor().toString());
                    dos.writeInt(this.getIntForType(this.player.getDeck().getDeckAsList().get(i).getType()));
                    System.out.print(" | " + this.player.getDeck().getDeckAsList().get(i).getType().toString() + "\n");
                }

                //send the discard pile card
                dos.writeInt(this.getIntForColor(this.player.getDiscardPileCard().getColor()));
                dos.writeInt(this.getIntForType(this.player.getDiscardPileCard().getType()));
                System.out.println("Sending DPC: " + this.player.getDiscardPileCard().getColor() + " | " + this.player.getDiscardPileCard().getType());

            } else {
                System.out.println("And I'm here");
                //receive the deck
                final int numberOfCardsToRead = dis.readInt();
                final List<Card> remoteDeck = new ArrayList<>();

                CardColor currentCardColor;
                CardType currentCardType;

                for(int i = 0; i < numberOfCardsToRead; i++) {
                    currentCardColor = this.getColorFromInt(dis.readInt());
                    System.out.print("Received: " + currentCardColor.toString());
                    currentCardType = this.getTypeFromInt(dis.readInt());
                    System.out.print(" | " + currentCardType.toString() + "\n");
                    remoteDeck.add(new MauMauCard(currentCardColor, currentCardType));
                }

                //receive the discard pile card
                CardColor DPCcolor = this.getColorFromInt(dis.readInt());
                CardType DPCType = this.getTypeFromInt(dis.readInt());
                System.out.println("Received DPC: " + DPCcolor + " | " + DPCType);
                Card discardPileCard = new MauMauCard(DPCcolor, DPCType);

                this.player.synchronizeDeck(new MauMauDeck(remoteDeck));
                this.player.synchronizeFirstDiscardPileCard(discardPileCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.notifyListeners(this.partnerName);

        try {
            boolean again = true;
            while(again) {
                again = this.read();
            }
        } catch (IOException e) {
            this.logError("exception was thrown in protocol engine - fatal error");
        }

    }

    private boolean read() throws IOException {
        DataInputStream dis = new DataInputStream(this.is);

        final int method = dis.readInt();
        System.out.println("Method Received: " + method);

        switch (method) {
            case 0 : this.deserializePlayCard(); return true;
            case 1 : this.deserializeDraw(); return true;
            default: return false;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                player methods                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void drawCard() throws ProvokedEmptyDeckException, NotPlayersTurnException {
        this.serializeDraw();
    }

    @Override
    public boolean playCard(Card card, CardColor wishedColor) {
        this.serializePlayCard(card, wishedColor);
        return false;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                serialization                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final static int METHOD_PLAY = 0;
    private final static int METHOD_DRAW = 1;

    private final static int WISHED_COLOR_IS_NULL_INT = 4;

    private void serializePlayCard(Card card, CardColor wishedColor) {
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            dos.writeInt(METHOD_PLAY);
            dos.writeInt(this.getIntForColor(card.getColor()));
            dos.writeInt(this.getIntForType(card.getType()));

            if(wishedColor != null) {
                dos.writeInt(this.getIntForColor(wishedColor));
            } else {
                dos.writeInt(WISHED_COLOR_IS_NULL_INT);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serializeDraw() {
        DataOutputStream dos = new DataOutputStream(this.os);

        try {
            dos.writeInt(METHOD_DRAW);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int getIntForColor(CardColor color) {
        return switch (color) {
            case CLUBS -> 0;
            case SPADES -> 1;
            case HEART -> 2;
            case DIAMONDS -> 3;
        };
    }

    private int getIntForType(CardType type) {
        return switch (type) {
            case TWO -> 2;
            case THREE -> 3;
            case FOUR -> 4;
            case FIVE -> 5;
            case SIX -> 6;
            case SEVEN -> 7;
            case EIGHT -> 8;
            case NINE -> 9;
            case TEN -> 10;
            case JACK -> 11;
            case QUEEN -> 12;
            case KING -> 13;
            case ACE -> 14;
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               deserialization                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void deserializePlayCard() throws IOException {
        DataInputStream dis = new DataInputStream(this.is);

        final int cardColorInt = dis.readInt();
        final int cardTypeInt = dis.readInt();
        final int wishedColorInt = dis.readInt();

        final CardColor color = this.getColorFromInt(cardColorInt);
        final CardType type = this.getTypeFromInt(cardTypeInt);
        final CardColor wishedColor = wishedColorInt == WISHED_COLOR_IS_NULL_INT ? null : this.getColorFromInt(wishedColorInt);

        final Card card = new MauMauCard(color,type);

        this.player.updateDiscardPile(card);

        switch (type) {
            case SEVEN -> this.player.incrementSevenStackCount();
            case EIGHT -> this.player.incrementCurrentPlayerIndex();
            case JACK -> this.player.updateLastWishedColor(wishedColor);
            default -> {
                this.player.resetSevenStackCount();
            }
        }

        this.player.incrementCurrentPlayerIndex();

        this.player.notifyBoardChanged();
    }

    private void deserializeDraw() {
        this.player.updateDeck();
        this.player.incrementCurrentPlayerIndex();
    }

    private CardColor getColorFromInt(int color) {
        return switch (color) {
            case 0 -> CardColor.CLUBS;
            case 1 -> CardColor.SPADES;
            case 2 -> CardColor.HEART;
            case 3 -> CardColor.DIAMONDS;
            default -> throw new IllegalStateException("Unexpected value: " + color);
        };
    }

    private CardType getTypeFromInt(int type) {
        return switch (type) {
            case 2 -> CardType.TWO;
            case 3 -> CardType.THREE;
            case 4 -> CardType.FOUR;
            case 5 -> CardType.FIVE;
            case 6 -> CardType.SIX;
            case 7 -> CardType.SEVEN;
            case 8 -> CardType.EIGHT;
            case 9 -> CardType.NINE;
            case 10 -> CardType.TEN;
            case 11 -> CardType.JACK;
            case 12 -> CardType.QUEEN;
            case 13 -> CardType.KING;
            case 14 -> CardType.ACE;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
    

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                    logging                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void log(String message) {
        System.out.println(this.produceLogString(message));
    }

    private void logError(String message) {
        System.err.println(this.produceLogString(message));
    }

    private String produceLogString(String message) {
        StringBuilder sb = new StringBuilder();
        if(this.playerName != null) {
            sb.append(this.playerName);
            sb.append(": ");
        }

        sb.append(message);

        return sb.toString();
    }


}
