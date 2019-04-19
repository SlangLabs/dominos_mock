package in.slanglabs.dominos_mock;

import android.os.Bundle;
import android.speech.tts.Voice;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.prompt.SlangMessage;

/**
 * An example activity that shows the Slang trigger
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ImageView view = findViewById(R.id.main_image);
        view.setImageResource(R.drawable.main_screen);

        SlangBuddy.getBuiltinUI().show(this);

        String text = getIntent().getExtras() != null
            ? getIntent().toUri(0)
            : "No Extras";

        Log.d("gmurthy", text);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (getIntent().getBooleanExtra("extra_accl_intent", false)) {
            Log.d("gmurthy","launched by assistant");
            VoiceInterface.launchedByAssistant(true);
        } else {
            Log.d("gmurthy","launched manually");
        }
    }
}
