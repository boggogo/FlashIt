package koemdzhiev.com.flashit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class MainActivity extends Activity {
    private MediaPlayer mp;
    private Camera camera;
    private Camera.Parameters parameters;
    private ImageView flashLightButton;
    boolean isFlashLightOn = false;
    private ImageView mLightening;
    private TextView mVersion;
    private ImageView mOnButtonCover;
    private ImageView mOffButtonCover;
    public TourGuide mTutorialHandler;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private boolean is_tour_guide_shown = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = getSharedPreferences(Constants.APP_NAME,MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        is_tour_guide_shown = mSharedPreferences.getBoolean(Constants.IF_TOUR_GUIDE_SHOWN, false);

        mOnButtonCover = (ImageView)findViewById(R.id.onCoverView);
        mOffButtonCover = (ImageView)findViewById(R.id.offCoverView);

        mLightening = (ImageView)findViewById(R.id.lightening);
        mVersion = (TextView)findViewById(R.id.versionTV);
        mVersion.setText("app version: " + BuildConfig.VERSION_NAME);

        mLightening.setAlpha(70);

        if (ifHasFlash()) {
            //turn on the flash
            camera = Camera.open();
            parameters = camera.getParameters();
            flashLightButton = (ImageView)findViewById(R.id.torchBtn);
            //flashLightButton.setOnClickListener(new FlashOnOffListener());
            mOffButtonCover.setOnClickListener(new FlashOnOffListener());
            mOnButtonCover.setOnClickListener(new FlashOnOffListener());
        } else {
            //alert the user that the flash feature is not part of the camera
            showNoFlashAlert();
        }

        //set up tour guite
        Animation enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);


        Animation exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);

        if(!is_tour_guide_shown) {
        /* initialize TourGuide without playOn() */
            mTutorialHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                    .setPointer(new Pointer())
                    .setToolTip(new ToolTip()
                                    .setTitle("Tour Guide")
                                    .setDescription("Click on this button to activate flashlight.")
                                    .setGravity(Gravity.CENTER_HORIZONTAL)
                    )
                    .setOverlay(new Overlay()
                                    .setEnterAnimation(enterAnimation)
                                    .setExitAnimation(exitAnimation)
                    );


            mTutorialHandler.playOn(mOnButtonCover);
        }

    }

    @Override
    protected void onResume() {
        isFlashLightOn = false;
        flashLightButton.setImageResource(R.mipmap.off);
        mLightening.setImageResource(R.mipmap.layer_1);

        super.onResume();
        if(camera == null){
            camera = Camera.open();
            parameters = camera.getParameters();
        }
        //clean up any tour guide cards left
        if(is_tour_guide_shown && mTutorialHandler != null)
        mTutorialHandler.cleanUp();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }


    private class FlashOnOffListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == mOffButtonCover.getId()) {
                flashLightButton.setImageResource(R.mipmap.off);
                mLightening.setImageResource(R.mipmap.layer_1);
                mLightening.setAlpha(90);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                if(isFlashLightOn)
                camera.stopPreview();
                isFlashLightOn = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                playSound();

                if(!is_tour_guide_shown)
                mTutorialHandler.cleanUp();
                mEditor.putBoolean(Constants.IF_TOUR_GUIDE_SHOWN,true);
                is_tour_guide_shown = true;
                mEditor.commit();
            } else {
                flashLightButton.setImageResource(R.mipmap.on);
                mLightening.setImageResource(R.mipmap.layer_0);
                mLightening.setAlpha(150);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                if(!isFlashLightOn)
                camera.startPreview();
                isFlashLightOn = true;
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                playSound();

                if(!is_tour_guide_shown) {
                    mTutorialHandler.cleanUp();
                    mTutorialHandler.setToolTip(new ToolTip().setTitle("Tour Guide").setDescription("Click on this button to deactivate it.").setGravity(Gravity.TOP | Gravity.RIGHT)).playOn(mOffButtonCover);
                }

            }

            YoYo.with(Techniques.Pulse)
                    .duration(100)
                    .playOn(mLightening);

        }
    }

    private boolean ifHasFlash() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
    //play sound
    private void playSound(){
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                Log.i("MyApp","Silent mode");
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                Log.i("MyApp","Vibrate mode");
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                Log.i("MyApp", "Normal mode");

                if(isFlashLightOn){
                    mp = MediaPlayer.create(MainActivity.this, R.raw.switch_off);
                }else{
                    mp = MediaPlayer.create(MainActivity.this, R.raw.switch_on);
                }
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        mp.release();
                    }
                });
                mp.start();
                break;
        }

    }
    private void showNoFlashAlert() {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Your device hardware does not support flashlight!")
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }

}

