package com.eightydegreeswest.irisplus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.apiv2.DeviceApi;
import com.eightydegreeswest.irisplus.apiv2.IrisApi;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	//private IrisPlusLogger logger = new IrisPlusLogger();

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	//private static final int REQUEST_LINK_TO_DBX = 999;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//dropboxUtil = new DropboxUtil(this);
		setContentView(R.layout.activity_login);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		/* Dropbox stuff
		try {

			DbxAccountManager mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), IrisPlusConstants.PREF_DROPBOX_APP_KEY(), IrisPlusConstants.PREF_DROPBOX_APP_SECRET());
			
			if(!mDbxAcctMgr.hasLinkedAccount()) {
				mDbxAcctMgr.startLink((Activity) this, REQUEST_LINK_TO_DBX);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		// Set up the login form.
		mUsername = "";
		mUsernameView = (EditText) findViewById(R.id.username);
		mUsernameView.setText(mUsername);

        TextView versionNumber = (TextView) findViewById(R.id.version_number);
        PackageInfo pInfo = null;
        try {
            Calendar cal = Calendar.getInstance();
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionNumber.setText("v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		Intent startIntent = getIntent();
		if(startIntent != null) {
			String error = startIntent.getStringExtra("error");
			if(error != null && !"".equals(error)) {
				mUsernameView.setError(getString(R.string.error_invalid_username));
				mPasswordView.setError(getString(R.string.error_incorrect_password));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString().trim();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username.
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} else if (mUsername.length() < 4) {
			mUsernameView.setError(getString(R.string.error_invalid_username));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this.getApplicationContext());
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		private Context mContext = null;
		private List<DeviceItem> devices;
		
		public UserLoginTask(Context context) {
			mContext = context;
		}
		@SuppressWarnings("WrongThread")
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		    //String username = prefs.getString(IrisPlusConstants.PREF_USERNAME, "");
		    //String password = prefs.getString(IrisPlusConstants.PREF_PASSWORD, "");
			
			String username = mUsernameView.getText().toString().trim();
			String password = mPasswordView.getText().toString();
			Log.w(null, "Trying to login using " + username);
			
			IrisApi iris = new IrisApi(mContext, username, password);
			String token = iris.getIrisAuthToken();
			String hubID = iris.getHubID();
			Log.w(null, "\n\n\n\nSESSION ID: " + token + "\n\n\n\n");

			if(token != null && !"".equals(token)) {
				SharedPreferences.Editor editor = mSharedPrefs.edit();
				editor.putString(IrisPlusConstants.PREF_USERNAME, username);
				editor.putString(IrisPlusConstants.PREF_PASSWORD, password);
                editor.putString(IrisPlusConstants.PREF_PIN, "");
				editor.putString(IrisPlusConstants.PREF_TOKEN, token);
				editor.putString(IrisPlusConstants.PREF_HUB_ID, hubID);
				editor.commit();

				//Load devices for left nav
				DeviceApi deviceApi = new DeviceApi(mContext);
				devices = deviceApi.getHomeStatus();
				this.performNavDrawerCommand();
				return true;
			} else {
				Intent login = new Intent(mContext, LoginActivity.class);
				login.putExtra("error", "Invalid credentials!");
				startActivity(login);
			}
			
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}

		protected void performNavDrawerCommand() {
			try {
				if (devices.size() > 0) {
					Collections.sort(devices, new Comparator<DeviceItem>() {
						@Override
						public int compare(final DeviceItem object1, final DeviceItem object2) {
							return object1.getDeviceName().compareTo(object2.getDeviceName());
						}
					} );
				}

				try {
					//Cache list
					FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-nav-list.dat", Context.MODE_PRIVATE);
					ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
					objectOutputStream.writeObject(devices);
					objectOutputStream.close();
				} catch (Exception cacheException) {
					//Ignore
					cacheException.printStackTrace();
				}
				//mNavFragment.refreshFragment();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        //dropboxUtil.resume();
    }
}
