package com.ramazantiftik.diarybook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.ramazantiftik.diarybook.databinding.ActivityDiaryBinding;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DiaryActivity extends AppCompatActivity {

    private ActivityDiaryBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDiaryBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        registerLauncher();
        database=this.openOrCreateDatabase("MEMORIES",MODE_PRIVATE,null);

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        if(info.matches("new")){
            //new memory
            binding.memoryText.setText("");
            binding.explanationText.setText("");
            binding.dateText.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.memoryImage.setImageResource(R.drawable.image);
        }
        else{
            //old memory
            binding.saveButton.setVisibility(View.INVISIBLE);
            int memoryId=intent.getIntExtra("memoryId",1);

            try {

                Cursor cursor=database.rawQuery("SELECT * FROM memories where id=?",new String[] {String.valueOf(memoryId)});
                int titleIx=cursor.getColumnIndex("title");
                int explanationIx=cursor.getColumnIndex("explanation");
                int dateIx=cursor.getColumnIndex("date");
                int imageIx=cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.memoryText.setText(cursor.getString(titleIx));
                    binding.explanationText.setText(cursor.getString(explanationIx));
                    binding.dateText.setText(cursor.getString(dateIx));

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.memoryImage.setImageBitmap(bitmap);
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                String message="Permission Needed for gallery";
                Snackbar.make(view,message,Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            }
            else{
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else{
            //gallery
            Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }

    }

    public Bitmap makeSmaller(Bitmap image,int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    private void registerLauncher(){

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()== Activity.RESULT_OK){
                    Intent intentFromResult=result.getData();
                    if(intentFromResult!=null){
                        Uri imageData=intentFromResult.getData();

                        try{
                            if(Build.VERSION.SDK_INT>=28){
                                ImageDecoder.Source source=ImageDecoder.createSource(DiaryActivity.this.getContentResolver(),imageData);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                                binding.memoryImage.setImageBitmap(selectedImage);
                            }
                            else{
                                selectedImage=MediaStore.Images.Media.getBitmap(DiaryActivity.this.getContentResolver(),imageData);
                                binding.memoryImage.setImageBitmap(selectedImage);
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted
                    Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
                else{
                    //permission denied
                    Toast.makeText(DiaryActivity.this,"Permission Needed!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void save(View view) {

        String title = binding.memoryText.getText().toString();
        String explanation = binding.explanationText.getText().toString();
        String date = binding.dateText.getText().toString();

        Bitmap smallImage = makeSmaller(selectedImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Database",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS memoryTable (id INTEGER PRIMARY KEY,title VARCHAR, explanation VARCHAR, date VARCHAR, image BLOB)");
            String sqlString = "INSERT INTO memoryTable (title, explanation, date, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,title);
            sqLiteStatement.bindString(2,explanation);
            sqLiteStatement.bindString(3,date);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(DiaryActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

}