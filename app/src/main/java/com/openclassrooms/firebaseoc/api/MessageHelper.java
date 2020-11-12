package com.openclassrooms.firebaseoc.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.openclassrooms.firebaseoc.models.Message;
import com.openclassrooms.firebaseoc.models.User;

public class MessageHelper {

    private static final String COLLECTION_NAME = "message";

    //Le but de cette methode est de creer un message et le stocker dans la base de données
    public static Task<DocumentReference> createMessageWithImageForChat(String urLimage, String textMessage, String chat, User userSender){
        Message message = new Message(textMessage, userSender, urLimage);

        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }
    public static Task<DocumentReference> createMessageForChat(String textMessage, String chat, User userSender){
        Message message = new Message(textMessage, userSender);

        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }

    // --- GET ---

    public static Query getAllMessageForChat(String chat){
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated", Query.Direction.ASCENDING)
                .limit(50);
    }
}
