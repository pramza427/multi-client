/**
 * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 3
 */
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
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
	
	Label welcome;
	Button setIPandPort, quit, rock, paper, scissors, lizard, spock, challenge, acceptChallenge, declineChallenge;
	TextField challenger;
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
		
		rock = new Button();
		paper = new Button();
		scissors = new Button();
		lizard = new Button();
		spock = new Button();
		quit = new Button("Quit");
		setIPandPort = new Button("Set IP and Port");
		
		myStage = primaryStage;
		
		BorderPane playPane = playScreen();
		BorderPane namePane = nameScreen(primaryStage);
		
		nameScene = new Scene(namePane, 400, 400);
		playScene = new Scene(playPane, 1000, 900);
		
		primaryStage.setScene(playScene);
		primaryStage.show();	
		
	}
	
	//Create the Screen where the player chooses a name
	private BorderPane nameScreen(Stage pStage) {
		BorderPane tempPane = new BorderPane();
		tempPane.setPadding(new Insets(30));
		//Second prompt the player to choose a name to display
		TextField name = new TextField();
		Label askName = new Label("Please enter the name you want displayed.");
		Button enterName = new Button("Set Name");
		Button play = new Button("Play!");
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
				if(name.getText().length() < 3) {
					nameMessages.appendText("Names must be at least 3 letters");
				}
				else {
					try {
						playerName = name.getText();
						playMessages.appendText("You Are: " + playerName + "!!!\n");
						pStage.setScene(playScene);
						conn.send("New: " + playerName);
					
					} catch (Exception e) {
						nameMessages.appendText("Could not send name\n");
					}
				}
			}
		};
		enterName.setOnAction(clickEnterName);
		HBox nameLine = new HBox(10, name, enterName);
		VBox nameAll = new VBox(10 , askName, nameLine);
		
		//set the middle to prompts and buttons
		VBox all = new VBox(20, ipPortAll, nameAll);
		tempPane.setCenter(all);
		
		//set the bottom to a Text Field to display messages
		tempPane.setBottom(nameMessages);
		enterName.setDisable(true);
		
		return tempPane;
	}
	
	private BorderPane playScreen(){
		
		//Initialize the Border pane that will be returned
		BorderPane pane1 = new BorderPane();
		pane1.setPadding(new Insets(5, 5, 5, 5));
		
		
		//set a background color of green with a black background
		Rectangle blackBG = new Rectangle(1000, 900, Color.BLACK);
		Rectangle greenBG = new Rectangle(980, 880, Color.GREEN);
		greenBG.setArcHeight(50);
		greenBG.setArcWidth(50);
		
		StackPane allBG = new StackPane(blackBG, greenBG);
		
		VBox background = new VBox(allBG);
		
		
		
		EventHandler<ActionEvent> clickRock = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				
			}
		};
		
		pane1.getChildren().add(background);
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
								challenger.setText(k);
								acceptChallenge.setDisable(false);
								declineChallenge.setDisable(false);
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
					callback.accept("Connection Closed\n");
				}
			}
		}

	}
}