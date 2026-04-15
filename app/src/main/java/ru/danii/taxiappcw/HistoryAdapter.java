package ru.danii.taxiappcw;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ru.danii.taxiappcw.db.TripModel;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.TripViewHolder> {

    private final List<TripModel> trips;
    private final OnTripClickListener listener;

    // Интерфейс для обработки кликов
    public interface OnTripClickListener {
        void onTripClick(TripModel trip);
    }

    public HistoryAdapter(List<TripModel> trips, OnTripClickListener listener) {
        this.trips = trips;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        TripModel trip = trips.get(position);
        holder.bind(trip, listener);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTariff, tvFrom, tvTo;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvTripDate);
            tvTariff = itemView.findViewById(R.id.tvTripTariff);
            tvFrom = itemView.findViewById(R.id.tvTripFrom);
            tvTo = itemView.findViewById(R.id.tvTripTo);
        }

        public void bind(final TripModel trip, final OnTripClickListener listener) {
            tvDate.setText(trip.date);
            tvTariff.setText(trip.tariff);
            tvFrom.setText(trip.from);
            tvTo.setText(trip.to);

            // Обработка клика на всю карточку
            itemView.setOnClickListener(v -> listener.onTripClick(trip));
        }
    }
}