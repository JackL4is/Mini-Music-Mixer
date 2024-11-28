/////////////////////////////////////////////////////////////////////////////////////////
//                                                                                     //
//  Mini-Music Machine                                                                 //
//                                                                                     //
//  Created by Jack Lais 2024                                                          //
//                                                                                     //
/////////////////////////////////////////////////////////////////////////////////////////
// Credits - Samples from Keegan Rany (Magical Trials) & Toby Fox (Deltarune)          //
/////////////////////////////////////////////////////////////////////////////////////////


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.sound.sampled.*;

public class MusicPlayer {
  private static ArrayList<String> availableSongs = new ArrayList<>();
  private static Clip clip; // Make the clip global to control it across methods
  private static int lastSelection = -1; // To track the last played song
  private static boolean isPaused = false;
  private static boolean isLooped = false;

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    File directory = new File("src/sample"); //input folder reference
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isFile() && file.getName().endsWith(".wav")) {
          availableSongs.add(file.getName());
        }
      }
    }

    if (availableSongs.isEmpty()) {
      System.out.println("No .wav songs found in the directory.");
      return;
    }

    playRandomSong();

    try {
      String response = "";

      while (!response.equals("Q")) {
        System.out.println("P = Play, S = Skip, O = Stop, L = Pause & Loop, R = Reset, Q = Quit");
        System.out.print("Enter your choice: ");

        response = scanner.next();
        response = response.toUpperCase();

        if (isLooped) {
          clip.loop(Clip.LOOP_CONTINUOUSLY);
        }

        switch (response) {
          case "P":
            if (clip != null && !clip.isActive()) {
              isPaused = false;
              clip.start();
              System.out.println("Resumed playing...");
            }
            break;
          case "S":
            clip.setMicrosecondPosition(clip.getMicrosecondLength());
            break;
          case "L":
            isLooped = !isLooped;
            System.out.println("Looped: " + isLooped);
          case "O":
            if (clip != null && clip.isRunning()) {
              isPaused = true;
              clip.stop();
              System.out.println("Audio stopped.");
            }
            break;
          case "R":
            if (clip != null) {
              clip.setMicrosecondPosition(0);
              clip.start();
              System.out.println("Audio reset to the beginning.");
            }
            break;
          case "Q":
            if (clip != null) {
              clip.close();
            }
            System.out.println("Quitting program. Bye!");
            break;
          default:
            System.out.println("Not a valid response.");
        }
      }
    } catch (Exception e) {
      System.out.println("An error occurred: " + e.getMessage());
    } finally {
      scanner.close();
    }
  }

  private static void playRandomSong() {
    try {
      if (clip != null && clip.isOpen()) {
        clip.stop();
        clip.close();
      }

      String fileName = fileFinder();
      File audio = new File("src/music/" + fileName);

      if (!audio.exists()) {
        System.out.println("File not found: " + fileName);
        return;
      }

      AudioInputStream audioStream = AudioSystem.getAudioInputStream(audio);
      clip = AudioSystem.getClip();
      clip.open(audioStream);

      clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP && !isPaused) {
          playRandomSong();
        }
      });

      clip.start();
      System.out.println("Playing: " + fileName);
    } catch (UnsupportedAudioFileException e) {
      System.out.println("Audio file is unsupported: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("An I/O error occurred: " + e.getMessage());
    } catch (LineUnavailableException e) {
      System.out.println("Audio unavailable: " + e.getMessage());
    }
  }

  private static String fileFinder() {
    int rand = lastSelection;
    while (rand == lastSelection) {
      rand = (int) (Math.random() * availableSongs.size());
    }
    lastSelection = rand;
    return availableSongs.get(rand);
  }
}
