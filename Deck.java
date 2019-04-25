/**
 * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 2
 * 
 */

import java.util.ArrayList;

import javafx.scene.image.Image;

public class Deck {
	ArrayList<Card> cards = new ArrayList<Card>();
	
	public Deck() {
		String[] suits = {"C", "D", "H", "S"};
		for(int i = 0; i < 4; i++) {
			for(int j = 2; j <= 14; j++) {
				Image pic = new Image( suits[i] + String.valueOf(j) + ".png");
				Card temp = new Card(j, suits[i], pic);
				cards.add(temp);
			}
		}
	}
	
	//grabs a random card and places it in a random spot 100 times
	public void shuffle() {
		for(int i = 0; i < 100; i++) {
			int rand = (int)(Math.random() * 52);
			int rand2 = (int)(Math.random() * 52);
			
			Card temp = cards.remove(rand);
			cards.add(rand2, temp);
		}
	}
	
	public Card drawCard() {
		return cards.remove(0);
	}
	
	public void reset() {
		cards.clear();
		String[] suits = {"C", "D", "H", "S"};
		for(int i = 0; i < 4; i++) {
			for(int j = 2; j <= 14; j++) {
				Image pic = new Image( suits[i] + String.valueOf(j) + ".png");
				Card temp = new Card(j, suits[i], pic);
				cards.add(temp);
			}
		}
	}
}
