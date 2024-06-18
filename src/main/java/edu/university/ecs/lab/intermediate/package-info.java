/**
 * This package contains the classes and sub-packages responsible for the intermediate representation extraction and merging processes.
 *
 * <p>The main sub-packages within this package are:</p>
 *   - {@link  edu.university.ecs.lab.intermediate.create}: Includes the classes responsible for generating the intermediate representation from the source code.
 *   - {@link  edu.university.ecs.lab.intermediate.merge}: Includes the classes responsible for merging the intermediate representation with delta changes.
 *   - {@link  edu.university.ecs.lab.intermediate.utils}: Includes utility classes used throughout the intermediate representation processes.
 * </ul>
 *
 * <p>The intermediate extraction process involves cloning remote services, scanning through each local repository to extract REST endpoints and calls, and writing the extracted data into an intermediate representation.</p>
 *
 * <p>The intermediate merging process involves taking an existing intermediate representation and applying changes based on delta files to generate an updated intermediate representation.</p>
 */
package edu.university.ecs.lab.intermediate;
