//package edu.university.ecs.lab.temporal;
//
//import edu.university.ecs.lab.common.models.ir.JClass;
//import edu.university.ecs.lab.common.models.ir.Microservice;
//import edu.university.ecs.lab.common.models.ir.MicroserviceSystem;
//import edu.university.ecs.lab.common.utils.JsonReadWriteUtils;
//
//import java.util.Objects;
//
//public class ComparisonTest {
//    public static void main(String[] args) {
//        MicroserviceSystem ms1 = JsonReadWriteUtils.readFromJSON("./output/IR.json", MicroserviceSystem.class);
//        MicroserviceSystem ms2 = JsonReadWriteUtils.readFromJSON("./output/IRCompare.json", MicroserviceSystem.class);
//
//        for (Microservice m1 : ms1.getMicroservices()) {
//            boolean microserviceFound = false;
//            outer1:
//            {
//                for (Microservice m2 : ms2.getMicroservices()) {
//                    if (m1.getName().equals(m2.getName())) {
//                        System.out.println(m1.getName() + " " + Objects.equals(m1, m2));
//
//                        for (JClass jClass1 : m1.getClasses()) {
//                            boolean classFound = false;
//                            outer2:
//                            {
//                                for (JClass jClass2 : m2.getClasses()) {
//                                    if (jClass1.get().equals(jClass2.getClassPath())) {
//                                        System.out.println(jClass1.getClassPath() + " " + Objects.equals(jClass1, jClass2));
//                                        classFound = true;
//                                        break outer2;
//                                    }
//                                }
//                            }
//                            if (!classFound) {
//                                System.out.println(jClass1.getClassPath() + " NO MATCH FOUND!");
//                            }
//
//                        }
//                        microserviceFound = true;
//                        break outer1;
//                    }
//                }
//            }
//            if (!microserviceFound) {
//                System.out.println(m1.getName() + " NO MATCH FOUND!");
//            }
//        }
//
//        System.out.println(Objects.equals(ms1, ms2));
//    }
//}
