package edu.tongji.ml.matrix.variant;

import prea.util.EvaluationMetrics;
import edu.tongji.data.MatlabFasionSparseMatrix;
import edu.tongji.data.SparseColumnMatrix;
import edu.tongji.data.SparseMatrix;
import edu.tongji.data.SparseRowMatrix;
import edu.tongji.data.SparseVector;
import edu.tongji.ml.matrix.MatrixFactorizationRecommender;
import edu.tongji.util.FileUtil;
import edu.tongji.util.LoggerUtil;

/**
 * 
 * @author Hanke
 * @version $Id: ConstraintRSVD.java, v 0.1 2015-3-30 下午7:39:01 Exp $
 */
public class ISVD extends MatrixFactorizationRecommender {
    /** SerialVersionNum */
    private static final long   serialVersionUID = 1L;

    /** User profile in low-rank matrix form. */
    protected SparseRowMatrix[] userFeaturesAss;
    /** User profile in low-rank matrix form. */
    protected int[]             assigmnt;

    /*========================================
     * Constructors
     *========================================*/
    /**
     * Construct a matrix-factorization model with the given data.
     * 
     * @param uc The number of users in the dataset.
     * @param ic The number of items in the dataset.
     * @param max The maximum rating value in the dataset.
     * @param min The minimum rating value in the dataset.
     * @param fc The number of features used for describing user and item profiles.
     * @param lr Learning rate for gradient-based or iterative optimization.
     * @param r Controlling factor for the degree of regularization. 
     * @param m Momentum used in gradient-based or iterative optimization.
     * @param iter The maximum number of iterations.
     */
    public ISVD(int uc, int ic, double max, double min, int fc, double lr, double r, double m,
                int iter, int k, int[] ia, boolean verbose) {
        super(uc, ic, max, min, fc, lr, r, m, iter, verbose);
        userFeaturesAss = new SparseRowMatrix[k];
        this.assigmnt = ia;
    }

    /*========================================
     * Model Builder
     *========================================*/
    /**
     * @see edu.tongji.ml.matrix.MatrixFactorizationRecommender#buildModel(edu.tongji.data.SparseRowMatrix)
     */
    @Override
    public void buildModel(SparseRowMatrix rateMatrix, SparseRowMatrix testMatrix) {
        initFeatures();

        // Gradient Descent:
        int round = 0;
        int rateCount = rateMatrix.itemCount();
        double prevErr = 99999;
        double currErr = 9999;

        while (Math.abs(prevErr - currErr) > 0.0001 && round < maxIter) {
            double sum = 0.0;
            for (int u = 0; u < userCount; u++) {
                SparseVector items = rateMatrix.getRowRef(u);
                int[] itemIndexList = items.indexList();

                if (itemIndexList != null) {
                    for (int i : itemIndexList) {
                        //global model
                        SparseVector Fu = userFeatures.getRowRef(u);
                        SparseVector Gi = itemFeatures.getColRef(i);
                        double AuiEst = Fu.innerProduct(Gi);
                        double AuiReal = rateMatrix.getValue(u, i);
                        double err = AuiReal - AuiEst;
                        sum += Math.pow(err, 2.0d);

                        // item clustering local models
                        SparseVector Su = userFeaturesAss[assigmnt[i]].getRowRef(u);
                        double IuiEst = Su.innerProduct(Gi);
                        double errIui = AuiReal - IuiEst;

                        for (int s = 0; s < featureCount; s++) {
                            double Fus = userFeatures.getValue(u, s);
                            double fus = userFeaturesAss[assigmnt[i]].getValue(u, s);
                            double Gis = itemFeatures.getValue(s, i);

                            //local models updates
                            userFeaturesAss[assigmnt[i]].setValue(u, s,
                                fus + learningRate * (errIui * Gis - regularizer * fus));

                            //global model updates
                            userFeatures.setValue(u, s, Fus + learningRate
                                                        * (err * Gis - regularizer * Fus));
                            itemFeatures.setValue(s, i, Gis
                                                        + learningRate
                                                        * (err * Fus + errIui * fus - regularizer
                                                                                      * Gis));
                        }
                    }
                }
            }

            prevErr = currErr;
            currErr = Math.sqrt(sum / rateCount);

            round++;
            if (showProgress && (round % 10 == 0)) {
                EvaluationMetrics metric = this.evaluate(tMatrix);
                FileUtil.writeAsAppend(
                    "E://IC[" + featureCount + "]_k" + userFeaturesAss.length + "_" + maxIter,
                    round + "\t" + String.format("%.4f", currErr) + "\t"
                            + String.format("%.4f", metric.getRMSE()) + "\n");
            }

            // Show progress:
            LoggerUtil.info(logger, round + "\t" + currErr);
        }
    }

