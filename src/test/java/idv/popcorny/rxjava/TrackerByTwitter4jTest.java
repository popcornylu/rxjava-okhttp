package idv.popcorny.rxjava;

import org.junit.Test;
import org.slf4j.Logger;
import twitter4j.Status;

import java.util.Arrays;

public class TrackerByTwitter4jTest {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());

    @Test
    public void testTrack() throws Exception {
        TrackerByTwitter4j tracker = new TrackerByTwitter4j();
        tracker.track(Arrays.asList("taiwan"))
                .map(Status::getText)
                .toBlocking()
                .forEach(s -> logger.info("{}", s));
    }
}
