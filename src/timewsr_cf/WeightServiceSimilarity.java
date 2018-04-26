
package timewsr_cf;

public class WeightServiceSimilarity {

    int serviceIdi, serviceIdj;
    double throughputWeight,responseTimeWeight;

    public WeightServiceSimilarity(int serviceIdi, int serviceIdj, double throughputWeight, double responseTimeWeight) {
        this.serviceIdi = serviceIdi;
        this.serviceIdj = serviceIdj;
        this.throughputWeight = throughputWeight;
        this.responseTimeWeight=responseTimeWeight;
    }
}
