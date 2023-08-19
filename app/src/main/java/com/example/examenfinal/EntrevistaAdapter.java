package com.example.examenfinal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EntrevistaAdapter extends RecyclerView.Adapter<EntrevistaAdapter.EntrevistaViewHolder> {

    private Context context;
    private List<Entrevista> entrevistaList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public EntrevistaAdapter(Context context, List<Entrevista> entrevistaList) {
        this.context = context;
        this.entrevistaList = entrevistaList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @NonNull
    @Override
    public EntrevistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_foto, parent, false);
        return new EntrevistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrevistaViewHolder holder, int position) {
        Entrevista entrevista = entrevistaList.get(position);

        String imagenBase64 = entrevista.getImagen();

        if (imagenBase64 != null) {
            byte[] imagenBytes = Base64.decode(imagenBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            holder.imagen.setImageBitmap(bitmap);
        } else {
            holder.imagen.setImageResource(R.drawable.ic_launcher_foreground);
        }

        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.ic_launcher_background);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(entrevista.getFecha());

        holder.descripcion.setText(entrevista.getDescripcion());
        holder.periodista.setText(entrevista.getPeriodista());
        holder.fecha.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return entrevistaList.size();
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    class EntrevistaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView descripcion;
        TextView periodista;
        TextView fecha;

        EntrevistaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.row_imagen);
            descripcion = itemView.findViewById(R.id.row_descripcion);
            periodista = itemView.findViewById(R.id.row_periodista);
            fecha = itemView.findViewById(R.id.row_fecha);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onItemClick(position);
                    }
                }
            });
        }
    }
}