package cn.iba8.module.generator.service.biz;

import cn.iba8.module.generator.common.BaseException;
import cn.iba8.module.generator.common.ResponseCode;
import cn.iba8.module.generator.common.enums.TemplateScopeEnum;
import cn.iba8.module.generator.common.ftl.TemplateDefinition;
import cn.iba8.module.generator.common.request.CodeTemplateGenerateRequest;
import cn.iba8.module.generator.repository.dao.*;
import cn.iba8.module.generator.repository.entity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CodeGenerateBizService {

    private final ModuleRepository moduleRepository;

    private final ModuleMetaDatabaseTableRepository moduleMetaDatabaseTableRepository;

    private final MetaDatabaseTableColumnRepository metaDatabaseTableColumnRepository;

    private final CodeTemplateRepository codeTemplateRepository;

    private final MetaDatabaseTableRepository metaDatabaseTableRepository;

    private final CodeTemplateSuffixRepository codeTemplateSuffixRepository;

    private final CodeTemplateCodeClassRepository codeTemplateCodeClassRepository;

    public List<TemplateDefinition.TemplateFileBean> getCodeFiles(CodeTemplateGenerateRequest request) {
        String moduleCode = request.getModuleCode();
        String version = request.getVersion();
        String typeGroup = request.getTypeGroup();
        String templateGroup = request.getTemplateGroup();
        Module module = moduleRepository.findFirstByCodeAndVersion(moduleCode, version);
        if (null == module) {
            throw BaseException.of(ResponseCode.MODULE_NOT_EXIST);
        }
        List<ModuleMetaDatabaseTable> moduleMetaDatabaseTables = moduleMetaDatabaseTableRepository.findAllByModuleCode(module.getCode());
        if (CollectionUtils.isEmpty(moduleMetaDatabaseTables)) {
            return null;
        }
        Set<Long> tableIds = moduleMetaDatabaseTables.stream().map(ModuleMetaDatabaseTable::getMetaDatabaseTableId).collect(Collectors.toSet());
        List<MetaDatabaseTableColumn> metaDatabaseTableIdIn = metaDatabaseTableColumnRepository.findAllByMetaDatabaseTableIdIn(tableIds);
        if (CollectionUtils.isEmpty(metaDatabaseTableIdIn)) {
            return null;
        }
        List<CodeTemplate> codeTemplateList = codeTemplateRepository.findAllByTypeGroupAndTemplateGroupAndLatest(typeGroup, templateGroup, 1);
        List<CodeTemplate> codeTemplates = codeTemplateFilter(codeTemplateList, request);
        if (CollectionUtils.isEmpty(codeTemplates)) {
            return null;
        }
        List<MetaDatabaseTable> metaDatabaseTables = metaDatabaseTableRepository.findAllById(tableIds);
        if (CollectionUtils.isEmpty(metaDatabaseTables)) {
            return null;
        }
        List<CodeTemplateSuffix> codeTemplateSuffixes = codeTemplateSuffixRepository.findAllByTypeGroupAndTemplateGroup(typeGroup, templateGroup);
        if (CollectionUtils.isEmpty(codeTemplateSuffixes)) {
            return null;
        }
        List<CodeTemplateCodeClass> codeTemplateCodeClasses = codeTemplateCodeClassRepository.findAllByTypeGroupAndTemplateGroup(typeGroup, templateGroup);
        Map<String, CodeTemplateCodeClass> codeTemplateCodeClassMap = codeTemplateCodeClasses.stream().collect(Collectors.toMap(CodeTemplateCodeClass::getType, r -> r, (k1, k2) -> k2));
        Map<String, CodeTemplateSuffix> codeTemplateSuffixMap = codeTemplateSuffixes.stream().collect(Collectors.toMap(CodeTemplateSuffix::getType, r -> r, (k1, k2) -> k2));
        Map<Long, List<MetaDatabaseTableColumn>> columnsMap = metaDatabaseTableIdIn.stream().collect(Collectors.groupingBy(MetaDatabaseTableColumn::getMetaDatabaseTableId));
        List<TemplateDefinition.TemplateFileBean> templateFileBeanList = new ArrayList<>();
        for (MetaDatabaseTable metaDatabaseTable : metaDatabaseTables) {
            List<MetaDatabaseTableColumn> metaDatabaseTableColumns = columnsMap.get(metaDatabaseTable.getId());
            if (CollectionUtils.isEmpty(metaDatabaseTableColumns)) {
                continue;
            }
            TemplateDefinition.TableColumnBean tableColumnBean = TemplateDefinition.TableColumnBean.of(module, metaDatabaseTable, metaDatabaseTableColumns);
            List<TemplateDefinition.TemplateFileBean> templateFileBeans = TemplateDefinition.TemplateFileBean.of(tableColumnBean, codeTemplates, codeTemplateSuffixMap, codeTemplateCodeClassMap);
            templateFileBeanList.addAll(templateFileBeans);
        }
        return templateFileBeanList;
    }

    private List<CodeTemplate> codeTemplateFilter(List<CodeTemplate> codeTemplates, CodeTemplateGenerateRequest request) {
        List<CodeTemplate> target = new ArrayList<>();
        if (!CollectionUtils.isEmpty(codeTemplates)) {
            for (CodeTemplate r : codeTemplates) {
                if (!request.isWithMethod()) {
                    if (TemplateScopeEnum.METHOD.getName().equals(r.getScope())) {
                        continue;
                    }
                }
                if (!request.isWithMethodExcel()) {
                    if (r.getExcel() == 1) {
                        continue;
                    }
                }

                target.add(r);
            }
        }
        return target;
    }

}
