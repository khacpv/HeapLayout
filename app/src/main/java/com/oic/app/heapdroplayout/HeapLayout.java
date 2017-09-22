package com.oic.app.heapdroplayout;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

public class HeapLayout extends RelativeLayout implements View.OnTouchListener {

    private CircleImageView currentView;
    private List<ItemData> itemList = new ArrayList<>();

    private ImageView imvDelete;
    private TextView tvLog;

    private boolean touchToAdd = false;

    public HeapLayout(Context context) {
        super(context);
        init();
    }

    public HeapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    public void add(ItemData item) {
        itemList.add(item);
        refresh();
    }

    public void clearImage() {
        itemList.clear();
        refresh();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refresh();
    }

    public void refresh() {
        removeAllViews();
        if (imvDelete != null) {
            imvDelete.setVisibility(View.GONE);
        }

        reInvalidate();
    }

    public void reInvalidate() {
        removeAllViews();

        for (ItemData item : itemList) {
            CircleImageView imageView = new CircleImageView(getContext());
            int size = (int) (getWidth() * item.size);   // circle image
            HeapLayout.LayoutParams params = new HeapLayout.LayoutParams(size, size);
            imageView.setImageResource(item.src);
            params.leftMargin = (int) (getWidth() * item.x) - params.width / 2;
            params.topMargin = (int) (getHeight() * item.y) - params.height / 2;
            imageView.setOnTouchListener(this);
            imageView.setTag(item);
            addView(imageView, params);
        }
    }

    public void setTouchToAdd(boolean touchToAdd) {
        this.touchToAdd = touchToAdd;
    }

    CircleImageView newItemView;
    ItemData newItem;
    float lastX = 0f;
    float lastY = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!touchToAdd) {
            return false;
        }
        switch (event.getAction()) {
            case ACTION_DOWN:
                if (newItem != null) {
                    return false;
                }
                lastX = event.getX();
                lastY = event.getY();
                newItem = new ItemData(lastX / getWidth(), lastY / getHeight(), ItemData.Size.SMALL,
                    R.drawable.user);

                newItemView = new CircleImageView(getContext());
                int viewSize = (int) (getWidth() * newItem.size);   // circle image
                HeapLayout.LayoutParams params = new HeapLayout.LayoutParams(viewSize, viewSize);
                newItemView.setImageResource(newItem.src);
                params.leftMargin = (int) (getWidth() * newItem.x) - params.width / 2;
                params.topMargin = (int) (getHeight() * newItem.y) - params.height / 2;
                newItemView.setTag(newItem);
                addView(newItemView, params);
                return true;
            case ACTION_MOVE:
                float deltaX = event.getX() - lastX;
                float deltaY = event.getY() - lastY;

                float radius = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                ItemData.Size size = ItemData.Size.SMALL;
                if (radius > ItemData.Size.MEDIUM.getValue() * getWidth()) {
                    size = ItemData.Size.MEDIUM;
                }
                if (radius > ItemData.Size.LARGE.getValue() * getWidth()) {
                    size = ItemData.Size.LARGE;
                }

                newItem.size = size.getValue();

                params = (HeapLayout.LayoutParams) newItemView.getLayoutParams();
                viewSize = (int) (getWidth() * newItem.size);   // circle image
                params.width = params.height = viewSize;
                newItemView.setLayoutParams(params);
                return true;
            case ACTION_UP:
                if (newItem != null) {
                    itemList.add(newItem);
                }
                newItem = null;
                refresh();
                break;
        }
        return true;
    }

    public void setImvDelete(ImageView imvDelete) {
        this.imvDelete = imvDelete;
    }

    public void setTvLog(TextView tvLog) {
        this.tvLog = tvLog;
    }

    private void log(String log) {
        if (tvLog != null) {
            tvLog.setText(log);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case ACTION_DOWN:
                if (currentView != null) {
                    return false;
                }
                currentView = (CircleImageView) v;
                ItemData item = (ItemData) currentView.getTag();
                int position = itemList.indexOf(item);
                log("item: " + position + ": " + item.toString());
                return true;
            case MotionEvent.ACTION_MOVE:
                if (currentView == null) {
                    return false;
                }
                int[] location = new int[2];
                this.getLocationOnScreen(location);

                item = (ItemData) currentView.getTag();

                float newX = event.getRawX() - location[0] - getWidth() * item.size / 2;
                float newY = event.getRawY() - location[1] - getHeight() * item.size / 2;

                imvDelete.setVisibility(View.GONE);
                if (newX > getWidth() - dpToPx(50) && newY < dpToPx(50)) {
                    imvDelete.setVisibility(View.VISIBLE);
                }

                HeapLayout.LayoutParams params =
                    (HeapLayout.LayoutParams) currentView.getLayoutParams();
                params.leftMargin = (int) newX;
                params.topMargin = (int) newY;
                currentView.setLayoutParams(params);

                item.x = (params.leftMargin + item.size * getWidth() / 2) / (float) getWidth();
                item.y = (params.topMargin + item.size * getWidth() / 2) / (float) getHeight();

                position = itemList.indexOf(item);
                log("item: " + position + ": " + item.toString());
                return true;
            case MotionEvent.ACTION_UP:
                if (currentView != null) {
                    params = (HeapLayout.LayoutParams) currentView.getLayoutParams();
                    item = (ItemData) currentView.getTag();

                    item.x = (params.leftMargin + item.size * getWidth() / 2) / (float) getWidth();
                    item.y = (params.topMargin + item.size * getWidth() / 2) / (float) getHeight();

                    // check for delete
                    if (params.leftMargin > getWidth() - dpToPx(20) && params.topMargin < dpToPx(
                        20)) {
                        itemList.remove(item);
                    }
                }
                currentView = null;
                refresh();
                return true;
        }
        return false;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public String getOutput() {
        StringBuilder str = new StringBuilder();

        for (ItemData item : itemList) {
            String itemStr = item.toString();
            str.append(itemStr);
            str.append(",\n");
        }

        return "[" + str.toString() + "]";
    }

    public String getPatternFromAsset(String pattern) {
        AssetManager am = getContext().getAssets();
        try {
            InputStreamReader is = new InputStreamReader(am.open(pattern));
            BufferedReader reader = new BufferedReader(is);
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "[]";
    }

    public static class ItemData {
        public enum Size {
            LARGE(42f / 360f),
            MEDIUM(35f / 360f),
            SMALL(28f / 360f);

            private float value;

            Size(float value) {
                this.value = value;
            }

            public float getValue() {
                return value;
            }
        }

        public float x;
        public float y;
        public float size;

        public int src;

        public ItemData(float x, float y, Size size, int src) {
            this.x = x;
            this.y = y;
            this.size = size.getValue();
            this.src = src;
        }

        @Override
        public String toString() {
            String output = "{" + "\t\"x\":%s," + "\t\"y\":%s," + "\t\"size\":%s" + "}";
            return String.format(output, x + "", y + "", size + "");
        }
    }
}
