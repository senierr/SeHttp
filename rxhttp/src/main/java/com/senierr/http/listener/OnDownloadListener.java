package com.senierr.http.listener;

import com.senierr.http.model.ProgressResponse;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * 下载进度过滤器
 *
 * @author zhouchunjie
 * @date 2019/4/25 17:25
 */
public abstract class OnDownloadListener<T> implements ObservableTransformer<ProgressResponse<T>, T>, OnProgressListener {

    @Override
    public final ObservableSource<T> apply(Observable<ProgressResponse<T>> upstream) {
        return upstream.filter(new Predicate<ProgressResponse<T>>() {
            @Override
            public boolean test(ProgressResponse<T> t) throws Exception {
                if (t.type() == ProgressResponse.TYPE_DOWNLOAD) {
                    onProgress(t.totalSize(), t.currentSize(), t.percent());
                    return false;
                }
                return t.type() == ProgressResponse.TYPE_RESULT && t.result() != null;
            }
        }).map(new Function<ProgressResponse<T>, T>() {
            @Override
            public T apply(ProgressResponse<T> t) throws Exception {
                return t.result();
            }
        });
    }
}
