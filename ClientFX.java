/**
  * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 5
 */
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ClientFX extends Application{
	
	Label welcome, money, sign, pot;
	Button setIPandPort, quit, hit, pass, bet, enterName, play;
	TextField betAmount, potAmount, moneyAmount;
	Stage myStage;
	Scene nameScene, playScene;
	private TextArea nameMessages = new TextArea();
	private TextArea playMessages = new TextArea();
	boolean stop;
	String playerName = "";
	private final ObservableList<String> opponentList = FXCollections.observableArrayList();
	ListView<String> opponentListView = new ListView<String>();
	private Integer score = 0;
	Label scoreCounter = new Label(score.toString());
	
	Deck deck = new Deck();
	
	ArrayList<Card> playerCards = new ArrayList<Card>();
	ArrayList<String> opponents = new ArrayList<String>();
	
	private Client conn;
	
	public String getName() {
		return playerName;
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void stop() throws Exception{
		conn.send(playerName + " Quit");
		stop = true;
	}
	
	public void start(Stage primaryStage) throws Exception {
		
		score = 0;
		stop = false;
		
		primaryStage.setTitle("BlackJack!");
		
		deck.shuffle();
		hit = new Button("Hit");
		hit.setPrefSize(50, 30);
		pass = new Button("Pass");
		pass.setPrefSize(50, 30);
		bet = new Button("Bet");
		bet.setPrefSize(50, 30);
		sign = new Label("$");
		sign.setFont(Font.font(20));
		betAmount = new TextField("10");
		betAmount.setPrefWidth(50);
		
		money = new Label("You have:");
		moneyAmount = new TextField("1000");
		moneyAmount.setPrefWidth(60);
		moneyAmount.setEditable(false);
		
		pot = new Label("Money in the pot:");
		potAmount = new TextField("0");
		potAmount.setMaxWidth(60);
		potAmount.setEditable(false);
		
		playMessages.setPrefWidth(50);
		playMessages.setMaxWidth(50);
		playMessages.setMaxSize(300, 200);
		playMessages.setEditable(false);
		
		quit = new Button("Quit");
		setIPandPort = new Button("Set IP and Port");
		
		myStage = primaryStage;
		
		BorderPane playPane = playScreen();
		BorderPane namePane = nameScreen(primaryStage);
		
		nameScene = new Scene(namePane, 600, 400);
		playScene = new Scene(playPane, 1000, 1000);
		
		primaryStage.setScene(nameScene);
		primaryStage.show();	
		
	}
	
	//Create the Screen where the player chooses a name
	private BorderPane nameScreen(Stage pStage) {
		BorderPane tempPane = new BorderPane();
		tempPane.setPadding(new Insets(30));
		//Second prompt the player to choose a name to display
		TextField name = new TextField();
		Label askName = new Label("Please enter the name you want displayed.");
		enterName = new Button("Set Name");
		play = new Button("Ready to Play!");
		//First prompt Player to connect to a server		
		Label ipPortPrompt = new Label("Please input your IP and Port:");
		TextField clientIPIn = new TextField("127.0.0.1");
		clientIPIn.setPrefWidth(70);
		TextField clientPortIn = new TextField("5555");
		clientPortIn.setPrefWidth(50);
		Button setIPandPort = new Button("Connect");
		
		//What happens when the player clicks the Connect button to connect to the server
		EventHandler<ActionEvent> clickSetIPPort = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				if(clientIPIn.getLength() != 9 ) {
					nameMessages.appendText("Please enter a Valid IP address (ex: 127.0.0.1)\n");
				}
				else if(Integer.parseInt(clientPortIn.getText()) < 1 || Integer.parseInt(clientPortIn.getText()) > 66666) {
					nameMessages.appendText("Port number must be between 1 and 66666\n");
				}
				else {
					conn = createClient(clientIPIn.getText(), Integer.parseInt(clientPortIn.getText()));
					try {
						stop = false;
						conn.startConn();
						nameMessages.appendText("Connected to Port " + clientPortIn.getText() + "\n");
						enterName.setDisable(false);
					} catch (Exception e) {
						nameMessages.appendText("Could not set up a connection to Port " + clientPortIn.getText() + "\n");
					}
				}
			}
		};
		setIPandPort.setOnAction(clickSetIPPort);
		HBox ipAndPort = new HBox(10, clientIPIn, clientPortIn, setIPandPort);
		VBox ipPortAll = new VBox(10, ipPortPrompt, ipAndPort);
		
		
		//What happens when you enter a name
		EventHandler<ActionEvent> clickEnterName = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				System.out.println("Checking if name is available");
				//Checks if its 3 or more characters
				if(name.getText().length() < 3) {
					nameMessages.appendText("Names must be at least 3 letters");
				}
				else {
					try {
						//sets player name and sends it to the server to add to player list 
						//and check if the name is already taken
						playerName = name.getText();
						conn.send("New: " + playerName);
						play.setDisable(false);
					} catch (Exception e) {
						nameMessages.appendText("Could not send name\n");
					}
				}
			}
		};
		
		//What happens when you enter a name
		EventHandler<ActionEvent> clickPlay = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				try {
					playMessages.appendText("You Are: " + playerName + "!!!\n");
					pStage.setScene(playScene);
					conn.send("ReadyToPlay: " + playerName);
				
				} catch (Exception e) {
					nameMessages.appendText("Could not send name\n");
				}
			}
		};
		enterName.setOnAction(clickEnterName);
		play.setOnAction(clickPlay);
		play.setDisable(true);
		
		HBox nameLine = new HBox(10, name, enterName);
		VBox nameAll = new VBox(10 , askName, nameLine);
		Label opLabel = new Label("Connected Players:");
		VBox opponentBox = new VBox(10, opLabel, opponentListView);
		
		//set the middle to prompts and buttons
		VBox all = new VBox(20, ipPortAll, nameAll, play, nameMessages);
		tempPane.setCenter(all);
		
		//set right to Player list
		tempPane.setRight(opponentBox);
		opponentListView.setMaxWidth(200);
		

		enterName.setDisable(true);
		
		return tempPane;
	}
	
	private BorderPane playScreen(){
		
		//Initialize the Border pane that will be returned
		BorderPane pane1 = new BorderPane();
		pane1.setPadding(new Insets(5, 5, 5, 5));
		
		
		//set a background color of green with a black background
		Rectangle blackBG = new Rectangle(1000, 1000, Color.BLACK);
		Rectangle greenBG = new Rectangle(980, 980, Color.GREEN);
		greenBG.setArcHeight(50);
		greenBG.setArcWidth(50);
		
		StackPane allBG = new StackPane(blackBG, greenBG);
		
		VBox background = new VBox(allBG);
		pane1.getChildren().add(background);
		//Finish setting up the background
		
		//Items for the center of the pane
		Label messageLabel = new Label("Messages");
		VBox potBox = new VBox(10, pot, potAmount, messageLabel, playMessages);
		potBox.setAlignment(Pos.CENTER);
		pane1.setCenter(potBox);
		
		//Finish Items for the center of the pane
		
		//Set player UI
		playerCards.add(deck.drawCard());
		playerCards.add(deck.drawCard());
		
		HBox cardImages = new HBox(8);
		for(Card c: playerCards) {
			cardImages.getChildren().add(c.getPic());
		}
		cardImages.setAlignment(Pos.CENTER);
		
		
		HBox moneyBox = new HBox(8, money, moneyAmount);
		money.setFont(Font.font(30));
		moneyBox.setAlignment(Pos.CENTER);
		HBox inputLeft = new HBox(8, hit, pass);
		HBox inputRight = new HBox(8, bet, sign, betAmount);
		HBox walkAway = new HBox(quit);
		walkAway.setAlignment(Pos.CENTER);
		HBox inputs = new HBox(30, inputLeft, inputRight);
		inputs.setAlignment(Pos.CENTER);
		Label you = new Label("You:");
		you.setFont(Font.font(20));
		VBox playerUI = new VBox(10, you, cardImages, moneyBox, inputs, walkAway);
		playerUI.setAlignment(Pos.CENTER);
		//Finish player UI
		
		//Set opponent UI
		
		Image pic = new Image("back.png");
		ImageView iv = new ImageView(pic);
		iv.setFitHeight(200);
		iv.setFitWidth(100);
		iv.setPreserveRatio(true);
		
		ImageView back1 = new ImageView(pic);
		back1.setFitHeight(200);
		back1.setFitWidth(100);
		back1.setPreserveRatio(true);
		
		ImageView back2 = new ImageView(pic);
		back2.setFitHeight(200);
		back2.setFitWidth(100);
		back2.setPreserveRatio(true);
		
		HBox opponentCards1 = new HBox(10, back1);
		opponentCards1.setAlignment(Pos.CENTER);
		
		pane1.setRight(opponentCards1);
		
		HBox opponentCards2 = new HBox(10, back2);
		opponentCards2.setAlignment(Pos.CENTER);
		
		pane1.setLeft(opponentCards2);
		
		HBox opponentCards3 = new HBox(10, iv);
		opponentCards3.setAlignment(Pos.CENTER);
		
		pane1.setTop(opponentCards3);
		
		EventHandler<ActionEvent> clickHit = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				Card c = deck.drawCard();
				playerCards.add(c);
				cardImages.getChildren().add(c.getPic());
				playMessages.appendText("You Hit!\n");
				int s = 0;
				for(Card c1 : playerCards) {
					s += c1.getScore();
				}
				if(s > 21) {
					playMessages.appendText("You bust at " + s + "\n");
					hit.setDisable(true);
					pass.setDisable(true);
					bet.setDisable(true);
				}
			}
		};
		EventHandler<ActionEvent> clickPass = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				playMessages.appendText("You Passed!\n");
			}
		};
		EventHandler<ActionEvent> clickBet = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				int inBet = Integer.parseInt(betAmount.getText());
				int currentMoney = Integer.parseInt(moneyAmount.getText());
				if(inBet <= 0) {
					playMessages.appendText("You can not bet less than 1\n");
				}
				else if(inBet > currentMoney) {
					playMessages.appendText("You do not have enough money to bet $" + inBet + "\n");
				}
				else {
					int inPot = Integer.parseInt(potAmount.getText());
					Integer newPot = inPot + inBet;
					potAmount.setText(newPot.toString());
					playMessages.appendText("You bet " + inBet + "\n");
					Integer moneyLeft = currentMoney - inBet;
					moneyAmount.setText(moneyLeft.toString());
				}
				
				
			}
		};
		EventHandler<ActionEvent> clickQuit = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				
			}
		};
		
		hit.setOnAction(clickHit);
		pass.setOnAction(clickPass);
		bet.setOnAction(clickBet);
		quit.setOnAction(clickQuit);
		
		
		
		pane1.setBottom(playerUI);
		
		return pane1;
	}
	
	//disables all the buttons except the set IP and Port
	private void disableAll() {
		
	}
	
	
	private Client createClient(String tempIP, Integer tempPort) {
		//the client is sending the data and this decides what will be done with the data 
		return new Client(tempIP, tempPort, data -> {
			Platform.runLater(()->{
				playMessages.appendText(data.toString() + "\n");
			});
		});
	}
	
	/* #########################################################################################
	 * Embedded Client class that actually sets up the connection
	 * #########################################################################################
	 */
	
	public class Client {

		private String ip;
		private int port; 
		Consumer<Serializable> callback;
		private ConnThread connthread = new ConnThread();
		public Client(String ip, int port, Consumer<Serializable> callback) {
			this.callback = callback;
			this.ip = ip;
			this.port = port;
			connthread.setDaemon(true);
		}

		public void startConn() throws Exception{
			connthread.start();
		}
		
		public void send(Serializable data) throws Exception{
			connthread.out.writeObject(data);
		}
		
		public void closeConn() throws Exception{
			connthread.socket.close();
		}
		protected boolean isServer() {
			return false;
		}

		protected String getIP() {
			return this.ip;
		}

		protected int getPort() {
			return this.port;
		}
		
		class ConnThread extends Thread{
			private Socket socket;
			private ObjectOutputStream out;
			
			public void run() {
				try{
					Socket socket = new Socket(getIP(), getPort());
					ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream( socket.getInputStream());
					this.socket = socket;
					this.out = out;
					socket.setTcpNoDelay(true);
						
					while(!stop) {
						Serializable data = (Serializable) in.readObject();
						if(data.toString().equals("Quit")) {
							playMessages.appendText("Server is closing.\n");
							this.socket.close();
							break;
							
						}
						else if(data.toString().contains("started")) {
							
						}
						else if(data.toString().contains("closing") || data.toString().contains("Tie!")) {
							
						}
						else if(data.toString().contains("You")) {
							
						}
						//When the server gives you the player list
						// "Players: name1 name2 name3 name4"
						else if(data.toString().contains("Players: ")) {
							int firstSpace = data.toString().indexOf(" ", 1);
							int secondSpace = data.toString().indexOf(" ", 2);
							int thirdSpace = data.toString().indexOf(" ", 3);
							int fourthSpace = data.toString().indexOf(" ", 4);
							
							String name1 = data.toString().substring(firstSpace, secondSpace);
							String name2 = data.toString().substring(secondSpace, thirdSpace);
							String name3 = data.toString().substring(thirdSpace, fourthSpace);
							String name4 = data.toString().substring(fourthSpace);
							
							opponents.add(name1);
							opponents.add(name2);
							opponents.add(name3);
							opponents.add(name4);
							opponents.remove(playerName);
							
							playMessages.appendText("You are playing against: ");
							for(String s : opponents) {
								playMessages.appendText(s + ", ");
							}
							playMessages.appendText("\n");
							nameMessages.appendText(data.toString());
							
						}
						
						//when the server send "NAME has joined the server!"
						else if(data.toString().contains("joined the server!")) {
							int firstSpace = data.toString().indexOf(" ", 1);
							String n = data.toString().substring(0, firstSpace);
							opponentList.add(n);
							opponentListView.refresh();
						}
						//when a player disconnects, remove them from the opponent list
						else if(data.toString().contains("Has Disconnected!")) {
							int firstSpace = data.toString().indexOf(" ", 1);
							String n = data.toString().substring(0, firstSpace);
							opponentList.remove(n);
							opponentListView.refresh();
						}

						//If another user has challenged you, display their name in the textbox and enable
						// accept and reject buttons

						else if(data.toString().contains(" has challenged ")) {
							int firstSpace = data.toString().indexOf("challenged");
							String n = data.toString().substring(firstSpace+11);

							if (n.equals(playerName)){
								int thirdSpace = data.toString().indexOf(" ", 1);
								String k = data.toString().substring(0, thirdSpace);
								
							}
						}
                           //###################################################
                          //ALWAYS GOES TO CATCH, NEEDS MODIFICATION
						else if(data.toString().contains(" has accepted")) {
							int firstSpace = data.toString().indexOf("from");
							String n = data.toString().substring(firstSpace + 5);
							if (n.equals(playerName)) {
								int thirdSpace = data.toString().indexOf(" ", 1);
								String k = data.toString().substring(0, thirdSpace);
									send(playerName + " ready for " + k);
							}
						}
						//when the server sends that you won, increase score and display it
						else if(data.toString().contains("You Won!")) {
							score++;
							scoreCounter.textProperty().set(score.toString());
						}
						callback.accept(data);
					}
					
				}
				catch(Exception e) {
					nameMessages.appendText("Could not connect to server.\nPlease close and try again.\n");
					playMessages.appendText("Could not connect to server.\nPlease close and try again.\n");
					enterName.setDisable(true);
					play.setDisable(true);
				}
			}
		}

	}
}