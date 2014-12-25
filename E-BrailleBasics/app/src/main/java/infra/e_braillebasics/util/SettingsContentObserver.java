package infra.e_braillebasics.util;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.widget.Toast;

import infra.e_braillebasics.main_activity;

public class SettingsContentObserver extends ContentObserver {
    int previousVolume;
    Context context;

    public SettingsContentObserver(Context c, Handler handler) {
        super(handler);
        context=c;

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        previousVolume = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_SYSTEM);
        int delta=previousVolume-currentVolume;

        Toast.makeText(context, "Vol:" + currentVolume, Toast.LENGTH_SHORT).show();

        if(delta>0)
        {
            previousVolume = currentVolume;
            //Toast.makeText(context, "Vol-:" + delta, Toast.LENGTH_SHORT).show();
        }
        else if(delta<0)
        {
            previousVolume = currentVolume;
            //Toast.makeText(context, "Vol+:" + delta, Toast.LENGTH_SHORT).show();
        }
    }
}