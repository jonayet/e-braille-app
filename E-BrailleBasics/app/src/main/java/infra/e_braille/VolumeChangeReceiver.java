package infra.e_braille;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import java.io.OutputStream;

public class VolumeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
            int newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", 0);
            int oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", 0);
            int volumeChanged = newVolume - oldVolume;

            if (volumeChanged < 0 || newVolume == 0) {
                // VOLUME_DOWN button pressed

                // take a screen shot
                try {
                    Process sh = null;
                    sh = Runtime.getRuntime().exec("su", null,null);
                    OutputStream os = sh.getOutputStream();
                    os.write(("/system/bin/screencap -p " + "/sdcard/screenshot.jpg").getBytes("ASCII"));
                    os.flush();
                    os.close();
                    sh.waitFor();
                } catch(Exception e) { }

/*                // load image
                File image = new  File("/sdcard/screenshot.jpg");
                if(image.exists()){
                    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.main_activity_fullscreen, null);
                    ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(image.getAbsolutePath()));
                }*/

                Intent mainActivityIntent = new Intent(context, main_activity.class);
                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(mainActivityIntent);

                // Vibrate the mobile phone
                ((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
            }
        }
    }
}