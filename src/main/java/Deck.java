import java.io.Serializable;
import java.util.*;

import javafx.scene.image.Image;
import java.io.Serializable;

public class Deck implements Serializable{
    private static final long serialVersionUID = 1L;
    private List<Card> cards;
    private String[] suits = {"hearts", "diamonds", "spades", "clubs"};
    private String[] numbers = {"ace", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king"};

    //Constructor for building a deck of cards
    public Deck() {
        cards = new ArrayList<Card>();
        for (String suit : suits) {
            for (String number : numbers) {
                Card newCard = new Card(suit, number);
                cards.add(newCard);
            }
        }
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public String getImagePath(String suit, String number) {
        return "/PNG-cards-1.3/" + number + "_of_" + suit + ".png";
    }

    public List<Card> drawCards(int count) {
        List<Card> hand = new ArrayList<Card>();
        List<Card> cardsCopy = new ArrayList<Card>(cards);
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int randomIndex = random.nextInt(cardsCopy.size()); //Ensures uniqueness
            hand.add(cardsCopy.get(randomIndex));
            cardsCopy.remove(randomIndex);
        }
        return hand;
    }

    //Nested Class since Card is a part of the deck
    public class Card implements Serializable {
        private String suit;
        private String number;
      //  private Image cardImage;
        private String cardPath;

        //Probably add a cardimagelink property here


        public Card(String suit, String number) {
            this.suit = suit;
            this.number = number;
            //String imagePath = getImagePath(suit, number);

            this.cardPath = getImagePath(suit, number);
           // this.cardImage = new Image(getClass().getResourceAsStream(imagePath));
        }

        public String getSuit() {
            return suit;
        }
        public String getNumber() {
            return number;
        }
        public String getImage() {
            return cardPath;
        }

    //    public Image getImage() {
    //        return cardImage;
    //    }
    }
}