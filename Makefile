submodule_update:
	git submodule update --remote --merge
test_walk:
	java -cp "out\production\walk" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.walk RecursiveWalk ru.ifmo.rain.yatcheniy.walk.RecursiveWalk
test_array:
	java -cp "out\production\arrayset" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.arrayset NavigableSet ru.ifmo.rain.yatcheniy.arrayset.ArraySet
test_student:
	java -cp "out\production\student" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.student AdvancedStudentGroupQuery ru.ifmo.rain.yatcheniy.student.StudentDB
test_implementor:
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor class ru.ifmo.rain.yatcheniy.implementor.Implementor
#build_jar_module:
#	jar cvfm out\artifacts\ru_ifmo_rain_yatcheniy_implementor_jar\ru.ifmo.rain.yatcheniy.implementor.jar src\modules\ru.ifmo.rain.yatcheniy.implementor\META-INF\MANIFEST.MF C:\Users\jetbrains\Documents\IFMO\Java\java-advanced\out\production\ru.ifmo.rain.yatcheniy.implementor\ru\ifmo\rain\yatcheniy\implementor
test_jar_implementor:
	java -cp "out\production\implementor" -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor2:
	java -cp C:\Users\jetbrains\Documents\IFMO\Java\java-advanced\out\artifacts\ru_ifmo_rain_yatcheniy_implementor_jar\ru.ifmo.rain.yatcheniy.implementor.jar -p "java-advanced-2019\lib;java-advanced-2019\artifacts" -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor
test_jar_implementor3:
	java -p "java-advanced-2019\lib;java-advanced-2019\artifacts;out\artifacts\ru_ifmo_rain_yatcheniy_implementor_jar" -m info.kgeorgiy.java.advanced.implementor jar-class ru.ifmo.rain.yatcheniy.implementor.Implementor