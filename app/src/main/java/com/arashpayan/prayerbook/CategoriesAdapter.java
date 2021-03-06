package com.arashpayan.prayerbook;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

    private final Cursor mCategoriesCursor;
    private final Language mLanguage;
    private OnCategorySelectedListener mListener;

    public CategoriesAdapter(Language language) {
        this.mLanguage = language;
        mCategoriesCursor = Database.getInstance().getCategories(language);
        setHasStableIds(false);
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category, parent, false);
        final CategoryViewHolder holder = new CategoryViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) {
                    return;
                }

                mListener.onCategorySelected(holder.category.getText().toString(), holder.getLanguage());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        mCategoriesCursor.moveToPosition(position);

        int categoryColIdx = mCategoriesCursor.getColumnIndexOrThrow(Database.CATEGORY_COLUMN);
        String category = mCategoriesCursor.getString(categoryColIdx);
        holder.category.setText(category);
        holder.setLanguage(mLanguage);

        int prayerCount = Database.getInstance().getPrayerCountForCategory(category, mLanguage.code);
        holder.prayerCount.setText(String.format(mLanguage.locale, "%d", prayerCount));
    }

    @Override
    public int getItemCount() {
        return mCategoriesCursor.getCount();
    }

    public Language getLanguage() {
        return mLanguage;
    }

    public void setListener(OnCategorySelectedListener l) {
        mListener = l;
    }

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category, Language language);
    }
}
