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
        holder.tvRoomName.setText(room.getRoomName());
        holder.tvStationName.setText(room.getStationName());

        // Color coding based on SRS Status Definitions
        switch (room.getStatus()) {
            case 1: // Call
                holder.tvStatus.setText("CALL");
                holder.cardBackground.setBackgroundColor(Color.parseColor("#FFF59D")); // Yellow
                break;
            case 2: // Assistance
                holder.tvStatus.setText("ASSISTANCE");
                holder.cardBackground.setBackgroundColor(Color.parseColor("#FFCC80")); // Orange
                break;
            case 3: // Care Required
                holder.tvStatus.setText("CARE REQUIRED");
                holder.cardBackground.setBackgroundColor(Color.parseColor("#EF9A9A")); // Red-Orange
                break;
            case 4: // Emergency
                holder.tvStatus.setText("EMERGENCY - CODE BLUE");
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.tvRoomName.setTextColor(Color.WHITE);
                holder.cardBackground.setBackgroundColor(Color.parseColor("#B71C1C")); // Dark Red
                break;
            default: // 0 = Idle
                holder.tvStatus.setText("IDLE");
                holder.tvStatus.setTextColor(Color.BLACK);
                holder.tvRoomName.setTextColor(Color.BLACK);
                holder.cardBackground.setBackgroundColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvStationName, tvStatus;
        LinearLayout cardBackground;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvStationName = itemView.findViewById(R.id.tvStationName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cardBackground = itemView.findViewById(R.id.cardBackground);
        }
    }
}