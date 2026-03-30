package com.kaijie.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 代码生成器（运行此类的 main 方法生成代码）
 * 注意：请在运行前将 username/password 填写为你的数据库账号密码
 */
public class CodeGenerator {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/family_doctor_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";

        List<String> tables = Arrays.asList(
                "sys_user",
                "ehr_patient_profile",
                "ehr_medical_history",
                "doc_contract",
                "cms_article",
                "im_consultation_record",
                "im_chat_message",
                "med_drug_library",
                "med_prescription",
                "med_prescription_item",
                "health_daily_record",
                "diet_exercise_plan",
                "doc_schedule",
                "doc_appointment",
                "health_vital_signs"
        );

        // 输出目录
        String projectPath = System.getProperty("user.dir");
        String javaOutDir = Paths.get(projectPath, "src", "main", "java").toFile().getAbsolutePath();
        String xmlOutDir = Paths.get(projectPath, "src", "main", "resources", "mapper").toFile().getAbsolutePath();

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("kaijie")
                        .enableSwagger()
                        .fileOverride()
                        .outputDir(javaOutDir)
                )
                .packageConfig(builder -> builder
                        .parent("com.kaijie") // 设置包名
                )
                .strategyConfig(builder -> builder
                        .addInclude(tables) // 需要生成的表
                        .addTablePrefix("sys_", "ehr_", "doc_", "cms_", "im_", "med_") // 过滤表前缀
                        .entityBuilder()
                            .enableLombok() // 开启 Lombok
                            .logicDeleteColumnName("is_deleted") // 逻辑删除字段名
                            .enableTableFieldAnnotation() // 开启 @TableField 注解
                            .build()
                        .controllerBuilder()
                            .enableRestStyle() // @RestController
                            .build()
                        .mapperBuilder()
                            .enableMapperAnnotation()
                            .build()
                )
                .templateEngine(new FreemarkerTemplateEngine()) // 使用 freemarker 模板引擎
                .execute();

        System.out.println("代码生成完成，输出路径：\n Java -> " + javaOutDir + "\n XML -> " + xmlOutDir);
    }
}
