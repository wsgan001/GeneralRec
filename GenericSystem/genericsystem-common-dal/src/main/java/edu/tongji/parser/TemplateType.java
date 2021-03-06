/**
 * Tongji Edu.
 * Copyright (c) 2004-2013 All Rights Reserved.
 */
package edu.tongji.parser;

import edu.tongji.parser.netflix.NetflixRatingVOTemplateParser;
import edu.tongji.parser.netflix.SimilarityTemplateParser;
import edu.tongji.parser.smartgrid.BayesianEventParser;
import edu.tongji.parser.smartgrid.ReddSmartGridTemplateParser;
import edu.tongji.parser.smartgrid.UMassSmartGrid2TemplateParser;
import edu.tongji.parser.smartgrid.UMassSmartGridTemplateParser;

/**
 * 解析模板
 * 
 * @author Hanke Chen
 * @version $Id: TemplateParserType.java, v 0.1 2013-9-6 下午6:31:41 chench Exp $
 */
public enum TemplateType implements Parser, Filter {

    /** 评分解析处理模板 */
    MOVIELENS_RATING_TEMPLATE(new MovielensRatingTemplateParser(), new DefaultTemplateFilter()),

    /** NetFlix评分解析处理模板 */
    NETFLIX_RATINGVO_TEMPLATE(new NetflixRatingVOTemplateParser(), new DefaultTemplateFilter()),
    /** 相似度解析处理模板 */
    SIMILARITY_TEMPLATE(new SimilarityTemplateParser(), new DefaultTemplateFilter()),

    /** UMASS电表读数处理模板*/
    UMASS_SMART_GRID_TEMPLATE(new UMassSmartGridTemplateParser(), new DefaultTemplateFilter()),
    /** UMASS电表读数处理模板*/
    UMASS_SMART_GRID_2_TEMPLATE(new UMassSmartGrid2TemplateParser(), new DefaultTemplateFilter()),

    /** REDD电表读数处理模板*/
    REDD_SMART_GRID_TEMPLATE(new ReddSmartGridTemplateParser(), new DefaultTemplateFilter()),

    /** Bayesian Networks概率事件处理模板*/
    BAYESIAN_EVENT_TEMPLATE(new BayesianEventParser(), new DefaultTemplateFilter());

    /** 模板解析类 */
    private final Parser parser;
    /** 模板过滤类 */
    private final Filter filter;

    private TemplateType(Parser parser, Filter filter) {
        this.parser = parser;
        this.filter = filter;
    }

    /** 
     * @see edu.tongji.parser.Parser#parser(edu.tongji.parser.ParserTemplate)
     */
    @Override
    public Object parser(ParserTemplate template) {
        return this.parser.parser(template);
    }

    /** 
     * @see edu.tongji.parser.Filter#isFiler(edu.tongji.parser.ParserTemplate)
     */
    @Override
    public boolean isFiler(ParserTemplate template) {
        return this.filter.isFiler(template);
    }

    /** 
     * @see edu.tongji.parser.Parser#parse(java.lang.String)
     */
    @Override
    public Object parse(String template) {
        return this.parser.parse(template);
    }

}
