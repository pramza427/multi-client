import java.util.ArrayList;

public class Player {
    private String name;
    private ArrayList<Card> hand;
    private double balance;
    private double currentBet;
    private boolean ready;

    Player(String n){
        name = n;
        hand = null;
        currentBet = 0;
        ready = false;
    }

    public double getCurrentBet() {
        return currentBet;
    }
    public ArrayList<Card> getHand() {
        return hand;
    }
    public int getHandValue(){
        int total = 0;
        for(Card c : hand){
            total += c.getNumber();
        }
        return total;
    }
    public String getName() {
        return name;
    }
    public boolean isReady() {
        return ready;
    }

    public void setReady(){
        ready = !ready;
    }

    // Reset the players' bet
    public void clearBet(){
        currentBet = 0;
    }

    // Raising bets
    // Returns false if new raise is larger than balance allows
    // Otherwise returns true
    // **** AT END OF BETTING ROUND MUST SUBTRACT CURRENT BET FROM BALANCE ****
    public boolean raise(double amount, double pot){
        double newPot = pot + amount;
        if(newPot > balance){
            return false;
        } else {
            currentBet += pot + amount;
            return true;
        }
    }

    // Matches with the highest bet.
    // Returns false if balance does allow player to match
    public boolean call(double pot){
        if(pot > balance){
            return false;
        } else {
            currentBet = pot;
            return true;
        }
    }

    // Bets all the player has in their balance
    // Returns the bet the player is placing
    public double allIn(){
        currentBet = balance;
        return balance;
    }

    // Hit - player obtains another card
    // Returns the value of the player's hand
    public int hit(Card c){
        hand.add(c);
        return getHandValue();
    }

    // Empties the hand of the player
    public void clearHand(){
        hand.clear();
    }
}
