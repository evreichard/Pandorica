import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.geometry.*;
import javafx.scene.image.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

import java.net.URL;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.Console;

import javax.imageio.ImageIO;

public class PandoricaGUI extends Application{
	private Stage pandoricaStage;
	
	// Receives Singleton Objects (And creates it if they don't already exist)
	private MainPandora pandoraBackEnd = MainPandora.getInstance();
	private QueueManager queueMan = QueueManager.getInstance();
	
	// The lock object
	private final Object threadLock = new Object();

	// Only created when we're using the '-nogui' flag
	private Scanner in;
	
	private ComboBox stationList;
	
	public static void main(String[] args){
		Platform.setImplicitExit(false);
		if(args.length == 0){
			launch();
		}else if(args.length == 1 && args[0].equals("-nogui")){
			// Execute no gui
			
		}else{
			System.out.println("Invalid Command Line Arguments.\n\t-nogui\n\t\tAppend this flag to execute the program with no gui.");
		}
    }

    @Override
    public void start(Stage primaryStage){
		pandoricaStage = primaryStage;
		
		pandoricaStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
		   @Override
		   public void handle(WindowEvent t) {
			  Platform.exit();
			  System.exit(0);
		   }
		});
		
		// If settings have auto login, skip login scene and go directly to the player
		loginScene();
    }

	public void loginScene(){
		pandoricaStage.setTitle("Pandorica Login");
		
		GridPane loginGrid = new GridPane();
		loginGrid.setAlignment(Pos.CENTER);
		loginGrid.setHgap(10);
		loginGrid.setVgap(10);
		loginGrid.setPadding(new Insets(25, 25, 25, 25));
		
		Text passwordTitle = new Text("Please Login");
		passwordTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		
		Text emailText = new Text("Email:");
		Text passwordText = new Text("Password:");
		
		TextField emailField = new TextField();
		PasswordField passwordField = new PasswordField();
		
		Button loginButton = new Button("Login");
		Button cancelButton = new Button("Cancel");
		
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event){
				if(pandoraBackEnd.pandoraLogin(emailField.getText(), passwordField.getText())){
					loginGrid.getChildren().removeAll();
					playerScene();
					LoadGUIStations();
				}else{
					System.out.println("Logon failed");
				}
            }
        });
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
 
            @Override
            public void handle(ActionEvent event){
				System.exit(0);
            }
        });
		
		loginGrid.add(passwordTitle, 0, 0, 2, 1);
		loginGrid.add(emailText, 0, 1);
		loginGrid.add(passwordText, 0, 2);
		loginGrid.add(emailField, 1, 1, 3, 1);
		loginGrid.add(passwordField, 1, 2, 3, 1);
		loginGrid.add(loginButton, 3, 3);
		loginGrid.add(cancelButton, 2, 3);
		
        pandoricaStage.setScene(new Scene(loginGrid, 800, 370));
        pandoricaStage.show();
	}
	
	public void playerScene(){
		pandoricaStage.setTitle("Pandorica");
		
		GridPane playerGrid = new GridPane();
		playerGrid.setPadding(new Insets(10, 10, 10, 10));
		
		Button toggleButton = new Button("||");
		Button nextButton = new Button(">>");
		
		Image settingsIcon = new Image(getClass().getResourceAsStream("res/settings.png"));
		
		Button settingsButton = new Button(null, new ImageView(settingsIcon));
		stationList = new ComboBox();
		
		toggleButton.setPrefSize(100, 25);
		nextButton.setPrefSize(100, 25);
		settingsButton.setPrefSize(25, 25);
		stationList.setPrefSize(300, 25);
		
		stationList.valueProperty().addListener(new ChangeListener<ComboItem>(){
            @Override 
            public void changed(ObservableValue ov, ComboItem oldObject, ComboItem newObject){
				if(newObject != null){
					queueMan.stop();
					queueMan.playStation(newObject.getValue());
				}
            }    
        });
		
        toggleButton.setOnAction(new EventHandler<ActionEvent>(){
 
            @Override
            public void handle(ActionEvent event){
               queueMan.toggle();
            }
        });
		
		nextButton.setOnAction(new EventHandler<ActionEvent>(){
 
            @Override
            public void handle(ActionEvent event){
				queueMan.nextSong();
            }
        });
		
		settingsButton.setOnAction(new EventHandler<ActionEvent>(){
 
            @Override
            public void handle(ActionEvent event){
				
            }
        });
		
		Text songText = new Text();
		Text artistText = new Text();
		Text progressText = new Text();
		
		// Load and set local fonts
		Font.loadFont(getClass().getResource("res/Raleway.ttf").toExternalForm(), 30);
		Font.loadFont(getClass().getResource("res/Ubuntu.ttf").toExternalForm(), 14);
		songText.setFont(Font.font("Raleway", FontWeight.NORMAL, 30));
		artistText.setFont(Font.font("Raleway", FontWeight.NORMAL, 14));
		progressText.setFont(Font.font("Ubuntu", FontWeight.NORMAL, 14));

		String imgURL = (String)getClass().getResource("res/PandoricaDummyArt.png").toString();
		Image defaultAlbumArt = new Image(imgURL);
		ImageView albumImage = new ImageView(defaultAlbumArt);
		
		/*
		URL sourceImage = new URL(currentSongInfo.getAlbumArtUrl());
		Image AAImage = ImageIO.read(sourceImage).getScaledInstance(300, 300,  java.awt.Image.SCALE_SMOOTH);  
		albumArt.setIcon(new ImageIcon(AAImage));
		*/
		
		GridPane songContent = new GridPane();
		songContent.setHgap(10);
		songContent.setVgap(10);
		songContent.add(songText, 0, 0);
		songContent.add(artistText, 0, 1);
		songContent.add(progressText, 0, 2);
		
		GridPane songControl = new GridPane();
		songControl.setHgap(10);
		songControl.setVgap(10);
		songControl.add(toggleButton, 0, 0);
		songControl.add(nextButton, 1, 0);
		songControl.add(stationList, 6, 0);
		
		BorderPane rightContent = new BorderPane();
		rightContent.setPadding(new Insets(10, 10, 10, 20));
		rightContent.setCenter(songContent);
		rightContent.setBottom(songControl);
		rightContent.setRight(settingsButton);
		
		playerGrid.add(albumImage, 0, 0);
		playerGrid.add(rightContent, 1, 0);
		
        pandoricaStage.setScene(new Scene(playerGrid, 800, 315));
        pandoricaStage.setResizable(false);
		pandoricaStage.show();
		
		
		Task songGUIContentUpdate = new Task<Void>(){
			@Override
			public Void call(){
				
				while(true){
					synchronized(threadLock){
						// On first start, we wait for song to start playing
						while(queueMan.getCurrentSong().getSongStatus() != PlayerStatus.PLAYING){
							// Sleep for 100ms
							try{
								Thread.sleep(100);
							}catch(Exception e){}
						}
						
						// Gather playing song
						PandoraSong currentSongInfo = queueMan.getCurrentSong();
						
						// Set album art
						Image albumArt = new Image(currentSongInfo.getAlbumArtUrl(), 300, 300, true, true);
						
						Platform.runLater(new Runnable(){
							@Override
							public void run(){
								// Set song title & artist
								songText.setText(currentSongInfo.getSongName());
								artistText.setText(currentSongInfo.getArtistName());
								albumImage.setImage(albumArt);
							}
						});
						
						// Loop while our cached song is the same as the one in queue
						while(currentSongInfo.getAudioUrl().equals(queueMan.getCurrentSong().getAudioUrl())){
							
							Platform.runLater(new Runnable(){
								@Override
								public void run(){
									progressText.setText(queueMan.getCurrentSongPosition() + " / " + queueMan.getCurrentSongLength());
									
									// Set toggleButton to the appropriate state
									if(currentSongInfo.getSongStatus() == PlayerStatus.PAUSED){
										toggleButton.setText("\u25BA");
									}else if(currentSongInfo.getSongStatus() == PlayerStatus.PLAYING){
										toggleButton.setText("||");
									}
								}
							});
							
							// Sleep for 100ms
							try{
								Thread.sleep(1000);
							}catch(Exception e){}
						}
						
					}
				}
			}
		};
		
		Thread GUIUpdater = new Thread(songGUIContentUpdate);
		GUIUpdater.setDaemon(true);
		GUIUpdater.start();
	}
	
	public void settingsScene(){
		
	}
	
	public void LoadGUIStations(){
		// Get Station List
		ArrayList<ArrayList<String>> tempStationList = pandoraBackEnd.getStationList();
		
		// Print and choose station
		for(ArrayList<String> tempAL : tempStationList){
			stationList.getItems().addAll(new ComboItem(tempAL.get(0), tempAL.get(1)));
		}
	}
	
	// --------------------------------------------------------------------------------------\\
	// --------------------------------- TERMINAL LOADERS ---------------------------------- \\
	// --------------------------------------------------------------------------------------\\
	
	public void LoadPasswordTerminal(){
		
		in = new Scanner(System.in);
		System.out.print("Email: ");
		String username = in.nextLine();
		Console console = System.console();
		char passwordArray[] = console.readPassword("Password: ");

		// Login with password
		pandoraBackEnd.pandoraLogin(username, new String(passwordArray));
		
		LoadPlayerTerminal();
	}

	public void LoadPlayerTerminal(){
		queueMan.saveAsMP3(true);

		queueMan.playStation(LoadTerminalStations());

		SongTerminalContentUpdate songClass = new SongTerminalContentUpdate();
		Thread songPlayingThread = new Thread(songClass);
		songPlayingThread.start();
		
		String tempIn;

		while(true){
			tempIn = in.next();

			if(tempIn.equals("p")){
				queueMan.pause();
			}else if(tempIn.equals("r")){
				queueMan.resume();
			}else if(tempIn.equals("n")){
				queueMan.nextSong();
			}else if(tempIn.equals("s")){
				// Stop everything here
				queueMan.stop();
				queueMan.playStation(LoadTerminalStations());
			}else if(tempIn.equals("h")){
				System.out.println("p - pause\nr - resume\nn - next\ns - stations");
			}
		}
	
	}
	
	public String LoadTerminalStations(){
		// Get Station List
		ArrayList<ArrayList<String>> tempStationList = pandoraBackEnd.getStationList();

		// Print and choose station
		for(ArrayList<String> tempAL : tempStationList){
			System.out.println("(" + (tempStationList.indexOf(tempAL) + 1) + ") " + tempAL.get(0) + ", " + tempAL.get(1));
		}

		System.out.print("\nPlease enter a station: ");
		int selection = in.nextInt() - 1;
		String stationId = tempStationList.get(selection).get(1);

		return stationId;
	}
	
	// --------------------------------------------------------------------------------------\\
	// --------------------------------- EMBEDDED CLASSES ---------------------------------- \\
	// --------------------------------------------------------------------------------------\\
	
	
	
	class SongTerminalContentUpdate implements Runnable{
		public void run(){
			synchronized(threadLock){
				while(true){

					// On first start, we wait for song to start playing
					while(queueMan.getCurrentSong().getSongStatus() != PlayerStatus.PLAYING){						
						
						// Sleep for 100ms
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
					
					// Gather playing song
					PandoraSong currentSongInfo = queueMan.getCurrentSong();
					
					
					String songName = currentSongInfo.getSongName();
					String artistName = currentSongInfo.getArtistName(); 
					String currentSongLength = queueMan.getCurrentSongLength();
					
					// Loop while our cached song is the same as the one in queue
					while(currentSongInfo.getAudioUrl().equals(queueMan.getCurrentSong().getAudioUrl())){
						String currentSongPosition = queueMan.getCurrentSongPosition();
						Integer bufferPercentage = queueMan.getBufferPercentage();
						
						System.out.print("\r" + "[" + bufferPercentage + "%] " + artistName + " - " + songName + " (" + currentSongPosition + " / " + currentSongLength + ")");
						
						// Sleep for 100ms
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
					
					System.out.println("\r" + artistName + " - " + songName + " (PLAYED)                            ");
					
				}
			}
		}
	}
	
	class ComboItem{
	
		private String key;
		private String value;

		public ComboItem(String key, String value){
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString(){
			return key;
		}

		public String getKey(){
			return key;
		}

		public String getValue(){
			return value;
		}
	}
}