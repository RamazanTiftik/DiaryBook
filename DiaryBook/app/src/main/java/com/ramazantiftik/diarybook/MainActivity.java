package com.ramazantiftik.diarybook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.ramazantiftik.diarybook.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<MemoryData> memoryDataArrayList;
    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        memoryDataArrayList=new ArrayList<>();

        //use adapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerAdapter=new RecyclerAdapter(memoryDataArrayList);
        binding.recyclerView.setAdapter(recyclerAdapter);
        getData();
    }

    public void getData() {

        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Database",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM memoryTable", null);
            int titleIx = cursor.getColumnIndex("title");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                String title = cursor.getString(titleIx);
                int id = cursor.getInt(idIx);
                MemoryData memoryData = new MemoryData(title,id);
                memoryDataArrayList.add(memoryData);
            }
            recyclerAdapter.notifyDataSetChanged(); //recyclerRow updating

            cursor.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Main menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.diary_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.add_memory){
            Intent intent=new Intent(MainActivity.this,DiaryActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        else if(item.getItemId()==R.id.delete_memory){

        }

        return super.onOptionsItemSelected(item);
    }
}