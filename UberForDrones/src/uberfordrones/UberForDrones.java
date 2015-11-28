package uberfordrones;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.canvas.*;
import javafx.scene.text.TextAlignment;

public class UberForDrones extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 300;
    private static final int JFXPANEL_HEIGHT_INT = 250;
    private static JFXPanel fxContainer;

    private String buttonReturn1 = "";
    private String buttonReturn2 = "";
    private String buttonReturn3 = "";

    private static int minutes = 0;
    private static int seconds = 0;

    private String ReturnCode = "";

    public static void main(String[] args) {

        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
            System.out.println("JDBC driver not found");
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn
                    = DriverManager.getConnection(
                            "jdbc:mysql://localhost:3306/test?"
                            + "user=root&password="
                    );

            stmt = conn.createStatement();
            stmt.executeUpdate(
                    "CREATE TABLE States ("
                    + "name VARCHAR(32),"
                    + "capital VARCHAR(32),"
                    + "population INTEGER"
                    + ")"
            );
            stmt.executeUpdate(
                    "INSERT INTO States "
                    + "VALUES ('California', 'Sacramento', 39000000),"
                    + "('Oregon', 'Salem', 4000000),"
                    + "('Washington', 'Olympia', 7000000)"
            );
            rs = stmt.executeQuery("select * from states");
            while (rs.next()) {
                String stateName = rs.getString("name");
                int population = rs.getInt("population");
                String capital = rs.getString("capital");
                System.out.println(stateName + "\t" + capital + "\t" + population);
            }

        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        System.out.println("completed connection");

	    try {
	            conn =
	               DriverManager.getConnection(
	            		           "jdbc:mysql://localhost:3306/test?" +
	                               "user=root&password="
	            		           );

	    	    stmt = conn.createStatement();
	    	    stmt.executeUpdate(
	    	    		"CREATE TABLE DRIVER (" +
	    	            "name VARCHAR(32)," +
	    	    	    "startLocation VARCHAR(32)," +
	    	    	    "endLocation VARCHAR(32)," +
                            "startTime TIME," +
                            "balance FLOAT," +
                            "currentPackageCount INTEGER," +
                            "maxPackageCount INTEGER" +
	    	            ")"
	    	           );
         stmt.executeUpdate(
	    	    		"CREATE TABLE SENDER (" +
	    	            "name VARCHAR(32)," +
	    	    	    "location VARCHAR(32)" +
	    	            ")"
	    	           );
         stmt.executeUpdate(
	    	    		"CREATE TABLE RECIEVER (" +
	    	            "name VARCHAR(32)," +
	    	    	    "location VARCHAR(32)" +
	    	            ")"
	    	           );
         stmt.executeUpdate(
	    	    		"CREATE TABLE DRONE (" +
	    	            "id VARCHAR(32)," +
	    	    	    "startLocation VARCHAR(32)," +
                            "endLocation VARCHAR(32)," +
                            "currentDriver VARCHAR(32)," +
                            "reciever VARCHAR(32)" +
	    	            ")"
	    	           );
	 stmt.executeUpdate(
	    	    		"INSERT INTO DRIVER " +
	    	            "VALUES ('Billy', 'Sacramento', 'Los Angeles'," +
                              " '00:00:00', 0.0, 0, 2)," +
	    	    	    "('Bob', 'Fremont', 'Los Angeles'," +
                              " '00:30:00', 0.0, 0, 1)," +
                            "('Joe', 'Sacramento', 'Fremont'," +
                              " '00:45:00', 0.0, 0, 3)");
	 rs = stmt.executeQuery("select * from DRIVER");
	 while (rs.next()) {
	         String driverName = rs.getString("name");
                 String startLoc = rs.getString("startLocation");
                 String endLoc = rs.getString("endLocation");
                 java.sql.Time startTime = rs.getTime("startTime");
                 float balance = rs.getFloat("balance");
                 int curPkgCount = rs.getInt("currentPackageCount");
                 int maxPkgCount = rs.getInt("maxPackageCount");
	         System.out.println(driverName + "\t" + startLoc + "\t" +
                         endLoc + "\t" + startTime.toString() + "\t" +
                         balance + "\t" + curPkgCount + "\t" + maxPkgCount);
	 }
	     } catch (SQLException ex) {
	            // handle any errors
	            System.out.println("SQLException: " + ex.getMessage());
	            System.out.println("SQLState: " + ex.getSQLState());
	            System.out.println("VendorError: " + ex.getErrorCode());
	            }		
	    System.out.println("completed connection");
	    
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                }

                JFrame frame = new JFrame("Uber for Delivery Drones");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JApplet applet = new UberForDrones();
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

        Label theLabel = new Label();
        Label timeLabel = new Label();

        HBox buttonHBox = new HBox();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        theLabel.setText("Drone 1257: Can I get a ride for $3?");
        theLabel.setAlignment(Pos.CENTER);

        timeLabel.setText("" + minutes + ":" + seconds);
        timeLabel.setAlignment(Pos.CENTER);
        timeLabel.setFont(Font.font("Verdana", 20));

        StackPane overlord = new StackPane();
        VBox vpane = new VBox();
        vpane.setAlignment(Pos.CENTER);
        vpane.setSpacing(5);
        buttonHBox.setSpacing(6);
        buttonHBox.getChildren().add(button1);

        buttonHBox.getChildren().add(button2);

        Canvas canvas = new Canvas(200, 60);
        /*
         Timeline timeline = new Timeline(
         new KeyFrame(Duration.seconds(1))
                
         );
        
         timeline.setAutoReverse(true);
         timeline.setCycleCount(Timeline.INDEFINITE);
         */
        /*
         ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);
         executor.schedule(new Runnable() {
         @Override
         public void run() {
         // Do something here.
         seconds--;
         }
         }, 1, TimeUnit.SECONDS);
         */

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.millis(2500),
                ae -> subractSeconds()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, 200, 60);
                gc.setFill(Color.FORESTGREEN);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setFont(Font.font("Verdana", 20));
                gc.fillText(minutes + ":" + seconds, 100, 30);

                //gc.fillOval(x.doubleValue(),y.doubleValue(),D,D);
            }
        };

        timer.start();
        //timeline.play();

        //GraphicsContext gc = canvas.getGraphicsContext2D();
        //gc.setFill(Color.BLUE);
        //gc.fillRect(0,0,250,250);
        Image image1 = new Image("/drone.png", 100, 100, true, false);

        ImageView iv3 = new ImageView();
        iv3.setImage(image1);

        vpane.getChildren().add(iv3);
        vpane.getChildren().add(theLabel);

        vpane.getChildren().add(buttonHBox);

        
        overlord.getChildren().add(vpane);

        buttonHBox.setAlignment(Pos.CENTER);

        fxContainer.setScene(new Scene(overlord));

        /*
         class SayHello extends TimerTask {
         public void run() {
         System.out.println("Hello World!"); 
         if (seconds > 0)
         {
         seconds--;
         //timeLabel.setText("" + minutes + ":"+ seconds);
         }
         }
         }
         // And From your main() method or any other method
         Timer timer = new Timer();
         timer.schedule(new SayHello(), 0, 1000);
         */
        button1.setText("No");
        button1.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("No");
                buttonHBox.getChildren().remove(button1);
                buttonHBox.getChildren().remove(button2);
                theLabel.setText("Your current balance is $0");
            }
        });

        button2.setText("Yes");
        button2.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Yes");
                buttonHBox.getChildren().remove(button1);
                buttonHBox.getChildren().remove(button2);
                buttonHBox.getChildren().add(button3);
                theLabel.setText("Transaction accepted.\n$3 has been added to your balance.");
            }
        });

        button3.setText("Done");
        button3.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Yes");
                buttonHBox.getChildren().remove(button3);
                theLabel.setText("Drone Detaches in");
                //vpane.getChildren().add(timeLabel);
                vpane.getChildren().add(canvas);

                minutes = 5;
                seconds = 32;
                timeLabel.setText("" + minutes + ":" + seconds);
            }

        });

        System.out.println(buttonReturn1);
        System.out.println(buttonReturn2);
        System.out.println(buttonReturn3);
    }

    public void subractSeconds() {
        if (seconds > 0) {
            seconds--;
        }
    }
}
