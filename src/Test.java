import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import org.apache.http.client.entity.UrlEncodedFormEntity;


public class Test implements Runnable{



    private static final String PROTOCOL = "http";
    private int PORT;
    private static final String HOST = "localhost";
    private static final String COOKIE_NAME = "login-token";



    public Test(int port){
        PORT = port;
    }




    public Test(){
        PORT = 8080;
    }


    public void run() {

        try {
            System.out.println(PORT);
            httpMain(PORT);


        } catch(Exception e){
            System.out.println("we failed");
            System.out.println(e);

        }




    }

    //


    //connects to the server, gets token, adds token to the header
    public void httpMain(int port) throws Exception{
        String username = "admin";
        String password = "admin";
        String specialPath = "/home/users/a/admin.json";

        CloseableHttpClient client = HttpClients.createDefault();
        String token = getToken(username, password, client, port);
        if (token == null) {
            System.err.println("No login cookie set.");
            return;
        }
        System.out.println("token = " + token);

        // look up format strings
        HttpGet get = new HttpGet(String.format("%s://%s:%s%s", PROTOCOL, HOST, PORT, specialPath));
        get.addHeader("Cookie", String.format("%s=%s", COOKIE_NAME, token));

        // putting a request into the server once it is signed in. eventually it will execute the query builder
        HttpResponse status = client.execute(get);
    }



    public static void main(String[] args) throws Exception {

        // allows for multi thread support
        // later we should be able to paramaterize ports and hosts
        (new Thread(new Test(4502))).start();
        (new Thread(new Test(8002))).start();


    }

    // builds http request
    private static String getToken(String username, String password, HttpClient client, int port) throws IOException,
            HttpResponseException {
        String token = null;

        // building the login request
        HttpPost authRequest = new HttpPost(String.format("%s://%s:%s/j_security_check", PROTOCOL, HOST, port));
        ArrayList<NameValuePair> postParameters;
        postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair("j_username", username));
        postParameters.add(new BasicNameValuePair("j_password", password));
        postParameters.add(new BasicNameValuePair("j_validate", "true"));
        authRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
        // executing the request
        HttpResponse status = client.execute(authRequest);

        // System.out.println(status.getStatusLine());

        // if the page came back ok -> parse out login token
        if (status.getStatusLine().getStatusCode() == 200) {
            System.out.println(status.getStatusLine());
            Header[] headers = status.getAllHeaders();

            // iterating all the headers that came back and check if one is login token
            for (Header header : headers) {
                String value = header.getValue();
                System.out.println("Key : " + header.getName() + " ,Value : " + header.getValue());
                if (value.startsWith(COOKIE_NAME + "=")) {
                    int endIdx = value.indexOf(';');
                    if (endIdx > 0) {
                        token = value.substring(COOKIE_NAME.length() + 1, endIdx);
                    }
                }
            }
        } else {
            System.err.println("Unexcepted response code " + status + "; msg: " + status.getStatusLine());
        }
        return token;
    }

}