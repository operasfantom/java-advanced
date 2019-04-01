package ru.ifmo.rain.yatcheniy.student;

import info.kgeorgiy.java.advanced.student.AdvancedStudentGroupQuery;
import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentGroupQuery, AdvancedStudentGroupQuery {
    private static final Comparator<Student> STUDENT_COMPARATOR =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .thenComparing(Student::getId);
    private static final Comparator<Group> GROUP_ASCENDING_NAME_COMPARATOR = Comparator.comparing(Group::getName);
    private static final Comparator<Group> GROUP_DESCENDING_NAME_COMPARATOR = GROUP_ASCENDING_NAME_COMPARATOR.reversed();

    private static String getFullName(Student s) {
        return String.format("%s %s", s.getFirstName(), s.getLastName());
    }

    private Stream<Student> getSortedStudents(Collection<Student> students) {
        return students.stream()
                .sorted(STUDENT_COMPARATOR);
    }

    private Stream<Group> getGroupsByNameStream(Collection<Student> students) {
        return getGroups(students)
                .sorted(GROUP_ASCENDING_NAME_COMPARATOR)
                .map(group -> new Group(group.getName(), sortStudentsByName(group.getStudents())));
    }

    private Stream<Student> findStudentsByGroup0(Collection<Student> students, String group) {
        return getSortedStudents(students)
                .filter(getPredicateByMember(Student::getGroup, group));
    }

    private <T, R> Predicate<T> getPredicateByMember(Function<? super T, ? extends R> keyExtractor, R key) {
        return student -> Objects.equals(key, keyExtractor.apply(student));
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsByNameStream(students)
                .collect(Collectors.toList());
    }

    private Stream<Group> getGroups(Collection<Student> student) {
        return student.stream()
                .collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .map(name -> new Group(name.getKey(), name.getValue()));
    }

    private <T, R> List<R> getMappedCollection(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    private <T> long getDistinctCount(Stream<T> stream) {
        return stream
                .distinct()
                .count();
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsByNameStream(students)
                .map(group -> new Group(group.getName(), sortStudentsById(group.getStudents())))
                .collect(Collectors.toList());
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getGroups(students)
                .max(Comparator.comparingInt((Group g) -> g.getStudents().size())
                        .thenComparing(GROUP_DESCENDING_NAME_COMPARATOR))
                .map(Group::getName)
                .orElse("");
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getGroups(students)
                .max(Comparator.comparingLong((Group group) ->
                        getDistinctCount(group.getStudents().stream().map(Student::getFirstName)))
                        .thenComparing(GROUP_DESCENDING_NAME_COMPARATOR))
                .map(Group::getName)
                .orElse("");
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getMappedCollection(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getMappedCollection(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getMappedCollection(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getMappedCollection(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.comparingInt(Student::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSortedStudents(students)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return getSortedStudents(students)
                .filter(getPredicateByMember(Student::getFirstName, name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return getSortedStudents(students)
                .filter(getPredicateByMember(Student::getLastName, name))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup0(students, group)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return findStudentsByGroup0(students, group)
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return students.stream()
                .max(Comparator.comparingLong((Student student) ->
                        getDistinctCount(students.stream()
                                .filter(getPredicateByMember(StudentDB::getFullName, getFullName(student)))
                                .map(Student::getGroup))
                ).thenComparing(StudentDB::getFullName))
                .map(StudentDB::getFullName)
                .orElse("");
    }
}
