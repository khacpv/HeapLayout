package com.oic.app.heapdroplayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import de.hdodenhof.circleimageview.CircleImageView;
import java.lang.reflect.Type;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imvDelete;
    TextView tvLog;
    CheckBox cbTouchToAdd;
    HeapLayout layoutHeap;

    int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutHeap = (HeapLayout) findViewById(R.id.layout_heap);
        imvDelete = (CircleImageView) findViewById(R.id.imv_delete);
        tvLog = (TextView) findViewById(R.id.tv_log);
        cbTouchToAdd = (CheckBox) findViewById(R.id.cb_touch_to_add);
        layoutHeap.setImvDelete(imvDelete);
        layoutHeap.setTvLog(tvLog);

        cbTouchToAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutHeap.setTouchToAdd(isChecked);
            }
        });

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setLongSerializationPolicy(LongSerializationPolicy.STRING).create();

        Type type = new TypeToken<List<HeapLayout.ItemData>>() {
        }.getType();

        String json = layoutHeap.getPatternFromAsset("pattern_72.js");
        final List<HeapLayout.ItemData> data = gson.fromJson(json, type);
        position = 0;
        layoutHeap.refresh();
        layoutHeap.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (position >= data.size()) {
                    return;
                }
                HeapLayout.ItemData itemData = data.get(position);
                if (itemData != null) {
                    switch (position % 4) {
                        case 0:
                            itemData.src = R.drawable.user;
                            break;
                        case 1:
                            itemData.src = R.drawable.taylor;
                            break;
                        case 2:
                            itemData.src = R.drawable.madona;
                            break;
                        case 3:
                            itemData.src = R.drawable.ariana;
                            break;
                        default:
                            itemData.src = R.drawable.user;
                            break;
                    }

                    layoutHeap.add(itemData);
                }
                position++;
                layoutHeap.postDelayed(this, 500);
            }
        }, 1000);
    }

    public void printOutput(View view) {
        tvLog.setText(layoutHeap.getOutput());
        Log.e("TAG", layoutHeap.getOutput());
    }

    public void clearAllImage(View view) {
        layoutHeap.clearImage();
    }

    public void onImvClick(View view) {
        switch (view.getId()) {
            case R.id.imv_large:
                layoutHeap.add(new HeapLayout.ItemData(0.5f, 0.5f, HeapLayout.ItemData.Size.LARGE,
                    R.drawable.user));
                break;
            case R.id.imv_medium:
                layoutHeap.add(new HeapLayout.ItemData(0.5f, 0.5f, HeapLayout.ItemData.Size.MEDIUM,
                    R.drawable.taylor));
                break;
            case R.id.imv_small:
                layoutHeap.add(new HeapLayout.ItemData(0.5f, 0.5f, HeapLayout.ItemData.Size.SMALL,
                    R.drawable.madona));
                break;
        }
    }
}
