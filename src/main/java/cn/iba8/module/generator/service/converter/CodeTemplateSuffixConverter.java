package cn.iba8.module.generator.service.converter;

import cn.iba8.module.generator.common.ftl.FileTemplateDefinition;
import cn.iba8.module.generator.repository.entity.CodeTemplateSuffix;

public abstract class CodeTemplateSuffixConverter {

    public static CodeTemplateSuffix toCodeTemplateSuffix(FileTemplateDefinition.FileTemplateSuffixDefinition fileTemplateDefinition, String templateGroup) {
        CodeTemplateSuffix target = new CodeTemplateSuffix();
        target.setFileSuffix(fileTemplateDefinition.getFileSuffix());
        target.setMd5(fileTemplateDefinition.md5(templateGroup));
        target.setPackageSuffix(fileTemplateDefinition.getPackageSuffix());
        target.setType(fileTemplateDefinition.getFileType());
        target.setTypeGroup(fileTemplateDefinition.getFileTypeGroup());
        target.setTemplateGroup(fileTemplateDefinition.getFileTypeGroup());
        target.setTemplateGroup(templateGroup);
        return target;
    }

}
