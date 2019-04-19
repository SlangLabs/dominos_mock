package in.slanglabs.dominos_mock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.SlangEntity;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.action.SlangMultiStepIntentAction;
import in.slanglabs.platform.prompt.SlangMessage;

/**
 * This is where all Slang specific handling goes
 */

public class VoiceInterface {
    private static String app_id = "5efd230bd7a64466a95882e56f3c9aa6";
    private static String api_key = "c80525dd5fa146d6a3a1aba91fc5d6b9";
    private static Context appContext;

    // @SlangIntent "I want to order a <ref entity="order_mode">takeaway</ref> for <ref entity="order_qty">3</ref> <ref entity="order_size">medium</ref> <ref entity="order_desc">garden veggie </ref> <ref entity="order_item">pizza</ref>, 2 garlic bread and a brownie"
    public static final String ORDER_FOOD = "order_food";
    // @SlangEntity order_food "takeaway, delivery" none mandatory
    public static final String ORDER_FOOD__MODE = "order_mode";
    // @SlangEntity order_food string none mandatory
    public static final String ORDER_FOOD__LOCATION = "order_location";
    // @SlangEntity order_food int 1
    public static final String ORDER_FOOD__QTY = "order_qty";
    // @SlangEntity order_food "small, regular, medium, larger"
    public static final String ORDER_FOOD__SIZE = "order_size";
    // @SlangEntity order_food "garden veggie, pepperoni"
    public static final String ORDER_FOOD__DESC = "order_desc";
    // @SlangEntity order_food "garlic bread, pizza, brownie"
    public static final String ORDER_FOOD__ITEM = "order_item";

    // @SlangIntent "Yes"
    public static final String CONFIRM_INTENT = "confirm";
    // @SlangEntity confirm "yes, no" none
    public static final String CONFIRM_RESULT = "result";

    // The field that indicates what action to be taken
    public static final String ACTION = "action_name";
    // The various values of the action field
    public static final String ACTION_MAIN = "main";
    public static final String ACTION_ORDER_DELIVERY = "order_delivery";
    public static final String ACTION_ORDER_TAKEAWAY = "order_takeaway";
    private static boolean mLaunchedByAssistant = false;

    // To initialize Slang in your application, simply call VoiceInterface.init(context)
    public static void init(Context context) {
        appContext = context;

        try {
            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                .setContext(context)
                .setBuddyId(app_id)
                .setAPIKey(api_key)
                .setListener(new BuddyListener(context))
                .setIntentAction(new MyActionHandler())
                .setRequestedLocales(SlangLocale.getSupportedLocales())
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                // change env to production when the buddy is published to production
                .setEnvironment(SlangBuddy.Environment.STAGING)
                .build();
            SlangBuddy.initialize(options);
        } catch (SlangBuddyOptions.InvalidOptionException e) {
            e.printStackTrace();
        } catch (SlangBuddy.InsufficientPrivilegeException e) {
            e.printStackTrace();
        }
    }

    public static void launchedByAssistant(boolean b) {
        mLaunchedByAssistant = b;
        // If Slang is initialized by now, then do the same thing we would haev done at
        // init time
        if (SlangBuddy.isInitialized()) {
            launchAtStart();
        }
    }

    private static void launchAtStart() {
        try {
            HashMap<Locale, String> strings = new HashMap<>();
            SlangMessage message;

            strings.put(
                SlangLocale.LOCALE_ENGLISH_IN,
                "Welcome to Dominos. What would order today?"
            );
            message = SlangMessage.create(strings);
            SlangBuddy.startConversation(message, true);
        } catch (SlangBuddy.UninitializedUsageException e) {
            e.printStackTrace();
        }
    }

    private static class BuddyListener implements SlangBuddy.Listener {
        private Context appContext;

        public BuddyListener(Context appContext) {
            this.appContext = appContext;
        }

