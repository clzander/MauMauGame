package maumau.cards;

public class MauMauCard implements Card{
    private final CardColor color;
    private final CardType type;

    public MauMauCard(CardColor color, CardType type) {
        this.color = color;
        this.type = type;
    }

    @Override
    public CardColor getColor() {
        return this.color;
    }

    @Override
    public CardType getType() {
        return this.type;
    }
}
