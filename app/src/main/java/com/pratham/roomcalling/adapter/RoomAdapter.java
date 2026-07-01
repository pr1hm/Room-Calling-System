package com.pratham.roomcalling.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pratham.roomcalling.R;
import com.pratham.roomcalling.model.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList = new ArrayList<>();

    public void updateRooms(List<Room> newRooms) {
        this.roomList.clear();
        this.roomList.addAll(newRooms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_card, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // Bind both texts
        holder.tvRoomId.setText("Room: " + room.getRoomId());
        holder.tvRoomName.setText(room.getRoomName());

        // Color coding based on status
        switch (room.getStatus()) {
            case 4: // Emergency
                holder.cardBackground.setBackgroundColor(Color.parseColor("#EF4444")); // Red
                holder.tvStatus.setText("CODE BLUE / EMERGENCY");
                holder.setTextColor(Color.WHITE);
                break;
            case 3: // Care Required
                holder.cardBackground.setBackgroundColor(Color.parseColor("#F97316")); // Orange
                holder.tvStatus.setText("CARE REQUIRED");
                holder.setTextColor(Color.WHITE);
                break;
            case 2: // Assistance
                holder.cardBackground.setBackgroundColor(Color.parseColor("#EAB308")); // Yellow
                holder.tvStatus.setText("ASSISTANCE");
                holder.setTextColor(Color.BLACK);
                break;
            case 1: // Standard Call
                holder.cardBackground.setBackgroundColor(Color.parseColor("#3B82F6")); // Blue
                holder.tvStatus.setText("NURSE CALL");
                holder.setTextColor(Color.WHITE);
                break;
            case 0: // Idle
            default:
                holder.cardBackground.setBackgroundColor(Color.WHITE);
                holder.tvStatus.setText("IDLE");
                holder.setTextColor(Color.parseColor("#1E293B"));
                holder.tvRoomId.setTextColor(Color.parseColor("#64748B")); // Reset subtitle color
                break;
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomId, tvRoomName, tvStatus;
        LinearLayout cardBackground;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomId = itemView.findViewById(R.id.tvRoomId);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardBackground = itemView.findViewById(R.id.cardBackground);
        }

        public void setTextColor(int color) {
            tvRoomName.setTextColor(color);
            tvStatus.setTextColor(color);
            // If the background is a dark color (White text), make the Room ID semi-transparent white
            if (color == Color.WHITE) {
                tvRoomId.setTextColor(Color.parseColor("#E2E8F0"));
            } else {
                tvRoomId.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }
}