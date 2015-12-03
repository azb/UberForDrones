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
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Arthur
 */
public class UberForDronesControlApp extends JApplet {
    
    private static final int JFXPANEL_WIDTH_INT = 1300;
    private static final int JFXPANEL_HEIGHT_INT = 630;
    private static JFXPanel fxContainer;
    private fakeTime time = new fakeTime(0, 0, 0);

    private static double[] dronex = new double[100]; 
    private static double[] droney = new double[100];
    
    private static double[] drone_target_x = new double[100];
    private static double[] drone_target_y = new double[100];
    
    private static double[] drone_start_x = new double[100];
    private static double[] drone_start_y = new double[100];
    
    private static String[] drone_status = new String[100];
    private static double[] drone_battery_level = new double[100];
    
    private static ArrayList<Label> DroneStatusLabels = new ArrayList<Label>();
    private static ArrayList<Label> DroneBatteryLabels = new ArrayList<Label>();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
            
    for(int i = 0 ; i < 10 ; i++)
        {
        dronex[i] = Math.random() * 600;
        droney[i] = Math.random() * 400;
        drone_start_x[i] = Math.random() * 600;
        drone_start_y[i] = Math.random() * 400;
        drone_target_x[i] = Math.random() * 600;
        drone_target_y[i] = Math.random() * 400;
        drone_status[i] = "Flying";
        drone_battery_level[i] = 100 - Math.random() * 25;
        }
    
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
	                               "user=root&password="
	            		           );
          stmt = conn.createStatement();
          // Drop off drones/packages and update drivers
          System.out.println("Dropoffs");
          rs = stmt.executeQuery("SELECT driver.name AS name, c.droneID AS " +
                  "droneID, c.packageID, driver.currentPackageCount AS " +
                  "currentPackageCount, driver.balance AS balance, " +
                  "driver.endLocation AS endLocation FROM driver\n" +
"	INNER JOIN ( SELECT package.id AS packageID, drone.id as droneID, " +
                  "drone.currentDriver AS driver FROM drone\n" +
"				INNER JOIN package ON package.id = drone.packageID) AS c\n" +
"		ON c.driver = driver.name");
          ArrayList<HashMap> parking = new ArrayList<>();
          while (rs.next()) {
             String name = rs.getString("name");
             String droneID = rs.getString("droneID");
             String packageID = rs.getString("packageID");
             int packageCount = rs.getInt("currentPackageCount");
             float balance = rs.getFloat("balance");
             String endLocation = rs.getString("endLocation");
             System.out.println(name + "\t" + droneID + "\t" + packageID + "\t" +
                     packageCount + "\t" + balance + "\t" + endLocation);
             HashMap<String, Object> map = new HashMap<>();
             map.put("name", name);
             map.put("droneID", droneID);
             map.put("packageID", packageID);
             map.put("packageCount", packageCount);
             map.put("balance", balance);
             map.put("endLocation", endLocation);
             parking.add(map);
          }
          ListIterator parkIter = parking.listIterator();
          while (parkIter.hasNext()) {
             HashMap<String, Object> map = (HashMap<String, Object>) parkIter.next();
             // Update driver
             if (stmt.execute("UPDATE driver\n" +
               "SET\n" +
               "balance = " + ((float)map.get("balance") + 3) + ", currentPackageCount = " + 
                     ((int)map.get("packageCount") - 1) + "\n" + "WHERE name = '" + 
                     map.get("name") + "'")) {
                System.out.println("Update failed");
             }
             // Update drone
             if (stmt.execute("UPDATE drone\n" +
               "SET\n" +
               "location = '" + map.get("endLocation") + "', currentDriver = NULL, packageID = NULL\n" +
               "WHERE id = '" + map.get("droneID") + "'")) {
                System.out.println("Update failed");
             }
             // Update package
             if (stmt.execute("UPDATE package\n" +
               "SET\n" +
               "location = '" + map.get("endLocation") + "'\n" +
               "WHERE id = '" + map.get("packageID") + "'")) {
                System.out.println("Update failed");
             }
          }
          
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
        TabPane tabPane = new TabPane();
        Tab dronesTab = new Tab();
        dronesTab.setText("Drones");
        dronesTab.setClosable(false);
        
        Tab usersTab = new Tab();
        usersTab.setText("Users");
        usersTab.setClosable(false);
        
        Tab packagesTab = new Tab();
        packagesTab.setText("Packages");
        packagesTab.setClosable(false);
        
        //dronesTab.setContent(new Rectangle(200,200, Color.LIGHTSTEELBLUE));
        
        tabPane.getTabs().addAll(dronesTab,usersTab,packagesTab);

        VBox overlord = new VBox();
        HBox dronesTable = new HBox();
        GridPane dronesTableGrid = new GridPane();
        dronesTableGrid.setMinWidth(670);
        overlord.getChildren().add(dronesTable);
        
        Canvas canvas = new Canvas(600,600);
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        Image image1 = new Image("/theMap.png", 800, 800, true, false);
        Image mapMarker = new Image("/mapMarker.png", 800, 800, true, false);
        
        gc.drawImage(image1, 0,0);
        
        for(int i = 0 ; i < 10 ; i++)
        {
        //double dronex = Math.random() * 600;
        //double droney = Math.random() * 400;
        
        gc.setFill(Color.WHITE);
        gc.fillRect(dronex[i]-4-16,droney[i]+32-32,50,20);
        gc.setFill(Color.BLACK);
        gc.fillText("Drone "+(i+1),dronex[i]-16,droney[i]+48-32);
        gc.drawImage(mapMarker, dronex[i] - 16,droney[i] - 32,32,32);
        gc.setFill(Color.BLUE);
        gc.strokeLine(dronex[i], droney[i], drone_target_x[i], drone_target_y[i]);
        
        if (pointDistance(dronex[i],droney[i],drone_target_x[i],drone_target_y[i]) > 2)
            {
            double dir = pointDirection(dronex[i],droney[i],drone_target_x[i],drone_target_y[i]);
        
            dronex[i] += lengthdir_x(.5,dir);
            droney[i] += lengthdir_y(.5,dir);
            
            if (pointDistance(dronex[i],droney[i],drone_target_x[i],drone_target_y[i]) < 15)
                {
                if (drone_target_x[i] != drone_start_x[i] && drone_target_y[i] != drone_start_y[i])
                drone_status[i] = "Dropping off package";
                }
            else
                {
                if (drone_target_x[i] == drone_start_x[i] && drone_target_y[i] == drone_start_y[i])
                    drone_status[i] = "Flying home";
                else
                    drone_status[i] = "Flying to destination";
                }
            drone_battery_level[i]-=.02;
            }
        else
            {
            if (drone_target_x[i] == drone_start_x[i] && drone_target_y[i] == drone_start_y[i])
                {
                    //If arrived at package processing facility
                    
                    if (drone_battery_level[i] < 100)
                        {
                        drone_status[i] = "Swapping battery";
                        drone_battery_level[i]+=.04;
                        }
                    else
                        {
                        drone_status[i] = "Waiting for ride";
                        drone_target_x[i] = Math.random() * 600;
                        drone_target_y[i] = Math.random() * 400;
                        }
                    
                }
            else
                {
                    //If arrived at package drop off destination
                    
                    drone_target_x[i] = drone_start_x[i];
                    drone_target_y[i] = drone_start_y[i];
                    drone_status[i] = "Dropping off package";
                }
            
            }
        
        DroneStatusLabels.get(i+1).setText(drone_status[i]);
        
        if (pointDistance(dronex[i],droney[i],drone_start_x[i],drone_start_y[i]) > 4)
        DroneBatteryLabels.get(i+1).setText("" + (int)drone_battery_level[i] + "%");
        else
        DroneBatteryLabels.get(i+1).setText("N/A");
        }
        
        ImageView iv3 = new ImageView();
        iv3.setImage(image1);
        }
        };
        
        timer.start();
        
        dronesTable.getChildren().add(dronesTableGrid);
        dronesTable.getChildren().add(canvas);
        
        dronesTableGrid.setPrefSize(500, 500);
        
        dronesTableGrid.setHgap(20);
        dronesTableGrid.setVgap(20);
                
        //DRONE IDS
        Button droneIDButton = new Button();
        droneIDButton.setText("Drone ID [v]");
        Label DroneIDLabel = new Label();
        DroneIDLabel.setText("Drone ID");
        dronesTableGrid.add( droneIDButton, 0, 0);
        
        Connection conn = null;
	Statement stmt = null;
        ResultSet rs = null;
        try {
	            conn =
	               DriverManager.getConnection(
	            		           "jdbc:mysql://localhost:3306/test?" +
	                               "user=root&password="
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
        Button destinationButton = new Button();
        destinationButton.setText("Destination [v]");
        Label DestinationLabel = new Label();
        DestinationLabel.setText("Destination");
        dronesTableGrid.add( destinationButton, 1, 0);
        
        //DRIVER IDS
        Button driverIDButton = new Button();
        driverIDButton.setText("Driver ID [v]");
        Label DriverIDLabel = new Label();
        DriverIDLabel.setText("Driver ID");
        dronesTableGrid.add( driverIDButton, 2, 0);
        
        //PACKAGE IDS
        Button packageIDButton = new Button();
        packageIDButton.setText("Package ID [v]");
        DroneIDLabel = new Label();
        DroneIDLabel.setText("Package ID");
        dronesTableGrid.add(packageIDButton , 3, 0); //DroneIDLabel
        
        //BATTERY LIFE
        Button sortButton = new Button();
        sortButton.setText("Battery [v]");
        Label DroneBatteryLabel = new Label();
        DroneBatteryLabel.setText("Battery Life");
        DroneBatteryLabels.add(DroneBatteryLabel);        
        dronesTableGrid.add( sortButton, 4, 0); //DroneBatteryLabels.get(0)
        
        /*
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneIDLabel = new Label();
        DroneIDLabel.setText(""+(row+1)*10+"%");
        dronesTableGrid.add( DroneIDLabel, 4, row+1);
        }
        */
        
        //DRONE STATUS
        Button statusButton = new Button();
        statusButton.setText("Status [v]");
        Label DroneStatusLabel = new Label();
        DroneStatusLabel.setText("Status");
        DroneStatusLabels.add(DroneStatusLabel);
        //dronesTableGrid.add( DroneStatusLabels.get(0), 5, 0);
        dronesTableGrid.add( statusButton, 5, 0); //DroneBatteryLabels.get(0)
        
        for(int row = 0 ; row < 10 ; row++)
        {
        DroneStatusLabel = new Label();
        DroneStatusLabel.setText(drone_status[row]);
        DroneStatusLabels.add(DroneStatusLabel);
        DroneBatteryLabel = new Label();
        DroneBatteryLabel.setText(""+(int)drone_battery_level[row]+"%");
        DroneBatteryLabels.add(DroneBatteryLabel);
        }
        
        for(int row = 0 ; row < 10 ; row++)
        {
        dronesTableGrid.add( DroneStatusLabels.get(row+1), 5, row+1);
        dronesTableGrid.add( DroneBatteryLabels.get(row+1), 4, row+1);
        }
        
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
        overlord.getChildren().add(btn);
        
        dronesTab.setContent(overlord);
        
        root.getChildren().add(tabPane);
        
        
        fxContainer.setScene(new Scene(root));
    }
    public double lengthdir_x(double speed, double dir){
		return Math.cos(dir)*speed;
	}
    public double lengthdir_y(double speed, double dir){
		return -Math.sin(dir)*speed;
	}
    public double pointDirection(double x1, double y1, double x2, double y2){
		double xchange = (x2-x1);
		double ychange = (y2-y1);
		double dir;
		if (xchange == 0){
			if (ychange >= 0)
				dir = -Math.PI/2;
			else
				dir = Math.PI/2;
		}
		else{
			dir = Math.atan2(xchange,ychange)-Math.PI/2;
		}
		return dir;
	}
    public double pointDistance(double x1, double y1, double x2, double y2){
		double xchange = (x2-x1);
		double ychange = (y2-y1);
		double output = Math.sqrt((xchange*xchange)+(ychange*ychange));
		return output;
	}
}
