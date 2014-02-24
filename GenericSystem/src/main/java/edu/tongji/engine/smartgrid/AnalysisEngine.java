/**
 * Tongji Edu.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package edu.tongji.engine.smartgrid;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.util.StopWatch;

import edu.tongji.crack.CrackObject;
import edu.tongji.crack.PrivacyCracker;
import edu.tongji.extend.gnuplot.FigureFormatter;
import edu.tongji.extend.noise.Noise;
import edu.tongji.orm.SmartGridDataSource;
import edu.tongji.util.DateUtil;
import edu.tongji.util.GnuplotUtil;
import edu.tongji.util.LoggerUtil;
import edu.tongji.vo.MeterReadingVO;

/**
 * 针对智能电网的分析引擎
 * 
 * @author chench
 * @version $Id: AnalysisEngine.java, v 0.1 2014-2-14 下午12:42:00 chench Exp $
 */
public class AnalysisEngine extends SmartGridEngine {
    /** 高斯噪声产生范围*/
    private Noise[]         noises;

    /** 主部和高斯噪声对应的比重系数*/
    private double[]        weightDomain = { 1.0 };

    /** 列休息数据*/
    private String          columnMsg;

    /** 数据文件存储绝对地址 */
    private String          absolutePath;

    /** 隐私破解器*/
    private PrivacyCracker  cracker;

    /** 图像格式器*/
    private FigureFormatter formatter;

    /** 
     * @see edu.tongji.engine.smartgrid.SmartGridEngine#assembleDataSet()
     */
    @Override
    protected void assembleDataSet() {
        //物理数据集为老数据集，
        //逻辑数据集保持稳健
        if (!dataSource.isFresh() && keepSteady) {
            return;
        }

        //交替上下文
        List<MeterReadingVO> context = new ArrayList<MeterReadingVO>();
        context.addAll(SmartGridDataSource.meterContexts);
        SmartGridDataSource.meterContexts.clear();

        //对上下文进行排序
        Collections.sort(context, new Comparator<MeterReadingVO>() {
            @Override
            public int compare(MeterReadingVO o1, MeterReadingVO o2) {
                return ((Long) (o1.getTimeVal() - o2.getTimeVal())).intValue();
            }
        });

        //处理数据集
        MeterReadingVO meterReading = null;
        for (MeterReadingVO meter : context) {

            if (meterReading == null) {
                //初始化
                meterReading = meter;
                continue;
            } else if (!DateUtil.sameDayAndHour(meterReading.getTimeVal(), meter.getTimeVal())) {
                LoggerUtil.debug(logger, "O：" + meterReading.getReading() + "   Date："
                                         + new Timestamp(meterReading.getTimeVal()));

                //新的电表计时周期
                SmartGridDataSource.meterContexts.add(meterReading);
                meterReading = meter;

                continue;
            }
            //在同一计时周期，累计读数
            meterReading.setReading(meterReading.getReading() + meter.getReading());

        }
    }

    /** 
     * @see edu.tongji.engine.smartgrid.SmartGridEngine#emulate()
     */
    @Override
    protected void emulate() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        //1.在原始数据后，添加隐私保护的数据
        int blockSize = SmartGridDataSource.meterContexts.size();
        for (int index = 0; index < blockSize; index++) {
            MeterReadingVO reading = SmartGridDataSource.meterContexts.get(index);
            double reads = reading.getReading() * weightDomain[0];
            for (int i = 0; i < noises.length; i++) {
                reads += weightDomain[i + 1] * noises[i].random();
            }
            LoggerUtil.debug(logger, "O：" + reading.getReading() + " R：" + reads);

            SmartGridDataSource.meterContexts.add(new MeterReadingVO(reads, null, reading
                .getTimeVal()));
        }

        //2.Crack Privacy Scheme
        cracker.crack(new CrackObject(SmartGridDataSource.meterContexts), blockSize);

        //3.按不同图像要求，格式化数据
        List<String> stream = new ArrayList<String>();
        stream.add(columnMsg);
        stream.addAll(formatter.format(SmartGridDataSource.meterContexts, blockSize));

        //4.持久化操作，存储到目标文件
        String fileAbsolutePath = (new StringBuilder(absolutePath)).append(
            DateUtil.formatCurrent(DateUtil.LONG_FORMAT)).toString();
        GnuplotUtil.genDataFile(stream, SmartGridDataSource.meterContexts.size() / blockSize,
            fileAbsolutePath, formatter.needRowNum());
    }

    protected void prepareDataSet() {

    }

    /**
     * Getter method for property <tt>noises</tt>.
     * 
     * @return property value of noises
     */
    public Noise[] getNoises() {
        return noises;
    }

    /**
     * Setter method for property <tt>noises</tt>.
     * 
     * @param noises value to be assigned to property noises
     */
    public void setNoises(Noise[] noises) {
        this.noises = noises;
    }

    /**
     * Getter method for property <tt>weightDomain</tt>.
     * 
     * @return property value of weightDomain
     */
    public double[] getWeightDomain() {
        return weightDomain;
    }

    /**
     * Setter method for property <tt>weightDomain</tt>.
     * 
     * @param weightDomain value to be assigned to property weightDomain
     */
    public void setWeightDomain(double[] weightDomain) {
        this.weightDomain = weightDomain;
    }

    /**
     * Getter method for property <tt>absolutePath</tt>.
     * 
     * @return property value of absolutePath
     */
    public String getAbsolutePath() {
        return absolutePath;
    }

    /**
     * Setter method for property <tt>absolutePath</tt>.
     * 
     * @param absolutePath value to be assigned to property absolutePath
     */
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    /**
     * Getter method for property <tt>formatter</tt>.
     * 
     * @return property value of formatter
     */
    public FigureFormatter getFormatter() {
        return formatter;
    }

    /**
     * Setter method for property <tt>formatter</tt>.
     * 
     * @param formatter value to be assigned to property formatter
     */
    public void setFormatter(FigureFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Getter method for property <tt>columnMsg</tt>.
     * 
     * @return property value of columnMsg
     */
    public String getColumnMsg() {
        return columnMsg;
    }

    /**
     * Setter method for property <tt>columnMsg</tt>.
     * 
     * @param columnMsg value to be assigned to property columnMsg
     */
    public void setColumnMsg(String columnMsg) {
        this.columnMsg = columnMsg;
    }

    /**
     * Getter method for property <tt>cracker</tt>.
     * 
     * @return property value of cracker
     */
    public PrivacyCracker getCracker() {
        return cracker;
    }

    /**
     * Setter method for property <tt>cracker</tt>.
     * 
     * @param cracker value to be assigned to property cracker
     */
    public void setCracker(PrivacyCracker cracker) {
        this.cracker = cracker;
    }

}