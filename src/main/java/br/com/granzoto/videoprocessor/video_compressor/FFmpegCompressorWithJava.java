package br.com.granzoto.videoprocessor.video_compressor;

import java.util.logging.Logger;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class FFmpegCompressorWithJava implements VideoCompressor {

    private static final Logger LOGGER = Logger.getLogger(FFmpegCompressorWithJava.class.getName());

    @Override
    public boolean executeCompression(java.io.File inputFile, java.io.File outputFile) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(),
                        grabber.getImageHeight(), grabber.getAudioChannels())) {

            grabber.start();

            if (grabber.getAudioChannels() > 0) {
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // AAC codec
                // recorder.setSampleRate(grabber.getSampleRate());
                // recorder.setAudioBitrate(grabber.getAudioBitrate());
            }

            // Configure video
            // recorder.setPixelFormat(avutil.AV_PIX_FMT_RGB32);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            //recorder.setVideoBitrate(8000 * 1024); // Adjust bitrate (2 Mbps example)
            //recorder.setFrameRate(grabber.getFrameRate());
            recorder.setFormat("mp4");

            // Adjust video scaling
            int targetWidth = 1280;
            int targetHeight = (grabber.getImageHeight() * targetWidth) / grabber.getImageWidth();
            // int targetWidth = grabber.getImageWidth();
            // int targetHeight = grabber.getImageHeight();

            recorder.setImageWidth(targetWidth);
            recorder.setImageHeight(targetHeight);

            // Start the recorder
            recorder.start();

            // Read and write frames
            while (grabber.grab() != null) {
                recorder.record(grabber.grab());
            }

            // Stop grabber and recorder
            recorder.stop();
            grabber.stop();

            LOGGER.info("Compression completed: " + outputFile);
        } catch (Exception e) {
            LOGGER.severe("Compression failed, output file will be deleted.");
            outputFile.deleteOnExit();
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
