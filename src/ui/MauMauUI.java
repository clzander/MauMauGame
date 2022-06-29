package ui;


import maumau.board.Board;
import maumau.board.MauMauBoard;
import maumau.board.ProvokedEmptyDeckException;
import maumau.cards.Card;
import maumau.cards.CardColor;
import maumau.cards.CardType;
import maumau.cards.MauMauCard;
import maumau.player.*;
import network.*;
import ui.view.GameVisualizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MauMauUI implements TCPStreamStatusListener, GameSessionEstablishedListener, BoardChangedListener {
    private final String EXIT = "exit";
    private final String OPEN = "open";
    private final String CONNECT = "connect";
    private final String PLAY = "play";
    private final String DRAW_CARD = "draw";
    private final String RULES = "rules";
    private final String COMMANDS = "commands";
    private final String MAN = "man";

    private final int DEFAULT_MAUMAU_PORT = 7777;

    private final PrintStream outStream;
    private final BufferedReader inBufferedReader;

    private TCPStream tcpStream;
    private TCPProtocolEngine protocolEngine;
    private String partnerName;
    private boolean localPlayerHasFirstTurn;
    private boolean gameSessionIsEstablished = false;

    private GameVisualizer visualizer;

    private final String playerName;
    private RemoteChangeablePlayer localPlayer;


    public static void main(String[] args) {
        printTitleMessage();
        String playerName = readPlayerName();
        welcomePlayer(playerName);

        MauMauUI userCmd = new MauMauUI(playerName, System.out, System.in);
        userCmd.printUsage();
        userCmd.runCommandLoop();
    }

    private static void printTitleMessage() {
        //MauMau in ASCII-Art
        System.out.println("  __  __               __  __             ");
        System.out.println(" |  \\/  | __ _ _   _  |  \\/  | __ _ _   _ ");
        System.out.println(" | |\\/| |/ _` | | | | | |\\/| |/ _` | | | |");
        System.out.println(" | |  | | (_| | |_| | | |  | | (_| | |_| |");
        System.out.println(" |_|  |_|\\__,_|\\__,_| |_|  |_|\\__,_|\\__,_|\n");
        //one line break
        System.out.println("Welcome to MauMau version 0.1\n\n");
        //two line breaks
    }

    /**
     * Asks user for a name
     *
     * @return player name inserted by the user on the console
     */
    private static String readPlayerName() {
        Scanner scanner = new Scanner(System.in);
        String name;

        do {
            System.out.print("Please insert your name: ");
            //name = inBufferedReader.readLine();
            name = scanner.nextLine();
        } while (Objects.equals(name, ""));

        return name;
    }

    /**
     * Welcomes player in the console
     *
     * @param playerName the name of the player to welcome
     */
    private static void welcomePlayer(String playerName) {
        System.out.println("\nWelcome " + playerName);
        System.out.println("Let's play!");
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                  constructor                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor of class MauMauUI
     *
     * @param playerName name of the player who's playing
     * @param os         stream to write to
     * @param is         stream to read from
     */
    public MauMauUI(String playerName, PrintStream os, InputStream is) {
        this.playerName = playerName;
        this.outStream = os;
        this.inBufferedReader = new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Prints usage on the console
     */
    private void printUsage() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        sb.append("COMMANDS:\n");
        sb.append(COMMANDS);
        sb.append("\tthis list of valid commands\n");
        sb.append(CONNECT);
        sb.append("\t\tconnect as tcp client\n");
        sb.append(DRAW_CARD);
        sb.append("\t\tdraw a card from the deck\n");
        sb.append(MAN);
        sb.append("\t\tget more detailed description of any command\tUsage: ");
        sb.append(PLAY);
        sb.append("\t\tdiscard a card from your hand\n");
        sb.append(EXIT);
        sb.append("\t\texit the game\n");
        sb.append(OPEN);
        sb.append("\t\topen port become tcp server\n");
        sb.append(RULES);
        sb.append("\t\thow to play\n");

        this.outStream.println(sb);
    }

    /**
     * Starting loop to listen for commands entered by the user
     */
    public void runCommandLoop() {
        boolean again = true;
        while (again) {
            try {
                String cmdLineString = inBufferedReader.readLine();

                if (cmdLineString != null) {
                    List<String> cmd = optimizeUserInputString(cmdLineString);

                    //the first String in the list is the command
                    //it can be removed because this string will never be used once the right method was called
                    //it is saved just for the switch to determine the right method
                    final String commandIdentifier = cmd.remove(0);

                    switch (commandIdentifier) {
                        case COMMANDS : {
                            this.printUsage();
                        }
                        case CONNECT : {
                            this.doConnect(cmd);
                        }
                        case DRAW_CARD : {
                            this.doDrawCard();
                            this.doPrint();//redraw
                            //TODO: Print when necessary
                        }
                        case EXIT : {
                            again = false;
                            this.doExit();
                        }
                        case MAN : {
                            this.doMan();
                        }
                        case PLAY : {
                            this.doPlay(cmd);
                            this.doPrint();//redraw
                        }
                        case OPEN : {
                            this.doOpen(cmd);
                        }
                        case RULES : {
                            this.doRules();
                        }
                        default : {
                            this.outStream.println("unknown command:" + cmd.get(0) + "\n");
                        }
                    }
                }

            } catch (IOException e) {
                this.outStream.println("error - cannot read from input stream");
                this.doExit();
            } catch (NotInAGameException e) {
                this.outStream.println("There is no running game yet.");
            } catch (UnknownCardException e) {
                this.outStream.println("Can't extract card name out of the command. Please verify the spelling.");
            } catch (AlreadyConnectedException e) {
                this.outStream.println("You are already connected to a game.");
            } catch (WrongPlayCommandSyntax e) {
                e.printStackTrace();
            } catch (PlayerHasNoSuchCardException e) {
                e.printStackTrace();
            } catch (NotPlayersTurnException e) {
                e.printStackTrace();
            } catch (ProvokedEmptyDeckException e) {
                e.printStackTrace();
            } catch (PlayerViolatesGameRulesException e) {
                e.printStackTrace();
            }
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                commands                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Connect to an already hosted game
     *
     * @param args command identifier and additional arguments inputted by the user
     */
    private void doConnect(List<String> args) throws AlreadyConnectedException {
        if(!alreadyConnected()) {
            String hostname;

            if(args.size() > 0) {
                hostname = args.get(0);
            } else {
                hostname = "localhost";
            }

            //first establish tcp stream to a server
            this.tcpStream = new TCPStream(this.DEFAULT_MAUMAU_PORT, false, this.playerName);
            this.tcpStream.setRemoteEngine(hostname);
            this.tcpStream.setStreamCreationListener(this);
            this.tcpStream.run();
        } else {
            throw new AlreadyConnectedException();
        }
    }

    /**
     * Draw a card from the deck
     */
    private void doDrawCard() throws NotInAGameException {
        if(this.gameSessionIsEstablished) {
            try {
                this.localPlayer.drawCard();

            } catch (NotPlayersTurnException e) {
                this.outStream.println("Not your turn!");

            }catch (ProvokedEmptyDeckException e) {
                //check if the player has a playable card
                if(e.getCouldHavePlayed()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The deck is empty and can't be refilled with cards from the discard pile.");
                    sb.append("\n");
                    sb.append("You have a card on your hand which you could play.");

                    this.outStream.println(sb);
                }
                //otherwise, his turn has to end
                else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The deck is empty and can't be refilled with cards from the discard pile.");
                    sb.append("\n");
                    sb.append("Sadly, you don't have a card on your hand which you could play.");
                    sb.append("You must wait until your opponent has played a card.");
                    sb.append("Your turn will end now.");
                    this.outStream.println(sb);
                }

            }
        } else {
            throw new NotInAGameException();
        }
    }

    /**
     * Exit game
     */
    private void doExit() {
        this.outStream.println("Bye");
    }

    private void doMan() {

    }

    /**
     * Discard a card from the hand
     * e.g.:    discard 2 clubs,
     *          discard jack spades diamonds
     *
     * @param args command identifier and additional arguments inputted by the user
     */
    private void doPlay(List<String> args)
            throws  UnknownCardException, NumberFormatException, NotInAGameException,
            WrongPlayCommandSyntax, PlayerHasNoSuchCardException, NotPlayersTurnException,
            ProvokedEmptyDeckException, PlayerViolatesGameRulesException {

        //playing a card is only possible if the player is currently in a game
        if(this.gameSessionIsEstablished) {
            CardColor cardColor;
            CardType cardType;
            CardColor wishedColor;

            if(args.size() > 0) {
                //at least one attribute -> 1st interpreted as the card type
                cardType = this.parseStringToType(args.get(0));
            } else {
                throw new WrongPlayCommandSyntax();
            }
            if(args.size() > 1) {
                //at least two attributes -> 2nd interpreted as the card color
                cardColor = this.parseStringToColor(args.get(1));
            } else {
                throw new WrongPlayCommandSyntax();
            }
            if(args.size() > 2) {
                //at least three attributes -> 3rd interpreted as the card color which was wished
                if(cardType == CardType.JACK) {
                    wishedColor = this.parseStringToColor(args.get(2));
                } else {
                    wishedColor = null;
                }
            } else if (cardType == CardType.JACK) {
                throw new WrongPlayCommandSyntax();
            } else {
                wishedColor = null;
            }

            //card determined by user input
            Card card = new MauMauCard(cardColor, cardType);

            //play the card
            this.localPlayer.playCard(card, wishedColor);

        } else {
            throw new NotInAGameException();
        }
    }


    private void doPrint() {
        this.visualizer.printView();
    }


    /**
     * Open port to host a game
     * @param args command identifier and additional arguments inputted by the user
     */
    private void doOpen(List<String> args) throws NumberFormatException, AlreadyConnectedException {
        if(!alreadyConnected()) {
            //first establish the tcp stream so clients can connect
            this.tcpStream = new TCPStream(this.DEFAULT_MAUMAU_PORT, true, this.playerName);
            this.tcpStream.setStreamCreationListener(this);
            this.tcpStream.run();

        } else {
            throw new AlreadyConnectedException();
        }
    }

    /**
     * Explains the rules of the game
     */
    private void doRules() {
        StringBuilder sb = new StringBuilder();
        sb.append("**********\tGAME PREPARATION\t**********");
        sb.append("\n");
        sb.append("The following points are automated:");
        sb.append("\n\t");
        sb.append("1.) A player is randomly selected to start.");
        sb.append("\n\t");
        sb.append("2.) Every player gets seven cards.");
        sb.append("\n\t");
        sb.append("3.) A first card is revealed from the deck building the discard pile.");
        sb.append("\n\n");
        sb.append("**********\tHOW TO PLAY\t**********");
        sb.append("\n\t");
        sb.append("* The first player begins playing a card from his hand. If non of his cards can be played he needs to draw a card from the deck.");
        sb.append("\n\t\t");
        sb.append("e.g: 'discard 4 clubs'");
        sb.append("\n\t\t");
        sb.append("e.g: 'draw'");
        sb.append("\n\n\t");
        sb.append("* A card can only be played if its color and/or type is equal to the color/type of card on the discard pile.");
        sb.append("\n\t");
        sb.append("* Some cards are action cards:");
        sb.append("\n\t\t");
        sb.append("7\t\t-\tThe other player has to draw two cards. However, if he has a 7 as well you would need to draw four cards and so on.");
        sb.append("\n\t\t");
        sb.append("8\t\t-\tThe next player is being skipped (You have another turn).");
        sb.append("\n\t\t");
        sb.append("Jack\t-\tThe current player can wish any card color.");
        sb.append("\n\t\t\t\t\t");
        sb.append("If a Jack is the first card on the discard pile in a game, the player who starts can play any card.");
        sb.append("\n\t\t\t\t\t\t");
        sb.append("e.g: 'discard jack spades clubs'");

        sb.append("\n\n");
        sb.append("**********\tEND OF THE GAME\t**********");
        sb.append("\n\t");
        sb.append("* Once a player has successfully discarded his last card he wins.");
        sb.append("\n\t");
        sb.append("* If the players agreed to only play one round the game ends and another game can be created.");
        sb.append("\n\t\t");
        sb.append("e.g: 'open' or 'connect'");
        sb.append("\n\n\t");
        sb.append("* Otherwise, the player who loosed gets minus points for each cards he still has on his hand:");
        sb.append("\n\t\t");
        sb.append("AS\t\t-\t-11 points");
        sb.append("\n\t\t");
        sb.append("KING\t-\t-10 points");
        sb.append("\n\t\t");
        sb.append("QUEEN\t-\t-10 points");
        sb.append("\n\t\t");
        sb.append("JACK\t-\t-20 points");
        sb.append("\n\t\t");
        sb.append("2-10\t-\tminus points according to the number on the card");
        sb.append("\n\t");
        sb.append("* The game ends after all rounds have been finished. The winner is the player with the most points. That means, that the player with the most minus points obviously looses.");
        this.outStream.println(sb);

    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                               network                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void streamCreated(TCPStream tcpStream) {
        this.outStream.println("TCP connection established - the game will start quite soon.");

        //set up players
        if(tcpStream.isServer()) {
            this.localPlayerHasFirstTurn = true;
            this.localPlayer = new MauMauPlayer(this.playerName, 0);
        } else {
            this.localPlayerHasFirstTurn = false;
            this.localPlayer = new MauMauPlayer(this.playerName, 1);
        }
        this.visualizer = new GameVisualizer(this.outStream, this.localPlayer);
        this.visualizer.printView();
        this.localPlayer.subscribeChangeListener(this);

        this.protocolEngine = new MauMauTCPProtocolEngine(this.localPlayer, this.playerName);
        this.localPlayer.setProtocolEngine(this.protocolEngine);
        this.protocolEngine.subscribeGameSessionEstablishedListener(this);

        try {
            this.protocolEngine.handleConnection(tcpStream.getInputStream(), tcpStream.getOutputStream());
        } catch (Exception e) {
        }

    }

    @Override
    public void streamCreationFailed() {
        this.tcpStream = null;
        this.localPlayer = null;
        this.outStream.println("TCP connection failed - please try again.");
    }

    @Override
    public void gameSessionEstablished(String partnerName) {
        this.partnerName = partnerName;
        if(this.localPlayerHasFirstTurn) {
            this.outStream.println("Your turn");
        } else {
            this.outStream.println("Wait for your partner to play / draw a card.");
        }
        this.gameSessionIsEstablished = true;
        this.doPrint();
    }

    @Override
    public void boardChanged() {
        this.doPrint();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                      helper                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<String> optimizeUserInputString(String input) {
        List<String> cmd = new ArrayList<>();
        String SPACE = " ";

        String[] unfinishedCmd = input.split(SPACE);

        for (String attribute : unfinishedCmd) {
            attribute = attribute.toLowerCase();
            attribute = attribute.trim();

            if (!attribute.equals(" ")) {
                cmd.add(attribute);
            }
        }

        return cmd;
    }


    private CardType parseStringToType(String typeString) throws UnknownCardException {
        final String TWO = "2";
        final String THREE = "3";
        final String FOUR = "4";
        final String FIVE = "5";
        final String SIX = "6";
        final String SEVEN = "7";
        final String EIGHT = "8";
        final String NINE = "9";
        final String TEN = "10";
        final String JACK = "jack";
        final String QUEEN = "queen";
        final String KING = "king";
        final String ACE = "ace";

        CardType cardType;

        switch (typeString) {
            case TWO : cardType = CardType.TWO; break;
            case THREE : cardType = CardType.THREE; break;
            case FOUR : cardType = CardType.FOUR; break;
            case FIVE : cardType = CardType.FIVE; break;
            case SIX : cardType = CardType.SIX; break;
            case SEVEN : cardType = CardType.SEVEN; break;
            case EIGHT : cardType = CardType.EIGHT; break;
            case NINE : cardType = CardType.NINE; break;
            case TEN : cardType = CardType.TEN; break;
            case JACK : cardType = CardType.JACK; break;
            case QUEEN : cardType = CardType.QUEEN; break;
            case KING : cardType = CardType.KING; break;
            case ACE : cardType = CardType.ACE; break;
            default : throw new UnknownCardException("Unknown card type: " + typeString);
        }

        return cardType;
    }


    private CardColor parseStringToColor(String colorString) throws UnknownCardException {
        final String CLUBS = "clubs";
        final String SPADES = "spades";
        final String HEART = "heart";
        final String DIAMONDS = "diamonds";

        CardColor cardColor;

        switch (colorString) {
            case CLUBS : cardColor = CardColor.CLUBS; break;
            case SPADES : cardColor = CardColor.SPADES; break;
            case HEART : cardColor = CardColor.HEART; break;
            case DIAMONDS : cardColor = CardColor.DIAMONDS; break;
            default : throw new UnknownCardException("Unknown card color: " + colorString);
        }

        return cardColor;
    }


    private boolean alreadyConnected() {
        return this.tcpStream != null;
    }
}
