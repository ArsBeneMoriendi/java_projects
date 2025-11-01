import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
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

import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.StringTokenizer;


class Las
{
    Double[] tablica;
    double x_p[];
    double y_p[];


    public Las(Double[] coords)
    {
        this.tablica = coords;
        x_p = new double[coords.length/2];
        y_p = new double[coords.length/2];
    }
}

class Handler_1 extends DefaultHandler
{
    String loc_name;

    int lasy = 0;

    Double[] tablica;

    double[] t;

    Las wood[] = new Las[3];
    int l = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException
    {
        for (int i=0; i < attributes.getLength(); i++)
        {
            loc_name = attributes.getQName(i);

            if (loc_name.equalsIgnoreCase("id") && attributes.getValue(loc_name).equalsIgnoreCase("lasy"))
            {
                System.out.println("W lesie !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("attr name: : " + loc_name );
                lasy = 1;
            }



            if(lasy == 1)
            {
                if (qName=="path" && loc_name == "d")
                {
                    System.out.println("attr name: : " + loc_name );

                    StringTokenizer st = new StringTokenizer(attributes.getValue(loc_name), " ,Mlz");
                    java.util.List<Double> coor_list = new java.util.ArrayList<>();
                    while(st.hasMoreTokens())
                    {

                        coor_list.add(Double.parseDouble(st.nextToken()));
                    }
                    //Konwersja listy na tablice
                    tablica = coor_list.toArray(new Double[0]);

                    for (int j = 0; j < tablica.length-2; j++)
                    {
                        tablica[j+2] = tablica[j] + tablica[j+2];
                    }

                    for (Double s : tablica)
                    {
                        System.out.println(s);
                    }

                    wood[l] = new Las(tablica);
                    l++;
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    throws SAXException
    {
        if (qName.equalsIgnoreCase("g"))
        {
            System.out.println("End Element :" + qName);
            if(lasy == 1) lasy = 0;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException
    {
        System.out.println(new String(ch, start, length));
    }
}


public class Map_1 extends Application
{
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;  

    boolean w =false;
    Image image = new Image("map.jpg");

    GraphicsContext gc;
    Canvas canvas;

    File inputFile;
    SAXParserFactory factory;
    SAXParser saxParser;
    Handler_1 handler_1;
        
    public static void main(String[] args) 
    {
        launch(args);
    }
     
    @Override
    public void start(Stage primaryStage) 
    {
        try
        {
            inputFile = new File("points.xml");
            factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
            handler_1 = new Handler_1();
            saxParser.parse(inputFile, handler_1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        AnchorPane root = new AnchorPane();
        primaryStage.setTitle("Map");
    
        canvas = new Canvas(FRAME_WIDTH, FRAME_HEIGHT);
        canvas.setOnMousePressed(this::mouse);
        
        gc = canvas.getGraphicsContext2D();
        
        gc.drawImage(image, 0, 0, FRAME_WIDTH, FRAME_HEIGHT);      

        for (int n = 0; n<3; n++)
        {
            int t = 0;
            System.out.println(handler_1.wood[n].tablica.length);
            for (int z = 0; z < handler_1.wood[n].tablica.length/2; z++)
            {
                handler_1.wood[n].x_p[t] = handler_1.wood[n].tablica[2*z];
                handler_1.wood[n].y_p[t] = handler_1.wood[n].tablica[2*z+1];
                t++;
            }

            gc.setFill(Color.GREEN);
            gc.setGlobalAlpha(0.5);
            gc.fillPolygon(handler_1.wood[n].x_p, handler_1.wood[n].y_p, handler_1.wood[n].tablica.length/2);
        }
          
        root.getChildren().add(canvas);	
        
        RadioButton rbtn1 = new RadioButton();
        rbtn1.setText("Woods");
        rbtn1.setSelected(true);      
        rbtn1.setOnAction(this::woods);	

        root.getChildren().add(rbtn1);
        AnchorPane.setBottomAnchor( rbtn1, 5.0d );
        AnchorPane.setLeftAnchor( rbtn1, 50.0d );      


        RadioButton rbtn2 = new RadioButton();
        rbtn2.setText("Rocks");
        rbtn2.setSelected(true);            
        rbtn2.setOnAction(this::rocks);	

        root.getChildren().add(rbtn2);
        AnchorPane.setBottomAnchor( rbtn2, 5.0d );
        AnchorPane.setLeftAnchor( rbtn2, 200.0d );      

        Scene scene = new Scene(root);
        primaryStage.setTitle("Dolina Bedkowska");
        primaryStage.setScene( scene );
        primaryStage.setWidth(FRAME_WIDTH + 10);
        primaryStage.setHeight(FRAME_HEIGHT+ 80);
        primaryStage.show();
    }

    private void woods(ActionEvent e)    
    {
        System.out.println("woods");
        if (w==false)
        {
            gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
            gc.setGlobalAlpha(1.0);
            gc.drawImage(image, 0, 0, FRAME_WIDTH, FRAME_HEIGHT);
            w=true;
            System.out.println("woods, w=" + w);
        }
        else
        {
            gc.setGlobalAlpha(0.5);
            gc.fillPolygon(handler_1.wood[0].x_p, handler_1.wood[0].y_p, handler_1.wood[0].tablica.length/2);
            gc.fillPolygon(handler_1.wood[1].x_p, handler_1.wood[1].y_p, handler_1.wood[1].tablica.length/2);
            gc.fillPolygon(handler_1.wood[2].x_p, handler_1.wood[2].y_p, handler_1.wood[2].tablica.length/2);
            w=false;
            System.out.println("woods, w=" + w);
        }
    }

    private void rocks(ActionEvent e)    
    {
        System.out.println("rocks");
    }

    private void mouse(MouseEvent e)
    {
        System.out.println("X=" + e.getX());
        System.out.println("Y=" + e.getY());         
    }   
}	
