
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
    
    public void shuffleDeck() {
    	Collections.shuffle(this.deck);
    }
    
    public ArrayList<Card> dealFirstTwoCards(){

        Card tempCard;
        ArrayList<Card> tempArray = new ArrayList<Card>(2);
        ArrayList<Card> deckTemp = this.deck;


        for(int i = 0; i < 2; i++){
            tempCard = deckTemp.get(0);
            tempArray.add(tempCard);	//Place the topmost card on a new array.
            deckTemp.remove(0);			//Remove the card that was taken from the top of the deck
        }

        updateDealerDeck(deckTemp);		//Update the deck after cards are removed 
        return tempArray;				//Return the 2 cards and deal it to the players
    }
    
    public Card dealOneCard() {
    	
    	Card tempCard;
    	ArrayList<Card> deckTemp = this.deck;
    	
    	tempCard = deckTemp.get(0);
    	deckTemp.remove(0);
    	
    	updateDealerDeck(deckTemp);
    	
    	return tempCard;
    }
    
    public void updateDealerDeck(ArrayList<Card> c){
        this.deck = c;
    }
}