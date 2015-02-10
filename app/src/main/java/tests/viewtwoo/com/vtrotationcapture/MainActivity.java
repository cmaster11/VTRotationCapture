package tests.viewtwoo.com.vtrotationcapture;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity implements SensorEventListener, SurfaceHolder.Callback {

    public final static String TAG = MainActivity.class.toString();

    TextView accelerometerLogTextViewX;
    TextView accelerometerLogTextViewY;
    TextView accelerometerLogTextViewZ;
    TextView accelerometerLogTextViewDegreeZ;
    TextView accelerometerLogTextViewDegreeX;
    TextView accelerometerLogTextViewDegreeY;
    TextView accelerometerLogTotalAcceleration;
    TextView sensorAverageCountTextView;
    TextView sensorCountLimitTextView;
    TextView sensorGyroXTextView;
    TextView sensorGyroYTextView;
    TextView sensorGyroZTextView;
    Button recordBtn;
    Button saveDeviceInfoBtn;
    CheckBox bRecordVideoCheckBox;

    public MainActivity() {

    }

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    Display mDisplay;

    private int accValuesLimit = 100;
    private int gyroValuesLimit = 50;

    HashMap<String, StatisticList> stats;

    /*StatisticList accX;
    StatisticList accY;
    StatisticList accZ;
    StatisticList accDegreeZ;
    StatisticList accDegreeX;
    StatisticList accDegreeY;
    StatisticList accTotalAcceleration;
*/
    // Recording section
    private boolean isRecording = false;

    // Recording acceleration section
    private String baseRecordingDirectoryPath = null;
    private BufferedWriter recordingFileBufferedWriter = null;
    private String currentRecordingAccFilename = null;
    private long recordingStartNanoSeconds = 0;
    private boolean recordingJustBegan = true;

    // Recording video section
    private String currentRecordingVideoFilename = null;
    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mHolder = null;
    private MediaRecorder mMediaRecorder = null;
    private Camera mCamera = null;
    private boolean mCameraInitSuccesful;
    private boolean isRecordingVideo = false;

    // Save device info section
    private String currentDeviceInfoFilename = null;
    private BufferedWriter deviceInfoBufferedWriter = null;

    private void initStats()
    {

        stats = new HashMap<>();
        stats.put("accX", new StatisticList(accValuesLimit));
        stats.put("accY", new StatisticList(accValuesLimit));
        stats.put("accZ", new StatisticList(accValuesLimit));
        stats.put("accDegreeZ", new StatisticList(accValuesLimit));
        stats.put("accDegreeX", new StatisticList(accValuesLimit));
        stats.put("accDegreeY", new StatisticList(accValuesLimit));
        stats.put("accTotalAcceleration", new StatisticList(accValuesLimit));
        stats.put("gyroX", new StatisticList(gyroValuesLimit));
        stats.put("gyroY", new StatisticList(gyroValuesLimit));
        stats.put("gyroZ", new StatisticList(gyroValuesLimit));


        stats.get("accX").setCalculateAtAdd(true);
        stats.get("accY").setCalculateAtAdd(true);
        stats.get("accZ").setCalculateAtAdd(true);
        stats.get("accDegreeZ").setCalculateAtAdd(true);
        stats.get("accDegreeX").setCalculateAtAdd(true);
        stats.get("accDegreeY").setCalculateAtAdd(true);
        stats.get("accTotalAcceleration").setCalculateAtAdd(true);
        stats.get("gyroX").setCalculateAtAdd(true);
        stats.get("gyroY").setCalculateAtAdd(true);
        stats.get("gyroZ").setCalculateAtAdd(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        WindowManager mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        accelerometerLogTextViewX = (TextView) findViewById(R.id.accelerometerLogTextViewX);
        accelerometerLogTextViewY = (TextView) findViewById(R.id.accelerometerLogTextViewY);
        accelerometerLogTextViewZ = (TextView) findViewById(R.id.accelerometerLogTextViewZ);
        accelerometerLogTextViewDegreeZ = (TextView) findViewById(R.id.accelerometerLogTextViewDegreeZ);
        accelerometerLogTextViewDegreeX = (TextView) findViewById(R.id.accelerometerLogTextViewDegreeX);
        accelerometerLogTextViewDegreeY = (TextView) findViewById(R.id.accelerometerLogTextViewDegreeY);
        accelerometerLogTotalAcceleration = (TextView) findViewById(R.id.accelerometerLogTotalAcceleration);
        sensorAverageCountTextView = (TextView) findViewById(R.id.sensorAverageCountTextView);
        sensorCountLimitTextView = (TextView) findViewById(R.id.sensorCountLimitTextView);
        sensorGyroXTextView = (TextView) findViewById(R.id.sensorGyroXTextView);
        sensorGyroYTextView = (TextView) findViewById(R.id.sensorGyroYTextView);
        sensorGyroZTextView = (TextView) findViewById(R.id.sensorGyroZTextView);

        // Recording
        if (baseRecordingDirectoryPath == null) {
            baseRecordingDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/VTRotationCapture");
            File baseRecordingDirectory = new File(baseRecordingDirectoryPath);
            if (!baseRecordingDirectory.isDirectory()) {
                if (!baseRecordingDirectory.mkdir()) {
                    Log.e(TAG, "Cannot create recording folder!");
                }
            }
        }

        // Recording camera
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);

        // Recording
        recordBtn = (Button) findViewById(R.id.recordBtn);
        saveDeviceInfoBtn = (Button) findViewById(R.id.saveDeviceInfoBtn);
        bRecordVideoCheckBox = (CheckBox) findViewById(R.id.bRecordVideoCheckBox);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {

                    // Acceleration
                    Calendar c = Calendar.getInstance();
                    currentRecordingAccFilename = String.format("%s/%d_%d_%d_%d_%d_%d.csv",
                            baseRecordingDirectoryPath,
                            c.get(Calendar.YEAR),
                            c.get(Calendar.MONTH) + 1,
                            c.get(Calendar.DAY_OF_MONTH),
                            c.get(Calendar.HOUR_OF_DAY),
                            c.get(Calendar.MINUTE),
                            c.get(Calendar.SECOND)
                    );
                    try {
                        recordingFileBufferedWriter = new BufferedWriter(new FileWriter(currentRecordingAccFilename));

                    } catch (IOException e) {
                        Log.e(TAG, "Cannot create recording file!", e);
                    }

                    if (bRecordVideoCheckBox.isChecked()) {
                        bRecordVideoCheckBox.setEnabled(false);
                        // Camera video
                        currentRecordingVideoFilename = String.format("%s/%d_%d_%d_%d_%d_%d.mp4",
                                baseRecordingDirectoryPath,
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH) + 1,
                                c.get(Calendar.DAY_OF_MONTH),
                                c.get(Calendar.HOUR_OF_DAY),
                                c.get(Calendar.MINUTE),
                                c.get(Calendar.SECOND)
                        );

                        mMediaRecorder.setOutputFile(currentRecordingVideoFilename);

                        try {
                            mMediaRecorder.prepare();
                        } catch (IllegalStateException e) {
                            Log.e(TAG, "Cannot prepare MediaRecorder!", e);
                        } catch (IOException e) {
                            Log.e(TAG, "Cannot prepare MediaRecorder!", e);
                        }

                        // http://stackoverflow.com/questions/23948573/record-video-using-surface-view-android

                        mMediaRecorder.start();

                        isRecordingVideo = true;
                    } else
                        isRecordingVideo = false;

                    Log.d(TAG, "Recording started!");
                    Toast.makeText(MainActivity.this, "Recording started!", Toast.LENGTH_LONG).show();

                    recordBtn.setText(R.string.recordBtnStop);
                    recordingStartNanoSeconds = System.nanoTime();
                    isRecording = true;
                    recordingJustBegan = true;

                } else {

                    if (isRecordingVideo) {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        try {
                            initRecorder(mHolder.getSurface());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        isRecordingVideo = false;
                    } else
                        currentRecordingVideoFilename = null;

                    if (recordingFileBufferedWriter != null) {
                        try {
                            recordingFileBufferedWriter.close();

                        } catch (IOException e) {
                            Log.e(TAG, "Cannot close recording file!", e);
                        }
                    }

                    shareMultipleFiles(currentRecordingAccFilename, currentRecordingVideoFilename);
                    Toast.makeText(MainActivity.this, "Recording completed!", Toast.LENGTH_LONG).show();

                    isRecording = false;
                    recordBtn.setText(R.string.recordBtnStart);
                    bRecordVideoCheckBox.setEnabled(true);
                }


            }
        });

        saveDeviceInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDeviceInfoFilename = String.format("%s/deviceInfo.csv",
                        baseRecordingDirectoryPath
                );

                try {

                    String deviceInfoHeader = "";
                    String deviceInfoString = "";
                    Field[] fields = Build.class.getFields();
                    Build build = new Build();
                    //Class<Build> c = Class.forName(Build.class.getDeclaredFields())

                    for (Field field : fields) {
                        deviceInfoHeader += field.getName() + CSV_SEPARATOR;
                        deviceInfoString += field.get(build).toString() + CSV_SEPARATOR;
                    }

                    if (deviceInfoHeader.length() > 0) {
                        deviceInfoHeader = deviceInfoHeader.substring(0, deviceInfoHeader.length() - 1) + "\n";
                        deviceInfoString = deviceInfoString.substring(0, deviceInfoString.length() - 1);
                    }

                    deviceInfoBufferedWriter = new BufferedWriter(new FileWriter(currentDeviceInfoFilename));

                    deviceInfoBufferedWriter.write(deviceInfoHeader);
                    deviceInfoBufferedWriter.write(deviceInfoString);

                    deviceInfoBufferedWriter.close();

                    shareCsvFile(currentDeviceInfoFilename);

                } catch (IOException e) {
                    Log.e(TAG, "Cannot create device info file!", e);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Cannot create device info file!", e);
                }
            }
        });

        /*if(savedInstanceState != null)
        {
            stats = (HashMap<String, StatisticList>) savedInstanceState.getSerializable("stats");
        }
        else
        {*/



        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    private void initRecorder(Surface surface) throws IOException {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.unlock();
        }

        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
        //mMediaRecorder.setPreviewDisplay(surface);
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        mMediaRecorder.setProfile(CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setVideoFrameRate(30);
        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setOrientationHint(90);

        mCameraInitSuccesful = true;
    }

    private void shareCsvFile(String fileName) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri csvFilePath = Uri.parse("file://" + fileName);
        sharingIntent.setType("text/csv");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, csvFilePath);
        startActivity(Intent.createChooser(sharingIntent, "Share csv file using"));
    }

    private void shareMultipleFiles(String fileName1, String fileName2) {
        if (fileName1 == null && fileName2 == null)
            return;

        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> uris = new ArrayList<>();
        if (fileName1 != null)
            uris.add(Uri.parse("file://" + fileName1));
        if (fileName2 != null)
            uris.add(Uri.parse("file://" + fileName2));
        sharingIntent.setType("*/*");
        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();

        initStats();

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    double mAccelerometerSensorX, mAccelerometerSensorY, mAccelerometerSensorZ, mAccelerometerTotalAcceleration, mRotationDegreeZ, mRotationDegreeX, mRotationDegreeY;
    double mGyroX, mGyroY, mGyroZ;
    double tmp_x, tmp_y, tmp_z, tmp_a, tmp_b;

    private static final double g = 9.80665;

    private long nanotime = 0;
    private boolean sensorFirstRead = true;
    private long sensorCount = 0;
    private long sensorCounterIntervalDelay = 1000 * 1000000; // ms
    private double sensorAverageCount = 0;
    private long differenceNanoTime = 0;
    private double differenceSeconds = 0;
    private boolean sensorFirstAverageCycle = true;

    public void onSensorChanged(SensorEvent event) {


        //Right in here is where you put code to read the current sensor values and
        //update any views you might have that are displaying the sensor information
        //You'd get accelerometer values like this:
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER && event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
            return;

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorCount++;

            if (sensorFirstRead) {
                nanotime = System.nanoTime();
                sensorFirstRead = false;
            } else {

                if (sensorCount > 0 && sensorCount % 10 == 0) {
                    differenceNanoTime = System.nanoTime() - nanotime;
                    if (differenceNanoTime > sensorCounterIntervalDelay) {
                        differenceSeconds = (double) differenceNanoTime / 1000000000f;

                        sensorAverageCount = sensorCount / differenceSeconds;
                        sensorAverageCountTextView.setText(String.format("Read/s: %.2f", sensorAverageCount));

                        if (sensorFirstAverageCycle || Math.abs(sensorAverageCount - accValuesLimit * 2) > accValuesLimit * 0.2) {
                            if (sensorFirstAverageCycle)
                                sensorFirstAverageCycle = false;

                            accValuesLimit = (int) sensorAverageCount / 2;
                            gyroValuesLimit = accValuesLimit / 2;
                            sensorCountLimitTextView.setText(String.format("Sensor count limit: %d", accValuesLimit));

                            stats.get("accX").setLimit(accValuesLimit);
                            stats.get("accY").setLimit(accValuesLimit);
                            stats.get("accZ").setLimit(accValuesLimit);
                            stats.get("accDegreeZ").setLimit(accValuesLimit);
                            stats.get("accDegreeX").setLimit(accValuesLimit);
                            stats.get("accDegreeY").setLimit(accValuesLimit);
                            stats.get("accTotalAcceleration").setLimit(accValuesLimit);
                            stats.put("gyroX", new StatisticList(gyroValuesLimit));
                            stats.put("gyroY", new StatisticList(gyroValuesLimit));
                            stats.put("gyroZ", new StatisticList(gyroValuesLimit));
                        }

                        sensorCount = 0;
                        nanotime = System.nanoTime();
                    }
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            mAccelerometerSensorX = event.values[0];
            mAccelerometerSensorY = event.values[1];
            mAccelerometerSensorZ = event.values[2];

            mAccelerometerTotalAcceleration = Math.sqrt(Math.pow(mAccelerometerSensorX, 2) + Math.pow(mAccelerometerSensorY, 2) + Math.pow(mAccelerometerSensorZ, 2));

            stats.get("accX").addValue(mAccelerometerSensorX);
            stats.get("accY").addValue(mAccelerometerSensorY);
            stats.get("accZ").addValue(mAccelerometerSensorZ);
            stats.get("accTotalAcceleration").addValue(mAccelerometerTotalAcceleration);

            tmp_y = stats.get("accY").getAverage() / stats.get("accTotalAcceleration").getAverage(); // g
            tmp_x = stats.get("accX").getAverage() / stats.get("accTotalAcceleration").getAverage();
            tmp_z = stats.get("accZ").getAverage() / stats.get("accTotalAcceleration").getAverage();

            mRotationDegreeZ = Math.asin(tmp_x) * 180 / Math.PI;
            if (Double.isNaN(mRotationDegreeZ))
                mRotationDegreeZ = stats.get("accDegreeZ").getAverage() > 0 ? 90 : -90;

            mRotationDegreeX = Math.asin(tmp_z) * 180 / Math.PI;
            if (Double.isNaN(mRotationDegreeX))
                mRotationDegreeX = stats.get("accDegreeX").getAverage() > 0 ? 90 : -90;

            mRotationDegreeY = Math.asin(tmp_y) * 180 / Math.PI;
            if (Double.isNaN(mRotationDegreeY))
                mRotationDegreeY = stats.get("accDegreeY").getAverage() > 0 ? 90 : -90;

            stats.get("accDegreeZ").addValue(mRotationDegreeZ);
            stats.get("accDegreeX").addValue(mRotationDegreeX);
            stats.get("accDegreeY").addValue(mRotationDegreeY);

            updateLog(Sensor.TYPE_ACCELEROMETER);
        }
        else
            if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE)
            {
                mGyroX = event.values[0];
                mGyroY = event.values[1];
                mGyroZ = event.values[2];

                stats.get("gyroX").addValue(mGyroX);
                stats.get("gyroY").addValue(mGyroY);
                stats.get("gyroZ").addValue(mGyroZ);

                updateLog(Sensor.TYPE_GYROSCOPE);
            }
    }

    private String currentLog;
    private double currentTimeDifference;
    private String CSV_SEPARATOR = ",";

    private long updateVisualNanoTime = 0;
    private long updateNanoTime = 0;
    private boolean updateFirstRun = true;

    private static final long minUpdateWriteTime = 10 * 1000000;// ms

    private void updateLog(int mSensorType) {
        // Accelerometer
        //currentLog = String.format("X: %.5f, Y: %.5f, Degree: %.5f", mAccelerometerSensorX,mAccelerometerSensorY, mRotationDegreeZ);

        //currentLog = "X: " + mAccelerometerSensorX + " Y: " + mAccelerometerSensorY + " Angle: " + mRotationDegreeZ;

        if (updateFirstRun) {
            updateVisualNanoTime = System.nanoTime();
            updateNanoTime = System.nanoTime();
            updateFirstRun = false;
        }

        if(System.nanoTime() - updateNanoTime > minUpdateWriteTime) {

            updateNanoTime = System.nanoTime();

            if (isRecording) {
                if (recordingJustBegan) {
                    recordingJustBegan = false;
                    currentLog = String.format("time%saccX%saccY%saccZ%sdegX%sdegY%sdegZ%sgyroX%sgyroY%sgyroZ\n",
                            CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR, CSV_SEPARATOR);
                } else {
                    currentTimeDifference = (double) (System.nanoTime() - recordingStartNanoSeconds) / 1000000000f;
                    currentLog = String.format("%.5f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f%s%.3f\n",
                            currentTimeDifference, CSV_SEPARATOR,
                            stats.get("accX").getAverage(), CSV_SEPARATOR,
                            stats.get("accY").getAverage(), CSV_SEPARATOR,
                            stats.get("accZ").getAverage(), CSV_SEPARATOR,
                            stats.get("accDegreeX").getAverage(), CSV_SEPARATOR,
                            stats.get("accDegreeY").getAverage(), CSV_SEPARATOR,
                            stats.get("accDegreeZ").getAverage(), CSV_SEPARATOR,
                            stats.get("gyroX").getAverage(), CSV_SEPARATOR,
                            stats.get("gyroY").getAverage(), CSV_SEPARATOR,
                            stats.get("gyroZ").getAverage()
                    );
                }

                try {
                    recordingFileBufferedWriter.write(currentLog);
                } catch (IOException e) {
                    Log.e(TAG, "Cannot log current values!", e);
                }
            }


        }

        if (System.nanoTime() - updateVisualNanoTime > 250 * 1000000) {
            updateVisualNanoTime = System.nanoTime();
            accelerometerLogTotalAcceleration.setText(String.format("TotAcc: %.5f, %s",  stats.get("accTotalAcceleration").getAverage(), stats.get("accTotalAcceleration")));
            accelerometerLogTextViewX.setText(String.format("X: %.5f, %s", stats.get("accX").getAverage(), stats.get("accX")));
            accelerometerLogTextViewY.setText(String.format("Y: %.5f,%s", stats.get("accY").getAverage(), stats.get("accY")));
            accelerometerLogTextViewZ.setText(String.format("Z: %.5f, %s", stats.get("accZ").getAverage(), stats.get("accZ")));
            accelerometerLogTextViewDegreeZ.setText(String.format("Z_D: %.5f, %s", stats.get("accDegreeX").getAverage(), stats.get("accDegreeZ")));
            accelerometerLogTextViewDegreeY.setText(String.format("Y_D: %.5f, %s", stats.get("accDegreeY").getAverage(), stats.get("accDegreeY")));
            accelerometerLogTextViewDegreeX.setText(String.format("X_D: %.5f, %s", stats.get("accDegreeZ").getAverage(), stats.get("accDegreeX")));
            sensorGyroXTextView.setText(String.format("G_X: %.5f, %s", stats.get("gyroX").getAverage(), stats.get("gyroX")));
            sensorGyroYTextView.setText(String.format("G_Y: %.5f, %s", stats.get("gyroY").getAverage(), stats.get("gyroY")));
            sensorGyroZTextView.setText(String.format("G_Z: %.5f, %s", stats.get("gyroZ").getAverage(), stats.get("gyroZ")));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        //  state.putSerializable("stats", stats);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
         //   if (!mCameraInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
    }
}