        @Override
        public void onInitialized() {
            Log.d("BuddyListener", "Slang Initialised Successfully");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, "Slang Initialised", Toast.LENGTH_LONG).show();
                    // launched by assistant
                    if (mLaunchedByAssistant) {
                        launchAtStart();
                    }
                }
            }, 10);
        }

        @Override
        public void onInitializationFailed(final SlangBuddy.InitializationError e) {
            Log.d("BuddyListener", "Slang failed:" + e.getMessage());

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, "Failed to initialise Slang:" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, 10);
        }

        @Override
        public void onLocaleChanged(final Locale newLocale) {
            Log.d("BuddyListener", "Locale Changed:" + newLocale.getDisplayName());

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext, "Locale Changed:" + newLocale.getDisplayName(), Toast.LENGTH_LONG).show();
                }
            }, 10);
        }

        @Override
        public void onLocaleChangeFailed(final Locale newLocale, final SlangBuddy.LocaleChangeError e) {
            Log.d("BuddyListener",
                "Locale(" + newLocale.getDisplayName() + ") Change Failed:" + e.getMessage());

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(appContext,
                        "Locale(" + newLocale.getDisplayName() + ") Change Failed:" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            }, 10);
        }
    }

    private static class MyActionHandler implements SlangMultiStepIntentAction {
        private String mLastSeenIntent = null;
        private String mLastSeenMissingEntity = null;
        private String mLastActivity = null;
        private String mCurrentActivity = ACTION_MAIN;

        private class ItemOrdered {
            String item;
            String size;
            int qty;
        }

        private enum IntentContext {
            UPSELL_ORDER,
            CONTINUE_ORDER
        }

        private enum MessageType {
            CONTINUE_ORDERING,
            CHECKOUT_READY,
            START_ORDER,
            UPSELL_TO_LARGE_PIZZA
        }

        private IntentContext mIntentContext;
        private List<ItemOrdered> mItemsOrdered = new ArrayList<>();

        @Override
        public Status action(SlangIntent slangIntent, SlangSession slangSession) {
            SlangMessage message = null;

            switch (slangIntent.getName()) {
                case ORDER_FOOD:
                    // If no item has been not specified then ask user what they want to order
                    if (mItemsOrdered.size() == 0) {
                        message = getMessage(MessageType.START_ORDER);
                    } else if ((message = shouldUpSell(slangIntent)) != null) {
                        // Try and upsell
                        mIntentContext = IntentContext.UPSELL_ORDER;
                    } else {
                        // Ask if there is anything else
                        mIntentContext = IntentContext.CONTINUE_ORDER;
                        message = getMessage(MessageType.CONTINUE_ORDERING);
                    }
                    break;

                case CONFIRM_INTENT:
                    // If the previous intent was order food, then this confirmation was for
                    // continuing the order
                    switch (mIntentContext) {
                        case UPSELL_ORDER:
                            if (slangIntent.getEntity(CONFIRM_RESULT).getValue().equalsIgnoreCase("yes")) {
                                slangIntent.getCompletionStatement().overrideAffirmative("Sure. Order has been updated");
                            } else {
                                slangIntent.getCompletionStatement().overrideAffirmative("Okay.");
                            }
                            message = getMessage(MessageType.CHECKOUT_READY);
                            break;

                        case CONTINUE_ORDER:
                            if (slangIntent.getEntity(CONFIRM_RESULT).getValue().equalsIgnoreCase("yes")) {
                                slangIntent.getCompletionStatement().overrideAffirmative("");
                                message = getMessage(MessageType.CHECKOUT_READY);
                            } else {
                                slangIntent.getCompletionStatement().overrideAffirmative("Okay.");
                            }
                            break;
                        default:
                            // Any other context. Just ignore this.
                            return Status.FAILURE;
                    }
                    break;
            }

            try {
                if (message != null) {
                    SlangBuddy.startConversation(message, true);
                }
            } catch (SlangBuddy.UninitializedUsageException e) {
                e.printStackTrace();
            }

            return Status.SUCCESS;
        }

        @Override
        public void onIntentResolutionBegin(SlangIntent slangIntent, SlangSession slangSession) {
            mLastSeenIntent = slangIntent.getName();
        }

        @Override
        public Status onEntityUnresolved(SlangEntity slangEntity, SlangSession slangSession) {
            mLastSeenMissingEntity = ORDER_FOOD__LOCATION;
            switch (slangEntity.getIntent().getName()) {
                case ORDER_FOOD:
                    switch (slangEntity.getName()) {
                        case ORDER_FOOD__MODE:
                            // TODO: Show the choices visibly if possible
                            break;

                        case ORDER_FOOD__LOCATION:
                            if (slangEntity.getIntent().getEntity(ORDER_FOOD__MODE).equals("delivery")) {
                                slangEntity.getPrompt().overrideQuestion("Where would you like it to be delivered?");
                            } else {
                                slangEntity.getPrompt().overrideQuestion("Where would you like to pick it up from?");
                            }
                            break;

                        case ORDER_FOOD__SIZE:
                            // If the item is pizza, then we need to gather size. Else its okay
                            // to skip it
                            if (!slangEntity.getValue().toLowerCase().contains("pizza")) {
                                slangEntity.resolve("default");
                            } else {
                                slangEntity.resolve("medium");
                            }
                            break;
                    }
                    break;
            }

            return Status.SUCCESS;
        }

        @Override
        public Status onEntityResolved(SlangEntity slangEntity, SlangSession slangSession) {
            switch (slangEntity.getIntent().getName()) {
                case ORDER_FOOD:
                    switch (slangEntity.getName()) {
                        case ORDER_FOOD__MODE:
                            if (slangEntity.getIntent().getEntity(ORDER_FOOD__MODE).equals("delivery")) {
                                switchTo(ACTION_ORDER_DELIVERY, slangSession);
                            } else {
                                switchTo(ACTION_ORDER_TAKEAWAY, slangSession);
                            }
                            break;

                        case ORDER_FOOD__LOCATION:
                            // The location has been selected. Switch back to previous screen
                            switchTo(mLastActivity, slangSession);
                            break;

                        case ORDER_FOOD__ITEM:
                            // Add this to the list of ordered items
                            ItemOrdered itemOrdered = new ItemOrdered();

                            itemOrdered.item = slangEntity.getValue();
                            itemOrdered.qty = Integer.parseInt(slangEntity.getIntent().getEntity(ORDER_FOOD__QTY).getValue());
                            itemOrdered.size = slangEntity.getIntent().getEntity(ORDER_FOOD__SIZE).getValue();

                            mItemsOrdered.add(itemOrdered);
                            break;
                    }
                    break;
            }
            return Status.SUCCESS;
        }

        @Override
        public void onIntentResolutionEnd(SlangIntent slangIntent, SlangSession slangSession) {

        }

        private void switchTo(final String screenName, final SlangSession session) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Intent i = new Intent(session.getCurrentActivity(), ActionActivity.class);

                    mLastActivity = mCurrentActivity;
                    i.putExtra(ACTION, screenName);
                    appContext.startActivity(i);
                    mCurrentActivity = screenName;
                }
                , 0
            );
        }

        private SlangMessage shouldUpSell(SlangIntent slangIntent) {
            // Check if the entities are up-sellable
            if (slangIntent.getEntity(ORDER_FOOD__ITEM).isResolved() &&
                slangIntent.getEntity(ORDER_FOOD__ITEM).getValue().toLowerCase().contains("pizza") &&
                !slangIntent.getEntity(ORDER_FOOD__SIZE).getValue().equals("large")) {

                return getMessage(MessageType.UPSELL_TO_LARGE_PIZZA);
            } else {
                return null;
            }
        }

        private SlangMessage getMessage(MessageType messageType) {
            HashMap<Locale, String> strings = new HashMap<>();

            switch(messageType) {
                case CONTINUE_ORDERING:
                    strings.put(SlangLocale.LOCALE_ENGLISH_IN, "Would you like to order anything else?");
                    strings.put(SlangLocale.LOCALE_ENGLISH_US, "Would you like to order anything else?");
                    break;

                case CHECKOUT_READY:
                    strings.put(SlangLocale.LOCALE_ENGLISH_IN, "Would you like to checkout now?");
                    strings.put(SlangLocale.LOCALE_ENGLISH_US, "Would you like to checkout now?");
                    break;

                case UPSELL_TO_LARGE_PIZZA:
                    strings.put(SlangLocale.LOCALE_ENGLISH_IN, "We have a buy one get one free offer on large pizzas. Would you like to change your pizza order to a large one?");
                    strings.put(SlangLocale.LOCALE_ENGLISH_US, "We have a buy one get one free offer on large pizzas. Would you like to change your pizza order to a large one?");
                    break;

                case START_ORDER:
                    strings.put(SlangLocale.LOCALE_ENGLISH_IN, "What would you like to order?");
                    strings.put(SlangLocale.LOCALE_ENGLISH_US, "What would you like to order?");
                    break;
            }

            SlangMessage message = new SlangMessage(strings);
            return message;
        }
    }
}
