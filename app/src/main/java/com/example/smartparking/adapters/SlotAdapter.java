package com.example.smartparking.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparking.R;
import com.example.smartparking.models.ParkingSlot;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private List<ParkingSlot> slotList;
    private OnSlotClickListener listener;
    private int selectedPosition = -1;

    public interface OnSlotClickListener {
        void onSlotClick(ParkingSlot slot);
    }

    public SlotAdapter(List<ParkingSlot> slotList, OnSlotClickListener listener) {
        this.slotList = slotList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ParkingSlot slot = slotList.get(position);

        holder.tvSlotId.setText(slot.getSlotId());

        if ("reserved".equals(slot.getStatus())) {
            // حالة [محجوز]: إخفاء الكارت بالكامل وإظهار صورة السيارة فقط
            holder.cvAvailableContainer.setVisibility(View.GONE);
            holder.ivCar.setVisibility(View.VISIBLE);

            // تكبير حجم السيارة قليلاً برمجياً لتقليل أي مسافة رأسية متبقية لتلتحم مع الصفوف
            holder.ivCar.setScaleX(1.35f);
            holder.ivCar.setScaleY(1.35f);

            // تلوين أيقونة ذوي الاحتياجات بلون رمادي لتكون واضحة (إن وجدت)
            holder.ivSpecialNeedsIcon.setColorFilter(Color.parseColor("#9E9E9E"));

            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = position;
                Toast.makeText(v.getContext(), slot.getSlotId(), Toast.LENGTH_SHORT).show();
                //holder.ivCar.setBackgroundColor(Color.parseColor("#D97A9A"));
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                listener.onSlotClick(slot);
            });

            // منع النقر على الموقف المحجوز
            //holder.itemView.setOnClickListener(null);

        } else {
            // حالة [متاح]: إظهار الكارت الدائري وإخفاء السيارة
            holder.cvAvailableContainer.setVisibility(View.VISIBLE);
            holder.ivCar.setVisibility(View.GONE);

            // إعادة حجم الصورة للطبيعي حتى لا تتأثر عند إعادة استخدام الكارت (Recycling)
            holder.ivCar.setScaleX(1.0f);
            holder.ivCar.setScaleY(1.0f);

            // تلوين أيقونة ذوي الاحتياجات بالأبيض لتظهر فوق الكارت الملون
            holder.ivSpecialNeedsIcon.setColorFilter(Color.parseColor("#FFFFFF"));

            if (selectedPosition == position) {
                // حالة [محدد]: تلوين بالوردي
                holder.layoutSlotContainer.setBackgroundColor(Color.parseColor("#D97A9A"));
                holder.tvAvailableText.setText("محدد");
            } else {
                // الحفاظ على الألوان الأصلية لمواقف ذوي الاحتياجات والمواقف العادية
                if (slot.isSpecialNeeds()) {
                    holder.layoutSlotContainer.setBackgroundColor(Color.parseColor("#C5A36F")); // ذهبي
                    holder.tvAvailableText.setText("متاح");
                } else {
                    holder.layoutSlotContainer.setBackgroundColor(Color.parseColor("#8AB682")); // أخضر
                    holder.tvAvailableText.setText("متاح");
                }
            }

            // تفعيل النقر وتحديث العنصر المحدد
            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = position;
                Toast.makeText(v.getContext(), slot.getSlotId(), Toast.LENGTH_SHORT).show();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                listener.onSlotClick(slot);
            });
        }

        // التحقق من توافر ميزة ذوي الاحتياجات لإظهار/إخفاء الأيقونة
        if (slot.isSpecialNeeds()) {
            holder.ivSpecialNeedsIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivSpecialNeedsIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public static class SlotViewHolder extends RecyclerView.ViewHolder {
        CardView cvAvailableContainer;
        LinearLayout layoutSlotContainer;
        TextView tvSlotId, tvAvailableText;
        ImageView ivSpecialNeedsIcon, ivCar;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cvAvailableContainer = itemView.findViewById(R.id.cvAvailableContainer);
            layoutSlotContainer = itemView.findViewById(R.id.layoutSlotContainer);
            tvSlotId = itemView.findViewById(R.id.tvSlotId);
            tvAvailableText = itemView.findViewById(R.id.tvAvailableText);
            ivSpecialNeedsIcon = itemView.findViewById(R.id.ivSpecialNeedsIcon);
            ivCar = itemView.findViewById(R.id.ivCar);
        }
    }
}