package jeongari.com.turtleneck;

import android.app.Activity;

import java.io.IOException;

/**
 * Created by jeongahri on 2018. 7. 3..
 */

public class ImageClassifierPoseEstimation extends ImageClassifier {

    /**
    * 추론 결과를 저장하기 위한 Array
    */
    private byte[][] labelProbArray = null;

    ImageClassifierPoseEstimation(Activity activity) throws IOException{
        super(activity);
        labelProbArray = new byte[1][14];
    }

    @Override
    protected String getModelPath() {
        return "mv2-cpm-224.tflite";
    }

    @Override
    protected int getImageSizeX() {
        return 256;
    }

    @Override
    protected int getImageSizeY() {
        return 256;
    }

    @Override
    protected int getNumBytesPerChannel() {
        // the quantized model uses a single byte only
        return 1;
    }

    @Override
    protected void addPixelValue(int pixelValue) {
        imgData.put((byte) ((pixelValue >> 16) & 0xFF));
        imgData.put((byte) ((pixelValue >> 8) & 0xFF));
        imgData.put((byte) (pixelValue & 0xFF));
    }

    @Override
    protected float getProbability(int labelIndex) {
        return labelProbArray[0][labelIndex];
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {
        labelProbArray[0][labelIndex] = value.byteValue();
    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return (labelProbArray[0][labelIndex] & 0xff) / 255.0f;
    }

    @Override
    protected void runInference() {
        tflite.run(imgData, labelProbArray);
    }

}
