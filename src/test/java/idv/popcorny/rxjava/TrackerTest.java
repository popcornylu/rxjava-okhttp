package idv.popcorny.rxjava;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

public class TrackerTest {

    @Test
    public void testStream() throws Exception {
        Gson gson = new Gson();

        Tracker t = new Tracker("taiwan");
        t.streamRequest()
         .map(s -> gson.fromJson(s, TwitterMessage.class))
         .map(TwitterMessage::getText)
         .subscribeOn(Schedulers.io())
         .toBlocking()
         .forEach(System.out::println);
    }

    public static class TwitterMessage {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}