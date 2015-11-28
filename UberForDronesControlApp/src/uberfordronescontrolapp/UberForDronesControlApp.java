/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uberfordronescontrolapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Arthur
 */
public class UberForDronesControlApp extends JApplet {
    
    private static final int JFXPANEL_WIDTH_INT = 1400;
    private static final int JFXPANEL_HEIGHT_INT = 600;
    private static JFXPanel fxContainer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                }
                
                JFrame frame = new JFrame("Uber for Drones Master Control Panel");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                
                JApplet applet = new UberForDronesControlApp();
                applet.init();
                
                frame.setContentPane(applet.getContentPane());
                
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                applet.start();
            }
        });
    }
    
    @Override
    public void init() {
        fxContainer = new JFXPanel();
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);
        // create JavaFX scene
        Platform.runLater(new Runnable() {
            
            @Override
            public void run() {
                createScene();
            }
        });
    }
    
    private void createScene() {
        
        HBox dronesTable = new HBox();
        GridPane dronesTableGrid = new GridPane();
        
        Canvas canvas = new Canvas(600,600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        
        Image image1 = new Image("/theMap.png", 800, 800, true, false);
        Image mapMarker = new Image("/mapMarker.png", 800, 800, true, false);
        
        gc.drawImage(image1, 0,0);
        
        double[] drone_target_x = new double[10];
        double[] drone_target_y = new double[10];
        
        for(int i = 0 ; i < 10 ; i++)
        {
        drone_target_x[i] = Math.random() * 600;
        drone_target_y[i] = Math.random() * 400;
        }
        
        for(int i = 0 ; i < 10 ; i++)
        {
        double dronex = Math.random() * 600;
        double droney = Math.random() * 400;
        
        gc.setFill(Color.WHITE);
        gc.fillRect(dronex-4,droney+32,50,20);
        gc.setFill(Color.BLACK);
        gc.fillText("Drone "+(i+1),dronex,droney+48);
        gc.drawImage(mapMarker, dronex,droney,32,32);
        gc.setFill(Color.BLUE);
        gc.strokeLine(dronex + 16, droney + 32, drone_target_x[i], drone_target_y[i]);
        }
        
        ImageView iv3 = new ImageView();
        iv3.setImage(image1);

        dronesTable.getChildren().add(dronesTableGrid);
        dronesTable.getChildren().add(canvas);
        
        dronesTableGrid.setPrefSize(500, 500);
        
        dronesTableGrid.setHgap(20);
        dronesTableGrid.setVgap(20);
                
        //DRONE IDS
        Label DroneIDLabel = new Label();
        DroneIDLabel.setText("Drone ID");
        dronesTableGrid.add( DroneIDLabel, 0, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText(""+(row+1));
        dronesTableGrid.add( DroneIDLabel, 0, row+1);
        }
        
        //DESTINATIONS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Destination");
        dronesTableGrid.add( DroneIDLabel, 1, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText("City "+(row+1));
        dronesTableGrid.add( DroneIDLabel, 1, row+1);
        }
        
        //DRIVER IDS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Driver ID");
        dronesTableGrid.add( DroneIDLabel, 2, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Driver "+(row+1));
        dronesTableGrid.add( DroneIDLabel, 2, row+1);
        }
        
        //PACKAGE IDS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Package ID");
        dronesTableGrid.add( DroneIDLabel, 3, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Package "+(row+1));
        dronesTableGrid.add( DroneIDLabel, 3, row+1);
        }
        
        //BATTERY LIFE
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Battery Life");
        dronesTableGrid.add( DroneIDLabel, 4, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText(""+(row+1)*10+"%");
        dronesTableGrid.add( DroneIDLabel, 4, row+1);
        }
        
        //DRONE STATUS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Status");
        dronesTableGrid.add( DroneIDLabel, 5, 0);
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Flying");
        dronesTableGrid.add( DroneIDLabel, 5, row+1);
        }
        
        //DroneIDLabel.setText("Drone ID");
        
        //Label DroneIDLabel = new Label();
        
        
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        StackPane root = new StackPane();
        //root.getChildren().add(btn);
        root.getChildren().add(dronesTable);
        
        fxContainer.setScene(new Scene(root));
    }
    
}
