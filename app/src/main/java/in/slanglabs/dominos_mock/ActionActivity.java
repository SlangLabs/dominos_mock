package in.slanglabs.dominos_mock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Add a class header comment!
 */

public final class ActionActivity extends AppCompatActivity {
    private static Map<String, Integer> actions = new HashMap<String, Integer>() {{
        put(VoiceInterface.ACTION_MAIN, R.drawable.main_screen);
        put(VoiceInterface.ACTION_ORDER_DELIVERY, R.drawable.order_food_order_delivery);
        put(VoiceInterface.ACTION_ORDER_TAKEAWAY, R.drawable.order_food_order_takeaway);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        performAction();
    }

    void performAction() {
        ImageView view = findViewById(R.id.main_image);
        Intent i = getIntent();

        view.setImageResource(actions.get(i.getStringExtra(VoiceInterface.ACTION)));
    }
}
