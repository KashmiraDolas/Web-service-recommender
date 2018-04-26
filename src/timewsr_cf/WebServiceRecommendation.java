package timewsr_cf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.text.DecimalFormat;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class WebServiceRecommendation implements ActionListener {

    private Connection c = DBConnection.getDBConnection();
    private int tcurrent = 64;
    private double alpha = 0.085, beta = 0.085, d = 0.85, gamma1 = 0.085, gamma2 = 0.085, lambda = 0.4;
    private HashMap<Integer, HashSet<Integer>> Rui, Rsk;

    JTextField location, category;
    JButton submit, reset;
    JLabel l1, l2;

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {

        //WebServiceRecommendation mainobj = new WebServiceRecommendation();
        //mainobj.addcategories_in_webserviceinfo_full();
        //mainobj.generate_services_for_functionalities("Germany","Shopping");
        new WebServiceRecommendation();

    }

    WebServiceRecommendation() {
        JFrame f = new JFrame();

        l1 = new JLabel("Enter your location");
        l1.setBounds(50, 50, 150, 20);

        location = new JTextField();
        location.setBounds(50, 75, 150, 20);

        l2 = new JLabel("Enter your category");
        l2.setBounds(50, 115, 150, 20);
        category = new JTextField();
        category.setBounds(50, 140, 150, 20);

        submit = new JButton("Submit");
        submit.setBounds(50, 175, 70, 50);
        reset = new JButton("Reset");
        reset.setBounds(120, 175, 70, 50);
        submit.addActionListener(this);
        reset.addActionListener(this);

        f.add(l1);
        f.add(location);
        f.add(l2);
        f.add(category);
        f.add(submit);
        f.add(reset);

        f.setSize(300, 300);
        f.setLayout(null);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reset) {
            //getContentPane().removeAll();
            location.setText("");
            category.setText("");

        } else if (e.getSource() == submit) {
            String s1 = location.getText();
            String s2 = category.getText();

            try {
                String[][] optimalServices = generate_services_for_functionalities(s1, s2);

                JFrame f = new JFrame();
                String column[] = {"ID", "NAME"};
                JTable jt = new JTable(optimalServices, column);
                jt.setBounds(30, 40, 200, 300);
                JScrollPane sp = new JScrollPane(jt);
                f.add(sp);
                f.setSize(500, 500);
                f.setVisible(true);

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(WebServiceRecommendation.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(WebServiceRecommendation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //String result=String.valueOf(c);  
        // tf3.setText(result);  
    }

    public String[][] generate_services_for_functionalities(String location, String category) throws ClassNotFoundException, SQLException {

        PreparedStatement preparedStatement;
        String selectSQL = "SELECT A.userid, A.serviceid, A.timesliceid,A.responsetime, D.throughput "
                + "FROM userservicematrix_timeinvoked_p A, userinfo_full B, webserviceinfo_full C, USERservicematrix_qos_p D "
                + "WHERE A.userid=B.userid AND C.serviceid= A.serviceid AND D.userid=B.userid AND C.serviceid= D.serviceid and A.timesliceid=D.timesliceid  "
                + " AND B.country=? AND C.category=?  order by A.userid,A.serviceid, A.timesliceid";

        ArrayList<Integer> listWebServices = new ArrayList<Integer>();
        ArrayList<Integer> listUsers = new ArrayList<Integer>();
        preparedStatement = c.prepareStatement(selectSQL);
        preparedStatement.setString(2, category);
        preparedStatement.setString(1, location);

        LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> userMatrix_List = new LinkedHashMap<>();
        LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> serviceMatrix_List = new LinkedHashMap<>();
        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()) {
            UserServiceMatrix_Info userServiceMatrix_Info = new UserServiceMatrix_Info(rs.getInt("userid"),
                    rs.getInt("serviceid"), rs.getInt("timesliceid"), rs.getDouble("responsetime"), rs.getDouble("throughput"));

            //adding in userMatrix_List
            if (userMatrix_List.containsKey(rs.getInt("userid"))) {
                LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> serviceMatrix = userMatrix_List.get(rs.getInt("userid"));
                if (serviceMatrix.containsKey(rs.getInt("serviceid"))) {
                    serviceMatrix.get(rs.getInt("serviceid")).add(userServiceMatrix_Info);
                } else {
                    serviceMatrix.put(rs.getInt("serviceid"), new ArrayList<UserServiceMatrix_Info>());
                    serviceMatrix.get(rs.getInt("serviceid")).add(userServiceMatrix_Info);
                }
            } else {
                userMatrix_List.put(rs.getInt("userid"), new LinkedHashMap<>());
                userMatrix_List.get(rs.getInt("userid")).put(rs.getInt("serviceid"), new ArrayList<>());
                userMatrix_List.get(rs.getInt("userid")).get(rs.getInt("serviceid")).add(userServiceMatrix_Info);
            }

            //adding in serviceMatrix_List
            if (serviceMatrix_List.containsKey(rs.getInt("serviceid"))) {
                LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> userMatrix = serviceMatrix_List.get(rs.getInt("serviceid"));
                if (userMatrix.containsKey(rs.getInt("userid"))) {
                    userMatrix.get(rs.getInt("userid")).add(userServiceMatrix_Info);
                } else {
                    userMatrix.put(rs.getInt("userid"), new ArrayList<UserServiceMatrix_Info>());
                    userMatrix.get(rs.getInt("userid")).add(userServiceMatrix_Info);
                }
            } else {
                serviceMatrix_List.put(rs.getInt("serviceid"), new LinkedHashMap<>());
                serviceMatrix_List.get(rs.getInt("serviceid")).put(rs.getInt("userid"), new ArrayList<>());
                serviceMatrix_List.get(rs.getInt("serviceid")).get(rs.getInt("userid")).add(userServiceMatrix_Info);
            }

            //creating list of web services
            if (!(listWebServices.contains(rs.getInt("serviceid")))) {
                listWebServices.add(rs.getInt("serviceid"));
            }
            if (!(listUsers.contains(rs.getInt("userid")))) {
                listUsers.add(rs.getInt("userid"));
            }

        }
        DecimalFormat df = new DecimalFormat("###.####");
        UserBasedQoSPrediction userBased = new UserBasedQoSPrediction();
        ServiceBasedQoSPrediction serviceBased = new ServiceBasedQoSPrediction();
        QoSPredictionInfo[][] qosUser = userBased.getPrediction(userMatrix_List, listWebServices);
        QoSPredictionInfo[][] qosService = serviceBased.getPrediction(serviceMatrix_List, listUsers);

        double hu = UserBasedQoSPrediction.hut;
        double hs = ServiceBasedQoSPrediction.hst;

        QoSPredictionInfo[][] qos = new QoSPredictionInfo[qosService.length][qosService[0].length];

        for (int i = 0; i < qosService.length; i++) {
            for (int j = 0; j < qosService[0].length; j++) {
                qos[i][j] = new QoSPredictionInfo(qosUser[i][j].userIdi, qosUser[i][j].serviceIdk, (hu * qosUser[i][j].throughputValue) + (hs * qosService[i][j].throughputValue), (hu * qosUser[i][j].responseTimeValue) + (hs * qosService[i][j].responseTimeValue));
            }
        }
        double differencet=0,differencer=0,count=0;
        for (int i = 0; i < qos.length; i++) {
            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesPresenti = userMatrix_List.get(qosUser[i][0].userIdi);
            for (int j = 0; j < qos[0].length; j++) {
                double sumUseriServicekt=0,sumUseriServicekr=0,countUseriServicek=0;
                if (servicesPresenti.containsKey(qosUser[i][j].serviceIdk)){
                ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) servicesPresenti.get(qosUser[i][j].serviceIdk);
                    for (int z = 0; z < detailsi.size(); z++) {
                        sumUseriServicekt += detailsi.get(z).throughput;
                        sumUseriServicekr += detailsi.get(z).responseTime;
                        countUseriServicek += 1;
                    }
                     differencet+=Math.abs(qosUser[i][j].throughputValue - (sumUseriServicekt / countUseriServicek));
                     differencer+=Math.abs(qosUser[i][j].responseTimeValue-(sumUseriServicekr / countUseriServicek));
                     count+=1;
                    
            }}
        }
        System.out.println("Mean Absolute Error for Throughput "+differencet / count);
        System.out.println("Mean Absolute Error for Response Time "+differencer / count);
        Recommendation r = new Recommendation();
        String[][] optimalServices = r.performRecommendation(qos);
        return optimalServices;
    }

}
