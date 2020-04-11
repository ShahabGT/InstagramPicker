package ir.shahabazimi.instagrampicker.gallery;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ir.shahabazimi.instagrampicker.InstagramPicker;
import ir.shahabazimi.instagrampicker.R;
import ir.shahabazimi.instagrampicker.filter.FilterActivity;

import static android.app.Activity.RESULT_OK;


public class CameraFragment extends Fragment {

    public static Bitmap finalImage;

    public CameraFragment() {
    }


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private AutoFitTextureView textureView;
    private ImageView change, capture;
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private File file;
    private String[] cameraIdList;
    private boolean front = false;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_camera, container, false);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setTitle(getString(R.string.instagrampicker_camera_title));
        setHasOptionsMenu(true);

        textureView = v.findViewById(R.id.c_texture);
        textureView.setAspectRatio(1,1);

        capture = v.findViewById(R.id.c_capture);
        change = v.findViewById(R.id.c_change);

        textureView.setSurfaceTextureListener(textureListener);
        capture.setOnClickListener(w -> takePicture());
        change.setOnClickListener(w -> {
            stopCamera();
            if (front) {
                openCamera(cameraIdList[0]);
                front = false;
            } else {
                openCamera(cameraIdList[1]);
                front = true;
            }
        });

        return v;
    }

    private void takePicture() {
        if (cameraDevice == null)
            return;
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);

            int width = 1000;
            int height = 1000;
            if (jpegSizes != null && jpegSizes.length > 0) {
//                int i=0;
//                for (Size s:jpegSizes){
//                    if(s.getWidth()==s.getHeight())
//                    {
//                        width = jpegSizes[i].getWidth();
//                        height = jpegSizes[i].getHeight();
//                        break;
//                    }
//                    i++;
//                }
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            file = File.createTempFile(UUID.randomUUID().toString(), ".jpeg", getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES));

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        {
                            if (image != null)
                                image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    try (OutputStream outputStream = new FileOutputStream(file)) {
                        outputStream.write(bytes);
                        startCropping(file);
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
        Surface surface = new Surface(texture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera(@Nullable String id) {
        try {
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

            cameraIdList = manager.getCameraIdList();
            if (id == null)
                cameraId = manager.getCameraIdList()[0];
            else
                cameraId = id;

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            for(Size s : map.getOutputSizes(SurfaceTexture.class)){
                if(s.getWidth()==s.getHeight()){
                    imageDimension=s;
                    break;
                }
            }

            if (ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(),"Please Enable Camera Permission",Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.select_container, new CameraFragment())
                        .commit();

                return;
            }
            manager.openCamera(cameraId, stateCallBack, null);
        } catch (Exception e) {
        }
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(null);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        textureView.setVisibility(View.GONE);
        stopCamera();
        stopBackgroundThread();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        textureView.setVisibility(View.VISIBLE);

        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera(null);
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread=null;
            mBackgroundHandler=null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopCamera(){
        try {
            if (cameraCaptureSession != null) {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
        }catch (Exception e){}
        finally {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
                cameraCaptureSession = null;
            }
        }
    }

    private void startCropping(File f){
        int x= InstagramPicker.x;
        int y=InstagramPicker.y;
        CropImage.activity(Uri.fromFile(f))
                .setAspectRatio(x,y)
                .start(getContext(), this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Intent in = new Intent(getContext(), FilterActivity.class);
                in.putExtra("uri",resultUri);
                FilterActivity.picAddress=resultUri;
                startActivityForResult(in,444);
            }
        }
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.action_open);
        if(item!=null)
            item.setVisible(false);
    }

}
