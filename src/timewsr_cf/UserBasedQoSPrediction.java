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

public class UserBasedQoSPrediction {

    private double alpha = 0.085, beta = 0.085, d = 0.85, gamma1 = 0.085, lambda = 0.4;
    private int tcurrent = 64;
    static double hut, hur;

    public QoSPredictionInfo[][] getPrediction(LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> userMatrix_List, ArrayList<Integer> listWebServices) {
        DecimalFormat df = new DecimalFormat("###.####");
        WeightUserSimilarity[][] weightUserSimilarityMatrix = time_aware_similarity_computation_users(userMatrix_List);

        WeightUserSimilarity[][] reconstructedUserSimilarity = calculateUserSimilarityInference(weightUserSimilarityMatrix);

        QoSPredictionInfo[][] qosUser = calculateQoSPredictionUser(reconstructedUserSimilarity, userMatrix_List, listWebServices);

        consolidatePredictionUser(reconstructedUserSimilarity);
        return qosUser;
    }

    public void consolidatePredictionUser(WeightUserSimilarity[][] reconstructedUserSimilarity) {
        double conut = 0, conur = 0;
        for (int i = 0; i < reconstructedUserSimilarity.length; i++) {
            double sumt = 0, sumr = 0;
            for (int j = 0; j < reconstructedUserSimilarity[i].length; j++) {
                sumt += reconstructedUserSimilarity[i][j].throughputWeight;
                sumr += reconstructedUserSimilarity[i][j].responseTimeWeight;
            }
            if (sumt > 0) {
                for (int j = 0; j < reconstructedUserSimilarity[i].length; j++) {
                    conut += Math.pow(reconstructedUserSimilarity[i][j].throughputWeight, 2) / sumt;
                }
            }
            if (sumr > 0) {
                for (int j = 0; j < reconstructedUserSimilarity[i].length; j++) {
                    conur += Math.pow(reconstructedUserSimilarity[i][j].responseTimeWeight, 2) / sumr;
                }
            }
        }

        hut = (lambda * conut) / ((lambda * conut) + ((1 - lambda) * conut));
        hur = (lambda * conur) / ((lambda * conur) + ((1 - lambda) * conur));

    }

