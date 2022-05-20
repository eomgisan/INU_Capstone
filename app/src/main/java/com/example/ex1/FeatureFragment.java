package com.example.ex1;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ex1.dataStructure.UserFeature;
import com.example.ex1.weather.Weather3;
import com.example.ex1.weather.Weather7;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class FeatureFragment extends Fragment {

    MainActivity activity;
    String TAG = "FeatureFragment";

    UserFeature userFeature;

    TextView averInc1;
    TextView averInc2;
    TextView averPeriod1;
    TextView averPeriod2;

    BarChart incChart1;

    BarChart periodChart1;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    String[] labels = new String[]{"첫번째","두번쨰","세번째","네번쨰","다섯번째","여섯번째","일곱번째"};

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//이 메소드가 호출될떄는 프래그먼트가 엑티비티위에 올라와있는거니깐 getActivity메소드로 엑티비티참조가능
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
//이제 더이상 엑티비티 참초가안됨
        activity = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_feature, container, false);

        FirebaseFirestore db = activity.db;
        FirebaseUser user = activity.user;

        userFeature = activity.userFeature;
        averInc1 = rootview.findViewById(R.id.averInc1);
        averInc2 = rootview.findViewById(R.id.averInc2);
        averPeriod1 = rootview.findViewById(R.id.averPeriod1);
        averPeriod2 = rootview.findViewById(R.id.averPeriod2);

        incChart1 = rootview.findViewById(R.id.incChart1);
        periodChart1 = rootview.findViewById(R.id.periodChart1);

        BarData barDataP = new BarData();
        BarData barDataW = new BarData();

        if(userFeature.getAver_inc1() == 0 && userFeature.getAver_inc2() == 0){
            activity.startToast("아직 사용자의 빨래 정보가 데이터베이스에 없습니다!");
        }
        else{
            DocumentReference docRef = db.collection("datas").document(user.getUid());
            docRef.collection("featureAnalysis").document("period1").
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(task!=null){

                        Map map = document.getData();
                        if(map.size() != 0){
                                ArrayList<BarEntry> entries = new ArrayList<>();
                                TreeMap map1 = new TreeMap(map);
                                List<String> keyList = new ArrayList<>(map1.keySet());
                            for(int i =0; i<6 && i<keyList.size()-1 ; i++){
                                try{
                                    Date date1 = dateFormat.parse(keyList.get(keyList.size()-1-i));
                                    Date date2 = dateFormat.parse(keyList.get(keyList.size()-1-(i+1)));
                                    long day = (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);

                                    entries.add(new BarEntry(i,day));

                                }catch(Exception e){

                                    Log.d("FeatureFragment",e.toString());
                                }
                            }
                            BarDataSet barDataSetP1 = new BarDataSet(entries,"1번빨래통");
                            barDataSetP1.setColor(Color.RED);
                            barDataSetP1.setValueTextColor(Color.RED);
                            barDataSetP1.setValueTextSize(13f);



                            barDataP.addDataSet(barDataSetP1);
                            barDataP.setBarWidth(0.5f);




                            periodChart1.animateY(500);
                            periodChart1.setTouchEnabled(false);
                            periodChart1.setMaxVisibleValueCount(5);
                            periodChart1.setData(barDataP);

                            XAxis xAxis = periodChart1.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정
                            xAxis.setValueFormatter(new ValueFormatter() {

                                @Override
                                public String getFormattedValue(float value) {
                                    return labels[(int) value];
                                }
                            });
                            YAxis yAxisLeft = periodChart1.getAxisLeft();
                            yAxisLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.black)); //Y축 텍스트 컬러 설정
                            yAxisLeft.setGridColor(ContextCompat.getColor(getContext(), R.color.black)); // Y축 줄의 컬러 설정

                            YAxis yAxisRight = periodChart1.getAxisRight(); //Y축의 오른쪽면 설정
                            yAxisRight.setDrawLabels(false);
                            yAxisRight.setDrawAxisLine(false);
                            yAxisRight.setDrawGridLines(false);

                            periodChart1.invalidate();

                        }

                    }
                    else{

                    }
                }
            });
            docRef.collection("featureAnalysis").document("period2").
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(task!=null){

                        Map map = document.getData();

                        if(map.size() != 0) {
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            TreeMap map1 = new TreeMap(map);
                            List<String> keyList = new ArrayList<>(map1.keySet());
                            for (int i = 0; i < 6 && i < keyList.size() - 1; i++) {
                                                                try{
                                    Date date1 = dateFormat.parse(keyList.get(keyList.size()-1-i));
                                    Date date2 = dateFormat.parse(keyList.get(keyList.size()-1-(i+1)));
                                    long day = (date1.getTime() - date2.getTime()) / (1000 * 60 * 60 * 24);

                                    entries.add(new BarEntry(i,day));

                                }catch(Exception e){

                                    Log.d("FeatureFragment",e.toString());
                                }
                            }
                            BarDataSet barDataSetP2 = new BarDataSet(entries, "2번빨래통");
                            barDataSetP2.setColor(Color.BLUE);
                            barDataSetP2.setValueTextColor(Color.BLUE);
                            barDataSetP2.setValueTextSize(13f);

                            barDataP.addDataSet(barDataSetP2);

                            barDataP.setBarWidth(0.25f);

                            XAxis xAxis = periodChart1.getXAxis();
                            xAxis.setGranularity(1f);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정
                            xAxis.setValueFormatter(new ValueFormatter() {

                                @Override
                                public String getFormattedValue(float value) {
                                    return labels[(int) value];
                                }
                            });
                            YAxis yAxisLeft = periodChart1.getAxisLeft();
                            yAxisLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.black)); //Y축 텍스트 컬러 설정
                            yAxisLeft.setGridColor(ContextCompat.getColor(getContext(), R.color.black)); // Y축 줄의 컬러 설정

                            YAxis yAxisRight = periodChart1.getAxisRight(); //Y축의 오른쪽면 설정
                            yAxisRight.setDrawLabels(false);
                            yAxisRight.setDrawAxisLine(false);
                            yAxisRight.setDrawGridLines(false);

                            periodChart1.animateY(500);
                            periodChart1.setTouchEnabled(false);
                            periodChart1.setMaxVisibleValueCount(5);
                            periodChart1.setData(barDataP);
                            periodChart1.invalidate();


                        }
                    }
                    else{

                    }
                }
            });


            docRef.collection("featureAnalysis").document("weight_increment1").
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(task!=null){

                        Map map = document.getData();
                        if(map.size() != 0) {
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            TreeMap map1 = new TreeMap(map);
                            List<String> keyList = new ArrayList<>(map1.keySet());
                            for (int i = 0; i < 7 && i < keyList.size() - 1; i++) {
                                float weight = Float.valueOf(String.valueOf(map.get(keyList.get(keyList.size()-1-i))));
                                entries.add(new BarEntry(i, weight));
                            }
                            BarDataSet barDataSetW1 = new BarDataSet(entries, "1번빨래통");
                            barDataSetW1.setColor(Color.RED);
                            barDataSetW1.setValueTextColor(Color.RED);
                            barDataSetW1.setValueTextSize(13f);


                            barDataW.addDataSet(barDataSetW1);

                            barDataW.setBarWidth(0.5f);

                            XAxis xAxis = incChart1.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정
                            xAxis.setValueFormatter(new ValueFormatter() {

                                @Override
                                public String getFormattedValue(float value) {
                                    return labels[(int) value];
                                }
                            });
                            xAxis.setGranularity(1f);
                            YAxis yAxisLeft = incChart1.getAxisLeft();
                            yAxisLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.black)); //Y축 텍스트 컬러 설정
                            yAxisLeft.setGridColor(ContextCompat.getColor(getContext(), R.color.black)); // Y축 줄의 컬러 설정

                            YAxis yAxisRight = incChart1.getAxisRight(); //Y축의 오른쪽면 설정
                            yAxisRight.setDrawLabels(false);
                            yAxisRight.setDrawAxisLine(false);
                            yAxisRight.setDrawGridLines(false);

                            incChart1.animateY(500);
                            incChart1.setMaxVisibleValueCount(7);
                            incChart1.setTouchEnabled(false);
                            incChart1.setData(barDataW);
                            incChart1.invalidate();
                        }

                    }


                    else{

                    }
                }
            });
            docRef.collection("featureAnalysis").document("weight_increment2").
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot document = task.getResult();
                    if(task!=null){
                        Map map = document.getData();
                        if(map.size() != 0) {
                            ArrayList<BarEntry> entries = new ArrayList<>();
                            TreeMap map1 = new TreeMap(map);
                            List<String> keyList = new ArrayList<>(map1.keySet());
                            for (int i = 0; i < 7 && i < keyList.size() - 1; i++) {
                                float weight = Float.valueOf(String.valueOf(map.get(keyList.get(keyList.size()-1-i))));
                                entries.add(new BarEntry(i, weight));
                            }
                            BarDataSet barDataSetW1 = new BarDataSet(entries, "2번빨래통");
                            barDataSetW1.setColor(Color.BLUE);
                            barDataSetW1.setValueTextColor(Color.BLUE);
                            barDataSetW1.setValueTextSize(13f);


                            barDataW.addDataSet(barDataSetW1);

                            barDataW.setBarWidth(0.5f);

                            XAxis xAxis = incChart1.getXAxis();
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x 축 표시에 대한 위치 설정
                            xAxis.setValueFormatter(new ValueFormatter() {

                                @Override
                                public String getFormattedValue(float value) {
                                    return labels[(int) value];
                                }
                            });
                            xAxis.setGranularity(1f);
                            YAxis yAxisLeft = incChart1.getAxisLeft();
                            yAxisLeft.setTextColor(ContextCompat.getColor(getContext(), R.color.black)); //Y축 텍스트 컬러 설정
                            yAxisLeft.setGridColor(ContextCompat.getColor(getContext(), R.color.black)); // Y축 줄의 컬러 설정

                            YAxis yAxisRight = incChart1.getAxisRight(); //Y축의 오른쪽면 설정
                            yAxisRight.setDrawLabels(false);
                            yAxisRight.setDrawAxisLine(false);
                            yAxisRight.setDrawGridLines(false);

                            incChart1.animateY(500);
                            incChart1.setMaxVisibleValueCount(7);
                            incChart1.setTouchEnabled(false);
                            incChart1.setData(barDataW);
                            incChart1.invalidate();

                        }
                    }
                    else{

                    }
                }
            });



        }


        String averinc1 = String.format("%.2f",activity.userFeature.getAver_inc1());
        String averinc2 = String.format("%.2f",activity.userFeature.getAver_inc2());
        String averperiod1 = String.format("%.2f",activity.userFeature.getPriod1());
        String averperiod2 = String.format("%.2f",activity.userFeature.getPriod2());

        averInc1.setText(averinc1+"kg");
        averInc2.setText(averinc2+"kg");
        averPeriod1.setText(averperiod1+"일");
        averPeriod2.setText(averperiod2+"일");



        return rootview;
    }
}