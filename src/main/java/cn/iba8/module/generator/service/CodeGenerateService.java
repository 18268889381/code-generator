package cn.iba8.module.generator.service;

import cn.iba8.module.generator.common.ftl.TemplateDefinition;
import cn.iba8.module.generator.common.request.CodeTemplateGenerateRequest;

import java.util.List;

/*
 * @Author sc.wan
 * @Description 代码生成
 * @Date 16:10 2019/8/26
 * @Param
 * @return
 **/
public interface CodeGenerateService {

    List<TemplateDefinition.TemplateFileBean> getCodeFiles(CodeTemplateGenerateRequest request);

}
