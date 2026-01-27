import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import cam.Frames;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Camera extends Application {

    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;

    private static final double UI_X = 670;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");

    private Group root;
    private Scene scene;
    private Stage stage;

    private Canvas canvas;
    private GraphicsContext gc;

    private Frames frames;

    private boolean negativeOn = false;
    private double zoomFactor = 1.0;

    private byte[] buffer;
    private byte[] negativeBuffer;

    private File saveDir;
    private ChoiceBox<String> formatChoice;

    private ImageView imageView; // ważne: trzymamy jedno, nie dodajemy co klatkę

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        frames = new Frames();
        frames.open_shm("/frames");

        this.stage = primaryStage;
        primaryStage.setTitle("MicroVision");

        root = new Group();

        canvas = new Canvas(850, 490);
        canvas.setMouseTransparent(true);
        gc = canvas.getGraphicsContext2D();

        imageView = new ImageView();
        imageView.setLayoutX(5);
        imageView.setLayoutY(5);

        Button btnProperties = new Button("Wlasciwosci zdjecia");
        btnProperties.setStyle("-fx-background-color: #999999;");
        btnProperties.setLayoutX(UI_X);
        btnProperties.setLayoutY(20);
        btnProperties.setOnAction(this::showProperties);

        Button btnPhoto = new Button("Zrob zdjecie");
        btnPhoto.setStyle("-fx-background-color: #999999;");
        btnPhoto.setLayoutX(UI_X);
        btnPhoto.setLayoutY(140);
        btnPhoto.setOnAction(this::photo);

        Button btnNegative = new Button("Negatyw");
        btnNegative.setStyle("-fx-background-color: #30556f;");
        btnNegative.setLayoutX(UI_X);
        btnNegative.setLayoutY(380);
        btnNegative.setOnAction(this::toggleNegative);

        Label zoomLabel = new Label("Zoom");
        zoomLabel.setLayoutX(UI_X);
        zoomLabel.setLayoutY(420);

        Slider zoomSlider = new Slider(1.0, 10.0, 1.0);
        zoomSlider.setLayoutX(UI_X);
        zoomSlider.setLayoutY(450);
        zoomSlider.setPrefWidth(150);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setMinorTickCount(4);
        zoomSlider.setBlockIncrement(0.1);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> zoomFactor = newVal.doubleValue());

        root.getChildren().addAll(canvas, imageView, btnProperties, btnPhoto, btnNegative, zoomLabel, zoomSlider);

        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(130), e -> dispFrame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void dispFrame() {
        buffer = frames.get_frame();
        if (buffer == null || buffer.length == 0) {
            return;
        }

        byte[] drawBuffer = buffer;

        if (negativeOn) {
            if (negativeBuffer == null || negativeBuffer.length != buffer.length) {
                negativeBuffer = new byte[buffer.length];
            }
            for (int i = 0; i < buffer.length; i++) {
                int unsigned = buffer[i] & 0xFF;
                negativeBuffer[i] = (byte) (255 - unsigned);
            }
            drawBuffer = negativeBuffer;
        }

        BufferedImage src = frames.convert_to_BI(drawBuffer);
        if (src == null) {
            return;
        }

        BufferedImage zoomed = zoomCenterCropAndScale(src, zoomFactor);
        WritableImage fx = bufferedToWritable(zoomed, FRAME_WIDTH, FRAME_HEIGHT);

        imageView.setImage(fx);
    }

    private BufferedImage zoomCenterCropAndScale(BufferedImage src, double zoom) {
        if (zoom <= 1.0) {
            return src;
        }

        int cropW = Math.max(1, (int) (FRAME_WIDTH / zoom));
        int cropH = Math.max(1, (int) (FRAME_HEIGHT / zoom));

        int cropX = Math.max(0, (FRAME_WIDTH - cropW) / 2);
        int cropY = Math.max(0, (FRAME_HEIGHT - cropH) / 2);

        BufferedImage cropped = src.getSubimage(cropX, cropY, cropW, cropH);

        AffineTransform at = new AffineTransform();
        at.scale(zoom, zoom);

        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(cropped, null);
    }

    private WritableImage bufferedToWritable(BufferedImage img, int outW, int outH) {
        WritableImage wr = new WritableImage(outW, outH);
        PixelWriter pw = wr.getPixelWriter();

        int w = Math.min(img.getWidth(), outW);
        int h = Math.min(img.getHeight(), outH);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pw.setArgb(x, y, img.getRGB(x, y));
            }
        }
        return wr;
    }

    private void photo(ActionEvent e) {
        if (buffer == null) {
            return;
        }
        if (saveDir == null) {
            return;
        }

        String time = dateFormat.format(new Date());
        BufferedImage img = frames.convert_to_BI(buffer);

        String ext = getSelectedFormatOrDefault();
        String fileName = time + ext;
        File out = new File(saveDir, fileName);

        try {
            ImageIO.write(img, ext.replace(".", ""), out);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showProperties(ActionEvent e) {
        Button btnFolder = new Button("Choose folder");
        btnFolder.setStyle("-fx-background-color: #cfe2f3;");
        btnFolder.setLayoutX(UI_X);
        btnFolder.setLayoutY(100);
        btnFolder.setOnAction(this::chooseFolder);

        String[] formats = { ".png", ".jpg", ".tif" };
        formatChoice = new ChoiceBox<>(FXCollections.observableArrayList(formats));
        formatChoice.setLayoutX(UI_X);
        formatChoice.setLayoutY(60);
        formatChoice.getSelectionModel().select(1); // domyślnie .jpg

        formatChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                // cos tu mozna dodac w przyszlosci
            }
        });

        gc.fillText("Properties", UI_X, 80);

        root.getChildren().addAll(btnFolder, formatChoice);
    }

    private void chooseFolder(ActionEvent e) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Save");
        File chosen = dirChooser.showDialog(stage);
        if (chosen != null) {
            saveDir = chosen;
        }
    }

    private String getSelectedFormatOrDefault() {
        if (formatChoice == null || formatChoice.getSelectionModel().getSelectedItem() == null) {
            return ".jpg";
        }
        return formatChoice.getSelectionModel().getSelectedItem();
    }

    private void toggleNegative(ActionEvent e) {
        negativeOn = !negativeOn;
    }
}
