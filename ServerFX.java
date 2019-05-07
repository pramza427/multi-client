/**
 * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 5
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ServerFX extends Application{
	
	Label welcome;
	Button setPort, quit, on, off;
	Stage myStage;
	Scene scene;
	private TextArea messages = new TextArea();
	//keep track of player score
	Integer score1, score2, round;
	//move1 holds the String of whoever chose first
	TextField serverPortIn;
	int portNumber = 0;
	boolean play1, play2, stop;
	//The server connection for 4 people
	Server.toClientThread tct = null;
	Server.toClientThread tct2 = null;
	Server.toClientThread tct3 = null;
	Server.toClientThread tct4 = null;
	private Server conn = null;
	private final ObservableList<String> clientList = FXCollections.observableArrayList();
	
	ArrayList<Server.toClientThread> clientThreadList= new ArrayList<Server.toClientThread>();
	
	ArrayList<Server.toClientThread> ReadyList= new ArrayList<Server.toClientThread>();
	
	Deck deck = new Deck();
	
	public static void main(String[] args) throws Exception {
		launch(args);
	}
	
	@Override
	public void stop(){
		stop = true;
		tct = null;
		tct2 = null;
		tct3 = null;
		tct4 = null;
		try {
			conn.server.serverSocket.close();
			
		} catch (Exception e) {
			System.out.println("serverSocket.close in stop() did not run!");
		}
		
		
	}
	
	public void start(Stage primaryStage) throws Exception {
		
		//set player scores to 0
		score1 = 0;
		score2 = 0;
		//set play1 and play2 to false, these control whether players want to play again
		play1 = false;
		play2 = false;
		//set round (which has a max of 2) to 1
		round = 1;
		stop = false;
		
		deck.shuffle();
		
		primaryStage.setTitle("Server for Rock Paper Scissors Lizard Spock!");
		
		quit = new Button("Quit");
		on = new Button("On");
		off = new Button("Off");
		setPort = new Button("Set Port");
		
		myStage = primaryStage;
		
		BorderPane pane1 = serverScreen();

		scene = new Scene(pane1, 850, 500);
		
		primaryStage.setScene(scene);
		primaryStage.show();	
		
	}
	
	
	
	
	private BorderPane serverScreen(){
		
		//Initialize the Border pane that will be returned
		BorderPane pane1 = new BorderPane();
		pane1.setPadding(new Insets(10));
		
		//Label that greats the player to the game
		Label welcome = new Label("This is the Server for Rock Paper Scissors Lizard Spock!\n");
		Label space = new Label(" ");
		VBox welcomeBox = new VBox(10, welcome, space);
		welcomeBox.setAlignment(Pos.TOP_CENTER);
		pane1.setTop(welcomeBox);

		
		//Set the actions for what happens when the buttons are clicked
		EventHandler<ActionEvent> clickOn = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				if(portNumber != 0) {
					conn = createServer(portNumber);
					messages.appendText("Server Created at port " + portNumber + "\n");
					stop = false;
					on.setDisable(true);
					off.setDisable(false);
					setPort.setDisable(true);
				}
				else {
					messages.appendText("Please enter a valid Port number first\n");
				}
			}
		};
		//###############################################################
		//############ PROBLEM: server off button does not work
		//############          always goes to catch 
		//############     clicking the X at the top right works fine tho
		//###############################################################
		EventHandler<ActionEvent> clickOff = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				try {
					//conn.server.serverSocket.close();
					conn.closeConn();
					stop = true;
					on.setDisable(false);
					off.setDisable(true);
					setPort.setDisable(false);
					messages.appendText("Server was turned off.\n");
				} catch (Exception e) {
					messages.appendText("Server socket could not close.\n");
				}
			}
		};
		EventHandler<ActionEvent> clickSetPort = new EventHandler<ActionEvent>(){
			public void handle(ActionEvent event){
				if(Integer.parseInt(serverPortIn.getText()) < 1 || Integer.parseInt(serverPortIn.getText()) > 66666) {
					messages.appendText("Port number must be between 1 and 66666\n");
				}
				else {
					portNumber = Integer.parseInt(serverPortIn.getText());
					messages.appendText("Port number set to: " + portNumber + "\n");
				}
			}
		};
		
			
		//set buttons to do things!
		on.setOnAction(clickOn);
		off.setOnAction(clickOff);
		setPort.setOnAction(clickSetPort);
		
		//prompt to ask client for IP and Port
		Label ipPortPrompt = new Label("Please enter the Port:");
		serverPortIn = new TextField("5555");
		serverPortIn.setPrefWidth(60.0);
		HBox serverInput = new HBox(10, ipPortPrompt, serverPortIn, setPort);
		serverInput.setAlignment(Pos.CENTER);
		
		//Port and on/off buttons to the center of the pane
		Label onOrOff = new Label("Turn Server: ");
		HBox onOffBox= new HBox(8, onOrOff, on, off);
		onOffBox.setAlignment(Pos.CENTER);
		//message box that tells the players what's happening
		messages.setPrefHeight(365);
		VBox centerBox = new VBox(8, serverInput, onOffBox, messages);
		pane1.setCenter(centerBox);
		
		ListView<String> clientListView = new ListView<String>();
		clientListView.setItems(clientList);
		clientListView.setPrefWidth(350);
		Label listLabel = new Label("List of connected Players:");
		VBox listBox = new VBox(8, listLabel, clientListView);
		listBox.setAlignment(Pos.CENTER);
		pane1.setRight(listBox);
		 
		off.setDisable(true);
		
		return pane1;
	}
	
	private Server createServer(Integer tempPort) {
		try {
			return new Server(tempPort, data-> {
				Platform.runLater(()->{
					messages.appendText(data.toString());
				});
			});
		} catch (IOException e) {
			messages.appendText("Could not create Server.");
		}
		return conn;
	}
	
	public ObservableList<String> getClientList(){
		return clientList;
	}
	/* #########################################################################################
	 * Embedded Server class that actually sets up the connection
	 * #########################################################################################
	 */
	public class Server {

		private int port;
		private Consumer<Serializable> callback;
		int round = 1;
		String move1, move2;
		Socket s;
		ServerThread server;
		public Server(int port, Consumer<Serializable> callback) throws IOException {
			this.callback = callback;
			this.port = port;
			server = new ServerThread();
			server.start();
			
		}

		protected boolean isServer() {
			return true;
		}

		protected String getIP() {
			return null;
		}

		protected int getPort() {
			return port;
		}
		
		public void closeConn() throws Exception{
			if(tct.connection != null) {
				tct.connection.close();
			}
			if(tct2.connection != null) {
				tct2.connection.close();
			}
			
		}
		
		public void sendAll(Serializable data) throws Exception{
			//go through the arrayList of connections and send the data to each
			for(toClientThread tct: clientThreadList) {
				tct.out.writeObject(data);
			}
		}
		public void send(Serializable data) throws Exception{
			//only send to the 4 connected players
			tct.out.writeObject(data);
			tct2.out.writeObject(data);
			tct3.out.writeObject(data);
			tct4.out.writeObject(data);
		}
		
		public void startGame() throws Exception {
			
			
			tct = ReadyList.get(0);
			tct2 = ReadyList.get(1);
			tct3 = ReadyList.get(2);
			tct4 = ReadyList.get(3);
			
			String s = "Players: " + clientList.get(0) + " " + clientList.get(1) + " "
					+ clientList.get(2) + " " + clientList.get(3);		
			try {
				sendAll(s);
			} catch (Exception e) {
				messages.appendText("Cound not start game.");
			}
		}
		
		
		public void dealInitialCards(String n){
			
			Card c1 = deck.drawCard();
			Card c2 = deck.drawCard();
			
			String s = "Dealing: " + n + " " + c1.getSuit() + " " + c1.getNumber() + " " + c2.getSuit() + " " + c2.getNumber();
			
			try {
				sendAll(s);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				messages.appendText("Cound deal two cards.");
			}
			
		}
	
		
		
		class ServerThread extends Thread{
			ServerSocket serverSocket = null;
			public void run() {
				try {
					serverSocket = new ServerSocket(portNumber);
				} catch (IOException e1) {
					callback.accept("Server Couldn't connect to socket\n");
				}
				while(!stop) {
					try {
						Socket s = serverSocket.accept();
						toClientThread t = new toClientThread(s);
						//store each connection into an ArrayList
						clientThreadList.add(t);
						t.start();
						messages.appendText("Client Connected: " + s + "\n");
						
					}
					catch(Exception e) {
						callback.accept("Connection Closed\n");
						callback.accept("### Solution: Terminate Program in Console ###\n");
						e.printStackTrace();
						break;
					}
				}
			}
		}
		
		// Embedded Thread class
		class toClientThread extends Thread{
			
			Socket connection;
			ObjectInputStream in;
			ObjectOutputStream out;
			int score;
			
			toClientThread(Socket s){
				this.connection = s;
				score = 0;
			}
			
			public void run() {
				try{
					ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
					s = connection;
					this.out = out;
					this.in = in;
					
					while(!stop) {
						Serializable data = (Serializable) in.readObject();
						String stringData = (String) data;
						
						//What to do when a Client chooses to quit
						if(stringData.contains("Quit")) {
							messages.appendText("Connection " + connection + " is closing.\n");
							int firstSpace = stringData.indexOf(" ", 1);
							String n = stringData.substring(0, firstSpace);
							clientList.remove(n);
							clientThreadList.remove(this);
							this.connection.close();
							this.in.close();
							this.out.close();
							conn.send(n +" Has Disconnected!");
							break;
						}
						
						///////////////////////////////////////////////////////////////////////
						if(stringData.contains("hit")) {
							Card c = deck.drawCard();
							
							int firstSpace = stringData.indexOf(" ", 1);
							
							String n = stringData.substring(0, firstSpace);
							messages.appendText(n + " chose to hit! They drew: " + c.getSuit() + c.getNumber() + "\n");
							
							conn.sendAll(n + " has drawn " + c.getSuit() + " of "  + c.getNumber());
							//playerName has drawn S of N 
						}
						
						if(stringData.contains("drew their first two cards")) {
							
							int firstSpace = stringData.indexOf(" ", 1);
							String n = stringData.substring(0, firstSpace);
							dealInitialCards(n);
							
						}
						
						if(stringData.contains("playing against")) {
						}
						//////////////////////////////////////////////////////////////////////
						
						if(stringData.contains("ReadyToPlay:")) {
							boolean startedGame = false;
							String s = stringData.substring(13);
							messages.appendText(s + "\n");
							int clientIndex = clientList.indexOf(s);
							if(clientIndex != -1) {
								ReadyList.add(clientThreadList.get(clientIndex));
								if(ReadyList.size() == 4) {
									startGame();
								}
							}
							else {
								messages.appendText("clientIndex was wrong");
							}
						}
						
						//New player has Joined the server
						//#########################################################
						// This is where you would check if the player name already exists
						//#########################################################
						if(stringData.contains("New:")) {
							String newName = stringData.substring(5);
							for(String name: clientList) {
								this.out.writeObject(name + " has joined the server!");
							}
							clientList.add(newName);
							conn.sendAll(newName + " has joined the server!");
						}
						
					}	
				}
				catch (Exception e) {
					callback.accept("Connection Closed\n");
				}	
				
			}
			
		}
		
	}
	
	
}