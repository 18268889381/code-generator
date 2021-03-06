package cn.iba8.module.generator.service.biz;

import cn.iba8.module.generator.common.enums.FileSuffixEnum;
import cn.iba8.module.generator.common.enums.I18nFileTypeEnum;
import cn.iba8.module.generator.common.i18n.I18nFileDefinition;
import cn.iba8.module.generator.common.i18n.I18nKVLanguage;
import cn.iba8.module.generator.common.util.CopyUtil;
import cn.iba8.module.generator.common.util.Json2Map;
import cn.iba8.module.generator.config.CodeGeneratorProperties;
import cn.iba8.module.generator.repository.dao.*;
import cn.iba8.module.generator.repository.entity.*;
import cn.iba8.module.generator.service.converter.I18nConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class I18nBizService {

    private final CodeGeneratorProperties codeGeneratorProperties;

    private final I18nFileOriginRepository i18nFileOriginRepository;

    private final ModuleRepository moduleRepository;

    private final I18nLanguageRepository i18nLanguageRepository;

    private final I18nCodeLanguageRepository i18nCodeLanguageRepository;

    private final I18nCodeRepository i18nCodeRepository;

    private final AppRepository appRepository;

    private final AppModuleRepository appModuleRepository;

    private final I18nFileTargetRepository i18nFileTargetRepository;

    @Transactional(rollbackFor = Exception.class)
    public Set<String> loadI18n() {
        String inputDir = codeGeneratorProperties.getInputDir();
        I18nFileDefinition i18nFileDefinition = I18nFileDefinition.of(inputDir);
        if (null == i18nFileDefinition || CollectionUtils.isEmpty(i18nFileDefinition.getModules())) {
            return null;
        }
        List<I18nFileDefinition.I18nFileDefinitionModule> modules = i18nFileDefinition.getModules();
        List<Module> targetModules = new ArrayList<>();
        List<I18nFileOrigin> targetFileOrigins = new ArrayList<>();
        List<I18nLanguage> targetLanguages = new ArrayList<>();
        List<I18nLanguage> languages = new ArrayList<>();
        Set<String> filePaths = new HashSet<>();
        for (I18nFileDefinition.I18nFileDefinitionModule definitionModule : modules) {
            Module module = moduleRepository.findFirstByCodeAndVersion(definitionModule.getCode(), definitionModule.getVersion());
            if (null == module) {
                log.warn("模块不存在 {}:{}, 将自动创建", definitionModule.getCode(), definitionModule.getVersion());
                Module copy = CopyUtil.copy(definitionModule, Module.class);
                copy.setName(definitionModule.getCode());
                copy.setPackageName("");
                copy.setRestClient("");
                copy.setRestClientRoute("");
                targetModules.add(copy);
            }
            List<I18nFileDefinition.I18nFileDefinitionFile> moduleFiles = definitionModule.getFiles();
            if (!CollectionUtils.isEmpty(moduleFiles)) {
                Set<String> existLan = new HashSet<>();
                for (I18nFileDefinition.I18nFileDefinitionFile i18nFileDefinitionFile : moduleFiles) {
                    filePaths.add(i18nFileDefinitionFile.getPath());
                    I18nFileOrigin i18nFileOrigin = CopyUtil.copy(i18nFileDefinitionFile, I18nFileOrigin.class);
                    long l = i18nFileOriginRepository.countAllByMd5(i18nFileDefinitionFile.getMd5());
                    i18nFileOrigin.setCreateTs(System.currentTimeMillis());
                    i18nFileOrigin.setModuleCode(definitionModule.getCode());
                    if (l > 0) {
                        i18nFileOrigin.setProcessed(2);
                        i18nFileOrigin.setModifyTs(System.currentTimeMillis());
                    } else {
                        i18nFileOrigin.setProcessed(0);
                        i18nFileOrigin.setModifyTs(0L);
                    }
                    targetFileOrigins.add(i18nFileOrigin);
                    String exist = definitionModule.getCode() + "_" + i18nFileDefinitionFile.getLanguage();
                    if (!existLan.contains(exist)) {
                        I18nLanguage i18nLanguage = new I18nLanguage();
                        i18nLanguage.setCode(i18nFileDefinitionFile.getLanguage());
                        i18nLanguage.setName(i18nFileDefinitionFile.getLanguage());
                        i18nLanguage.setModuleCode(definitionModule.getCode());
                        languages.add(i18nLanguage);
                    } else {
                        existLan.add(exist);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(languages)) {
            List<I18nLanguage> i18nLanguages = i18nLanguageRepository.findAll();
            Map<String, I18nLanguage> existMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(i18nLanguages)) {
                i18nLanguages.forEach(r -> existMap.put(r.getModuleCode() + "_" + r.getCode(), r));
            }
            languages.forEach(r -> {
                I18nLanguage nLanguage = existMap.get(r.getModuleCode() + "_" + r.getCode());
                if (null == nLanguage) {
                    I18nLanguage i18nLanguage = new I18nLanguage();
                    i18nLanguage.setCode(r.getCode());
                    i18nLanguage.setName(r.getName());
                    i18nLanguage.setModuleCode(r.getModuleCode());
                    targetLanguages.add(i18nLanguage);
                }
            });
        }
        if (!CollectionUtils.isEmpty(targetModules)) {
            moduleRepository.saveAll(targetModules);
        }
        if (!CollectionUtils.isEmpty(targetLanguages)) {
            i18nLanguageRepository.saveAll(targetLanguages);
        }
        if (!CollectionUtils.isEmpty(targetFileOrigins)) {
            i18nFileOriginRepository.saveAll(targetFileOrigins);
        }
        return filePaths;
    }


    @Transactional(rollbackFor = Exception.class)
    public void processI18nOriginForJsonBackend(I18nFileOrigin i18nFileOrigin) {
        if (null == i18nFileOrigin) {
            return;
        }
        if (!FileSuffixEnum.JSON.getName().equals(i18nFileOrigin.getSuffix()) || 1 != i18nFileOrigin.getType()) {
            return;
        }
        if (StringUtils.isBlank(i18nFileOrigin.getContent())) {
            log.info("文件内容为空，文件名:{}", i18nFileOrigin.getName());
            return;
        }
        List<I18nCode> i18nCodeList = i18nCodeRepository.findAllByModuleCode(i18nFileOrigin.getModuleCode());

        Map<String, I18nCode> existMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(i18nCodeList)) {
            i18nCodeList.forEach(r -> existMap.put(r.getCode(), r));
        }
        Map<String, Object> kvMap = Json2Map.jsonToMap(i18nFileOrigin.getContent());
        List<I18nKVLanguage> i18nKVLanguages = I18nKVLanguage.ofMap(kvMap, i18nFileOrigin.getLanguage(), i18nFileOrigin.getModuleCode());
        List<I18nCode> i18nCodes = CopyUtil.copyList(i18nKVLanguages, I18nCode.class);
        List<I18nCode> targetCodeLanguages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(i18nCodes)) {
            i18nCodes.forEach(r -> {
                I18nCode i18nCode = existMap.get(r.getCode());
                if (null != i18nCode) {
                    if (StringUtils.isNotBlank(r.getMessage()) && !r.getMessage().equals(i18nCode.getMessage())) {
                        i18nCode.setMessage(r.getMessage());
                        targetCodeLanguages.add(i18nCode);
                    }
                } else {
                    i18nCode = CopyUtil.copy(r, I18nCode.class);
                    targetCodeLanguages.add(i18nCode);
                }
            });
        }
        if (!CollectionUtils.isEmpty(targetCodeLanguages)) {
            i18nCodeRepository.saveAll(targetCodeLanguages);
        }
        I18nFileOrigin p = CopyUtil.copy(i18nFileOrigin, I18nFileOrigin.class);
        p.setModifyTs(System.currentTimeMillis());
        p.setProcessed(1);
        i18nFileOriginRepository.save(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processI18nOriginForJsonFrontend(I18nFileOrigin i18nFileOrigin) {
        if (null == i18nFileOrigin) {
            return;
        }
        if (!FileSuffixEnum.JSON.getName().equals(i18nFileOrigin.getSuffix()) || 2 != i18nFileOrigin.getType()) {
            log.info("不是json格式的文件，文件名:{}", i18nFileOrigin.getName());
            return;
        }
        if (StringUtils.isBlank(i18nFileOrigin.getContent())) {
            log.info("文件内容为空，文件名:{}", i18nFileOrigin.getName());
            return;
        }
        List<I18nCodeLanguage> moduleCodeAndLanguage = i18nCodeLanguageRepository.findAllByModuleCodeAndLanguage(i18nFileOrigin.getModuleCode(), i18nFileOrigin.getLanguage());
        Map<String, I18nCodeLanguage> existMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(moduleCodeAndLanguage)) {
            moduleCodeAndLanguage.forEach(r -> existMap.put(r.getCode(), r));
        }
        Map<String, Object> kvMap = Json2Map.jsonToMap(i18nFileOrigin.getContent());
        List<I18nKVLanguage> i18nKVLanguages = I18nKVLanguage.ofMap(kvMap, i18nFileOrigin.getLanguage(), i18nFileOrigin.getModuleCode());
        List<I18nCodeLanguage> i18nCodeLanguages = CopyUtil.copyList(i18nKVLanguages, I18nCodeLanguage.class);
        List<I18nCodeLanguage> targetCodeLanguages = new ArrayList<>();
        if (!CollectionUtils.isEmpty(i18nCodeLanguages)) {
            i18nCodeLanguages.forEach(r -> {
                I18nCodeLanguage i18nCodeLanguage = existMap.get(r.getCode());
                if (null != i18nCodeLanguage) {
                    if (StringUtils.isNotBlank(r.getMessage()) && !r.getMessage().equals(i18nCodeLanguage.getMessage())) {
                        i18nCodeLanguage.setMessage(r.getMessage());
                        targetCodeLanguages.add(i18nCodeLanguage);
                    }
                } else {
                    i18nCodeLanguage = CopyUtil.copy(r, I18nCodeLanguage.class);
                    targetCodeLanguages.add(i18nCodeLanguage);
                }
            });
        }
        if (!CollectionUtils.isEmpty(targetCodeLanguages)) {
            i18nCodeLanguageRepository.saveAll(targetCodeLanguages);
        }
        I18nFileOrigin p = CopyUtil.copy(i18nFileOrigin, I18nFileOrigin.class);
        p.setModifyTs(System.currentTimeMillis());
        p.setProcessed(1);
        i18nFileOriginRepository.save(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void compensateToLanguage() {
        List<I18nLanguage> i18nLanguages = i18nLanguageRepository.findAll();
        if (CollectionUtils.isEmpty(i18nLanguages)) {
            return;
        }
        Map<String, List<I18nLanguage>> i18nLanMap = i18nLanguages.stream().collect(Collectors.groupingBy(I18nLanguage::getModuleCode));
        Set<String> modules = i18nLanMap.keySet();
        List<I18nCodeLanguage> target = new ArrayList<>();
        for (String module : modules) {
            List<I18nCodeLanguage> i18nCodeLanguages = i18nCodeLanguageRepository.findAllByModuleCode(module);
            List<I18nCode> i18nCodes = i18nCodeRepository.findAllByModuleCode(module);
            List<I18nLanguage> nLanguages = i18nLanMap.get(module);
            Map<String, List<I18nCodeLanguage>> lanCodeLanMap = i18nCodeLanguages.stream().collect(Collectors.groupingBy(I18nCodeLanguage::getLanguage));
            for (I18nLanguage i18nLanguage : nLanguages) {
                String code = i18nLanguage.getCode();
                List<I18nCodeLanguage> codeLanguages = lanCodeLanMap.get(code);
                Set<String> existCodes = new HashSet<>();
                if (!CollectionUtils.isEmpty(codeLanguages)) {
                    codeLanguages.forEach(r -> existCodes.add(r.getCode()));
                }
                i18nCodes.forEach(r -> {
                    if (!existCodes.contains(r.getCode())) {
                        I18nCodeLanguage codeLanguage = new I18nCodeLanguage();
                        codeLanguage.setMessage(r.getMessage());
                        codeLanguage.setCode(r.getCode());
                        codeLanguage.setLanguage(code);
                        codeLanguage.setModuleCode(module);
                        target.add(codeLanguage);
                    }
                });
            }
        }
        if (!CollectionUtils.isEmpty(target)) {
            i18nCodeLanguageRepository.saveAll(target);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void compensateToCode() {
        List<I18nLanguage> i18nLanguages = i18nLanguageRepository.findAll();
        if (CollectionUtils.isEmpty(i18nLanguages)) {
            return;
        }
        Map<String, List<I18nLanguage>> i18nLanMap = i18nLanguages.stream().collect(Collectors.groupingBy(I18nLanguage::getModuleCode));
        Set<String> modules = i18nLanMap.keySet();
        List<I18nCode> target = new ArrayList<>();
        for (String module : modules) {
            List<I18nCodeLanguage> i18nCodeLanguages = i18nCodeLanguageRepository.findAllByModuleCodeAndLanguage(module, "zh_CN");
            Map<String, I18nCodeLanguage> i18nCodeLanguageMap = new HashMap<>();
            if (CollectionUtils.isEmpty(i18nCodeLanguages)) {
                continue;
            }
            i18nCodeLanguages.forEach(r -> i18nCodeLanguageMap.put(r.getCode(), r));
            List<I18nCode> i18nCodes = i18nCodeRepository.findAllByModuleCode(module);
            Map<String, I18nCode> i18nCodeMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(i18nCodes)) {
                i18nCodes.forEach(r -> i18nCodeMap.put(r.getCode(), r));
            }
            i18nCodeLanguages.forEach(r -> {
                I18nCode i18nCode = i18nCodeMap.get(r.getCode());
                if (null == i18nCode) {
                    I18nCode i18nC = new I18nCode();
                    i18nC.setMessage(r.getMessage());
                    i18nC.setCode(r.getCode());
                    i18nC.setModuleCode(r.getModuleCode());
                    target.add(i18nC);
                }
            });
        }
        if (!CollectionUtils.isEmpty(target)) {
            i18nCodeRepository.saveAll(target);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateApp(String appCode) {
        App app = appRepository.findFirstByCode(appCode);
        if (null == app) {
            return;
        }
        List<AppModule> appModules = appModuleRepository.findAllByAppCode(appCode);
        if (CollectionUtils.isEmpty(appModules)) {
            return;
        }
        List<I18nCodeLanguage> targetLans = new ArrayList<>();
        Set<String> notes = new HashSet<>();
        for (AppModule appModule : appModules) {
            List<I18nCodeLanguage> i18nCodeLanguages = i18nCodeLanguageRepository.findAllByModuleCode(appModule.getModuleCode());
            if (!CollectionUtils.isEmpty(i18nCodeLanguages)) {
                targetLans.addAll(i18nCodeLanguages);
                notes.add(appModule.getModuleCode());
            }
        }
        List<I18nFileTarget> target = new ArrayList<>();
        Map<String, I18nFileTarget> lanTargetFileMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(targetLans)) {
            Map<String, List<I18nCodeLanguage>> i18nCodeMap = targetLans.stream().collect(Collectors.groupingBy(I18nCodeLanguage::getLanguage));
            i18nCodeMap.keySet().forEach(r -> {
                I18nFileTarget i18nFileTarget = i18nFileTargetRepository.findFirstByAppCodeAndLanguageAndTypeOrderByCreateTsDesc(appCode, r, I18nFileTypeEnum.ALL.getCode());
                lanTargetFileMap.put(r, i18nFileTarget);
            });
            target.addAll(I18nConverter.toI18nFileTargetJson(app, i18nCodeMap, lanTargetFileMap, notes));
        }
        if (!CollectionUtils.isEmpty(target)) {
            i18nFileTargetRepository.saveAll(target);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void generateLatestTargetFile(String appCode) {
        App app = appRepository.findFirstByCode(appCode);
        if (null == app) {
            return;
        }
        List<I18nFileTarget> i18nFileTargets = i18nFileTargetRepository.findAllByAppCodeOrderByCreateTsDesc(appCode);
        if (CollectionUtils.isEmpty(i18nFileTargets)) {
            return;
        }
        Map<String, List<I18nFileTarget>> lanMap = i18nFileTargets.stream().collect(Collectors.groupingBy(I18nFileTarget::getLanguage));
        String outputDir = codeGeneratorProperties.getOutputDir();
        String path = outputDir + "/" + app.getCode() + "/i18n/" + System.currentTimeMillis();
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        Set<String> langs = lanMap.keySet();
        for (String lang : langs) {
            List<I18nFileTarget> fileTargets = lanMap.get(lang);
            I18nFileTarget i18nFileTarget = fileTargets.get(0);
            writeFile(path, i18nFileTarget);
        }
    }

    private void writeFile(String path, I18nFileTarget i18nFileTarget) {
        OutputStream os = null;
        try {
            File lanFile = new File(path + "/" + i18nFileTarget.getLanguage());
            if (!lanFile.exists()) {
                lanFile.mkdirs();
            }
            os = new FileOutputStream(new File(path + "/" + i18nFileTarget.getLanguage() + "/" + i18nFileTarget.getName()));
            os.write(i18nFileTarget.getContent().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != os) {
                try {
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
