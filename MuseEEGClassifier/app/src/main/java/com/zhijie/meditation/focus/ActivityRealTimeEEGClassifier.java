package com.zhijie.meditation.focus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseManagerAndroid;
import java.util.List;
import controllers.MuseControllers.MuseConnectionHelper;
import controllers.SVMController.SVM_Helper;
import static constants.AppConstants.SVM_MODEL_FN;
import static constants.AppConstants.USE_MUSE;

public class ActivityRealTimeEEGClassifier extends Activity implements View.OnClickListener {

    private final String TAG = "RealTimeEEGClassifier";
    private final Handler handler = new Handler();
    private MuseConnectionHelper museConnectionHelper;
    private MuseManagerAndroid manager;
    private String muse_status;
    private Context context;
    private String name;
    private SVM_Helper sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context = this;
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        sh = new SVM_Helper(context, SVM_MODEL_FN);
        museConnectionHelper = new MuseConnectionHelper(sh);
        if (USE_MUSE) {
            Intent i = new Intent(this, ActivityConnectMuse.class);
            startActivityForResult(i, R.integer.SELECT_MUSE_REQUEST);
        } else {
            init();
        }
    }

    private void init() {
        setContentView(R.layout.activity_eeg_realtime_classifier);
        TextView hsi1 = findViewById(R.id.hsi1);
        TextView hsi2 = findViewById(R.id.hsi2);
        TextView hsi3 = findViewById(R.id.hsi3);
        TextView hsi4 = findViewById(R.id.hsi4);
        TextView tv_muse_status = findViewById(R.id.tv_muse_status);
        museConnectionHelper.setHSITextView(hsi1, hsi2, hsi3, hsi4);
        museConnectionHelper.setTv_muse_status(tv_muse_status);
        museConnectionHelper.updateGUI.run();
        handler.post(sh.processEEG);
    }

    @Override
    public void onClick(View v) {
        museConnectionHelper.stoplistening();
        Intent myIntent = new Intent(this,ActivityRealTimeEEGClassifier.class);
        startActivity(myIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == R.integer.SELECT_MUSE_REQUEST) {
            if (resultCode == RESULT_OK) {

                int position = data.getIntExtra("pos", 0);
                List<Muse> availableMuse = manager.getMuses();
                connect_to_muse(availableMuse.get(position));
                init();
            } else {
                finish();
            }
        }
    }

    private void connect_to_muse(Muse muse) {
        museConnectionHelper.setMuse(muse);
        museConnectionHelper.connect_to_muse();
    }

}
