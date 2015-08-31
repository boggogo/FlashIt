package koemdzhiev.com.flashit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private MediaPlayer mp;
    private Camera camera;
    private Camera.Parameters parameters;
    private ImageView flashLightButton;
    boolean isFlashLightOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                camera.stopPreview();
                isFlashLightOn = false;
                playSound();
            } else {
                flashLightButton.setImageResource(R.mipmap.on);
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

