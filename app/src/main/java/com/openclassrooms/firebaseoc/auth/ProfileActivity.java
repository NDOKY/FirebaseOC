package com.openclassrooms.firebaseoc.auth;

import android.content.DialogInterface;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.firebaseoc.R;
import com.openclassrooms.firebaseoc.api.UserHelper;
import com.openclassrooms.firebaseoc.base.BaseActivity;
import com.openclassrooms.firebaseoc.models.User;

import butterknife.BindView;
import butterknife.OnClick;

public class ProfileActivity extends BaseActivity {
    //FOR DESIGN
    @BindView(R.id.profile_activity_imageview_profile)
    ImageView imageViewProfile;
    @BindView(R.id.profile_activity_edit_text_username)
    TextInputEditText textInputEditTextUsername;
    @BindView(R.id.profile_activity_text_view_email)
    TextView textViewEmail;
    @BindView(R.id.profile_activity_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.profile_activity_check_box_is_mentor)
    CheckBox checkBoxIsMentor;

    //FOR DATA
    // iDENTIFY EACH hTTP Request
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    //Identifier to identify REST REQUEST (Update username)
    private static final int UPDATE_USERNAME = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureToolbar();
        this.updateUIWhenCreating();

    }
    // --------------------
    // UI
    // --------------------

    // 1 - Update UI when activity is creating
    private void updateUIWhenCreating() {
        if (this.getCurrentUser() != null) {

            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }

            //Get email & username from firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ?
                    getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();

            this.textViewEmail.setText(email);
// TODO: iL y'a une erreur ici la trouver et la corriger
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    String username = TextUtils.isEmpty(currentUser.getUsername()) ?
                            getString(R.string.info_no_username_found) : currentUser.getUsername();
                    checkBoxIsMentor.setChecked(currentUser.getIsMentor());
                    textInputEditTextUsername.setText(username);
                }
            });

//        String username = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ?
//                getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            //this.textInputEditTextUsername.setText(username);
            //this.textViewEmail.setText(email);
        }
    }
//Create OnCompleteListener called after tasks ended
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin) {

        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case UPDATE_USERNAME:
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case SIGN_OUT_TASK:
                        finish();
                        break;
                    case DELETE_USER_TASK:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_profile;
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.profile_activity_button_update)
    public void onClickUpdateButton() {
        this.updateUsernameFirebase();
    }
    @OnClick(R.id.profile_activity_check_box_is_mentor)
    public void onClickCheckBoxIsMentor(){
        this.updateUserIsMentor();
    }

    //Adding request to button listeners
    @OnClick(R.id.profile_activity_button_sign_out)
    public void onClickSignOutButton() {
        this.signOutUserFromFirebase();
    }

    @OnClick(R.id.profile_activity_button_delete)
    public void onClickDeleteButton() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteUserFromFirebase();
                    }
                })
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();
    }

    // ----------------
    // REST REQUESTS
    // -----------------

    //Create http requests (SignOut & DELETE)

    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private void deleteUserFromFirebase(){
        if(this.getCurrentUser() != null){
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
        }
    }
//Update Username
    private void updateUsernameFirebase(){

        this.progressBar.setVisibility(View.VISIBLE);
        String username = this.textInputEditTextUsername.getText().toString();

        if (this.getCurrentUser() != null){
            if (!username.isEmpty() && !username.equals(getString(R.string.info_no_username_found))){
                UserHelper.updateUsername(username,
                        this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    //Update User Mentor (is or not)
    private void updateUserIsMentor(){
        if (this.getCurrentUser() != null){
            UserHelper.updateIsMentor(this.getCurrentUser().getUid(),
                    this.checkBoxIsMentor.isChecked()).addOnFailureListener(this.onFailureListener());
        }
    }
}
