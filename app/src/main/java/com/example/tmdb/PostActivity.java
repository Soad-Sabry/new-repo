package com.example.tmdb;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Uri mImageUri;
    private String miUrlOk = "";
    private StorageReference storageRef;

    ImageView close, image_added;
    TextView post;
    EditText description;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE_REQUEST = 1;

    public PostActivity() {
    }

    public PostActivity(Uri mImageUri) {
        this.mImageUri = mImageUri;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageRef = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        uploadImage();
                                    }
                                });

                // Check for storage permission and request it if not granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
//        }

        if (mImageUri != null) {
            Log.d("UCrop", "Input Uri: " + mImageUri.toString());
            UCrop.of(mImageUri, Uri.fromFile(new File(getCacheDir(), "cropped_image")))
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(400, 400)
                    .start(PostActivity.this);
        } else {
            Log.e("UCrop", "Input Uri is null");
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImage() {
        // Check if the permission is granted

       // if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Posting");
            pd.show();
            if (mImageUri != null) {
                final StorageReference fileReference = storageRef.child(System.currentTimeMillis()
                        + "." + getFileExtension(mImageUri));

                StorageTask<UploadTask.TaskSnapshot> uploadTask = fileReference.putFile(mImageUri);
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

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                            String postid = reference.push().getKey();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("postid", postid);
                            hashMap.put("postimage", miUrlOk);
                            hashMap.put("description", description.getText().toString());
                            hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                            reference.child(postid).setValue(hashMap);

                            pd.dismiss();

                            startActivity(new Intent(PostActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(PostActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(PostActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
//        } else {
//            // Show a message to the user to notify that permissions are required to access storage
//            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
      // }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // الأذن ممنوح، يمكنك القيام بعملية اختيار الصورة وتقصيها
                openImagePicker(); // تنفيذ دالة لاختيار الصورة
            } else {
                Toast.makeText(this, "Storage permission is required to upload images.", Toast.LENGTH_SHORT).show();
                // يمكنك توجيه المستخدم للإعدادات لتفعيل الأذونة يدويًا
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                Log.e("TAG","data"+data.getData());
            }
            else {
                Toast.makeText(this,"Canot not select",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
