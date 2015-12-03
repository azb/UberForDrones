/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uberfordronescontrolapp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
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
    private fakeTime time = new fakeTime(0, 0, 0);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
            System.out.println("JDBC driver not found");
        }
       
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
    
    private void update() {
       Connection conn = null;
       Statement stmt = null;
       ResultSet rs = null;
       
       try {
          conn =
	               DriverManager.getConnection(
	            		           "jdbc:mysql://localhost:3306/test?" +
	                               "user=root&password=MYSQL"
	            		           );
          stmt = conn.createStatement();
          // Pair drones with packages
          System.out.println("Packages & Drones");
          rs = stmt.executeQuery("SELECT package.id AS packageID, drone.id AS droneID FROM package\n" +
"	INNER JOIN drone\n" +
"		ON drone.location = package.location\n" +
"	WHERE drone.packageID IS NULL AND NOT package.location = package.endLocation");
          ArrayList<String> drones = new ArrayList<String>();
          ArrayList<String> packages = new ArrayList<String>();
          while (rs.next()) {
             String packageID = rs.getString("packageID");
             String droneID = rs.getString("droneID");
             System.out.println(packageID + "\t" + droneID);
             if (packages.indexOf(packageID) == -1 && drones.indexOf(droneID) == -1)
             {
                drones.add(droneID);
                packages.add(packageID);
             }
          }
          // Iterate over the filetered drone-package pairs and insert them
          ListIterator dronesIt = drones.listIterator();
          ListIterator packageIt = packages.listIterator();
          while (dronesIt.hasNext()) {
             if (stmt.execute("UPDATE drone\n" +
               "SET\n" +
               "packageID = '" + packageIt.next() + "'\n" +
               "WHERE id = '" + dronesIt.next() + "'")) {
                  System.out.println("Update failed!");
             }
          }
          
          // Get drones and drivers that match, set to driving
          System.out.println("Drones & Drivers");
          rs = stmt.executeQuery("SELECT driver.name AS name, c.id AS id, " +
                  "driver.startTime AS time, driver.currentPackageCount AS " +
                  "currentPackageCount, driver.maxPackageCount AS " +
                  "maxPackageCount  FROM driver\n" +
"	INNER JOIN ( SELECT package.endLocation AS endLocation, drone.id as id FROM drone\n" +
"				INNER JOIN package ON package.id = drone.packageID\n" +
"				WHERE NOT drone.location = 'driving' AND NOT package.location = 'driving') AS c\n" +
"		ON c.endLocation = driver.endLocation\n" +
"WHERE driver.currentPackageCount < driver.maxPackageCount");
          ArrayList<HashMap<String, Object>> dronesNdrivers = new ArrayList<>();
          HashMap<String, Boolean> droneCount = new HashMap<>();
          while (rs.next()) {
             String name = rs.getString("name");
             String id = rs.getString("id");
             String startTime = rs.getString("time");
             int currentPackageCount = rs.getInt("currentPackageCount");
             int maxPackageCount = rs.getInt("maxPackagecount");
             System.out.println(name + "\t" + id + "\t" + startTime + "\t" +
                     currentPackageCount + "\t" + maxPackageCount);
             System.out.println("------------------");
             if (time.earlierOrEqual(new fakeTime(startTime)) && maxPackageCount
                     > currentPackageCount) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("name", name);
                map.put("id", id);
                map.put("startTime", startTime);
                map.put("currentPackageCount", currentPackageCount);
                map.put("maxPackageCount", maxPackageCount);
                dronesNdrivers.add(map);
                droneCount.put(id, Boolean.FALSE);
             }             
          }
          ListIterator dNdIter = dronesNdrivers.listIterator();
          // Go through the possible combination of drones and drivers
          while (dNdIter.hasNext()) {
             HashMap<String, Object> map = (HashMap<String, Object>) dNdIter.next();
             if ((int)map.get("currentPackageCount") < (int)map.get("maxPackageCount")) {
                if (!droneCount.get(map.get("id"))) {
                   if (stmt.execute("UPDATE drone\n" +
               "SET\n" +
               "location = 'driving', currentDriver = '" + map.get("name") + "'\n" +
               "WHERE id = '" + map.get("id") + "'")) {
                        System.out.println("Update failed!");
                   }
                   map.replace("currentPackageCount", ((int)map.get("currentPackageCount") + 1));
                   droneCount.replace((String)map.get("id"), true);
               }
             }
          }
          // Update the location of all packages 'moved'
          if (stmt.execute("UPDATE package\n" +
            "SET\n" +
            "location = 'driving'\n" +
            "WHERE id in (SELECT packageID FROM drone WHERE location = 'driving')")) {
             System.out.print("Update failed");
          }
          dNdIter = dronesNdrivers.listIterator(0);
          while (dNdIter.hasNext()) {
             HashMap<String, Object> map = (HashMap<String, Object>) dNdIter.next();
             if (stmt.execute("UPDATE driver\n" +
               "SET\n" +
               "currentPackageCount = " + map.get("currentPackageCount") + "\n" +
               "WHERE name = '"+ map.get("name") + "'")) {
                  System.out.println("Update failed");
             }
          }
          
          // Drop off drones/packages if stopped
          
          
       } catch (SQLException ex) {
          // handle any errors
          System.out.println("SQLException: " + ex.getMessage());
          System.out.println("SQLState: " + ex.getSQLState());
          System.out.println("VendorError: " + ex.getErrorCode());
       }
       time.increment(0, 30, 0);
       System.out.println(time);
    }
    
    private boolean checkStringIsNull(String s) {
       try {
          if (s.length() > 0) {
            return false;
          }
       } catch (NullPointerException e) {
          return true;
       }
       return false;
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
        
        
        
        Connection conn = null;
	Statement stmt = null;
        ResultSet rs = null;
        try {
	            conn =
	               DriverManager.getConnection(
	            		           "jdbc:mysql://localhost:3306/test?" +
	                               "user=root&password=MYSQL"
	            		           );
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery("SELECT * FROM drone");
                    int i = 1;
                    while (rs.next()) {
                        String id = rs.getString("id");
	                String location = rs.getString("location");
	                String currentDriver = rs.getString("currentDriver");
                        String packageID = rs.getString("packageID");
                        DroneIDLabel = new Label();
                        DroneIDLabel.setText(id);
                        dronesTableGrid.add(DroneIDLabel, 0, i);
                        DroneIDLabel = new Label();
                        DroneIDLabel.setText(location);
                        dronesTableGrid.add(DroneIDLabel, 1, i);
                        DroneIDLabel = new Label();
                        DroneIDLabel.setText(currentDriver);
                        dronesTableGrid.add(DroneIDLabel, 2, i);
                        DroneIDLabel = new Label();
                        DroneIDLabel.setText(packageID);
                        dronesTableGrid.add(DroneIDLabel, 3, i);
                        DroneIDLabel = new Label();
                        if (checkStringIsNull(currentDriver)) {
                           DroneIDLabel.setText("Flying");
                        } else {
                           DroneIDLabel.setText("Hitching");
                        }
                        dronesTableGrid.add(DroneIDLabel, 5, i);
                        i++;
	             }
        } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
        }
        //DESTINATIONS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Destination");
        dronesTableGrid.add( DroneIDLabel, 1, 0);
        
        //DRIVER IDS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Driver ID");
        dronesTableGrid.add( DroneIDLabel, 2, 0);
        
        //PACKAGE IDS
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Package ID");
        dronesTableGrid.add( DroneIDLabel, 3, 0);
        
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
        
        Button btn = new Button();
        btn.setText("Update");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent event) {
                update();
            }
        });
        StackPane root = new StackPane();
        //root.getChildren().add(btn);
        root.getChildren().add(dronesTable);
        root.getChildren().add(btn);
        
        fxContainer.setScene(new Scene(root));
    }
    
}
