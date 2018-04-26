package timewsr_cf;

import Jama.Matrix;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ServiceBasedQoSPrediction {

    private double alpha = 0.085, beta = 0.085, d = 0.85, gamma1 = 0.085, gamma2 = 0.085, lambda = 0.4;
    private int tcurrent = 64;
    static double hst, hsr;

    public QoSPredictionInfo[][] getPrediction(LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> serviceMatrix_List, ArrayList<Integer> listUsers) {
        DecimalFormat df = new DecimalFormat("###.####");

        WeightServiceSimilarity[][] weightServiceSimilarityMatrix = time_aware_similarity_computation_services(serviceMatrix_List);

        WeightServiceSimilarity[][] reconstructedServiceSimilarity = calculateServiceSimilarityInference(weightServiceSimilarityMatrix);

        QoSPredictionInfo[][] qosService = calculateQoSPredictionService(reconstructedServiceSimilarity, serviceMatrix_List, listUsers);

        consolidatePredictionService(reconstructedServiceSimilarity);

        return qosService;
    }

    public QoSPredictionInfo[][] calculateQoSPredictionService(WeightServiceSimilarity[][] reconstructedServiceSimilarity, LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> serviceMatrix_List, ArrayList<Integer> listUsers) {

        QoSPredictionInfo[][] qosPrediction = new QoSPredictionInfo[listUsers.size()][reconstructedServiceSimilarity.length];

        for (int k = 0; k < reconstructedServiceSimilarity.length; k++) {

            int servicek = reconstructedServiceSimilarity[k][0].serviceIdi;
            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> useri = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) serviceMatrix_List.get(servicek);
            Iterator usri = useri.entrySet().iterator();
            double sumqkt = 0, sumqkr = 0;
            int countqk = 0;
            while (usri.hasNext()) {
                Map.Entry pairi = (Map.Entry) usri.next();

                ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) pairi.getValue();
                for (int z = 0; z < detailsi.size(); z++) {
                    sumqkt += detailsi.get(z).throughput;
                    sumqkr += detailsi.get(z).responseTime;
                    countqk += 1;
                }
            }
            double qkbart = sumqkt / countqk;
            double qkbarr = sumqkr / countqk;
            for (int i = 0; i < listUsers.size(); i++) {
                int userIdi = listUsers.get(i);

                LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> UsersPresenti = serviceMatrix_List.get(servicek);
                //if (!UsersPresenti.containsKey(userIdi)) {
                    double numeratort = 0, denominatort = 0, numeratorr = 0, denominatorr = 0;

                    for (int l = 0; l < reconstructedServiceSimilarity[k].length; l++) {

                        int serviceIdj = reconstructedServiceSimilarity[k][l].serviceIdj;
                        LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> UsersPresentj = serviceMatrix_List.get(serviceIdj);
                        if (UsersPresentj.containsKey(userIdi) && reconstructedServiceSimilarity[k][l].throughputWeight > 0) {
                            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> usersj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) serviceMatrix_List.get(serviceIdj);
                            Iterator usrj = usersj.entrySet().iterator();
                            double sumqj = 0;
                            int countpj = 0;
                            while (usrj.hasNext()) {
                                Map.Entry pairj = (Map.Entry) usrj.next();

                                ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairj.getValue();
                                for (int z = 0; z < detailsj.size(); z++) {
                                    sumqj += detailsj.get(z).throughput;
                                    countpj += 1;
                                }
                            }
                            double qlbar = sumqj / countpj;

                            if (reconstructedServiceSimilarity[k][l].serviceIdi != reconstructedServiceSimilarity[k][l].serviceIdj) {
                                ArrayList<UserServiceMatrix_Info> f4t = serviceMatrix_List.get(serviceIdj).get(userIdi);
                                double temp = 0, f4sum = 0;
                                for (int p = 0; p < f4t.size(); p++) {

                                    double f4 = Math.exp(-1 * gamma2 * Math.abs(tcurrent - f4t.get(p).timeSliceId));

                                    temp += (f4 * Math.abs(f4t.get(p).throughput - qlbar));
                                    f4sum += f4;
                                }

                                numeratort += (reconstructedServiceSimilarity[k][l].throughputWeight * temp) / f4t.size();
                                denominatort += ((reconstructedServiceSimilarity[k][l].throughputWeight * f4sum) / f4t.size());
                            }

                        }

                        if (UsersPresentj.containsKey(userIdi) && reconstructedServiceSimilarity[k][l].responseTimeWeight > 0) {

                            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> usersj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) serviceMatrix_List.get(serviceIdj);
                            Iterator usrj = usersj.entrySet().iterator();
                            double sumqj = 0;
                            int countpj = 0;
                            while (usrj.hasNext()) {
                                Map.Entry pairj = (Map.Entry) usrj.next();

                                ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairj.getValue();
                                for (int z = 0; z < detailsj.size(); z++) {
                                    sumqj += detailsj.get(z).responseTime;
                                    countpj += 1;
                                }
                            }
                            double qlbar = sumqj / countpj;

                            if (reconstructedServiceSimilarity[k][l].serviceIdi != reconstructedServiceSimilarity[k][l].serviceIdj) {
                                ArrayList<UserServiceMatrix_Info> f4t = serviceMatrix_List.get(serviceIdj).get(userIdi);
                                double temp = 0, f4sum = 0;
                                for (int p = 0; p < f4t.size(); p++) {

                                    double f4 = Math.exp(-1 * gamma2 * Math.abs(tcurrent - f4t.get(p).timeSliceId));

                                    temp += (f4 * Math.abs(f4t.get(p).responseTime - qlbar));
                                    f4sum += f4;
                                }

                                numeratorr += (reconstructedServiceSimilarity[k][l].responseTimeWeight * temp) / f4t.size();
                                denominatorr += ((reconstructedServiceSimilarity[k][l].responseTimeWeight * f4sum) / f4t.size());
                            }

                        }

                    }
                    double qCapSIKt = 0, qCapSIKr = 0;
                    if (denominatort > 0) {
                        qCapSIKt = qkbart + (numeratort / denominatort);
                    }
                    if (denominatorr > 0) {
                        qCapSIKr = qkbarr + (numeratorr / denominatorr);
                    }
                    QoSPredictionInfo qosValue = new QoSPredictionInfo(userIdi, servicek, qCapSIKt, qCapSIKr);
                    qosPrediction[i][k] = qosValue;

              /*  } else {
                    double sumUseriServicekt = 0, sumUseriServicekr = 0, countUseriServicek = 0;
                    ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) UsersPresenti.get(userIdi);
                    for (int z = 0; z < detailsi.size(); z++) {
                        sumUseriServicekt += detailsi.get(z).throughput;
                        sumUseriServicekr += detailsi.get(z).responseTime;
                        countUseriServicek += 1;
                    }
                    QoSPredictionInfo qosValue = new QoSPredictionInfo(userIdi, servicek, sumUseriServicekt / countUseriServicek, sumUseriServicekr / countUseriServicek);
                    qosPrediction[i][k] = qosValue;
                }*/
            }
        }

        return qosPrediction;

    }

    public WeightServiceSimilarity[][] calculateServiceSimilarityInference(WeightServiceSimilarity[][] weightServiceSimilarityMatrix) {
        WeightServiceSimilarity[][] reconstructedSimilarity = new WeightServiceSimilarity[weightServiceSimilarityMatrix.length][weightServiceSimilarityMatrix.length];

        HashMap<Integer, HashSet<Integer>> R = new HashMap<Integer, HashSet<Integer>>();

        for (int i = 0; i < weightServiceSimilarityMatrix.length; i++) {
            Matrix weightMatrixt = new Matrix(weightServiceSimilarityMatrix.length, weightServiceSimilarityMatrix.length);
            Matrix weightMatrixr = new Matrix(weightServiceSimilarityMatrix.length, weightServiceSimilarityMatrix.length);
            Matrix p = new Matrix(weightServiceSimilarityMatrix.length, 1);

            p.set(i, 0, 1.0);

            for (int j = 0; j < weightServiceSimilarityMatrix.length; j++) {

                double colsumt = 0, colsumr = 0;
                for (int k = 0; k < weightServiceSimilarityMatrix.length; k++) {
                    colsumt += weightServiceSimilarityMatrix[k][j].throughputWeight;
                    colsumr += weightServiceSimilarityMatrix[k][j].responseTimeWeight;
                }
                for (int k = 0; k < weightServiceSimilarityMatrix.length; k++) {
                    if (colsumt == 0) {
                        weightMatrixt.set(k, j, 0);
                    } else {
                        double result = weightServiceSimilarityMatrix[k][j].throughputWeight / colsumt;

                        weightMatrixt.set(k, j, result);
                    }
                    if (colsumr == 0) {
                        weightMatrixr.set(k, j, 0);
                    } else {
                        double result = weightServiceSimilarityMatrix[k][j].responseTimeWeight / colsumr;

                        weightMatrixr.set(k, j, result);
                    }
                }

            }

            Matrix inverseMatrixt = Matrix.identity(weightServiceSimilarityMatrix.length, weightServiceSimilarityMatrix.length).minus(weightMatrixt.times(d)).inverse().times(1 - d);
            Matrix inverseMatrixr = Matrix.identity(weightServiceSimilarityMatrix.length, weightServiceSimilarityMatrix.length).minus(weightMatrixr.times(d)).inverse().times(1 - d);

            Matrix rt = inverseMatrixt.times(p);
            Matrix rr = inverseMatrixr.times(p);

            rt.set(i, 0, 0);
            rr.set(i, 0, 0);
            double sumt = 0, sumr = 0;
            HashSet<Integer> Nuit = new HashSet<>();
            HashSet<Integer> Nuir = new HashSet<>();
            for (int j = 0; j < weightServiceSimilarityMatrix.length; j++) {
                if (weightServiceSimilarityMatrix[i][j].throughputWeight > 0) {
                    Nuit.add(weightServiceSimilarityMatrix[i][j].serviceIdj);
                    if (rt.get(j, 0) != 0) {
                        sumt += (weightServiceSimilarityMatrix[i][j].throughputWeight / rt.get(j, 0));
                    }
                }
                if (weightServiceSimilarityMatrix[i][j].responseTimeWeight > 0) {
                    Nuir.add(weightServiceSimilarityMatrix[i][j].serviceIdj);
                    if (rr.get(j, 0) != 0) {
                        sumr += (weightServiceSimilarityMatrix[i][j].responseTimeWeight / rr.get(j, 0));
                    }
                }
            }
            Matrix reconSimilarityMatrixt = new Matrix(1, weightServiceSimilarityMatrix.length);
            Matrix reconSimilarityMatrixr = new Matrix(1, weightServiceSimilarityMatrix.length);
            if (Nuit.size() > 0 && sumt > 0) {
                reconSimilarityMatrixt = rt.transpose().times(sumt / (Nuit.size()));
            } else {
                reconSimilarityMatrixt = rt.transpose().times(0.0);
            }
            if (Nuir.size() > 0 && sumr > 0) {
                reconSimilarityMatrixr = rr.transpose().times(sumr / (Nuir.size()));
            } else {
                reconSimilarityMatrixr = rr.transpose().times(0.0);
            }

            for (int j = 0; j < weightServiceSimilarityMatrix.length; j++) {
                reconstructedSimilarity[i][j] = new WeightServiceSimilarity(weightServiceSimilarityMatrix[i][j].serviceIdi, weightServiceSimilarityMatrix[i][j].serviceIdj, reconSimilarityMatrixt.get(0, j), reconSimilarityMatrixr.get(0, j));
            }

        }

        return reconstructedSimilarity;
    }

    public WeightServiceSimilarity[][] time_aware_similarity_computation_services(LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> serviceMatrix_List) {
        WeightServiceSimilarity[][] weight = new WeightServiceSimilarity[serviceMatrix_List.size()][serviceMatrix_List.size()];
        HashSet<String> pairOfServicesComputed = new HashSet<String>();
        Iterator iti = serviceMatrix_List.entrySet().iterator();
        int weightRow = 0, weightColumn = 0;
        while (iti.hasNext()) {
            Map.Entry pairi = (Map.Entry) iti.next();

            Iterator itj = serviceMatrix_List.entrySet().iterator();
            while (itj.hasNext()) {
                Map.Entry pairj = (Map.Entry) itj.next();
                if (pairi.getKey() != pairj.getKey() && !(pairOfServicesComputed.contains(pairi.getKey() + "-" + pairj.getKey()) || pairOfServicesComputed.contains(pairj.getKey() + "-" + pairi.getKey()))) {
                    pairOfServicesComputed.add(pairi.getKey() + "-" + pairj.getKey());
                    LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> useri = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) pairi.getValue();
                    LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> userj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) pairj.getValue();
                    int n = Math.max(useri.size(), userj.size());

                    HashSet<Integer> servicesUserI = new HashSet<Integer>();
                    HashSet<Integer> servicesUserJ = new HashSet<Integer>();
                    Iterator usri = useri.entrySet().iterator();
                    Iterator usrj = userj.entrySet().iterator();
                    double sumqit = 0, sumqjt = 0, sumqir = 0, sumqjr = 0;
                    int countpi = 0, countpj = 0;

                    for (int i = 0; i < n; i++) {
                        if (usri.hasNext()) {
                            Map.Entry pairii = (Map.Entry) usri.next();
                            int useridi = (int) pairii.getKey();
                            servicesUserI.add(useridi);
                            ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) pairii.getValue();
                            for (int j = 0; j < detailsi.size(); j++) {
                                sumqit += detailsi.get(j).throughput;
                                sumqir += detailsi.get(j).responseTime;
                                countpi += 1;
                            }

                        }

                        if (usrj.hasNext()) {
                            Map.Entry pairjj = (Map.Entry) usrj.next();
                            int useridj = (int) pairjj.getKey();
                            servicesUserJ.add(useridj);
                            ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairjj.getValue();
                            for (int j = 0; j < detailsj.size(); j++) {
                                sumqjt += detailsj.get(j).throughput;
                                sumqjr += detailsj.get(j).responseTime;

                                countpj += 1;
                            }

                        }
                    }

                    double qibart = sumqit / countpi;
                    double qjbart = sumqjt / countpj;
                    double qibarr = sumqir / countpi;
                    double qjbarr = sumqjr / countpj;

                    Set<Integer> intersectionServices = new HashSet<Integer>(servicesUserI);
                    intersectionServices.retainAll(servicesUserJ);

                    double sim_weight = (double) (2 * intersectionServices.size()) / (servicesUserI.size() + servicesUserJ.size());
                    Iterator<Integer> itis = intersectionServices.iterator();

                    // to store math.pow((qik - qibar),2)
                    double Ait = 0, Ajt = 0, Air = 0, Ajr = 0, At, Ar;
                    while (itis.hasNext()) {
                        int userid = (int) itis.next();

                        int countOfInvocationi = useri.get(userid).size();
                        double diffofqit = 0, diffofqjt = 0, diffofqir = 0, diffofqjr = 0;
                        ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) useri.get(userid);
                        for (int j = 0; j < detailsi.size(); j++) {
                            diffofqit += Math.pow((detailsi.get(j).throughput - qibart), 2);
                            diffofqir += Math.pow((detailsi.get(j).responseTime - qibarr), 2);
                        }

                        int countOfInvocationj = userj.get(userid).size();
                        ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) userj.get(userid);
                        for (int j = 0; j < detailsj.size(); j++) {
                            diffofqjt += Math.pow((detailsj.get(j).throughput - qjbart), 2);
                            diffofqjr += Math.pow((detailsj.get(j).responseTime - qjbarr), 2);
                        }

                        Ait += ((diffofqit / countOfInvocationi));
                        Ajt += ((diffofqjt / countOfInvocationj));
                        Air += ((diffofqir / countOfInvocationi));
                        Ajr += ((diffofqjr / countOfInvocationj));

                    }

                    At = Math.sqrt(Ait) * Math.sqrt(Ajt);
                    Ar = Math.sqrt(Air) * Math.sqrt(Ajr);

                    itis = intersectionServices.iterator();
                    double sumt = 0, sumr = 0;
                    while (itis.hasNext()) {
                        int serviceid = (int) itis.next();
                        double sum_est = 0, sum_esr = 0;
                        ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) useri.get(serviceid);
                        ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) userj.get(serviceid);
                        for (int j = 0; j < detailsi.size(); j++) {
                            for (int k = 0; k < detailsj.size(); k++) {
                                double f1 = Math.exp(-1 * alpha * Math.abs(detailsi.get(j).timeSliceId - detailsj.get(k).timeSliceId));

                                double f2 = Math.exp(-1 * beta * Math.abs(tcurrent - ((detailsi.get(j).timeSliceId + detailsj.get(k).timeSliceId) / 2)));

                                sum_est += ((detailsi.get(j).throughput - qibart) * (detailsj.get(k).throughput - qjbart) * f1 * f2);
                                sum_esr += ((detailsi.get(j).responseTime - qibarr) * (detailsj.get(k).responseTime - qjbarr) * f1 * f2);
                            }
                        }
                        sumt += ((sum_est / (detailsi.size() * detailsj.size())));
                        sumr += ((sum_esr / (detailsi.size() * detailsj.size())));
                    }

                    double wt = 0, wr = 0;
                    if (At != 0) {
                        wt = (sim_weight * sumt) / At;
                    }
                    if (Ar != 0) {
                        wr = (sim_weight * sumr) / Ar;
                    }

                    weight[weightRow][weightColumn] = new WeightServiceSimilarity((int) pairi.getKey(), (int) pairj.getKey(), wt, wr);
                    weight[weightColumn][weightRow] = new WeightServiceSimilarity((int) pairj.getKey(), (int) pairi.getKey(), wt, wr);

                } else {
                    weight[weightRow][weightRow] = new WeightServiceSimilarity((int) pairi.getKey(), (int) pairj.getKey(), 0.0, 0);

                }
                weightColumn++;
            }
            weightRow++;
            weightColumn = 0;
        }
        return weight;
    }

    public void consolidatePredictionService(WeightServiceSimilarity[][] reconstructedServiceSimilarity) {
        double consth = 0, consr = 0;
        for (int k = 0; k < reconstructedServiceSimilarity.length; k++) {
            double sumt = 0, sumr = 0;
            for (int l = 0; l < reconstructedServiceSimilarity[k].length; l++) {
                sumt += reconstructedServiceSimilarity[k][l].throughputWeight;
                sumr += reconstructedServiceSimilarity[k][l].responseTimeWeight;
            }
            if (sumt > 0) {
                for (int l = 0; l < reconstructedServiceSimilarity[k].length; l++) {
                    consth += Math.pow(reconstructedServiceSimilarity[k][l].throughputWeight, 2) / sumt;
                }
            }

            if (sumr > 0) {
                for (int l = 0; l < reconstructedServiceSimilarity[k].length; l++) {
                    consr += Math.pow(reconstructedServiceSimilarity[k][l].responseTimeWeight, 2) / sumr;
                }
            }
        }
        hst = ((1 - lambda) * consth) / ((lambda * consth) + ((1 - lambda) * consth));
        hsr = ((1 - lambda) * consr) / ((lambda * consr) + ((1 - lambda) * consr));

    }
}