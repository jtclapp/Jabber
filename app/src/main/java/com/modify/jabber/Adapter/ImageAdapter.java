package com.modify.jabber.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends PagerAdapter
{
    private Context mContext;
    private List<String> imageIDs;

    public ImageAdapter(Context context, List<String> images)
    {
        mContext = context;
        imageIDs = images;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(mContext);
            Glide.with(mContext).load(imageIDs.get(position)).centerCrop().into(imageView);
            container.addView(imageView,0);
            return imageView;
    }
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((ImageView) object);
    }
    @Override
    public int getCount() {
        return imageIDs.size();
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
