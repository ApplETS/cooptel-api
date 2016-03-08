/**
 * Created by gnut3ll4 on 08/03/16.
 */
public class ConsommationDate {
    String idChambre;
    String date;
    float upload;
    float download;

    public ConsommationDate(String idChambre, String date, float upload, float download) {
        this.idChambre = idChambre;
        this.date = date;
        this.upload = upload;
        this.download = download;
    }
}
