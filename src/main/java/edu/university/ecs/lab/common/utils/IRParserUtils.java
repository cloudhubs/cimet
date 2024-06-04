package edu.university.ecs.lab.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.university.ecs.lab.common.models.MicroserviceSystem;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;


/** Utility class for parsing IR and delta files previously created. */
public class IRParserUtils {
  /** Gson parser */
  private static final Gson gson =
          new GsonBuilder().create();

  /**
   * Read in an IR (original or merged/new) file and parse it into a MsSystem object.
   *
   * @param irFileName the file path/name of the IR file to parse
   * @return the parsed MsSystem object
   * @throws IOException if an I/O error occurs
   */
  public static MicroserviceSystem parseIRSystem(String irFileName) {
    MicroserviceSystem microserviceSystem = null;
    try {
      Reader irReader = new FileReader(irFileName);
      microserviceSystem = gson.fromJson(irReader, MicroserviceSystem.class);
      irReader.close();
    } catch (FileNotFoundException e) {
      java.lang.System.err.println("IR File not Found: " + irFileName);
    } catch (IOException e) {
      java.lang.System.err.println("Error reading IR file: " + irFileName);
    }

    return microserviceSystem;
  }

//  /**
//   * Read in a delta file and parse it into a SystemChange object.
//   *
//   * @param deltaFileName the file path/name of the delta file to parse
//   * @return the parsed SystemChange object
//   * @throws IOException if an I/O error occurs
//   */
//  public static SystemChange parseSystemChange(String deltaFileName) {
//    SystemChangeDTO systemChangeDto = null;
//    try {
//      Reader deltaReader = new FileReader(deltaFileName);
//      systemChangeDto = gson.fromJson(deltaReader, SystemChangeDTO.class);
//      deltaReader.close();
//    } catch (FileNotFoundException e) {
//      java.lang.System.err.println("Delta file not Found: " + deltaFileName);
//    } catch (IOException e) {
//      java.lang.System.err.println("Error reading delta file: " + deltaFileName);
//    }
//
//    return systemChangeDto.toSystemChange();
//  }
}
