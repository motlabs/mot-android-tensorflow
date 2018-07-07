package jeongari.com.turtleneck;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    public static final String TAG = CameraPreview.class.getSimpleName();

    private Camera mCamera;
    public List<Camera.Size> listPreviewSizes;
    private Camera.Size previewSize;
    private Context context;

    Bitmap captureImg;

    // SurfaceView 생성자
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mCamera = MainActivity.getCamera();
        if(mCamera == null){
            mCamera = Camera.open();
        }
        listPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();


    }



    //  SurfaceView 생성시 호출
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            // 카메라 객체를 사용할 수 있게 연결한다.
            if(mCamera  == null){
                mCamera  = Camera.open();
            }

            // 카메라 설정
            Camera.Parameters parameters = mCamera .getParameters();

            // 카메라의 회전이 가로/세로일때 화면을 설정한다.
            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait");
                mCamera.setDisplayOrientation(90);
                parameters.setRotation(90);
            } else {
                parameters.set("orientation", "landscape");
                mCamera.setDisplayOrientation(0);
                parameters.setRotation(0);
            }
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(surfaceHolder);

            // 카메라 미리보기를 시작한다.
            mCamera.startPreview();

            // 자동포커스 설정
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {

                    }
                }
            });
        } catch (IOException e) {
        }

        // 프리뷰에 있는 이미지를 비트맵으로 반환

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {

            public void onPreviewFrame(byte[] data, Camera camera) {

                Camera.Parameters params = mCamera.getParameters();

                int w = params.getPreviewSize().width;

                int h = params.getPreviewSize().height;

                int format = params.getPreviewFormat();

                YuvImage image = new YuvImage(data, format, w, h, null);

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                Rect area = new Rect(0, 0, w, h);

                image.compressToJpeg(area, 50, out);

                captureImg = BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size());

                //TODO: bitmap Array를 Byte Buffer 서식에 맞게 변환

                Log.d(TAG + " Byte String", bitmapToArray(captureImg).toString());

            }

        });
    }

    // SurfaceView 의 크기가 바뀌면 호출
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {

        // 카메라 화면을 회전 할 때의 처리
        if (surfaceHolder.getSurface() == null){
            // 프리뷰가 존재하지 않을때
            return;
        }
        // 프리뷰를 다시 설정한다.
        try {
            mCamera .stopPreview();

            Camera.Parameters parameters = mCamera .getParameters();

            // 화면 회전시 사진 회전 속성을 맞추기 위해 설정한다.
            int rotation = MainActivity.getInstance.getWindowManager().getDefaultDisplay().getRotation();
            if (rotation == Surface.ROTATION_0) {
                mCamera .setDisplayOrientation(90);
                parameters.setRotation(90);
            }else if(rotation == Surface.ROTATION_90){
                mCamera .setDisplayOrientation(0);
                parameters.setRotation(0);
            }else if(rotation == Surface.ROTATION_180){
                mCamera .setDisplayOrientation(270);
                parameters.setRotation(270);
            }else{
                mCamera .setDisplayOrientation(180);
                parameters.setRotation(180);
            }

            // 변경된 화면 넓이를 설정한다.
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            mCamera .setParameters(parameters);

            // 새로 변경된 설정으로 프리뷰를 시작한다
            mCamera .setPreviewDisplay(surfaceHolder);
            mCamera .startPreview();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // SurfaceView가 종료시 호출
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mCamera != null){

            mCamera.setPreviewCallback(null);
            // 카메라 미리보기를 종료한다.
            mCamera.stopPreview();
            mCamera.release();

            mCamera = null;
        }
    }

    // 화면이 회전할 때 화면 사이즈를 구한다
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (listPreviewSizes != null) {
            previewSize = getPreviewSize(listPreviewSizes, width, height);
        }
    }
    public Camera.Size getPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    public byte[] bitmapToArray(Bitmap bmp){

        final int lnth= bmp.getByteCount();
        ByteBuffer dst= ByteBuffer.allocate(lnth);
        bmp.copyPixelsToBuffer( dst);
        byte[] barray=dst.array();

        return barray;
    }
}