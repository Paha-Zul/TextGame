package com.quickbite.rx2020;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.quickbite.rx2020.interfaces.IPlatformSpecific;
import com.quickbite.rx2020.util.IabHelper;
import com.quickbite.rx2020.util.IabResult;
import com.quickbite.rx2020.util.Inventory;
import com.quickbite.rx2020.util.Logger;
import com.quickbite.rx2020.util.Purchase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class AndroidLauncher extends AndroidApplication implements IPlatformSpecific {
	IabHelper mHelper;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new TextGame(this), config);

		String base64EncodedPublicKey = getString(R.string.key3) + getString(R.string.key1) + getString(R.string.key2) + getString(R.string.key4);

		// compute your public key and store it in base64EncodedPublicKey
		mHelper = new IabHelper(this, base64EncodedPublicKey);

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					Logger.log("AndroidLauncher", "Problem setting up In-app Billing: " + result, Logger.LogLevel.Error);
				}else {
					Logger.log("AndroidLauncher", "Billing success:: " + result, Logger.LogLevel.Info);

					//Let's then query the inventory...
					try {
                        Logger.log("AndroidLauncher", "Trying to query the inventory", Logger.LogLevel.Info);
                        mHelper.queryInventoryAsync(mGotInventoryListener);
					} catch (IabHelper.IabAsyncInProgressException e) {
						e.printStackTrace();
					}
				}
			}

		});
	}

	@Override
	public void openURL(@NotNull String link) {
//		Intent i = new Intent(Intent.ACTION_VIEW);
//		i.setData(Uri.parse("https://play.google.com/whateveryoururlis"));
//		this.startActivity(i);
		Gdx.net.openURI(link);
	}

    @Override
    public void donate(int amount) {
        Logger.log("IAB", "Trying to donate $"+amount, Logger.LogLevel.Info);
		if(amount != 0) {
			//Don't do anything right now.
			String sku = "";
			String payload = "";
			if(amount == 1) {
				sku = SKU_DONATE_SMALL;
				payload = "DON_SMALL";
			}else if(amount == 5) {
				sku = SKU_DONATE_MEDIUM;
				payload = "DON_MEDIUM";
			}else if(amount == 10) {
				sku = SKU_DONATE_LARGE;
				payload = "DON_LARGE";
			}else if(amount == 20) {
				sku = SKU_DONATE_HUGE;
				payload = "DON_HUGE";
			}else {
				sku = SKU_DONATE_ERROR;
				payload = "DON_ERROR";
			}

			Logger.log("IAB", "Trying to purchase sku: "+sku+" with payload: "+payload, Logger.LogLevel.Info);

			try {
				//Launch the purchase flow with a test SKU for now.
				mHelper.launchPurchaseFlow(this, sku, RC_REQUEST, mPurchaseFinishedListener, payload);
			} catch (IabHelper.IabAsyncInProgressException e) {
				e.printStackTrace();
			}
		}
    }

    // Callback for when a purchase is finished
	private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if ( purchase == null) {
				Logger.log("IAB", "Purchase is null apparently, result: " + result, Logger.LogLevel.Info);
				Logger.writeLog("log.txt");
				return;
			}
            Logger.log("IAB", "Purchase finished: " + result + ", purchase: " + purchase, Logger.LogLevel.Info);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Logger.log("Android", "Error purchasing: "+result, Logger.LogLevel.Error);
                //complain("Error purchasing: " + result);
                //setWaitScreen(false);
				Logger.writeLog("log.txt");
                return;
            }
