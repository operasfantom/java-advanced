update_submodule:
	git submodule update --remote --merge

LIB_PATH=java-advanced-2019/lib
ARTIFACTS_PATH=java-advanced-2019/artifacts
INFO_BASE_MODULE_PATH=${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar
#01

test_walk:
	java -cp out/production/walk -p ${LIB_PATH};${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.implementor.jar -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.yatcheniy.walk.RecursiveWalk
#02
test_array:
	java -cp out/production/arrayset -p java-advanced-2019/lib;java-advanced-2019/artifacts -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.yatcheniy.arrayset.ArraySet
#03
test_student:
	java -cp out/production/student -p java-advanced-2019/lib;java-advanced-2019/artifacts -m info.kgeorgiy.java.advanced.student AdvancedStudentGroupQuery ru.ifmo.rain.yatcheniy.student.StudentDB
#04-06
RU_IMPLEMENTOR_SOURCE_PATH=src/modules/ru.ifmo.rain.yatcheniy.implementor
RU_IMPLEMENTOR_PACKAGE_PATH=$(RU_IMPLEMENTOR_SOURCE_PATH)/ru/ifmo/rain/yatcheniy/implementor
INFO_IMPLEMENTOR_MODULE_PATH=${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.implementor.jar;${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;${LIB_PATH}
RU_IMPLEMENTOR_JAR_PATH=out/artifacts/ru_ifmo_rain_yatcheniy_implementor_jar
JAVADOC_PATH=out/javadoc

build_classes:
	javac -d out/production/implementor -p ${INFO_IMPLEMENTOR_MODULE_PATH} ${RU_IMPLEMENTOR_PACKAGE_PATH}/*.java
	javac -d out/production/implementor -p ${INFO_IMPLEMENTOR_MODULE_PATH} src/modules/ru.ifmo.rain.yatcheniy.implementor/module-info.java
build_jar_module: build_classes
	jar --create --file ${RU_IMPLEMENTOR_JAR_PATH}/ru.ifmo.rain.yatcheniy.implementor.jar --main-class ru.ifmo.rain.yatcheniy.implementor.Implementor --module-version 1.0 -C out/production/ru.ifmo.rain.yatcheniy.implementor .
test_implementor: build_classes
	java -cp out/production/implementor -p ${INFO_IMPLEMENTOR_MODULE_PATH} -m info.kgeorgiy.java.advanced.implementor class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor_classes: build_jar_module
	java -cp out/production/implementor -p ${INFO_IMPLEMENTOR_MODULE_PATH} -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor: build_jar_module
	java -p ${INFO_IMPLEMENTOR_MODULE_PATH};${RU_IMPLEMENTOR_JAR_PATH} --add-modules ru.ifmo.rain.yatcheniy.implementor -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor
java_doc:
	javadoc -d ${JAVADOC_PATH} --source-path ${RU_IMPLEMENTOR_SOURCE_PATH} --module-path ${RU_IMPLEMENTOR_JAR_PATH};${INFO_IMPLEMENTOR_MODULE_PATH} --module ru.ifmo.rain.yatcheniy.implementor


#07
INFO_CONCURRENT_MODULE_PATH=${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.concurrent.jar;${ARTIFACTS_PATH}/info.kgeorgiy.java.advanced.base.jar;java-advanced-2019/lib
RU_CONCURRENT_JAR_PATH=out/artifacts/ru_ifmo_rain_yatcheniy_concurrent_jar
test_concurrent:
	java -p ${RU_CONCURRENT_JAR_PATH};${INFO_CONCURRENT_MODULE_PATH} --add-modules ru.ifmo.rain.yatcheniy.concurrent -m info.kgeorgiy.java.advanced.concurrent list ru.ifmo.rain.yatcheniy.concurrent.IterativeParallelism