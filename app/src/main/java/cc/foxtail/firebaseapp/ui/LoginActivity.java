package cc.foxtail.firebaseapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.foxtail.firebaseapp.R;
import cc.foxtail.firebaseapp.model.UserModel;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.layout)
    RelativeLayout layout;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.email_edit)
    EditText emailEdit;

    @BindView(R.id.password_edit)
    EditText passwordEdit;

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.google_login_button)
    SignInButton googleLoginButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();

                String email = emailEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                Task<AuthResult> authResultTask = auth.signInWithEmailAndPassword(email, password);
                authResultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        hideProgressBar();

                        String email = authResult.getUser().getEmail();

                        UserModel userModel = UserModel.getInstance();
                        userModel.setEmail(email);

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

                authResultTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        Snackbar.make(layout, e.getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);
        googleLoginButton.setEnabled(false);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        loginButton.setEnabled(true);
        googleLoginButton.setEnabled(true);

    }
}