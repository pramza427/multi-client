/**
 * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 2
 * 
 */

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Card {
	private int number;
	private String suit;
	private int scoreVal;
	private Image pic;
	private ImageView iv;
	
	public Card(int num, String s) {
		number = num;
		suit = s;
		String st = "PNG/" + suit + String.valueOf(number) + ".png";
		Image p = new Image(st);
		iv = new ImageView(p);
		iv.setFitHeight(200);
		iv.setFitWidth(100);
		iv.setPreserveRatio(true);
		scoreVal = num;
		if(num == 14) { scoreVal = 11; }
		else if(num == 10) { scoreVal = 10; }
		else if(num == 11) { scoreVal = 10; }
		else if(num == 12) { scoreVal = 10; }
		else if(num == 13) { scoreVal = 10; }
	}
	
	public Card(int num, String s, Image p) {
		number = num;
		suit = s;
		iv = new ImageView(p);
		iv.setFitHeight(200);
		iv.setFitWidth(100);
		iv.setPreserveRatio(true);
		scoreVal = num;
		if(num == 14) { scoreVal = 11; }
		else if(num == 10) { scoreVal = 10; }
		else if(num == 11) { scoreVal = 10; }
		else if(num == 12) { scoreVal = 10; }
		else if(num == 13) { scoreVal = 10; }
	}
	
	public void setNumber(int num) {
		number = num;
	}
	public int getNumber() {
		return number;
	}
	
	public void setSuit(String s) {
		suit = s;
	}
	public String getSuit() {
		return suit;
	}
	
	public int getScore() {
		return scoreVal;
	}
	
	public void setPic(Image temp) {
		pic = temp;
		iv.setImage(pic);
		iv.setFitHeight(200);
		iv.setFitWidth(100);
		iv.setPreserveRatio(true);
	}
	public ImageView getPic() {
		return iv;
	}
}
