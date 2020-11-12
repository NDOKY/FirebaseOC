package com.openclassrooms.firebaseoc;

import android.content.Intent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.openclassrooms.firebaseoc.api.UserHelper;
import com.openclassrooms.firebaseoc.auth.ProfileActivity;
import com.openclassrooms.firebaseoc.base.BaseActivity;
import com.openclassrooms.firebaseoc.mentor_chat.MentorChatActivity;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
//FOR DESIGN
    // 1 - Get Coordinator Layout

    @BindView(R.id.main_activity_coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.main_activity_button_login)
    Button buttonLogin;

    @Override
    protected void onResume(){
        super.onResume();
        //Update UI when activity is resuming
        this.updateUIWhenResuming();
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_main; }

    //FOR DATA
    // 1- Identifier for sign-In Activity
    private static final int RC_SIGN_IN = 123;

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.main_activity_button_login)
    public void onClickLoginButton()
    {
        if (this.isCurrentUserLogged())
            this.startProfileActivity();
        else
            this.startSignInActivity();
    }

    @OnClick(R.id.main_activity_button_chat)
    public void onClickChatButton(){
        if(this.isCurrentUserLogged()){
            this.startMentorChatActivity();
        }else{
            this.showSnackBar(this.coordinatorLayout, getString(R.string.error_not_connected));
        }
    }

    // --------------------
    // NAVIGATION
    // --------------------
    // 2 - Launch Sign-In Activity
    private void startSignInActivity()
    {
        startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()//FireBase permet d'obtenir l'interface de connexion
                    .setTheme(R.style.LoginTheme)
                    .setAvailableProviders(
                            Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build())
                    )
                    .setIsSmartLockEnabled(false, true)
                    .setLogo(R.drawable.ic_logo_auth)
                    .build(),

        RC_SIGN_IN);
    }

    //Launching Profile Activity
    private void startProfileActivity(){
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void startMentorChatActivity(){
        Intent intent = new Intent(this, MentorChatActivity.class);
        startActivity(intent);
    }

    // -----------------
    // UI
    // -----------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 4 - Handle SignIn Activity response on activity result

        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // 2 - Show Snack Bar with a message
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }
    // ---------------
    // REST REQUEST
    // ---------------

    private void createUserInFirestore(){

        if(this.getCurrentUser() != null){
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ?
                    this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
        }
    }
    // 3 - Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                this.createUserInFirestore();
                showSnackBar(this.coordinatorLayout, getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // Update UI when activity is resuming
    private void updateUIWhenResuming(){
        this.buttonLogin.setText(this.isCurrentUserLogged() ?
                getString(R.string.button_login_text_logged) : getString(R.string.button_login_text_not_logged));
    }



}
