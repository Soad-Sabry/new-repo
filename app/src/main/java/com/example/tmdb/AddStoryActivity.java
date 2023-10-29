package com.example.tmdb;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {
    private Uri mImageUri;
    String miUrlOk = "";

    private StorageTask uploadTask;

    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference= FirebaseStorage.getInstance().getReference("story");

        // تغيير استخدام UCrop بدلاً من CropImage
        UCrop.of(mImageUri, Uri.fromFile(new File(getCacheDir(), "temp_image.jpg")))
                .withAspectRatio(9, 16)
                .start(AddStoryActivity.this);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

//    private void publishStory(){
//        ProgressDialog pd = new ProgressDialog(this);
//        pd.setMessage("Posting");
//        pd.show();
//
//        if (mImageUri != null){
//            StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "" + getFileExtension(mImageUri));
//            StorageTask storageTask = imageReference.putFile(mImageUri);
//            storageTask.continueWithTask(new Continuation() {
//                @Override
//                public Object then(@NonNull Task task) throws Exception {
//                    if (!task.isSuccessful()){
//                        throw task.getException();
//                    }
//                    return imageReference.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()){
//                        Uri downloadUri = task.getResult();
//                        mUri = downloadUri.toString();
//                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
//                                .child(myId);
//                        String storyId = reference.push().getKey();
//                        long timeEnd = System.currentTimeMillis() + 86400000;
//
//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("imageUri", mUri);
//                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
//                        hashMap.put("timeEnd", timeEnd);
//                        hashMap.put("storyId", storyId);
//                        hashMap.put("userId", myId);
//
//                        reference.child(storyId).setValue(hashMap);
//                        pd.dismiss();
//                        finish();
//                    } else {
//                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        } else {
//            Toast.makeText(this, "No image selected!", Toast.LENGTH_LONG).show();
//        }
//    }
//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            mImageUri = UCrop.getOutput(data);

            // تحميل الصورة بعد القص باستخدام UCrop
            uploadImage_10();
        } else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }
    }
    private void uploadImage_10(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
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
                        miUrlOk = downloadUri.toString();

                        String myid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                                .child(myid);

                        String storyid = reference.push().getKey();
                        long timeend = System.currentTimeMillis()+86400000; // 1 day later

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageurl", miUrlOk);
                        hashMap.put("timestart", ServerValue.TIMESTAMP);
                        hashMap.put("timeend", timeend);
                        hashMap.put("storyid", storyid);
                        hashMap.put("userid", myid);

                        reference.child(storyid).setValue(hashMap);

                        pd.dismiss();

                        finish();

                    } else {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(AddStoryActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
   }

}
