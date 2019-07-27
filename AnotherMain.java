/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencv;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import javafx.scene.image.WritableImage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import static javafx.scene.input.KeyCode.T;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author AMUTHAN
 */

  public class AnotherMain extends Application {
      VideoCapture camera ;
      Mat frame = new Mat();
      Mat frame2 = new Mat();
      Mat blurred=new Mat();
      MatOfRect faces=new MatOfRect(); 
      ByteArrayInputStream stream;
      MatOfByte buffer = new MatOfByte();
      GraphicsContext gc;
      Image img;
      CheckBox colorCheck,calibCheck;
      Button capture;
      Stage st;
      double absoluteFaceSize=0;
      CascadeClassifier cls;
      Timer timer;
      Stage canStage;
      GraphicsContext ctx;Canvas can;
      TextArea text;
      double hue,sat,val;
    public static void main(String[] args) {
     Application.launch(args);
    }
  
     static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
           }

    @Override
    public void start(Stage primaryStage) throws Exception {
    
    camera = new VideoCapture(0);
    
    Canvas c=new Canvas();
    
    c.setWidth(500);
    c.setHeight(500);
    gc = c.getGraphicsContext2D();
    Pane pane=new Pane();
    pane.getChildren().add(c);
    colorCheck=new CheckBox("Detect specified color");
    calibCheck=new CheckBox("Start to calibarate");
    capture=new Button("CAPTURE");
    text=new TextArea("--Calibrated color code--");
    capture.setVisible(false);
    VBox vb=new VBox();
    vb.setSpacing(10);
    vb.getChildren().addAll(new HBox(calibCheck,capture),colorCheck,text,pane);
    capture.setOnAction(new EventHandler() {
        @Override
        public void handle(Event event) {
            timer.cancel();            
        }
    });
    text.setPrefRowCount(6);
    pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            System.out.println("value on "+event.getX()+" "+event.getY());
            if(canStage!=null){
            ctx.setFill(img.getPixelReader().getColor((int)event.getX(),(int)event.getY()));
            ctx.fillRect(0,0,can.getWidth(),can.getHeight());
            if(!canStage.isShowing()){
            canStage.show();
            }
            }
            else{
            canStage=new Stage();
            can=new Canvas(300,300);
            ctx=can.getGraphicsContext2D();
            ctx.setFill(img.getPixelReader().getColor((int)event.getX(),(int)event.getY()));
            ctx.fillRect(0,0,can.getWidth(),can.getHeight());
            canStage.setScene(new Scene(new Pane(can)));
            canStage.setHeight(can.getHeight());canStage.setWidth(can.getWidth());
            canStage.setX(100);
            canStage.setY(100);
            canStage.show();
            canStage.setResizable(false);
            System.out.println(img.getPixelReader().getColor((int)event.getX(),(int)event.getY()));
        }
            double redness=img.getPixelReader().getColor((int)event.getX(),(int)event.getY()).getRed()*255;
            double greenness=img.getPixelReader().getColor((int)event.getX(),(int)event.getY()).getGreen()*255;
            double blueness=img.getPixelReader().getColor((int)event.getX(),(int)event.getY()).getBlue()*255;
            double hue=img.getPixelReader().getColor((int)event.getX(),(int)event.getY()).getHue();
            double saturation=img.getPixelReader().getColor((int)event.getX(),(int)event.getY()).getSaturation();
            System.out.println("saturation "+saturation*255);
            sat=saturation*255;
            
            rgbTohsv(redness,greenness,blueness);
           
        }
    });
    

    calibCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(newValue){
        capture.setVisible(true);
        }
        else{
        capture.setVisible(false);
        timer=new Timer();
        timer.schedule(new task(),1000,1);
        img.getPixelReader().getColor(100, 100);
        }
        }
    });
    st=new Stage();
    st.setScene(new Scene(vb));
    st.show();
    

    timer=new Timer();
    
    timer.schedule(new task(),1000,1);
    
    st.setOnCloseRequest(new EventHandler<WindowEvent>() {
    @Override
    public void handle(WindowEvent t) {
        Platform.exit();
        System.exit(0);
    }
    
});
       

    }
    
    void rgbTohsv(double r,double g,double b){
    double hue,sat,val;    
    double max=Math.max(r, g);max=Math.max(max,b);
    double min=Math.min(r, g);min=Math.min(min, b);
    double delta=max-min;
    val=max;
    if(max!=0){sat=delta/max;}
    else{sat=0;hue=-1;}
    if(r==max){hue=(g-b)/delta;}
    else if(g==max){hue=2+(b-r)/delta;}
    else{hue=4+(r-g)/delta;}
    hue*=60;
    if(hue<0){
    hue=hue+360;
    }
    this.hue=hue/2;
    this.val=val;
    valueUpdate(r,g,b,hue,this.sat,val);
    }
    void valueUpdate(double r,double g,double b,double h,double s,double v){
            String redness=String.valueOf(r);
            String greenness=String.valueOf(g);
            String blueness=String.valueOf(b);
            
            String hue=String.valueOf(h);
            
            
            text.setText("Red "+redness+"\n"+"Green "+greenness+"\n"+"Blue "+blueness+"\n"+"Hue "+hue
            +"\n"+"Saturation "+s+"\n"+"Value "+v);
    
    } 

    class task extends TimerTask{

        @Override
        public void run() {
            //System.out.println("running...");
            if (camera.read(frame)) {
                if(calibCheck.isSelected()){
                capture.setVisible(true);
                }
                else if(colorCheck.isSelected()){
                 List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                 
                 Mat hierarchy = new Mat();
                 Mat mask=new Mat();
                 
                 Imgproc.cvtColor(frame, frame2, Imgproc.COLOR_BGR2HSV);
                 
                // Core.inRange(frame2, new Scalar(60, 100, 97), new Scalar(52, 47, 48),frame2);
                 //for red
                 int h1=(int)hue-2;int h2=(int)hue+2;
                 int s1=(int)sat-2;int s2=(int)sat+2;
                 int v1=(int)val-2;int v2=(int)val+2;
                 Core.inRange(frame2, new Scalar(h1,s1,v1),new Scalar(h2,s2,v2),frame2);
               
                 //Core.bitwise_not(frame2,frame2);
               
                 
                
                //Core.inRange(frame2, new Scalar(160, 100, 100), new Scalar(179, 255, 255), frame2);
                //Core.inRange(frame2, new Scalar(0, 100, 100), new Scalar(10, 255, 255), frame2);
                //Core.bitwise_not(frame2,frame2);
                 Imgproc.findContours(frame2, contours,hierarchy , Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
                  // Draw all the contours such that they are filled in.
                 double minx=0,miny=0,maxx=0,maxy=0;
                 if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
                       // for each contour, display it in blue
                        for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
                             {  
                              Imgproc.drawContours(frame2, contours, idx, new Scalar(250, 0, 0)); 
                             }
                             }
                 drawContour(contours);
                 }
            try{
            Imgcodecs.imencode(".bmp", frame, buffer);
            stream = new ByteArrayInputStream(buffer.toArray());
            Image img1=new Image(stream);
            img=img1;
                gc.drawImage(img1, 0, 0);
             }
            catch(Exception e){}
            }
            
         }
        
void capture(){
            Imgcodecs.imencode(".bmp", frame, buffer);
            stream = new ByteArrayInputStream(buffer.toArray());
            Image img=new Image(stream);
            
}        
        
void drawContour(List<MatOfPoint> contours){
        MatOfPoint2f         approxCurve = new MatOfPoint2f();
          Rect rect1=null;
    //For each contour found
    for (int i=0; i<contours.size(); i++)
    {
        //Convert contours(i) from MatOfPoint to MatOfPoint2f
        MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
        //Processing on mMOP2f1 which is in type MatOfPoint2f
        double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
        Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

        //Convert back to MatOfPoint
        MatOfPoint points = new MatOfPoint( approxCurve.toArray() );
         
        // Get bounding rect of contour
        Rect rect = Imgproc.boundingRect(points);
        //Imgproc.rectangle(frame,new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0,255, 0,0), 2);  
        if(rect1==null){
            rect1=rect;
        }
        else
            if(rect.area()>rect1.area()){
            rect1=rect;
            }
          Imgproc.rectangle(frame,new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0,255, 0,0), 2); 

    }  
    if(rect1!=null){
        Imgproc.rectangle(frame,new Point(rect1.x,rect1.y), new Point(rect1.x+rect1.width,rect1.y+rect1.height), new Scalar(0,255, 0,0), 2);  
    }
}

       
    void detectSomething(){
          if (absoluteFaceSize == 0){ 
                         int height = frame.rows();
                         if (Math.round(height * 0.2f) > 0){
                         absoluteFaceSize= Math.round(height * 0.2f);
                    }
     
                 }
                  cls.detectMultiScale(frame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize,absoluteFaceSize), new Size());
                  Rect[] facesArray = faces.toArray();
                  if(facesArray.length!=0){
                 // System.out.println("area "+(facesArray[0].tl().x-facesArray[0].br().x)*(facesArray[0].tl().y-facesArray[0].br().y));    
                 System.out.println(facesArray[0].tl()); 
                 System.out.println("detected.... ");
                  }
                  for(int i = 0; i < facesArray.length; i++){
                  Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0,255, 0, 255), 3);   
                  }
        }
        
}
  }




