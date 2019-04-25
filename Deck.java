
import java.util.ArrayList;
import java.util.Collections;

public class Deck {

    public final int numberOfCards = 52;    //Standard 52 card deck.

    public ArrayList<Card> deck = new ArrayList<Card>(numberOfCards);

    public Deck(){

        int i = 0;

        for(int suit = 0; suit <= 3; suit++){
            for(int number = 0; number <= 12; number++){
                deck.add(new Card(suit, number));
                i++;
            }
        }
    }
}