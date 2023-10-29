package com.example.tmdb;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yalantis.ucrop.UCrop;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    ImageView close, image_profile;
    TextView save, tv_change;
    MaterialEditText fullname, username, bio;

    FirebaseUser firebaseUser;

    private Uri mImageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    StorageReference storageRef;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
//        close = findViewById(R.id.close);
//        image_profile = findViewById(R.id.image_profile);
//        save = findViewById(R.id.save);
//        tv_change = findViewById(R.id.tv_change);
//        fullname = findViewById(R.id.fullname);
//        username = findViewById(R.id.username);
//        bio = findViewById(R.id.bio);
//
//        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        storageRef = FirebaseStorage.getInstance().getReference("uploads");
//
//        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user=snapshot.getValue(User.class);
//                fullname.setText(user.getFullname());
//                username.setText(user.getUsername());
//                bio.setText(user.getBio());
//                Glide.with(getApplicationContext()).load(user.getImageurl()).into(image_profile);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                updateProfile(fullname.getText().toString(),
//                        username.getText().toString(),
//                        bio.getText().toString());
//            }
//        });
//
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//
//        tv_change.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // تحديد الملف الذي ترغب في قصه
//                UCrop.of(mImageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
//                        .withAspectRatio(1, 1) // تحديد نسبة القص المطلوبة
//                        .withMaxResultSize(400, 400) // تحديد حجم الناتج الأقصى
//                        .start(EditProfileActivity.this); // بدء عملية القص
//            }
//        });
//
//        image_profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // تحديد الملف الذي ترغب في قصه
//                UCrop.of(mImageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
//                        .withAspectRatio(1, 1)
//                        .withMaxResultSize(400, 400)
//                        .start(EditProfileActivity.this);
//            }
//        });
    }

    private void updateProfile(String fullname, String username, String bio){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", fullname);
        map.put("username", username);
        map.put("bio", bio);
        reference.updateChildren(map);
        Toast.makeText(EditProfileActivity.this, "Successfully updated!", Toast.LENGTH_SHORT).show();
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();
        if (mImageUri != null){
            final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            uploadTask = fileReference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String miUrlOk = downloadUri.toString();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map1 = new HashMap<>();
                        map1.put("imageurl", ""+miUrlOk);
                        reference.updateChildren(map1);
                        pd.dismiss();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);

            if (resultUri != null) {
                mImageUri = resultUri;
                uploadImage();
            } else {
                Toast.makeText(this, "Failed to crop the image", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