    public WeightUserSimilarity[][] time_aware_similarity_computation_users(LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> userMatrix_List) {
        WeightUserSimilarity[][] weight = new WeightUserSimilarity[userMatrix_List.size()][userMatrix_List.size()];
        HashSet<String> pairOfUsersComputed = new HashSet<String>();
        Iterator iti = userMatrix_List.entrySet().iterator();
        int weightRow = 0, weightColumn = 0;
        while (iti.hasNext()) {
            Map.Entry pairi = (Map.Entry) iti.next();

            Iterator itj = userMatrix_List.entrySet().iterator();
            while (itj.hasNext()) {
                Map.Entry pairj = (Map.Entry) itj.next();
                if (pairi.getKey() != pairj.getKey() && !(pairOfUsersComputed.contains(pairi.getKey() + "-" + pairj.getKey()) || pairOfUsersComputed.contains(pairj.getKey() + "-" + pairi.getKey()))) {
                    pairOfUsersComputed.add(pairi.getKey() + "-" + pairj.getKey());
                    LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesi = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) pairi.getValue();
                    LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) pairj.getValue();
                    int n = Math.max(servicesi.size(), servicesj.size());
                    HashSet<Integer> servicesUserI = new HashSet<Integer>();
                    HashSet<Integer> servicesUserJ = new HashSet<Integer>();
                    Iterator seri = servicesi.entrySet().iterator();
                    Iterator serj = servicesj.entrySet().iterator();
                    double sumqit = 0, sumqir = 0, sumqjr = 0, sumqjt = 0;
                    int countpi = 0, countpj = 0;
                    for (int i = 0; i < n; i++) {
                        if (seri.hasNext()) {
                            Map.Entry pairii = (Map.Entry) seri.next();
                            int serviceid = (int) pairii.getKey();
                            servicesUserI.add(serviceid);
                            ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) pairii.getValue();
                            for (int j = 0; j < detailsi.size(); j++) {
                                sumqit += detailsi.get(j).throughput;
                                sumqir += detailsi.get(j).responseTime;
                                countpi += 1;
                            }
                        }

                        if (serj.hasNext()) {
                            Map.Entry pairjj = (Map.Entry) serj.next();
                            int serviceid = (int) pairjj.getKey();
                            servicesUserJ.add(serviceid);
                            ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairjj.getValue();
                            for (int j = 0; j < detailsj.size(); j++) {
                                sumqjt += detailsj.get(j).throughput;
                                sumqjr += detailsj.get(j).responseTime;
                                countpj += 1;
                            }
                        }
                    }

                    double qibart = sumqit / countpi;
                    double qibarr = sumqir / countpi;
                    double qjbart = sumqjt / countpj;
                    double qjbarr = sumqjr / countpj;

                    Set<Integer> intersectionServices = new HashSet<Integer>(servicesUserI); // use the copy constructor
                    intersectionServices.retainAll(servicesUserJ);
                    double sim_weight = (double) (2 * intersectionServices.size()) / (servicesUserI.size() + servicesUserJ.size());
                    Iterator<Integer> itis = intersectionServices.iterator();
                    // to store math.pow((qik - qibar),2)
                    double Ait = 0, Ajt = 0, At, Air = 0, Ajr = 0, Ar;
                    while (itis.hasNext()) {
                        int serviceid = (int) itis.next();
                        int countOfInvocationi = servicesi.get(serviceid).size();
                        double diffofqit = 0, diffofqjt = 0, diffofqir = 0, diffofqjr = 0;
                        ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) servicesi.get(serviceid);
                        for (int j = 0; j < detailsi.size(); j++) {
                            diffofqit += Math.pow((detailsi.get(j).throughput - qibart), 2);
                            diffofqir += Math.pow((detailsi.get(j).responseTime - qibarr), 2);
                        }

                        int countOfInvocationj = servicesj.get(serviceid).size();
                        ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) servicesj.get(serviceid);
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
                        ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) servicesi.get(serviceid);
                        ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) servicesj.get(serviceid);
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
                    weight[weightRow][weightColumn] = new WeightUserSimilarity((int) pairi.getKey(), (int) pairj.getKey(), wt, wr);
                    weight[weightColumn][weightRow] = new WeightUserSimilarity((int) pairj.getKey(), (int) pairi.getKey(), wt, wr);

                } else {
                    weight[weightRow][weightRow] = new WeightUserSimilarity((int) pairi.getKey(), (int) pairj.getKey(), 0.0, 0.0);
                }
                weightColumn++;
            }
            weightRow++;
            weightColumn = 0;
        }
        return weight;
    }

    public QoSPredictionInfo[][] calculateQoSPredictionUser(WeightUserSimilarity[][] reconstructedUserSimilarity, LinkedHashMap<Integer, LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>> userMatrix_List, ArrayList<Integer> listWebServices) {

        QoSPredictionInfo[][] qosPrediction = new QoSPredictionInfo[reconstructedUserSimilarity.length][listWebServices.size()];

        for (int i = 0; i < reconstructedUserSimilarity.length; i++) {

            int useri = reconstructedUserSimilarity[i][0].userIdi;

            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesi = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) userMatrix_List.get(useri);
            Iterator seri = servicesi.entrySet().iterator();
            double sumqit = 0, sumqir = 0;
            int countpi = 0;
            while (seri.hasNext()) {
                Map.Entry pairi = (Map.Entry) seri.next();

                ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) pairi.getValue();
                for (int z = 0; z < detailsi.size(); z++) {
                    sumqit += detailsi.get(z).throughput;
                    sumqir += detailsi.get(z).responseTime;
                    countpi += 1;
                }
            }
            double qibart = sumqit / countpi;
            double qibarr = sumqir / countpi;
            for (int k = 0; k < listWebServices.size(); k++) {
                int servicek = listWebServices.get(k);

                LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesPresenti = userMatrix_List.get(useri);
               // if (!servicesPresenti.containsKey(servicek)) {
                    double numeratort = 0, denominatort = 0, numeratorr = 0, denominatorr = 0;

                    for (int j = 0; j < reconstructedUserSimilarity[i].length; j++) {

                        int userj = reconstructedUserSimilarity[i][j].userIdj;

                        LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesPresent = userMatrix_List.get(userj);
                        if (servicesPresent.containsKey(servicek) && reconstructedUserSimilarity[i][j].throughputWeight > 0) {
                            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) userMatrix_List.get(userj);
                            Iterator serj = servicesj.entrySet().iterator();
                            double sumqj = 0;
                            int countpj = 0;
                            while (serj.hasNext()) {
                                Map.Entry pairj = (Map.Entry) serj.next();

                                ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairj.getValue();
                                for (int z = 0; z < detailsj.size(); z++) {
                                    sumqj += detailsj.get(z).throughput;
                                    countpj += 1;
                                }
                            }
                            double qjbar = sumqj / countpj;

                            if (reconstructedUserSimilarity[i][j].userIdi != reconstructedUserSimilarity[i][j].userIdj) {
                                ArrayList<UserServiceMatrix_Info> f3t = userMatrix_List.get(userj).get(servicek);
                                double temp = 0, f3sum = 0;
                                for (int p = 0; p < f3t.size(); p++) {

                                    double f3 = Math.exp(-1 * gamma1 * Math.abs(tcurrent - f3t.get(p).timeSliceId));

                                    temp += (f3 * Math.abs(f3t.get(p).throughput - qjbar));
                                    f3sum += f3;
                                }

                                numeratort += (reconstructedUserSimilarity[i][j].throughputWeight * temp) / f3t.size();
                                denominatort += ((reconstructedUserSimilarity[i][j].throughputWeight * f3sum) / f3t.size());
                            }

                        }

                        if (servicesPresent.containsKey(servicek) && reconstructedUserSimilarity[i][j].responseTimeWeight > 0) {
                            LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>> servicesj = (LinkedHashMap<Integer, ArrayList<UserServiceMatrix_Info>>) userMatrix_List.get(userj);
                            Iterator serj = servicesj.entrySet().iterator();
                            double sumqj = 0;
                            int countpj = 0;
                            while (serj.hasNext()) {
                                Map.Entry pairj = (Map.Entry) serj.next();

                                ArrayList<UserServiceMatrix_Info> detailsj = (ArrayList<UserServiceMatrix_Info>) pairj.getValue();
                                for (int z = 0; z < detailsj.size(); z++) {
                                    sumqj += detailsj.get(z).responseTime;
                                    countpj += 1;
                                }
                            }
                            double qjbar = sumqj / countpj;

                            if (reconstructedUserSimilarity[i][j].userIdi != reconstructedUserSimilarity[i][j].userIdj) {
                                ArrayList<UserServiceMatrix_Info> f3t = userMatrix_List.get(userj).get(servicek);
                                double temp = 0, f3sum = 0;
                                for (int p = 0; p < f3t.size(); p++) {

                                    double f3 = Math.exp(-1 * gamma1 * Math.abs(tcurrent - f3t.get(p).timeSliceId));
                                    if (f3 > 1 || f3 < 0) {
                                        //System.out.println("f3 " + f3);
                                    }
                                    temp += (f3 * Math.abs(f3t.get(p).responseTime - qjbar));
                                    f3sum += f3;
                                }

                                numeratorr += (reconstructedUserSimilarity[i][j].responseTimeWeight * temp) / f3t.size();
                                denominatorr += ((reconstructedUserSimilarity[i][j].responseTimeWeight * f3sum) / f3t.size());
                            }

                        }

                    }

                    double qCapUIKt = 0, qCapUIKr = 0;
                    if (denominatort > 0) {
                        qCapUIKt = qibart + (numeratort / denominatort);
                    }
                    if (denominatorr > 0) {
                        qCapUIKr = qibarr + (numeratorr / denominatorr);
                    }
                    QoSPredictionInfo qosValue = new QoSPredictionInfo(useri, servicek, qCapUIKt, qCapUIKr);
                    qosPrediction[i][k] = qosValue;

                /*} else {
                    double sumUseriServicekt = 0, sumUseriServicekr = 0, countUseriServicek = 0;
                    ArrayList<UserServiceMatrix_Info> detailsi = (ArrayList<UserServiceMatrix_Info>) servicesPresenti.get(servicek);
                    for (int z = 0; z < detailsi.size(); z++) {
                        sumUseriServicekt += detailsi.get(z).throughput;
                        sumUseriServicekr += detailsi.get(z).responseTime;
                        countUseriServicek += 1;
                    }
                    QoSPredictionInfo qosValue = new QoSPredictionInfo(useri, servicek, sumUseriServicekt / countUseriServicek, sumUseriServicekr / countUseriServicek);

                    qosPrediction[i][k] = qosValue;
                }*/
            }
        }
        return qosPrediction;

    }

    public WeightUserSimilarity[][] calculateUserSimilarityInference(WeightUserSimilarity[][] weightUserSimilarityMatrix) {
        WeightUserSimilarity[][] reconstructedSimilarity = new WeightUserSimilarity[weightUserSimilarityMatrix.length][weightUserSimilarityMatrix.length];

        HashMap<Integer, HashSet<Integer>> R = new HashMap<Integer, HashSet<Integer>>();

        for (int i = 0; i < weightUserSimilarityMatrix.length; i++) { //changed form weightUserSimilarityMatrix.length to 1
            Matrix weightMatrixt = new Matrix(weightUserSimilarityMatrix.length, weightUserSimilarityMatrix.length);
            Matrix weightMatrixr = new Matrix(weightUserSimilarityMatrix.length, weightUserSimilarityMatrix.length);
            Matrix p = new Matrix(weightUserSimilarityMatrix.length, 1);

            p.set(i, 0, 1.0);

            for (int j = 0; j < weightUserSimilarityMatrix.length; j++) {

                double colsumt = 0, colsumr = 0;
                for (int k = 0; k < weightUserSimilarityMatrix.length; k++) {
                    colsumt += weightUserSimilarityMatrix[k][j].throughputWeight;
                    colsumr += weightUserSimilarityMatrix[k][j].responseTimeWeight;
                }
                for (int k = 0; k < weightUserSimilarityMatrix.length; k++) {
                    if (colsumt == 0) {
                        weightMatrixt.set(k, j, 0);
                    } else {
                        double result = weightUserSimilarityMatrix[k][j].throughputWeight / colsumt;

                        weightMatrixt.set(k, j, result);
                    }
                    if (colsumr == 0) {
                        weightMatrixr.set(k, j, 0);
                    } else {
                        double result = weightUserSimilarityMatrix[k][j].responseTimeWeight / colsumr;

                        weightMatrixr.set(k, j, result);
                    }
                }

            }

            Matrix inverseMatrixt = Matrix.identity(weightUserSimilarityMatrix.length, weightUserSimilarityMatrix.length).minus(weightMatrixt.times(d)).inverse().times(1 - d);
            Matrix inverseMatrixr = Matrix.identity(weightUserSimilarityMatrix.length, weightUserSimilarityMatrix.length).minus(weightMatrixr.times(d)).inverse().times(1 - d);
            Matrix rt = inverseMatrixt.times(p);
            Matrix rr = inverseMatrixr.times(p);

            rt.set(i, 0, 0);
            rr.set(i, 0, 0);
            double sumt = 0, sumr = 0;
            HashSet<Integer> Nuit = new HashSet<>();
            HashSet<Integer> Nuir = new HashSet<>();
            for (int j = 0; j < weightUserSimilarityMatrix.length; j++) {
                if (weightUserSimilarityMatrix[i][j].throughputWeight > 0) {
                    Nuit.add(weightUserSimilarityMatrix[i][j].userIdj);
                    if (rt.get(j, 0) != 0) {
                        sumt += (weightUserSimilarityMatrix[i][j].throughputWeight / rt.get(j, 0));
                    }
                }
                if (weightUserSimilarityMatrix[i][j].responseTimeWeight > 0) {
                    Nuir.add(weightUserSimilarityMatrix[i][j].userIdj);
                    if (rr.get(j, 0) != 0) {
                        sumr += (weightUserSimilarityMatrix[i][j].responseTimeWeight / rr.get(j, 0));
                    }
                }
            }
            Matrix reconSimilarityMatrixt = new Matrix(1, weightUserSimilarityMatrix.length);
            Matrix reconSimilarityMatrixr = new Matrix(1, weightUserSimilarityMatrix.length);

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

            for (int j = 0; j < weightUserSimilarityMatrix.length; j++) {
                reconstructedSimilarity[i][j] = new WeightUserSimilarity(weightUserSimilarityMatrix[i][j].userIdi, weightUserSimilarityMatrix[i][j].userIdj, reconSimilarityMatrixt.get(0, j), reconSimilarityMatrixr.get(0, j));
            }
        }
        return reconstructedSimilarity;
    }
}
