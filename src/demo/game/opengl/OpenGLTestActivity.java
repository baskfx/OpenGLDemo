/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo.game.opengl;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Activity for testing OpenGL ES drawing speed.  This activity sets up sprites 
 * and passes them off to an OpenGLSurfaceView for rendering and movement.
 */
public class OpenGLTestActivity extends Activity implements SensorEventListener {
    private final static int SPRITE_WIDTH = 64;
    private final static int SPRITE_HEIGHT = 64;
    private final static int BACKGROUND_WIDTH = 512;
    private final static int BACKGROUND_HEIGHT = 512;
    
    private MyGLSurfaceView mGLSurfaceView;
    private GLSurfaceView glSurfaceView;

    SensorManager mSensorManager;
	Sensor mAccelerometerSensor; 
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
     // �������������� ������������
     		mSensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
     		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
     		if (sensors.size() > 0) {
     			for (Sensor sensor : sensors) {
     				if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
     					if (mAccelerometerSensor == null)
     						mAccelerometerSensor = sensor;
     				}
     			}
     		} 
        
        //SceneSprites();
        
        SceneCube();
        
        //SceneShaders();
    }
    
 // ������ ��������
 	@Override
 	public void onStart() {
 		super.onStart();
 		mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 	}

 	// ������������ ��������� ��������
 	@Override
 	public void onStop() {
 		super.onStop();
 		mSensorManager.unregisterListener(this);
 	}

    private void SceneShaders(){
    	// Fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
        // We create our Surfaceview for our OpenGL here.
        glSurfaceView = new GLSurf(this);
 
        // Set our view.
        setContentView(R.layout.activity_main);
 
        // Retrieve our Relative layout from our main layout we just set to our view.
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.gamelayout);
 
        //glSurfaceView.setEGLConfigChooser(new MultisampleConfigChooser());

        // Attach our surfaceview to our relative layout from our main layout.
        RelativeLayout.LayoutParams glParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(glSurfaceView, glParams);
    }
    
    private void SceneCube(){
    	GLSurfaceView view = new GLSurfaceView(this);
    	OpenGLRenderer openGLrenderer = new OpenGLRenderer();
        view.setRenderer(openGLrenderer);
        setContentView(view);
    }
    
    private void SceneSprites(){
    	boolean surfaceViewIsLayout = true; 

        if(surfaceViewIsLayout)
        {
            setContentView(R.layout.gl_test_layout);
            mGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.glSurfaceView);
        }
        else
        {
        	setContentView(R.layout.main);
        	mGLSurfaceView = new MyGLSurfaceView(this);
        }
        
        
        SimpleGLRenderer spriteRenderer = new SimpleGLRenderer(this);
       
        // Clear out any old profile results.
        ProfileRecorder.sSingleton.resetAll();
        
        final Intent callingIntent = getIntent();
        // Allocate our sprites and add them to an array.
        final int robotCount = callingIntent.getIntExtra("spriteCount", 10);
        final boolean animate = callingIntent.getBooleanExtra("animate", true);
        final boolean useVerts = 
            callingIntent.getBooleanExtra("useVerts", false);
        final boolean useHardwareBuffers = 
            callingIntent.getBooleanExtra("useHardwareBuffers", false);
        
        // Allocate space for the robot sprites + one background sprite.
        GLSprite[] spriteArray = new GLSprite[robotCount + 1];    
        
        GLSprite background = new GLSprite(R.drawable.background);
        background.width = BACKGROUND_WIDTH;
        background.height = BACKGROUND_HEIGHT;
        if (useVerts) {
            // Setup the background grid.  This is just a quad.
            Grid backgroundGrid = new Grid(2, 2);
            backgroundGrid.set(0, 0,  0.0f, 0.0f, 0.0f, 0.0f , 1.0f);
            backgroundGrid.set(1, 0, background.width, 0.0f, 0.0f, 1.0f, 1.0f);
            backgroundGrid.set(0, 1, 0.0f, background.height, 0.0f, 0.0f, 0.0f);
            backgroundGrid.set(1, 1, background.width, background.height, 0.0f, 
                    1.0f, 0.0f );
            background.setGrid(backgroundGrid);
        }
        spriteArray[0] = background;
        
        
        Grid spriteGrid = null;
        if (useVerts) {
            // Setup a quad for the sprites to use.  All sprites will use the
            // same sprite grid intance.
            spriteGrid = new Grid(2, 2);
            spriteGrid.set(0, 0,  0.0f, 0.0f, 0.0f, 0.0f , 1.0f);
            spriteGrid.set(1, 0, SPRITE_WIDTH, 0.0f, 0.0f, 1.0f, 1.0f);
            spriteGrid.set(0, 1, 0.0f, SPRITE_HEIGHT, 0.0f, 0.0f, 0.0f);
            spriteGrid.set(1, 1, SPRITE_WIDTH, SPRITE_HEIGHT, 0.0f, 1.0f, 0.0f);
        }
        
        // We need to know the width and height of the display pretty soon,
        // so grab the information now.
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        // This list of things to move. It points to the same content as the
        // sprite list except for the background.
        Renderable[] renderableArray = new Renderable[robotCount]; 
        final int robotBucketSize = robotCount / 3;
        for (int x = 0; x < robotCount; x++) {
            GLSprite robot;
            // Our robots come in three flavors.  Split them up accordingly.
            if (x < robotBucketSize) {
                robot = new GLSprite(R.drawable.skate1);
            } else if (x < robotBucketSize * 2) {
                robot = new GLSprite(R.drawable.skate2);
            } else {
                robot = new GLSprite(R.drawable.skate3);
            }
        
            robot.width = SPRITE_WIDTH;
            robot.height = SPRITE_HEIGHT;
            
            // Pick a random location for this sprite.
            robot.x = (float)(Math.random() * dm.widthPixels);
            robot.y = (float)(Math.random() * dm.heightPixels);
            
            // All sprites can reuse the same grid.  If we're running the
            // DrawTexture extension test, this is null.
            robot.setGrid(spriteGrid);
            
            // Add this robot to the spriteArray so it gets drawn and to the
            // renderableArray so that it gets moved.
            spriteArray[x + 1] = robot;
            renderableArray[x] = robot;
        }
        
       
        // Now's a good time to run the GC.  Since we won't do any explicit
        // allocation during the test, the GC should stay dormant and not
        // influence our results.
        Runtime r = Runtime.getRuntime();
        r.gc();
        
        spriteRenderer.setSprites(spriteArray);
        spriteRenderer.setVertMode(useVerts, useHardwareBuffers);
        
        mGLSurfaceView.setRenderer(spriteRenderer);
        
        if (animate) {
            Mover simulationRuntime = new Mover();
            simulationRuntime.setRenderables(renderableArray);
            
            simulationRuntime.setViewSize(dm.widthPixels, dm.heightPixels);
            mGLSurfaceView.setEvent(simulationRuntime);
        }
        
        if(!surfaceViewIsLayout)
        	setContentView(mGLSurfaceView);
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float sx = event.values[0];
		float sy = event.values[1]; 
		float sz = event.values[2]; 
		
		App.Instance().Alpha = sx;
		App.Instance().Beta = sx;
		App.Instance().Gamma = sz;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
