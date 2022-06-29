package maumau.player;

//Thrown when a player tried to play or draw a card, but it wasn't his turn
public class NotPlayersTurnException extends Exception {
    public NotPlayersTurnException() {
        super();
    }
}
