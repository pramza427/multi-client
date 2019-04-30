/**
 * Piotr Ramza
 * pramza2
 * 663328597
 * 
 * CS 342 Project 3
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
	private int selectedIndex;
	ArrayList<Server.toClientThread> clientThreadList= new ArrayList<Server.toClientThread>();
	
	
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
		
		public void send(Serializable data) throws Exception{
			//go through the arrayList of connections and send the data to each
			for(toClientThread tct: clientThreadList) {
				tct.out.writeObject(data);
			}
		}
		
		public void startGame() {
			
			
			
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
						if(clientThreadList.size() == 4) {
							startGame();
						}
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
						//When a user challenges someone else
						if(stringData.contains("challenged")) {
							int thirdSpace = stringData.indexOf(" ", 1);
							int tSpace = stringData.indexOf("by");
							String t = stringData.substring(0, thirdSpace);
							String x = stringData.substring(tSpace+3);
                            conn.send (x + " has challenged " + t);
						}
                        //If a user declines a challenge
						if(stringData.contains("declined")) {
							int thirdSpace = stringData.indexOf(" ", 1);
							int tSpace = stringData.indexOf("declined");
							String t = stringData.substring(0, thirdSpace);
							String x = stringData.substring(tSpace+9);
							conn.send (t + " has declined the challenge from " + x);
						}
                        //If a user accepts a challenge
						if(stringData.contains("accepted")) {
							int thirdSpace = stringData.indexOf(" ", 1);
							int tSpace = stringData.indexOf("accepted");
							String t = stringData.substring(0, thirdSpace);
							String x = stringData.substring(tSpace+9);
							conn.send (t + " has accepted the challenge from " + x);
							tct.connection = this.connection;
						}
                         ///###################################################################
                        //DOES NOT WORK, NEEDS MODIFICATION
						if(stringData.contains("ready")) {
							int thirdSpace = stringData.indexOf(" ", 1);
							String t = stringData.substring(0, thirdSpace);
							int tSpace = stringData.indexOf("for");
							String x = stringData.substring(tSpace+4);
							conn.send(x + " is player 1.");
							tct2.connection = this.connection;
						}
						
						if(stringData.contains("NewGame:")) {
							//get the names of the players that want to battle
							int firstSpace = stringData.indexOf(" ", 1);
							int secondSpace = stringData.indexOf(" ", firstSpace + 2);
							String name1 = stringData.substring(firstSpace + 1, secondSpace);
							String name2 = stringData.substring(secondSpace + 1);
							//get the index of the 2 players
							int index1 = clientList.indexOf(name1);
							int index2 = clientList.indexOf(name2);
							if(index1 != -1 && index2 != -1) {
								tct = clientThreadList.get(index1);
								tct.out.writeObject("You are Player1\n");
								tct2 = clientThreadList.get(index2);
								tct2.out.writeObject("You are Player2\n");
								messages.appendText("Game between " + name1 + " and " + name2 + " has started!\n");
								tct.out.writeObject("Game between " + name1 + " and " + name2 + " has started!\n");
								tct2.out.writeObject("Game between " + name1 + " and " + name2 + " has started!\n");
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
							conn.send(newName + " has joined the server!");
						}
						
					}	
				}
				catch (Exception e) {
					callback.accept("Connection Closed\n");
				}	
				
			}
			
		}
		
		
		
		/**
		 * This function takes the strings of the two players choices and prints the winner and returns a string:
		 * @param a String of player1
		 * @param b	String of player2
		 * a and be are in {Rock, Paper, Scissors, Lizard, Spock}
		 * @return
		 * possible returns
		 * 0 for tie
		 * 1 if player1 won
		 * 2 if player2 won
		 */
		private int getWinner(String a, String b) throws Exception {

			if(a.equals(b)) {
				return 0;
			}
			
			else if(a.equals("Rock")) {
				if(b.equals("Lizard") || b.equals("Scissors")) {
					return 1;
				}
				else if(b.equals("Spock") || b.equals("Paper")) {
					return 2;
				}
			}
			
			else if(a.equals("Paper")) {
				if(b.equals("Rock") || b.equals("Spock")) {
					return 1;
				}
				else if(b.equals("Lizard") || b.equals("Scissors")) {
					return 2;
				}
			}
			
			else if(a.equals("Scissors")) {
				if(b.equals("Paper") || b.equals("Lizard")) {
					return 1;
				}
				else if(b.equals("Spock") || b.equals("Rock")) {
					return 2;
				}
			}
			
			else if(a.equals("Lizard")) {
				if(b.equals("Paper") || b.equals("Spock")) {
					return 1;
				}
				else if(b.equals("Scissors") || b.equals("Rock")) {
					return 2;
				}
			}
			
			else if(a.equals("Spock")) {
				if(b.equals("Rock") || b.equals("Scissors")) {
					return 1;
				}
				else if(b.equals("Paper") || b.equals("Lizard")) {
					return 2;
				}
			}
			
			return 0;
		}
	}
	
	
}