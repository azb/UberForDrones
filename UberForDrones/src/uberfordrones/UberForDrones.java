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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UberForDrones extends JApplet {
    
    private static final int JFXPANEL_WIDTH_INT = 300;
    private static final int JFXPANEL_HEIGHT_INT = 250;
    private static JFXPanel fxContainer;
    
    private String buttonReturn1 = "";
    private String buttonReturn2 = "";
    private String buttonReturn3 = "";
    
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
        HBox buttonHBox = new HBox();

        Button button1 = new Button();
        Button button2 = new Button();
        Button button3 = new Button();

        theLabel.setText("Drone 1257: Can I get a ride for $3?");
        theLabel.setAlignment(Pos.CENTER);
        StackPane overlord = new StackPane();
        VBox vpane = new VBox();
        vpane.setAlignment(Pos.CENTER);
        vpane.setSpacing(5);
        buttonHBox.setSpacing(6);
        buttonHBox.getChildren().add(button1);
        
        buttonHBox.getChildren().add(button2);
        
        Image image1 = new Image("/drone.png", 100, 100, true, false);
        
        ImageView iv3 = new ImageView();
         iv3.setImage(image1);
         
        vpane.getChildren().add(iv3);
        vpane.getChildren().add(theLabel);
        vpane.getChildren().add(buttonHBox);
        
        overlord.getChildren().add(vpane);
        
        buttonHBox.setAlignment(Pos.CENTER);
        
        fxContainer.setScene(new Scene(overlord));
        
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
                theLabel.setText("Transaction accepted.\nYour current balance is $171\nYou have earned $18"
                        + ""
                        + " this week.");
            }
        });
        System.out.println(buttonReturn1);
        System.out.println(buttonReturn2);
        System.out.println(buttonReturn3);
    }
    
    /*
    public void setScreen(String labelText, String buttonText1, String returnCode1, String buttonText2, String returnCode2, String buttonText3, String returnCode3)
    {
    theLabel.setText(labelText);
    button1.setText(buttonText1);
    
    buttonReturn1 = returnCode1;
    buttonReturn2 = returnCode2;
    buttonReturn3 = returnCode3;
    
    if (buttonText2.length() > 0)
        {
        button2.setText(buttonText2);
        
        }
    else
        {
        buttonHBox.getChildren().remove(button2);
        }
    
    if (buttonText3.length() > 0)
        {
        button3.setText(buttonText3);
        }
    else
        {
        buttonHBox.getChildren().remove(button3);
        }
    }
    */
}
