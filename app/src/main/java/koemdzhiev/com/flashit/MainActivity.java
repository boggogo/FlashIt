package koemdzhiev.com.flashit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private MediaPlayer mp;
    private Camera camera;
    private Camera.Parameters parameters;
    private ImageView flashLightButton;
    boolean isFlashLightOn = false;
    private ImageView mLightening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLightening = (ImageView)findViewById(R.id.lightening);

        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 0.5f);
        animation1.setDuration(700);
        //animation1.setStartOffset(5000);
        animation1.setFillAfter(true);
        mLightening.startAnimation(animation1);
       // mLightening.setAlpha(70);

        if (ifHasFlash()) {
            //turn on the flash
            camera = Camera.open();
            parameters = camera.getParameters();
            flashLightButton = (ImageView)findViewById(R.id.torchBtn);
            flashLightButton.setOnClickListener(new FlashOnOffListener());
        } else {
            //alert the user that the flash feature is not part of the camera
            showNoFlashAlert();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

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
            if (isFlashLightOn) {
                flashLightButton.setImageResource(R.mipmap.off);
                mLightening.setImageResource(R.mipmap.layer_1);
                mLightening.setAlpha(90);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
                isFlashLightOn = false;
                playSound();
            } else {
                flashLightButton.setImageResource(R.mipmap.on);
                mLightening.setImageResource(R.mipmap.layer_0);
                mLightening.setAlpha(100);
                if(camera == null){
                    camera = Camera.open();
                    parameters = camera.getParameters();
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                camera.startPreview();
                isFlashLightOn = true;
                playSound();
            }

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

