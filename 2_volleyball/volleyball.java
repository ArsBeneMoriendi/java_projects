import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

class Ball
{
    double r, x0, y0, v, alpha, x, y;
    String color;
    double t;

    public Ball(double radius, double x0, double y0)
    {
        r = radius;
        this.x0 = x0;
        this.y0 = y0;
        t = 0;
    }

    void calc_position()
    {
        x = x0 + v * Math.cos(Math.toRadians(alpha)) * t;
        y = y0 + v * Math.sin(Math.toRadians(alpha)) * t - (10 * t * t) / 2;
        t+=0.5;
    }
}

public class Volleyball extends Application implements ChangeListener<Number>
{
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;  

    int x, y;

    GraphicsContext gc;
    Canvas canvas;
    Slider alpha, v;

    Ball ball;
        
    public static void main(String[] args) 
    {
        launch(args);
    }
      
    @Override
    public void start(Stage primaryStage)
    {
        AnchorPane root = new AnchorPane();
        primaryStage.setTitle("Volleyball");
    
        canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        canvas.setOnMousePressed(this::mouse); // reagowanie na nacisk myszki
        
        gc = canvas.getGraphicsContext2D();

        double x;
        double y;

        root.getChildren().add(canvas);	
    
        Button btn = new Button();
        btn.setText("Play");
        btn.setOnAction(this::play);	

        root.getChildren().add(btn);
        AnchorPane.setBottomAnchor( btn, 5.0d );

        Slider alpha, v;

        // suwak
        alpha = new Slider(30, 80, 5); 
        alpha.setShowTickMarks(true);
        alpha.setShowTickLabels(true);
        alpha.valueProperty().addListener(new ChangeListener<Number>() 
        {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) 
            {    
                System.out.println("alpha=" + new_val);
                ball.alpha = new_val.doubleValue();
            }
        });
            
        root.getChildren().add(alpha);      

        AnchorPane.setBottomAnchor( alpha, 2.0d );
        AnchorPane.setLeftAnchor( alpha, 150.0d );      
        
        v = new Slider(10, 100, 10);
        v.setShowTickMarks(true);      
        v.setShowTickLabels(true);
        v.valueProperty().addListener(this::changed);
        
        root.getChildren().add(v);
                
        AnchorPane.setBottomAnchor( v, 2.0d );
        AnchorPane.setLeftAnchor( v, 300.0d );            
        
        Scene scene = new Scene(root);
        primaryStage.setTitle("Volleyball");
        primaryStage.setScene( scene );
        primaryStage.setWidth(FRAME_WIDTH + 10);
        primaryStage.setHeight(FRAME_HEIGHT+ 80);

        // rysowanie siatki
        drawNet(gc);

        // rysowanie pilki
        ball = new Ball(50, 100, 350);
        gc.setFill(Color.BLUE);
        gc.fillOval(ball.x0, ball.y0, ball.r, ball.r);
        primaryStage.show();
    }

    private void drawNet(GraphicsContext gc) 
    {
        gc.setStroke(Color.ORANGE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 460, 600, 460);
        gc.setStroke(Color.BLACK);
        gc.strokeLine(320, 460, 320, 200);
    }


    // wyswietlenie zmiany ustawienia suwaka
    public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) 
    {    
        System.out.println("v=" + new_val);  
        ball.v = new_val.doubleValue();
    }

    private void step()
    {
        System.out.println("step");
        ball.calc_position();
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        drawNet(gc);
        gc.fillOval(ball.x, canvas.getHeight() - ball.y, ball.r, ball.r);
    }

    private void mouse(MouseEvent e)
    {
        System.out.println("X=" + e.getX());
        System.out.println("Y=" + e.getY());
        ball.x0 = e.getX();
        ball.y0 = e.getY();
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        drawNet(gc);
        gc.fillOval(ball.x0, ball.y0, ball.r, ball.r);
        ball.y0 = canvas.getHeight() - ball.y0;
    }
    
    // animacja poklatkowa
    private void play(ActionEvent e)    
    {
        Timeline timeline;
        timeline = new Timeline(new KeyFrame(Duration.millis(200), e1->step())); // w czasie 200 ms ma nastapic krok (step) malowania klatek
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }    
}	