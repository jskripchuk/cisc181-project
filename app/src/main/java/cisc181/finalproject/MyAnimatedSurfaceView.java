package cisc181.finalproject;

/*

SimpleAnimate app -- animated drawing in a separate thread

Christopher Rasmussen
copyright 2017, University of Delaware

*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.lang.Override;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// note that we are not implementing the SurfaceHolder.Callback interface at this level anymore

public class MyAnimatedSurfaceView extends SurfaceView {

    private MyThread myThread;

    Paint mPaint, mPaintHit, mPaintMiss;
    Bitmap bmap, ast;

    // object position, velocity

    int xPos = 0;
    int yPos = 0;
    double xDelta = 5;
    double yDelta = 5;
    double xAccel = 1.1;
    double yAccel = 1.1;

    float PLAYER_CENTER_X = 200;
    float PLAYER_CENTER_Y = 200;
    float SCREEN_WIDTH = 0;
    float SCREEN_HEIGHT = 0;

    // object orientation

    int angleDegs = 0;
    int angleDegsDelta = 5;

    // object scale

    float scaleFactor = 3.0f;

    // touch activity

    boolean touchDown = false;
    boolean touchStarted = false;
    boolean touchHit = false;
    float touchX, touchY;

    Ship playerShip;
    Asteroid aste = new Asteroid(new FloatPoint(500,500), new FloatPoint(0,0), new FloatPoint(0,0));

    ArrayList<Entity> entities = new ArrayList<Entity>();

    // constructor

    public MyAnimatedSurfaceView(Context context, AttributeSet attrs) {

        super(context, attrs);

        myThread = new MyThread(this);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);

        SCREEN_WIDTH = size.x;
        SCREEN_HEIGHT = size.y;
        PLAYER_CENTER_X = size.x/2;
        PLAYER_CENTER_Y = size.y/2;
        playerShip = new Ship(new FloatPoint(PLAYER_CENTER_X,PLAYER_CENTER_Y), new FloatPoint(0,0), new FloatPoint(0,0));

        mPaint = new Paint();
        mPaint.setStrokeWidth(5);

        mPaintHit = new Paint();
        ColorFilter redFilter = new LightingColorFilter(Color.RED, 0);
        mPaintHit.setColorFilter(redFilter);

        mPaintMiss = new Paint();
        ColorFilter blueFilter = new LightingColorFilter(Color.BLUE, 0);
        mPaintMiss.setColorFilter(blueFilter);

        bmap = BitmapFactory.decodeResource(getResources(), R.drawable.ship);
        ast = BitmapFactory.decodeResource(getResources(), R.drawable.asteroid);
        bmap = Bitmap.createScaledBitmap(bmap,256,256,false);
        ast = Bitmap.createScaledBitmap(ast,256,256,false);

        playerShip.setSprite(bmap);
        aste.setSprite(ast);
        aste.width=256;
        aste.height=256;

        //entities.add(aste);

        for(int i = 0; i < 100; i++){
            //Math.random()*2000;
            Random r = new Random();
            float randomX = (float)r.nextInt(10000);
            float randomY = (float)r.nextInt(10000);
            float randomVelX = (float)r.nextInt(10-5+1)-5;
            float randomVelY = (float)r.nextInt(10-5+1)-5;
            Asteroid a = new Asteroid(new FloatPoint(randomX,randomY), new FloatPoint(randomVelX,randomVelY), new FloatPoint(0,0));
            Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.asteroid);
            img = Bitmap.createScaledBitmap(img,256,256,false);
            a.width=256;
            a.height=256;
            a.setSprite(img);
            entities.add(a);
        }
       // a.setSprite(ast);

        SurfaceHolder holder = getHolder();

        // implement SurfaceHolder.Callback interface with anonymous inner class

        holder.addCallback(new SurfaceHolder.Callback() {

            // start draw thread

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                myThread.setRunning(true);
                myThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            // stop draw thread

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                myThread.setRunning(false);
                while (retry) {
                    try {
                        myThread.join();
                        retry = false;
                    }
                    catch (InterruptedException e) { }
                }
            }
        });

    }

    // do drawing on current canvas

    float cameraX = 0;
    float cameraY = 0;
    //FloatPoint camera = new FloatPoint(0,0);
    Camera camera = new Camera(new FloatPoint(0,0));


    public boolean collide(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2){
        return x1<x2+w2 &&
                x1+w1 > x2 &&
                y1 < y2+h2 &&
                h1+y1>h2;
    }

    public boolean collide(FloatPoint pos1, float w1, float h1, FloatPoint pos2, float w2, float h2){
        return pos1.x<pos2.x+w2 &&
                pos1.x+w1 > pos2.x &&
                pos1.y < pos2.y+h2 &&
                h1+pos1.y>h2;
    }

    public void myDraw(Canvas canvas) {
        camera = new Camera(new FloatPoint(playerShip.pos.x-PLAYER_CENTER_X, playerShip.pos.y-PLAYER_CENTER_Y));
        canvas.drawColor(Color.WHITE);


        playerShip.update();

        Iterator<Entity> it = entities.iterator();

        //Use an iterator so we can safely remove entities
        ArrayList<Entity> toAdd = new ArrayList<Entity>();
        while(it.hasNext()){
            Entity e = it.next();
            if(e.dead){
                it.remove();
            }else {
                e.update();
                if(e instanceof Asteroid){
                    if(e.dead){
                        //TODO
                        //Add stuff with the cargo
                        Item i = new Item();
                        i.pos = e.pos;
                        i.vel = new FloatPoint(1,1);
                        Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.iron);
                        img = Bitmap.createScaledBitmap(img,256,256,false);
                        i.setSprite(img);
                        toAdd.add(i);
                        //entities.add(i);
                    }
                }
                e.render(canvas, mPaint, camera);
            }
        }

        for(Entity e: toAdd){
            entities.add(e);
        }

        canvas.save();
        canvas.rotate(playerShip.angle-45+90, PLAYER_CENTER_X, PLAYER_CENTER_Y);
        canvas.drawBitmap(playerShip.getSprite(), PLAYER_CENTER_X-playerShip.getSprite().getWidth()/2, PLAYER_CENTER_Y-playerShip.getSprite().getHeight()/2,mPaint);
        canvas.restore();
    }

    // respond to the SurfaceView being clicked/dragged

    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction() == MotionEvent.ACTION_DOWN){
            FloatPoint touchPosWorld = camera.screenToWorldPos(new FloatPoint(e.getX(),e.getY()));
            ArrayList<Entity> toAdd = new ArrayList<Entity>();

            for(Entity ent: entities){
                if(ent.containsPoint(touchPosWorld)){
                    if(ent instanceof Asteroid){
                        Asteroid a = (Asteroid)ent;
                        a.removeHealth(playerShip.damage);

                    }
                }
            }
        }

        if(e.getAction() == MotionEvent.ACTION_MOVE){
            FloatPoint touch = camera.screenToWorldPos(new FloatPoint(e.getX(),e.getY()));

            float theta = (float)Math.atan2((touch.y-playerShip.pos.y),(touch.x-playerShip.pos.x));

            playerShip.setAcc(new FloatPoint((float)Math.cos(theta), (float)Math.sin(theta)));
            playerShip.angle = (float)Math.toDegrees(theta);
        }else{
            playerShip.setAcc(new FloatPoint(0,0));
        }

        return true;
    }
}