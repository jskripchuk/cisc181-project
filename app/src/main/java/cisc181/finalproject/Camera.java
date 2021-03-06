package cisc181.finalproject;

/**
 * Created by jimmy on 5/8/17.
 *
 * Camera
 * Used to give the illusion of a camera tracking the player ship by instead rendering all other objects based on a simple transformation
 */

public class Camera {
    FloatPoint camera;

    Camera(FloatPoint camera){
        this.camera = camera;
    }

    public FloatPoint screenToWorldPos(FloatPoint screenPoint){
        return new FloatPoint(screenPoint.x+camera.x, screenPoint.y+camera.y);
    }

    public FloatPoint worldToScreenPos(FloatPoint worldPoint){
        //Note to self,
        //Don't use += or else you will change the worldPoint value
        //Thanks java
        return new FloatPoint(worldPoint.x-camera.x, worldPoint.y-camera.y);
    }
}
