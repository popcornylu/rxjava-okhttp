package idv.popcorny.rxjava;

import com.squareup.okhttp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.StringObservable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class RxOkHttp {
    private static Logger logger = LoggerFactory.getLogger(RxOkHttp.class);

    private OkHttpClient client;

    public RxOkHttp(OkHttpClient client) {
        this.client = client;
    }

    public Observable<byte[]> streamBytes(Request request) {
        return Observable.create(subj -> {
            try {
                Response response = client.newCall(request).execute();
                byte[] buffer = new byte[1024 * 64];
                InputStream in = response.body().byteStream();
                int read = 0;
                subj.onStart();
                while((read = in.read(buffer)) > 0) {
                    subj.onNext(Arrays.copyOfRange(buffer, 0, read));
                }
                subj.onCompleted();
            } catch (IOException e) {
                subj.onError(e);
            }
        });
    }

    public Observable<String> streamStrings(Request request) {
        return StringObservable.decode(streamBytes(request), "utf-8");
    }

    public Observable<String> streamLines(Request request) {
        return StringObservable.byLine(streamStrings(request));
    }

}

