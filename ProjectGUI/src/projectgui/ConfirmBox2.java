/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package projectgui;

import com.sun.prism.paint.Color;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.*;

public class ConfirmBox2 {

    //Create variable
    static boolean answer;

    public boolean display(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        Label label = new Label();
        label.setText(message);
        label.setTextFill(javafx.scene.paint.Color.web("#99A3A4"));

        //Create two buttons
        Button reButton = new Button("Retry");
        Button clButton = new Button("close");

        //Clicking will set answer and close window
        reButton.setOnAction(e -> {
            answer = true;
            window.close();
        });
        clButton.setOnAction(e -> {
            answer = false;
            window.close();
        });

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(8);
        layout.setHgap(10);
        layout.setId("pane");

        //Add buttons
        FlowPane pane = new FlowPane(Orientation.HORIZONTAL);
        pane.getChildren().addAll(reButton,new Label("          "),clButton);
        layout.getChildren().addAll(label, pane);   
        GridPane.setConstraints(label, 0, 0);
        GridPane.setConstraints(pane, 1, 5);
        
        Scene scene = new Scene(layout, 685, 150);
        scene.getStylesheets().addAll(this.getClass().getResource("MyStyles.css").toExternalForm());
        
        window.setScene(scene);
        window.showAndWait();

        //Make sure to return answer
        return answer;
    }

}