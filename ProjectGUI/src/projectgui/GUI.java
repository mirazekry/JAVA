/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectgui;

/**
 *
 * @author Electronica CARE
 */
import CommPackage.CommProtocol;
import CommPackage.SerialConnection;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.skins.DashboardSkin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GUI extends Application implements Runnable {

    Stage window;
    Scene scene2;

    int flagConnect = 0; // conn
    char dir;      //dirction 
    private int speedG = 0; // speed
    double speed = 0;
    char start;
    private Gauge gauge;

    //Thread th;
    Thread thRotate;
    ImageView imageView;
    int angle = 0;

    DropShadow shadow = new DropShadow();
    

    public static void main(String[] args) {
        
        // create an object of this class and call its start()
        new Thread(new SerialConnection()).start();
        
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        window = primaryStage;
        //----------------------------------------------------Guauge---------------------------------------
        gauge = new Gauge();
        gauge.setSkin(new DashboardSkin(gauge));  //ModernSkin : you guys can change the skin
        gauge.setTitle("Speed");  //title
        gauge.setUnit("RPM");  //unit
        gauge.setUnitColor(Color.WHITE);
        gauge.setDecimals(0);
        gauge.setValue(0.00); //deafult position of needle on gauage
        gauge.setAnimated(true);
        gauge.setValueColor(Color.WHITE);
        gauge.setTitleColor(Color.WHITE);
        gauge.setBarColor(Color.rgb(0, 214, 215));
        gauge.setBarBackgroundColor(Color.WHITE);
        gauge.setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);

        //--------------------------------------------------------scene1-------------------------------------
        //Grid
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        //Label
        Label welcomeL = new Label("Controlling Motor Application");
        welcomeL.setFont(new Font("Arial", 30));
        welcomeL.setTextFill(Color.WHITE);
        GridPane.setConstraints(welcomeL, 28, 40);

        //Buuton
        Button buttonConnect = new Button("Connect");
        Button buttonExit = new Button("Exit");
        buttonExit.setMaxWidth(100);

        FlowPane pane = new FlowPane(Orientation.HORIZONTAL);
        pane.getChildren().addAll(new Label("                        "), buttonConnect, new Label("      "), buttonExit);

        GridPane.setConstraints(pane, 28, 45);

        //Button shadow
        buttonConnect.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        buttonConnect.setEffect(shadow);
                    }
                });

        buttonConnect.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent e) {
                        buttonConnect.setEffect(null);
                    }
                });

        //Button Connect Action
        buttonConnect.setOnAction(e -> {
            if (CommProtocol.isMotorAlive) {
                
                // pass to comm layer
                ////////////////////////////////////////////////////////////////////////
                CommProtocol.pwmAndDir = 0;
                thRotate.suspend();
                window.setScene(scene2);
            }
            else {
                ConfirmBox2 b = new ConfirmBox2();
                boolean result = b.display("Alert", "  You are disconnect..\n  Retry connection or close program?\n");
                if (result == false) {
                    window.close();
                    Platform.exit();
                    System.exit(0);
                }
            }
        });

        //Button Exit Action
        buttonExit.setOnAction(e -> {
            // pass to comm layer
            ////////////////////////////////////////////////////////////////////////
            CommProtocol.pwmAndDir = 0;
            try {
                Thread.sleep(1500);
            } catch (Exception ex) {
                
            }
                
            window.close();
            Platform.exit();
            System.exit(0);
        }
        );

        grid.getChildren().addAll(welcomeL, pane);
        grid.setId("pane");

        Scene scene = new Scene(grid, 950, 650);

        //---------------------------------------------------scene2------------------------------------------
        //------------------------------------------text field-----------------------------------
        
        TextField dirTxt = new TextField("Direction");

        dirTxt.setEditable(false);

        //-----------------------------------slider-------------------------------------
        // create label 
        Label label = new Label("Speed                            ");

        // set the color of the tesxt 
        label.setTextFill(Color.WHITE);

        label.setFont(new Font("Arial", 30));

        // create slider 
        Slider slider = new Slider();

        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(0);
        slider.setMaxWidth(250);

        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        slider.setBlockIncrement(10);

        // Adding Listener to value property. 
        slider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                        speed = (double) newValue;
                        speedG = (int) speed;
                        gauge.setAnimated(true);
                        gauge.setValue(speedG);

                        System.out.println("Speed = " + speedG);
                        
                        // pass to comm layer
                        ////////////////////////////////////////////////////////////////////////
                        CommProtocol.pwmAndDir = (byte)(((byte)dir << 7) | (byte)((speedG * 127) / 100));
                        
                        if (speedG > 0) {
                            thRotate.resume();
                        }
                        else {
                            thRotate.suspend();
                        }
                    }
                }
        );

        VBox root1 = new VBox();
        root1.setPadding(new Insets(10));
        root1.setSpacing(10);
        root1.getChildren().addAll(label, slider);
        //-------------------------------------------------------------------CheckBox----------------------------------
        Label dirLabel = new Label("Direction                       ");
        dirLabel.setTextFill(Color.WHITE);
        dirLabel.setFont(new Font("Arial", 30));

        CheckBox box1 = new CheckBox("Clockwise");
        box1.fontProperty().set(new Font("Arial", 20));
        box1.setTextFill(Color.WHITE);

        CheckBox box2 = new CheckBox("Anticlockwise");
        box2.setSelected(true);
        dir = 0;         //anticlockwise
        box2.fontProperty().set(new Font("Arial", 20));
        dirTxt.setText("Anticlockwise");
        box2.setTextFill(Color.WHITE);

        // callback for clockwise direction
        box1.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                dir = 1;                             //clockwise
                
                // pass to comm layer
                ////////////////////////////////////////////////////////////////////////
                CommProtocol.pwmAndDir = (byte)(((byte)dir << 7) | (byte)((speedG * 127) / 100));
                
                dirTxt.setText("Clockwise");
                if (box2.isSelected()) {
                    box2.setSelected(false);
                }
            }
        });

        // callback for anticlockwise direction
        box2.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                dir = 0;                             //anticlockwise
                
                // pass to comm layer
                ////////////////////////////////////////////////////////////////////////
                CommProtocol.pwmAndDir = (byte)(((byte)dir << 7) | (byte)((speedG * 127) / 100));
                
                dirTxt.setText("Anticlockwise");
                if (box1.isSelected()) {
                    box1.setSelected(false);
                }
            }
        });

        
        HBox checkBox = new HBox();
        checkBox.setPadding(new Insets(10));
        checkBox.setSpacing(10);
        checkBox.getChildren().addAll(box1, box2);

        VBox root2 = new VBox();
        root2.setPadding(new Insets(10));
        root2.setSpacing(10);
        root2.getChildren().addAll(dirLabel, checkBox);

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(20);
        root.getChildren().addAll(root1, root2);

        GridPane pane2 = new GridPane();
        pane2.setPadding(new Insets(10, 10, 10, 10));
        pane2.setVgap(90);
        pane2.setHgap(250);

        pane2.setId("pane");

        //-------------------------------------Rotating Image-------------------------------------
        FileInputStream input = new FileInputStream(new File("src/projectgui/2.png"));
        Image image = new Image(input);
        imageView = new ImageView(image);

        thRotate = new Thread(this);
        thRotate.start();

        //-----------------------------------------------------------GridPane------------------
        dirTxt.setPrefWidth(120);
        FlowPane txtFlow = new FlowPane(Orientation.HORIZONTAL);
        Button startBt = new Button("Start");
        Button stopBt = new Button("Stop");
        Button disConncBt = new Button("Disconnect");

        disConncBt.setMaxWidth(100);
        FlowPane sFlow = new FlowPane(Orientation.HORIZONTAL);
        sFlow.getChildren().addAll(new Label("            "), startBt, new Label("               "), stopBt);

        GridPane newG = new GridPane();
        newG.setVgap(30);
        newG.setHgap(8);

        FlowPane cFlow = new FlowPane(Orientation.HORIZONTAL);
        cFlow.getChildren().addAll(new Label("                     "), disConncBt);

        newG.getChildren().addAll(cFlow, sFlow);
        GridPane.setConstraints(sFlow, 0, 1);
        GridPane.setConstraints(cFlow, 0, 2);

        pane2.getChildren().addAll(root, gauge, newG, imageView);

        GridPane.setConstraints(gauge, 1, 2);

        GridPane.setConstraints(newG, 0, 2);

        GridPane.setConstraints(root, 0, 1);
        GridPane.setConstraints(imageView, 1, 1);

        scene2 = new Scene(pane2, 950, 650);

        scene2.getStylesheets().addAll(this.getClass().getResource("MyStyles.css").toExternalForm());

        //----------------------------------------------------end of scene 2----------------------------------------
        //-----------------------------------------------
        // disconnect button callback
        disConncBt.setOnAction(e -> {
            
            // pass to comm layer
            ////////////////////////////////////////////////////////////////////////
            CommProtocol.pwmAndDir = 0;
                
            try {
                Thread.sleep(1500);
            } catch (Exception ex) {
                
            }
            
            window.close();
            Platform.exit();
            System.exit(0);
        });
        //-----------------------------------------------
        startBt.setOnAction(e -> {
            // pass to comm layer
            ////////////////////////////////////////////////////////////////////////
            CommProtocol.pwmAndDir = (byte)(((byte)dir << 7) | (byte)((speedG * 127) / 100));
                
            start = 1;
            thRotate.resume();
        });
        //------------------------------------------------
        // stop button callback
        stopBt.setOnAction(e -> {
            // pass to comm layer
            ////////////////////////////////////////////////////////////////////////
            CommProtocol.pwmAndDir = 0;
                
            start = 0;
            //slider.setValue(0);
            thRotate.suspend();
        });
        //----------------------------------------------------

        //Background
        scene.getStylesheets().addAll(this.getClass().getResource("MyStyles.css").toExternalForm());

        window.setScene(scene);
        window.setTitle("Motor App");
        window.initStyle(StageStyle.UNDECORATED);
        window.show();
    }

    @Override
    public void run() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        int x = 500;
        while (true) {
            if (dir == 1) {
                imageView.setRotate(angle += 30);
            } else if (dir == 0) {
                imageView.setRotate(angle -= 30);
            }

            if ((angle >= 360) || (angle <= -360)) {
                angle = 0;
            }
            try {
                Thread.sleep(200);
                
            } catch (InterruptedException ex) {
                //Logger.getLogger(TryImg.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
