package infra.e_braille;

import infra.e_braille.util.SystemUiHider;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

import com.googlecode.tesseract.android.TessBaseAPI;
import infra.e_braille.ocr_helper.TessDataManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class main_activity extends Activity implements View.OnTouchListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    Bitmap screenshot;
    Bitmap cropped_screenshot;
    ImageView imageView;
    ImageView previewImageView;
    TextView textView;

    TessBaseAPI baseApi;
    public static final String DATA_PATH = "/sdcard/e-braille/";
    public static final String lang = "eng";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        setContentView(R.layout.main_activity_fullscreen);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.show();

        previewImageView = (ImageView)findViewById(R.id.previewImageView);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnTouchListener(this);

        // vibrate at startup
        ((Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);

        baseApi = new TessBaseAPI();
        baseApi.setDebug(false);
        baseApi.init(DATA_PATH, lang);
        baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, ".-+?abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345789");
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        File image = new  File("/sdcard/screenshot.jpg");
        if(image.exists()){
            screenshot = BitmapFactory.decodeFile(image.getAbsolutePath());
            imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageBitmap(screenshot);
        }
    }

    @Override
    public void onDestroy()
    {
        baseApi.end();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    moveTaskToBack (true);
                    ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                }
                return true;

            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    Toast.makeText(this, "Vol- : clicked", Toast.LENGTH_SHORT).show();
                    ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(100);
                }
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        String text;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                try {
                    cropped_screenshot = Bitmap.createBitmap(screenshot, (int) event.getX(), (int) event.getY(), 100, 50);
                    previewImageView.setImageBitmap(cropped_screenshot);

                    baseApi.setImage(cropped_screenshot);
                    text = baseApi.getUTF8Text();
                    textView.setText(text);
                }
                catch(Exception e){}
                break;
            case MotionEvent.ACTION_MOVE:
                try{
                    cropped_screenshot = Bitmap.createBitmap(screenshot, (int) event.getX(), (int) event.getY(), 100, 50);
                    previewImageView.setImageBitmap(cropped_screenshot);

                    baseApi.setImage(cropped_screenshot);
                    text = baseApi.getUTF8Text();
                    textView.setText(text);
                }
                catch(Exception e){}
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }
}
