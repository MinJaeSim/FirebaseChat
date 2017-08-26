package cc.foxtail.firebaseapp.ui;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.FileNotFoundException;
import java.io.InputStream;

import cc.foxtail.firebaseapp.R;
import cc.foxtail.firebaseapp.model.ChatModel;
import cc.foxtail.firebaseapp.model.OnDataChangedListener;
import cc.foxtail.firebaseapp.model.OnUploadImageListener;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 100;

    private ChatModel model = new ChatModel();
    private Uri currentImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText chatEdit = (EditText) findViewById(R.id.chat_edit);

        Button chatButton = (Button) findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message = chatEdit.getText().toString();
                if (message.length() > 0) {
                    if (currentImageUri != null) {
                        try {
                            ContentResolver resolver = getContentResolver();
                            InputStream is = resolver.openInputStream(currentImageUri);
                            currentImageUri = null;

                            model.uploadImage(is, new OnUploadImageListener() {
                                @Override
                                public void onSuccess(String url) {
                                    model.sendMessageWithImage(message, url);
                                }
                                @Override
                                public void onFail() {
                                    //...
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        model.sendMessage(message);
                    }
                }
            }
        });

        Button photoButton = (Button) findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(new RecyclerView.Adapter<ChatHolder>() {
            @Override
            public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                View view = layoutInflater.inflate(R.layout.recycler_item_chat, parent, false);
                return new ChatHolder(view);
            }

            @Override
            public void onBindViewHolder(ChatHolder holder, int position) {
                String message = model.getMessage(position);
                holder.setText(message);

                String imageUrl = model.getImageURL(position);
                holder.setImage(imageUrl);
            }

            @Override
            public int getItemCount() {
                return model.getMessageCount();
            }
        });


        model.setOnDataChangedListener(new OnDataChangedListener() {
            @Override
            public void onDataChanged() {
                recyclerView.getAdapter().notifyDataSetChanged();
                int count = recyclerView.getAdapter().getItemCount();
                recyclerView.scrollToPosition(count - 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == PICK_FROM_ALBUM) {
            currentImageUri = data.getData();
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setMessage("앱을 종료하시겠습니까?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.show();
    }

    class ChatHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        public ChatHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.chat_text_view);
            imageView = (ImageView) itemView.findViewById(R.id.chat_image_view);
        }

        public void setText(String text) {
            textView.setText(text);
        }

        // 공통성과 가변성의 분리 - 변하는 것과 변하지 않는 것은 분리되어야 한다.
        public void setImage(String imageUrl) {
            int visibility = imageUrl.isEmpty() ? View.GONE : View.VISIBLE;
            imageView.setVisibility(visibility);

            Glide.with(MainActivity.this)
                    .load(imageUrl)
                    .into(imageView);
        }
    }
}
