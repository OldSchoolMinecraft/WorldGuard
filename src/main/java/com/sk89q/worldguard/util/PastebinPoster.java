package com.sk89q.worldguard.util;

import java.net.*;
import java.io.*;

public class PastebinPoster
{
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    
    public static void paste(final String code, final PasteCallback callback) {
        final PasteProcessor processor = new PasteProcessor(code, callback);
        final Thread thread = new Thread(processor);
        thread.start();
    }
    
    private static class PasteProcessor implements Runnable
    {
        private String code;
        private PasteCallback callback;
        
        public PasteProcessor(final String code, final PasteCallback callback) {
            this.code = code;
            this.callback = callback;
        }
        
        public void run() {
            HttpURLConnection conn = null;
            OutputStream out = null;
            InputStream in = null;
            try {
                final URL url = new URL("http://pastebin.com/api/api_post.php");
                conn = (HttpURLConnection)url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setInstanceFollowRedirects(false);
                conn.setDoOutput(true);
                out = conn.getOutputStream();
                out.write(("api_option=paste&api_dev_key=" + URLEncoder.encode("4867eae74c6990dbdef07c543cf8f805", "utf-8") + "&api_paste_code=" + URLEncoder.encode(this.code, "utf-8") + "&api_paste_private=" + URLEncoder.encode("0", "utf-8") + "&api_paste_name=" + URLEncoder.encode("", "utf-8") + "&api_paste_expire_date=" + URLEncoder.encode("1D", "utf-8") + "&api_paste_format=" + URLEncoder.encode("text", "utf-8") + "&api_user_key=" + URLEncoder.encode("", "utf-8")).getBytes());
                out.flush();
                out.close();
                if (conn.getResponseCode() == 200) {
                    in = conn.getInputStream();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    final StringBuffer response = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                        response.append("\r\n");
                    }
                    reader.close();
                    final String result = response.toString().trim();
                    if (result.matches("^https?://.*")) {
                        this.callback.handleSuccess(result.trim());
                    }
                    else {
                        String err = result.trim();
                        if (err.length() > 100) {
                            err = err.substring(0, 100);
                        }
                        this.callback.handleError(err);
                    }
                }
                else {
                    this.callback.handleError("didn't get a 200 response code!");
                }
            }
            catch (IOException e) {
                this.callback.handleError(e.getMessage());
            }
            finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ex) {}
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException ex2) {}
                }
            }
        }
    }
    
    public interface PasteCallback
    {
        void handleSuccess(final String p0);
        
        void handleError(final String p0);
    }
}
