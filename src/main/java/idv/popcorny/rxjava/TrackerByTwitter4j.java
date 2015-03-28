package idv.popcorny.rxjava;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class TrackerByTwitter4j {
    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    Subject<Status , Status> subject = PublishSubject.create();

    private TwitterStream twitterStream ;

    public TrackerByTwitter4j() {
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(TwitterConfig.CONSUMER_KEY)
                .setOAuthConsumerSecret(TwitterConfig.SECRET_KEY)
                .setOAuthAccessToken(TwitterConfig.ACCESS_TOKEN)
                .setOAuthAccessTokenSecret(TwitterConfig.ACCESS_SECRET);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        twitterStream.addListener(new StatusAdapter() {
            @Override
            public void onStatus(Status status) {
                super.onStatus(status);
                subject.onNext(status);
            }
        });
    }

    public Observable<Status> track(List<String> tracks) throws TwitterException {
        FilterQuery fq = new FilterQuery();
        twitterStream.filter(fq.track(tracks.toArray(new String[tracks.size()])));
        return subject.asObservable();
    }
}