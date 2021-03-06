/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package edu.tongji.noise;

import org.apache.commons.math3.distribution.UniformRealDistribution;

import edu.tongji.exception.FunctionErrorCode;
import edu.tongji.exception.OwnedException;
import edu.tongji.noise.support.NoiseParamSupport;

/**
 * 高斯混合模型噪声 <br/>
 * Smart Grid应用中：K个Component, K-1个噪声，第K个为估计量
 * 
 * @author Hanke Chen
 * @version $Id: GaussMixtureNoise.java, v 0.1 2 Apr 2014 17:21:25 chench Exp $
 */
public class GaussMixtureNoise implements Noise {

    /** 混合高斯分布隐变量参数*/
    private double[]                      weight;

    /** 正态分布*/
    private NormalNoise[]                 normalNoise;

    /** 概率转盘[0,1]*/
    private final UniformRealDistribution uniform = new UniformRealDistribution();

    /**
     * 
     * 
     * @param noiseParamSupport
     */
    public GaussMixtureNoise(NoiseParamSupport noiseParamSupport) {
        this.normalNoise = noiseParamSupport.gmm();
        this.weight = noiseParamSupport.getWeighit();
    }

    /** 
     * @see edu.tongji.noise.Noise#random()
     */
    @Override
    public double random() {
        throw new OwnedException(FunctionErrorCode.NOT_DEFINITION);
    }

    /** 
     * @see edu.tongji.noise.Noise#perturb(double)
     */
    @Override
    public double perturb(double input) {
        //1. 隐参数投点
        double index = uniform.sample() - weight[0];

        // Smart Grid应用中：K个Component,
        //第1个为电表参数估计量
        if (index <= 0) {
            return input;
        }

        // K-1个噪声，第K个为电表参数估计量
        for (int i = 1, numNoise = weight.length; i < numNoise; i++) {

            //2.  走轮盘，落点则返回
            if ((index -= weight[i]) < 0) {
                return normalNoise[i].random();
            }

        }
        return normalNoise[weight.length - 1].random();
    }

    /** 
     * @see edu.tongji.noise.Noise#standardDeviation()
     */
    @Override
    public double standardDeviation() {
        return Double.NaN;
    }

    /** 
     * @see edu.tongji.noise.Noise#mean()
     */
    @Override
    public double mean() {
        return Double.NaN;
    }

    /** 
     * @see edu.tongji.noise.Noise#getName()
     */
    @Override
    public String getName() {
        return "高斯混合模型";
    }

    /**
     * Getter method for property <tt>weight</tt>.
     * 
     * @return property value of weight
     */
    public double[] getWeight() {
        return weight;
    }

    /**
     * Setter method for property <tt>weight</tt>.
     * 
     * @param weight value to be assigned to property weight
     */
    public void setWeight(double[] weight) {
        //单位化概率为1
        double sum = 0.0d;
        for (int i = 0; i < weight.length; i++) {
            sum += weight[i];
        }

        for (int i = 0; i < weight.length; i++) {
            weight[i] /= sum;
        }

        this.weight = weight;
    }

    /**
     * Getter method for property <tt>normalNoise</tt>.
     * 
     * @return property value of normalNoise
     */
    public NormalNoise[] getNormalNoise() {
        return normalNoise;
    }

    /**
     * Setter method for property <tt>normalNoise</tt>.
     * 
     * @param normalNoise value to be assigned to property normalNoise
     */
    public void setNormalNoise(NormalNoise[] normalNoise) {
        this.normalNoise = normalNoise;
    }

}
