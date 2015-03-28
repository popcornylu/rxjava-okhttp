/**
 * Created by smallufo on 2015-02-22.
 */
package idv.popcorny.rxjava;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import net.oauth.OAuth;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Tracker {
  private Logger logger = LoggerFactory.getLogger(getClass());



  private final String track;


  public Tracker(String track) {this.track = track;}


  public Observable<String> streamRequest() throws Exception {
    OkHttpClient client = new OkHttpClient();

    long timestamp = System.currentTimeMillis() / 1000;
    String nonce = RandomStringUtils.randomAlphanumeric(32);
    logger.info("timestamp = {} , nonce = {}", timestamp , nonce);

    String sigBaseString = getSigBaseString(nonce , timestamp);

    logger.info("sigBaseString = {}", sigBaseString);


    StringBuilder sb = new StringBuilder();
    sb.append(OAuth.percentEncode(TwitterConfig.SECRET_KEY));
    sb.append("&");
    sb.append(OAuth.percentEncode(TwitterConfig.ACCESS_SECRET));
    String signingKey = sb.toString();

    logger.info("signingKey = {}" , signingKey);


    String signature = getSignature(signingKey , sigBaseString);
    logger.info("sign = {}" , signature);

    String dst = getDst(nonce , signature , timestamp);
    logger.info("dst = {}" , dst);


    Request request = new Request.Builder()
      .url("https://stream.twitter.com/1.1/statuses/filter.json?track=" + track)
      .header("Authorization", dst)
      .post(RequestBody.create(null, "track=" + track))
      .build();

    return new RxOkHttp(client).streamLines(request);
  }

   private String getSigBaseString(String oauthNonce , long timestamp) {
    Map<String , String> parameters = new HashMap<String , String>() {{
      put("oauth_consumer_key" , TwitterConfig.CONSUMER_KEY);
      put("oauth_nonce" , oauthNonce);
      put("oauth_signature_method" , "HMAC-SHA1");
      put("oauth_timestamp" , String.valueOf(timestamp));
      put("oauth_token" , TwitterConfig.ACCESS_TOKEN);
      put("oauth_version" , "1.0");
      put("track" , track);
    }};

    String join = parameters.entrySet().stream()
      .map(kv -> OAuth.percentEncode(kv.getKey()) + "=" + OAuth.percentEncode(kv.getValue()))
      .sorted()
      .collect(Collectors.joining("&"));
    logger.debug("joined parameters = {}" , join);

    StringBuilder sb = new StringBuilder();
    sb.append("POST&");
    sb.append(OAuth.percentEncode("https://stream.twitter.com/1.1/statuses/filter.json"));
    sb.append("&");
    sb.append(OAuth.percentEncode(join));
    String sigBaseString = sb.toString();
    return sigBaseString;
  } // base string

  private String getDst(String nonce , String signature , long timestamp) {
    Map<String , String> headers7 = new LinkedHashMap<String , String>() {{
      put("oauth_consumer_key" , TwitterConfig.CONSUMER_KEY);
      put("oauth_nonce" , nonce);
      put("oauth_signature" , signature);
      put("oauth_signature_method" , "HMAC-SHA1");
      put("oauth_timestamp" , String.valueOf(timestamp));
      put("oauth_token" , TwitterConfig.ACCESS_TOKEN);
      put("oauth_version" , "1.0");
    }};
    StringBuilder sb = new StringBuilder();
    sb.append("OAuth ");
    sb.append(headers7.entrySet().stream()
      .map(kv -> OAuth.percentEncode(kv.getKey()) + "=\"" + OAuth.percentEncode(kv.getValue()) + "\"")
      .collect(Collectors.joining(", "))
    );
    String dst = sb.toString();
    logger.debug("dst = {}", dst);
    return dst;
  } // dst

  private String getSignature(String signingKey , String sigBaseString) throws Exception {
    String sha1 = "HmacSHA1";
    SecretKeySpec keySpec = new SecretKeySpec(signingKey.getBytes("UTF-8"), sha1);
		Mac mac = Mac.getInstance(sha1);
		mac.init(keySpec);

    byte[] digest = mac.doFinal(sigBaseString.getBytes("UTF-8"));

    String sign = new String(Base64.getEncoder().encode(digest));
    return sign;
  }
}
