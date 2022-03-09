package edu.iu.ess.security;

import edu.iu.ess.util.StringUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Provides utility functions for supporting REST service authentication
 */
public final class Aws4AuthUtil {

   private static final Logger LOG = Logger.getLogger(Aws4AuthUtil.class.getName());

   private static final ThreadLocal<DateFormat> GMT_DATE_FORMAT = new ThreadLocal<DateFormat>() {
      @Override
      protected DateFormat initialValue() {
         SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         return df;
      }
   };

   private static final ThreadLocal<DateFormat> DATESTAMP_FORMAT = new ThreadLocal<DateFormat>() {
      @Override
      protected DateFormat initialValue() {
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
         return df;
      }
   };

   private static final ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
      @Override
      protected MessageDigest initialValue() {
         try {
            return MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
         }
      }
   };

   private static final ThreadLocal<MessageDigest> SHA256 = new ThreadLocal<MessageDigest>() {
      @Override
      protected MessageDigest initialValue() {
         try {
            return MessageDigest.getInstance("SHA-256");
         } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
         }
      }
   };

   private static final byte[] AWS4_REQUEST = utf8("aws4_request");
   private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
   private static final byte[] b0 = new byte[0];
   private static final String EMPTY_PAYLOADHASH = encodeHex(SHA256.get().digest(new byte[0]));

   private static String encodeHex(byte[] buf) {
      char[] chars = new char[2 * buf.length];
      for (int i = 0; i < buf.length; ++i) {
         chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
         chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
      }
      return new String(chars);
   }

   private static byte[] utf8(String data) {
      if (data == null)
         return b0;
      try {
         return data.getBytes("UTF-8");
      } catch (UnsupportedEncodingException e) {
         throw new IllegalStateException(e);
      }
   }

   private static String uriEncode(String s) {
      if (s == null)
         return "";

      byte[] ba = utf8(s);

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < ba.length; i++) {
         int b = ba[i];
         char c = (char) b;
         if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_'
               || c == '.' || c == '~') {
            // skip encoding
            sb.append(c);
            continue;
         }

         sb.append('%');
         sb.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xf, 16)));
         sb.append(Character.toUpperCase(Character.forDigit(b & 0xf, 16)));
      }
      return sb.toString();
   }

   private static String trimAll(String s) {
      StringBuilder sb = new StringBuilder(s.trim());
      for (int i = 0; i < sb.length(); i++) {
         if (Character.isWhitespace(sb.charAt(i))) {
            if (i == 0 || !Character.isWhitespace(sb.charAt(i - 1)))
               sb.setCharAt(i, ' ');
            else
               sb.deleteCharAt(i--);
         }
      }
      return sb.toString();
   }

   /**
    * Convert a path to URI-canonical form: ensuring that the path begins with a
    * slash '/' character and that all path elements is URI encoded.
    * <p>
    * This method is compatible for signing AWS4 messages
    * </p>
    *
    * @param path path to canonicalize
    * @return canonical path
    */
   public static String getCanonicalPath(String path) {
      if (path == null || "".equals(path) || "/".equals(path))
         return "/";

      StringBuilder sb = new StringBuilder(path);
      if (sb.charAt(0) != '/')
         sb.insert(0, '/');

      int slash = 0;
      while (slash != -1) {
         int start = slash + 1;
         slash = sb.indexOf("/", start);
         if (slash == start)
            continue;

         String pathElement;
         if (slash == -1) {
            pathElement = sb.substring(start);
            sb.delete(start, sb.length());
         } else {
            pathElement = sb.substring(start, slash);
            sb.delete(start, slash);
         }

         sb.insert(start, uriEncode(pathElement));
      }

      return sb.toString();
   }

   /**
    * Creates a canonical query string from a set of request parameters. All
    * parameters are sorted by name, and all names and values are URI encoded.
    * <p>
    * This method is compatible for signing AWS4 messages
    * </p>
    *
    * @param query raw query string
    * @return canonical query string, does not include a leading '?' character
    */
   public static String getCanonicalQuery(String query) {
      if (query == null || "".equals(query))
         return "";

      int lastToken = 0;
      int token = query.indexOf('=');
      Map<String, List<String>> params = new TreeMap<>();
      while (token >= 0) {
         String name = query.substring(lastToken, token);
         List<String> values = params.get(name);
         if (values == null)
            params.put(name, values = new ArrayList<>());

         lastToken = token + 1;
         token = query.indexOf('&', lastToken);
         if (token >= 0) {
            try {
               values.add(URLDecoder.decode(query.substring(lastToken, token), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
               throw new IllegalStateException(e);
            }
            lastToken = token + 1;
            token = query.indexOf('=', lastToken);
         } else
            try {
               values.add(URLDecoder.decode(query.substring(lastToken), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
               throw new IllegalStateException(e);
            }
      }

      StringBuilder sb = new StringBuilder();
      params.forEach((name, values) -> values.forEach(value -> {
         if (sb.length() > 0)
            sb.append('&');
         sb.append(uriEncode(name)).append('=').append(uriEncode(value));
      }));
      return sb.toString();
   }

   /**
    * Creates a canonical query string from a set of request parameters. All
    * parameters are sorted by name, and all names and values are URI encoded.
    * <p>
    * This method is compatible for signing AWS4 messages
    * </p>
    *
    * @param params mapping of request parameters. Values may be string, string
    *               array, or Iterable of strings.
    * @return canonical query string, does not include a leading '?' character
    */
   public static String getCanonicalQuery(Map<String, ?> params) {
      if (params == null)
         return "";

      StringBuilder sb = new StringBuilder();
      BiConsumer<String, String> append = (name, value) -> {
         if (sb.length() > 0)
            sb.append('&');
         sb.append(uriEncode(name)).append('=').append(uriEncode(value));
      };

      new TreeMap<>(params).forEach((name, values) -> {
         if (values instanceof String)
            append.accept(name, (String) values);
         else if (values instanceof String[])
            for (String value : (String[]) values)
               append.accept(name, value);
         else if (values instanceof Iterable)
            for (Object value : (Iterable<?>) values)
               append.accept(name, (String) value);
      });
      return sb.toString();
   }

   /**
    * Canonicalizes a set of request headers. Header values are printed, one per
    * line, in order and with any extraneous whitespace removed.
    * <p>
    * This method is compatible for signing AWS4 messages
    * </p>
    *
    * @param params mapping of request headers
    * @return canonical query string, does not include a leading '?' character
    */
   public static String getCanonicalHeaders(Map<String, ?> headers) {
      if (headers == null)
         return "";

      StringBuilder sb = new StringBuilder();

      new TreeMap<>(headers).forEach((name, values) -> {
         sb.append(name.toLowerCase()).append(':');
         if (values instanceof String)
            sb.append((String) values);
         else if (values instanceof String[]) {
            boolean first = true;
            for (String value : (String[]) values) {
               if (first)
                  first = false;
               else
                  sb.append(',');
               sb.append(trimAll(value));
            }
         } else if (values instanceof Iterable) {
            boolean first = true;
            for (Object value : (Iterable<?>) values) {
               if (first)
                  first = false;
               else
                  sb.append(',');
               sb.append(trimAll((String) value));
            }
         }
         sb.append("\n");
      });
      return sb.toString();
   }

   /**
    * Canonicalizes a set of request headers. Header values are printed, one per
    * line, in order and with any extraneous whitespace removed.
    * <p>
    * This method is compatible for signing AWS4 messages
    * </p>
    *
    * @param params mapping of request headers
    * @return canonical query string, does not include a leading '?' character
    */
   public static String getSignedHeaders(Collection<String> headerNames) {
      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (String name : new TreeSet<>(headerNames)) {
         if (first)
            first = false;
         else
            sb.append(';');
         sb.append(name.toLowerCase());
      }

      return sb.toString();
   }

   /**
    * Gets a hex-encoded SHA-256 digest for character data.
    * <p>
    * The string passed into this method is first converted to UTF-8 binary format,
    * then digested.
    * </p>
    *
    * @param data character data
    * @return SHA-256 digest, hex encoded
    */
   public static String sha256(String data) {
      return sha256(utf8(data));
   }

   /**
    * Gets a SHA-256 digest for character data.
    * <p>
    * The string passed into this method is first converted to UTF-8 binary format,
    * then digested.
    * </p>
    *
    * @param data character data
    * @return SHA-256 digest, hex encoded
    */
   public static String sha256(byte[] data) {
      if (data == null || data.length == 0)
         return EMPTY_PAYLOADHASH;
      return encodeHex(SHA256.get().digest(data));
   }

   /**
    * Gets a hex-encoded MD5 digest for character data.
    * <p>
    * The string passed into this method is first converted to UTF-8 binary format,
    * then digested.
    * </p>
    *
    * @param data character data
    * @return MD5 digest, hex encoded
    */
   public static String md5(String data) {
      return md5(utf8(data));
   }

   /**
    * Gets a MD5 digest for character data.
    * <p>
    * The string passed into this method is first converted to UTF-8 binary format,
    * then digested.
    * </p>
    *
    * @param data character data
    * @return MD5 digest, hex encoded
    */
   public static String md5(byte[] data) {
      return encodeHex(MD5.get().digest(data));
   }

   /**
    * Gets an HMAC-SHA-256 signature for a data using a set of seed data as the
    * secret key.
    *
    * @param key  Seed data to use a the secret key
    * @param data Data to sign
    * @return signature data
    */
   public static byte[] hmac(byte[] key, byte[] data) {
      try {
         Mac hmac = Mac.getInstance("HMACSHA256");
         hmac.init(new SecretKeySpec(key, "HMACSHA256"));
         return hmac.doFinal(data);
      } catch (InvalidKeyException | NoSuchAlgorithmException e) {
         throw new SecurityException(e);
      }
   }

   /**
    * Gets an AWS4 signing key.
    *
    * @param secretKey password associated with access token
    * @param region    region name
    * @param service   service name
    * @return AWS4 compliant signing key
    */
   public static byte[] getSigningKey(String secretKey, String region, String service) {
      byte[] k = hmac(utf8("AWS4" + md5(secretKey)), utf8(DATESTAMP_FORMAT.get().format(System.currentTimeMillis())));
      k = hmac(k, utf8(region));
      k = hmac(k, utf8(service));
      k = hmac(k, AWS4_REQUEST);
      return k;
   }

   /**
    * Gets the scope for an AWS4 authenticated request.
    *
    * @param region  region name
    * @param service service name
    * @return AWS4 compliant signing key
    */
   public static String getScope(String region, String service) {
      return DATESTAMP_FORMAT.get().format(System.currentTimeMillis()) + '/' + region + '/' + service
            + "/aws4_request";
   }

   /**
    * Calculates an AWS4 signature for a request
    *
    * @param accessToken AWS access token
    * @param secretKey   AWS secret key
    * @param region      AWS region
    * @param service     AWS service
    * @param method      HTTP request method
    * @param path        Path segment of the URL
    * @param query       Query string from the URL
    * @param payload     Raw HTTP request data
    * @param headers     Mapping of headers to use to calculate the signature. Will
    *                    not be modified.
    * @return Calculated AWS4 request signature
    */
   public static String getSignature(String accessToken, String secretKey, String region, String service,
                                     String method, String path, String query, byte[] payload, Map<String, Iterable<String>> headers) {
      StringBuilder canonicalRequestBuilder = new StringBuilder(method);
      canonicalRequestBuilder.append("\n");
      canonicalRequestBuilder.append(getCanonicalPath(path));
      canonicalRequestBuilder.append("\n");
      canonicalRequestBuilder.append(getCanonicalQuery(query));
      canonicalRequestBuilder.append("\n");
      canonicalRequestBuilder.append(getCanonicalHeaders(headers));
      canonicalRequestBuilder.append("\n");
      canonicalRequestBuilder.append(getSignedHeaders(headers.keySet()));
      canonicalRequestBuilder.append("\n");
      canonicalRequestBuilder.append(sha256(payload));
      String canonicalRequest = canonicalRequestBuilder.toString();
      LOG.finer(() -> "Canonical Request:\n" + canonicalRequest);

      String scope = getScope(region, service);
      StringBuilder stringToSignBuilder = new StringBuilder("AWS4-HMAC-SHA256\n");
      stringToSignBuilder.append(headers.get("x-amz-date").iterator().next()).append('\n');
      stringToSignBuilder.append(scope).append('\n');
      stringToSignBuilder.append(sha256(canonicalRequest));
      String stringToSign = stringToSignBuilder.toString();
      LOG.finer(() -> "String To Sign:\n" + stringToSign);

      return encodeHex(hmac(getSigningKey(secretKey, region, service), utf8(stringToSign)));
   }

   /**
    * Populates HTTP request headers for AWS4 authentication.
    *
    * @param accessToken AWS access token
    * @param secretKey   AWS secret key
    * @param region      AWS region
    * @param service     AWS service
    * @param method      HTTP request method
    * @param url         Full URL, including query string if applicable, for the
    *                    requested
    * @param payload     Raw HTTP request data
    * @param headers     Mutable mapping of headers to consider when signing the
    *                    request. Additional headers will be added to this map, and
    *                    should be included on the outbound REST call.
    */
   public static void addAuthHeaders(String accessToken, String secretKey, String region, String service,
                                     String method, URL url, byte[] payload, Map<String, Iterable<String>> headers) {
      if (headers == null)
         headers = new LinkedHashMap<>();

      long time = System.currentTimeMillis();
      String host;
      int port = url.getPort();
      if (port != -1)
         host = url.getHost() + ':' + port;
      else
         host = url.getHost();
      headers.put("host", Collections.singleton(host));
      headers.put("x-amz-date", Collections.singleton(GMT_DATE_FORMAT.get().format(time)));
      headers.put("x-amz-content-sha256", Collections.singleton(sha256(payload)));

      headers.put("authorization", Collections.singleton("AWS4-HMAC-SHA256 Credential="
            + Base64.getEncoder().encodeToString(utf8(accessToken)) + '/' + getScope(region, service)
            + ", SignedHeaders=" + getSignedHeaders(headers.keySet()) + ", Signature=" + getSignature(accessToken,
            secretKey, region, service, method, url.getPath(), url.getQuery(), payload, headers)));
   }

   /**
    * Populates HTTP request headers for AWS4 authentication using an IU-generated
    * API Token.
    *
    * @param apiToken IU generated API token
    * @param region   AWS4 region name
    * @param service  AWS4 service name
    * @param method   HTTP request method
    * @param url      Full URL, including query string if applicable, for the
    *                 requested
    * @param payload  Raw HTTP request data
    * @param headers  Mutable mapping of headers to consider when signing the
    *                 request. Additional headers will be added to this map, and
    *                 should be included on the outbound REST call.
    */
   public static Map<String, Iterable<String>> getAuthHeaders(String apiToken, String region, String service,
                                                              String method, URL url, byte[] payload) {
      byte[] token = Base64.getDecoder().decode(apiToken);
      if (token == null || token.length != 544)
         throw new SecurityException(
               "Invalid token length " + token.length + " " + StringUtil.mask(apiToken, '*', 1));

      String accessToken = Base64.getEncoder().encodeToString(Arrays.copyOfRange(token, 0, 16));
      String secretKey = Base64.getEncoder().encodeToString(Arrays.copyOfRange(token, 16, 32));
      String apiSignature = Base64.getEncoder().encodeToString(Arrays.copyOfRange(token, 32, 544));

      LinkedHashMap<String, Iterable<String>> headers = new LinkedHashMap<>();
      headers.put("x-iu-api-signature", Collections.singleton(apiSignature));

      addAuthHeaders(accessToken, secretKey, region, service, method, url, payload, headers);

      return headers;
   }

   private Aws4AuthUtil() {
   }

}