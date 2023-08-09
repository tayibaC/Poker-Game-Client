import java.io.Serializable;
import java.util.List;

public class pokerInfo implements Serializable {
    // Serializes all of these variables
    private static final long serialVersionUID = 1L;
    List<Deck.Card> dealerHand;
    List<Deck.Card> playerHand;
    int ante;
    int pairPlus;
    int totalWinnings;
    int gameWinnings;
    String message;
    boolean winsGame;
    boolean playerFolded;
    boolean requestCards;
}