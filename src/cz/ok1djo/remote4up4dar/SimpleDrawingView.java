package cz.ok1djo.remote4up4dar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import org.snmp4j.smi.OctetString;

public class SimpleDrawingView extends View {
    // setup initial color
    private final int paintColor = Color.BLACK;
    // defines paint and canvas
    private Paint drawPaint;
    private static int pixelSize = 2;
    public static int w = 128, h = 64, 	onPixel = 0xFF202080,  offPixel = 0xFFE0E0FF;
    public static Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
    public static Bitmap MyBitmap = Bitmap.createBitmap(w*pixelSize+pixelSize, h*pixelSize+pixelSize, conf); // this creates a MUTABLE bitmap
    private Rect rectangle = new Rect(0,0,256,128);
    public static byte p[] = new byte[1344];
    public SimpleDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
    }

    // Setup paint with color and stroke styles
    private void setupPaint() {
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        MyBitmap.setPixel(0,0,Color.RED);
        MyBitmap.setPixel(1,1,Color.RED);
        MyBitmap.setPixel(2,2,Color.RED);
        MyBitmap.setPixel(3,3,Color.RED);
    }
    public static void fillBitmap() {
        for (int i = 0 ; i < w*pixelSize;i++){
            for (int j = 0 ; j < h*pixelSize;j++){
                MyBitmap.setPixel(i,j,Color.DKGRAY);
            }
        }
    }
    public static void paintBitmap(String octetString) {
        for (int a=0;a<1024;a++) {
            p[a] = (byte) ((Character.digit(octetString.charAt(a*3), 16) << 4)
                    + Character.digit(octetString.charAt(a*3+1), 16));
        }
        for (int x = 0 ; x < w;x++){
            for (int y = 0 ; y < h;y++){
                int b = ((int) p[((x >> 3) | (y << 4))]) & 0xff;
                int color =  ( (b & (1 << ( 7 - (x & 0x07)))) != 0 ) ? onPixel : offPixel;
                int i, j;
                for (i=0; i < pixelSize; i++)
                {
                    for (j=0; j < pixelSize; j++)
                    {
                        MyBitmap.setPixel(x*pixelSize + i,y*pixelSize + j,color);
                    }
                }
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(MyBitmap, null, rectangle, null);
    }
}