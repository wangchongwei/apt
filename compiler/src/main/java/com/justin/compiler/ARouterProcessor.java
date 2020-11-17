package com.justin.compiler;

import com.google.auto.service.AutoService;
import com.justin.annotationprocessor.ARouter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;



@AutoService(Processor.class) // 启用服务 google 自动服务，一直监控
@SupportedAnnotationTypes({"com.justin.annotationprocessor.ARouter"}) // 注解 包名.类名
@SupportedSourceVersion(SourceVersion.RELEASE_8) // 环境的版本

@SupportedOptions("student") // 接收在app/build.gradle中声明的参数
public class ARouterProcessor extends AbstractProcessor {

    private Elements elementTool;

    private Messager messager;

    private Filer filer;

    private Types typeTool;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        // 获取在app/build.gradle中申明的参数
        String value = processingEnvironment.getOptions().get("student");
        messager.printMessage(Diagnostic.Kind.NOTE, "=========>" + value);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 此时会运行两次，一次执行，一次检查
        messager.printMessage(Diagnostic.Kind.NOTE, "=====> compiler is running");

        if(set.isEmpty()) {
            // 使用注解的类集合为空
            return false;
        }

        // 生成一个固定的java代码
        /**
         模块一
         package com.example.helloworld;

         public final class HelloWorld {

         public static void main(String[] args) {
         System.out.println("Hello, JavaPoet!");
         }

         public int add(int a, int b){
         return a + b;
         }
         }
         */

        // javapoet生成java文件的方式时，OOP思维，先生成函数，再生成类，最后生成包
        // 1 生成函数
        MethodSpec methodSpec = MethodSpec.methodBuilder("main") // 构造一个函数， 函数名
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC) // public 、static
                .returns(void.class) // 返回值类型
                .addParameter(String[].class, "args") // 入参类型
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")// 函数执行语句 不需要添加分号
                .addStatement("$T.out.println($S)", System.class, "Hello, agent!")
                .build();

        MethodSpec methodSpec1 = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addParameter(int.class, "a")
                .addParameter(int.class, "b")
                .addStatement("return 5")
                .build();

        // 2 生成类
        TypeSpec myClass = TypeSpec.classBuilder("HelloWorld") // 构造一个类，类名
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL) // 添加申明 public、final
                .addMethod(methodSpec) // 绑定函数
                .addMethod(methodSpec1)
                .build();

        // 3、生成包
        JavaFile myPackage = JavaFile.builder("com.example.helloworld", myClass).build();

        try {
            myPackage.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "=====> 创建HelloWorld类失败,异常原因：" + e.getMessage());
        }


        // 获取被 ARouter注解的 "类节点信息"
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for(Element element : elements) {
            // 动态生成java代码

            // 获取组件class的包路径
            String packageName = elementTool.getPackageOf(element).getQualifiedName().toString();
            // 获取注解的类名
            String className = element.getSimpleName().toString();


            // 获取到注解以及注解时的参数
            ARouter aRouter = element.getAnnotation(ARouter.class);
            String path = aRouter.path();

            /**
             模板：
             public class MainActivity3$$$$$$$$$ARouter {
             public static Class findTargetClass(String path) {
             return path.equals("/app/MainActivity3") ? MainActivity3.class : null;
             }
             }
             */

            // 1、方法
            MethodSpec method = MethodSpec.methodBuilder("findTargetClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(Class.class)
                    .addParameter(String.class, "path")
                    .addStatement("return path.equals($S) ? $T.class : null",
                            path,
                            ClassName.get((TypeElement) element) // 获取注解类的class对象
                    )
                    .build();

            // 2 类
            TypeSpec mineClass = TypeSpec.classBuilder(className + "$$$$$$$$$ARouter")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(method)
                    .build();
            // 3 包
            JavaFile minePackage = JavaFile
                    .builder(packageName, mineClass)
                    .build();

            try {
                minePackage.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "创建" + className + "类失败");
            }
        }

        return false;
    }



}