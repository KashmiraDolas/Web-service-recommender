
package timewsr_cf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeMap;

class Recommendation {
    private Connection c = DBConnection.getDBConnection();
    public String[][] performRecommendation(QoSPredictionInfo[][] qos) throws SQLException {
        DecisionCriteriaValue[][] decisionMatrix = new DecisionCriteriaValue[qos[0].length][2];
        for (int i = 0; i < qos[0].length; i++) {
            double sumt = 0, sumr = 0;
            for (int l = 0; l < qos.length; l++) {
                sumt += qos[l][i].throughputValue;
                sumr += qos[l][i].responseTimeValue;
            }
            decisionMatrix[i][0] = new DecisionCriteriaValue(qos[0][i].serviceIdk, "Throughput", sumt / qos.length);
            decisionMatrix[i][1] = new DecisionCriteriaValue(qos[0][i].serviceIdk, "ResponseTime", sumr / qos.length);
        }

        double[] w = {0.5, 0.5};
        DecisionCriteriaValue[][] overallAssessmentEachCriteria = new DecisionCriteriaValue[qos[0].length][2];

        for (int i = 0; i < decisionMatrix.length; i++) {
            overallAssessmentEachCriteria[i][0] = new DecisionCriteriaValue(decisionMatrix[i][0].serviceId, "Throughput", decisionMatrix[i][1].value * w[1]);
            overallAssessmentEachCriteria[i][1] = new DecisionCriteriaValue(decisionMatrix[i][0].serviceId, "ResponseTime", decisionMatrix[i][0].value * w[0]);
        }

        double[] R = new double[2];
        double[] std = new double[2];
        for (int j = 0; j < 2; j++) {
            double numerator = 0, denominator = 0;
            double djbar = 0, xjbar = 0, sumdj = 0, sumxj = 0;
            for (int i = 0; i < overallAssessmentEachCriteria.length; i++) {
                sumdj += overallAssessmentEachCriteria[i][j].value;
                sumxj += decisionMatrix[i][j].value;
            }
            djbar = sumdj / overallAssessmentEachCriteria.length;
            xjbar = sumxj / decisionMatrix.length;

            double denompartA = 0, denompartB = 0;
            for (int i = 0; i < 2; i++) {

                numerator += ((overallAssessmentEachCriteria[i][j].value - djbar) * (decisionMatrix[i][j].value - xjbar));
                denompartA += Math.pow(overallAssessmentEachCriteria[i][j].value - djbar, 2);
                denompartB += Math.pow(decisionMatrix[i][j].value - xjbar, 2);
            }
            denominator = Math.sqrt(denompartA * denompartB);
            std[j] = Math.sqrt(denompartB / overallAssessmentEachCriteria.length);
            R[j] = numerator / denominator;
        }

        double[] wnew = new double[2];
        double sum = 0;
        for (int j = 0; j < 2; j++) {
            sum += (std[j] * Math.sqrt((1 - R[j])));
        }

        wnew[0] = (std[0] * Math.sqrt((1 - R[0]))) / sum;
        wnew[1] = (std[1] * Math.sqrt((1 - R[1]))) / sum;
        
        String services="";
        
        PriorityQueue<DecisionCriteriaValue> pq = new PriorityQueue<>( new Comparator<DecisionCriteriaValue>() {
        @Override
        public int compare(DecisionCriteriaValue lhs, DecisionCriteriaValue rhs) {
        if (lhs.value < rhs.value) return +1;
        
        return -1;
        }
        });
        
        //DecisionCriteriaValue[] d = new DecisionCriteriaValue[overallAssessmentEachCriteria.length];
        for (int i = 0; i < overallAssessmentEachCriteria.length; i++) {
            double temp = (wnew[0] * decisionMatrix[i][0].value) + (wnew[1] * decisionMatrix[i][1].value);
            
            DecisionCriteriaValue service = new DecisionCriteriaValue(decisionMatrix[i][0].serviceId,"",temp);
            pq.add(service);
        }
        int i=0;
        Iterator ser=pq.iterator();
         while ( ser.hasNext() && i<3) {
            DecisionCriteriaValue service =(DecisionCriteriaValue) ser.next();
            services+=service.serviceId+",";
            i++;
         }
        
        PreparedStatement preparedStatement;
        String selectSQL = "SELECT A.serviceid,A.wsdladdress from webserviceinfo_full A where A.serviceid in ("+services.substring(0, services.length()-1)+")";
        preparedStatement = c.prepareStatement(selectSQL);
        ResultSet rs = preparedStatement.executeQuery();
        String[][] optimalServices= new String[3][2];
        i=0;
        while (rs.next()) {
            optimalServices[i][0]=rs.getString("serviceid");
            optimalServices[i][1]=rs.getString("wsdladdress");
            i++;
            
        }
        return optimalServices;
    }

}

class DecisionCriteriaValue {

    int serviceId;
    double value;
    String criteria;

    public DecisionCriteriaValue(int serviceId, String criteria, double value) {

        this.serviceId = serviceId;
        this.criteria = criteria;
        this.value = value;
    }
}