    /** 
     * @see edu.tongji.ml.matrix.MatrixFactorizationRecommender#buildModel(edu.tongji.data.SparseMatrix)
     */
    @Override
    public void buildModel(SparseMatrix rateMatrix) {
        initFeatures();

        // Gradient Descent:
        int round = 0;
        int rateCount = rateMatrix.itemCount();
        double prevErr = 99999;
        double currErr = 9999;

        while (Math.abs(prevErr - currErr) > 0.0001 && round < maxIter) {
            double sum = 0.0;
            for (int u = 0; u < userCount; u++) {
                SparseVector items = rateMatrix.getRowRef(u);
                int[] itemIndexList = items.indexList();

                if (itemIndexList != null) {
                    for (int i : itemIndexList) {
                        //global model
                        SparseVector Fu = userFeatures.getRowRef(u);
                        SparseVector Gi = itemFeatures.getColRef(i);
                        double AuiEst = Fu.innerProduct(Gi);
                        double AuiReal = rateMatrix.getValue(u, i);
                        double err = AuiReal - AuiEst;
                        sum += Math.pow(err, 2.0d);

                        // item clustering local models
                        SparseVector Su = userFeaturesAss[assigmnt[i]].getRowRef(u);
                        double IuiEst = Su.innerProduct(Gi);
                        double errIui = AuiReal - IuiEst;

                        for (int s = 0; s < featureCount; s++) {
                            double Fus = userFeatures.getValue(u, s);
                            double fus = userFeaturesAss[assigmnt[i]].getValue(u, s);
                            double Gis = itemFeatures.getValue(s, i);

                            //local models updates
                            userFeaturesAss[assigmnt[i]].setValue(u, s,
                                fus + learningRate * (errIui * Gis - regularizer * fus));

                            //global model updates
                            userFeatures.setValue(u, s, Fus + learningRate
                                                        * (err * Gis - regularizer * Fus));
                            itemFeatures.setValue(s, i, Gis
                                                        + learningRate
                                                        * (err * Fus + errIui * fus - regularizer
                                                                                      * Gis));
                        }
                    }
                }
            }

            prevErr = currErr;
            currErr = Math.sqrt(sum / rateCount);

            round++;
            if (showProgress && (round % 10 == 0)) {
                EvaluationMetrics metric = this.evaluate(tMatrix);
                FileUtil.writeAsAppend(
                    "E://IC[" + featureCount + "]_k" + userFeaturesAss.length + "_" + maxIter,
                    round + "\t" + String.format("%.4f", currErr) + "\t"
                            + String.format("%.4f", metric.getRMSE()) + "\n");
            }

            // Show progress:
            LoggerUtil.info(logger, round + "\t" + currErr);
        }
    }

    protected void initFeatures() {
        // Initialize user features:
        userFeatures = new SparseRowMatrix(userCount, featureCount);
        for (int k = 0; k < userFeaturesAss.length; k++) {
            userFeaturesAss[k] = new SparseRowMatrix(userCount, featureCount);
        }
        for (int u = 0; u < userCount; u++) {
            for (int f = 0; f < featureCount; f++) {
                double rdm = Math.random() / featureCount;
                userFeatures.setValue(u, f, rdm);
            }

            for (int k = 0; k < userFeaturesAss.length; k++) {
                for (int f = 0; f < featureCount; f++) {
                    double rdm = Math.random() / featureCount;
                    userFeaturesAss[k].setValue(u, f, rdm);
                }
            }
        }

        // Initialize item features:
        itemFeatures = new SparseColumnMatrix(featureCount, itemCount);
        for (int i = 0; i < itemCount; i++) {
            for (int f = 0; f < featureCount; f++) {
                double rdm = Math.random() / featureCount;
                itemFeatures.setValue(f, i, rdm);
            }
        }
    }

    /**
     * @see edu.tongji.ml.matrix.MatrixFactorizationRecommender#buildModel(edu.tongji.data.MatlabFasionSparseMatrix, edu.tongji.data.MatlabFasionSparseMatrix)
     */
    @Override
    public void buildModel(MatlabFasionSparseMatrix rateMatrix, MatlabFasionSparseMatrix tMatrix) {
        throw new RuntimeException(
            "buildModel for MatlabFasionSparseMatrix requires implementation!");
    }
}
