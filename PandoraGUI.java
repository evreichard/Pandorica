/**
 * Evan Reichard
 * https://github.com/evreichard
 * evan@evanreichard.com
 * 2013 - 2014
 **/

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PandoraGUI{

	public final static int NOTSTARTED = 0;
	public final static int PLAYING = 1;
    public final static int PAUSED = 2;
	public final static int FINISHED = 3;
	public final static int STOPPED = 4;
	
	MainPandora pandoraBackEnd = new MainPandora();
	QueueManager queueMan = new QueueManager(pandoraBackEnd);
	Scanner in = new Scanner(System.in);
	
	PandoraSong currentSong = new PandoraSong();
	private final Object threadLock = new Object();

	JComboBox<ComboItem> stationCB = new JComboBox<ComboItem>();
	JLabel songTitle = new JLabel();
	JLabel artistName = new JLabel();
	JLabel songProgress = new JLabel();
	JLabel albumArt = new JLabel();
	
	public PandoraGUI(boolean GUI){
		if(GUI){
			PromptGUIPassword();
		}else{
			PromptTerminalPassword();
		}
	}
	
	public void LoadMainScreen(){
		JFrame mainFrame = new JFrame("Pandorica");
		mainFrame.setSize(800, 370);
		mainFrame.setLayout(new BorderLayout());
		
		songTitle.setFont(new Font("Serif", Font.PLAIN, 24));
		artistName.setFont(new Font("Serif", Font.BOLD, 26));
		songProgress.setFont(new Font("Serif", Font.PLAIN, 18));
		albumArt.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JButton pausePlayButton = new JButton("||");
		JButton nextButton = new JButton(">>");
		
		pausePlayButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				queueMan.toggle();
			}
		});
		
		nextButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				queueMan.nextSong();
			}
		});
		
		stationCB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				queueMan.stop();
				ComboItem tempItem = (ComboItem)stationCB.getSelectedItem();
				queueMan.playStation(tempItem.getValue());
			}
		});
		
		JPanel rightContent = new JPanel(new GridLayout(7, 0));
		JPanel buttonPanel = new JPanel();
		
		rightContent.setBorder(new EmptyBorder(30, 20, 20, 30));
		
		buttonPanel.add(pausePlayButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(stationCB);
		
		rightContent.add(songTitle);
		rightContent.add(artistName);
		rightContent.add(songProgress);
		rightContent.add(new JPanel());
		rightContent.add(new JPanel());
		rightContent.add(new JPanel());
		rightContent.add(buttonPanel);
		
		mainFrame.add(albumArt, BorderLayout.WEST);
		mainFrame.add(rightContent, BorderLayout.CENTER);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// We lock here
		SongGUIContentUpdate songClass = new SongGUIContentUpdate();
		Thread songPlayingThread = new Thread(songClass);
		songPlayingThread.start();
	}
	
	public void PromptGUIPassword(){
		final JFrame passwordFrame = new JFrame("Pandorica Login");
		passwordFrame.setSize(350, 200);
		passwordFrame.setLayout(new GridLayout(3, 0));
		
		JLabel emailLabel = new JLabel("Email:");
		JLabel passwordLabel = new JLabel("Password:");
		
		final JTextField emailField = new JTextField();
		final JPasswordField passwordField = new JPasswordField();
		
		JButton loginButton = new JButton("Login");
		
		loginButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				if(pandoraBackEnd.pandoraLogin(emailField.getText(), new String(passwordField.getPassword()))){
					passwordFrame.setVisible(false);
					PopulateGUIStations();
					LoadMainScreen();
					// queueMan.saveAsMP3(true);
				}
			}
		});
		
		JPanel emailPanel = new JPanel(new GridLayout(0, 2));
		JPanel passwordPanel = new JPanel(new GridLayout(0, 2));
		
		emailPanel.add(emailLabel);
		emailPanel.add(emailField);
		
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		
		passwordFrame.add(emailPanel);
		passwordFrame.add(passwordPanel);
		passwordFrame.add(loginButton);
		
		passwordFrame.setLocationRelativeTo(null);
		passwordFrame.setVisible(true);
		passwordFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void PromptTerminalPassword(){
		
		Scanner in = new Scanner(System.in);
		System.out.print("Email: ");
		String username = in.nextLine();
		Console console = System.console();
		char passwordArray[] = console.readPassword("Password: ");

		// Login with password
		pandoraBackEnd.pandoraLogin(username, new String(passwordArray));

		queueMan.saveAsMP3(true);

		queueMan.playStation(PopulateTerminalStations());

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
				queueMan.playStation(PopulateTerminalStations());
			}else if(tempIn.equals("h")){
				System.out.println("p - pause\nr - resume\nn - next\ns - stations");
			}
		}
	
	}
	
	public void PopulateGUIStations(){
		// Get Station List
		ArrayList<ArrayList<String>> stationList = pandoraBackEnd.getStationList();
		
		// Print and choose station
		for(ArrayList<String> tempAL : stationList){
			// stationCB.addItem(tempAL.get(0));
			stationCB.addItem(new ComboItem(tempAL.get(0), tempAL.get(1)));
		}
	}
	
	public String PopulateTerminalStations(){
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
	
	class SongGUIContentUpdate implements Runnable{
		public void run(){
			synchronized(threadLock){
				while(true){

					// On first start, we wait for song to start playing
					while(queueMan.getCurrentSong().getSongStatus() != PLAYING){						
						
						// Sleep for 100ms
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
					
					// Gather playing song
					currentSong = queueMan.getCurrentSong();
					
					// Set song title & artist
					songTitle.setText(currentSong.getSongName());
					artistName.setText(currentSong.getArtistName());
					
					// Set album art
					try{
						URL sourceImage = new URL(currentSong.getAlbumArtUrl());
						Image AAImage = ImageIO.read(sourceImage).getScaledInstance(300, 300,  java.awt.Image.SCALE_SMOOTH);  
						albumArt.setIcon(new ImageIcon(AAImage));
						
					}catch(Exception e){}
					
					// Loop while our cached song is the same as the one in queue
					while(currentSong.getAudioUrl().equals(queueMan.getCurrentSong().getAudioUrl())){
						
						// Set song progress
						songProgress.setText(queueMan.getCurrentSongPosition() + " / " + queueMan.getCurrentSongLength());
						
						// Sleep for 100ms
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
					
				}
			}
		}
	}
	
	class SongTerminalContentUpdate implements Runnable{
		public void run(){
			synchronized(threadLock){
				while(true){

					// On first start, we wait for song to start playing
					while(queueMan.getCurrentSong().getSongStatus() != PLAYING){						
						
						// Sleep for 100ms
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
					
					// Gather playing song
					currentSong = queueMan.getCurrentSong();
					
					
					String songName = currentSong.getSongName();
					String artistName = currentSong.getArtistName(); 
					String currentSongLength = queueMan.getCurrentSongLength();
					
					// Loop while our cached song is the same as the one in queue
					while(currentSong.getAudioUrl().equals(queueMan.getCurrentSong().getAudioUrl())){
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

		public ComboItem(String key, String value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString()
		{
			return key;
		}

		public String getKey()
		{
			return key;
		}

		public String getValue()
		{
			return value;
		}
	}
	
	public static void main(String args[]){
		if(args.length == 0){
			new PandoraGUI(true);
		}else if(args.length == 1 && args[0].equals("-nogui")){
			// Execute no gui
			new PandoraGUI(false);
		}else{
			System.out.println("Invalid Command Line Arguments.\n\t-nogui\n\t\tAppend this flag to execute the program with no gui.");
		}
	}
}