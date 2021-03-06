/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package prea.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import prea.data.structure.SparseMatrix;
import prea.data.structure.SparseRowMatrix;
import prea.data.structure.SparseVector;
import prea.recommender.matrix.MatrixFactorizationRecommender;

/**
 * Matrix information utility
 * 
 * @author Hanke Chen
 * @version $Id: MatrixInformationUtil.java, v 0.1 2014-10-28 下午2:55:50 chench Exp $
 */
public final class MatrixInformationUtil {

    /**
     * forbid construction
     */
    private MatrixInformationUtil() {

    }

    /**
     * compute the rating distribution w.r.t the given matrix
     * 
     * @param rateMatrix    the matrix to compute
     * @return the rating distribution
     */
    public static String hierarchy(SparseMatrix rateMatrix) {
        int[] countTable = new int[10];
        int M = rateMatrix.length()[0];
        for (int u = 0; u < M; u++) {
            SparseVector Ru = rateMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double val = Ru.getValue(v);
                int pivot = Double.valueOf(val / 0.5 - 1).intValue();
                countTable[pivot]++;
            }
        }

        StringBuilder msg = new StringBuilder();
        int itemCount = rateMatrix.itemCount();
        for (int i = 0; i < 10; i++) {
            msg.append("\n\t").append(String.format("%.1f", (i + 1) * 0.5)).append('\t')
                .append(countTable[i] * 1.0 / itemCount);
        }

