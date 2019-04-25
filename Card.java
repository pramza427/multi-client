
public class Card {

    //Values of the cards are represented with the following:
    //Ace to 10
    //Jack = 10
    //Queen = 10
    //King = 10
    //Ace = 10

    //Suits of the cards are represented with the following:
    //0 = Clubs
    //1 = Diamonds
    //2 = Hearts
    //3 = Spades

    private final int suit;
    private final int number;
    private int aceSecondaryInt;
    private String suitString;
    private String numberString;
    //private String numberInt;
    private int numberInt;
    private final String[] cardSuit = {"Clubs", "Diamonds", "Hearts", "Spades"};
    private final String[] cardNumber = {"Ace", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Jack", "Queen", "King"};
    //private final String[] cardNumberInt = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "10", "10", "10", "10"};
    private final int[] cardNumberInt = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};


    public Card(int suit, int number) {
        this.suit = suit;
        this.number = number;
        this.suitString = cardSuit[suit];
        this.numberString = cardNumber[number];
        this.numberInt = cardNumberInt[number];
        
        if(this.numberString == "Ace") {
        	this.aceSecondaryInt = 11;
        }
    }

    //For the purpose of checking and debugging, we're going to add the option-
    //to represent the cards as Strings

    public void printCard(){

        int s = getSuit();
        int n = getNumber();

        System.out.println(cardNumber[n] + " of " + cardSuit[s]);
    }

    public int getSuit() {
        return this.suit;
    }

    public int getNumber() {
        return this.number;
    }

    public String getSuitString(){
        return this.suitString;
    }

    public String getNumberString(){
        return this.numberString;
    }

    public String getCardtoFileName(){
        String r = this.numberString + "of" + this.suitString + ".jpg";
        return r;
    }

    /*
    public String getCardNumberInt(){
        return this.numberInt;
    }
    */
    
    public int getCardNumberInt() {
    	return this.numberInt;
    }
}
