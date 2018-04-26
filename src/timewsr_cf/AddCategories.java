package timewsr_cf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddCategories {

    private Connection c = DBConnection.getDBConnection();

    public void addcategories_in_webserviceinfo_full() throws SQLException {

        PreparedStatement preparedStatement = null;
        String selectSQL = "SELECT wsdladdress,serviceid FROM WebServiceInfo_full";

        preparedStatement = c.prepareStatement(selectSQL);

        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            String web_service = rs.getString("wsdladdress");
            String update_category_SQL1 = "UPDATE WebServiceInfo_full SET category = ? WHERE serviceid =" + rs.getString("serviceid");

            preparedStatement = c.prepareStatement(update_category_SQL1);
            String lower = web_service.toLowerCase();
            String category = find_category(lower);
            preparedStatement.setString(1, category);
            preparedStatement.executeUpdate();

        }
        System.out.println("categories added");

    }

    public String find_category(String lower) {
        String category = "";

        if (lower.contains("cinema") || lower.contains("drama")
                || lower.contains("theatre") || lower.contains("movie")
                || lower.contains("video") || lower.contains("audio")
                || lower.contains("music") || lower.contains("mp3")
                || lower.contains("juke") || lower.contains("cine")
                || lower.contains("studio")) {
            category += "entertainment" + " ";
        } else if (lower.contains("shop") || lower.contains("taet")
                || lower.contains("allesheute")) {
            category += "Shopping" + " ";
        } else if (lower.contains("tourism") || lower.contains("meusem")
                || lower.contains("holiday") || lower.contains("visit")
                || lower.contains("vacation") || lower.contains("westernaustralia")
                || lower.contains("g-o.be") || lower.contains("saveabuck")) {
            category += "tourism" + " ";
        } else if (lower.contains("edu") || lower.contains("nafsa") || lower.contains("hydroseek")
                || lower.contains("deeptraining") || lower.contains("napier")
                || lower.contains("academic") || lower.contains("study")
                || lower.contains("iclp") || lower.contains("genom")
                || lower.contains("webxml") || lower.contains("lab")
                || lower.contains("jku") || lower.contains("bauakademie")
                || lower.contains("univ") || lower.contains("biomoby")
                || lower.contains("ubc") || lower.contains("etfo") || lower.contains("sd76")) {
            category += "education" + " ";
        } else if (lower.contains("panasonic") || lower.contains("electronic")) {
            category += "electronics";
        } else if (lower.contains("pjgoldhomes") || lower.contains("house")
                || lower.contains("agent") || lower.contains("realestate")
                || lower.contains("room")) {
            category += "realestate" + " ";
        } else if (lower.contains("news") || lower.contains("vladars")) {
            category += "news" + " ";
        } else if (lower.contains("finance") || lower.contains("account")
                || lower.contains("pay") || lower.contains("priso")) {
            category += "finanace" + " ";
        } else if (lower.contains("sport") || lower.contains("stadium")
                || lower.contains("cric") || lower.contains("soccer")
                || lower.contains("game") || lower.contains("tab.com")
                || lower.contains("golf")) {
            category += "sports" + " ";
        } else if (lower.contains("gov")) {
            category += "government" + " ";
        } else if (lower.contains("stock") || lower.contains("buy")
                || lower.contains("sell") || lower.contains("profit")
                || lower.contains("statistic") || lower.contains("business")
                || lower.contains("data") || lower.contains("enterprise")
                || lower.contains("industry") || lower.contains("ecomm")
                || lower.contains("biz") || lower.contains("hcsbi")
                || lower.contains("equipment") || lower.contains("ccni")) {
            category += "business" + " ";
        } else if (lower.contains("phone") || lower.contains("blackberry")
                || lower.contains("iphone") || lower.contains("mobile")
                || lower.contains("wireless") || lower.contains("redcoal")
                || lower.contains("connect") || lower.contains("message")
                || lower.contains("sms") || lower.contains("sim")
                || lower.contains("email")) {
            category += "communication" + " ";
        } else if (lower.contains("hotel") || lower.contains("resturant")
                || lower.contains("break") || lower.contains("bed")) {
            category = "hotel" + " ";
        } else if (lower.contains("location") || lower.contains("geo")
                || lower.contains("map") || lower.contains("distance")
                || lower.contains("weather") || lower.contains("zip")
                || lower.contains("city") || lower.contains("state")
                || lower.contains("country") || lower.contains("atlas")
                || lower.contains("earth") || lower.contains("sate")) {
            category += "location" + " ";
        } else if (lower.contains("wiki") || lower.contains("elba")
                || lower.contains("myboot") || lower.contains("marinespecies")
                || lower.contains("health") || lower.contains("librar")
                || lower.contains("people") || lower.contains("blog") || lower.contains("lists")) {
            category += "information";
        } else if (lower.contains("job") || lower.contains("seek")
                || lower.contains("people")) {
            category += "job portal";
        } else if (lower.contains("transport") || lower.contains("trip")
                || lower.contains("bus") || lower.contains("air")
                || lower.contains("truck") || lower.contains("subaru")
                || lower.contains("motor") || lower.contains("godo")) {
            category += "transport" + " ";
        } else if (lower.contains("security") || lower.contains("authentication")
                || lower.contains("code") || lower.contains("barcode")
                || lower.contains("user")) {
            category += "authentication" + " ";
        } else if (lower.contains("convertor") || lower.contains("widget")
                || lower.contains("microsoft") || lower.contains("iress")
                || lower.contains("solutions") || lower.contains("nano")
                || lower.contains("service") || lower.contains("app")
                || lower.contains("blueridge") || lower.contains("soft")
                || lower.contains("extensio") || lower.contains("dundas")
                || lower.contains("envisionit") || lower.contains("web")
                || lower.contains("auto")) {
            category += "widgets";
        }

        category = category.trim();
        if (category.length() == 0) {
            category = "other services";
        }

        return category;
    }

}