        return msg.toString();
    }

    /**
     * compute the rating distribution w.r.t the given matrix
     * 
     * @param rateMatrix    the matrix to compute
     * @return the rating distribution
     */
    public static String hierarchy(SparseRowMatrix rateMatrix) {
        int[] countTable = new int[10];
        int M = rateMatrix.length()[0];
        for (int u = 0; u < M; u++) {
            SparseVector Ru = rateMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double val = Ru.getValue(v);
                int pivot = Double.valueOf(val / 0.5 - 1).intValue();
                countTable[pivot]++;
            }
        }

        StringBuilder msg = new StringBuilder();
        int itemCount = rateMatrix.itemCount();
        for (int i = 0; i < 10; i++) {
            msg.append("\n\t").append(String.format("%.1f", (i + 1) * 0.5)).append('\t')
                .append(countTable[i] * 1.0 / itemCount);
        }

        return msg.toString();
    }

    /**
     * analyze the error distribution
     * 
     * @param testMatrix        
     * @param predictedMatrix
     * @return
     */
    public static String RMSEAnalysis(SparseRowMatrix testMatrix, SparseRowMatrix predictedMatrix) {
        int[] countTable = new int[10];
        double[] rmseTable = new double[10];
        double[] distanceTable = new double[10];

        int rowCount = testMatrix.length()[0];
        for (int u = 0; u < rowCount; u++) {
            SparseVector Ru = testMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double RuvReal = Ru.getValue(v);
                double RuvEsitm = predictedMatrix.getValue(u, v);

                //RMSE
                int pivot = Double.valueOf(RuvReal / 0.5 - 1).intValue();
                countTable[pivot]++;
                rmseTable[pivot] += Math.pow(RuvReal - RuvEsitm, 2.0d);

                //Distance
                for (int r = 0; r < 10; r++) {
                    distanceTable[r] += Math.pow(RuvEsitm - 0.5 * (r + 1), 2.0d);
                }
            }
        }

        double rmseTotal = 0.0d;
        double countTotal = 0.0d;
        for (int i = 0; i < 10; i++) {
            rmseTotal += rmseTable[i];
            countTotal += countTable[i];
        }

        // message
        double globalRMSE = Math.sqrt(rmseTotal / countTotal);
        StringBuilder msg = new StringBuilder();
        msg.append("RMSE: ").append(String.format("%.6f", globalRMSE) + "\t\t[*]");
        for (int i = 0; i < 10; i++) {
            if (countTable[i] == 0) {
                continue;
            }
            double RMSE = Math.sqrt(rmseTable[i] / countTable[i]);
            double Dis = Math.sqrt(distanceTable[i] / countTotal);
            msg.append("\n\t").append((i + 1) * 0.5).append('[')
                .append(String.format("%.5f", countTable[i] / countTotal)).append("]\t")
                .append(String.format("%.6f", RMSE)).append('\t')
                .append(String.format("%.6f", Dis));
        }
        return msg.toString();
    }

    public static double offlineRMSE(MatrixFactorizationRecommender recmmd, String testFile,
                                     int rowCount, int colCount) {

        double RMSE = 0.0d;
        int itemCount = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(testFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] elems = line.split("\\::");
                int u = Integer.valueOf(elems[0]) + 1;
                int i = Integer.valueOf(elems[1]) + 1;
                double AuiReal = Double.valueOf(elems[2]);
                double AuiEst = recmmd.getPredictedRating(u, i);
                RMSE += Math.pow(AuiReal - AuiEst, 2.0d);
                itemCount++;
            }

            return Math.sqrt(RMSE / itemCount);
        } catch (FileNotFoundException e) {
            ExceptionUtil.caught(e, "无法找到对应的加载文件: " + testFile);
        } catch (IOException e) {
            ExceptionUtil.caught(e, "读取文件发生异常，校验文件格式");
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return 0.0d;
    }

    /**
     * analyze the error distribution
     * 
     * @param testMatrix        
     * @param predictedMatrix
     * @return
     */
    public static String RMSEAnalysis(SparseMatrix testMatrix, SparseMatrix predictedMatrix) {
        int[] countTable = new int[10];
        double[] rmseTable = new double[10];

        int rowCount = testMatrix.length()[0];
        for (int u = 0; u < rowCount; u++) {
            SparseVector Ru = testMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double RuvReal = Ru.getValue(v);
                double RuvEsitm = predictedMatrix.getValue(u, v);

                int pivot = Double.valueOf(RuvReal / 0.5 - 1).intValue();
                countTable[pivot]++;
                rmseTable[pivot] += Math.pow(RuvReal - RuvEsitm, 2.0d);
            }
        }

        double rmseTotal = 0.0d;
        double countTotal = 0.0d;
        for (int i = 0; i < 10; i++) {
            rmseTotal += rmseTable[i];
            countTotal += countTable[i];
        }

        // message
        StringBuilder msg = new StringBuilder("\n");
        for (int i = 0; i < 10; i++) {
            if (countTable[i] == 0) {
                continue;
            }
            double RMSE = Math.sqrt(rmseTable[i] / countTable[i]);
            msg.append("\t").append((i + 1) * 0.5).append('\t')
                .append(String.format("%.5f", countTable[i] / countTotal)).append("\t\t")
                .append(String.format("%.6f", RMSE)).append(" [")
                .append(String.format("%.5f", rmseTable[i] / rmseTotal)).append("]\n");
        }
        return msg.toString();
    }

    public static String PredictionReliabilityAnalysis(SparseRowMatrix testMatrix,
                                                       SparseRowMatrix weightedTestMatrix,
                                                       SparseRowMatrix weightedPredictedMatrix) {
        int[] countTable = new int[10];
        double[] rmseTable = new double[10];

        int rowCount = testMatrix.length()[0];
        for (int u = 0; u < rowCount; u++) {
            SparseVector Ru = testMatrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double RuvReal = testMatrix.getValue(u, v);
                double wReal = weightedTestMatrix.getValue(u, v);
                double wEsitm = weightedPredictedMatrix.getValue(u, v);

                //RMSE
                int pivot = Double.valueOf(RuvReal / 0.5 - 1).intValue();
                countTable[pivot]++;
                rmseTable[pivot] += Math.pow((wReal - wEsitm), 2.0d);
            }
        }

        double rmseTotal = 0.0d;
        double countTotal = 0.0d;
        for (int i = 0; i < 10; i++) {
            rmseTotal += rmseTable[i];
            countTotal += countTable[i];
        }

        // message
        double globalRMSE = Math.sqrt(rmseTotal / countTotal);
        StringBuilder msg = new StringBuilder("RMSE: " + String.format("%.6f", globalRMSE) + "\n");
        for (int i = 0; i < 10; i++) {
            if (countTable[i] == 0) {
                continue;
            }
            double RMSE = Math.sqrt(rmseTable[i] / countTable[i]);
            msg.append("\t").append((i + 1) * 0.5).append('[')
                .append(String.format("%.5f", countTable[i] / countTotal)).append("]\t")
                .append(String.format("%.6f", RMSE)).append('\n');
        }
        return msg.toString();
    }

    public static double[] ratingDistribution(SparseMatrix matrix, double maxValue, double minValue) {
        int len = (int) (maxValue / minValue);
        int[] countTable = new int[len];

        int M = matrix.length()[0];
        for (int u = 0; u < M; u++) {
            SparseVector Ru = matrix.getRowRef(u);
            int[] indexList = Ru.indexList();
            if (indexList == null) {
                continue;
            }

            for (int v : indexList) {
                double val = Ru.getValue(v);
                int pivot = Double.valueOf(val / minValue - 1).intValue();
                countTable[pivot]++;
            }
        }

        double[] result = new double[len];
        int itemCount = matrix.itemCount();
        for (int i = 0; i < len; i++) {
            result[i] = countTable[i] * 1.0 / itemCount;
        }

        return result;
    }
}
