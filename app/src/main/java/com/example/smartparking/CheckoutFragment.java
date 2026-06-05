package com.example.smartparking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CheckoutFragment extends Fragment {

    private String parkingName, spotId, plateNumber, duration, totalPrice, pricePerHour;
    private String date, startTime, endTime;
    private long startTimestamp, endTimestamp;

    private String selectedPaymentMethod = "";

    private View layoutHasCard, layoutNoCard;
    private TextView tvCheckoutCardNumber;
    private Button btnFinalConfirm;

    public static CheckoutFragment newInstance(String parkingLotId,String parkingName, String spotId, String plateNumber,
                                               String duration, String totalPrice, String pricePerHour,
                                               String date, String startTime, String endTime,
                                               long startTimestamp, long endTimestamp) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putString("parkingLotId", parkingLotId);
        args.putString("parkingName", parkingName);
        args.putString("spotId", spotId);
        args.putString("plateNumber", plateNumber);
        args.putString("duration", duration);
        args.putString("totalPrice", totalPrice);
        args.putString("pricePerHour", pricePerHour);
        args.putString("date", date);
        args.putString("startTime", startTime);
        args.putString("endTime", endTime);
        args.putLong("startTimestamp", startTimestamp);
        args.putLong("endTimestamp", endTimestamp);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        if (getArguments() != null) {
            parkingName = getArguments().getString("parkingName");
            spotId = getArguments().getString("spotId");
            plateNumber = getArguments().getString("plateNumber");
            duration = getArguments().getString("duration");
            totalPrice = getArguments().getString("totalPrice");
            pricePerHour = getArguments().getString("pricePerHour");

            date = getArguments().getString("date");
            startTime = getArguments().getString("startTime");
            endTime = getArguments().getString("endTime");
            startTimestamp = getArguments().getLong("startTimestamp", 0);
            endTimestamp = getArguments().getLong("endTimestamp", 0);
        }

        TextView tvCheckoutParkingName = view.findViewById(R.id.tvCheckoutParkingName);
        TextView tvCheckoutRegion = view.findViewById(R.id.tvCheckoutRegion);
        TextView tvCheckoutSpot = view.findViewById(R.id.tvCheckoutSpot);
        TextView tvCheckoutDuration = view.findViewById(R.id.tvCheckoutDuration);
        TextView tvCheckoutPricePerHour = view.findViewById(R.id.tvCheckoutPricePerHour);
        TextView tvCheckoutDurationSummary = view.findViewById(R.id.tvCheckoutDurationSummary);
        TextView tvCheckoutTotal = view.findViewById(R.id.tvCheckoutTotal);

        tvCheckoutCardNumber = view.findViewById(R.id.tvCheckoutCardNumber);
        layoutHasCard = view.findViewById(R.id.layoutHasCard);
        layoutNoCard = view.findViewById(R.id.layoutNoCard);

        btnFinalConfirm = view.findViewById(R.id.btnFinalConfirm);
        View btnBackCheckout = view.findViewById(R.id.btnBackCheckout);
        View btnChangePayment = view.findViewById(R.id.btnChangePayment);

        tvCheckoutParkingName.setText(parkingName);

        String regionStr = "A";
        if (spotId != null && !spotId.isEmpty()) {
            regionStr = spotId.substring(0, 1).toUpperCase();
        }
        tvCheckoutRegion.setText("المنطقة " + regionStr);

        tvCheckoutSpot.setText(spotId);
        tvCheckoutDuration.setText(duration);
        tvCheckoutDurationSummary.setText(duration);
        tvCheckoutPricePerHour.setText(pricePerHour);
        tvCheckoutTotal.setText(totalPrice);

        checkUserPaymentMethod();

        getParentFragmentManager().setFragmentResultListener("cardRequestKey", this, (requestKey, bundle) -> {
            String last4Digits = bundle.getString("last4Digits", "0000");
            selectedPaymentMethod = "بطاقة تنتهي بـ " + last4Digits;
            if (tvCheckoutCardNumber != null) {
                tvCheckoutCardNumber.setText(last4Digits + " ••••");
            }
            layoutHasCard.setVisibility(View.VISIBLE);
            layoutNoCard.setVisibility(View.GONE);
        });

        View.OnClickListener openMyCardsListener = v -> {
            getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, MyCardsFragment.newInstance(true)).addToBackStack(null).commit();
        };

        btnChangePayment.setOnClickListener(openMyCardsListener);
        layoutNoCard.setOnClickListener(openMyCardsListener);

        btnFinalConfirm.setOnClickListener(v -> handleFinalCheckout());
        btnBackCheckout.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void checkUserPaymentMethod() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference().child("payments").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasCard = false;
                        String last4 = "";

                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                if (child.hasChild("last4Digits")) {
                                    hasCard = true;
                                    last4 = child.child("last4Digits").getValue(String.class);
                                    break;
                                }
                            }
                        }

                        if (hasCard) {
                            if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
                                selectedPaymentMethod = "بطاقة تنتهي بـ " + last4;
                                if (tvCheckoutCardNumber != null) {
                                    tvCheckoutCardNumber.setText(last4 + " ••••");
                                }
                            }
                            layoutHasCard.setVisibility(View.VISIBLE);
                            layoutNoCard.setVisibility(View.GONE);
                        } else {
                            if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
                                layoutHasCard.setVisibility(View.GONE);
                                layoutNoCard.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
                            layoutHasCard.setVisibility(View.GONE);
                            layoutNoCard.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void handleFinalCheckout() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        if (selectedPaymentMethod == null || selectedPaymentMethod.isEmpty()) {
            Toast.makeText(getContext(), "يرجى إضافة طريقة دفع أولاً لإتمام الحجز", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFinalConfirm.setEnabled(false);
        btnFinalConfirm.setText("جاري معالجة الحجز...");

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("ParkingLots").orderByChild("title").equalTo(parkingName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String realParkingId = parkingName.replaceAll("\\s+", "");
                        if (snapshot.exists()) {
                            for (DataSnapshot pSnap : snapshot.getChildren()) {
                                realParkingId = pSnap.getKey();
                                break;
                            }
                        }

                        checkSpotAvailabilityAndBook(userId, realParkingId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnFinalConfirm.setEnabled(true);
                        btnFinalConfirm.setText("تأكيد الحجز");
                        Toast.makeText(getContext(), "حدث خطأ في الاتصال بقاعدة البيانات", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkSpotAvailabilityAndBook(String userId, String realParkingId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("Reservations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isConflict = false;

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    for (DataSnapshot resSnap : userSnap.getChildren()) {
                        String resParkingName = resSnap.child("parkingName").getValue(String.class);
                        String resSpotId = resSnap.child("spotId").getValue(String.class);

                        if (parkingName.equals(resParkingName) && spotId.equals(resSpotId)) {
                            Long otherStart = resSnap.child("startTimestamp").getValue(Long.class);
                            Long otherEnd = resSnap.child("endTimestamp").getValue(Long.class);

                            if (otherStart != null && otherEnd != null) {
                                if (startTimestamp < otherEnd && endTimestamp > otherStart) {
                                    isConflict = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (isConflict) break;
                }

                if (isConflict) {
                    Toast.makeText(getContext(), "عذراً، هذا الموقف محجوز بالفعل في هذا الوقت أو هناك تداخل بالحجز", Toast.LENGTH_LONG).show();
                    btnFinalConfirm.setEnabled(true);
                    btnFinalConfirm.setText("تأكيد الحجز");
                } else {
                    executeBooking(userId, realParkingId, spotId.substring(0, 1).toUpperCase());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnFinalConfirm.setEnabled(true);
                btnFinalConfirm.setText("تأكيد الحجز");
            }
        });
    }

    private void executeBooking(String userId, String realParkingId, String rStr) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        String resId = mDatabase.child("Reservations").child(userId).push().getKey();
        String paymentId = mDatabase.child("payments").child(userId).push().getKey();

        long now = System.currentTimeMillis();
        String initialStatus = "محجوز";
        if (now > endTimestamp) {
            initialStatus = "منتهي";
        } else if (now >= startTimestamp) {
            initialStatus = "نشط";
        }

        Map<String, Object> resData = new HashMap<>();
        resData.put("parkingName", parkingName);
        resData.put("spotId", spotId);
        resData.put("plateNumber", plateNumber);
        resData.put("duration", duration);
        resData.put("totalPrice", totalPrice);
        resData.put("date", date);
        resData.put("startTime", startTime);
        resData.put("endTime", endTime);
        resData.put("startTimestamp", startTimestamp);
        resData.put("endTimestamp", endTimestamp);
        resData.put("status", initialStatus);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("reservationId", resId);
        paymentData.put("amount", totalPrice);
        paymentData.put("paymentMethod", selectedPaymentMethod);
        paymentData.put("status", "مكتمل");
        paymentData.put("timestamp", System.currentTimeMillis());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/Reservations/" + userId + "/" + resId, resData);
        childUpdates.put("/payments/" + userId + "/" + paymentId, paymentData);

        if ("نشط".equals(initialStatus)) {
            childUpdates.put("/ParkingLots/" + realParkingId + "/regions/" + rStr + "/slots/" + spotId + "/status", "reserved");
        }

        mDatabase.updateChildren(childUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        String receiptId = "TPS-" + System.currentTimeMillis();

                        BookingSuccessFragment successFragment = BookingSuccessFragment.newInstance(
                                parkingName, spotId, duration, totalPrice, receiptId
                        );

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, successFragment)
                                .commit();

                    } else {
                        btnFinalConfirm.setEnabled(true);
                        btnFinalConfirm.setText("تأكيد الحجز");
                        Toast.makeText(getContext(), "فشل تأكيد الحجز، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}