//            if (!verifyDeveloperPayload(purchase)) {
//                //complain("Error purchasing. Authenticity verification failed.");
//                //setWaitScreen(false);
//                return;
//            }

            Logger.log("IAB", "Purchase successful.", Logger.LogLevel.Info);

            if (purchase.getSku().equals(SKU_DONATE_SMALL) || purchase.getSku().equals(SKU_DONATE_MEDIUM) || purchase.getSku().equals(SKU_DONATE_LARGE) || purchase.getSku().equals(SKU_DONATE_HUGE)) {
                Logger.log("IAB", "Purchased a donation: "+purchase.getSku(), Logger.LogLevel.Info);
				try {
					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
				} catch (IabHelper.IabAsyncInProgressException e) {
					e.printStackTrace();
				}

				// Do what you want here maybe call your game to do some update
                //
                // Maybe set a flag to indicate that ads shouldn't show anymore
//                mAdsRemoved = true;

            }
        }
    };

	private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
		new IabHelper.OnConsumeFinishedListener() {
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				if (result.isSuccess()) {
					Logger.log("Android","Consume success on "+purchase.getSku(), Logger.LogLevel.Info);
					// provision the in-app purchase to the user
					// (for example, credit 50 gold coins to player's character)
				}
				else {
					Logger.log("Android","Consume failed on "+purchase.getSku(), Logger.LogLevel.Info);
					// handle error
				}
			}
		};

	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener
			= new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

			if (result.isFailure()) {
				// handle error here
				Logger.log("Android", "Query inventory failed: "+result, Logger.LogLevel.Error);
			}else {
                List<Purchase> list = new ArrayList<Purchase>();
				if(inventory.hasPurchase(SKU_DONATE_SMALL)) {
                    Logger.log("Android", "Trying to consume " + SKU_DONATE_SMALL, Logger.LogLevel.Info);
                    list.add(inventory.getPurchase(SKU_DONATE_SMALL));
                }
                if(inventory.hasPurchase(SKU_DONATE_MEDIUM)) {
                    Logger.log("Android", "Trying to consume " + SKU_DONATE_MEDIUM, Logger.LogLevel.Info);
                    list.add(inventory.getPurchase(SKU_DONATE_MEDIUM));
                }
                if(inventory.hasPurchase(SKU_DONATE_LARGE)) {
                    Logger.log("Android", "Trying to consume " + SKU_DONATE_LARGE, Logger.LogLevel.Info);
                    list.add(inventory.getPurchase(SKU_DONATE_LARGE));
                }
                if(inventory.hasPurchase(SKU_DONATE_HUGE)) {
                    Logger.log("Android", "Trying to consume " + SKU_DONATE_HUGE, Logger.LogLevel.Info);
                    list.add(inventory.getPurchase(SKU_DONATE_HUGE));
                }

                if(list.size() > 0)
                    try {
                        Logger.log("Android", "Calling multi consume.", Logger.LogLevel.Info);
                        mHelper.consumeAsync(list, mConsumedMultiListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {
                        e.printStackTrace();
                    }

                // update UI accordingly
			}
		}
	};

    private IabHelper.OnConsumeMultiFinishedListener mConsumedMultiListener = new IabHelper.OnConsumeMultiFinishedListener() {
        @Override
        public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
            for(int i = 0; i < results.size(); i++){
                IabResult result = results.get(i);
                Purchase purchase = purchases.get(i);
                if(result.isFailure()){
                    Logger.log("Android", "Consume failed on purchase: "+purchase.getSku()+", result: "+result, Logger.LogLevel.Error);
                }else{
                    Logger.log("Android", "Consume success on purchase: "+purchase.getSku(), Logger.LogLevel.Info);
                }
            }
        }
    };

	@Override
	public void exit() {
		super.exit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mHelper != null) try {
			mHelper.dispose();
		} catch (IabHelper.IabAsyncInProgressException e) {
			e.printStackTrace();
		}
		mHelper = null;
	}

	@NotNull
	@Override
	public String getCurrDeviceID() {
		return Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	@Override
	public boolean isTestDevice() {
		return testDevices.contains(getCurrDeviceID());
	}

	@Override
	public void displayText(final String text, final String duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(duration.equals("short"))
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                }
            }
        });
	}

    @Override
    public synchronized void outputToLog(String fileName, String[] text) {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

		System.out.println("Writing to output log.");

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        File dirToWriteTo;
        if(mExternalStorageAvailable && mExternalStorageWriteable)
            dirToWriteTo = Environment.getExternalStorageDirectory(); //Get the SDCard directory
        else
            dirToWriteTo = Environment.getDataDirectory(); //Get the SDCard directory

        final File dir = new File(dirToWriteTo.getAbsolutePath() + "/rx2020_output/"); //Make the rx2020_output directory
        boolean mkDir = dir.mkdirs(); //Make the dirs
        final File logFile = new File(dir, fileName); //Make the log file
        try {
            FileWriter writer = new FileWriter(logFile);
            // Writes the content to the file
            for(String t : text)
                writer.write(t+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            displayText("There was an error writing to the file " + fileName+". Tried to write to "+logFile.getAbsolutePath()+" and mdDir was "+mkDir, "short");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            displayText(sw.toString(), "long");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Apparently we really need this or we can't buy anything else until the game is restarted.
        if (mHelper != null) {
            // Pass on the activity result to the helper for handling
            if (mHelper.handleActivityResult(requestCode, resultCode, data)) {
                Logger.log("IAB", "onActivityResult handled by IABUtil.", Logger.LogLevel.Info);
            }
        }
    }
}
