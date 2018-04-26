package timewsr_cf;

public class QoSPredictionInfo {

    int userIdi, serviceIdk;
    double throughputValue, responseTimeValue;

    public QoSPredictionInfo(int userIdi, int serviceIdk, double throughputValue, double responseTimeValue) {
        this.userIdi = userIdi;
        this.serviceIdk = serviceIdk;
        this.throughputValue = throughputValue;
        this.responseTimeValue = responseTimeValue;
    }
}
