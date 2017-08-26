package cc.foxtail.firebaseapp.model;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Chat {
    final String message;
    final String timestamp;
    final String imageURL;

    public Chat() {
        this("", "", "");
    }

    public Chat(String message, String timestamp) {
        this(message, timestamp, "");
    }

    public Chat(String message1234, String timestamp1234, String imageURL) {
        this.message = message1234;
        this.timestamp = timestamp1234;
        this.imageURL = imageURL;
    }

    public static Chat newChat(String message) {
        return new Chat(message, timestamp());
    }

    public static Chat newChatWithImage(String message, String imageUrl) {
        return new Chat(message, timestamp(), imageUrl);
    }

    private static String timestamp() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("a h:mm", Locale.KOREA);
        return dateFormat.format(date);
    }
}


public class ChatModel {
    private DatabaseReference ref;
    private List<Chat> chats = new ArrayList<>();
    private OnDataChangedListener onDataChangedListener;

    private StorageReference storageReference;

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.onDataChangedListener = listener;
    }

    public ChatModel() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://ourguide-app.appspot.com").child("images");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.ref = database.getReference();
        this.ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Chat> newChats = new ArrayList<Chat>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                for (DataSnapshot e : children) {
                    Chat chat = e.getValue(Chat.class);
                    newChats.add(chat);
                }

                chats = newChats;
                if (onDataChangedListener != null) {
                    onDataChangedListener.onDataChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });
    }

    private String gererateTempFilename() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }


    public void uploadImage(InputStream is, final OnUploadImageListener listener) {
        UploadTask task = storageReference.child(gererateTempFilename()).putStream(is);
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String imageUrl = taskSnapshot.getDownloadUrl().toString();
                listener.onSuccess(imageUrl);
            }
        });
    }

    public void sendMessage(String message) {
        DatabaseReference childRef = ref.push();
        childRef.setValue(Chat.newChat(message));
    }

    public void sendMessageWithImage(String message, String imageUrl) {
        DatabaseReference childRef = ref.push();
        childRef.setValue(Chat.newChatWithImage(message, imageUrl));
    }

    public String getMessage(int position) {
        return chats.get(position).message;
    }

    public String getImageURL(int position) {
        return chats.get(position).imageURL;
    }

    public int getMessageCount() {
        return chats.size();
    }
}











