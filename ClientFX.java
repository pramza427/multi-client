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
	Button setIPandPort, quit, hit, pass, bet, enterName, play, fold, draw;
	TextField betAmount, potAmount, moneyAmount;
	Stage myStage;
	Scene nameScene, playScene;
	private TextArea nameMessages = new TextArea();
	private TextArea playMessages = new TextArea();
	boolean stop, first1, first2, first3;
	String playerName = "";
	boolean hasFold = false;
	private final ObservableList<String> opponentList = FXCollections.observableArrayList();
	ListView<String> opponentListView = new ListView<String>();
	private Integer score = 0;
	Label scoreCounter = new Label(score.toString());

	Card op1Card, op2Card, op3Card;

	ArrayList<Card> playerCards = new ArrayList<Card>();
	ArrayList<String> opponents = new ArrayList<String>();

	HBox cardImages = new HBox();
	HBox opponentCards1;
	HBox opponentCards2;
	HBox opponentCards3;

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

		first1 = true;
		first2 = true;
		first3 = true;

		cardImages.setAlignment(Pos.CENTER);

		hit = new Button("Hit");
		hit.setPrefSize(50, 30);
		pass = new Button("Pass");
		pass.setPrefSize(50, 30);
		draw = new Button("Draw");
		draw.setPrefSize(50, 30);

		fold = new Button("Fold");
		fold.setPrefSize(50, 30);
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

		BorderPane playPane = playScreen(primaryStage);
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

	private BorderPane playScreen(Stage pStage){

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

		HBox moneyBox = new HBox(8, money, moneyAmount);
		money.setFont(Font.font(30));
		moneyBox.setAlignment(Pos.CENTER);
		HBox inputLeft = new HBox(8, hit, pass, draw);
		HBox inputRight = new HBox(8, fold, bet, sign, betAmount);
		HBox walkAway = new HBox(quit);
		walkAway.setAlignment(Pos.CENTER);
		HBox inputs = new HBox(40, inputLeft, inputRight);
		inputs.setAlignment(Pos.CENTER);
		Label you = new Label("You:");
		you.setFont(Font.font(20));
		VBox playerUI = new VBox(10, you, cardImages, moneyBox, inputs, walkAway);
		playerUI.setAlignment(Pos.CENTER);
		//Finish player UI

		//Set opponent UI

		opponentCards1 = new HBox(2);
		opponentCards1.setAlignment(Pos.CENTER);

		pane1.setRight(opponentCards1);

		opponentCards2 = new HBox(2);
		opponentCards2.setAlignment(Pos.CENTER);

		pane1.setLeft(opponentCards2);

		opponentCards3 = new HBox(2);
		opponentCards3.setAlignment(Pos.CENTER);

		pane1.setTop(opponentCards3);

		EventHandler<ActionEvent> clickHit = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				playMessages.appendText("You chose to hit!\n");
				try {

					conn.send(playerName + " chose to hit\n");

				} catch (Exception e) {
					playMessages.appendText("Could not hit\n");
					System.out.println(e.getMessage());
				}
			}
		};
		EventHandler<ActionEvent> clickPass = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){

				playMessages.appendText("You Passed!\n");

				try {
					conn.send(playerName + " chose to pass\n");
					disableAll();
				} catch (Exception e) {
					playMessages.appendText("Could not pass\n");
					System.out.println(e.getMessage());
				}
			}
		};
		EventHandler<ActionEvent> clickDraw = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){

				playMessages.appendText("You drew your first two cards!\n");

				try {
					conn.send(playerName + " chose to hit\n");
					conn.send(playerName + " chose to hit\n");

					draw.setDisable(true);

					//enableAll();
					
					bet.setDisable(false);
					fold.setDisable(false);
					
				} catch (Exception e) {
					playMessages.appendText("Could not draw your first two cards\n");
					System.out.println(e.getMessage());
				}
			}
		};
		EventHandler<ActionEvent> clickFold = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){

				playMessages.appendText("You chose to fold!\n");

				try {
					conn.send(playerName + " chose to fold\n");
					hasFold = true;
					disableAll();
				} catch (Exception e) {
					playMessages.appendText("Could not fold\n");
					System.out.println(e.getMessage());
				}
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
					
					try {
						conn.send(playerName + " has placed a bet!\n");
						bet.setDisable(true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						playMessages.appendText("Could not bet\n");
						System.out.println(e.getMessage());
					}
				}
			}
		};
		EventHandler<ActionEvent> clickQuit = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				playMessages.appendText(playerName + " Quit");
				pStage.setScene(nameScene);

				try {
					conn.send( playerName + " Quit");
				} catch (Exception e){
					playMessages.appendText("Could not quit");
					System.out.println(e.getMessage());
				}
			}
		};

		hit.setOnAction(clickHit);
		pass.setOnAction(clickPass);
		fold.setOnAction(clickFold);
		bet.setOnAction(clickBet);
		quit.setOnAction(clickQuit);
		draw.setOnAction(clickDraw);

		disableAll();

		pane1.setBottom(playerUI);

		return pane1;
	}

	//disables all the buttons On the UI
	private void disableAll() {
		hit.setDisable(true);
		pass.setDisable(true);
		fold.setDisable(true);
		bet.setDisable(true);
		draw.setDisable(true);
	}
	//enables all the buttons on the UI
	private void enableAll() {
		hit.setDisable(false);
		pass.setDisable(false);
		fold.setDisable(false);
		bet.setDisable(false);
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

							//enableAll();
							draw.setDisable(false);
							callback.accept(data);
						}

						//when the server send "NAME has joined the server!"
						else if(data.toString().contains("joined the server!")) {
							int firstSpace = data.toString().indexOf(" ", 1);
							String n = data.toString().substring(0, firstSpace);
							opponentList.add(n);
							opponentListView.refresh();
							opponents.add(n);
							callback.accept(data);
						}
						//when a player disconnects, remove them from the opponent list
						else if(data.toString().contains("Has Disconnected!")) {
							int firstSpace = data.toString().indexOf(" ", 1);
							String n = data.toString().substring(0, firstSpace);
							opponentList.remove(n);
							opponentListView.refresh();
							callback.accept(data);
						}

						////////////////////////////////////////////////////////
						else if(data.toString().contains(" has drawn")) {
							int substringOne = data.toString().indexOf(" ", 1);
							int substringTwo = data.toString().indexOf("drawn");
							int substringThree = data.toString().indexOf("of");

							String name = data.toString().substring(0, substringOne);
							String suit = data.toString().substring(substringTwo + 6, substringThree - 1);
							String numString = data.toString().substring(substringThree + 3);
							int number = Integer.parseInt(numString);

							Card c = new Card(number, suit);
							//if its an opponent that drew a card, get the index from the opponent list
							int opIndex = opponents.indexOf(name);

							if(name.equals(playerName)) {
								playerCards.add(c);
								score += c.getScore();
								if(score > 21) {
									playMessages.appendText("you bust at " + score);
									bet.setDisable(true);
									hit.setDisable(true);
									fold.setDisable(true);
									pass.setDisable(true);
								}

								Platform.runLater(()-> {
									cardImages.getChildren().add(c.getPic());
								});
							}

							else if(opIndex == 0) {
								if(first1) {
									first1 = false;
									Image back = new Image("PNG/back.png");
									ImageView backIV = new ImageView(back);
									backIV.setFitHeight(200);
									backIV.setFitWidth(100);
									backIV.setPreserveRatio(true);
									op1Card = c;
									Platform.runLater(()-> {
										opponentCards1.getChildren().add(backIV);
									});
								}
								else {
									Platform.runLater(()-> {
										opponentCards1.getChildren().add(c.getPic());
									});
								}
							}
							else if(opIndex == 1) {
								if(first2) {
									first2 = false;
									Image back = new Image("PNG/back.png");
									ImageView backIV = new ImageView(back);
									backIV.setFitHeight(200);
									backIV.setFitWidth(100);
									backIV.setPreserveRatio(true);
									op2Card = c;
									Platform.runLater(()-> {
										opponentCards2.getChildren().add(backIV);
									});
								}
								else {
									Platform.runLater(()-> {
										opponentCards2.getChildren().add(c.getPic());
									});
								}
							}
							else if(opIndex == 2) {
								if(first3) {
									first3 = false;
									Image back = new Image("PNG/back.png");
									ImageView backIV = new ImageView(back);
									backIV.setFitHeight(200);
									backIV.setFitWidth(100);
									backIV.setPreserveRatio(true);
									op3Card = c;
									Platform.runLater(()-> {
										opponentCards3.getChildren().add(backIV);
									});
								}
								else {
									Platform.runLater(()-> {
										opponentCards3.getChildren().add(c.getPic());
									});
								}
							}
						}
						
						else if(data.toString().contains("Starting")) {
							if(hasFold == true) {
								disableAll();
							}
							else {
								bet.setDisable(true);
								fold.setDisable(true);
								hit.setDisable(false);
								pass.setDisable(false);
							}
							/*
							bet.setDisable(true);
							hit.setDisable(false);
							pass.setDisable(false);
							*/
							callback.accept(data);
						}

						///////////////////////////////////////////////////////

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
							callback.accept(data);
						}
						//when the server sends that you won, increase score and display it
						else if(data.toString().contains("You Won!")) {
							score++;
							scoreCounter.textProperty().set(score.toString());
							callback.accept(data);
						}

					}

				}
				catch(Exception e) {
					nameMessages.appendText("Could not connect to server.\nPlease close and try again.\n");
					playMessages.appendText("Could not connect to server.\nPlease close and try again.\n");
					enterName.setDisable(true);
					play.setDisable(true);
					System.out.println(e.getMessage());
				}
			}
		}

	}
}