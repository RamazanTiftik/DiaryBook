package com.ramazantiftik.diarybook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ramazantiftik.diarybook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.DiaryHolder> {

    private ArrayList<MemoryData> memoryDataArrayList;

    public RecyclerAdapter(ArrayList<MemoryData> memoryDataArrayList){
        this.memoryDataArrayList=memoryDataArrayList;
    }

    @NonNull
    @Override
    public DiaryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new DiaryHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryHolder holder, int position) {
        holder.binding.recyclerRowTextView.setText(memoryDataArrayList.get(position).getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),DiaryActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("memoryId",memoryDataArrayList.get(holder.getAdapterPosition()).getId());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoryDataArrayList.size();
    }

    public class DiaryHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
        public DiaryHolder(@NonNull RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }
}
