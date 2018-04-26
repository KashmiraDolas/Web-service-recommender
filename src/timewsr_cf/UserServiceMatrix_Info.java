package timewsr_cf;

public class UserServiceMatrix_Info {

    int userId, serviceId, timeSliceId;
    double responseTime;
    double throughput;

    public UserServiceMatrix_Info(int userId, int serviceId, int timeSliceId, double responseTime, double throughput) {
        this.userId = userId;
        this.serviceId = serviceId;
        this.timeSliceId = timeSliceId;
        this.responseTime = responseTime;
        this.throughput = throughput;

    }
}
