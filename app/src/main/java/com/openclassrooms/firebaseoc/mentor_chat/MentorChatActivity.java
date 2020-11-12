package com.openclassrooms.firebaseoc.mentor_chat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.openclassrooms.firebaseoc.R;
import com.openclassrooms.firebaseoc.api.MessageHelper;
import com.openclassrooms.firebaseoc.api.UserHelper;
import com.openclassrooms.firebaseoc.base.BaseActivity;
import com.openclassrooms.firebaseoc.models.Message;
import com.openclassrooms.firebaseoc.models.User;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MentorChatActivity extends BaseActivity implements mentorChatAdapter.Listener {


    // FOR DESIGN
    // 1 - Getting all views needed
    @BindView(R.id.activity_mentor_chat_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.activity_mentor_chat_text_view_recycler_view_empty)
    TextView textViewRecyclerViewEmpty;
    @BindView(R.id.activity_mentor_chat_message_edit_text)
    TextInputEditText editTextMessage;
    @BindView(R.id.activity_mentor_chat_image_chosen_preview)
    ImageView imageViewPreview;


    // FOR DATA
    // 2 - Declaring Adapter and data
    private mentorChatAdapter chatAdapter;
    @Nullable
    private User modelCurrentUser;
    private String currentChatName;

    // STATIC DATA FOR CHAT (3)
    private static final String CHAT_NAME_ANDROID = "android";
    private static final String CHAT_NAME_BUG = "bug";
    private static final String CHAT_NAME_FIREBASE = "firebase";

    // STATIC DATA FOR PICTURE
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private Uri uriImageSelected;
//    FirebaseStorage storage = FirebaseStorage.getInstance();



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Envoyez resultat a EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MentorChatActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.configureRecyclerView(CHAT_NAME_ANDROID);
        this.configureToolbar();
        this.getCurrentUserFromFirestore();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.handleResponse(requestCode, resultCode, data);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_mentor_chat; }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.activity_mentor_chat_send_button)
    public void onClickSendMessage() {
        if(!TextUtils.isEmpty(editTextMessage.getText().toString()) && modelCurrentUser != null){
            if(this.imageViewPreview.getDrawable() == null){
                // Envoyez message texte
                MessageHelper.createMessageForChat(editTextMessage.getText().toString(), this.currentChatName, modelCurrentUser)
                        .addOnFailureListener(this.onFailureListener());
                this.editTextMessage.setText("");

            }else{
                // Methode a appeler
                this.uploadPhotoInFirebaseAndSendMessage(editTextMessage.getText().toString());
                this.editTextMessage.setText("");
                this.imageViewPreview.setImageDrawable(null);
            }

        }
    }

    @OnClick({ R.id.activity_mentor_chat_android_chat_button, R.id.activity_mentor_chat_firebase_chat_button, R.id.activity_mentor_chat_bug_chat_button})
    public void onClickChatButtons(ImageButton imageButton) {
        // 8 - Re-Configure the RecyclerView depending chosen chat
        switch (Integer.valueOf(imageButton.getTag().toString())){
            case 10:
                this.configureRecyclerView(CHAT_NAME_ANDROID);
                break;
            case 20:
                this.configureRecyclerView(CHAT_NAME_FIREBASE);
                break;
            case 30:
                this.configureRecyclerView(CHAT_NAME_BUG);
                break;
        }
    }

    @OnClick(R.id.activity_mentor_chat_add_file_button)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        this.ChooseImageFile();
    }

    private void ChooseImageFile(){
        PermissionListener permission = new PermissionListener() {
            //Methode permettant d'executer des actions apres que la permission ai été accordée
            @Override
            public void onPermissionGranted() {

                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Autre methode pour recuperer PLUSIEURS images dans le telephone
                /*gallery.setType("image/*");
                gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                gallery.setAction(Intent.ACTION_GET_CONTENT);*/
                startActivityForResult(gallery, RC_IMAGE_PERMS);
            }
            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };
        //Gestion des permissions
        TedPermission.with(MentorChatActivity.this)
                .setPermissionListener(permission)
                .setPermissions(PERMS)
                .check();

    }
    //Methode permettant de gerer ce qui se passera apres la selection de l'image
    private void handleResponse(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == RC_IMAGE_PERMS){
            if (resultCode == RESULT_OK){
                this.uriImageSelected = data.getData();
                Glide.with(this)
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(this.imageViewPreview);
            }
            else{
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --------------------
    // REST REQUESTS
    // --------------------
    // Get Current User from Firestore
    private void getCurrentUserFromFirestore(){
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> modelCurrentUser = documentSnapshot.toObject(User.class));
    }
    // Upload image and message on the firebase
    private void uploadPhotoInFirebaseAndSendMessage(final String message){
        String uuid = UUID.randomUUID().toString(); // Generer un string unique
        //StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference imagesRef = storageReference.child("images");

        UploadTask uploadTask = imagesRef.putFile(this.uriImageSelected);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });


        /*mImageRef.putFile(this.uriImageSelected)
                .addOnSuccessListener(this, taskSnapshot -> {
                    *//*String pathImageSavedInFirebase =
                            taskSnapshot.getMetadata().getDownloadUrl().toString();*//*
                    // Enregistrement du message
                   *//* MessageHelper.createMessageWithImageForChat(pathImageSavedInFirebase, message,
                            currentChatName, modelCurrentUser).addOnFailureListener(onFailureListener());*//*
                })
                .addOnFailureListener(this.onFailureListener());*/
    }


    // --------------------
    // UI
    // --------------------
    // 5 - Configure RecyclerView with a Query
    private void configureRecyclerView(String chatName){
        //Track current chat name
        this.currentChatName = chatName;
        //Configure Adapter & RecyclerView
        this.chatAdapter = new mentorChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChat(this.currentChatName)), Glide.with(this), this, this.getCurrentUser().getUid());
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(chatAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(this.chatAdapter);
    }

    // 6 - Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();
    }

    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {
        // 7 - Show TextView in case RecyclerView is empty
        textViewRecyclerViewEmpty.setVisibility(this.chatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
