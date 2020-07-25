package com.fuelio.fuelio;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]


    private GoogleSignInClient mGoogleSignInClient;

    private AppCompatEditText txtPhone;
    ProgressDialog progress;
    CardView cardError;
    CountryCodePicker ccp;

    CardView cv1,cv2;
    TextView time;
    Pinview etOTP;
    public static String phoneNumber, otp;
    FancyButton btnSignIn,btnLogin;
    Button resend;
    FirebaseAuth auth;


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    private String verificationCode,token;

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;


    public static Activity fa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_layout);
//        Intent intent = new Intent(this, IntroActivity.class);
//        startActivity(intent);

        time= findViewById(R.id.timer);


        btnSignIn=findViewById(R.id.btnPassWord);
        resend= findViewById(R.id.resend);
        etOTP=findViewById(R.id.pinView);
        cv1= findViewById(R.id.card_view);
        cv2= findViewById(R.id.card_view2);


        if(!new PrefManager(this).getPhone().equals("none")){

            Intent in = new Intent(getApplicationContext(), RegisterActivity.class);
            in.putExtra("user", false);
            in.putExtra("phone", new PrefManager(this).getPhone());

            startActivity(in);
            finish();
        }


        StartFirebaseLogin();

//        Crashlytics.getInstance().crash();

        //setContentView(R.layout.activity_login);
        btnLogin=findViewById(R.id.btnLogin);
        txtPhone=findViewById(R.id.txtPhone);
        cardError=findViewById(R.id.cardError);
        ccp =  findViewById(R.id.ccp);
        ccp.detectLocaleCountry(true);
        ccp.detectNetworkCountry(true);
        ccp.detectSIMCountry(true);

        ccp.registerCarrierNumberEditText(txtPhone);
        askPermission();
        listenNetwork();
        fa=this;

        if(getIntent().getStringExtra("phone")!=null){
            String phn=getIntent().getStringExtra("phone");
            txtPhone.setText(phn);
        }

        //--------------------------------------------------------------------------------------------------

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();


        btnLogin.setOnClickListener(v -> {

            if (ccp.isValidFullNumber()) {
                String phne=txtPhone.getText().toString();
               phoneNumber=ccp.getFullNumberWithPlus();

                show();
            }
            else
            {
                Toasty.error(getApplicationContext(), "Please enter a valid Phone", Toasty.LENGTH_LONG).show();
            }});


        btnSignIn.setOnClickListener(v -> {

            if(!etOTP.getValue().equals("")) {
                progress = ProgressDialog.show(LoginActivity.this, "", "Verifying...", false, false);

                otp= etOTP.getValue();
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, otp);

                SigninWithPhone(credential);
            }
            else
            {
                Toasty.error(getApplicationContext(),"Please enter verification code first",Toasty.LENGTH_LONG).show();
            }
        });

        resend.setOnClickListener(v -> show());

    }



    //FUNCTION TO LISTEN FOR NETWORK
    public void listenNetwork()
    {
        final int[] num = {0};
        final Timer timers  = new Timer();
        timers.schedule(new TimerTask(){
            @Override
            public void run() {
                runOnUiThread(() -> {
                    // final Toast toast  = Toasty.error(getApplicationContext(), ++num[0] +"",Toasty.LENGTH_SHORT );
                    //Toasty.show();
                    if(isNetworkAvailable())
                    {
                        btnLogin.setEnabled(true);
                        cardError.setVisibility(View.INVISIBLE);
                    }
                    else
                    {
                        btnLogin.setEnabled(false);
                        cardError.setVisibility(View.VISIBLE);
                    }
                });
            }
        },0,5000);
    }

    //FUNCTION TO ASK FOR PERMISSION
    public void askPermission() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                //call the function to ask for permission
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){

        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
        }
    }

    //////////////////////////////////////
    private void signIn() {

        Toasty.info(LoginActivity.this,"Preparing...",Toasty.LENGTH_SHORT).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Toasty.success(LoginActivity.this,"sign in successfully",Toasty.LENGTH_SHORT).show();

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toasty.error(LoginActivity.this,"Error occurred.",Toasty.LENGTH_LONG).show();

                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        progress=ProgressDialog.show(LoginActivity.this,"","Signing...",false,false);

        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progress.dismiss();
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        progress.dismiss();
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.mainLayout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // [START_EXCLUDE]

                    // [END_EXCLUDE]
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {

            if (user.isEmailVerified()) {
//                creator cr = new creator();
//                cr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user.getEmail(), user.getDisplayName(), user.getUid(), user.getPhotoUrl().toString(), user.getPhoneNumber());
//
                Snackbar.make(findViewById(R.id.mainLayout), "Authenticated.", Snackbar.LENGTH_SHORT).show();

                  phoneNumber=user.getPhoneNumber();

                if(user.getPhoneNumber()!=null) {
                    if(!user.getPhoneNumber().equals("")){
                        loginAccount();
                    }else {
                        Snackbar.make(findViewById(R.id.mainLayout), "Please use phone number, your email doesn't have phone", Snackbar.LENGTH_SHORT).show();

                    }
                }else{
                    Snackbar.make(findViewById(R.id.mainLayout), "Please use phone number, your email doesn't have phone", Snackbar.LENGTH_SHORT).show();

                }


            }
            else {
                Toasty.info(LoginActivity.this,"Verify email...",Toasty.LENGTH_LONG).show();

            }

        }
        else
        {


        }
    }


    //function to login
    public void loginAccount()
    {
        phoneNumber=ccp.getFullNumber().trim();

        progress=ProgressDialog.show(LoginActivity.this,"","Checking user...",false,false);

        CollectionReference userRef= FirebaseFirestore.getInstance().collection("users");
        Query userQuery=userRef.whereEqualTo("phone",phoneNumber);
        userQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {

            if(queryDocumentSnapshots.isEmpty()){
                Toasty.warning(getApplicationContext(),"User not registered").show();

                new PrefManager(getApplicationContext()).setPhone(phoneNumber);

                Intent in=new Intent(getApplicationContext(),RegisterActivity.class);
                        in.putExtra("user",false);
                        in.putExtra("phone",phoneNumber.replace("+",""));

                        startActivity(in);
                        finish();

            }else{

                Toasty.success(getApplicationContext(),"Your account is found.").show();

                List<DocumentSnapshot> doc=queryDocumentSnapshots.getDocuments();
                String id=doc.get(0).getId();
                String fnm=(String)doc.get(0).get("first_name");
                String lnm=(String)doc.get(0).get("last_name");

                String tp=(String)doc.get(0).get("type");

                if (tp != null && tp.equals("station")) {
                    String st = (String) doc.get(0).get("station");

                    new PrefManager(getApplicationContext()).setRegistrationtoken(st);
                    new PrefManager(getApplicationContext()).setType("station");
                }

                new PrefManager(getApplicationContext()).setPhone(phoneNumber);
                new PrefManager(getApplicationContext()).saveUser(id,fnm+" "+lnm,tp);
                new PrefManager(getApplicationContext()).setFirstTimeLaunch(false);


                Intent in = null;
                if (tp != null) {
                    if(tp.equals("station")){
                        in=new Intent(getApplicationContext(), StationActivity.class);
                    }else {
                        in = new Intent(getApplicationContext(), MainActivity.class);
                    }
                }else{
                    in=new Intent(getApplicationContext(),MainActivity.class);
                }

                in.putExtra("user",true);
                in.putExtra("phone",phoneNumber.replace("+",""));

                startActivity(in);
                finish();
            }
        }).addOnFailureListener(e -> Toasty.error(getApplicationContext(),"Error getting your info").show()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                progress.cancel();

            }
        });
    }

    public boolean isNetworkAvailable() {
        try
        {
            ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    private void SigninWithPhone(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        try {
                            progress.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        Toasty.success(getApplicationContext(),"your phone verified successfully",Toasty.LENGTH_SHORT).show();
                        loginAccount();

                        //---------------------------------
                    } else {
                        progress.dismiss();
                        //Snackbar.make(rl,"Invalid code,enter valid one please",Snackbar.LENGTH_LONG).show();
                        Toasty.error(LoginActivity.this,"Invalid verification code",Toasty.LENGTH_SHORT).show();
                    }
                });

    }

    private void StartFirebaseLogin() {

        auth = FirebaseAuth.getInstance();
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(final PhoneAuthCredential phoneAuthCredential) {

                cv2.setVisibility(View.VISIBLE);
                cv1.setVisibility(View.GONE);
                Toasty.info(LoginActivity.this,"Initializing auto verify...",Toasty.LENGTH_SHORT).show();
                progress.dismiss();

                progress=ProgressDialog.show(LoginActivity.this,"","Auto verify...",false,false);

                new CountDownTimer(3000,1000)
                {
                    @Override
                    public void onTick(long l) {

                    }
                    @Override
                    public void onFinish() {
                        SigninWithPhone(phoneAuthCredential);

                    }
                }.start();

            }

            @Override
            public void onVerificationFailed(@NotNull FirebaseException e) {
                Toasty.error(LoginActivity.this,"Verification failed",Toasty.LENGTH_LONG).show();
                progress.dismiss();
            }

            @Override
            public void onCodeSent(@NotNull String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationCode = s;
                token=forceResendingToken.toString();

                Toasty.info(LoginActivity.this,"Verification code sent",Toasty.LENGTH_SHORT).show();
                cv2.setVisibility(View.VISIBLE);
                cv1.setVisibility(View.GONE);
                progress.dismiss();

                new CountDownTimer(60000,1000)
                {

                    @Override
                    public void onTick(long l) {

                        time.setText(String.format("Resend code in: %d", l / 1000));
                    }

                    @Override
                    public void onFinish() {

                        time.setText("Click to resend code.");
                        resend.setVisibility(View.VISIBLE);
                    }
                }.start();


            }
            @Override
            public void onCodeAutoRetrievalTimeOut(@NotNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);

                Toasty.warning(LoginActivity.this,"Auto verify timed out",Toasty.LENGTH_SHORT).show();
            }
        };
    }

    public void show()
    {
        progress = ProgressDialog.show(LoginActivity.this, "", "Loading...", false, false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                     // Phone number to verify
                60,                           // Timeout duration
                TimeUnit.SECONDS,                // Unit of timeout
                LoginActivity.this,        // Activity (for callback binding)
                mCallback);// OnVerificationStateChangedCallbacks

        StartFirebaseLogin();
    }
    ////////////////////////////////////////////////
//////////////////////////////////////////////////////////
    @Override
    protected void onPause() {
        super.onPause();

    }
}
