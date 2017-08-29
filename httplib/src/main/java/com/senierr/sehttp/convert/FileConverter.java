package com.senierr.sehttp.convert;

import android.util.Log;

import com.senierr.sehttp.SeHttp;
import com.senierr.sehttp.callback.FileCallback;
import com.senierr.sehttp.util.SeLogger;

import java.io.File;
import java.io.IOException;

import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * File类型解析
 *
 * @author zhouchunjie
 * @date 2017/3/29
 */

public class FileConverter implements Converter<File> {

    private FileCallback fileCallback;
    private File destFile;

    public FileConverter(FileCallback fileCallback) {
        this.fileCallback = fileCallback;
        destFile = fileCallback.getDestFile();
    }

    @Override
    public File convert(Response response) throws Exception {
        File destFileDir = new File(destFile.getParent() + File.separator);
        if (!destFileDir.exists()) {
            destFileDir.mkdirs();
        }
        if (destFile.exists()) {
            if (fileCallback.onDiff(response, destFile)) {
                return destFile;
            } else {
                destFile.delete();
            }
        }

        BufferedSource bufferedSource = null;
        BufferedSink bufferedSink = null;
        try {
            bufferedSource = Okio.buffer(Okio.source(response.body().byteStream()));
            bufferedSink = Okio.buffer(Okio.sink(destFile));
            // 计算总大小
            final long total = response.body().contentLength();
            // 上次刷新的时间
            long lastTime = 0;

            byte[] bytes = new byte[1024];
            long sum = 0;
            int len;
            while ((len = bufferedSource.read(bytes)) != -1) {
                sum += len;
                bufferedSink.write(bytes, 0, len);

                final long finalSum = sum;
                long curTime = System.currentTimeMillis();
                if (curTime - lastTime >= SeHttp.REFRESH_MIN_INTERVAL || finalSum == total) {
                    SeHttp.getInstance().getMainScheduler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (fileCallback != null) {
                                fileCallback.onProgress(total, finalSum, (int) (finalSum * 100 / total));
                            }
                        }
                    });
                    lastTime = curTime;
                }
            }
            bufferedSink.flush();
            return destFile;
        } finally {
            try {
                if (bufferedSource != null) bufferedSource.close();
                if (bufferedSink != null) bufferedSink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
