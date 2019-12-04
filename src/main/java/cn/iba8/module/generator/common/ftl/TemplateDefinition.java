package cn.iba8.module.generator.common.ftl;

import cn.iba8.module.generator.common.enums.DataTypeMappingEnum;
import cn.iba8.module.generator.common.enums.TemplateTypeEnum;
import cn.iba8.module.generator.common.util.ColumnNameUtil;
import cn.iba8.module.generator.common.util.FileNameUtil;
import cn.iba8.module.generator.common.util.TemplateDataUtil;
import cn.iba8.module.generator.repository.entity.CodeTemplate;
import cn.iba8.module.generator.repository.entity.MetaDatabaseTable;
import cn.iba8.module.generator.repository.entity.MetaDatabaseTableColumn;
import cn.iba8.module.generator.repository.entity.Module;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateDefinition {

    @Data
    public static class TemplateFileBean implements Serializable {

        private String filename;

        private String fileDir;

        private String content;

        public static List<TemplateFileBean> of(TableColumnBean tableColumnBean, List<CodeTemplate> codeTemplates) {
            List<TemplateFileBean> target = new ArrayList<>();
            Module module = tableColumnBean.getModule();
            String packagePrefix = module.getPackageName();
            tableColumnBean.toJava();
            for (CodeTemplate r : codeTemplates) {
                TemplateTypeEnum templateTypeEnum = TemplateTypeEnum.ofName(r.getType());
                String template = r.getTemplate();
                switch (templateTypeEnum) {
                    case REPOSITORY_ENTITY:
                        target.add(TemplateFileBean.ofRepositoryEntity(packagePrefix, template, tableColumnBean));
                        break;
                    case REPOSITORY_DAO:
                        target.add(TemplateFileBean.ofRepositoryDao(packagePrefix, template, tableColumnBean));
                        break;
                    case SERVICE:
                        target.add(TemplateFileBean.ofServic(packagePrefix, template, tableColumnBean));
                        break;
                    case SERVICE_BIZ:
                        target.add(TemplateFileBean.ofServiceBiz(packagePrefix, template, tableColumnBean));
                        break;
                    case SERVICE_IMPL:
                        target.add(TemplateFileBean.ofServiceImpl(packagePrefix, template, tableColumnBean));
                        break;
                    case API:
                        target.add(TemplateFileBean.ofApi(packagePrefix, template, tableColumnBean));
                        break;
                    case API_IMPL:
                        target.add(TemplateFileBean.ofApiImpl(packagePrefix, template, tableColumnBean));
                        break;
                    case VUE_INDEX:
                        target.add(TemplateFileBean.ofApiVueIndex(packagePrefix, template, tableColumnBean));
                        break;
                    case VUE_ADD_OR_UPDATE:
                        target.add(TemplateFileBean.ofApiVueAddOrUpdate(packagePrefix, template, tableColumnBean));
                        break;
                    default:
                        break;
                }
            }
            return target;
        }

        public static TemplateFileBean ofRepositoryEntity(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()));
            templateFileBean.setFileDir(packagePrefix + ".repository.entity");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.REPOSITORY_ENTITY));
            return templateFileBean;
        }

        public static TemplateFileBean ofRepositoryDao(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.REPOSITORY_DAO.getName());
            templateFileBean.setFileDir(packagePrefix + ".repository.dao");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.REPOSITORY_DAO));
            return templateFileBean;
        }

        public static TemplateFileBean ofServiceBiz(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.SERVICE_BIZ.getName());
            templateFileBean.setFileDir(packagePrefix + ".service.biz");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.SERVICE_BIZ));
            return templateFileBean;
        }

        public static TemplateFileBean ofServic(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.SERVICE.getName());
            templateFileBean.setFileDir(packagePrefix + ".service");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.SERVICE));
            return templateFileBean;
        }

        public static TemplateFileBean ofServiceImpl(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.SERVICE_IMPL.getName());
            templateFileBean.setFileDir(packagePrefix + ".service.impl");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.SERVICE_IMPL));
            return templateFileBean;
        }

        public static TemplateFileBean ofApi(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.API.getName());
            templateFileBean.setFileDir(packagePrefix + ".api");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.API));
            return templateFileBean;
        }

        public static TemplateFileBean ofApiImpl(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()) + TemplateTypeEnum.API_IMPL.getName());
            templateFileBean.setFileDir(packagePrefix + ".controller");
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, TemplateTypeEnum.API_IMPL));
            return templateFileBean;
        }



        public static TemplateFileBean ofApiRequest(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            return templateFileBean;
        }

        public static TemplateFileBean ofApiResponse(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            return templateFileBean;
        }

        public static TemplateFileBean ofApiExcel(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            return templateFileBean;
        }

        public static TemplateFileBean ofApiVueIndex(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            return templateFileBean;
        }

        public static TemplateFileBean ofApiVueAddOrUpdate(String packagePrefix, String template, TableColumnBean tableColumnBean) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            return templateFileBean;
        }

        public static TemplateFileBean of(String packagePrefix, String template, TableColumnBean tableColumnBean, String suffix, TemplateTypeEnum templateTypeEnum) {
            TemplateFileBean templateFileBean = new TemplateFileBean();
            MetaDatabaseTable metaDatabaseTable = tableColumnBean.getMetaDatabaseTable();
            templateFileBean.setFilename(FileNameUtil.getRepositoryEntityName(metaDatabaseTable.getTableName()));
            templateFileBean.setFileDir(packagePrefix + suffix);
            templateFileBean.setContent(TemplateDataUtil.getContent(packagePrefix, template, tableColumnBean, templateTypeEnum));
            return templateFileBean;
        }
    }

    @Data
    public static class TableColumnBean implements Serializable {
        private Module module;
        private MetaDatabaseTable metaDatabaseTable;
        private List<MetaDatabaseTableColumn> metaDatabaseTableColumns = new ArrayList<>();

        public static TableColumnBean of(Module module, MetaDatabaseTable metaDatabaseTable, List<MetaDatabaseTableColumn> metaDatabaseTableColumns) {
            TableColumnBean tableColumnBean = new TableColumnBean();
            tableColumnBean.setMetaDatabaseTable(metaDatabaseTable);
            tableColumnBean.setModule(module);
            List<MetaDatabaseTableColumn> idList = new ArrayList<>();
            List<MetaDatabaseTableColumn> otherList = new ArrayList<>();
            for (MetaDatabaseTableColumn metaDatabaseTableColumn : metaDatabaseTableColumns) {
                if (metaDatabaseTableColumn.isPrimaryKey()) {
                    if (!"String".equals(metaDatabaseTableColumn.getColumnType())) {
                        metaDatabaseTableColumn.setColumnType("Long");
                    }
                    idList.add(metaDatabaseTableColumn);
                } else {
                    otherList.add(metaDatabaseTableColumn);
                }
            }
            idList.addAll(otherList);
            tableColumnBean.setMetaDatabaseTableColumns(idList);
            return tableColumnBean;
        }

        public void toJava() {
            for (MetaDatabaseTableColumn metaDatabaseTableColumn : metaDatabaseTableColumns) {
                metaDatabaseTableColumn.setColumnName(ColumnNameUtil.toJavaField(metaDatabaseTableColumn.getColumnName()));
                metaDatabaseTableColumn.setColumnType(DataTypeMappingEnum.mysqlTypeToJavaType(metaDatabaseTableColumn.getColumnType()));
            }
        }
    }

}
