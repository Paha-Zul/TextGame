package com.quickbite.rx2020;

import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.quickbite.rx2020.interfaces.IGPGServices;
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

public class AndroidLauncher extends AndroidApplication implements IGPGServices {
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
		if(amount != 0) {
			//Don't do anything right now.

//			try {
//				//Launch the purchase flow with a test SKU for now.
//				mHelper.launchPurchaseFlow(this, SKU_TEST_PURCHASED, RC_REQUEST, mPurchaseFinishedListener, "HANDLE_PAYLOADS");
//			} catch (IabHelper.IabAsyncInProgressException e) {
//				e.printStackTrace();
//			}
		}
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if ( purchase == null) return;
            Logger.log("IAB", "Purchase finished: " + result + ", purchase: " + purchase, Logger.LogLevel.Info);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                Logger.log("Android", "Error purchasing: "+result, Logger.LogLevel.Error);
                //complain("Error purchasing: " + result);
                //setWaitScreen(false);
                return;
            }
//            if (!verifyDeveloperPayload(purchase)) {
//                //complain("Error purchasing. Authenticity verification failed.");
//                //setWaitScreen(false);
//                return;
//            }

            Logger.log("IAB", "Purchase successful.", Logger.LogLevel.Info);


//            if (purchase.getSku().equals(SKU_TEST_PURCHASED)) {
//                // bought the premium upgrade!
//                Logger.log("IAB", "Purchase is premium upgrade. Congratulating user.", Logger.LogLevel.Info);
//				try {
//					mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//				} catch (IabHelper.IabAsyncInProgressException e) {
//					e.printStackTrace();
//				}
//
//				// Do what you want here maybe call your game to do some update
//                //
//                // Maybe set a flag to indicate that ads shouldn't show anymore
////                mAdsRemoved = true;
//
//            }
        }
    };

	IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
		new IabHelper.OnConsumeFinishedListener() {
			public void onConsumeFinished(Purchase purchase, IabResult result) {
				if (result.isSuccess()) {
					// provision the in-app purchase to the user
					// (for example, credit 50 gold coins to player's character)
				}
				else {
					// handle error
				}
			}
		};

	IabHelper.QueryInventoryFinishedListener mGotInventoryListener
			= new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

			if (result.isFailure()) {
				// handle error here
				Logger.log("Android", "Query inventory failed: "+result, Logger.LogLevel.Error);
			}
			else {
				// does the user have the premium upgrade?
//				if(inventory.hasPurchase(SKU_TEST_PURCHASED))
//					try {
//						mHelper.consumeAsync(inventory.getPurchase(SKU_TEST_PURCHASED), mConsumeFinishedListener);
//					} catch (IabHelper.IabAsyncInProgressException e) {
//						e.printStackTrace();
//					}

				// update UI accordingly
			}
		}
	};

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
    public void outputToLog(String fileName, String[] text) {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

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
}
