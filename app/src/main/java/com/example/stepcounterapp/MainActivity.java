package com.example.stepcounterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.stepcounterapp.databinding.ActivityMainBinding;
import com.example.stepcounterapp.permissionUtil.PermissionManager;
import com.example.stepcounterapp.permissionUtil.Permissions;
import com.example.stepcounterapp.utils.CommonUtils;


import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
public class MainActivity extends AppCompatActivity implements PermissionManager.PermissionListener, OnSuccessListener {

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private final String TAG = "MainActivity";
    private FitnessOptions fitnessOptions;
    private FitnessDataResponseModel fitnessDataResponseModel;
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initialization();
        checkPermissions();
    }

    private void initialization() {
        fitnessDataResponseModel = new FitnessDataResponseModel();
    }

    private void checkPermissions() {
        if (!PermissionManager.hasPermissions(this, Permissions.LOCATION_PERMISSION)) {
            PermissionManager.requestPermissions(this, this, "", Permissions.LOCATION_PERMISSION);
        } else {
            checkGoogleFitPermission();
        }
    }

    private void checkGoogleFitPermission() {

        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)

                .build();
        GoogleSignInAccount account = getGoogleAccount();

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    MainActivity.this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions);
        } else {
            startDataReading();
        }

    }

    private void startDataReading() {

        getTodayData();

        subscribeAndGetRealTimeData(DataType.TYPE_STEP_COUNT_DELTA);

    }

    private void subscribeAndGetRealTimeData(DataType dataType) {
        Fitness.getRecordingClient(this, getGoogleAccount())
                .subscribe(dataType)
                .addOnSuccessListener(aVoid -> {
                    Log.e(TAG, "Subscribed");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failure " + e.getLocalizedMessage());
                });

        getDataUsingSensor(dataType);

    }

    private void getDataUsingSensor(DataType dataType) {
        Fitness.getSensorsClient(this, getGoogleAccount())
                .add(new SensorRequest.Builder()
                                .setDataType(dataType)
                                .setSamplingRate(1, TimeUnit.SECONDS)  // sample once per minute
                                .build(),
                        new OnDataPointListener() {
                            @Override
                            public void onDataPoint(@NonNull DataPoint dataPoint) {
                                float value = Float.parseFloat(dataPoint.getValue(Field.FIELD_STEPS).toString());
                                fitnessDataResponseModel.steps = Float.parseFloat(new DecimalFormat("#.##").format(value + fitnessDataResponseModel.steps));
                                activityMainBinding.setFitnessData(fitnessDataResponseModel);
                            }
                        }
                );
    }


    private void getTodayData() {

        Fitness.getHistoryClient(this, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(this);
        Fitness.getHistoryClient(this, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(this);
        Fitness.getHistoryClient(this, getGoogleAccount())
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(this);
    }


    private void requestForHistory() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();

        cal.set(2021, 2, 5);
        cal.set(Calendar.HOUR_OF_DAY, 0); //so it get all day and not the current hour
        cal.set(Calendar.MINUTE, 0); //so it get all day and not the current minute
        cal.set(Calendar.SECOND, 0); //so it get all day and not the current second
        long startTime = cal.getTimeInMillis();


        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
                .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED)
                .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_DISTANCE_DELTA)
                .aggregate(DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.HOURS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, getGoogleAccount())
                .readData(readRequest)
                .addOnSuccessListener(this);
    }

    private GoogleSignInAccount getGoogleAccount() {
        return GoogleSignIn.getAccountForExtension(MainActivity.this, fitnessOptions);
    }


    @Override
    public void onSuccess(Object o) {
        if (o instanceof DataSet) {
            DataSet dataSet = (DataSet) o;
            if (dataSet != null) {
                getDataFromDataSet(dataSet);
            }
        } else if (o instanceof DataReadResponse) {
            fitnessDataResponseModel.steps = 0f;
            fitnessDataResponseModel.distance = 0f;
            DataReadResponse dataReadResponse = (DataReadResponse) o;
            if (dataReadResponse.getBuckets() != null && !dataReadResponse.getBuckets().isEmpty()) {
                List<Bucket> bucketList = dataReadResponse.getBuckets();

                if (bucketList != null && !bucketList.isEmpty()) {
                    for (Bucket bucket : bucketList) {
                        DataSet stepsDataSet = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                        getDataFromDataReadResponse(stepsDataSet);
                        DataSet distanceDataSet = bucket.getDataSet(DataType.TYPE_DISTANCE_DELTA);
                        getDataFromDataReadResponse(distanceDataSet);
                    }
                }
            }
        }

    }

    private void getDataFromDataReadResponse(DataSet dataSet) {

        List<DataPoint> dataPoints = dataSet.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            for (Field field : dataPoint.getDataType().getFields()) {
                Log.e(TAG, " data manual history : " + dataPoint.getOriginalDataSource().getStreamName());

                float value = Float.parseFloat(dataPoint.getValue(field).toString());
                Log.e(TAG, " data : " + value);

                if (field.getName().equals(Field.FIELD_STEPS.getName())) {
                    fitnessDataResponseModel.steps = Float.parseFloat(new DecimalFormat("#.##").format(value + fitnessDataResponseModel.steps));
                }else if (field.getName().equals(Field.FIELD_DISTANCE.getName())) {
                    fitnessDataResponseModel.distance = Float.parseFloat(new DecimalFormat("#.##").format(value + fitnessDataResponseModel.distance));
                }
            }
        }
        activityMainBinding.setFitnessData(fitnessDataResponseModel);
    }

    private void getDataFromDataSet(DataSet dataSet) {

        List<DataPoint> dataPoints = dataSet.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
            Log.e(TAG, " data manual : " + dataPoint.getOriginalDataSource().getStreamName());

            for (Field field : dataPoint.getDataType().getFields()) {

                float value = Float.parseFloat(dataPoint.getValue(field).toString());
                Log.e(TAG, " data : " + value);

                if (field.getName().equals(Field.FIELD_STEPS.getName())) {
                    fitnessDataResponseModel.steps = Float.parseFloat(new DecimalFormat("#.##").format(value));
                }else if (field.getName().equals(Field.FIELD_DISTANCE.getName())) {
                    fitnessDataResponseModel.distance = Float.parseFloat(new DecimalFormat("#.##").format(value));
                }
            }
        }
        activityMainBinding.setFitnessData(fitnessDataResponseModel);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            startDataReading();
        }
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
        if (perms != null && perms.size() == Permissions.LOCATION_PERMISSION.length) {
            checkGoogleFitPermission();
        }
    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
        if (perms.size() > 0) {
            PermissionManager.requestPermissions(this, this, "", Permissions.LOCATION_PERMISSION);
        }
    }

    @Override
    public void onPermissionNeverAsked(List<String> perms) {
        CommonUtils.openSettingForPermission(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.onRequestPermissionsResult(this, this, requestCode, permissions, grantResults);
    }

}