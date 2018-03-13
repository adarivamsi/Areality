package com.passionateburger.areality;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Created by adari on 3/11/2018.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = "Login Activity";

    private static final int RC_SIGN_IN = 9001;
    private static final String DESIGNER_TYPE = "user";
    // UI references.
    private ProgressBar mProgressView;
    private LinearLayout mContainerView;
    private LinearLayout mLoginView;
    private LinearLayout mRegisterView;
    private EditText mLogin_emailView;
    private EditText mLogin_passwordView;
    private EditText mregister_fullnameView;
    private EditText mregister_emailView;
    private EditText mregister_passwordView;
    private Button mlogin;
    private Button mregister;
    private Button mlogin_form;
    private Button mregister_form;
    private Button mforget_password;
    private ImageView mfacebook;
    private ImageView mgoogle;
    private ImageView mtwitter;

    //Facebook
    private CallbackManager callbackManager;
    //Google
    private GoogleApiClient mGoogleApiClient;
    //FireBase
    private FirebaseAuth mAuth;

    /**
     * Show hidden view by animation
     *
     * @param view The View You Want Show
     */
    public static void expand(final View view) {
        view.measure(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? WindowManager.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        animation.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    /**
     * Hide view by animation
     *
     * @param view The View You Want Hide
     */
    public static void collapse(final View view) {
        final int initialHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        animation.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //region Initialization
        mProgressView = (ProgressBar) findViewById(R.id.progressBar);
        mContainerView = (LinearLayout) findViewById(R.id.form);
        mLoginView = (LinearLayout) findViewById(R.id.login);
        mRegisterView = (LinearLayout) findViewById(R.id.register);
        mLogin_emailView = (EditText) findViewById(R.id.login_email);
        mLogin_passwordView = (EditText) findViewById(R.id.login_password);
        mregister_fullnameView = (EditText) findViewById(R.id.register_full_name);
        mregister_emailView = (EditText) findViewById(R.id.register_email);
        mregister_passwordView = (EditText) findViewById(R.id.register_password);
        mlogin = (Button) findViewById(R.id.login_btn);
        mregister = (Button) findViewById(R.id.register_btn);
        mlogin_form = (Button) findViewById(R.id.login_form_btn);
        mregister_form = (Button) findViewById(R.id.register_form_btn);
        mforget_password = (Button) findViewById(R.id.forget_password_btn);
        mfacebook = (ImageView) findViewById(R.id.facebook);
        mgoogle = (ImageView) findViewById(R.id.google);
        mtwitter = (ImageView) findViewById(R.id.twitter);
        //endregion

        //region ClickListener Setup
        mlogin.setOnClickListener(this);
        mregister.setOnClickListener(this);
        mlogin.setOnClickListener(this);
        mlogin_form.setOnClickListener(this);
        mregister_form.setOnClickListener(this);
        mforget_password.setOnClickListener(this);
        mfacebook.setOnClickListener(this);
        mgoogle.setOnClickListener(this);
        mtwitter.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        //endregion


        GoogleLogin();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_form_btn:
                collapse(mRegisterView);
                expand(mLoginView);
                break;
            case R.id.register_form_btn:
                collapse(mLoginView);
                expand(mRegisterView);
                break;
            case R.id.forget_password_btn:
                EmailForgetPassword();
                break;
            case R.id.login_btn:
                EmailLogin();
                break;
            case R.id.register_btn:
                EmailRegister();
                break;
            case R.id.facebook:
                LoginButton btn = new LoginButton(LoginActivity.this);
                FacebookLogin(btn);
                btn.performClick();
                break;
            case R.id.google:
                //GoogleLogin();
                break;
            case R.id.twitter:
                //TwitterLogin();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Facebook
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //Google
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "Error Google Signin ");
                Toast.makeText(LoginActivity.this, result.getStatus().getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showProgress(false);
        Toast.makeText(LoginActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
    }

    private void GoogleLogin() {
        findViewById(R.id.google).setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
            showProgress(true);
        });
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void FacebookLogin(LoginButton fbLoginButton) {
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.setReadPermissions("email", "public_profile");
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                showProgress(true);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // App code
                Toast.makeText(LoginActivity.this, "Login Canceled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(final FacebookException exception) {
                Log.d(TAG, "facebook:onError", exception);
                // App code
                Toast.makeText(
                        LoginActivity.this,
                        exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        showProgress(false);
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.\n" + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final FirebaseUser user = task.getResult().getUser();
                        FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                        FUSER.name = user.getDisplayName();
                        FUSER.email = user.getEmail();
                        FUSER.image_uri = user.getPhotoUrl().toString();
                        FUSER.type_id = DESIGNER_TYPE;
                        FUSER.Add(user.getUid());
                        showProgress(false);
                        startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                        finish();
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        showProgress(false);
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.\n" + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        final FirebaseUser user = task.getResult().getUser();
                        final FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                        FUSER.name = user.getDisplayName();
                        FUSER.email = user.getEmail();
                        FUSER.image_uri = user.getPhotoUrl().toString();
                        FUSER.type_id = DESIGNER_TYPE;
                        FUSER.Add(user.getUid());
                        showProgress(false);
                        startActivity(new Intent(LoginActivity.this,BaseActivity.class));
                        finish();
                    }
                });
    }

    /**
     * Email Login
     */
    private void EmailLogin() {

        if (isEmailValid(mLogin_emailView) && isPasswordValid(mLogin_passwordView)) {
            showProgress(true);
            mAuth.signInWithEmailAndPassword(mLogin_emailView.getText().toString(), mLogin_passwordView.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, BaseActivity.class));
                            finish();
                            showProgress(false);
                        } else {
                            showProgress(false);
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //region Validation

    /**
     * Email Register
     */
    private void EmailRegister() {

        if (isFullnameValid(mregister_fullnameView)
                && isEmailValid(mregister_emailView) && isPasswordValid(mregister_passwordView)) {
            showProgress(true);
            new FireBaseHelper.Users().Where(FireBaseHelper.Users.Table.Email, mregister_emailView.getText().toString(), Data -> {
                if (Data.size() == 0) {
                    mAuth.createUserWithEmailAndPassword(mregister_emailView.getText().toString(), mregister_passwordView.getText().toString())
                            .addOnCompleteListener(this, task -> {
                                if (task.isSuccessful()) {

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(mregister_fullnameView.getText().toString())
                                            .build();
                                    mAuth.signOut();
                                    mAuth.signInWithEmailAndPassword(mregister_emailView.getText().toString(), mregister_passwordView.getText().toString());
                                    final FirebaseUser user = task.getResult().getUser();
                                    user.updateProfile(profileUpdates);
                                    FireBaseHelper.Users FUSER = new FireBaseHelper.Users();
                                    FUSER.name = mregister_fullnameView.getText().toString();
                                    FUSER.email = user.getEmail();
                                    FUSER.image_uri = "";
                                    FUSER.type_id = DESIGNER_TYPE;
                                    FUSER.Add(user.getUid());
                                    showProgress(false);
                                    startActivity(new Intent(LoginActivity.this, BaseActivity.class));
                                    finish();
                                } else {
                                    showProgress(false);
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    mregister_emailView.setError("Email Already used");
                    mregister_emailView.requestFocus();
                }
            });

        }
    }

    /**
     * Forget Email Password
     */
    private void EmailForgetPassword() {

        if (isEmailValid(mLogin_emailView)) {
            showProgress(true);
            mAuth.sendPasswordResetEmail(mLogin_emailView.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showProgress(false);
                            Toast.makeText(LoginActivity.this, "Please,Check Your Email Address\nWe send You a password Reset Link",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            showProgress(false);
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(EditText emailview) {
        emailview.setError(null);
        String email = emailview.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailview.setError(getString(R.string.error_field_required));
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailview.setError(getString(R.string.error_invalid_email));
        } else return true;
        emailview.requestFocus();
        return false;
    }

    private boolean isPasswordValid(EditText passwordview) {
        passwordview.setError(null);
        String password = passwordview.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordview.setError(getString(R.string.error_field_required));
        } else if (password.length() < 6) {
            passwordview.setError(getString(R.string.error_invalid_password));
        } else return true;
        passwordview.requestFocus();
        return false;
    }

    //endregion

    private boolean isUsernameValid(EditText usernameview) {
        usernameview.setError(null);
        String username = usernameview.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameview.setError(getString(R.string.error_field_required));
        } else if (username.length() < 6) {
            usernameview.setError(getString(R.string.error_invalid_username));
        } else if (!username.matches("^[a-zA-Z0-9]+$")) {
            usernameview.setError(getString(R.string.error_invalid_username));
        } else return true;
        usernameview.requestFocus();
        return false;
    }

    private boolean isFullnameValid(EditText fullnameview) {
        fullnameview.setError(null);
        String fullname = fullnameview.getText().toString();
        if (TextUtils.isEmpty(fullname)) {
            fullnameview.setError(getString(R.string.error_field_required));
        } else if (fullname.length() < 6) {
            fullnameview.setError(getString(R.string.error_invalid_fullname));
        } else if (!fullname.matches("^[a-zA-Z- ]+$")) {
            fullnameview.setError(getString(R.string.error_invalid_fullname));
        } else return true;
        fullnameview.requestFocus();
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
        mContainerView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContainerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
