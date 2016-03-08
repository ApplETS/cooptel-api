import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static spark.Spark.get;
import static spark.Spark.halt;

public class Main {
    public static void main(String[] args) {
        get("/cooptel", (req, res) -> {

            String phase = req.queryParams("phase"),
                    appt = req.queryParams("appt"),
                    username,
                    password;

            Date today = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            int month = cal.get(Calendar.MONTH) + 1;

            OkHttpClient client = new OkHttpClient();

            username = "ets-res" + phase + "-" + appt;
            password = "ets" + appt;
            String basicAuth = Base64.getEncoder().encodeToString(
                    (username + ":" + password).getBytes(StandardCharsets.UTF_8));

            Request request = new Request.Builder()
                    .url("http://www2.cooptel.qc.ca/services/temps/?mois=" + month + "&cmd=Visualiser")
                    .get()
                    .addHeader("authorization", "Basic " + basicAuth)
                    .build();

            Response response = client.newCall(request).execute();

            if(response.code() != 200) {
                halt(404, "Not found");
            }

            Document doc = Jsoup.parse(response.body().string());
            Elements tables = doc.select("table[border=1]");

            Pattern detailsPattern = Pattern.compile("<td>(.*)<\\/td><td>(.*)<\\/td><td align=\"RIGHT\">(.*)<\\/td><td align=\"RIGHT\">(.*)<\\/td>");
            Pattern totalPattern = Pattern.compile("<td>Quota permis pour la période<\\/td>.*<td align=\"RIGHT\">(.*)<\\/td>.*<\\/tr>");
            Matcher m = null;

            ArrayList<ConsommationDate> consommationDates = new ArrayList<ConsommationDate>();
            for (Element element : tables.get(0).children()) {
                for (Element element1 : element.children()) {
                    String cleanedString = element1.html().replace("\n", "");
                    m = detailsPattern.matcher(cleanedString);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                    if (m.matches()) {
                        consommationDates.add(new ConsommationDate(
                                m.group(1),
                                m.group(2).equals("Journée en cours") ? formatter.format(today) : m.group(2),
                                Float.parseFloat(m.group(3)),
                                Float.parseFloat(m.group(4))));
                    }

                }

            }

            ConsommationGlobal global = new ConsommationGlobal();

            m = totalPattern.matcher(
                    tables.get(1)
                            .children()
                            .html()
                            .replace("\n", ""));

            if(m.find()) {
                    global = new ConsommationGlobal(
                            Float.parseFloat(m.group(1)),
                            consommationDates
                    );
            }


            System.out.println(
                    tables.get(1)
                            .children()
                            .html()
                            .replace("\n", "")
            );

            return new Gson().toJson(global);

        });
    }
}