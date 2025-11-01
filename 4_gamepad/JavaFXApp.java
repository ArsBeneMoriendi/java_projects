import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.*;
import javafx.concurrent.*;
import javafx.beans.*;
import javafx.beans.value.*;
import gp.*;

class Basket
{
    public Basket(double x0, double y0)
    {

    }
    void calc_position()
    {

    }
}
 
class G_task extends Task<Gp_values>
{
    final  int VENDOR_ID   = 0x0810;
    Gp gamepad;
    Gp_values curr_v;
      
    public G_task()
    {   
        this.curr_v = new Gp_values();
    }
    
    @Override
    protected Gp_values call() throws Exception
    {
        int i = 0, result;
        gamepad = new Gp();
        result = gamepad.gp_open(VENDOR_ID, 0x0001);
        if(result == -1) System.out.println("gamepad not opened");

        while(true) 
        {
            System.out.println("Task's call method");
            curr_v = gamepad.get_values();
            updateValue(null); 
            updateValue(curr_v);
        }
    }
}

class Game_service extends Service<Gp_values>
{
    Task t; 
    public Game_service()
    {
   
    }
   
    protected Task createTask() 
    {
        t = new G_task();
        return t;
    }
}
 
 
public class JavaFXApp extends Application implements ChangeListener<Gp_values>
{
    Stage stage;

    Game_service g_s;

    private static final int FRAME_WIDTH  = 1280;
    private static final int FRAME_HEIGHT = 855;

    int x, y;

    Image tlo = new Image("img/forest.png");
    Image koszyk = new Image("img/basket.png");

    GraphicsContext gc;
    Canvas canvas;

    public static void main(String[] args) 
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) 
    {

        primaryStage.setTitle("Grzybobranie");
        
        canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        stage = primaryStage;

        Menu menu1 = new Menu("File");
        MenuItem menuItem1 = new MenuItem("Item 1");
        MenuItem menuItem2 = new MenuItem("Exit");
        
        menuItem2.setOnAction(e ->  {
                                        System.out.println("Exit Selected"); 
                                        exit_dialog();
                                    });
        
        menu1.getItems().add(menuItem1);
        menu1.getItems().add(menuItem2);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menu1);     
        VBox vBox = new VBox(menuBar);
        vBox.getChildren().add(canvas);
        Scene scene = new Scene(vBox, 1280, 855);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
                                                e.consume();
                                                exit_dialog();
                                            });

        gc.drawImage(tlo, 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        gc.drawImage(koszyk, 500, 350, 150, 150);

        g_s = new Game_service();
        g_s.valueProperty().addListener(this::changed);     
        g_s.start();    
        primaryStage.show();
    }
 
    public void changed(ObservableValue<? extends Gp_values> observable, Gp_values oldValue, Gp_values newValue)
    {
        if(newValue != null)
        {
            System.out.println("changed method called, x = " + newValue.RX + "y = " + newValue.RY);
        }
    }
 
    public void item_1()
    {
        System.out.println("item 1");
    } 
 
    public void exit_dialog()
    {
        System.out.println("exit dialog");
        Alert alert = new Alert(AlertType.CONFIRMATION,
                                "Do you really want to exit the program?.", 
                                ButtonType.YES, ButtonType.NO);

        alert.setResizable(true);
        alert.onShownProperty().addListener(e ->    { 
                                                        Platform.runLater(() -> alert.setResizable(false)); 
                                                    });

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.YES)
        {
            Platform.exit();
        } 
        else 
        {

        }
    }
}
