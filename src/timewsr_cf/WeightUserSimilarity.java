package timewsr_cf;

class WeightUserSimilarity {

    int userIdi, userIdj;
    double throughputWeight, responseTimeWeight;

    public WeightUserSimilarity(int userIdi, int userIdj, double throughputWeight, double responseTimeWeight) {
        this.userIdi = userIdi;
        this.userIdj = userIdj;
        this.throughputWeight = throughputWeight;
        this.responseTimeWeight = responseTimeWeight;
    }
}
